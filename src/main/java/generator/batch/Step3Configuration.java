package generator.batch;

import generator.FileUtils;
import generator.SiteGeneratorProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Log4j2
@RequiredArgsConstructor
@Configuration
class Step3Configuration {

	private final SiteGeneratorProperties properties;

	private static String NAME = "copy-index-file-into-place";

	private final StepBuilderFactory stepBuilderFactory;

	@Bean
	Step copyIndexIntoPlace() {
		return this.stepBuilderFactory.get(NAME)
				.tasklet((stepContribution, chunkContext) -> {
					var pagesFile = properties.getOutput().getPages();
					Arrays//
							.stream(Objects.requireNonNull(pagesFile.listFiles(
									f -> f.isFile() && f.getName().endsWith(".html"))))//
							.map(f -> f.getName().split("\\.")[0])//
							.map(Integer::parseInt)//
							.sorted()//
							.max(Integer::compareTo).ifPresent(maxYear -> {
								log.info("the max year is " + maxYear);
								var yearFileToNameAsIndex = new File(pagesFile,
										maxYear + ".html");
								FileUtils.copy(yearFileToNameAsIndex,
										new File(pagesFile, "index.html"));
							});
					return RepeatStatus.FINISHED;
				})

				.build();
	}

}
