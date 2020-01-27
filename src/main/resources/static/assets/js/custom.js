$(document).ready(function () {
    $('.mobile-nav-btn').on('click', function () {
        $('.hamburger-menu').toggleClass('open');
    });
});

$(document).ready(function () {
    $('.tab-pane-toggle').click(function (e) {
        var id = e.currentTarget.id;
        var contentDiv = id.split('-tab')[0] + '-content';
        $('.tab-pane-content').removeClass('active');
        $('#' + contentDiv).addClass('active');
    });
});

$(document).ready(function () {

    $("#scrollTop, .btn-slide").click(function () {
        // get the destination
        var destination = $(this).attr('href');

        $('html, body').stop().animate({
            scrollTop: $(destination).offset().top
        }, 700);

        // override the default link click behavior
        return false;
    });

});


var playerId = '#globalPlayerDataSource';
var containerId = '#containerOfDataSources';
var container = jQuery(containerId);

function buildMetaArtistHtml(title) {
    return "<span class=\"meta-artist\"><span class=\"the-artist\"> " + title + "</span></span>";
}

function initializePlayerForLatest(podcast) {
    console.log('initializing the latest with the episode information');
    var playerNode = jQuery(playerId);
    playerNode.attr('data-source', podcast.episodeUri);
    playerNode.attr('data-thumb', podcast.episodePhotoUri);
    playerNode.html(buildMetaArtistHtml(podcast.title));

    dzsap_init(playerId, {
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

}

function Podcast(id, uid, title, uri, photo) {


    this.uid = (uid != null && uid !== '' && uid) ? uid : 'data-source-' + uid;
    this.title = title;
    this.uri = uri;
    this.id = id;
    this.episodePhotoUri = photo;

    var e = $("<span>" + buildMetaArtistHtml(this.title) + "</span>");
    e.attr('data-source', this.uri);
    e.attr('id', this.uid);
    e.attr('data-type', 'audio');
    e.attr('data-scrubbg', 'assets/soundplugin/audioplayer/img/dzsplugins.png');
    e.attr('data-scrubprog', 'assets/soundplugin/audioplayer/img/bgminion.jpg');
    e.attr('data-thumb', this.episodePhotoUri);
    'aptest-with-play skin-wave-mode-small audioplayer-tobe skin-wave button-aspect-noir'.split(' ').forEach(function (clz) {
        e.addClass(clz.trim());
    });
    container.append(e);
    this.element = e;
    this.play = function () {
        var pargs = {
            type: "audio",
            fakeplayer_is_feeder: "off"
        };
        console.log('playing ' + this.title + ' with URI ' + this.uri);
        // var dataSource = jQuery(this.uid);
        document.getElementById(playerId.substr(1)).api_change_media(this.element, pargs);
    }
}


// init the player
jQuery(document).ready(function ($) {


    function resetEpisodePlayStatus() {
        $('.play-status').html('Listen Now');
    }

    function renderPodcast(value) {
        var p = value;
        var playFunction = function (e) {
            p.play();
            e.stopPropagation();
            e.preventDefault();
            resetEpisodePlayStatus();
            $('#episode-play-' + p.uid + '-status').html('Listening Now');
            return false;
        };
        $('#top3-play-' + p.uid).click(playFunction);
        $('#episode-play-' + p.uid).click(playFunction);
    }

    fetch('/podcasts.json')
        .then(function (response) {
            return response.json();
        })
        .then(function (podcasts) {
            console.log(podcasts);
            resetEpisodePlayStatus();

            podcasts.sort(function (a, b) {
                return b.date - a.date;
            });
            podcasts.forEach(function (p) {
                var podcast = new Podcast(p.id, p.uid, p.title, p.episodeUri, p.episodePhotoUri);
                console.log('the podcast is ', podcast);
                renderPodcast(podcast);
            });

            if (podcasts.length > 0) {
                var max = podcasts [0];
                console.log('the latest podcast is ', max);
                initializePlayerForLatest(max);
            }
        });
});

jQuery(document).ready(function () {
    $('.nav-link').attr('target', '_blank')
})