package generator.batch;

import generator.templates.MustacheService;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.util.FileCopyUtils;

import javax.sql.DataSource;
import java.io.*;
import java.time.Instant;
import java.time.temporal.TemporalField;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

@Log4j2
@Configuration
class Step1Configuration {

	private final String NAME = "read-podcasts-from-db";

	private final DataSource dataSource;

	private final StepBuilderFactory stepBuilderFactory;

	private final PodcastRowMapper podcastRowMapper;

	private final String loadAllPodcastsSql;

	private final File itemsDirectory, pagesDirectory;

	private final Resource episodeTemplateResource;

	private final MustacheService mustacheService;

	private final String apiServerHost;

	Step1Configuration(@Value("${api-server.host}") String apiServerHost,
			@Value("${podcast.generator.sql.load-podcasts}") String loadAllPodcastsSql,
			@Value("${podcast.generator.output-directory.items}") File itemsDirectory,
			@Value("${podcast.generator.output-directory.pages}") File pagesDirectory,
			@Value("${podcast.generator.episode-template}") Resource episodeTemplateResource,
			DataSource dataSource, MustacheService mustacheService,
			StepBuilderFactory stepBuilderFactory, PodcastRowMapper podcastRowMapper) {
		this.episodeTemplateResource = episodeTemplateResource;
		this.apiServerHost = apiServerHost;
		this.mustacheService = mustacheService;
		this.itemsDirectory = itemsDirectory;
		this.pagesDirectory = pagesDirectory;
		this.dataSource = dataSource;
		this.stepBuilderFactory = stepBuilderFactory;
		this.podcastRowMapper = podcastRowMapper;
		this.loadAllPodcastsSql = loadAllPodcastsSql;
	}

	@Bean
	Step readPodcastsIntoIndividualDescriptions() {
		return this.stepBuilderFactory//
				.get(NAME + "-step-1")//
				.<Podcast, Podcast>chunk(100)//
				.reader(this.podcastItemReader())//
				.writer(this.podcastItemWriter())//
				.build();
	}

	@Bean
	ItemWriter<Podcast> podcastItemWriter() {
		return items -> items.forEach(podcast -> {
			var podcastFile = Objects.requireNonNull(this.emitDescriptionFor(podcast));
			log.info("the podcast episode file is " + podcastFile.getAbsolutePath());
		});
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

	private String paddedDate(int num) {
		if (num < 10) {
			return "0" + num;
		}
		return Integer.toString(num);
	}

	@SneakyThrows
	private File emitDescriptionFor(Podcast podcast) {
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
		var html = this.mustacheService.convertMustacheTemplateToHtml(
				this.episodeTemplateResource,
				Map.of("href", podcast.getS3FullyQualifiedUri(), //
						"description", podcast.getDescription(), "title",
						podcast.getTitle())//
		);
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
