# Asset Conversion Service

The Asset Conversion Service (ACS) is a Web service for converting data in common geospatial formats
-- both 2D and 3D -- into a format that is compatible with [Atlas-Cesium][atlascesium].

[Cesium][cesium] is a very capable rendering library, but natively it only supports importing of a
few formats (namely GeoJSON, CZML and glTF). The GIS industry is notorious for storing data in a
wide variety of different formats, and conversion is often time-consuming and error prone.

ACS is designed to mitigate this pain, and make it easy for Atlas-based Web applications to accept
the most common GIS file formats out of the box.

## Features

ACS can import the following 2D and 3D formats:

* GeoJSON
* KML
* KMZ
* COLLADA
* IFC


[cesium]: https://cesiumjs.org/
[atlascesium]: https://github.com/urbanetic/atlas-cesium