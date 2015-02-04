FROM urbanetic/collada2gltf
MAINTAINER Oliver Lade <oliver.lade@unimelb.edu.au>

EXPOSE 8080

# Install Java 7 to run the ACS. The Docker Maven plugin will copy the packaged JAR into /opt/acs.
RUN apt-get update && apt-get -qq install openjdk-7-jdk

# Start Apache, then start the ACS server.
ENTRYPOINT java -jar $ACS_DIR/aurin-acs.jar server $ACS_DIR/configuration.yml
