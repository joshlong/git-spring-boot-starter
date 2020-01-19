package generator.batch;

import generator.DateUtils;
import generator.FileUtils;
import generator.SiteGeneratorProperties;
import generator.git.GitTemplate;
import generator.templates.MustacheService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.eclipse.jgit.api.Git;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.net.URL;
import java.text.DateFormat;
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

	private final Resource staticAssets;

	GeneratorJob(JdbcTemplate template, PodcastRowMapper podcastRowMapper,
			SiteGeneratorProperties properties, MustacheService mustacheService,
			GitTemplate gitTemplate, @Value("classpath:/static") Resource staticAssets) {
		this.template = template;
		this.staticAssets = staticAssets;
		this.podcastRowMapper = podcastRowMapper;
		this.properties = properties;
		this.mustacheService = mustacheService;
		this.gitTemplate = gitTemplate;
	}

	@SneakyThrows
	private void downloadImageFor(PodcastRecord podcast) {
		var uid = podcast.getPodcast().getUid();
		var imagesDirectory = new File(this.properties.getOutput().getPages(),
				"episode-photos");
		Assert.isTrue(imagesDirectory.mkdirs() || imagesDirectory.exists(),
				"the imagesDirectory ('" + imagesDirectory.getAbsolutePath()
						+ "') does not exist and could not be created");
		var profilePhotoUrl = new URL(this.properties.getApiServerUrl().toString()
				+ "/podcasts/" + uid + "/profile-photo");
		var file = new File(imagesDirectory, uid + ".jpg");
		try (var fin = profilePhotoUrl.openStream();
				var fout = new FileOutputStream(file)) {
			FileCopyUtils.copy(fin, fout);
			log.info("the image file lives in " + file.getAbsolutePath());
		}
	}

	private void reset(File file) {
		log.info("resetting the directory " + file.getAbsolutePath());
		FileUtils.delete(file);
		FileUtils.ensureDirectoryExists(file);
	}

	@SneakyThrows
	public void build() {
		DateFormat dateFormat = DateUtils.dateAndTime();
		log.info("starting the site generation @ " + dateFormat.format(new Date()));

		Stream.of(properties.getOutput().getItems(), properties.getOutput().getPages())
				.forEach(this::reset);

		var reversed = Comparator
				.comparing((Function<PodcastRecord, Date>) podcastRecord -> podcastRecord
						.getPodcast().getDate())
				.reversed();
		var allPodcasts = this.template
				.query(this.properties.getSql().getLoadPodcasts(), this.podcastRowMapper)
				.stream()
				.map(p -> new PodcastRecord(p, "episode-photos/" + p.getUid() + ".jpg",
						dateFormat.format(p.getDate())))
				.collect(Collectors.toList());
		allPodcasts.forEach(this::downloadImageFor);
		allPodcasts.sort(reversed);

		// get the top3 latest episodes
		var top3 = new ArrayList<PodcastRecord>();
		for (var i = 0; i < 3 && i < allPodcasts.size(); i++) {
			top3.add(allPodcasts.get(i));
		}

		var map = this.getPodcastsByYear(allPodcasts);
		var years = new ArrayList<YearRollup>();
		map.forEach((year, podcasts) -> {
			podcasts.sort(reversed);
			years.add(new YearRollup(year, podcasts));
		});
		years.sort(Comparator.comparing(YearRollup::getYear));
		var pageChromeTemplate = this.properties.getTemplates().getPageChromeTemplate();
		var html = this.mustacheService.convertMustacheTemplateToHtml(pageChromeTemplate,
				Map.of("top3", top3, "years", years));
		var page = new File(this.properties.getOutput().getPages(), "index.html");
		try (var fout = new FileWriter(page)) {
			FileCopyUtils.copy(html, fout);
		}
		log.info("wrote the template to " + page.getAbsolutePath());

		copyPagesIntoPlace();
		commit();
	}

	@SneakyThrows
	private void commit() {
		var gitCloneDirectory = properties.getOutput().getGitClone();
		var pagesDirectory = properties.getOutput().getPages();
		this.gitTemplate.executeAndPush(git -> Stream
				.of(Objects.requireNonNull(pagesDirectory.listFiles()))
				.map(fileToCopyToGitRepo -> FileUtils.copy(fileToCopyToGitRepo,
						new File(gitCloneDirectory, fileToCopyToGitRepo.getName())))
				.forEach(file -> add(git, file)));
	}

	@SneakyThrows
	private void add(Git g, File f) {
		log.info("adding " + f.getAbsolutePath());
		g.add().addFilepattern(f.getName()).call();
		g.commit().setMessage("Adding " + f.getName() + " @ " + Instant.now().toString())
				.call();
	}

	@SneakyThrows
	private void copyPagesIntoPlace() {

		var pagesFile = this.properties.getOutput().getPages();

		// copy all the files in /static/* to the output/*
		Arrays.asList(Objects.requireNonNull(staticAssets.getFile().listFiles())).forEach(
				file -> FileUtils.copy(file, new File(pagesFile, file.getName())));

	}

	private Map<Integer, List<PodcastRecord>> getPodcastsByYear(
			List<PodcastRecord> podcasts) {
		var map = new HashMap<Integer, List<PodcastRecord>>();
		for (var podcast : podcasts) {
			var calendar = DateUtils.getCalendarFor(podcast.getPodcast().getDate());
			var year = calendar.get(Calendar.YEAR);
			if (!map.containsKey(year)) {
				map.put(year, new ArrayList<>());
			}
			map.get(year).add(podcast);
		}
		map.forEach((key,
				value) -> value.sort(Comparator.comparing(
						(Function<PodcastRecord, Date>) podcastRecord -> podcastRecord
								.getPodcast().getDate())
						.reversed()));
		return map;
	}

}

@Data
@RequiredArgsConstructor
class YearRollup {

	private final int year;

	private final Collection<PodcastRecord> episodes;

}

@RequiredArgsConstructor
@Data
class PodcastRecord {

	private final Podcast podcast;

	private final String imageSrc;

	private final String dateAndTime;

}
