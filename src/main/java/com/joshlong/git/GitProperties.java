package com.joshlong.git;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.File;

@Data
@ConfigurationProperties(GitProperties.GIT_PROPERTIES_ROOT)
public class GitProperties {

	public static final String GIT_PROPERTIES_ROOT = "git";
	private boolean disabled;
	private String charset;
	private File localCloneDirectory = new File(System.getProperty("user.home"), "git-clone");
	private String uri;
	private final Ssh ssh = new Ssh();
	private final Http http = new Http();
	private boolean online = true;

	@Data
	public static class Ssh {
		private boolean enabled;
		private String password;
	}

	@Data
	public static class Http {
		private String username = null, password = "";
		private boolean enabled;
	}
}