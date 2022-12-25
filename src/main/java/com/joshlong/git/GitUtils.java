package com.joshlong.git;

import com.jcraft.jsch.UserInfo;
import lombok.SneakyThrows;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.TransportException;
import org.eclipse.jgit.transport.*;
import org.eclipse.jgit.util.FS;
import org.springframework.util.Assert;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.net.URI;

public abstract class GitUtils {

	public static TransportConfigCallback createSshTransportConfigCallback(SshSessionFactory sshSessionFactory) {
		return transport -> {
			Assert.isTrue(transport instanceof SshTransport,
					"the " + Transport.class.getName() + " must be an instance of " + SshTransport.class.getName());
			SshTransport ssh = SshTransport.class.cast(transport);
			ssh.setSshSessionFactory(sshSessionFactory);
		};
	}

	public static TransportConfigCallback createSshTransportConfigCallback(String pw) {
		var sshSessionFactory = createSshSessionFactory(pw);
		return transport -> {
			Assert.isTrue(transport instanceof SshTransport,
					"the " + Transport.class.getName() + " must be an instance of " + SshTransport.class.getName());
			SshTransport ssh = SshTransport.class.cast(transport);
			ssh.setSshSessionFactory(sshSessionFactory);
		};
	}

	@Deprecated
	static SshSessionFactory createSshSessionFactory(String pw) {

		var userinfo = new UserInfo() {

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

		return new SshSessionFactory() {
			@Override
			public RemoteSession getSession(URIish urIish, CredentialsProvider credentialsProvider, FS fs, int i)
					throws TransportException {
				return null;
			}

			@Override
			public String getType() {
				return null;
			}
		};
	}

	static public PushCommandCreator createSshPushCommandCreator(TransportConfigCallback transportConfigCallback) {
		return git -> git//
				.push()//
				.setRemote("origin")//
				.setTransportConfigCallback(transportConfigCallback);
	}

	/**
	 * We want a {@link Git} instance that represents a locally-cloned repository
	 */
	public static Git createLocalSshGitRepository(URI uri, File localCloneDirectory,
			TransportConfigCallback transportConfigCallback) throws GitAPIException {
		reset(localCloneDirectory);
		return Git//
				.cloneRepository()//
				.setTransportConfigCallback(transportConfigCallback)//
				.setURI(uri.toASCIIString())//
				.setDirectory(localCloneDirectory)//
				.call();
	}

	@SneakyThrows
	public static Git createLocalHttpGitRepository(URI uri, File localCloneDirectory) {
		reset(localCloneDirectory);
		return Git//
				.cloneRepository()//
				.setURI((uri.toASCIIString()))//
				.setDirectory(localCloneDirectory)//
				.call();
	}

	public static PushCommandCreator createHttpPushCommandCreator(String user, String pw) {
		Assert.notNull(user, "http.username can't be null");
		Assert.notNull(pw, "http.password can't be null");
		return git -> git.push().setRemote("origin")
				.setCredentialsProvider(new UsernamePasswordCredentialsProvider(user, pw));
	}

	private static void reset(File directory) {
		if (directory.exists()) {
			FileSystemUtils.deleteRecursively(directory);
		}
	}

}
