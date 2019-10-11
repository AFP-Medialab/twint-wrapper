# Twint Wrapper

Twint Wrapper is a java Spring Boot application that expose operations in front of [Twint](https://github.com/twintproject/twint) scraping tool.


This tool supported following operations:

* collect  => ask for a twitter scraping with search criteria 
* status => check the scraping process
* collect-history => get request history
* collect-update => re-process an existing scraping

All operations are defined through swagger page **/twint-wrapper/swagger-ui.html**

Target build is a Docker image that combine Twint image and the java Spring-Boot application.

Full application is run using a docker-compose script

	$src/main/script/prod
	$docker-compose up -d
	

## Requirements
* ElasticSearch to index twitter scraping results
* MySQL to manage scraping sessions
* Docker
* Twint

 src/scripts/dev folder gives docker-compose file to start MySQL and ElasticSearch
 
	$cd src/scripts/dev
	$docker-compose up -d
 
 An other script starts MySQL, ElasticSearch and Kibana

	$ cd src/scripts/dev
	$ docker-compose -f docker-compose.yml up -d


### Twint Docker build

Twint is the only scraper that is supported so far.
Project pom.xml defined the build of twint bases on a forked of the twint project.
Dockerfile that build twint image is located src/main/docker/twint

To build twint image with maven run:

	mvn docker:build -P twint-docker

Test twint image:

	docker run --rm -it twint:2.1.2 "twint -s '#pactemondialsurlesmigrations' --since '2018-12-01 00:00:00' --until '2018-12-15 00:00:00' -l fr --count"

## Builds
Default build build Spring-Boot application as jar file. Default profile is dev

	mvn package
	
Build docker image with Spring-Boot application and twint application

	mvn package -P prod
	
## Run service
Twitter-gateway can be run with different profiles (Default, dev & prod).

default:

	java -jar twint-wrapper.jar
	
dev:

	java -Dspring.profiles.active=dev -jar twint-wrapper.jar
	
prod

	java -Dspring.profiles.active=prod -jar twint-wrapper.jar

 




