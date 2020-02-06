$(document).ready(function () {
    $('.mobile-nav-btn').on('click', function () {
        $('.hamburger-menu').toggleClass('open');
    });
});

$(document).ready(function () {

    $('.tab-pane-toggle').each((index, element) => {
        const id = $(element).attr('id');
        const contentDiv = id.split('-tab')[0] + '-content';
        console.log('the id is', id, 'and the contentDiv is', contentDiv);
        $('#' + id).click(() => {
            console.log('clicked ', id);
        })
        $('#' + id).click(() => {
            console.log('clicking ', id);
            $('.tab-pane-content').hide();
            $('#' + contentDiv).show();
        });
        return true;
    });
});


$(document).ready(() => {
    $('.nav-link').attr('target', '_blank')
});

$(document).ready(() => {
    $("#scrollTop, .btn-slide").click(() => {
        const destination = $(this).attr('href');
        $('html, body').stop().animate({scrollTop: $(destination).offset().top}, 700);
        return false;
    });
});


function Podcast(id, uid, title, uri, photo) {
    this.uid = uid;
    this.title = title;
    this.uri = uri;
    this.id = id;
    this.episodePhotoUri = photo;
}

const bootiful = {latestPodcast: null, podcasts: {}};

// init the player
$(document).ready(() => {

    function resetEpisodePlayStatus() {
        $('.play-status').html('Listen Now');
    }

    fetch('/podcasts.json')
        .then((response) => {
            return response.json();
        })
        .then((podcasts) => {
            resetEpisodePlayStatus();
            podcasts.sort((a, b) => {
                return b.date - a.date;
            });
            podcasts.forEach((p) => {

                const uid = p.uid;
                const podcastObj = new Podcast(p.id, p.uid, p.title, p.episodeUri, p.episodePhotoUri);
                bootiful.podcasts[p.uid] = {
                    podcast: podcastObj,
                    view: new PodcastPlayerView(podcastObj)
                };
                const it = bootiful.podcasts[uid];

                const playFunction = (e) => {
                    const theViewForPodcast = it.view;
                    theViewForPodcast.show();
                    theViewForPodcast.play();
                    resetEpisodePlayStatus();
                    $('#episode-play-' + uid + '-status').html('Listening Now');
                    return false;
                };
                $('#top3-play-' + uid).click(playFunction);
                $('#episode-play-' + uid).click(playFunction);
            });
            console.log('there are', podcasts.length, 'podcasts')
            if (podcasts.length > 0) {
                initializePlayerForLatest(podcasts[0]);
            }
        });
});


/// the following controls the playing of podcasts

function getMainPlayerDataSourceId() {
    return getDataSourceElementIdFor(bootiful.latestPodcast.podcast.uid);
}

function initializePlayerForLatest(podcast) {
    $('.data-source-container').hide();
    const latestTuple = bootiful.podcasts [podcast.uid];
    bootiful.latestPodcast = latestTuple;
    const mainPlayerDataSourceId = getMainPlayerDataSourceId();
    console.log('the main player ID is', mainPlayerDataSourceId, 'for podcast title', bootiful.latestPodcast.podcast.title);
    $('#' + mainPlayerDataSourceId).show();
    dzsap_init('#' + mainPlayerDataSourceId, {
        autoplay: "off"
        , init_each: "on"
        , disable_volume: "on"
        , skinwave_mode: 'normal'
        , settings_backup_type: 'light' // == light or full
        , skinwave_: 'light' // == light or full
        , skinwave_enableSpectrum: "off"
        , embed_code: 'light' // == light or full
        , skinwave_wave_mode: "canvas"
        , skinwave_wave_mode_canvas_waves_number: "3"
        , skinwave_wave_mode_canvas_waves_padding: "1"
        , skinwave_wave_mode_canvas_reflection_size: '0' // == light or full
        , design_color_bg: '999999,ffffff' // --  light or full
        , skinwave_wave_mode_canvas_mode: 'reflecto' // --  light or full
        , preview_on_hover: 'off' // --  light or full
        , design_wave_color_progress: 'ff657a,ffffff' // -- light or full
        , pcm_data_try_to_generate: 'on'
        , skinwave_comments_enable: 'off' // -- enable the comments, publisher.php must be in the same folder as this html, also if you want the comments to automatically be taken from the database remember to set skinwave_comments_retrievefromajax to ON
        , skinwave_comments_retrievefromajax: 'off'// --- retrieve the comment form ajax
        , failsafe_repair_media_element: 500 // == light or full
    });

    latestTuple.view.show();
}


function getDataSourceElementIdFor(uid) {
    return 'data-source-' + uid + '-element';
}

function PodcastPlayerView(p) {


    function buildDataSourceForPodcast(podcast) {
        var e = $("<div><span class=\"meta-artist\"><span class=\"the-artist\"> " + podcast.title + "</span></span></div>");
        e.attr('data-source', podcast.uri);
        e.attr('id', getDataSourceElementIdFor(podcast.uid));
        e.attr('data-type', 'audio');
        e.attr('data-scrubbg', 'assets/soundplugin/audioplayer/img/dzsplugins.png');
        e.attr('data-scrubprog', 'assets/soundplugin/audioplayer/img/bgminion.jpg');
        e.attr('data-thumb', podcast.episodePhotoUri);
        'aptest-with-play skin-wave-mode-small audioplayer-tobe skin-wave button-aspect-noir data-source-container'.split(' ').forEach(function (clz) {
            e.addClass(clz.trim());
        });
        return e;
    }

    const containerId = 'containerOfDataSources';
    this.container = $('#' + containerId);
    this.podcast = p;
    this.uid = this.podcast.uid;
    this.dataSourceElement = buildDataSourceForPodcast(this.podcast);
    this.container.append(this.dataSourceElement);


    this.play = function () {
        const mainPlayerDataSourceId = getMainPlayerDataSourceId();
        // this.dataSourceElement.show();
        document.getElementById(mainPlayerDataSourceId).api_change_media(this.dataSourceElement, {
            type: "audio",
            fakeplayer_is_feeder: "off"
        });
    };

    this.show = function () {
        console.log('showing (' + this.uid + ')');
        this.dataSourceElement.show();
    };

}
