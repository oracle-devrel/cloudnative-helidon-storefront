# Cloud native development with Helidon - store front

[![License: UPL](https://img.shields.io/badge/license-UPL-green)](https://img.shields.io/badge/license-UPL-green) [![Quality gate](https://sonarcloud.io/api/project_badges/quality_gate?project=oracle-devrel_cloudnative-helidon-storefront)](https://sonarcloud.io/dashboard?id=oracle-devrel_cloudnative-helidon-storefront)

## Introduction
This repo holds the source code to the "Storefront" microservcies in it's "finished" form.

The Storefront micro service is an example microservice that is used in a number of Oracle labs to show how to use Helidon to create a microservcies with business logic (admitedly very simple in this case) and to use the Helidon REST Client to communicate with another misrocervice (The "StockManager")


## Getting Started
This is a "finished" version of the Storefront microservice. As a "finished" version of the code this is what someone who has completed the lab would expect to have created (approximately).
This code is being provided here for several reasons:
1/ As refrence for people who for whatevery reason do not want to run through the lab themselves.
2/ As a starting point for future lab development, to provide the source code of a simple Helidon based microservice without having to create it from scratch

### Prerequisites
This project uses Java 11 and Helidon. It was created using a reasonably recent version of Eclipse for Enterprise developers (2021-06 at the point of rwiting this). Eclipse was chosen due to it's open source license and an IDE was used for ease of development (and easi of running the associated labs) It uses Maven to run the builds. It also used JIB to create docker images - so if you want to have Maven perform that step you will need docker installed and running.
If you want to create native images using GraalVM you will need both GraalVM and the nbative image plugin installed and configured in your path.
For ease of building beans this project also uses Lombok,a wonderfully useul compile time tool. Lombok will be automatically used when building with Maven, but if using an IDE like Eclipse you will need to install it in the IDE, see the [Project Lombok](https://projectlombok.org/) documentation for details.

## Notes/Issues
This is a "finished" version of the Storefront microservice. As such there is little work expected to be done to it, though at various points in the future it may be updated to use newer versions of Helidon or other dependencies, or include hooks to code or microservcies used in yet to be developed lab modules.

## URLs
The link below is to the "full" lab that startes with the templates, ends up with something similar to this code and then progresses to creating docker images and finally to deploying in Kubernetes. This codce represents the "finished" Storefront code as of the end of the Helidon labs
[Associated base lab](https://apexapps.oracle.com/pls/apex/dbpm/r/livelabs/view-workshop?wid=728)

## Contributing
This project is open source.  Please submit your contributions by forking this repository and submitting a pull request!  Oracle appreciates any contributions that are made by the open source community. Please consider that any changes or modifications will need to remain in sync with the template code based on this content, any  and the associated Labs which use that template code.

## License
Copyright (c) 2021 Oracle and/or its affiliates.

Licensed under the Universal Permissive License (UPL), Version 1.0.

See [LICENSE](LICENSE) for more details.
