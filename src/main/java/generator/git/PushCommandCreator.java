package generator.git;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.errors.GitAPIException;

public interface PushCommandCreator {

	PushCommand createPushCommand(Git git) throws GitAPIException;

}
