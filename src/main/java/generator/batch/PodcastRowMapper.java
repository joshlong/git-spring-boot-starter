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
