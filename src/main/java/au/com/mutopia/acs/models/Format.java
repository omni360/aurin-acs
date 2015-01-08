package au.com.mutopia.acs.models;


/**
 * Enumerates the supported file formats.
 */
public enum Format {
  CITYGML("citygml"),
  COLLADA("dae"),
  IFC("ifc"),
  GEOJSON("json"),
  KML("kml"),
  KMZ("kmz"),
  SHP("shp"),
  ZIP("zip");

  private final String value;

  Format(String value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return value;
  }

  /**
   * Looks up the {@link Format} that has as a value the given string.
   * 
   * @param value The value to look up a format by.
   * @return The {@link Format} with the given value.
   */
  public static Format getByValue(String value) {
    for (Format format : values()) {
      if (format.toString().equals(value)) {
        return format;
      }
    }
    return null;
  }

}
