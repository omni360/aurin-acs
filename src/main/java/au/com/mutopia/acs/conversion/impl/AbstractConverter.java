package au.com.mutopia.acs.conversion.impl;

import java.util.List;

import au.com.mutopia.acs.conversion.Converter;
import au.com.mutopia.acs.exceptions.ConversionException;
import au.com.mutopia.acs.models.Asset;
import au.com.mutopia.acs.models.c3ml.C3mlEntity;

/**
 * A simple abstract implementation of {@link Converter}.
 */
public abstract class AbstractConverter implements Converter {

  /**
   * Default implementation for converting an asset without merging entities.
   */
  @Override
  public List<C3mlEntity> convert(Asset asset) throws ConversionException {
    return convert(asset, false);
  }

}
