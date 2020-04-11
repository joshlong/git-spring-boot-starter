package com.joshlong.git;

public interface GitTemplate {

	void execute(GitCallback gitCallback);

	void executeAndPush(GitCallback callback);

}
