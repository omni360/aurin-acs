package au.com.mutopia.acs.conversion.impl;

import java.io.File;
import java.util.List;

import lombok.extern.log4j.Log4j;
import au.com.mutopia.acs.exceptions.ConversionException;
import au.com.mutopia.acs.models.Asset;
import au.com.mutopia.acs.models.Format;
import au.com.mutopia.acs.models.c3ml.C3mlEntity;
import au.com.mutopia.acs.util.ZipUtils;

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

  /**
   * Unzips the ZIP archive and passes the files to a {@link ShapefileConverter}. Assumes that all
   * ZIP archives are Shapefiles.
   */
  public List<C3mlEntity> convert(Asset asset, boolean merge) throws ConversionException {
    log.debug("Converting ZIP asset " + asset + "...");

    List<File> shps = ZipUtils.extractByExtension(getAssetFile(asset), Format.SHP.toString());
    if (shps.isEmpty()) {
      throw new ConversionException("Failed to find .shp file.");
    }
    return shpConverter.convertFiles(shps);
  }

}
