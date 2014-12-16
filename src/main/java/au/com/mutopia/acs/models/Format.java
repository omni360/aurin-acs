package au.com.mutopia.acs.models;


/**
 * Enumerates the supported file formats.
 */
public enum Format {
  CITYGML("citygml"), COLLADA("dae"), IFC("ifc"), GEOJSON("json"), KML("kml"), KMZ("kmz"), SHP(
      "shp");

  private final String value;

  Format(String value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return value;
  }

}
