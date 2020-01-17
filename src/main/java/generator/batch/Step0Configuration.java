package generator.batch;

import generator.FileUtils;
import generator.SiteGeneratorProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.util.stream.Stream;

@Log4j2
@Configuration
@RequiredArgsConstructor
class Step0Configuration {

	private final SiteGeneratorProperties properties;

	private final StepBuilderFactory stepBuilderFactory;

	private static String NAME = Step0Configuration.class.getName() + "-reset";

	@Bean
	Step reset() {
		return this.stepBuilderFactory.get(NAME)//
				.tasklet((stepContribution, chunkContext) -> {
					var output = properties.getOutput();
					Stream.of(output.getItems(), output.getPages())
							.forEach(Step0Configuration.this::reset);
					return RepeatStatus.FINISHED;
				})//
				.build();
	}

	private void reset(File file) {
		log.info("resetting the directory " + file.getAbsolutePath());
		FileUtils.delete(file);
		FileUtils.ensureDirectoryExists(file);
	}

}
