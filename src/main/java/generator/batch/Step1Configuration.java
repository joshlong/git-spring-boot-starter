package generator.batch;

import lombok.extern.log4j.Log4j2;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Log4j2
@Configuration
class Step1Configuration {

	private final String NAME = "read-podcasts-from-db";

	private final DataSource dataSource;

	private final StepBuilderFactory stepBuilderFactory;

	private final PodcastRowMapper podcastRowMapper;

	private final String loadAllPodcastsSql;

	Step1Configuration(
		@Value("${podcast.generator.sql.load-podcasts}") String loadAllPodcastsSql,
		DataSource dataSource,
		StepBuilderFactory stepBuilderFactory,
		PodcastRowMapper podcastRowMapper) {
		this.dataSource = dataSource;
		this.stepBuilderFactory = stepBuilderFactory;
		this.podcastRowMapper = podcastRowMapper;
		this.loadAllPodcastsSql = loadAllPodcastsSql;
	}

	@Bean
	Step readMarkdownFromDatabaseStep() {
		return this.stepBuilderFactory.get(NAME + "-step1").<Podcast, Podcast>chunk(100)
			.reader(this.podcastItemReader()).writer(this.podcastItemWriter())
			.build();
	}

	@Bean
	ItemWriter<Podcast> podcastItemWriter() {
		return items -> items.forEach(log::info);
	}

	@Bean
	ItemReader<Podcast> podcastItemReader() {

		return new JdbcCursorItemReaderBuilder<Podcast>().dataSource(this.dataSource)
			.sql(this.loadAllPodcastsSql).rowMapper(this.podcastRowMapper)
			.name(NAME + "reader").build();
	}

}
