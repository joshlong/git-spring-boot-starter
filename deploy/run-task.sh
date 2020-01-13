#!/usr/bin/env bash

export APP_NAME=site-generator
export TASK_NAME=${APP_NAME}-task
echo "about to run the task..."
cf run-task ${APP-NAME} ".java-buildpack/open_jdk_jre/bin/java cp . org.springframework.boot.loader.JarLauncher" --name ${APP_NAME}