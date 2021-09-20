 #!/bin/bash
mvn package
docker build  --tag storefront --file Dockerfile .
