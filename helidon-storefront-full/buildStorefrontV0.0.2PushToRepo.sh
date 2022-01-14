 #!/bin/bash
echo Updating StatusResource version
mv src/main/java/com/oracle/labs/helidon/storefront/resources/StatusResource.java .
cat StatusResource.java | sed s/0.0.1/0.0.2/ > StatusResourcev2.java
mv StatusResourcev2.java src/main/java/com/oracle/labs/helidon/storefront/resources/StatusResource.java
rm StatusResource.java
. ./repoStorefrontConfig.sh
mvn package
echo Resetting StatusResource version
mv src/main/java/com/oracle/labs/helidon/storefront/resources/StatusResource.java .
cat StatusResource.java | sed s/0.0.2/0.0.1/ > StatusResourcev1.java
mv StatusResourcev1.java src/main/java/com/oracle/labs/helidon/storefront/resources/StatusResource.java
rm StatusResource.java
docker build  --tag "$REPO":latest --tag "$REPO":0.0.2 --file Dockerfile .
docker push "$REPO":latest
docker push "$REPO":0.0.2
echo build and pushed with tags 0.0.2
echo built and pushed v0.0.2
