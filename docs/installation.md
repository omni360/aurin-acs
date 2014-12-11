# Installing the ACS

ACS is a Web service that is intended to be deployed on a standard Web server.

## Requirements

* A server with access to temporary file storage.
* An HTTP server application such as [Apache][apache].
* Java 8 runtime environment (JRE8)
* [ogr2ogr][ogr] for converting common 2D data formats. A binary version of ogr2ogr is included in
the open-source [FWTools][fwt] toolkit.

[ogr]: http://www.gdal.org/ogr2ogr.html
[fwt]: http://fwtools.maptools.org/

## Installation

This installation guide assumes your server is running Ubuntu with [Apache][apache] installed.

1. [Download the JRE 8 from Oracle][jre8], accepting the license agreement, and [follow these instructions][jre8install] to install it.

2. [Download FWTools for Linux][fwt] and [follow these instructions][fwtinstall] to install it.

3. If building ACS from source, run `mvn package` to compile a [shaded JAR][shade].

4. Install ACS by copying the shaded `aurin-acs.jar` to the server.

5. Start the ACS server by executing the command:

    `java -jar aurin-acs.jar server configuration.yml`

The server is now running on port 8080 and ready to handle conversion requests.

### Mapping to port 80

To serve requests on the standard HTTP port 80 instead, you can use Apache's reverse proxy. Setting this up requires a few simple steps.

First, create a configuration file at `/etc/apache2/conf/acs-reverse-proxy.conf` containing:

```
<IfModule mod_proxy_http.c>

   ProxyRequests Off
   ProxyPreserveHost On

   ProxyPass /acs http://localhost:8080
   ProxyPassReverse /acs http://localhost:8080

</IfModule>
```

2. Open `/etc/apache2/conf/httpd.conf` for editing.

3. Ensure the following lines are uncommented:

    * `LoadModule deflate_module modules/mod_deflate.so`
    * `LoadModule proxy_module modules/mod_proxy.so`
    * `LoadModule proxy_html_module modules/mod_proxy_html.so`
    * `LoadModule proxy_http_module modules/mod_proxy_http.so`
    * `LoadModule xml2enc_module modules/mod_xml2enc.so`

4. Insert the following line at the end of the file:

    `Include conf/acs-reverse-proxy.conf`
    
5. Restart the apache server:

    `sudo service apache2 restart`

6. You can now access the ACS API at `http://<hostname>/acs`.

## Testing

To check that the installation was successful, visit `http://<hostname>/acs`. Although the ACS
does not have a Web application GUI, it should display a message telling you that.

[apache]: https://httpd.apache.org/
[jre8]: http://www.oracle.com/technetwork/java/javase/downloads/index.html
[jre8install]: http://docs.oracle.com/javase/8/docs/technotes/guides/install/linux_server_jre.html
[fwt]: http://fwtools.loskot.net/FWTools-linux-2.0.6.tar.gz
[fwtinstall]: http://fwtools.maptools.org/linux-main.html
[shade]: https://maven.apache.org/plugins/maven-shade-plugin/