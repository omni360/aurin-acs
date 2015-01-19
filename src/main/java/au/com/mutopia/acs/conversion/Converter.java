package au.com.mutopia.acs.conversion;

import java.awt.Color;
import java.util.List;

import au.com.mutopia.acs.exceptions.ConversionException;
import au.com.mutopia.acs.models.Asset;
import au.com.mutopia.acs.models.c3ml.C3mlEntity;

/**
 * Converts an {@link Asset} into a {@link C3mlEntity}.
 */
public interface Converter {
  /**
   * Default white color if material color is absent.
   */
  public final Color DEFAULT_COLOR = Color.WHITE;

  /**
   * Converts the given asset into a collection of {@link C3mlEntity} objects.
   * 
   * @param asset The {@link Asset} to convert.
   * @return A collection of {@link C3mlEntity} objects corresponding to the entities in the
   *         {@link Asset}.
   * @throws ConversionException If the conversion failed.
   */
  public List<C3mlEntity> convert(Asset asset) throws ConversionException;

}
