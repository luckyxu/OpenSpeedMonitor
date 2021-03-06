#!/bin/bash

file="./version.properties"

if [ -f "$file" ]
then
  while IFS='=' read -r key value
  do
    key=$(echo $key | tr '.' '_')
    eval ${key}=${value}
  done < "$file"
else
  echo "$file not found."
fi

function tagAndPush () {
  tag=$1
  docker tag iteratec/openspeedmonitor iteratec/openspeedmonitor:$tag
  docker push iteratec/openspeedmonitor:$tag
}

tagAndPush $app_version_major
tagAndPush $app_version_minor
tagAndPush $app_version_patch
tagAndPush "latest"
