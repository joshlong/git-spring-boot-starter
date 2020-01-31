package generator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import generator.git.GitTemplate;
import generator.templates.MustacheService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.eclipse.jgit.api.Git;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.ReflectionUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Log4j2
@Component
public class GeneratorJob {

	private final JdbcTemplate template;

	private final PodcastRowMapper podcastRowMapper;

	private final SiteGeneratorProperties properties;

	private final MustacheService mustacheService;

	private final GitTemplate gitTemplate;

	private final ObjectMapper objectMapper;

	private final Resource defaultEpisodePhoto = new ClassPathResource(
			"/static/assets/images/a-bootiful-podcast-default-square.jpg");

	private final Resource staticAssets;

	private final Comparator<PodcastRecord> reversed = Comparator
			.comparing((Function<PodcastRecord, Date>) podcastRecord -> podcastRecord.getPodcast().getDate())
			.reversed();

	GeneratorJob(JdbcTemplate template, ObjectMapper om, PodcastRowMapper podcastRowMapper,
			SiteGeneratorProperties properties, MustacheService mustacheService, GitTemplate gitTemplate,
			@Value("classpath:/static") Resource staticAssets) {
		this.template = template;
		this.objectMapper = om;
		this.staticAssets = staticAssets;
		this.podcastRowMapper = podcastRowMapper;
		this.properties = properties;
		this.mustacheService = mustacheService;
		this.gitTemplate = gitTemplate;
	}

	@SneakyThrows
	private void downloadImageFor(PodcastRecord podcast) {
		var uid = podcast.getPodcast().getUid();
		var imagesDirectory = new File(this.properties.getOutput().getPages(), "episode-photos");
		var file = new File(imagesDirectory, uid + ".jpg");
		try {
			Assert.isTrue(imagesDirectory.mkdirs() || imagesDirectory.exists(), "the imagesDirectory ('"
					+ imagesDirectory.getAbsolutePath() + "') does not exist and could not be created");
			var profilePhotoUrl = new URL(
					this.properties.getApiServerUrl().toString() + "/podcasts/" + uid + "/profile-photo");
			this.copyInputStreamToImage(profilePhotoUrl.openStream(), file);
		}
		catch (Exception e) {
			// we can't get a photo for this podcast, so we need to provide a default one.
			log.warn(NestedExceptionUtils
					.buildMessage("couldn't find a podcast with the UID " + podcast.getPodcast().getUid() + ".", e));
			this.copyInputStreamToImage(this.defaultEpisodePhoto.getInputStream(), file);
		}
	}

	@SneakyThrows
	private void copyInputStreamToImage(InputStream in, File file) {
		if (!file.exists()) {
			try (var fin = in; var fout = new FileOutputStream(file)) {
				FileCopyUtils.copy(fin, fout);
				log.info("the image file lives in " + file.getAbsolutePath());
			}
		}
		else {
			log.info("the image file " + file.getAbsolutePath() + " already exists. No need to download it again.");
		}
	}

	private void reset(File file) {
		log.info("resetting the directory " + file.getAbsolutePath());
		FileUtils.delete(file);
		FileUtils.ensureDirectoryExists(file);
	}

	@SneakyThrows
	public void build() {
		if (this.properties.isDisabled()) {
			log.info(this.getClass().getName() + " is not enabled. Skipping...");
			return;
		}
		var dateFormat = DateUtils.date();
		log.info("starting the site generation @ " + dateFormat.format(new Date()));
		Stream.of(this.properties.getOutput().getItems(), properties.getOutput().getPages()).forEach(this::reset);
		var podcastList = this.template.query(this.properties.getSql().getLoadPodcasts(), this.podcastRowMapper);
		var maxYear = podcastList.stream()//
				.max(Comparator.comparing(Podcast::getDate))//
				.map(podcast -> DateUtils.getYearFor(podcast.getDate()))//
				.orElse(DateUtils.getYearFor(new Date()));
		var allPodcasts = podcastList.stream()
				.map(p -> new PodcastRecord(p, "episode-photos/" + p.getUid() + ".jpg", dateFormat.format(p.getDate())))
				.collect(Collectors.toList());

		var json = buildJsonForAllPodcasts(allPodcasts);
		var jsonFile = new File(this.properties.getOutput().getPages(), "podcasts.json");
		FileCopyUtils.copy(json, new FileWriter(jsonFile));
		Assert.isTrue(jsonFile.exists(), "the json file '" + jsonFile.getAbsolutePath() + "' could not be created");

		allPodcasts.forEach(this::downloadImageFor);
		allPodcasts.sort(this.reversed);
		var top3 = new ArrayList<PodcastRecord>();
		for (var i = 0; i < 3 && i < allPodcasts.size(); i++) {
			top3.add(allPodcasts.get(i));
		}
		var map = this.getPodcastsByYear(allPodcasts);
		var years = new ArrayList<YearRollup>();
		map.forEach((year, podcasts) -> {
			podcasts.sort(this.reversed);
			years.add(new YearRollup(year, podcasts, year.equals(maxYear) ? "active" : ""));
		});
		years.sort(Comparator.comparing(YearRollup::getYear).reversed());
		var pageChromeTemplate = this.properties.getTemplates().getPageChromeTemplate();

		var context = new HashMap<String, Object>();
		context.put("top3", top3);
		context.put("years", years);
		context.put("currentYear", DateUtils.getYearFor(new Date()));
		/*
		 * allPodcasts.stream().max(this.reversed.reversed()).ifPresent(latest -> {
		 * context.put("latest", latest); log.debug("latest: " + latest.toString()); });
		 */
		var html = this.mustacheService.convertMustacheTemplateToHtml(pageChromeTemplate, context);
		var page = new File(this.properties.getOutput().getPages(), "index.html");
		FileCopyUtils.copy(html, new FileWriter(page));
		log.info("wrote the template to " + page.getAbsolutePath());
		copyPagesIntoPlace();
		commit();
	}

	private JsonNode jsonNodeForPodcast(PodcastRecord pr) {
		var objectNode = objectMapper.createObjectNode();
		objectNode.put("id", Long.toString(pr.getPodcast().getId()));
		objectNode.put("uid", pr.getPodcast().getUid());
		objectNode.put("title", pr.getPodcast().getTitle());
		objectNode.put("episodePhotoUri", pr.getPodcast().getPodbeanPhotoUri());
		objectNode.put("dataAndTime", pr.getDateAndTime());
		objectNode.put("episodeUri",
				this.properties.getApiServerUrl() + "/podcasts/" + pr.getPodcast().getUid() + "/produced-audio");
		return objectNode;
	}

	private String printJsonString(JsonNode jsonNode) {
		try {
			var json = this.objectMapper.readValue(jsonNode.toString(), Object.class);
			var objectWriter = this.objectMapper.writerWithDefaultPrettyPrinter();
			return objectWriter.writeValueAsString(json);
		}
		catch (Exception e) {
			ReflectionUtils.rethrowRuntimeException(e);
		}
		return null;
	}

	private String buildJsonForAllPodcasts(List<PodcastRecord> allPodcasts) {
		var collect = allPodcasts.stream().map(this::jsonNodeForPodcast).collect(Collectors.toList());
		var arrayNode = this.objectMapper.createArrayNode().addAll(collect);
		return printJsonString(arrayNode);
	}

	@SneakyThrows
	private void commit() {
		var gitCloneDirectory = properties.getOutput().getGitClone();
		var pagesDirectory = properties.getOutput().getPages();
		this.gitTemplate.executeAndPush(git -> Stream
				.of(Objects.requireNonNull(pagesDirectory.listFiles())).map(fileToCopyToGitRepo -> FileUtils
						.copy(fileToCopyToGitRepo, new File(gitCloneDirectory, fileToCopyToGitRepo.getName())))
				.forEach(file -> add(git, file)));
		log.info("committed everything");
	}

	@SneakyThrows
	private void add(Git git, File f) {
		git.add().addFilepattern(f.getName()).call();
		git.commit().setMessage("adding " + f.getName() + " @ " + Instant.now().toString()).call();
		log.info("added " + f.getAbsolutePath());
	}

	@SneakyThrows
	private void copyPagesIntoPlace() {
		var pagesFile = this.properties.getOutput().getPages();
		Arrays.asList(Objects.requireNonNull(this.staticAssets.getFile().listFiles())) //
				.forEach(file -> FileUtils.copy(file, new File(pagesFile, file.getName())));
	}

	private Map<Integer, List<PodcastRecord>> getPodcastsByYear(List<PodcastRecord> podcasts) {
		var map = new HashMap<Integer, List<PodcastRecord>>();
		for (var podcast : podcasts) {
			var calendar = DateUtils.getCalendarFor(podcast.getPodcast().getDate());
			var year = calendar.get(Calendar.YEAR);
			if (!map.containsKey(year)) {
				map.put(year, new ArrayList<>());
			}
			map.get(year).add(podcast);
		}
		map.forEach((key, value) -> value.sort(this.reversed.reversed()));
		return map;
	}

}

@Data
@RequiredArgsConstructor
class YearRollup {

	private final int year;

	private final Collection<PodcastRecord> episodes;

	private final String yearTabClassName;

}

@RequiredArgsConstructor
@Data
class PodcastRecord {

	private final Podcast podcast;

	private final String imageSrc, dateAndTime;

}
