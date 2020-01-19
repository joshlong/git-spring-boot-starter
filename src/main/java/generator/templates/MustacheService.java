package generator.templates;

import com.samskivert.mustache.Mustache;
import lombok.SneakyThrows;
import org.springframework.core.io.Resource;

import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Map;

public class MustacheService {

	private final Mustache.Compiler compiler;

	private final Charset charset;

	MustacheService(Mustache.Compiler compiler, Charset charset) {
		this.compiler = compiler;
		this.charset = charset;
	}

	@SneakyThrows
	public String convertMustacheTemplateToHtml(Resource template, Map<String, Object> context) {
		try (var reader = new InputStreamReader(template.getInputStream(), this.charset)) {
			return this.compiler.compile(reader).execute(context);
		}
	}

	@SneakyThrows
	public String convertMustacheTemplateToHtml(String template, Map<String, Object> context) {
		var processed = this.compiler.compile(template);
		return processed.execute(context);
	}

}
