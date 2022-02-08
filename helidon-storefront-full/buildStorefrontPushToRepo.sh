 #!/bin/bash
. ./repoStorefrontConfig.sh
mvn package
docker build  --tag "$REPO":latest --tag "$REPO":0.0.1 --file Dockerfile .
docker push "$REPO":latest
docker push "$REPO":0.0.1
echo build and pushed with tags 0.0.1
