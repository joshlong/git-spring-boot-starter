package generator.git;

import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;
import generator.FileUtils;
import lombok.extern.log4j.Log4j2;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;

@Configuration
@EnableConfigurationProperties(GitTemplateConfigurationProperties.class)
public class GitTemplateAutoConfiguration {

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
				Assert.isTrue(transport instanceof SshTransport,
						"the " + Transport.class.getName() + " must be an instance of " + SshTransport.class.getName());
				SshTransport ssh = SshTransport.class.cast(transport);
				ssh.setSshSessionFactory(sshSessionFactory);
			};
		}

		@Bean
		@ConditionalOnMissingBean
		SshSessionFactory sshSessionFactory(GitTemplateConfigurationProperties gsp) {

			String pw = gsp.getSsh().getPassword();

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
		@ConditionalOnMissingBean
		Git git(GitTemplateConfigurationProperties gsp, TransportConfigCallback transportConfigCallback)
				throws GitAPIException {
			return Git//
					.cloneRepository()//
					.setTransportConfigCallback(transportConfigCallback)//
					.setURI(gsp.getUri())//
					.setDirectory(gsp.getLocalCloneDirectory())//
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
	@ConditionalOnProperty(name = "git.http.enabled", havingValue = "true")
	public static class HttpConfig {

		@Bean
		@ConditionalOnMissingBean
		Git git(GitTemplateConfigurationProperties gsp) throws GitAPIException {
			var cloneDirectory = gsp.getLocalCloneDirectory();
			FileUtils.delete(gsp.getLocalCloneDirectory());
			log.info("going to clone the GIT repo " + gsp.getUri() + " into directory " + gsp.getLocalCloneDirectory()
					+ ".");
			return Git//
					.cloneRepository()//
					.setURI(gsp.getUri())//
					.setDirectory(cloneDirectory)//
					.call();
		}

		@Bean
		@ConditionalOnMissingBean
		PushCommandCreator httpPushCommandCreator(GitTemplateConfigurationProperties gsp) {
			var user = gsp.getHttp().getUsername();
			var pw = gsp.getHttp().getPassword();
			Assert.notNull(user, "http.username can't be null");
			Assert.notNull(pw, "http.password can't be null");
			return git -> git.push().setRemote("origin").setCredentialsProvider(
					new UsernamePasswordCredentialsProvider(gsp.getHttp().getUsername(), gsp.getHttp().getPassword()));
		}

	}

}
