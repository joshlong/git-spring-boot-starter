package generator.templates;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Log4j2
@SpringBootTest
@RunWith(SpringRunner.class)
public class MustacheServiceTest {

	@Autowired
	private MustacheService service;

	@Value("classpath:templates/sample.mustache")
	Resource sample;

	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	public static class Link {
		private String href, description;
	}

	@Test
	public void convertToHtml() throws Exception {

		var formatter = DateTimeFormatter
			.ISO_LOCAL_DATE
			.withLocale(Locale.US)
			.withZone(ZoneId.of(ZoneId.SHORT_IDS.get("PST")));

		var context = Map.of(
			"date", formatter.format(Instant.now()),
			"links", List.of(new Link("http://cnn.com", "A link to CNN"), new Link("http://microsoft.com", "a link to Microsoft")));

		var html = this.service.convertMustacheTemplateToHtml(this.sample, context);
		log.info("html: " + html);
	}

}