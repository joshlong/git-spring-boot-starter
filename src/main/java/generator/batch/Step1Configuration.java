package generator.batch;

import generator.DateUtils;
import generator.SiteGeneratorProperties;
import generator.templates.MustacheService;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.util.FileCopyUtils;

import javax.sql.DataSource;
import java.io.*;
import java.util.Calendar;
import java.util.Map;
import java.util.Objects;

/**
 * The first step in the pipeline writes fragments of markup for each episode into the
 * episodes folder.
 */
@Log4j2
@Configuration
class Step1Configuration {

	private final String NAME = "read-podcasts-from-db";

	private final DataSource dataSource;

	private final StepBuilderFactory stepBuilderFactory;

	private final PodcastRowMapper podcastRowMapper;

	private final String loadAllPodcastsSql;

	private final File itemsDirectory;

	private final Resource episodeTemplateResource;

	private final MustacheService mustacheService;

	@SneakyThrows
	Step1Configuration(SiteGeneratorProperties siteGeneratorProperties,
			DataSource dataSource, MustacheService mustacheService,
			StepBuilderFactory stepBuilderFactory, PodcastRowMapper podcastRowMapper) {
		this.mustacheService = mustacheService;
		this.dataSource = dataSource;
		this.stepBuilderFactory = stepBuilderFactory;
		this.podcastRowMapper = podcastRowMapper;
		this.episodeTemplateResource = siteGeneratorProperties.getTemplates()
				.getEpisodeTemplate();
		this.loadAllPodcastsSql = siteGeneratorProperties.getSql().getLoadPodcasts();
		this.itemsDirectory = siteGeneratorProperties.getOutput().getItems();
	}

	@Bean
	Step readPodcastsIntoDescriptions() {
		return this.stepBuilderFactory//
				.get(NAME + "-step-1")//
				.<Podcast, Podcast>chunk(100)//
				.reader(this.podcastItemReader())//
				.writer(this.podcastItemWriter())//
				.build();
	}

	@Bean
	ItemReader<Podcast> podcastItemReader() {
		return new JdbcCursorItemReaderBuilder<Podcast>()//
				.dataSource(this.dataSource)//
				.sql(this.loadAllPodcastsSql)//
				.rowMapper(this.podcastRowMapper)//
				.name(NAME + "reader")//
				.build();
	}

	@Bean
	ItemWriter<Podcast> podcastItemWriter() {
		return items -> items.forEach(podcast -> {
			podcast.getMedia().forEach(m -> log.info(" " + m.toString()));
			var podcastFile = Objects.requireNonNull(this.emitDescriptionFor(podcast));
			log.info("the episode .HTML for " + podcast.getUid() + " lives at "
					+ podcastFile.getAbsolutePath());
		});
	}

	private String paddedDate(int num) {
		if (num < 10) {
			return "0" + num;
		}
		return Integer.toString(num);
	}

	@SneakyThrows
	private File emitDescriptionFor(Podcast podcast) {
		var sdf = DateUtils.date();
		var cal = Calendar.getInstance();
		cal.setTime(podcast.getDate());
		var year = cal.get(Calendar.YEAR);
		var month = cal.get(Calendar.MONTH) + 1;
		var date = cal.get(Calendar.DAY_OF_MONTH);
		var folderForYear = new File(this.itemsDirectory, Long.toString(year));
		var sortingItemFileName = year + "_" + paddedDate(month) + "_" + paddedDate(date);
		var fileNameForEpisodeHtml = new File(folderForYear,
				sortingItemFileName + ".html");
		log.info("---------------------------------");
		log.info("podcast year: " + year + " " + podcast.toString());
		log.info("sorting file name " + sortingItemFileName);
		log.info("folder for year " + folderForYear);
		var contextForHtmlTemplate = Map.<String, Object>of("when",
				sdf.format(podcast.getDate()), //
				"href", podcast.getPodbeanMediaUri(), //
				"description", podcast.getDescription(), //
				"title", podcast.getTitle()//
		);
		var html = this.mustacheService.convertMustacheTemplateToHtml(
				this.episodeTemplateResource, contextForHtmlTemplate);
		log.info("html: " + html);
		var parentFile = fileNameForEpisodeHtml.getParentFile();
		Assert.isTrue(parentFile.exists() || parentFile.mkdirs(),
				"the directory for the year " + year + " does not exist.");

		try (var in = new BufferedReader(new StringReader(html));
				var out = new BufferedWriter(new FileWriter(fileNameForEpisodeHtml))) {
			FileCopyUtils.copy(in, out);
		}

		return fileNameForEpisodeHtml;
	}

}
