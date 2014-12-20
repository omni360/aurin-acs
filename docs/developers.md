# Developer Guide

This guide provides instructions on how to checkout, build and deploy the ACS. You can also read the
[ACS Javadocs][javadocs].

## Checkout

The source code for ACS is managed using Git on [GitHub][github]. You can check it out with:

    $ git clone https://github.com/urbanetic/aurin-acs.git


## Build

ACS uses [Maven][maven] for dependency management and all build processes. The following Maven goals
are the most useful:

* Run the tests: `mvn integration-test`
* Package for manual deployment: `mvn package` (also runs tests)
* Compile the Javadocs: `mvn site`


## Deployment

### Automated

ACS uses [Travis][travis] for continuous integration and deployment. This means a build is run every
time we push to Git, which compiles and runs all of the tests. If pushing to the master branch with
a version tag, Travis also deploys the system to [Heroku][heroku].

The configuration for Travis is contained within the [.travis.yml][travis.yml] file. You will need
to modify this to configure your own Travis deployment.

TODO: `.travis.yml` for Heroku.

### Manual

To deploy manually, run `mvn package`, which will compile a [shaded JAR][shade] called
`aurin-acs-<version>.jar`. This JAR file contains all of the conversion logic, as well as a
[Jetty][jetty] server that can be run on any server with Java installed. 

To run the server, run the command:

    $ java -jar /path/to/aurin-acs/target/aurin-acs-<version>.jar server /path/to/aurin-acs/configuration.yml

### Heroku

To deploy the ACS to Heroku, create a [`Procfile`][procfile] with:

    web    java $JAVA_OPTS -Ddw.http.port=$PORT -Ddw.http.adminPort=$PORT -jar target/aurin-acs-<version>.jar server configuration.yml

Push the repository to your Heroku remote, wait a couple of minutes, and the service should be
available.


[github]: https://github.com/urbanetic/aurin-acs
[javadocs]: http://javadocs.acs.urbanetic.net
[maven]: https://maven.apache.org/
[travis]: https://travis-ci.org/
[travis.yml]: https://github.com/urbanetic/aurin-acs/blob/develop/.travis.yml
[heroku]: https://heroku.com/
[shade]: https://maven.apache.org/plugins/maven-shade-plugin/
[jetty]: http://eclipse.org/jetty/
[procfile]: https://devcenter.heroku.com/articles/procfile