#!/usr/bin/env bash

mkdir -p $HOME/bin
cp ./deploy/cf-6.38.0 $HOME/bin/cf
cf login  -u $CF_USER -a $CF_API -o $CF_ORG -p $CF_PASSWORD -s $CF_SPACE
cf install-plugin -f ./deploy/scheduler-for-pcf-cliplugin-linux64-binary-1.1.0




