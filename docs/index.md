# Asset Conversion Service

[![Build Status](https://travis-ci.org/urbanetic/aurin-acs.svg)](https://travis-ci.org/urbanetic/aurin-acs)
[![Documentation Status](https://readthedocs.org/projects/aurin-acs/badge/?version=latest)](https://readthedocs.org/projects/aurin-acs/?badge=latest)

The Asset Conversion Service (ACS) is a Web service for converting data in common geospatial formats
-- both 2D and 3D -- into a format that is compatible with [Atlas-Cesium][atlas-cesium].

[Cesium][cesium] is a very capable rendering library, but natively it only supports importing of a
few formats (namely GeoJSON, CZML and glTF). The GIS industry is notorious for storing data in a
wide variety of different formats, and conversion is often time-consuming and error prone.

ACS is designed to mitigate this pain, and make it easy for Web applications built on [Atlas][atlas]
to accept the most common GIS file formats out of the box.


## Features

ACS can import the following 2D and 3D formats:

* ESRI Shapefiles
* GeoJSON
* KML
* KMZ
* COLLADA
* IFC

ACS output is in a custom format called [C3ML](design.md#c3ml), a simple JSON data structure that
contains more standard data formats. Learn more about it in the [Design section](design.md#c3ml).


## Contact

ACS is developed by [Urbanetic][urbanetic] with support from the [Australian Urban Research
Infrastructure Network][aurin] (AURIN). If you're interested in using either [Atlas][atlas] or ACS
in your own project, or are having any trouble, let us know at [hello@urbanetic.net][mail].


[atlas]: http://atlasjs.org/
[cesium]: https://cesiumjs.org/
[atlas-cesium]: https://github.com/urbanetic/atlas-cesium
[urbanetic]: http://www.urbanetic.net/
[aurin]: http://aurin.org.au
[mail]: mailto:hello@urbanetic.net
