#!/usr/bin/env bash

export CF_TRACE=true
export APP_NAME=site-generator
export TASK_NAME=${APP_NAME}-task
echo "about to run the task, ${APP_NAME} with task name ${TASK_NAME}."
cf run-task ${APP-NAME} ".java-buildpack/open_jdk_jre/bin/java -Dspring.profiles.active=cloud -cp . org.springframework.boot.loader.JarLauncher"