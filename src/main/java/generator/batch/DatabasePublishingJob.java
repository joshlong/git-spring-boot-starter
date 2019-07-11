package generator.batch;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;

@Configuration
class DatabasePublishingJob {

	@Bean
	JdbcTemplate jdbcTemplate(DataSource dataSource) {
		return new JdbcTemplate(dataSource);
	}

	@Bean
	Job job(JobBuilderFactory jobBuilderFactory, Step1Configuration s1) {
		return jobBuilderFactory.get("podcast-publishing-job")
				.incrementer(new RunIdIncrementer())
				.start(s1.readMarkdownFromDatabaseStep()).build();
	}

}

@Log4j2
@Configuration
class Step1Configuration {

	private final String NAME = "read-podcasts-from-db";

	private final DataSource dataSource;

	private final JdbcTemplate template;

	private final StepBuilderFactory stepBuilderFactory;

	private final PodcastRowMapper podcastRowMapper;

	private final String loadAllPodcastsSql;

	Step1Configuration(
			@Value("${podcast.generator.sql.load-podcasts}") String loadAllPodcastsSql,
			DataSource dataSource, JdbcTemplate template,
			StepBuilderFactory stepBuilderFactory, PodcastRowMapper podcastRowMapper) {
		this.dataSource = dataSource;
		this.template = template;
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

@Component
class PodcastRowMapper implements RowMapper<Podcast> {

	private final String loadMediaSql, loadLinkSql;

	private final JdbcTemplate template;

	private final MediaRowMapper mediaRowMapper;

	private final LinkRowMapper linkRowMapper;

	PodcastRowMapper(JdbcTemplate template, LinkRowMapper linkRowMapper,
			MediaRowMapper mediaRowMapper,
			@Value("${podcast.generator.sql.load-media}") String loadMediaSql,
			@Value("${podcast.generator.sql.load-links}") String loadLinkSql) {
		this.loadMediaSql = loadMediaSql;
		this.loadLinkSql = loadLinkSql;
		this.template = template;
		this.mediaRowMapper = mediaRowMapper;
		this.linkRowMapper = linkRowMapper;
	}

	@Override
	public Podcast mapRow(ResultSet resultSet, int i) throws SQLException {
		var description = resultSet.getString("description");
		var id = resultSet.getLong("id");
		var title = resultSet.getString("title");
		var date = resultSet.getDate("date");
		var notes = resultSet.getString("notes");
		var transcript = resultSet.getString("transcript");
		var uid = resultSet.getString("uid");
		var s3_output_file_name = resultSet.getString("s3_output_file_name");
		var s3_fqn_uri = resultSet.getString("s3_fqn_uri");
		var media = this.template.query(this.loadMediaSql, this.mediaRowMapper, id);
		var links = this.template.query(this.loadLinkSql, this.linkRowMapper, id);
		return new Podcast(id, date, description, notes, title, transcript, uid,
				s3_output_file_name, s3_fqn_uri, media, links);
	}

}

@RequiredArgsConstructor
@Data
class Link {

	private final Long id;

	private final String href, description;

}

@Component
class LinkRowMapper implements RowMapper<Link> {

	@Override
	public Link mapRow(ResultSet resultSet, int i) throws SQLException {
		return new Link(resultSet.getLong("id"), resultSet.getString("href"),
				resultSet.getString("description"));
	}

}

@Component
class MediaRowMapper implements RowMapper<Media> {

	@Override
	public Media mapRow(ResultSet resultSet, int i) throws SQLException {
		var description = resultSet.getString("description");
		var ext = resultSet.getString("extension");
		var fileName = resultSet.getString("file_name");
		var href = resultSet.getString("href");
		var type = resultSet.getString("type");
		return new Media(description, ext, fileName, href, type);
	}

}

@Data
@RequiredArgsConstructor
class Podcast {

	private final Long id;

	private final Date date;

	private final String description, notes, title, transcript, uid, s3OutputFileName,
			s3FullyQualifiedUri;

	private final Collection<Media> media;

	private final Collection<Link> links;

}

@Data
@AllArgsConstructor
@NoArgsConstructor
class Media {

	private String description, extension, fileName, href, type;

}