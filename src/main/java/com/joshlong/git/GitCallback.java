package com.joshlong.git;

import org.eclipse.jgit.api.Git;

public interface GitCallback {

	void execute(Git g) throws Exception;

}
