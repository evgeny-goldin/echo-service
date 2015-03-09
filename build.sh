#!/bin/bash

set -e

git_sha=$(git log -1 --format="%h")
job_name='echo'
image_name='evgenyg-docker-docker.bintray.io/evgenyg/echo'
image_tag="$git_sha"
helios="helios -z http://${MASTER}:5801"
# Add env variable - Git SHA
create_job="$helios create $job_name:v1 $image_name:$image_tag -p http=8080:8181 --register $job_name --env GIT_SHA=$git_sha"

if [[ "$HOME" =~ ^/Users/ ]]; then # OS X
  docker='docker'
else
  docker='sudo docker'
fi

exec 5>&1

# Ugly hack to fight
# "Error getting container fd208149782248bdbff5f5f0bce8aee44d717b682c9e01e74c0a4d0de569e6bf from driver devicemapper: Error mounting '/dev/mapper/docker-202:1-403169-fd208149782248bdbff5f5f0bce8aee44d717b682c9e01e74c0a4d0de569e6bf' on '/var/lib/docker/devicemapper/mnt/fd208149782248bdbff5f5f0bce8aee44d717b682c9e01e74c0a4d0de569e6bf': no such file or directory"
sudo service docker restart

./gradlew --version
$docker   --version
helios    --version

echo "\$git_sha    = [$git_sha]"
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
  $helios undeploy --all --yes "$job_name" || echo OK
  $helios remove         --yes "$job_name" || echo OK
  $create_job
  $helios jobs
  if [ "$AGENTS" != "" ]; then
    echo "Deploying job [$job_name] to [$AGENTS]"
    $helios deploy "$job_name" $AGENTS

    set +x

    counter="2"
    while [ "$counter" != "0" ]; do
      sleep 5
      counter=$($helios status --job "$job_name" | tee >(cat - >&5) | grep 'PULLING_IMAGE' | wc -l)
    done
    echo "----------------------------------------------------"
    echo "Deployed job [$job_name] as [$image_name:$image_tag]"
    echo "----------------------------------------------------"
  else
    echo ">> \$AGENTS are not defined, Helios job will not be deployed"
  fi
else
  echo ">> \$MASTER in not defined, Helios job will not be created"
fi
