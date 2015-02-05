package au.com.mutopia.acs.conversion.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import lombok.extern.log4j.Log4j;
import au.com.mutopia.acs.conversion.Converter;
import au.com.mutopia.acs.exceptions.ConversionException;
import au.com.mutopia.acs.models.Asset;
import au.com.mutopia.acs.models.Format;
import au.com.mutopia.acs.models.c3ml.C3mlEntity;
import au.com.mutopia.acs.util.Ogr2Ogr;
import au.com.mutopia.acs.util.ZipUtils;

import com.google.common.io.Files;
import com.google.inject.Inject;

/**
 * Converts Shapefiles into a collection of {@link C3mlEntity} objects.
 * 
 * First converts the Shapefile into KML using {@link Ogr2Ogr}, then delegates the conversion to a
 * {@link KmlConverter}.
 */
@Log4j
public class ShapefileConverter implements Converter {

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

  public List<C3mlEntity> convert(Asset asset) throws ConversionException {
    log.debug("Converting Shapefile asset " + asset.getName() + "...");
    try {
      // Extract all Shapefiles to be converted. A zip file may have multiple Shapefiles,
      // with each Shapefile containing c3mlEntities of a single geometry type.
      List<File> shapefiles = new ArrayList<>();
      String fileExtension = Files.getFileExtension(asset.getFileName());
      File assetTemporaryFile = asset.getTemporaryFile();
      if (fileExtension.equals(Format.ZIP.toString())) {
        List<File> unzippedFiles = ZipUtils.unzipToTempDirectory(assetTemporaryFile);
        for (File unzippedFile : unzippedFiles) {
          if (Files.getFileExtension(unzippedFile.getName()).equals(Format.SHP.toString())) {
            shapefiles.add(unzippedFile);
          }
        }
      } else {
        shapefiles.add(assetTemporaryFile);
      }
      if (shapefiles.isEmpty()) {
        throw new ConversionException("Failed to find .shp file.");
      }

      List<C3mlEntity> entities = new ArrayList<>();
      for (File shapefile : shapefiles) {
        File kml = Ogr2Ogr.convertToKml(shapefile);
        entities.addAll(kmlConverter.convert(new Asset(kml)));
      }
      for (C3mlEntity entity : entities) {
        entity.getParameters().remove("Name");
      }
      return entities;
    } catch (IOException e) {
      throw new ConversionException("Failed to read converted SHP file", e);
    }
  }

}
