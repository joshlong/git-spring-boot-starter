package generator.batch;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
class PodcastRowMapper implements RowMapper<Podcast> {

	private final String loadMediaSql, loadLinkSql;

	private final JdbcTemplate template;

	private final MediaRowMapper mediaRowMapper;

	private final LinkRowMapper linkRowMapper;

	PodcastRowMapper(JdbcTemplate template, LinkRowMapper linkRowMapper, MediaRowMapper mediaRowMapper,
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
		var podbeanMediaUri = resultSet.getString("podbean_media_uri");
		var podbeanPhotoUri = resultSet.getString("podbean_photo_uri");

		var s3AudioFileName = resultSet.getString("s3_audio_file_name");
		var s3AudioUri = resultSet.getString("s3_audio_uri");

		var s3PhotoFileName = resultSet.getString("s3_photo_file_name");
		var s3PhotoUri = resultSet.getString("s3_photo_uri");

		var media = this.template.query(this.loadMediaSql, this.mediaRowMapper, id);
		var links = this.template.query(this.loadLinkSql, this.linkRowMapper, id);

		return new Podcast(id, date, description, podbeanMediaUri, podbeanPhotoUri, notes, title, transcript, uid,
				s3AudioFileName, s3AudioUri, s3PhotoFileName, s3PhotoUri, media, links);
	}

}
