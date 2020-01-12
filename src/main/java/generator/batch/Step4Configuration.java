package generator.batch;

import generator.FileUtils;
import generator.SiteGeneratorProperties;
import generator.git.GitCallback;
import generator.git.GitTemplate;
import lombok.extern.log4j.Log4j2;
import org.eclipse.jgit.api.Git;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.*;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

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

	@Bean
	ItemWriter<File> gitItemWriter() {
		return new ItemWriter<>() {

			@Override
			public void write(List<? extends File> list) throws Exception {

				list.forEach(
						file -> log.info("gitItemWriter: " + file.getAbsolutePath()));

				/*
				 * gitTemplate.executeAndPush(new GitCallback() {
				 *
				 * @Override public void execute(Git git) throws Exception { //
				 * git.add().addFilepattern()
				 *
				 * } });
				 */
			}
		};
	}

}
