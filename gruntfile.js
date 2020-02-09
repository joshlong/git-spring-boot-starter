module.exports = function (grunt) {

    let jsFiles = ['/assets/soundplugin/audioplayer/wavesurfer.js', '/assets/soundplugin/audioplayer/audioplayer.dev.js',
        '/bootstrap/dist/js/bootstrap.min.js', '/assets/js/custom.js'].map((fileName) => {
        return 'src/main/resources/static/' + fileName;
    });
    let cssFiles = ['framework.css', 'all.css', 'main.css', 'responsive.css', 'audioplayer.css'].map((fileName) => {
        return 'src/main/resources/static/grunt/css/' + fileName;
    });

    console.log('going to minify the following .CSS files', cssFiles);
    console.log('going to minify the following .JS files', jsFiles);

    require("matchdep").filterDev("grunt-*").forEach(grunt.loadNpmTasks);

    grunt.initConfig({
        pkg: grunt.file.readJSON('package.json'),
        cssmin: {
            sitecss: {
                files: {
                    'src/main/resources/static/grunt/css/site.min.css': cssFiles
                }
            }
        },
        concat: {
            dev: {
                src: jsFiles,
                dest: 'src/main/resources/static/grunt/js/site.min.js'
            },
        }
    });


    grunt.registerTask('default', ['concat:dev', 'cssmin']);
};
