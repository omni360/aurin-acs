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

/**
 * Converts GeoJSON files into a collection of {@link C3mlEntity} objects.
 * 
 * First converts the GeoJSON into KML using {@link Ogr2Ogr}, then delegates the conversion to a
 * {@link KmlConverter}.
 */
public class GeoJsonConverter implements Converter {

  /** The {@link KmlConverter} to delegate the conversion operation to. */
  private final KmlConverter kmlConverter;

  /**
   * Creates the converter with a {@link KmlConverter} to delegate to.
   * 
   * @param kmlConverter A converter to use once the GeoJSON is converted to KML.
   */
  @Inject
  public GeoJsonConverter(KmlConverter kmlConverter) {
    this.kmlConverter = kmlConverter;
  }

  public List<C3mlEntity> convert(Asset asset) throws ConversionException {
    try {
      File kml = Ogr2Ogr.convertToKml(asset.getTemporaryFile());
      List<C3mlEntity> entities = kmlConverter.convert(new Asset(kml));
      // Remove duplicated 'Name' and 'Description' parameters created when Ogr2Ogr converts to KML.
      for (C3mlEntity entity : entities) {
        entity.getParameters().remove("Name");
        entity.getParameters().remove("Description");
      }
      return entities;
    } catch (IOException e) {
      throw new ConversionException("Failed to read converted GeoJson file", e);
    }
  }

}
