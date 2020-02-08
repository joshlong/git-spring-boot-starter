module.exports = function (grunt) {

    var cssFiles = ['framework.css', 'all.css', 'main.css', 'responsive.css', 'audioplayer.css'].map((fileName) => {
        return 'src/main/resources/static/grunt/css' + '/' + fileName;
    });
    console.log ('going to minify the following .CSS files', cssFiles);

    require("matchdep").filterDev("grunt-*").forEach(grunt.loadNpmTasks);

    grunt.initConfig({
        pkg: grunt.file.readJSON(
            'package.json'
        ),
        cssmin: {
            sitecss: {
                files: {
                    'src/main/resources/static/grunt/css/site.min.css': cssFiles
                }
            }
        },
        /*uglify: {
            options: {
                compress: true
            },
            applib: {
                src: [
                    'js/libs/dollarbill.min.js',
                    'js/libs/reqwest.js',
                    'js/libs/rottentomatoes.js',
                    'js/libs/fakeTheaters.js',
                    'js/libs/movie-data.js',
                    'js/libs/backpack.js',
                    'js/libs/deeptissue.js',
                    'js/libs/toolbar.js',
                    'js/libs/mustache.js',
                    'js/libs/panorama.js',
                    'js/libs/spa.js',
                    'js/libs/rqData.js',
                    'js/debug/movie.703cbfb696c2f7d3b47d97b764a7a51b.min.js',
                    'js/debug/movie.app.grid.js',
                    'js/debug/movie.app.home-view.js',
                    'js/debug/movie.app.account-view.js',
                    'js/debug/movie.app.maps-view.js',
                    'js/debug/movie.app.movie-view.js',
                    'js/debug/movie.app.movies-view.js',
                    'js/debug/movie.app.news-view.js',
                    'js/debug/movie.app.search-view.js',
                    'js/debug/movie.app.privacy-view.js',
                    'js/debug/movie.app.search-view.js',
                    'js/debug/movie.app.theater-view.js',
                    'js/debug/movie.app.notfound-view.js',
                    'js/debug/movie.app.bootstrap.js'
                ],
                dest: 'target/js/applib.js'
            }
        }*/
    });


    grunt.registerTask('default', [/*'uglify', */'cssmin']);
};
