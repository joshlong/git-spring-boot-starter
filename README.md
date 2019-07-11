# Site Generator 

This reads the data from the `podcast` tables and generates new pages and then commits those changes to a Github pages-based site.

*   It's a Spring Batch-based `Job`
*   There is a Github Pages-based [site](https://github.com/bootiful-podcast/bootiful-podcast.github.io). 
    Changes to the `markdown` files in the `bootiful-podcast` site result in changes to the 
    actual website, `http://bootiful-podcast.fm`.
*   This job will be run as a Cloud Foundry job every half hour and will regenerate all the content in the `bootiful-podcast` site.
*   When the job completes, it will use git to commit the changes to the `bootiful-podcast` site. 