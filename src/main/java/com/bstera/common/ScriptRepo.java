package com.bstera.common;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Repository;

import java.io.File;

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
		} else if (force){
			Repository rep = new FileRepository(new File(DEFAULT_DIR, ".git"));
			Git git = new Git(rep);
			git.pull().setRemote("origin").call();
		}
		return new File(DEFAULT_DIR, name);
	}
}