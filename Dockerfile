FROM homme/gdal:v1.11.0
MAINTAINER Oliver Lade <oliver.lade@unimelb.edu.au>

EXPOSE 80

ENV ACS_DIR /opt/acs

# Install Java 7, Maven and Apache.
RUN apt-get update && apt-get -qq install openjdk-7-jdk maven apache2

ADD . /opt/acs
ADD acs-reverse-proxy.conf /etc/apache2/conf.d/acs-reverse-proxy.conf

# Compile a shaded JAR of ACS to aurin-acs.jar.
RUN cd $ACS_DIR && mvn package && mv target/aurin-acs-*.jar target/aurin-acs.jar

# Configure Apache's reverse proxy.
RUN a2enmod deflate proxy proxy_http

# Start Apache, then start the ACS server.
ENTRYPOINT service apache2 restart && java -jar $ACS_DIR/target/aurin-acs.jar server $ACS_DIR/configuration.yml