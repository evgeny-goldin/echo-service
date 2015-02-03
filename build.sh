#!/bin/bash

set -e

docker_port='8181'
job='echo'
image_name='evgenyg/echo'
image_tag=$(date +"%m-%d-%y-%T" | tr ':' '-')
helios="helios -z http://${MASTER}:5801"


./gradlew --version
docker    --version
helios    --version

echo "\$image_name = [$image_name]"
echo "\$image_tag  = [$image_tag]"
echo "\$helios     = [$helios]"

./gradlew clean distTar
sudo docker build --rm --no-cache -t "$image_name:$image_tag" .
sudo docker images

if [ "$DOCKER_USER" != "" ] && [ "$DOCKER_PASS" != "" ]; then

  sudo docker login -u "$DOCKER_USER" -p "$DOCKER_PASS" -e "$DOCKER_MAIL"
  sudo docker push     "$image_name:$image_tag"
  sudo docker tag      "$image_name:$image_tag" "$image_name:latest"
  sudo docker push     "$image_name:latest"

  if [ "$MASTER" != "" ]; then
    set -x
    $helios hosts
    $helios jobs
    $helios undeploy --all --yes "$job"
    $helios remove         --yes "$job"
    $helios jobs
    $helios create "$job:v1" "$image_name:$image_tag" -p http=8080:8080 --register "$job"
    if [ "$AGENTS" != "" ]; then
      for agent in $AGENTS; do
        agent=$(echo $agent | tr -d '"') # Deleting remaining quotes from $AGENTS
        echo "Deploying job [$job] to agent [$agent]"
        $helios deploy $job   $agent
        $helios status --host $agent
      done
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
