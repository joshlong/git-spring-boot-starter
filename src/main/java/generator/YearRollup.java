package generator;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.Collection;

@Data
@RequiredArgsConstructor
class YearRollup {

	private final int year;

	private final Collection<PodcastRecord> episodes;

	private final String yearTabClassName;

}
