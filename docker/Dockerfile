# Runs the AURIN Asset Conversion Service with local installations of COLLADA2GLTF and BIMserver.
FROM urbanetic/geotree-bimserver
MAINTAINER Oliver Lade <oliver.lade@unimelb.edu.au>

ENV BIMSERVER_PORT 8080
ENV ACS_PORT 8090
EXPOSE $ACS_PORT

ENV ACS_HOME /opt/acs
WORKDIR /

# Add the binaries for ogr2ogr and collada2gltf.
RUN apt-get update -y && apt-get install -y gdal-bin 
ADD collada2gltf /usr/local/bin/

# Add the ACS JAR and configuration.
ADD aurin-acs.jar $ACS_HOME/
ADD configuration.yml $ACS_HOME/

# Start the ACS server along with Tomcat to host the local BIMserver.
ENTRYPOINT $CATALINA_HOME/bin/startup.sh && \
           java -jar $ACS_HOME/aurin-acs.jar server $ACS_HOME/configuration.yml
