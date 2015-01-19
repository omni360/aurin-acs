package au.com.mutopia.acs.util.mesh;

import java.util.List;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Tuple3d;
import javax.vecmath.Vector3d;

import au.com.mutopia.acs.util.CollectionUtils;

import com.google.common.collect.Lists;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Floats;

/**
 * Utility class containing common method for accessing and converting VecMath classes.
 */
public class VecMathUtil {
  /**
   * @return An 4x4 identity matrix.
   */
  public static Matrix4d createIdentityMatrix() {
    Matrix4d identityMatrix = new Matrix4d();
    identityMatrix.setIdentity();
    return identityMatrix;
  }

  /**
   * @param degrees The degree of rotation.
   * @return A matrix transformation rotated about the X axis.
   */
  public static Matrix4d createXAxisRotationMatrix(double degrees) {
    double radians = Math.toRadians(degrees);
    Matrix4d rotationMatrix = createIdentityMatrix();
    rotationMatrix.rotX(radians);
    return rotationMatrix;
  }

  /**
   * @param degrees The degree of rotation.
   * @return A matrix transformation rotated about the Y axis.
   */
  public static Matrix4d createYAxisRotationMatrix(double degrees) {
    double radians = Math.toRadians(degrees);
    Matrix4d rotationMatrix = createIdentityMatrix();
    rotationMatrix.rotY(radians);
    return rotationMatrix;
  }

  /**
   * Creates a matrix representing a rotation of the given angle around the Z axis.
   * 
   * @param degrees The degree of rotation.
   * @return A matrix transformation rotated about the Z axis.
   */
  public static Matrix4d createZAxisRotationMatrix(double degrees) {
    double radians = Math.toRadians(degrees);
    Matrix4d rotationMatrix = createIdentityMatrix();
    rotationMatrix.rotZ(radians);
    return rotationMatrix;
  }

  /**
   * Creates a matrix representing a scaling by the given value.
   * 
   * @param scale The factor to scale by. 1.0 is the identity matrix.
   * @return A matrix to use for scaling by the given factor.
   */
  public static Matrix4d createScaleMatrix(double scale) {
    Matrix4d scaleMatrix = createIdentityMatrix();
    scaleMatrix.setScale(scale);
    return scaleMatrix;
  }

  /**
   * Transforms the mesh's positions by applying the {@link Matrix4d} transformations.
   *
   * @param positions The mesh's positions to be transformed.
   * @param matrix4x4 The 4x4 matrix transformation to be applied.
   * @return The list of doubles representing the transformed positions.
   */
  public static List<Double> transformMeshPositions(List<Double> positions, Matrix4d matrix4x4) {
    List<Tuple3d> transformPoints = Lists.newArrayList();
    for (Point3d point3d : point3dsFromDoubles(positions)) {
      matrix4x4.transform(point3d);
      transformPoints.add(point3d);
    }
    return tuple3dToDoubles(transformPoints);
  }

  /**
   * Transforms the mesh's normals by applying the {@link Matrix4d} transformations.
   *
   * @param normals The mesh's normals to be transformed.
   * @param matrix4x4 The 4x4 matrix transformation to be applied.
   * @return The list of doubles representing the transformed normals.
   */
  public static List<Double> transformMeshNormals(List<Double> normals, Matrix4d matrix4x4) {
    List<Tuple3d> transformNormals = Lists.newArrayList();
    for (Vector3d vector3d : vector3dsFromDoubles(normals)) {
      matrix4x4.transform(vector3d);
      transformNormals.add(vector3d);
    }
    return tuple3dToDoubles(transformNormals);
  }

  /**
   * Gets the {@link Matrix4d} from an array of floats.
   *
   * @param floats The array of floats to be converted into {@link Matrix4d}.
   * @return The {@link Matrix4d}.
   * @throws IllegalArgumentException if the given array does not have exactly 16 values.
   */
  public static Matrix4d matrix4dFromFloats(float[] floats) throws IllegalArgumentException {
    if (floats.length != 16) {
      throw new IllegalArgumentException("Invalid array length for matrix data.");
    }
    List<Double> doubleList = CollectionUtils.doublesFromFloats(Floats.asList(floats));
    return new Matrix4d(Doubles.toArray(doubleList));
  }

  /**
   * Gets the {@link Matrix4d} from an array of doubles.
   *
   * @param doubles The array of doubles to be converted into {@link Matrix4d}.
   * @return The {@link Matrix4d}.
   * @throws IllegalArgumentException if the given array does not have exactly 16 values.
   */
  public static Matrix4d matrix4dFromDoubles(double[] doubles) throws IllegalArgumentException {
    if (doubles.length != 16) {
      throw new IllegalArgumentException("Invalid array length for matrix data.");
    }
    return new Matrix4d(doubles);
  }

  /**
   * Gets the list of Float value representing the {@link Matrix4d}.
   *
   * @param matrix4d The {@link Matrix4d}.
   * @return The list of Float.
   */
  public static List<Float> matrix4dToFloats(Matrix4d matrix4d) {
    List<Float> floats = Lists.newArrayList();
    for (Double value : matrix4dToDoubles(matrix4d)) {
      floats.add(value.floatValue());
    }
    return floats;
  }

  /**
   * Gets the list of Double value representing the {@link Matrix4d}.
   *
   * @param matrix4d The {@link Matrix4d}.
   * @return The list of Double.
   */
  public static List<Double> matrix4dToDoubles(Matrix4d matrix4d) {
    List<Double> doubles = Lists.newArrayList();
    doubles.addAll(getRowMatrix(matrix4d, 0));
    doubles.addAll(getRowMatrix(matrix4d, 1));
    doubles.addAll(getRowMatrix(matrix4d, 2));
    doubles.addAll(getRowMatrix(matrix4d, 3));
    return doubles;
  }

  /**
   * Gets the list of Double representing a row in the {@link Matrix4d}.
   *
   * @param matrix4d The {@link Matrix4d}.
   * @param index The index of the row.
   * @return The list of Double.
   */
  private static List<Double> getRowMatrix(Matrix4d matrix4d, int index) {
    double[] col = new double[4];
    matrix4d.getRow(index, col);
    return Doubles.asList(col);
  }

  /**
   * Gets the list of {@link javax.vecmath.Point3d} from the list of Double.
   *
   * @param doubles The list of Double to be converted to list of {@link javax.vecmath.Point3d}.
   * @return The list of {@link javax.vecmath.Point3d}.
   */
  public static List<Point3d> point3dsFromDoubles(List<Double> doubles) {
    List<Point3d> point3ds = Lists.newArrayList();
    for (int i = 0; i < doubles.size(); i += 3) {
      Point3d point3d = new Point3d(doubles.get(i), doubles.get(i + 1), doubles.get(i + 2));
      point3ds.add(point3d);
    }
    return point3ds;
  }

  /**
   * Gets the list of {@link javax.vecmath.Vector3d} from the list of Double.
   *
   * @param doubles The list of Double to be converted to list of {@link javax.vecmath.Vector3d}.
   * @return The list of {@link javax.vecmath.Vector3d}.
   */
  public static List<Vector3d> vector3dsFromDoubles(List<Double> doubles) {
    List<Vector3d> vector3ds = Lists.newArrayList();
    for (int i = 0; i < doubles.size(); i += 3) {
      Vector3d vector3d = new Vector3d(doubles.get(i), doubles.get(i + 1), doubles.get(i + 2));
      vector3ds.add(vector3d);
    }
    return vector3ds;
  }

  /**
   * Converts the list of {@link javax.vecmath.Tuple3d} to the list of Double.
   *
   * @param points The list of {@link javax.vecmath.Tuple3d} to be converted to list of Double.
   * @return The list of Double.
   */
  public static List<Double> tuple3dToDoubles(List<Tuple3d> points) {
    List<Double> doubles = Lists.newArrayList();
    for (Tuple3d point : points) {
      doubles.add(point.x);
      doubles.add(point.y);
      doubles.add(point.z);
    }
    return doubles;
  }
}
