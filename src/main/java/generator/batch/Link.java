package generator.batch;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Data
class Link {

	private final Long id;

	private final String href, description;

}
