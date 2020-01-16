package generator.batch;

import generator.FileUtils;
import generator.SiteGeneratorProperties;
import lombok.extern.log4j.Log4j2;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.File;
import java.util.Arrays;
import java.util.Objects;

@Log4j2
@Configuration
class Step3Configuration {

	private final SiteGeneratorProperties properties;

	private final StepBuilderFactory stepBuilderFactory;

	private final Resource staticAssets;

	private static String COPY_INDEX_NAME = "copy-index-file-into-place";

	Step3Configuration(@Value("classpath:/static") Resource staticAssets,
			SiteGeneratorProperties properties, StepBuilderFactory stepBuilderFactory) {
		this.properties = properties;
		this.staticAssets = staticAssets;
		this.stepBuilderFactory = stepBuilderFactory;
	}

	@Bean
	Step copyFilesIntoPlace() {
		return this.stepBuilderFactory.get(COPY_INDEX_NAME)
				.tasklet((stepContribution, chunkContext) -> {

					var pagesFile = this.properties.getOutput().getPages();

					// move the latest file to the output/index.html
					Arrays//
							.stream(Objects.requireNonNull(pagesFile.listFiles(
									f -> f.isFile() && f.getName().endsWith(".html"))))//
							.map(f -> f.getName().split("\\.")[0])//
							.map(Integer::parseInt)//
							.sorted()//
							.max(Integer::compareTo)//
							.ifPresent(maxYear -> {
								log.info("the max year is " + maxYear);
								var yearFileToNameAsIndex = new File(pagesFile,
										maxYear + ".html");
								FileUtils.copy(yearFileToNameAsIndex,
										new File(pagesFile, "index.html"));
							});

					// copy all the files in /static/* to the output/*
					Arrays.asList(Objects
							.requireNonNull(this.staticAssets.getFile().listFiles()))
							.forEach(file -> FileUtils.copy(file,
									new File(pagesFile, file.getName())));

					return RepeatStatus.FINISHED;
				}).build();
	}

}
