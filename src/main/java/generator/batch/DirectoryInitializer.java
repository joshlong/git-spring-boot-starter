package generator.batch;

import generator.FileUtils;
import generator.SiteGeneratorProperties;
import lombok.extern.log4j.Log4j;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.util.stream.Stream;

@Log4j2
@Configuration
class DirectoryInitializer {

	DirectoryInitializer(SiteGeneratorProperties properties) {
		var output = properties.getOutput();
		Stream.of(output.getItems(), output.getGitClone(), output.getPages())
				.forEach(this::reset);
	}

	private void reset(File file) {
		log.info("resetting the directory " + file.getAbsolutePath());
		FileSystemUtils.deleteRecursively(file);
		FileUtils.ensureDirectoryExists(file);
	}

}
