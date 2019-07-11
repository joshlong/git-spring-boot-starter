package generator.templates;

import lombok.SneakyThrows;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.core.io.Resource;

import java.nio.file.Files;

public class MarkdownService {

	private final Parser parser;

	private final HtmlRenderer renderer;

	MarkdownService(Parser parser, HtmlRenderer renderer) {
		this.parser = parser;
		this.renderer = renderer;
	}

	@SneakyThrows
	public String convertMarkdownToHtml(Resource resource) {
		var markdown = Files.readString(resource.getFile().toPath());
		return convertMarkdownToHtml(markdown);

	}

	public String convertMarkdownToHtml(String markdown) {
		var document = this.parser.parse(markdown);
		return this.renderer.render(document);
	}

}
