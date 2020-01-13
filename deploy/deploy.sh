#!/usr/bin/env bash

APP_NAME=site-generator
JOB_NAME=$APP_NAME
SCHEDULER_NAME=scheduler-bootiful-podcast

cf push -b java_buildpack -u none --no-route --no-start -p target/${APP_NAME}.jar ${APP_NAME}
cf set-health-check $APP_NAME none # the new version of the cf cli will take 'process' instead of 'none'
cf set-env $APP_NAME GIT_PASSWORD $GIT_PASSWORD
cf set-env $APP_NAME GIT_USERNAME $GIT_USERNAME

# scheduler
cf s | grep ${SCHEDULER_NAME} || cf cs scheduler-for-pcf standard ${SCHEDULER_NAME}
cf bs ${APP_NAME} ${SCHEDULER_NAME}
cf restage $APP_NAME

cf jobs | grep $JOB_NAME && cf delete-job -f $JOB_NAME
cf create-job $APP_NAME $JOB_NAME ".java-buildpack/open_jdk_jre/bin/java org.springframework.boot.loader.JarLauncher"
#cf schedule-job ${JOB_NAME} "0 20 ? * *"
