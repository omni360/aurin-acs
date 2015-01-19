package au.com.mutopia.acs.models.c3ml;

import lombok.Data;

/**
 * Represents a geographic point in 3D space.
 */
@Data
public class Vertex3D {

  /** Latitude value for this vertex (degree decimals). */
  private double latitude;

  /** Longitude value for this vertex (degree decimals). */
  private double longitude;

  /** Altitude value for this vertex (meters). */
  private double altitude;

  /**
   * Creates the vertex with the given values.
   * 
   * @param latitude The latitude of the point.
   * @param longitude The longitude of the point.
   * @param altitude The altitude of the point.
   */
  public Vertex3D(double latitude, double longitude, double altitude) {
    this.latitude = latitude;
    this.longitude = longitude;
    this.altitude = altitude;
  }

  public String toString() {
    return String.format("Vertex3D[%f, %f, %f]", longitude, latitude, altitude);
  }

}
