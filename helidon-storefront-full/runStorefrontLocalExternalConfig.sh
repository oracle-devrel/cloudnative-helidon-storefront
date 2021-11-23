#!/bin/bash
#
# Copyright (c) 2019, 2020, 2021 Oracle and/or its affiliates. All rights reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
RUNDIR=`pwd`
CONTAINER_DIR=
echo executing in $RUNDIR
export CONF=$RUNDIR/conf
export CONFSECURE=$RUNDIR/confsecure
export ZIPKINIP=`docker inspect --type container -f '{{.NetworkSettings.IPAddress}}' zipkin`
export STOCKIP=`docker inspect --type container -f '{{.NetworkSettings.IPAddress}}' stockmanager`
echo zipkin ip $ZIPKINIP
docker run  --add-host zipkin:$ZIPKINIP --add-host stockmanager:$STOCKIP --publish 8080:8080 --publish 9080:9080 --rm  --volume $CONF:/$CONTAINER_DIR/conf --volume $CONFSECURE:/$CONTAINER_DIR/confsecure --name storefront storefront
