#!/usr/bin/env bash

export APP_NAME=site-generator
export TASK_NAME=${APP_NAME}-task
cf push --no-start -u none --no-route -p target/${APP_NAME}.jar ${APP_NAME}
cf set-env ${APP_NAME} GIT_PASSWORD $GIT_PASSWORD
cf set-env ${APP_NAME} GIT_USERNAME $GIT_USERNAME
cf set-env ${APP_NAME} GIT_URI $GIT_URI
cf set-env ${APP_NAME} RMQ_ADDRESS $RMQ_ADDRESS
cf start $APP_NAME
cf stop $APP_NAME