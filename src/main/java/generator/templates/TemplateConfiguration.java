package generator.templates;

import com.samskivert.mustache.Mustache;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.nio.charset.Charset;

@Configuration
class TemplateConfiguration {

	private final Charset charset;

	// @Value("${classpath:/templates/index.templates}")
	// Resource resource ;

	TemplateConfiguration(@Value("${podcasts.generator.charset:}") String charset) {
		this.charset = !StringUtils.hasText(charset) ? Charset.defaultCharset()
				: Charset.forName(charset);
	}

	@Bean
	MarkdownService markdownService() {
		return new MarkdownService(Parser.builder().build(),
				HtmlRenderer.builder().build());
	}

	@Bean
	MustacheService mustacheService(Mustache.Compiler compiler) {
		return new MustacheService(compiler, this.charset);
	}

}
