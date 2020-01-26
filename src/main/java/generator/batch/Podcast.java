package generator.batch;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.Date;

@Data
@RequiredArgsConstructor
class Podcast {

	private final Long id;

	private final Date date;

	private final String description, podbeanMediaUri, podbeanPhotoUri, notes, title, transcript, uid, s3AudioFileName,
			s3AudioUri, s3PhotoFileName, s3PhotoUri;

	private final Collection<Media> media;

	private final Collection<Link> links;

}
