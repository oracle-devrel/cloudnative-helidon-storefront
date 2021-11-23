#!/bin/bash
RUNDIR=`pwd`
CONTAINER_DIR=
echo executing in $RUNDIR
export CONF=$RUNDIR/conf
export CONFSECURE=$RUNDIR/confsecure
export WALLET=$RUNDIR/Wallet_ATP
export ZIPKINIP=`docker inspect --type container -f '{{.NetworkSettings.IPAddress}}' zipkin`
echo zipkin ip $ZIPKINIP
docker run  --env-file $RUNDIR/database-env --add-host zipkin:$ZIPKINIP --publish 8081:8081 --publish 9081:9081 --rm --volume $WALLET:$CONTAINER_DIR/Wallet_ATP --volume $CONF:/$CONTAINER_DIR/conf  --volume $CONFSECURE:/$CONTAINER_DIR/confsecure --name stockmanager stockmanager
