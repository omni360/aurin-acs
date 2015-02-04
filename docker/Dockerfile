FROM urbanetic/collada2gltf
MAINTAINER Oliver Lade <oliver.lade@unimelb.edu.au>

EXPOSE 8080

# Install Java 7 to run the ACS. The Docker Maven plugin will copy the packaged JAR into /.
RUN apt-get update && apt-get -qq install openjdk-7-jdk

# Start the ACS server.
CMD ["java", "-jar", "/aurin-acs.jar", "server", "/configuration.yml"]
