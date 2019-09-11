package com.bstera.plugin;

import com.bstera.common.ScriptRepo;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

/**
 * @author kcmvp 2019/9/11
 */
@Mojo(name = "info", requiresDependencyResolution = ResolutionScope.TEST, threadSafe = true)
public class Info extends AbstractMojo {
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		getLog().info(">>> sbt usage ");
		getLog().info(">>> 1: sbt:mybatis ** generate schema from entity **");
		getLog().info(">>> 2: sbt:mybatis -Daction=mapper -Dentity=xxx.class ** generate mybatis mapper filers **");
		getLog().info(">>> 3: sbt:mybatis -Daction=index ** generate index according to queries **");
		try {
			ScriptRepo.Sync(true);
		} catch (Exception e) {
			getLog().error("failed to sync the scripts ......", e);
		}
	}
}
