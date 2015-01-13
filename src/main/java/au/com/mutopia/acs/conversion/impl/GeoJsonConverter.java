package au.com.mutopia.acs.conversion.impl;

import au.com.mutopia.acs.conversion.Converter;
import au.com.mutopia.acs.exceptions.ConversionException;
import au.com.mutopia.acs.models.Asset;
import au.com.mutopia.acs.models.c3ml.C3mlEntity;
import au.com.mutopia.acs.util.Ogr2Ogr;
import com.google.inject.Inject;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class GeoJsonConverter implements Converter {

  private final KmlConverter kmlConverter;

  @Inject
  public GeoJsonConverter(KmlConverter kmlConverter) {
    this.kmlConverter = kmlConverter;
  }

  public List<C3mlEntity> convert(Asset asset) throws ConversionException {
    try {
      File kml = Ogr2Ogr.convertToKml(asset.getTemporaryFile());
      List<C3mlEntity> c3mlEntities = kmlConverter.convert(new Asset(kml));
      // Remove duplicated 'Name' and 'Description' parameters created when Ogr2Ogr converts to KML.
      c3mlEntities.forEach(e -> {
        Map<String, String> parameters = e.getParameters();
        parameters.remove("Name");
        parameters.remove("Description");
        });
      return c3mlEntities;
    } catch (IOException e) {
      throw new ConversionException("Failed to read converted GeoJson file", e);
    }
  }

}
