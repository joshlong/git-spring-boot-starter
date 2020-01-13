package generator.batch;

import generator.FileUtils;
import generator.SiteGeneratorProperties;
import generator.git.GitTemplate;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.eclipse.jgit.api.Git;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.time.Instant;
import java.util.Arrays;
import java.util.Objects;

@Configuration
@Log4j2
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
	@StepScope
	ListItemReader<File> pagesRootItemReader() {
		return new ListItemReader<>(
				Arrays.asList(Objects.requireNonNull(this.pagesDirectory.listFiles())));
	}

	@Bean
	Step commitPagesToGithub() {
		return this.stepBuilderFactory//
				.get(NAME)//
				.<File, File>chunk(1000)//
				.reader(this.pagesRootItemReader())//
				.writer(this.gitItemWriter())//
				.build();
	}

	@SneakyThrows
	private void add(Git g, File f) {
		g.add().addFilepattern(f.getName()).call();
		g.commit().setMessage("Adding " + f.getName() + " @ " + Instant.now().toString())
				.call();
	}

	@Bean
	ItemWriter<File> gitItemWriter() {
		return list -> this.gitTemplate.executeAndPush(g -> list.stream()
				.map(fileToCopyToGitRepo -> FileUtils.copy(fileToCopyToGitRepo,
						new File(this.gitCloneDirectory, fileToCopyToGitRepo.getName())))
				.forEach(file -> add(g, file)));
	}

}
