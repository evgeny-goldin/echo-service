#!/bin/bash

set +x
set -e

helios="helios -z http://$HELIOS_MASTER:5801"
docker_port='8181'
job='echo'

# These two variables are read by Gradle
export IMAGE_NAME='evgenyg/echo'
export IMAGE_TAG=$(date +%s%N | openssl dgst -sha1 | sed 's/(stdin)= //' | head -c 8)

if [ "$DOCKER_HOST" == "" ]; then
  export DOCKER_HOST='127.0.0.1'
else
  # On OSX with boot2docker it's 'tcp://192.168.59.103:2376'
  export DOCKER_HOST=$(echo $DOCKER_HOST | cut -d'/' -f3 | cut -d':' -f1)
fi

./gradlew --version
docker    --version
helios    --version

echo "\$IMAGE_NAME  = [$IMAGE_NAME]"
echo "\$IMAGE_TAG   = [$IMAGE_TAG]"
echo "\$DOCKER_HOST = [$DOCKER_HOST]"
echo "\$helios      = [$helios]"

./gradlew clean image
docker images

container=$(docker run -d -p "$docker_port:8080" "$IMAGE_NAME:$IMAGE_TAG")
sleep 10
curl "http://$DOCKER_HOST:$docker_port/a/b/c/d?a=b&c=d"
docker kill "$container"
docker rm   "$container"

if [ "$DOCKER_USER" != "" ] && [ "$DOCKER_PASS" != "" ]; then

  ./gradlew push

  if [ "$HELIOS_MASTER" != "" ]; then
    "$helios" hosts
    "$helios" jobs
    "$helios" undeploy --all --yes "$job"
    "$helios" remove         --yes "$job"
    "$helios" jobs
    "$helios" create "$job:v1" "$IMAGE_NAME:$IMAGE_TAG" -p http=8080:8080 --register "$job"
    if [ "$HELIOS_AGENTS" != "" ]; then
      for agent in "$HELIOS_AGENTS"; do
        "$helios" deploy "$job" "$agent"
        sleep 5
        "$helios" status --host "$agent"
      done
      "$helios" status --job "$job"
    else
      echo ">> \$HELIOS_AGENTS are not defined, job will not be deployed"
    fi
  else
    echo ">> \$HELIOS_MASTER in not defined, job will not be created"
  fi
else
  echo ">> \$DOCKER_USER and \$DOCKER_PASS are not defined, image will not be pushed"
fi
