package generator.batch;

import lombok.extern.log4j.Log4j2;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.*;
import org.springframework.batch.item.support.IteratorItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;

import java.io.File;
import java.io.FileFilter;
import java.util.*;

@Log4j2
@Configuration
class Step2Configuration {

	private final String NAME = "podcast-items-to-pages";

	private final StepBuilderFactory stepBuilderFactory;

	private final File pagesDirectory, itemsDirectory;

	Step2Configuration(StepBuilderFactory sbf,
			@Value("${podcast.generator.output-directory.pages}") File pagesDirectory,
			@Value("${podcast.generator.output-directory.items}") File itemsDirectory) {
		this.stepBuilderFactory = sbf;
		this.pagesDirectory = pagesDirectory;
		this.itemsDirectory = itemsDirectory;
		assertThatTheDirectoryExists(this.pagesDirectory);
		assertThatTheDirectoryExists(this.itemsDirectory);
	}

	private void assertThatTheDirectoryExists(File file) {
		Assert.isTrue(file.exists() || file.mkdirs(), "the directory "
				+ file.getAbsolutePath() + " directory could not be found");
	}

	@Bean
	ItemReader<File> directoryItemReader() {
		var files = Arrays.asList(
				Objects.requireNonNull(itemsDirectory.listFiles(File::isDirectory)));
		var iterator = files.iterator();
		return new IteratorItemReader<>(iterator);
	}

	@Bean
	ItemWriter<File> pageFromFolderItemWriter() {
		return this::emitItemsAsAPage;
	}

	private void emitItemsAsAPage(List<? extends File> items) {
		for (var directory : items) {
			var year = Integer.parseInt(directory.getName());
			log.info("the year is " + year);
			var individualItemsInYearFolder = Objects.requireNonNull(directory
					.listFiles(pathname -> pathname.getName().endsWith(".html")));
			for (var item : individualItemsInYearFolder) {
				log.info("the item is " + item.getAbsolutePath());

			}
		}
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
