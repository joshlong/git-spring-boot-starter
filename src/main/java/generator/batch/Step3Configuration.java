package generator.batch;

import generator.templates.MustacheService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Log4j2
@Configuration
class Step3Configuration {

	private static String NAME = "build-index-page";

	private final StepBuilderFactory stepBuilderFactory;

	private final MustacheService mustacheService;

	private final File pagesDirectory;

	private final Resource indexResourceTemplate;

	private final Resource staticAssets;

	@SneakyThrows
	Step3Configuration(@Value("classpath:/static") Resource staticAssets,
			@Value("${podcast.generator.output-directory.pages}") File pagesDirectory,
			@Value("${podcast.generator.index-template}") Resource indexResourceTemplate,
			MustacheService mustacheService, StepBuilderFactory stepBuilderFactory) {
		this.stepBuilderFactory = stepBuilderFactory;
		this.pagesDirectory = pagesDirectory;
		this.indexResourceTemplate = indexResourceTemplate;
		this.mustacheService = mustacheService;
		this.staticAssets = staticAssets;

	}

	@AllArgsConstructor
	@Data
	private static class Year {

		private int year;

		private String html;

	}

	@Bean
	@StepScope
	ItemWriter<File> indexItemWriter() {
		return listOfYears -> this
				.buildIndexGivenAllTheYears(Objects.requireNonNull(listOfYears));
	}

	@SneakyThrows
	private Year toYear(File x) {
		var yearOfFile = Integer.parseInt((x.getName()).split("\\.")[0]);
		return new Year(yearOfFile, FileCopyUtils.copyToString(new FileReader(x)));
	}

	@SneakyThrows
	private void buildIndexGivenAllTheYears(List<? extends File> years) {
		log.info("there are " + years.size() + " year files in the pages directory.");
		var calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		var theCurrentYear = calendar.get(Calendar.YEAR);

		var context = new HashMap<String, Object>();
		var otherYears = new ArrayList<Year>();
		context.put("years", otherYears);
		years.stream().map(this::toYear).filter(yr -> yr.getYear() == theCurrentYear)
				.forEach(yr -> context.put("currentYear", yr));
		years.stream().map(this::toYear).filter(yr -> yr.getYear() != theCurrentYear)
				.forEach(otherYears::add);

		var html = this.mustacheService
				.convertMustacheTemplateToHtml(this.indexResourceTemplate, context);
		var indexPage = new File(this.pagesDirectory, "index.html");
		FileCopyUtils.copy(html, new FileWriter(indexPage));
		log.info("the index page is " + indexPage.getAbsolutePath());
		Arrays//
				.asList(Objects.requireNonNull(staticAssets.getFile().listFiles()))//
				.forEach(staticAsset -> this.copyRecursively(staticAsset,
						new File(this.pagesDirectory, staticAsset.getName())));
	}

	@Bean
	@StepScope
	ListItemReader<File> yearFileItemReader() {
		return new ListItemReader<>(Arrays.asList(Objects.requireNonNull(
				pagesDirectory.listFiles(f -> f.getName().endsWith(".html")))));
	}

	@Bean
	Step indexStep() {
		return this.stepBuilderFactory.get(NAME).<File, File>chunk(100)
				.reader(yearFileItemReader()).writer(indexItemWriter()).build();
	}

	@SneakyThrows
	private void copyRecursively(File a, File b) {
		FileSystemUtils.copyRecursively(a, b);
	}

}
