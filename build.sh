#!/bin/bash

set -e

docker_port='8181'
job='echo'
image_name='evgenyg/echo'
image_tag=$(date +"%m-%d-%y-%T" | tr ':' '-')
helios="helios -z http://${MASTER}:5801"

if [[ "$HOME" =~ ^/Users/ ]]; then # OS X
  docker='docker'
else
  docker='sudo docker'
fi

./gradlew --version
$docker   --version
helios    --version

echo "\$image_name = [$image_name]"
echo "\$image_tag  = [$image_tag]"
echo "\$helios     = [$helios]"

./gradlew clean distTar
$docker build -t "$image_name:$image_tag" .
$docker tag   -f "$image_name:$image_tag" "$image_name:latest"
$docker images

if [ "$DOCKER_USER" != "" ] && [ "$DOCKER_PASS" != "" ]; then

  echo "Login to DockerHub as [$DOCKER_USER/$DOCKER_MAIL]"
  $docker login -u "$DOCKER_USER" -p "$DOCKER_PASS" -e "$DOCKER_MAIL"
  $docker push     "$image_name:$image_tag"
  $docker push     "$image_name:latest"

  if [ "$MASTER" != "" ]; then
    set -x
    $helios hosts
    $helios undeploy --all --yes "$job" || echo OK
    $helios remove         --yes "$job" || echo OK
    $helios jobs
    $helios create "$job:v1" "$image_name:$image_tag" -p http=8080:8080 --register "$job"
    if [ "$AGENTS" != "" ]; then
      echo "Deploying job [$job] to [$AGENTS]"
      $helios deploy $job $AGENTS
      $helios status --job "$job"
    else
      echo ">> \$AGENTS are not defined, Helios job will not be deployed"
    fi
  else
    echo ">> \$MASTER in not defined, Helios job will not be created"
  fi
else
  echo ">> \$DOCKER_USER and \$DOCKER_PASS are not defined, image will not be pushed"
fi
