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
