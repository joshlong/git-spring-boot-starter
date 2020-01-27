package generator;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
class LinkRowMapper implements RowMapper<Link> {

	@Override
	public Link mapRow(ResultSet resultSet, int i) throws SQLException {
		return new Link(resultSet.getLong("id"), resultSet.getString("href"), resultSet.getString("description"));
	}

}
