package generator.batch;

import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class Step2Configuration {

	private final String NAME = "podcast-items-to-pages";

	private final StepBuilderFactory stepBuilderFactory;

	Step2Configuration(StepBuilderFactory sbf) {
		this.stepBuilderFactory = sbf;
	}

	// todo this will find the folders starting with years and then for each year generate
	// a page full of items

	// @Bean
	Step readDescriptionsIntoPages() {
		// return this.stepBuilderFactory.get(NAME + "-step-1")
		// .<File,>chunk();
		return null;
	}

}
