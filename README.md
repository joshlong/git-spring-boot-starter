# Site Generator 

This reads the data from the `podcast` tables and generates new pages and then commits those changes to a Github pages-based site.

*   There is a Github Pages-based [site](https://github.com/bootiful-podcast/bootiful-podcast.github.io). 
    Changes to the Markdown files in the `bootiful-podcast` site result in changes to the 
    actual website, `http://bootiful-podcast.fm`. They trigger a CI build which in turn triggers a deployment to Cloud Foundry where, 
    once the application has launched, it generates the `GeneratorJob` which ultimately deploys the changes to Github. 
*   This site is a responsive mobile and desktop-based application. It uses considerable `.html` and `.css` to do the job. 
    CSS, JavaScript, and other assets are post-processed with a NPM package called Grunt. You'll need to re-run the Grunt job 
    every time you have made changes to the JavaScript and CSS. Run `bin/rebuild-assets.sh` to update the assets. 
    This assumes you have  `npm` installed on your machine. 

