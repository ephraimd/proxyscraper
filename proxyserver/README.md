# Proxy Pool Server v1.0

### Introduction
This software is a part of the proxy list builder tool. Its job is to serve clients with proxies stored in an RDBMS, mysql. Its designed to be very simple and easy to extend.

### Configuration
The sofware autmatically creates a config file once its loaded for the first time. THe Config file can contain configurations that may alter the software in certain ways.
* To set database details
```python
 db_host = localhost
 db_user = root
 db_pwd = ***
 # database name must be existing in db already before
 # running the program. 
 # Please ensure utf8 encoding is used, otherwise you'll have many issues with online unicode characters
 db_name = jm_proxies # default
```
* To set other details
```python
 port = 8090 # this is the default
```

### Usage
There are three routes on the server through with you can interact with the proxy list.
* **/get_proxy** - you get the proxies from the route. You can specifiy the proxy configuration you want via the following parameters. All parameters are optional
    - country
    - security
    - quality (uses >= value in the db)
    - type
    - works_on_google (no value required)
    - limit
```url
http://localhost:8090/get_proxy?country=brazil&works_on_google&security=https&type=anonymous&limit=4'
```
* **/release_proxy** - when you get proxies, those proxies are locked and cannot be requested by anyone else until you release them. This route handles releasing of proxies. YOu can further enrich the proxy list by helping to specify useful attributes OPTIONALLY. 
    - ip_address (mandatory)
    - port (mandatory)
    - correct_security 
    - correct_type
    - correct_country
    - works_on_google (no value required)
    - update_quality (max is 10. You can specify how reliable this proxy here)
```url
http://localhost:8090/release_proxy?ip_address=45.234.144.106&port=8080&correct_security=http&correct_type=elite&works_on_google=true&update_quality=4'
```
* **/delete_proxy** - when a proxy is certainly non-reponsive or no longer serves, you can delete the proxy via this route
    - ip_address (mandatory)
    - port (mandatory)
```url
http://localhost:8090/delete_proxy?ip_address=45.234.144.106&port=8080
```
#### Response Format
The server API responds with `text/json`. The general contract is in the format
```json
{
    "ack":"ok", //'ok' or 'error'
    "message":"" //string or object
}
```
Sample response endpoint scenario
```url
http://localhost:8090/get_proxy?quality=3
```
```json
{
    "ack":"ok",
    "message":{
        "proxies":[{
                "ip":"45.234.144.106",
                "port":8080,"quality":4
            }]
        }
}
```
#### How it works
The server connects to the central mysql db and retrieves only the latest entries for use.
### Building from source
it is a gradle project built using IntelliJ IDE, simply load the project folder from IntelliJ or any IDE that supports Gradle projects. You can use the following commands
```shell
$ gradle clean # the clean
$ gradle build # to create a more comprehensive deployment
#OR build Fat jar - jar bundled with all dependencies
$ gradle customFatJar # if you love single jar deployments
```
Please note that fat jar '.jar file' is located in `build/libs`

### Installation
Once you grab the fat jar file, simply run it from the command line with appropriate java flags. To simply run the fat jar, use
```shell
$ java -jar <jar file name>
```

With Love, Ephraim!