package au.com.mutopia.acs.conversion.impl;

import au.com.mutopia.acs.conversion.Converter;
import au.com.mutopia.acs.exceptions.ConversionException;
import au.com.mutopia.acs.models.Asset;
import au.com.mutopia.acs.models.Format;
import au.com.mutopia.acs.models.c3ml.C3mlEntity;
import au.com.mutopia.acs.util.Ogr2Ogr;
import au.com.mutopia.acs.util.ZipUtils;
import com.google.common.io.Files;
import com.google.inject.Inject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ShapefileConverter implements Converter {

  private final KmlConverter kmlConverter;

  @Inject
  public ShapefileConverter(KmlConverter kmlConverter) {
    this.kmlConverter = kmlConverter;
  }

  public List<C3mlEntity> convert(Asset asset) throws ConversionException {
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
      List<C3mlEntity> c3mlEntities = new ArrayList<>();
      for (File shapefile : shapefiles) {
        File kml = Ogr2Ogr.convertToKml(shapefile);
        c3mlEntities.addAll(kmlConverter.convert(new Asset(kml)));
      }
      c3mlEntities.forEach(e -> {
        e.getParameters().remove("Name");
      });
      return c3mlEntities;
    } catch (IOException e) {
      throw new ConversionException("Failed to read converted SHP file", e);
    }
  }

}
