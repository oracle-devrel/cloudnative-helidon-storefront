#!/bin/bash
. ./repoStorefrontConfig.sh
RUNDIR=`pwd`
CONTAINER_DIR=
echo extracting from $RUNDIR
export CONF=$RUNDIR/conf
export CONFSECURE=$RUNDIR/confsecure
export ZIPKINIP=`docker inspect --type container -f '{{.NetworkSettings.IPAddress}}' zipkin`
export STOCKIP=`docker inspect --type container -f '{{.NetworkSettings.IPAddress}}' stockmanager`
echo zipkin ip $ZIPKINIP
docker run  --add-host zipkin:$ZIPKINIP --add-host stockmanager:$STOCKIP --publish  8080:8080 --publish  9080:9080 --rm  --volume $CONF:/$CONTAINER_DIR/conf --volume $CONFSECURE:/$CONTAINER_DIR/confsecure --name storefront  $REPO:0.0.1
