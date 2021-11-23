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
. ./repoStorefrontConfig.sh
mvn package
docker build  --tag $REPO/storefront:latest --tag $REPO/storefront:0.0.1 --file Dockerfile .
docker push $REPO/storefront:latest
docker push $REPO/storefront:0.0.1
echo build and pushed with tags 0.0.1
