package generator.batch;

import generator.DateUtils;
import generator.SiteGeneratorProperties;
import generator.git.GitTemplate;
import generator.templates.MustacheService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.net.URL;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Configuration
@Log4j2
@RequiredArgsConstructor
class GeneratorJob {

  private final JdbcTemplate template;
  private final PodcastRowMapper podcastRowMapper;
  private final SiteGeneratorProperties properties;
  private final MustacheService mustacheService;
  private final GitTemplate gitTemplate;

  @SneakyThrows
  private void downloadImageFor(PodcastRecord podcast) {
    var uid = podcast.getPodcast().getUid();
    var imagesDirectory = new File(this.properties.getOutput().getPages(), "episode-photos");
    var profilePhotoUrl = new URL(this.properties.getApiServerUrl().toString() + "/podcasts/" + uid + "/profile-photo");
    var file = new File(imagesDirectory, uid + ".jpg");
    try (var fin = profilePhotoUrl.openStream(); var fout = new FileOutputStream(file)) {
      FileCopyUtils.copy(fin, fout);
      log.info("the image file lives in " + file.getAbsolutePath());
    }
  }

  @EventListener(ApplicationReadyEvent.class)
  public void build() throws Exception {
    var allPodcasts = this.template.query(this.properties.getSql().getLoadPodcasts(), this.podcastRowMapper)
        .stream()
        .map(p -> new PodcastRecord(p, "episode-photos/" + p.getUid() + ".jpg"))
        .collect(Collectors.toList());
    allPodcasts.forEach(this::downloadImageFor);
    allPodcasts.sort((o1, o2) -> {
      var date1 = o1.getPodcast().getDate();
      var date2 = o2.getPodcast().getDate();
      return date1.compareTo(date2);
    });
    var top3 = new ArrayList<PodcastRecord>();
    var max = 3;
    for (var i = 0; i < max && i < allPodcasts.size(); i++) {
      var podcastRecord = allPodcasts.get(i);
      top3.add(podcastRecord);
      log.info("adding " + podcastRecord.getPodcast().getTitle());
    }
    var map = this.podcastsByYear(allPodcasts);
    var years = new ArrayList<YearRollup>();
    map.forEach((year, podcasts) -> years.add(new YearRollup(year, podcasts)));
    years.sort(Comparator.comparing(YearRollup::getYear));
    var html = this.mustacheService.convertMustacheTemplateToHtml(this.properties.getTemplates().getPageChromeTemplate(), Map.of("top3", top3, "years", years));
    var page = new File(this.properties.getOutput().getPages(), "episodes.html");
    try (var fout = new FileWriter(page)) {
      FileCopyUtils.copy(html, fout);
    }
    log.info("wrote the template to " + page.getAbsolutePath());
  }


  private Map<Integer, List<PodcastRecord>> podcastsByYear(List<PodcastRecord> podcasts) {
    var map = new HashMap<Integer, List<PodcastRecord>>();
    for (var podcast : podcasts) {
      var calendar = DateUtils.getCalendarFor(podcast.getPodcast().getDate());
      var year = calendar.get(Calendar.YEAR);
      if (!map.containsKey(year)) {
        map.put(year, new ArrayList<>());
      }
      map.get(year).add(podcast);
    }
    map.forEach((key, value) -> value.sort(Comparator.comparing((Function<PodcastRecord, Date>) podcastRecord -> podcastRecord.getPodcast().getDate()).reversed()));
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
}
