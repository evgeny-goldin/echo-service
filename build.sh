#!/bin/bash

set -e

job_name='echo'
image_name='evgenyg-docker-docker.bintray.io/evgenyg/echo'
image_tag=$(git log -1 --format="%h")
helios="helios -z http://${MASTER}:5801"
create_job="$helios create $job_name:v1 $image_name:$image_tag -p http=8080:8181 --register $job_name"

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

set -x

./gradlew clean distTar

time $docker build -t "$image_name:$image_tag" .
$docker tag -f "$image_name:$image_tag" "$image_name:latest"
$docker images
time $docker push "$image_name:$image_tag"
time $docker push "$image_name:latest"

if [ "$MASTER" != "" ]; then
  $helios hosts
  $helios remove --yes "$job_name" || echo OK
  $create_job
  $helios jobs
  if [ "$AGENTS" != "" ]; then
    echo "Deploying job [$job_name] to [$AGENTS]"
    $helios undeploy --all --yes "$job_name" || echo OK
    $helios deploy               "$job_name" $AGENTS
    $helios status --job         "$job_name"
    $helios inspect              "$job_name" 2>&1 | grep 'Image:'
  else
    echo ">> \$AGENTS are not defined, Helios job will not be deployed"
  fi
else
  echo ">> \$MASTER in not defined, Helios job will not be created"
fi
