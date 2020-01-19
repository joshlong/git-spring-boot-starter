package generator.batch;

import generator.DateUtils;
import generator.FileUtils;
import generator.SiteGeneratorProperties;
import generator.templates.MustacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.FileWriter;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.stream.Stream;

@Log4j2
//@Configuration
@RequiredArgsConstructor
class Step0Configuration {

	private final SiteGeneratorProperties properties;

	private final StepBuilderFactory stepBuilderFactory;

	private final MustacheService mustacheService;

	private static String NAME = Step0Configuration.class.getName() + "-reset";

	@Bean
	Step reset() {

		var pageChromeTemplate = properties.getTemplates().getPageChromeTemplate();
		var pagesDirectory = properties.getOutput().getPages();
		var output = properties.getOutput();

		return this.stepBuilderFactory.get(NAME)//
				.tasklet((stepContribution, chunkContext) -> {
					// first delete everything
					Stream.of(output.getItems(), output.getPages())
							.forEach(Step0Configuration.this::reset);

					// then render an empty index.html because the index.html
					// will not be generated if there are no records in the database.
					var calendar = DateUtils.getCalendarFor(new Date());
					var theYearOfThisDirectory = calendar.get(Calendar.YEAR);
					var sdf = DateUtils.dateAndTime();
					var html = this.mustacheService.convertMustacheTemplateToHtml(
							pageChromeTemplate,
							Map.of("currentYear", new Year(theYearOfThisDirectory, ""), //
									"dateOfLastSiteGeneration",
									sdf.format(calendar.getTime()), //
									"years", Collections.emptyList()));

					var pageForYear = new File(pagesDirectory,
							theYearOfThisDirectory + ".html");
					FileCopyUtils.copy(html, new FileWriter(pageForYear));

					// finished
					return RepeatStatus.FINISHED;
				})//
				.build();
	}

	private void reset(File file) {
		log.info("resetting the directory " + file.getAbsolutePath());
		FileUtils.delete(file);
		FileUtils.ensureDirectoryExists(file);
	}

}
