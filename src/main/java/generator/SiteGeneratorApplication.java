package generator;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * This is a giant Spring Batch {@link org.springframework.batch.core.Job job} that I use
 * to generate the web site for the <a href="http://bootifulpodcast.fm">podcast's
 * website</a>
 *
 * @author Josh Long
 */
@EnableBatchProcessing
@SpringBootApplication
public class SiteGeneratorApplication {

	public static void main(String[] args) {
		SpringApplication.run(SiteGeneratorApplication.class, args);
	}

}
