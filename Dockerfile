FROM homme/gdal:v1.11.0
MAINTAINER Oliver Lade <oliver.lade@unimelb.edu.au>

EXPOSE 80

ENV ACS_DIR /opt/acs

# Install Java 7, Maven and Apache
RUN apt-get update && apt-get -qq install openjdk-7-jdk maven apache2

ADD . /opt/acs
ADD acs-reverse-proxy.conf /etc/apache2/conf.d/acs-reverse-proxy.conf

RUN cd $ACS_DIR && mvn package

# Configure Apache's reverse proxy.
RUN a2enmod deflate proxy proxy_http

# Start Apache, then start the ACS server.
RUN service apache2 restart
RUN cd $ACS_DIR && java -jar target/aurin-acs.jar server configuration.yml
