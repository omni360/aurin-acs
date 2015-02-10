# Runs the AURIN Asset Conversion Service with local installations of COLLADA2GLTF and BIMserver.
FROM urbanetic/geotree-bimserver
MAINTAINER Oliver Lade <oliver.lade@unimelb.edu.au>

ENV BIMSERVER_PORT 8080
ENV ACS_PORT 8090
EXPOSE $ACS_PORT

ENV ACS_HOME /opt/acs
WORKDIR /

# Install build tools and dependencies to build COLLADA2GLTF, then clean up afterwards.
RUN apt-get update && apt-get -qq install git cmake g++ libxml2-dev libpng12-dev libpcre3-dev pkg-config
RUN git clone --recurse-submodules https://github.com/KhronosGroup/glTF.git
RUN cd glTF/converter/COLLADA2GLTF && cmake . && make
RUN mv glTF/converter/COLLADA2GLTF/bin/collada2gltf /usr/local/bin/
RUN apt-get purge git cmake g++ libxml2-dev libpng12-dev libpcre3-dev pkg-config
RUN rm -rf glTF

# Add the ACS JAR and configuration.
ADD aurin-acs.jar $ACS_HOME/
ADD configuration.yml $ACS_HOME/

# Start the ACS server along with Tomcat to host the local BIMserver.
ENTRYPOINT $CATALINA_HOME/bin/startup.sh && \
           java -jar $ACS_HOME/aurin-acs.jar server $ACS_HOME/configuration.yml
