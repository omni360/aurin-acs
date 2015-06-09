package au.com.mutopia.acs.util.mesh;

import au.com.mutopia.acs.models.c3ml.Vertex3D;
import au.com.mutopia.acs.util.mesh.clipper.Poly;
import au.com.mutopia.acs.util.mesh.clipper.PolyDefault;
import com.google.common.collect.Lists;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Ints;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.operation.union.CascadedPolygonUnion;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.log4j.Log4j;

/**
 * An utility class for Mesh related actions, such as scale, rotate, transform meshes,
 * obtaining polygon contour from mesh.
 */
@Log4j
public class MeshUtil {
  private static final GeometryFactory wktGeoFactory = new GeometryFactory();

  /**
   * Earth’s radius, sphere (in meters).
   */
  private static final float EARTH_RADIUS = 6378137;

  /**
   * View from 10,000 meters below the ground.
   */
  private static final float UNDERGROUND_VIEW = -10000;

  /**
   * The length of a 4x4 transformation matrix.
   */
  private static final int MATRIX_SIZE = 16;

  /**
   * Creates a list of positions forming the flatten mesh.
   *
   * @param positions The list of positions forming the mesh.
   * @return The list of positions forming the flatten mesh.
   */
  public List<Double> getFlattenMeshPositions(List<Double> positions) {
    List<Double> flattenPositions = new ArrayList<>();
    for (int i = 0; i < positions.size(); i += 3) {
      // position.x
      flattenPositions.add(positions.get(i));
      // position.y
      flattenPositions.add(positions.get(i + 1));
      // position height
      flattenPositions.add(positions.get(0));
    }
    return flattenPositions;
  }

  /**
   * Converts the list of positions in meters to decimal degrees.
   *
   * @param positions The list of positions forming the mesh.
   * @param lat The latitude of the mesh's geographic location (degree decimal).
   * @param lon The longitude of the mesh's geographic location (degree decimal).
   * @param alt The altitude of the mesh's geographic location (meter).
   * @return The list of positions in meters to decimal degrees.
   */
  public List<Double> convertPositionsToDecimalDegrees(List<Double> positions, double lat,
      double lon, double alt) {
    List<Double> decimalDegreesPositions = Lists.newArrayList();
    for (int i = 0; i < positions.size(); i += 3) {
      Vertex3D vertex3D =
          toDecimalDegrees(positions.get(i), positions.get(i + 1), positions.get(i + 2), lat, lon,
              alt);
      decimalDegreesPositions.add(vertex3D.getLatitude());
      decimalDegreesPositions.add(vertex3D.getLongitude());
      decimalDegreesPositions.add(vertex3D.getAltitude());
    }
    return decimalDegreesPositions;
  }

  /**
   * Creates a {@link Polygon} from mesh positions, triangle indices, polygon height and the
   * geographic coordinate of the mesh {latitude, longitude, altitude}.
   *
   * @param pos The list of positions forming the mesh.
   * @param tris The list of triangles indices forming the mesh.
   * @param height The height of the polygon.
   * @param lat The latitude of the mesh's geographic location (degree decimal).
   * @param lon The longitude of the mesh's geographic location (degree decimal).
   * @param alt The altitude of the mesh's geographic location (meter).
   * @return The {@link Polygon} converted from mesh.
   */
  public Polygon getPolygon(List<Double> pos, List<Integer> tris, double height,
      double lat, double lon, double alt) {
    try {
      List<Double> newPos = convertPositionsToDecimalDegrees(pos, lat, lon, alt);
      List<Polygon> polygonsFromMeshData = getPolygonsFromMeshData(newPos, tris);
      Geometry geometry = CascadedPolygonUnion.union(polygonsFromMeshData);
      if (geometry instanceof MultiPolygon) {
        return singlePolygonFromMulti((MultiPolygon) geometry);
      }
      if (!(geometry instanceof Polygon)) {
        log.error("Non-polygon representation from mesh is not supported yet.");
        return null;
      }
      return (Polygon) geometry;
    } catch (IllegalArgumentException e) {
      log.error("Unable to create polygon from mesh positions.", e);
    }
    return null;
  }

  /**
   * Builds a list of {@link Polygon} from unique triangles that constructs the mesh.
   *
   * @param positions The list of positions forming the mesh.
   * @param triangles The list of triangles indices forming the mesh.
   * @return A list of {@link Polygon} representing each unique triangles from the mesh.
   */
  public List<Polygon> getPolygonsFromMeshData(List<Double> positions, List<Integer> triangles) {
    if (positions.isEmpty() || triangles.isEmpty()) {
      return null;
    }
    List<Triangle> uniqueTriangles = getUniqueTriangles(Doubles.toArray(positions),
        Ints.toArray(triangles));
    List<Polygon> polygons = new ArrayList<>();
    for (Triangle uniqueTriangle : uniqueTriangles) {
      polygons.add(wktGeoFactory.createPolygon(uniqueTriangle.getCoordinates()));
    }
    return polygons;
  }

  /**
   * Attempt to extract single {@link Polygon} from {@link MultiPolygon} if
   * {@link CascadedPolygonUnion} fails to combine into a single {@link Polygon}. Return null if
   * there are more than 1 external {@link Polygon}.
   *
   * @param multiPolygon The {@link MultiPolygon} resulted from {@link CascadedPolygonUnion}.
   * @return
   */
  public Polygon singlePolygonFromMulti(MultiPolygon multiPolygon) {
    PolyDefault polyDefault = polyDefaultFromPolygon((Polygon) multiPolygon.getGeometryN(0));
    for (int i = 1; i < multiPolygon.getNumGeometries(); i++) {
      polyDefault = (PolyDefault) polyDefault.union(
              polyDefaultFromPolygon((Polygon) multiPolygon.getGeometryN(i)));
    }
    List<LinearRing> outerPolys = new ArrayList<>();
    List<LinearRing> innerPolys = new ArrayList<>();
    for (int i = 0; i < polyDefault.getNumInnerPoly(); i++) {
      Poly poly = polyDefault.getInnerPoly(i);
      if (poly.isHole()) {
        innerPolys.add(wktGeoFactory.createLinearRing(getLatLonCoordinatesFromPoly(poly)));
      } else {
        outerPolys.add(wktGeoFactory.createLinearRing(getLatLonCoordinatesFromPoly(poly)));
      }
    }
    if (outerPolys.size() == 1) {
      return wktGeoFactory.createPolygon(outerPolys.get(0),
          innerPolys.toArray(new LinearRing[innerPolys.size()]));
    }
    log.error("Multi-polygon representation from mesh is not supported yet.");
    return null;
  }

  /**
   * Create {@link PolyDefault} from {@link Polygon}, used to perform more accurate geometry union
   * but with more computational time.
   *
   * @param polygon
   * @return
   */
  public PolyDefault polyDefaultFromPolygon(Polygon polygon) {
    PolyDefault polyDefault = new PolyDefault();
    PolyDefault exteriorPoly = polyFromCoordinates(polygon.getExteriorRing().getCoordinates());
    exteriorPoly.setIsHole(false);
    polyDefault.add(exteriorPoly);
    for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
      PolyDefault innerPoly = polyFromCoordinates(polygon.getInteriorRingN(i).getCoordinates());
      innerPoly.setIsHole(true);
      polyDefault.add(innerPoly);
    }
    return polyDefault;
  }

  /**
   * Create {@link PolyDefault} from a list of {@link Coordinate}.
   *
   * @param coordinates
   * @return
   */
  public PolyDefault polyFromCoordinates(Coordinate[] coordinates) {
    PolyDefault polyDefault = new PolyDefault();
    for (Coordinate coordinate : coordinates) {
      polyDefault.add(new Point2D.Double(coordinate.x, coordinate.y));
    }
    return polyDefault;
  }

  /**
   * Gets the Coordinate array representing the {@link Poly} in Latitude/Longitude format.
   *
   * @param poly
   * @return
   */
  public Coordinate[] getLatLonCoordinatesFromPoly(Poly poly) {
    List<Coordinate> coordinates = new ArrayList<>();
    for (int i = 0; i < poly.getNumPoints(); i++) {
      coordinates.add(new Coordinate(poly.getX(i), poly.getY(i)));
    }
    if (coordinates.get(0) != coordinates.get(coordinates.size() - 1)) {
      coordinates.add(coordinates.get(0));
    }
    return coordinates.toArray(new Coordinate[coordinates.size()]);
  }

  /**
   * Gets the Coordinate array representing the list of {@link Vertex3D} in Latitude/Longitude
   * format.
   *
   * @param points The list of {@link Vertex3D}.
   * @param isClosedPolygon Boolean tag for closed polygon.
   * @return The Coordinate array in (lat, lon).
   */
  public static Coordinate[] getLatLonCoordinate(List<Vertex3D> points, boolean isClosedPolygon) {
    if (points == null || points.size() < 2) {
      return null;
    }
    if (isClosedPolygon && !isClosedPolygon(points)) {
      points.add(points.get(0));
    }
    List<Coordinate> coordinates = new ArrayList<>();
    for (Vertex3D point : points) {
      coordinates.add(new Coordinate(point.getLatitude(), point.getLongitude()));
    }
    return coordinates.toArray(new Coordinate[coordinates.size()]);
  }

  /**
   * Checks if this is a closed polygon, where it starts and ends with the same point.
   *
   * @param points The list of {@link Vertex3D}.
   * @return True if the list of {@link Vertex3D} forms a closed polygon.
   */
  public static boolean isClosedPolygon(List<Vertex3D> points) {
    return points.get(0).equals(points.get(points.size() - 1));
  }

  /**
   * Gets the base footprint as list of Vertex3D, defined by this mesh. Height of Vertex3D is
   * defined by the altitude of the mesh.
   *
   * @return a list of Vertex3D, or null if the mesh is not defined.
   */
  public List<Vertex3D> getFootprint(List<Double> pos, List<Integer> tris, double height,
      double lat, double lon, double alt) {
    PolyDefault polyDefault = getPolyDefault(pos, tris);
    if (polyDefault == null) {
      return null;
    }
    List<Vertex3D> footprintFromMeshData =
        get3DVerticesFootprintFromMeshData(polyDefault, height, lat, lon, alt);
    if (footprintFromMeshData == null) {
      footprintFromMeshData = null;
    }
    return footprintFromMeshData;
  }

  /**
   * Gets the resulting polygon from clipping all mesh triangles.
   *
   * @return The resulting polygon from clipping all mesh triangles.
   */
  private PolyDefault getPolyDefault(List<Double> positions, List<Integer> triangles) {
    if (positions.isEmpty() || triangles.isEmpty()) {
      return null;
    }
    return getPolyDefaultFromMeshData(positions, triangles);
  }

  /**
   * Gets a list of Vertex3D from mesh data (positions and triangles).
   *
   * @param pos The list of positions forming the mesh.
   * @param tris The list of triangles forming the mesh.
   * @param height The height of the mesh.
   * @param lon The longitude of the mesh's geographic location (degree decimal).
   * @param lat The latitude of the mesh's geographic location (degree decimal).
   * @param alt The altitude of the mesh's geographic location (meter).
   * @return The list of Vertex3D defining the mesh.
   */
  public List<Vertex3D> get3DVerticesFootprintFromMeshData(List<Double> pos,
      List<Integer> tris, double height, double lat, double lon, double alt) {
    return get3DVerticesFromMeshData(getPolyDefaultFromMeshData(pos, tris), height,
        lat, lon, alt);
  }

  /**
   * Gets a list of Vertex3D from mesh data (positions and triangles).
   *
   * @param poly The resulting polygon from clipping all mesh triangles.
   * @param height The height of the mesh.
   * @param lon The longitude of the mesh's geographic location (degree decimal).
   * @param lat The latitude of the mesh's geographic location (degree decimal).
   * @param alt The altitude of the mesh's geographic location (meter).
   * @return The list of Vertex3D defining the mesh.
   */
  public List<Vertex3D> get3DVerticesFootprintFromMeshData(PolyDefault poly,
      double height, double lat, double lon, double alt) {
    return get3DVerticesFromMeshData(poly, height, lat, lon, alt);
  }

  /**
   * Gets the area of the resulting polygon from clipping all mesh triangles.
   *
   * @param poly The resulting polygon from clipping all mesh triangles.
   * @return The area of clipped polygon (meter), 0 if polygon is empty.
   */
  public double getAreaFromPolyDefault(PolyDefault poly) {
    if (poly == null) {
      return 0.0;
    }
    return poly.getArea();
  }

  /**
   * Get the resulting polygon from clipping all mesh triangles.
   *
   * @param pos The list of positions forming the mesh.
   * @param tris The list of triangles forming the mesh.
   * @return The resulting polygon from clipping all mesh triangles
   */
  public PolyDefault getPolyDefaultFromMeshData(List<Double> pos, List<Integer> tris) {
    List<Triangle> uniqueTriangles = getUniqueTriangles(Doubles.toArray(pos),
        Ints.toArray(tris));
    PolyDefault poly = getPolyFromUniqueTriangles(uniqueTriangles);
    if (poly == null || poly.isEmpty()) {
      return null;
    }
    return poly;
  }

  /**
   * Gets a list of Vertex3D from mesh data (positions and triangles).
   *
   * @param poly The resulting polygon from clipping all mesh triangles.
   * @param height The height of the mesh.
   * @param lon The longitude of the mesh's geographic location (degree decimal).
   * @param lat The latitude of the mesh's geographic location (degree decimal).
   * @param alt The altitude of the mesh's geographic location (meter).
   * @return The list of Vertex3D defining the mesh.
   */
  private List<Vertex3D> get3DVerticesFromMeshData(PolyDefault poly,
      double height, double lat, double lon, double alt) {
    if (poly == null) {
      return null;
    }
    List<Vertex3D> vertices = Lists.newArrayList();
    for (int i = 0; i < poly.getNumPoints(); i++) {
      vertices.add(toDecimalDegrees(poly.getX(i), poly.getY(i), height, lat, lon, alt));
    }
    vertices.add(vertices.get(0));
    return vertices;
  }

  /**
   * Converts and updates the given position from meters to decimal degrees. Approximated
   * calculations from meters to decimal degrees, since assumes the earth is a sphere.
   *
   * @param x The mesh vertex x position (longitude).
   * @param y The mesh vertex y position (latitude).
   * @param z The mesh vertex z position (altitude).
   * @return The Vertex3D converted to longitude, latitude and altitude.
   */
  public Vertex3D toDecimalDegrees(double x, double y, double z,
                                           double lat, double lon, double alt) {

    // Coordinate offsets in radians
    double dLon = x / (EARTH_RADIUS * Math.cos(Math.PI * lat / 180));
    double dLat = y / EARTH_RADIUS;

    // OffsetPosition, decimal degrees
    double newLon = lon + dLon * 180 / Math.PI;
    double newLat = lat + dLat * 180 / Math.PI;
    double newAlt = z + alt;

    return new Vertex3D(newLat, newLon, newAlt);
  }

  /**
   * Gets a list of unique triangles from array of positions and triangles
   *
   * @param pos The array of positions forming the mesh.
   * @param tris The array of triangles forming the mesh.
   * @return The list of unique triangles.
   */
  private List<Triangle> getUniqueTriangles(double[] pos, int[] tris) {
    List<Triangle> uniqueTriangles = Lists.newArrayList();
    for (int i = 0; i < tris.length; i += 3) {
      int pos1 = tris[i] * 3;
      int pos2 = tris[i + 1] * 3;
      int pos3 = tris[i + 2] * 3;
      // Checks if the current triangle is a flat-surface.
      if ((pos[pos1 + 2] == pos[pos2 + 2]) &&
          (pos[pos1 + 2] == pos[pos3 + 2])) {
        // Creates a triangle with vertices transformed according to matrix transformation.
        // Transforms the vertices by first scalar multiplication then translation.
        // X-axis: scalar multiplication (matrixTransform[0]), translation (matrixTransform[3]).
        // Y-axis: scalar multiplication (matrixTransform[5]), translation (matrixTransform[7]).
        Point2D.Double vertex1 = new Point2D.Double(pos[pos1], pos[pos1 + 1]);
        Point2D.Double vertex2 = new Point2D.Double(pos[pos2], pos[pos2 + 1]);
        Point2D.Double vertex3 = new Point2D.Double(pos[pos3], pos[pos3 + 1]);
        Triangle newTriangle = new Triangle(vertex1, vertex2, vertex3);
        if (!uniqueTriangles.contains(newTriangle)) {
          uniqueTriangles.add(newTriangle);
        }
      }
    }

    return uniqueTriangles;
  }

  /**
   * Gets a polygon that encloses all points from the list of unique triangles.
   * TODO(Brandon) Improve the accuracy, as some points are 'ignored' when to close to another.
   *
   * @param uniqueTriangles The list of unique triangles.
   * @return A polygon.
   */
  private PolyDefault getPolyFromUniqueTriangles(List<Triangle> uniqueTriangles) {
    PolyDefault poly = new PolyDefault();
    boolean isEmptyMesh = true;
    for (Triangle triangle : uniqueTriangles) {
      // If the mesh is currently empty, add the first triangle to the mesh.
      if (isEmptyMesh) {
        poly.add(triangle.getVertex1());
        poly.add(triangle.getVertex2());
        poly.add(triangle.getVertex3());
        isEmptyMesh = false;
      } else {
        PolyDefault poly2 = new PolyDefault();
        poly2.add(triangle.getVertex1());
        poly2.add(triangle.getVertex2());
        poly2.add(triangle.getVertex3());
        poly = (PolyDefault) poly.union(poly2);
      }
    }

    return poly;
  }

  /**
   * Gets the minimum height value of the mesh's list of positions.
   *
   * @param positions The mesh's list of positions.
   * @return The minimum height value for the mesh.
   */
  public double getMinHeight(List<Double> positions) {
    double minHeight = positions.get(2);
    for (int i = 5; i < positions.size(); i += 3) {
      if (positions.get(i) < minHeight) {
        minHeight = positions.get(i);
      }
    }
    return minHeight;
  }

  /**
   * Gets the maximum height value of the mesh's list of positions.
   *
   * @param positions The mesh's list of positions.
   * @return The maximum height value for the mesh.
   */
  public double getMaxHeight(List<Double> positions) {
    double minHeight = positions.get(2);
    for (int i = 5; i < positions.size(); i += 3) {
      if (positions.get(i) > minHeight) {
        minHeight = positions.get(i);
      }
    }
    return minHeight;
  }

  /**
   * Updates the mesh's list of positions with the new height value.
   *
   * @param positions The mesh's list of positions.
   * @param newHeight The new height value for the mesh.
   */
  public void updateHeight(List<Double> positions, double newHeight) {
    double minHeight = getMinHeight(positions);
    double maxHeight = getMaxHeight(positions);
    // Do nothing if the mesh's current height is correct.
    if (maxHeight - minHeight == newHeight) {
      return;
    }
    // Update the mesh height based on ratio between highest and lowest vertical point.
    for (int i = 2; i < positions.size(); i += 3) {
      double newHeightRatio = ((positions.get(i) - minHeight) / (maxHeight - minHeight)
          * newHeight) + minHeight;
      positions.remove(i);
      positions.add(i, newHeightRatio);
    }
  }

  /**
   * Updates the mesh's list of positions with the new altitude value.
   *
   * @param positions The mesh's list of positions.
   * @param newAltitude The new altitude value for the mesh.
   */
  public void updateAltitude(List<Double> positions, double newAltitude) {
    double minHeight = getMinHeight(positions);
    // Do nothing if mesh altitude if correct.
    if (minHeight == newAltitude) {
      return;
    }
    // Difference between current altitude and new altitude.
    double altitudeDiff = newAltitude - minHeight;
    for (int i = 2; i < positions.size(); i += 3) {
      double newHeightRatio = positions.get(i) + altitudeDiff;
      positions.remove(i);
      positions.add(i, newHeightRatio);
    }
  }

  /**
   * Check if the mesh normals are front facing. Mesh is facing upwards if normal.z is positive.
   * Only applicable to flat surface mesh only, i.e. not solids.
   *
   * @param normals
   * @return True if the normals associated with mesh vertices are facing upwards.
   */
  public boolean isFrontFacing(List<Float> normals, List<Integer> triangles) {
    // Vertex is facing down if normal.z axis is negative (pointing downwards).
    List<Triangle> uniqueTriangles = Lists.newArrayList();
    int firstZposition = triangles.get(0) * 3 + 2;
    return normals.get(firstZposition) > 0.f;
  }
}
