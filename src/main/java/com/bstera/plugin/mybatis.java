package com.bstera.plugin;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.bstera.common.ScriptRepo;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.codehaus.gmavenplus.model.Version;
import org.codehaus.gmavenplus.mojo.AbstractToolsMojo;
import org.codehaus.gmavenplus.util.NoExitSecurityManager;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;

import static org.codehaus.gmavenplus.util.ReflectionUtils.findConstructor;
import static org.codehaus.gmavenplus.util.ReflectionUtils.findMethod;
import static org.codehaus.gmavenplus.util.ReflectionUtils.invokeConstructor;
import static org.codehaus.gmavenplus.util.ReflectionUtils.invokeMethod;


@Mojo(name = "mybatis", requiresDependencyResolution = ResolutionScope.TEST, threadSafe = true)
public class mybatis extends AbstractToolsMojo {

	/**
	 * Groovy 1.7.0 version.
	 */
	protected static final Version GROOVY_1_7_0 = new Version(1, 7, 0);


	@Parameter(defaultValue = "schema")
	protected String action;

	@Parameter
	protected String entity;

	/**
	 * The encoding of script files.
	 *
	 * @since 1.0-beta-2
	 */
	@Parameter(defaultValue = "${project.build.sourceEncoding}")
	protected String sourceEncoding;

	/**
	 * Executes this mojo.
	 *
	 * @throws MojoExecutionException If an unexpected problem occurs (causes a "BUILD ERROR" message to be displayed)
	 */
	public void execute() throws MojoExecutionException {
		doExecute();
	}


	/**
	 * Does the actual execution.
	 *
	 * @throws MojoExecutionException If an unexpected problem occurs (causes a "BUILD ERROR" message to be displayed)
	 */
	protected synchronized void doExecute() throws MojoExecutionException {
		try {
			setupClassWrangler(project.getTestClasspathElements(), useSharedClassLoader);
		} catch (MalformedURLException e) {
			throw new MojoExecutionException("Unable to add project test dependencies to classpath.", e);
		} catch (DependencyResolutionRequiredException e) {
			throw new MojoExecutionException("Test dependencies weren't resolved.", e);
		}

		logPluginClasspath();
		classWrangler.logGroovyVersion(mojoExecution.getMojoDescriptor().getGoal());

		try {
			getLog().debug("Project test classpath:\n" + project.getTestClasspathElements());
		} catch (DependencyResolutionRequiredException e) {
			getLog().warn("Unable to log project test classpath", e);
		}

		if (groovyVersionSupportsAction()) {
			final SecurityManager sm = System.getSecurityManager();
			try {
				if (!allowSystemExits) {
					System.setSecurityManager(new NoExitSecurityManager());
				}

				// get classes we need with reflection
				Class<?> groovyShellClass = classWrangler.getClass("groovy.lang.GroovyShell");

				// create a GroovyShell to run scripts in
				Object shell = setupShell(groovyShellClass);

				// run the scripts
				executeScripts(groovyShellClass, shell);
			} catch (ClassNotFoundException e) {
				throw new MojoExecutionException("Unable to get a Groovy class from classpath (" + e.getMessage() + "). Do you have Groovy as a compile dependency in your project or the plugin?", e);
			} catch (InvocationTargetException e) {
				throw new MojoExecutionException("Error occurred while calling a method on a Groovy class from classpath.", e);
			} catch (InstantiationException e) {
				throw new MojoExecutionException("Error occurred while instantiating a Groovy class from classpath.", e);
			} catch (IllegalAccessException e) {
				throw new MojoExecutionException("Unable to access a method on a Groovy class from classpath.", e);
			} finally {
				if (!allowSystemExits) {
					System.setSecurityManager(sm);
				}
			}
		} else {
			getLog().error("Your Groovy version (" + classWrangler.getGroovyVersionString() + ") doesn't support script execution. The minimum version of Groovy required is " + minGroovyVersion + ". Skipping script execution.");
		}
	}

	/**
	 * Instantiates a new groovy.lang.GroovyShell object.
	 *
	 * @param groovyShellClass the groovy.lang.GroovyShell class
	 * @return a new groovy.lang.GroovyShell object
	 * @throws InvocationTargetException when a reflection invocation needed for shell configuration cannot be completed
	 * @throws IllegalAccessException    when a method needed for shell configuration cannot be accessed
	 * @throws InstantiationException    when a class needed for shell configuration cannot be instantiated
	 * @throws ClassNotFoundException    when a class needed for shell configuration cannot be found
	 */
	protected Object setupShell(final Class<?> groovyShellClass) throws InvocationTargetException, IllegalAccessException, InstantiationException, ClassNotFoundException {
		Object shell;
		if (sourceEncoding != null) {
			Class<?> compilerConfigurationClass = classWrangler.getClass("org.codehaus.groovy.control.CompilerConfiguration");
			Object compilerConfiguration = invokeConstructor(findConstructor(compilerConfigurationClass));
			invokeMethod(findMethod(compilerConfigurationClass, "setSourceEncoding", String.class), compilerConfiguration, sourceEncoding);
			shell = invokeConstructor(findConstructor(groovyShellClass, compilerConfigurationClass), compilerConfiguration);
		} else {
			shell = invokeConstructor(findConstructor(groovyShellClass));
		}
		initializeProperties();
		Method setProperty = findMethod(groovyShellClass, "setProperty", String.class, Object.class);
		if (bindPropertiesToSeparateVariables) {
			for (Object k : properties.keySet()) {
				invokeMethod(setProperty, shell, k, properties.get(k));
			}
		} else {
			invokeMethod(setProperty, shell, "properties", properties);
		}

		return shell;
	}

	/**
	 * Executes the configured scripts.
	 *
	 * @param groovyShellClass the groovy.lang.GroovyShell class
	 * @param shell            a groovy.lag.GroovyShell object
	 * @throws InvocationTargetException when a reflection invocation needed for script execution cannot be completed
	 * @throws IllegalAccessException    when a method needed for script execution cannot be accessed
	 * @throws MojoExecutionException    when an exception occurred during script execution (causes a "BUILD ERROR" message to be displayed)
	 */
	protected void executeScripts(final Class<?> groovyShellClass, final Object shell) throws InvocationTargetException, IllegalAccessException, MojoExecutionException {
		try {

			String className = this.getClass().getSimpleName();
			File scriptFile = new File(ScriptRepo.DEFAULT_DIR+File.separator+className, action + ".groovy");
			if (scriptFile.isFile()) {
				getLog().info("Running Groovy script from " + scriptFile.getCanonicalPath() + ".");
				Method evaluateFile = findMethod(groovyShellClass, "evaluate", File.class);
				invokeMethod(evaluateFile, shell, scriptFile);
			} else {
			}
		} catch (IOException ioe) {
			throw new MojoExecutionException("An Exception occurred while executing script ", ioe);
		}
	}

}
