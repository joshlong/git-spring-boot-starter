package com.joshlong.git;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jgit.api.Git;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

public class DefaultGitTemplate implements GitTemplate {

	private final Git git;

	private final PushCommandCreator commandCreator;

	private final Log log = LogFactory.getLog(getClass());

	public DefaultGitTemplate(Git git, PushCommandCreator commandCreator) {
		this.git = git;
		this.commandCreator = commandCreator;
		Assert.notNull(this.git, "the " + Git.class.getName() + " reference can't be null");
		Assert.notNull(this.commandCreator, "the " + PushCommandCreator.class.getName() + " reference can't be null");
	}

	@Override
	public void execute(GitCallback gitCallback) {
		try {
			gitCallback.execute(this.git);
		}
		catch (Exception ex) {
			log.error(ex);
			ReflectionUtils.rethrowRuntimeException(ex);
		}
	}

	@Override
	public void executeAndPush(GitCallback callback) {
		try {
			execute(callback);
		}
		finally {
			try {
				this.commandCreator.createPushCommand(this.git).call();
			}
			catch (Throwable e) {
				log.error(e);
				ReflectionUtils.rethrowRuntimeException(e);
			}
		}
	}

}
