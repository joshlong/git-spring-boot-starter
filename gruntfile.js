module.exports = function (grunt) {

    let jsFiles = ['wavesurfer.js', 'audioplayer.dev.js', 'bootstrap.js', 'custom.js'].map((fileName) => {
        return 'src/main/resources/static/assets/js/' + fileName;
    });
    let cssFiles = ['framework.css', 'all.css', 'main.css', 'responsive.css', 'audioplayer.css'].map((fileName) => {
        return 'src/main/resources/static/assets/css/' + fileName;
    });

    console.log('going to minify the following .CSS files', cssFiles);
    console.log('going to minify the following .JS files', jsFiles);

    require("matchdep").filterDev("grunt-*").forEach(grunt.loadNpmTasks);

    let buildRoot = 'src/main/resources/static/assets/';
    let cssBuild = buildRoot + '/css/site.min.css';
    let jsBuild = buildRoot + '/js/site.min.js';

    let cssConfig = {};
    cssConfig [cssBuild] = cssFiles;

    grunt.initConfig({
        pkg: grunt.file.readJSON('package.json'),
        cssmin: {
            sitecss: {
                files: cssConfig
            }
        },
        concat: {
            dev: {
                src: jsFiles,
                dest: jsBuild
            },
        }
    });


    grunt.registerTask('default', ['concat:dev', 'cssmin']);
};
