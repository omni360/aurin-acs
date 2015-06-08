package au.com.mutopia.acs.util.geometry;

import au.com.mutopia.acs.models.c3ml.Vertex3D;
import com.google.common.collect.Lists;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.util.AffineTransformation;
import java.util.ArrayList;
import java.util.List;

public class GeometryUtils {
  private static final GeometryFactory wktGeoFactory = new GeometryFactory();

  /**
   * Get the minimum height (Z value) from a list of Vertex3D.
   * @param points The list of Vertex3D.
   * @return The minimum height.
   */
  public static double getMinHeight(List<Vertex3D> points) {
    double minHeight = points.get(0).getZ();
    for (Vertex3D point : points) {
      if (point.getZ() < minHeight) {
        minHeight = point.getZ();
      }
    }
    return minHeight;
  }

  /**
   * Get the maximum height (Z value) from a list of Vertex3D.
   * @param points The list of Vertex3D.
   * @return The maximum height.
   */
  public static double getMaxHeight(List<Vertex3D> points) {
    double maxHeight = points.get(0).getZ();
    for (Vertex3D point : points) {
      if (point.getZ() > maxHeight) {
        maxHeight = point.getZ();
      }
    }
    return maxHeight;
  }

  /**
   * Scales the {@link Polygon} along the x and y axis from the given centroid.
   *
   * @param polygon The {@link Polygon} to be scaled.
   * @param scaleX The scaling factor along the x-axis.
   * @param scaleY The scaling factor along the y-axis.
   * @param centroidX The x coordinate of the centroid.
   * @param centroidY The y coordinate of the centroid.
   * @return The scaled {@link Polygon}.
   */
  public Polygon scalePolygon(Polygon polygon, double scaleX, double scaleY,
      double centroidX, double centroidY) {
    AffineTransformation scaleTransformation =
        AffineTransformation.scaleInstance(scaleX, scaleY, centroidX, centroidY);
    return (Polygon) scaleTransformation.transform(polygon);
  }

  /**
   * Rotates the {@link Polygon} counter clockwise along the z axis from the given centroid.
   *
   * @param polygon The {@link Polygon} to be rotated.
   * @param rotation The angles of rotation (in degrees around the z axes, counter clockwise).
   * @param centroidX The x coordinate of the centroid.
   * @param centroidY The y coordinate of the centroid.
   * @return The rotated {@link Polygon}.
   */
  public Polygon rotatePolygon(Polygon polygon, double rotation, double centroidX,
      double centroidY) {
    AffineTransformation rotateTransformation =
        AffineTransformation.rotationInstance(Math.toRadians(rotation), centroidX, centroidY);
    return (Polygon) rotateTransformation.transform(polygon);
  }

  /**
   * Translates the {@link Polygon} along the x and y axis.
   *
   * @param polygon The {@link Polygon} to be translated.
   * @param translationX The amount of x-axis translation (assumed to be same unit as polygon).
   * @param translationY The amount of x-axis translation (assumed to be same unit as polygon).
   * @return The translated {@link Polygon}.
   */
  public Polygon translatePolygon(Polygon polygon, double translationX, double translationY) {
    AffineTransformation translationTransformation =
        AffineTransformation.translationInstance(translationX, translationY);
    return (Polygon) translationTransformation.transform(polygon);
  }

  /**
   * Creates {@link Polygon} from list of {@link Vertex3D} forming the polygon shell and holes.
   *
   * @param vertex3Ds The list of {@link Vertex3D} forming the polygon shell.
   * @param holes The list of polygon holes.
   * @return The created {@link Polygon}.
   */
  public Polygon polygonFromVertices(List<Vertex3D> vertex3Ds, List<List<Vertex3D>> holes) {
    if (vertex3Ds == null || vertex3Ds.isEmpty()) return null;
    Coordinate[] coordinates = coordinatesFromVertex3Ds(vertex3Ds);
    LinearRing linearRing = wktGeoFactory.createLinearRing(coordinates);
    List<LinearRing> linearRings = new ArrayList<>();
    for (List<Vertex3D> hole : holes) {
      Coordinate[] holeCoordinates = coordinatesFromVertex3Ds(hole);
      linearRings.add(wktGeoFactory.createLinearRing(holeCoordinates));
    }
    return wktGeoFactory.createPolygon(linearRing,
        linearRings.toArray(new LinearRing[linearRings.size()]));
  }

  /**
   * @return The list of {@link Vertex3D} points from a list of {@link Coordinate}.
   */
  public List<Vertex3D> vertex3DsFromCoordinates(Coordinate[] coordinates, double height) {
    List<Vertex3D> points = Lists.newArrayList();
    for (Coordinate coord : coordinates) {
      points.add(new Vertex3D(coord.x, coord.y, height));
    }
    return points;
  }

  /**
   * @return The list of {@link Vertex3D} points from a list of {@link Coordinate}.
   */
  public Coordinate[] coordinatesFromVertex3Ds(List<Vertex3D> vertex3Ds) {
    List<Coordinate> coordinates = Lists.newArrayList();
    for (Vertex3D vertex3D : vertex3Ds) {
      coordinates.add(new Coordinate(vertex3D.getX(), vertex3D.getY(), vertex3D.getZ()));
    }
    return coordinates.toArray(new Coordinate[coordinates.size()]);
  }
}
