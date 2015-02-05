package au.com.mutopia.acs.conversion.impl;

import java.util.List;

import lombok.extern.log4j.Log4j;
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
@Log4j
public class ZipConverter extends AbstractConverter {

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

  public List<C3mlEntity> convert(Asset asset, boolean merge) throws ConversionException {
    log.debug("Converting ZIP asset " + asset + "...");
    return this.shpConverter.convert(asset, merge);
  }

}
