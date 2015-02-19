package au.com.mutopia.acs.util.geometry;

import au.com.mutopia.acs.models.c3ml.Vertex3D;
import java.util.List;

public class GeometryUtils {
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
}
