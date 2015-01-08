FROM homme/gdal:v1.11.0
MAINTAINER Oliver Lade <oliver.lade@unimelb.edu.au>

EXPOSE 80

ENV ACS_DIR /opt/acs
ENV APACHE_CONF /etc/apache2/conf
ENV HTTPD_CONF $APACHE_CONF/httpd.conf

# Install Java 7, Maven and Apache
RUN apt-get update && apt-get -qq install openjdk-7-jdk maven apache2

ADD . /opt/acs
ADD acs-reverse-proxy.conf $APACHE_CONF/acs-reverse-proxy.conf

RUN cd $ACS_DIR && mvn package

# Configure Apache's reverse proxy.
RUN sed -i 's/#LoadModule deflate_module/LoadModule deflate_module/' $HTTPD_CONF
RUN sed -i 's/#LoadModule proxy_module/LoadModule proxy_module/' $HTTPD_CONF
RUN sed -i 's/#LoadModule proxy_html_module/LoadModule proxy_html_module/' $HTTPD_CONF
RUN sed -i 's/#LoadModule proxy_http_module/LoadModule proxy_http_module/' $HTTPD_CONF
RUN sed -i 's/#LoadModule xml2enc_module/LoadModule xml2enc_module/' $HTTPD_CONF
RUN "Include conf/acs-reverse-proxy.conf" >> $HTTPD_CONF

# Start Apache, then start the ACS server.
RUN service apache2 restart
RUN cd $ACS_DIR && java -jar target/aurin-acs.jar server configuration.yml
