package com.joshlong.git;

import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.*;
import org.eclipse.jgit.util.FileUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;

@Log4j2
@Configuration
@EnableConfigurationProperties(GitProperties.class)
public class GitTemplateAutoConfiguration {

	@Configuration
	@ConditionalOnProperty(name = "git.online", havingValue = "false")
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

		@Configuration
		@ConditionalOnProperty(name = "git.ssh.enabled", havingValue = "true")
		public static class SshConfig {

			@Bean
			@ConditionalOnMissingBean
			TransportConfigCallback transportConfigCallback(SshSessionFactory sshSessionFactory) {
				return transport -> {
					Assert.isTrue(transport instanceof SshTransport, "the " + Transport.class.getName()
							+ " must be an instance of " + SshTransport.class.getName());
					SshTransport ssh = SshTransport.class.cast(transport);
					ssh.setSshSessionFactory(sshSessionFactory);
				};
			}

			@Bean
			@ConditionalOnMissingBean
			SshSessionFactory sshSessionFactory(GitProperties properties) {

				String pw = properties.getGit().getSsh().getPassword();

				UserInfo userinfo = new UserInfo() {

					@Override
					public String getPassphrase() {
						return pw;
					}

					@Override
					public String getPassword() {
						return null;
					}

					@Override
					public boolean promptPassword(String s) {
						return false;
					}

					@Override
					public boolean promptPassphrase(String s) {
						return false;
					}

					@Override
					public boolean promptYesNo(String s) {
						return false;
					}

					@Override
					public void showMessage(String s) {
					}
				};

				return new JschConfigSessionFactory() {
					@Override
					protected void configure(OpenSshConfig.Host host, Session session) {
						session.setUserInfo(userinfo);
					}
				};
			}

			@Bean
			@ConditionalOnMissingBean(Git.class)
			Git git(GitProperties gsp, TransportConfigCallback transportConfigCallback) throws GitAPIException {
				return Git//
						.cloneRepository()//
						.setTransportConfigCallback(transportConfigCallback)//
						.setURI(gsp.getGit().getUri())//
						.setDirectory(gsp.getGit().getLocalCloneDirectory())//
						.call();
			}

			@Bean
			PushCommandCreator commandCreator(TransportConfigCallback transportConfigCallback) {
				return git -> git//
						.push()//
						.setRemote("origin")//
						.setTransportConfigCallback(transportConfigCallback);
			}

		}

		@Log4j2
		@Configuration
		@ConditionalOnProperty(name = GitProperties.GIT_PROPERTIES_ROOT + ".http.enabled", havingValue = "true")
		public static class HttpConfig {

			@Bean
			@SneakyThrows
			@ConditionalOnMissingBean
			Git git(GitProperties gsp) throws GitAPIException {
				var cloneDirectory = gsp.getGit().getLocalCloneDirectory();
				var uri = gsp.getGit().getUri();
				FileUtils.delete(cloneDirectory);
				log.info("going to clone the Git repo " + uri + " into directory "
						+ gsp.getGit().getLocalCloneDirectory() + ".");
				return Git//
						.cloneRepository()//
						.setURI(uri)//
						.setDirectory(cloneDirectory)//
						.call();
			}

			@Bean
			@ConditionalOnMissingBean
			PushCommandCreator httpPushCommandCreator(GitProperties gsp) {
				var http = gsp.getGit().getHttp();
				var user = http.getUsername();
				var pw = http.getPassword();
				Assert.notNull(user, "http.username can't be null");
				Assert.notNull(pw, "http.password can't be null");
				return git -> git.push().setRemote("origin")
						.setCredentialsProvider(new UsernamePasswordCredentialsProvider(user, pw));
			}

		}

	}

}