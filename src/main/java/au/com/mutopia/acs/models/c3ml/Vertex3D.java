package au.com.mutopia.acs.models.c3ml;

import lombok.Data;

@Data
public class Vertex3D {

  /** Latitude value for this vertex (degree decimals). */
  private double latitude;

  /** Longitude value for this vertex (degree decimals). */
  private double longitude;

  /** Altitude value for this vertex (meters). */
  private double altitude;

  public Vertex3D(double latitude, double longitude, double altitude) {
    this.latitude = latitude;
    this.longitude = longitude;
    this.altitude = altitude;
  }

}
