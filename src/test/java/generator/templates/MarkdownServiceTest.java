package generator.templates;

import lombok.extern.log4j.Log4j2;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.test.context.junit4.SpringRunner;

@Log4j2
@RunWith(SpringRunner.class)
@SpringBootTest
public class MarkdownServiceTest {

	@Autowired
	private MarkdownService markdownService;

	@Value("classpath:/templates/sample.md")
	private Resource resource;

	@Test
	public void markdown() throws Exception {
		String markdownHtml = this.markdownService.convertMarkdownToHtml(this.resource);
		log.debug(markdownHtml);
		Assert.assertTrue(markdownHtml.contains("<li>Now is the time</li>"));
		Assert.assertTrue(markdownHtml.contains("<h2>A Subheader</h2>"));
		Assert.assertTrue(
				markdownHtml.contains("<a href=\"http://slashdot.org\">nonsense</a>"));
		Assert.assertTrue(markdownHtml.contains("<h1>A Simple Markdown Document</h1>"));
	}

}