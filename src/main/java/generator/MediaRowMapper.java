package generator;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

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
