# Twint Wrapper

Twint Wrapper is a java Spring Boot application that expose operations in front of [Twint](https://github.com/twintproject/twint) scraping tool.


This tool supported following operations:

* collect  => ask for a twitter scraping with search criteria 
* status => check the scraping process
* collect-history => get request history
* collect-update => re-process an existing scraping

All operations are defined through swagger page **/twint-wrapper/swagger-ui.html**

Target build is a Docker image that combine Twint docker image and the java Spring-Boot application.



## Requirements
* ElasticSearch to index twitter scraping results
* MySQL to manage scraping sessions
* Docker
* Twint
* [FusionAuth](https://fusionauth.io/) to protect operations to authorized users

![architecture](doc/twint-wrapper-components.png)

### TwintPlus Docker build

TwintPlus is the scraper that is supported. It must be build as a docker image as a prerequisite to any new development.


Test twint image:

	docker run --rm -it twintplus "tplus -cq %23deepfake --since 2020-05-01 --until 2020-05-19 -ee elasticsearch:9200 -es --index-name tsna --limit 10000"
	
#### Env variable for TwintPlus
 There is several profiles that allow to increase or decrease scraping limits. The following results can be override with environment values:
 
 * TWINT_LIMIT_DEFAULT (15k)
 * TWINT_LIMIT_MIN (15k)
 * TWINT_LIMIT_DEFAULT (30k)
 * TWINT_LIMIT_MAX (60k)
 
## How to develop

 src/scripts/dev folder gives docker-compose file to start MySQL, ElasticSearch and FusionAuth.
 
	$cd src/scripts/dev
	$docker-compose up -d
 By default, the application will connect to the different containers 
 
 
 An other script add Kibana to monitor ElasticSearch Index.

	$ cd src/scripts/dev
	$ docker-compose -f docker-compose-kibana.yml up -d


Some linux system need to increase it virtual memory cf [https://www.elastic.co/guide/en/elasticsearch/reference/current/vm-max-map-count.html](https://www.elastic.co/guide/en/elasticsearch/reference/current/vm-max-map-count.html). If elasticsearch fails at startup:

	sudo sysctl -w vm.max_map_count=262144

## Builds
Default build build Spring-Boot application as jar file. Default profile is dev

	mvn clean package
	
Build docker image with Spring-Boot application and twint application

	mvn clean package -P prod
	
## Run service
Twitter-gateway can be run with different profiles (Default, dev & prod).

default:

	java -jar twint-wrapper.jar
	
dev:

	java -Dspring.profiles.active=dev -jar twint-wrapper.jar
	
prod

	java -Dspring.profiles.active=prod -jar twint-wrapper.jar

Full application with all components run with docker-compose

	$src/main/script/prod
	$docker-compose up -d
	

 
## FusionAuth configuration

FusionAuth executed with docker-compose use a setup sql script that embedded minimum configuration to add authorized users.
This inialisation will be set at first docker-compose startup.
Supported version is 1.15.* (tested with 1.15.2 & 1.15.5)

Setup to grant users are:

* Register user 
* Approved user through fusionAuth UI
* User ask a register code with his email address
* User ask a token with his received register code to use scraping operations. 


FusionAuth is accessible locally:
* [http://localhost:9011](http://localhost:9011)
* login: *weverify@weverify.eu*
* password: *Weverify$*

#### Email configuration with google account
FusionAuth send registration code by email.
To enable this feature, a SMTP server need to be setup in fusionAuth. 

Example with a Gmail Account:

 Tenants > Weverify (Edit) > Email (SMTP settings)

* Host: smtp.gmail.com
* Port: 587
* Username: <your google email address>
* Change password: <your google application generated password>  (password is stored encrypted locally)
* Security: TLS

To use your google account as a SMTP Gateway, you must [turn on 2-step Verification](https://support.google.com/accounts/answer/185839) in your account
* Account
* Security
* Turn on 2-step verification

Add an access to your local FusionAuth by creating [an application password](https://support.google.com/accounts/answer/185833?hl=en)

* Account
* Security
* Application password
* Other -> Give a Name -> Generated
* Copy paste the generated password to FusionAuth Email configuration. 
 

## Slack notification
It is possible to get notify to your slack account when a user registered.

To add this feature just set the environment variable $SLACK_URL in the run time with your own slack URL.

## Twitie configuration
There is 2 Twitie version. Application is supporting both.
Differences between the two version is about request format and endPoint calls. Response format is the same.
### Twitie Legacy
Legacy Twitie version that support only English

* URL: <DOMAIN>/gate/process?annotations=:Person,:UserID,:Location,:Organization
* Body format: String
* Content-Type: text/plain

### Twitie Spacy (Default local use)
Spacy base Twitie version. Can support following langues (de, el, en, es, fr, it, pt) default is english

* URL: <DOMAIN>/process
* Body format: json
* Content-Type: application/json

Example request:

```json
{
    "type":"text",
    "content": "The Wall Street Journal always “forgets” to mention that the ratings for the White House Press 
    Briefings are “through the roof” (Monday Night Football, Bachelor Finale, according to @nytimes) & is only way 
    for me to escape the Fake News & get my views across. WSJ is Fake News!",
    "features": {
        "lang":"en"
    }
}

```

### Properties list for Twitie

* TWITIE_IS_SPACY (true): set which version of Twitie to used
* TWITIE_URL (http://localhost:8081/process): Twitie service endPoint URL
* TWITIE_THREADS (8): number of threads used to process tweets with Twitie
* TWITIE_ES_PAGESIZE (100): ES request size result for Twitie processing 
* TWITIE_ES_BULKSIZE (5000): ES update bulk size after Twitie processing

