package au.com.mutopia.acs.util;

import java.util.ArrayList;
import java.util.List;

public class CollectionUtils {
  /**
   * @param list
   * @param <T>
   * @return True if the list is null or empty.
   */
  public static <T> boolean isNullOrEmpty(List<T> list) {
    return list == null || list.isEmpty();
  }

  /**
   * @param floats The list of {@link Float}s to be converted to {@link Double}s.
   * @return A list of {@link Double}s from a list of {@link Float}s.
   */
  public static List<Double> doublesFromFloats(List<Float> floats) {
    List<Double> doubles = new ArrayList<>();
    for (Float f : floats) {
      doubles.add(f.doubleValue());
    }
    return doubles;
  }
}
