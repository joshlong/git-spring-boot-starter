package generator.batch;

import generator.DateUtils;
import generator.SiteGeneratorProperties;
import generator.templates.MustacheService;
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

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The second step in the pipeline creates pages for each of the years.
 */
@Log4j2
@Configuration
class Step2Configuration {

	private static String NAME = "podcast-items-to-pages";

	private final StepBuilderFactory stepBuilderFactory;

	private final File pagesDirectory, itemsDirectory;

	private final MustacheService mustacheService;

	private final Resource yearTemplateResource, pageChromeTemplate, staticAssets;

	@SneakyThrows
	Step2Configuration(@Value("classpath:/static") Resource staticAssets,
			StepBuilderFactory sbf, MustacheService mustacheService,
			SiteGeneratorProperties properties) {
		this.mustacheService = mustacheService;
		this.staticAssets = staticAssets;
		this.stepBuilderFactory = sbf;
		this.pageChromeTemplate = properties.getTemplates().getPageChromeTemplate();
		this.pagesDirectory = properties.getOutput().getPages();
		this.itemsDirectory = properties.getOutput().getItems();
		this.yearTemplateResource = properties.getTemplates().getYearTemplate();
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
	private void generatePageForASingleYear(Collection<Integer> allYears,
			int theYearOfThisDirectory, Calendar calendar, File directory) {

		var otherYears = allYears.stream()//
				.filter(y -> y != theYearOfThisDirectory)//
				.sorted()//
				.collect(Collectors.toList());

		var allItems = Arrays
				.stream(Objects.requireNonNull(directory
						.listFiles(pathname -> pathname.getName().endsWith(".html"))))//
				.sorted(Comparator.comparing(File::getName))//
				.map(this::fileToString) //
				.map(this::envelopeEachItem)//
				.collect(Collectors.toList());

		var htmlForAyearsWorthOfEpisodes = this.mustacheService
				.convertMustacheTemplateToHtml(this.yearTemplateResource,
						Map.of("year", theYearOfThisDirectory, //
								"episodes", allItems//
						));

		var sdf = DateUtils.dateAndTime();

		var html = this.mustacheService.convertMustacheTemplateToHtml(
				this.pageChromeTemplate,
				Map.of("currentYear",
						new Year(theYearOfThisDirectory, htmlForAyearsWorthOfEpisodes), //
						"dateOfLastSiteGeneration", sdf.format(calendar.getTime()), //
						"years", otherYears));
		var pageForYear = new File(this.pagesDirectory, theYearOfThisDirectory + ".html");
		FileCopyUtils.copy(html, new FileWriter(pageForYear));
	}

	private void emitUniquePagesForEachYear(List<? extends File> items) {
		var calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		var allYears = items.stream().map(f -> Integer.parseInt(f.getName()))
				.collect(Collectors.toList());
		items.forEach(x -> generatePageForASingleYear(allYears,
				Integer.parseInt(x.getName()), calendar, x));
	}

	private String envelopeEachItem(String episodeFragment) {
		return episodeFragment;
	}

	@Bean
	Step readDescriptionsIntoPages() {
		return this.stepBuilderFactory//
				.get(NAME + "-step-1")//
				.<File, File>chunk(1000) //
				.reader(this.directoryItemReader())//
				.writer(this.pageFromFolderItemWriter())//
				.build();
	}

}
