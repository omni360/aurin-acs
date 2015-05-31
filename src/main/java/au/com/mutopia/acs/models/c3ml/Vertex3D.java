package au.com.mutopia.acs.models.c3ml;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

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

  public Vertex3D() {
    latitude = 0.0;
    longitude = 0.0;
    altitude = 0.0;
  }

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

  /**
   * Creates a vertex from a list of values.
   *
   * @param coordinates A 3-tuple of values.
   * @param geographic If true, indicates that the coordinate list is 'backwards' (lon, lat, alt).
   *        Otherwise assume that it is (x, y, z) or (lat, lon, alt).
   *
   */
  public Vertex3D(List<Double> coordinate, boolean geographic) {
    // Note once again that a coordinate list is lon, lat, alt, but the constructor is lat, lon,
    // alt.
    this(coordinate.get(geographic ? 1 : 0), coordinate.get(geographic ? 0 : 1), coordinate.get(2));
  }

  void setX(double x) {
    setLongitude(x);
  }

  void setY(double y) {
    setLatitude(y);
  }

  void setZ(double z) {
    setAltitude(z);
  }

  @JsonIgnore
  public double getX() {
    return longitude;
  }

  @JsonIgnore
  public double getY() {
    return latitude;
  }

  @JsonIgnore
  public double getZ() {
    return altitude;
  }

  public String toString() {
    return String.format("Vertex3D[%f, %f, %f]", longitude, latitude, altitude);
  }

}
