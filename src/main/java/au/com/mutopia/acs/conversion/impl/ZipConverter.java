package au.com.mutopia.acs.conversion.impl;

import java.util.List;

import au.com.mutopia.acs.conversion.Converter;
import au.com.mutopia.acs.exceptions.ConversionException;
import au.com.mutopia.acs.models.Asset;
import au.com.mutopia.acs.models.c3ml.C3mlEntity;

import com.google.inject.Inject;

/**
 * Converts ZIP archives into a collection of {@link C3mlEntity} objects.
 * 
 * For the time being, assumes that all ZIP archives are zipped Shapefiles, and delegates the
 * request to a {@link ShapefileConverter}.
 */
// TODO(orlade): Unzip and determine correct format to delegate to.
public class ZipConverter implements Converter {

  /** The {@link ShapefileConverter} to delegate the conversion operation to. */
  private final ShapefileConverter shpConverter;

  /**
   * Creates the converter with a {@link ShapefileConverter} to delegate to.
   * 
   * @param shpConverter A converter to use once the ZIP archive is unzipped.
   */
  @Inject
  public ZipConverter(ShapefileConverter shpConverter) {
    this.shpConverter = shpConverter;
  }

  public List<C3mlEntity> convert(Asset asset) throws ConversionException {
    return this.shpConverter.convert(asset);
  }

}
