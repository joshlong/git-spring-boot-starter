package generator;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;

import java.io.File;
import java.net.URI;

@Data
@ConfigurationProperties("podcast.generator")
public class SiteGeneratorProperties {

	private String charset;

	private URI apiServerUrl;

	private final Sql sql = new Sql();

	private final Templates templates = new Templates();

	private final Output output = new Output();

	private final Launcher launcher = new Launcher();

	@Data
	public static class Output {

		private File items, pages, gitClone;

	}

	@Data
	public static class Templates {

		private Resource episodeTemplate, pageChromeTemplate, yearTemplate;

	}

	@Data
	public static class Sql {

		private String loadPodcasts;

		private String loadLinks;

		private String loadMedia;

	}

	@Data
	public static class Launcher {

		private String requestsQueue = "site-generator-requests";

		private String requestsExchange = this.requestsQueue;

		private String requestsRoutingKey = this.requestsQueue;

	}

}
