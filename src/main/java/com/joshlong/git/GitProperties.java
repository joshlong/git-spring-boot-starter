package com.joshlong.git;


import lombok.Data;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;

import java.io.File;
import java.net.URI;


@Data
@ConfigurationProperties (GitProperties.GIT_PROPERTIES_ROOT)
public class GitProperties {

	public static final String GIT_PROPERTIES_ROOT = "git";

	private boolean disabled;

	private String charset;

	private URI apiServerUrl;

	private final Sql sql = new Sql();

	private final Templates templates = new Templates();

	private final Output output = new Output();

	private final Launcher launcher = new Launcher();

	private final Git git = new Git();

	@Data
	public static class Git {

		private File localCloneDirectory = new File(System.getProperty("user.home"), "blog-clone");

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

	@Data
	public static class Output {

		private File items, pages, gitClone;

	}

	@Data
	public static class Templates {

		private Resource episodeTemplate, pageChromeTemplate, yearTemplate;

	}

	@Data
	public static class Sql {

		private String loadPodcasts;

		private String loadLinks;

		private String loadMedia;

	}

	@Data
	public static class Launcher {

		private String requestsQueue = "site-generator-requests-queue";

		private String requestsExchange = this.requestsQueue;

		private String requestsRoutingKey = this.requestsQueue;

	}

}