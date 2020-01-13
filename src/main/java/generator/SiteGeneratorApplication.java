package generator;

import lombok.extern.log4j.Log4j2;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.batch.BasicBatchConfigurer;
import org.springframework.boot.autoconfigure.batch.BatchProperties;
import org.springframework.boot.autoconfigure.transaction.TransactionManagerCustomizers;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.function.HandlerFunction;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import javax.sql.DataSource;
import java.util.Date;

import static org.springframework.web.servlet.function.RouterFunctions.route;

/**
 * This is a giant Spring Batch {@link org.springframework.batch.core.Job job} that I use
 * to generate the web site for the <a href="http://bootifulpodcast.fm">podcast's
 * website</a>
 *
 * @author Josh Long
 */
@Log4j2
@EnableBatchProcessing
@EnableConfigurationProperties(SiteGeneratorProperties.class)
@SpringBootApplication
public class SiteGeneratorApplication {

	@Bean
	@Profile("manual-ignition")
	RouterFunction<ServerResponse> routes(JobLauncher jobLauncher, Job job) {
		return route()//
				.GET("/start", serverRequest -> {
					var jobExecution = jobLauncher.run(job, new JobParametersBuilder()
							.addDate("runtime", new Date()).toJobParameters());
					var ok = jobExecution.getEndTime() != null
							&& jobExecution.getExitStatus().equals(ExitStatus.COMPLETED);
					log.info("did the job complete successfully? " + ok);
					return ServerResponse.ok().build();
				})//
				.build();
	}

	public static void main(String[] args) {
		// this creates a unique argument and thus a
		// unique `JobParameter` to the provided `JobLauncher`.

		var newArgs = (args.length == 0) ? //
				new String[] { "runtime=" + System.currentTimeMillis() } : //
				args;
		SpringApplication.run(SiteGeneratorApplication.class, newArgs);
	}

}
