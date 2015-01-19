package au.com.mutopia.acs.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Utility functions for operating on collections.
 */
public class CollectionUtils {

  /**
   * Checks the collection for content.
   * 
   * @param coll The collection to test.
   * @return True if the list is null or empty, false if there is any content.
   */
  public static boolean isNullOrEmpty(Collection<?> coll) {
    return coll == null || coll.isEmpty();
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
