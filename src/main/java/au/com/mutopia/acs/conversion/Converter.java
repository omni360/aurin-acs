package au.com.mutopia.acs.conversion;

import au.com.mutopia.acs.exceptions.ConversionException;
import au.com.mutopia.acs.models.Asset;
import au.com.mutopia.acs.models.c3ml.C3mlEntity;

import java.awt.*;
import java.util.List;

/**
 * Converts an {@link Asset} into a {@link C3mlEntity}.
 */
public interface Converter {
  /**
   * Default white color if material color is absent.
   */
  public final Color DEFAULT_COLOR = Color.WHITE;

  public List<C3mlEntity> convert(Asset asset) throws ConversionException;

}
