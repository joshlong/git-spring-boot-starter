package generator.batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
class DatabasePublishingJob {

	@Bean
	JdbcTemplate jdbcTemplate(DataSource dataSource) {
		return new JdbcTemplate(dataSource);
	}

//	@Bean
	Job job(JobBuilderFactory jobBuilderFactory, Step0Configuration s0,
			Step1Configuration s1, Step2Configuration s2, Step3Configuration s3,
			Step4Configuration s4) {
		return jobBuilderFactory //
				.get("podcast-publishing-job") //
				.incrementer(new RunIdIncrementer()) //
				.start(s0.reset()).next(s1.readPodcastsIntoDescriptions())//
				.next(s2.readDescriptionsIntoPages())//
				.next(s3.copyFilesIntoPlace())//
				.next(s4.commitPagesToGithub())//
				.build();
	}

}
