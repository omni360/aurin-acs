package au.com.mutopia.acs.conversion.impl;

import java.io.File;
import java.io.IOException;
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

  /**
   * Reads the asset data into a {@link java.io.File}.
   *
   * @param asset The asset to load as a file.
   * @return The file stored in the asset.
   * @throws ConversionException if the file cannot be created and read.
   */
  protected File getAssetFile(Asset asset) throws ConversionException {
    try {
      return asset.getTemporaryFile();
    } catch (IOException e) {
      throw new ConversionException("Failed to read file for asset " + asset, e);
    }
  }

}
