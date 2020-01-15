package generator;

import fm.bootifulpodcast.rabbitmq.RabbitMqHelper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.amqp.dsl.Amqp;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;

import java.util.Date;

/**
 * This is a giant Spring Batch {@link org.springframework.batch.core.Job job} that I use
 * to generate the web site for the <a href="http://bootifulpodcast.fm">podcast's
 * website</a>.
 * <p>
 * The job runs both when the application starts up <em>and</em> when requests come via
 * the RabbitMQ queue defined in
 * {@link SiteGeneratorProperties.Launcher#getRequestsQueue()}.
 */
@Log4j2
@EnableBatchProcessing
@EnableConfigurationProperties(SiteGeneratorProperties.class)
@SpringBootApplication
@RequiredArgsConstructor
public class SiteGeneratorApplication {

	private final JobLauncher jobLauncher;

	private final Job job;

	@Bean
	IntegrationFlow integrationFlow(RabbitMqHelper helper,
			ConnectionFactory connectionFactory, SiteGeneratorProperties properties) {

		var requestsQueue = properties.getLauncher().getRequestsQueue();
		var requestsExchange = properties.getLauncher().getRequestsExchange();
		var requestsRoutingKey = properties.getLauncher().getRequestsRoutingKey();

		helper.defineDestination(requestsExchange, requestsQueue, requestsRoutingKey);

		var amqpInboundAdapter = Amqp//
				.inboundAdapter(connectionFactory, requestsQueue)//
				.get();

		return IntegrationFlows//
				.from(amqpInboundAdapter)//
				.handle(String.class, (str, messageHeaders) -> {//
					var ok = this.launchSiteGeneratorJob();
					if (ok) {
						log.info("the job completed.");
					}
					else {
						log.error("something went wrong with the job.");
					}
					return null;
				})//
				.get();
	}

	@SneakyThrows
	private boolean launchSiteGeneratorJob() {
		var parameters = new JobParametersBuilder()//
				.addDate("runtime", new Date())//
				.toJobParameters();
		var jobExecution = this.jobLauncher.run(this.job, parameters);
		return jobExecution.getEndTime() != null
				&& jobExecution.getExitStatus().equals(ExitStatus.COMPLETED);
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
