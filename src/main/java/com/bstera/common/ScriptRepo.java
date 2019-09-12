package com.bstera.common;


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

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Repository;
import java.io.File;
import java.util.Collection;

/**
 * @author kcmvp 2019/9/10
 */
public class ScriptRepo {

	private static final String URI = "https://github.com/kcmvp/sbt-maven-groovy.git";

	public static final File DEFAULT_DIR = new File(System.getProperty("user.home") + File.separator + ".sbt-groovy");


	public static File getScript(String name, boolean force) throws Exception {
		if (!DEFAULT_DIR.exists()) {
			DEFAULT_DIR.mkdirs();
			Git.cloneRepository().setURI(URI).setDirectory(DEFAULT_DIR).call();
		}
		Sync(force);
		return new File(DEFAULT_DIR, name);
	}

	public static void Sync(boolean force) throws Exception {
		if (force) {
			Repository rep = new FileRepository(new File(DEFAULT_DIR, ".git"));
			Git git = new Git(rep);
			Collection<String> changed = git.status().call().getUncommittedChanges();
			if (!changed.isEmpty())
				git.reset().setMode(ResetCommand.ResetType.HARD).call();
			git.pull().setRemote("origin").call();
		}

	}
}