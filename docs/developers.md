# Developer Guide

This guide provides instructions on how to deploy the ACS into production. The simplest method is to
use the ACS Docker image, however instructions to checkout, build and deploy the ACS from source are
included as well.

For documentation of the source code, check out the [ACS Javadocs][javadocs].

## Docker

[**Docker**][docker] is a lightweight virtualisation platform for Linux that allows applications to
be installed into an isolated environment with all of their dependencies. These "images" may be
deployed (and should behave exactly the same) on any Linux machine with Docker installed. Docker
makes administration a breeze: configure once, run anywhere. Install it with
`apt-get install docker.io`.

The `Dockerfile` in the ACS repository contains the instructions to build the ACS Docker image. This
is built automatically on [Dockerhub][dockerhub], but can also be [built manually][docker-build].

To run the ACS as a server, simply execute as root
`docker run -d -p 80:80 urbanetic/aurin-acs -D FOREGROUND`.

To instruct the server to run the ACS image every time it starts, run the following as root:

`crontab -l | { cat; echo "@reboot docker run -d -p 80:80 urbanetic/aurin-acs -D FOREGROUND"; } | crontab -`

To update a running instance of the ACS, run as root:

`docker pull urbanetic/aurin-acs`

This will download the most recently built Docker image, which Dockerhub will have automatically
built from the latest commit. After that, simply `docker stop` any running ACS containers and use
the same `docker run` command to start the newly-pulled version. Or if you're lazy, just reboot the
server.


## Checkout

The source code for ACS is managed using Git on [GitHub][github]. You can check it out with:

    $ git clone https://github.com/urbanetic/aurin-acs.git

If you don't want or need Git installed, you can download a ZIP from [GitHub][gitzip].


## Build

ACS uses [Maven][maven] for dependency management and all build processes. The following Maven goals
are the most useful:

* Run the tests: `mvn integration-test`
* Package for manual deployment: `mvn package` (also runs tests)
* Compile the Javadocs: `mvn site`

## Installation

### Pre-requisites

* A server with access to temporary file storage.
    * This guide assumes your server is running a recent version of Ubuntu.
* Java 7 runtime environment (JRE7) (JDK required to build from source)
* An HTTP server application such as [Apache][apache].
* [ogr2ogr][ogr] for converting common 2D data formats. A binary version of ogr2ogr is included in
the open-source [FWTools][fwt] toolkit.

### Process

1. Install OpenJDK 7:

    `sudo apt-get install openjdk-7-jdk`

2. [Download FWTools for Linux][fwt] and [follow these instructions][fwtinstall] to install it.

3. For integration tests using `ogr2ogr`, set the `GDAL_DATA` environment variable to the `/data`
   subdirectory in the FWTools directory.

4. If building ACS from source, run `mvn package` to compile a [shaded JAR][shade].

5. Install ACS by copying the shaded `aurin-acs.jar` to the server.

6. Start the ACS server by executing the command:

    `java -jar aurin-acs.jar server configuration.yml`

The server is now running on port 8080 and ready to handle conversion requests.

### Mapping to port 80

To serve requests on the standard HTTP port 80 instead, you can use Apache's reverse proxy. Setting
this up requires a few simple steps. Of course, if you prefer, you can simply change the application
server to serve on port 80 directly in `configuration.yml`.

First, create a configuration file at `/etc/apache2/conf.d/acs-reverse-proxy.conf` containing:

```
<IfModule mod_proxy_http.c>
   ProxyRequests Off
   ProxyPreserveHost On

   ProxyPass / http://localhost:8080/
   ProxyPassReverse / http://localhost:8080/
</IfModule>
```

2. Enable the necessary Apache proxy mods:

    `sudo a2enmod deflate proxy proxy_http`
    
3. Restart the Apache server:

    `sudo service apache2 restart`

4. You can now access the ACS API at `http://<hostname>/`.

## Testing

To check that the installation was successful, visit `http://<hostname>/acs`. Although the ACS
does not have a Web application GUI, it should display a message telling you that.


[apache]: https://httpd.apache.org/
[docker]: https://www.docker.com/
[dockerhub]: https://registry.hub.docker.com/u/urbanetic/aurin-acs/
[docker-build]: https://docs.docker.com/reference/commandline/cli/#build
[fwt]: http://fwtools.loskot.net/FWTools-linux-2.0.6.tar.gz
[fwtinstall]: http://fwtools.maptools.org/linux-main.html
[github]: https://github.com/urbanetic/aurin-acs
[gitzip]: https://github.com/urbanetic/aurin-acs/archive/develop.zip
[heroku]: https://heroku.com/
[javadocs]: http://javadocs.acs.urbanetic.net
[jetty]: http://eclipse.org/jetty/
[maven]: https://maven.apache.org/
[ogr]: http://www.gdal.org/ogr2ogr.html
[procfile]: https://devcenter.heroku.com/articles/procfile
[shade]: https://maven.apache.org/plugins/maven-shade-plugin/
[travis]: https://travis-ci.org/
[travis.yml]: https://github.com/urbanetic/aurin-acs/blob/develop/.travis.yml