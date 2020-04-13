# Proxy Pool Crawler v1.0

### Introduction
This software is a part of the proxy list builder tool. Its job is to crawl the internet for available proxies and save them in a RDBMS, namely mysql. Its designed to be very simple and easy to extend
### How it works
Public proxies are usually turned offline and online, or their ports rotated frequently. But they mostly don't go away. So the software extracts proxies from free public proxy lists and saves them in the DB. The resulting pool of proxies will contain about 40% working proxies at anytime. The clients have the power to delete or rate proxy quality from the client, this will further help improve the pool.
Each proxy is rated by the `quality` value. This value is between 0-10. The client should try to update the quality of the proxy when they return it back to the pool so as further enrich the pool.
### Configuration
The sofware autmatically creates a config file once its loaded for the first time. THe Config file can contain configurations that may alter the software in certain ways.
* To set interval at which proxy list will be re-crawled
```python
# The re-crawl rate in ms
# default is 2 minutes
 crawl_update_interval = 120000 
```
* To set database details
```python
 db_host = localhost
 db_user = root
 #db_pwd = 1234
 # database name must be existing in db already before
 # running the program. 
 # Please ensure utf8 encoding is used, otherwise you'll have many issues with online unicode characters
 db_name = jm_proxies # default

```
## Quick run
CD into the project folder root, then run the gradle command
```shell
$ gradle run
```
### Building from source
it is a gradle project built using IntelliJ IDE, simply load the project folder from IntelliJ or any IDE that supports Gradle projects. Or you can use the following commands
```shell
$ gradle clean # the clean
$ gradle build # to create a more comprehensive deployment
```
Or build Fat jar - jar bundled with all dependencies
```shell
#OR build Fat jar - jar bundled with all dependencies
$ gradle customFatJar # if you love single jar deployments
```
Please note that fat jar '.jar file' is located in `$PROJECT_ROOT/build/libs`

### Installation
Once you grab the fat jar file, simply run it from the command line with appropriate java flags. To simply run the fat jar, use
```shell
$ java -jar <jar file name>
```

With Love, Ephraim!