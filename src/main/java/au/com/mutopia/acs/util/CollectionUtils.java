package au.com.mutopia.acs.util;

import java.util.List;
import java.util.stream.Collectors;

public class CollectionUtils {
  /**
   * @param list
   * @param <T>
   * @return True if the list is null or empty.
   */
  public <T> boolean isNullOrEmpty(List<T> list) {
    return list == null || list.isEmpty();
  }

  /**
   * @param floats The list of {@link Float}s to be converted to {@link Double}s.
   * @return A list of {@link Double}s from a list of {@link Float}s.
   */
  public List<Double> doublesFromFloats(List<Float> floats) {
    return floats.stream().mapToDouble(e -> e.doubleValue()).boxed().collect(Collectors.toList());
  }
}
