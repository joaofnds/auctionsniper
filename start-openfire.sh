#!/usr/bin/env sh

docker run \
  --rm \
  --name openfire \
  --detach \
  --publish 9090:9090 \
  --publish 5222:5222 \
  --publish 7777:7777 \
  --volume $PWD/openfire:/var/lib/openfire \
  gizmotronic/openfire:4.4.4