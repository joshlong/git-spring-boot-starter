#!/bin/bash

##
## Run this script to re-build the minified CSS and JavaScript
##
cd $(dirname $0)/..
pwd
npm install grunt && npm install  && grunt