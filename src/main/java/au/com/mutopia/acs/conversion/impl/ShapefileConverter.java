package au.com.mutopia.acs.conversion.impl;

import java.io.File;
import java.io.IOException;
import java.util.List;

import au.com.mutopia.acs.conversion.Converter;
import au.com.mutopia.acs.exceptions.ConversionException;
import au.com.mutopia.acs.models.Asset;
import au.com.mutopia.acs.models.c3ml.C3mlEntity;
import au.com.mutopia.acs.util.Ogr2Ogr;

import com.google.inject.Inject;

public class ShapefileConverter implements Converter {

  private final KmlConverter kmlConverter;

  @Inject
  public ShapefileConverter(KmlConverter kmlConverter) {
    this.kmlConverter = kmlConverter;
  }

  public List<C3mlEntity> convert(Asset asset) throws ConversionException {
    File kml = Ogr2Ogr.convertToKml(new File(asset.getFileName()));
    try {
      return kmlConverter.convert(new Asset(kml));
    } catch (IOException e) {
      throw new ConversionException("Failed to read converted KML file", e);
    }
  }

}
