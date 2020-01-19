package generator.batch;

import generator.FileUtils;
import generator.SiteGeneratorProperties;
import generator.git.GitTemplate;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.eclipse.jgit.api.Git;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;

import java.io.File;
import java.time.Instant;
import java.util.Objects;
import java.util.stream.Stream;

@Log4j2
//@Configuration
class Step4Configuration {

	private final static String NAME = "git-push-the-project";

	private final StepBuilderFactory stepBuilderFactory;

	private final File pagesDirectory, gitCloneDirectory;

	private final GitTemplate gitTemplate;

	Step4Configuration(SiteGeneratorProperties properties, GitTemplate template,
			StepBuilderFactory stepBuilderFactory) {
		this.stepBuilderFactory = stepBuilderFactory;
		this.gitCloneDirectory = properties.getOutput().getGitClone();
		this.pagesDirectory = properties.getOutput().getPages();
		this.gitTemplate = template;
	}

	@Bean
	Step commitPagesToGithub() {
		return this.stepBuilderFactory//
				.get(NAME)//
				.tasklet((stepContribution, chunkContext) -> {
					gitTemplate.executeAndPush(git -> Stream
							.of(Objects.requireNonNull(pagesDirectory.listFiles()))
							.map(fileToCopyToGitRepo -> FileUtils.copy(
									fileToCopyToGitRepo,
									new File(gitCloneDirectory,
											fileToCopyToGitRepo.getName())))
							.forEach(file -> add(git, file)));
					return RepeatStatus.FINISHED;
				})//
				.build();
	}

	@SneakyThrows
	private void add(Git g, File f) {
		log.info("adding " + f.getAbsolutePath());
		g.add().addFilepattern(f.getName()).call();
		g.commit().setMessage("Adding " + f.getName() + " @ " + Instant.now().toString())
				.call();
	}

}
