# Runs the AURIN Asset Conversion Service with local installations of COLLADA2GLTF and BIMserver.
#
# Based on the COLLADA2GLTF image to avoid build.
FROM urbanetic/collada2gltf
MAINTAINER Oliver Lade <oliver.lade@unimelb.edu.au>

ENV BIMSERVER_PORT 8080
ENV ACS_PORT 8081
EXPOSE $ACS_PORT

ENV TOMCAT_HOME /usr/local/tomcat
ENV ACS_HOME /opt/acs
ENV BIMSERVER_HOME $TOMCAT_HOME/webapps/bimserver

# Install Java 7 to run the ACS. The Docker Maven plugin will copy the packaged JAR into /.
RUN apt-get update && apt-get -qq install openjdk-7-jdk tomcat7
# Download and unzip the BIMserver WAR file to Tomcat's webapps directory.
ADD https://github.com/opensourceBIM/BIMserver/releases/download/1.3.0-FINAL-2014-04-25/bimserver-1.3.0-FINAL-2014-04-25.war $BIMSERVER_APP.war
RUN unzip $BIMSERVER_HOME.war -d $BIMSERVER_HOME && rm $BIMSERVER_HOME.war

# Add the Geotree plugin to process IFC into ACS's format.
ADD https://github.com/urbanetic/geotree-bimserver-plugin/releases/download/0.1.0/geotree-bimserver-plugin-0.1.0.jar $BIMSERVER_APP/WEB-INF/plugins/geotree-bimserver-plugin.jar

# Add the ACS JAR and configuration.
ADD aurin-acs.jar $ACS_HOME
ADD configuration.yml $ACS_HOME

# Start the BIMserver process and send a request to perform the initial admin setup.
# Admin username is "admin@bimserver.org", and password is "admin".
# TODO(orlade): Do this more directly by writing to database.
# The 30 second sleep is required, since starting Tomcat synchronously doesn't yield once started.
RUN catalina.sh start && \
    echo "Waiting 30 secs for BIMserver to initialize..." && \
    sleep 30 && \
    curl -X POST localhost:$BIMSERVER_PORT/bimserver/json -d '{"request":{"interface":"org.bimserver.AdminInterface","method":"setup","parameters":{"siteAddress":"http://localhost:$BIMSERVER_PORT/bimserver","smtpServer":"bimserver.org","smtpSender":"admin@bimserver.org","adminName":"Administrator","adminUsername":"admin@bimserver.org","adminPassword":"admin"}}}'

# Start the ACS server along with Tomcat to host the local BIMserver.
ENTRYPOINT catalina.sh start && java -jar $ACS_HOME/aurin-acs.jar server $ACS_HOME/configuration.yml
