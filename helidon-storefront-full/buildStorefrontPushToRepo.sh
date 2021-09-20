 #!/bin/bash
. ./repoStorefrontConfig.sh
mvn package
docker build  --tag $REPO/storefront:latest --tag $REPO/storefront:0.0.1 --file Dockerfile .
docker push $REPO/storefront:latest
docker push $REPO/storefront:0.0.1
echo build and pushed with tags 0.0.1
