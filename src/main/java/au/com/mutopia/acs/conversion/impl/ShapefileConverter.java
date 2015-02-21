package au.com.mutopia.acs.conversion.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import lombok.extern.log4j.Log4j;
import au.com.mutopia.acs.exceptions.ConversionException;
import au.com.mutopia.acs.models.Asset;
import au.com.mutopia.acs.models.c3ml.C3mlEntity;
import au.com.mutopia.acs.util.Ogr2Ogr;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

/**
 * Converts Shapefiles into a collection of {@link C3mlEntity} objects.
 *
 * First converts the Shapefile into KML using {@link Ogr2Ogr}, then delegates the conversion to a
 * {@link KmlConverter}.
 */
@Log4j
public class ShapefileConverter extends AbstractConverter {

  /** The {@link KmlConverter} to delegate the conversion operation to. */
  private final KmlConverter kmlConverter;

  /**
   * Creates the converter with a {@link KmlConverter} to delegate to.
   *
   * @param kmlConverter A converter to use once the Shapefile is converted to KML.
   */
  @Inject
  public ShapefileConverter(KmlConverter kmlConverter) {
    this.kmlConverter = kmlConverter;
  }

  public List<C3mlEntity> convert(Asset asset, boolean merge) throws ConversionException {
    log.debug("Converting Shapefile asset " + asset + "...");
    // Convert a single uploaded SHP file.
    return convertFiles(ImmutableList.of(getAssetFile(asset)));
  }

  /**
   * Converts a set of unzipped Shapefile files. First converts the Shapefile to KML using
   * {@link Ogr2Ogr}, then converts that with a {@link KmlConverter}.
   */
  public List<C3mlEntity> convertFiles(List<File> shapefiles) throws ConversionException {
    log.debug("Converting Shapefile from files " + Joiner.on(", ").join(shapefiles) + "...");
    try {
      List<C3mlEntity> entities = new ArrayList<>();
      for (File shapefile : shapefiles) {
        File kml = Ogr2Ogr.convertToKml(shapefile);
        entities.addAll(kmlConverter.convert(new Asset(kml)));
      }
      for (C3mlEntity entity : entities) {
        entity.getProperties().remove("Name");
      }
      return entities;
    } catch (IOException e) {
      throw new ConversionException("Failed to read converted SHP file", e);
    }
  }

}
