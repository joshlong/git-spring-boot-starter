package generator;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Data
class PodcastRecord {

	private final Podcast podcast;

	private final String imageSrc, dateAndTime;

	private final String htmlDescription; // the HTML rendered from the Markdown

}
