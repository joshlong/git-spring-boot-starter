#!/bin/bash

##
## Run this script to re-build the minified CSS and JavaScript
##
# shellcheck disable=SC2164
cd "$(dirname $0)"/..
npm install grunt && npm install  && grunt