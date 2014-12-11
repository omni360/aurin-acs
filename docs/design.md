# ACS Design

The ACS is a Java Web service built with the [Dropwizard][dw] framework. Dropwizard is a collection
of popular Java libraries, including [Jetty][jetty], [Jersey][jersey], [Jackson][jackson] and
[Guava][guava]. ACS also uses [Guice][guice] for dependency injection.

## Components

ACS is relatively simple, and as much as possible keeps all of its processing in memory to avoid the
deployment complexity of persistent storage. The primary components that handle conversion requests
are:

1. RESTful Web service controllers (the `resources` package)
2. Conversion services for the relevant formats (the `conversion` package)
3. Data structures for the outputs (the `models` package)

The whole application is initialised and configured by the Dropwizard configuration class
`AcsRestService` (the `service` package).

## Conversion

ACS uses a variety of open-source libraries to parse the supported file formats, and the contents
are then marshalled manually into the C3ML structure.

Shapefiles and GeoJSON files are first converted to KML with the `ogr2ogr` tool so KML is the only
2D format that needs to be parsed. KML needs to be parsed anyway when used in KMZ files, so this
reduces the number of converters that need to be implemented.

[dw]: http://dropwizard.io/
[jetty]: http://www.eclipse.org/jetty/
[jersey]: https://jersey.java.net/
[jackson]: http://wiki.fasterxml.com/JacksonHome
[guava]: https://github.com/google/guava
[guice]: https://github.com/google/guice