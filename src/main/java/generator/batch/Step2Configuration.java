package generator.batch;

import generator.templates.MustacheService;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
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
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;
import java.util.stream.Collectors;

@Log4j2
@Configuration
class Step2Configuration {

	private final String NAME = "podcast-items-to-pages";

	private final StepBuilderFactory stepBuilderFactory;

	private final File pagesDirectory, itemsDirectory;

	private final MustacheService mustacheService;

	private final Resource yearTemplateResource;

	Step2Configuration(StepBuilderFactory sbf, MustacheService mustacheService,
			@Value("${podcast.generator.year-template}") Resource yearTemplateResource,
			@Value("${podcast.generator.output-directory.pages}") File pagesDirectory,
			@Value("${podcast.generator.output-directory.items}") File itemsDirectory) {
		this.mustacheService = mustacheService;
		this.stepBuilderFactory = sbf;
		this.pagesDirectory = pagesDirectory;
		this.itemsDirectory = itemsDirectory;
		this.yearTemplateResource = yearTemplateResource;

		this.assertThatTheDirectoryExists(this.pagesDirectory);
		this.assertThatTheDirectoryExists(this.itemsDirectory);
	}

	private void assertThatTheDirectoryExists(File file) {
		Assert.isTrue(file.exists() || file.mkdirs(), "the directory "
				+ file.getAbsolutePath() + " directory could not be found");
	}

	@Bean
	@StepScope
	ListItemReader<File> directoryItemReader() {
		return new ListItemReader<>(Arrays.asList(
				Objects.requireNonNull(itemsDirectory.listFiles(File::isDirectory))));
	}

	@Bean
	ItemWriter<File> pageFromFolderItemWriter() {
		return this::emitUniquePagesForEachYear;
	}

	@SneakyThrows
	private String fileToString(File file) {
		return FileCopyUtils.copyToString(new FileReader(file));
	}

	@SneakyThrows
	private void oneYear(File directory) {
		var year = Integer.parseInt(directory.getName());
		log.info("the year is " + year);
		var allItems = Arrays
				.stream(Objects.requireNonNull(directory
						.listFiles(pathname -> pathname.getName().endsWith(".html"))))
				.sorted(Comparator.comparing(File::getName)).map(this::fileToString)
				.map(this::envelopeEachItem).collect(Collectors.toList());
		var html = this.mustacheService.convertMustacheTemplateToHtml(
				this.yearTemplateResource, Map.of("year", year, "episodes", allItems));
		var pageForYear = new File(this.pagesDirectory, year + ".html");
		FileCopyUtils.copy(html, new FileWriter(pageForYear));
	}

	private void emitUniquePagesForEachYear(List<? extends File> items) {
		items.forEach(this::oneYear);
	}

	private String envelopeEachItem(String episodeFragment) {
		return episodeFragment;
	}

	@Bean
	Step readDescriptionsIntoPages() {
		return this.stepBuilderFactory.get(NAME + "-step-1").<File, File>chunk(1000) // I'm
																						// not
																						// going
																						// to
																						// do
																						// more
																						// than
																						// 1000
																						// podcasts
																						// per
																						// year!
				.reader(this.directoryItemReader())
				.writer(this.pageFromFolderItemWriter()).build();
	}

}
