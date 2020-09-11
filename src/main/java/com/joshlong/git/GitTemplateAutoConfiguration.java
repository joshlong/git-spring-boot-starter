package com.joshlong.git;

import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.net.URI;

@Log4j2
@Configuration
@EnableConfigurationProperties(GitProperties.class)
@ConditionalOnProperty(name = GitProperties.GIT_PROPERTIES_ROOT + ".enabled", havingValue = "true",
		matchIfMissing = true)
public class GitTemplateAutoConfiguration {

	@Configuration
	@ConditionalOnProperty(name = GitProperties.GIT_PROPERTIES_ROOT + ".online", havingValue = "false")
	public static class OfflineGitConfiguration {

		@Log4j2
		private static class NoOpGitTemplate implements GitTemplate {

			private String name = NoOpGitTemplate.class.getName();

			@Override
			public void execute(GitCallback gitCallback) {
				log.info(name + "#execute(GitCallback)");
			}

			@Override
			public void executeAndPush(GitCallback callback) {
				log.info(name + "#executeAndPush(GitCallback)");
			}

		}

		@Bean
		GitTemplate gitTemplate() {
			return new NoOpGitTemplate();
		}

	}

	@Configuration
	@ConditionalOnProperty(name = "git.online", havingValue = "true", matchIfMissing = true)
	public static class OnlineGitConfiguration {

		@Bean
		@ConditionalOnBean(Git.class)
		@ConditionalOnMissingBean
		GitTemplate gitService(Git git, PushCommandCreator commandCreator) {
			return new DefaultGitTemplate(git, commandCreator);
		}

		private static void reset(File directory) {
			if (directory.exists()) {
				FileSystemUtils.deleteRecursively(directory);
			}
		}

		@Configuration
		@ConditionalOnProperty(name = "git.ssh.enabled", havingValue = "true")
		public static class SshConfig {

			@Bean
			@ConditionalOnMissingBean
			TransportConfigCallback transportConfigCallback(SshSessionFactory sshSessionFactory) {
				return GitUtils.createSshTransportConfigCallback(sshSessionFactory);
			}

			@Bean
			@ConditionalOnMissingBean
			SshSessionFactory sshSessionFactory(GitProperties properties) {
				var pw = properties.getSsh().getPassword();
				return GitUtils.createSshSessionFactory(pw);
			}

			@Bean
			@ConditionalOnMissingBean(Git.class)
			Git git(GitProperties gsp, TransportConfigCallback transportConfigCallback) throws GitAPIException {
				return GitUtils.createLocalSshGitRepository(URI.create(gsp.getUri()), gsp.getLocalCloneDirectory(),
						transportConfigCallback);
			}

			@Bean
			PushCommandCreator commandCreator(TransportConfigCallback transportConfigCallback) {
				return GitUtils.createSshPushCommandCreator(transportConfigCallback);
			}

		}

		@Log4j2
		@Configuration
		@ConditionalOnProperty(name = GitProperties.GIT_PROPERTIES_ROOT + ".http.enabled", havingValue = "true")
		public static class HttpConfig {

			@Bean
			@SneakyThrows
			@ConditionalOnMissingBean
			Git git(GitProperties gsp) {
				return GitUtils.createLocalHttpGitRepository(URI.create(gsp.getUri()), gsp.getLocalCloneDirectory());
			}

			@Bean
			@ConditionalOnMissingBean
			PushCommandCreator httpPushCommandCreator(GitProperties gsp) {
				var http = gsp.getHttp();
				return GitUtils.createHttpPushCommandCreator(http.getUsername(), http.getPassword());
			}

		}

	}

}