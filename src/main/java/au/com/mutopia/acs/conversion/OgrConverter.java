package au.com.mutopia.acs.conversion;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import lombok.extern.log4j.Log4j;
import au.com.mutopia.acs.conversion.impl.AbstractConverter;
import au.com.mutopia.acs.conversion.impl.KmlConverter;
import au.com.mutopia.acs.exceptions.ConversionException;
import au.com.mutopia.acs.models.Asset;
import au.com.mutopia.acs.models.c3ml.C3mlEntity;
import au.com.mutopia.acs.util.Ogr2Ogr;

import com.google.inject.Inject;

/**
 * Converts 2D assets via {@link Ogr2Ogr} and {@link KmlConverter}.
 */
@Log4j
public abstract class OgrConverter extends AbstractConverter {

  /** The {@link KmlConverter} to delegate the conversion operation to. */
  private final KmlConverter kmlConverter;

  /**
   * Creates the converter with a {@link KmlConverter} to delegate to.
   *
   * @param kmlConverter A converter to use once the Shapefile is converted to KML.
   */
  @Inject
  public OgrConverter(KmlConverter kmlConverter) {
    this.kmlConverter = kmlConverter;
  }

  @Override
  public List<C3mlEntity> convert(Asset asset, boolean merge) throws ConversionException {
    return convertFile(getAssetFile(asset));
  }

  /**
   * Converts the file to KML using {@link Ogr2Ogr}, then converts the output with a
   * {@link KmlConverter}.
   */
  protected List<C3mlEntity> convertFile(File file) throws ConversionException {
    log.debug("Converting file " + file + " via ogr2ogr...");
    try {
      File kml = Ogr2Ogr.convertToKml(file);
      List<C3mlEntity> entities = kmlConverter.convert(new Asset(kml));
      // Remove duplicated 'Name' and 'Description' parameters created when Ogr2Ogr converts to KML.
      for (C3mlEntity entity : entities) {
        entity.getProperties().remove("Name");
        entity.getProperties().remove("Description");
      }
      return entities;
    } catch (IOException e) {
      throw new ConversionException("Failed to read converted ogr2ogr file", e);
    }
  }

  public List<C3mlEntity> convertFiles(List<File> files) throws ConversionException {
    List<C3mlEntity> entities = new ArrayList<>();
    for (File file : files) {
      entities.addAll(convertFile(file));
    }
    return entities;
  }

}
