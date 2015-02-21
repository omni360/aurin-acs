package au.com.mutopia.acs.conversion.impl;

import java.io.File;
import java.io.IOException;
import java.util.List;

import lombok.extern.log4j.Log4j;
import au.com.mutopia.acs.conversion.OgrConverter;
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
@Log4j
public class GeoJsonConverter extends OgrConverter {

  /**
   * Creates the converter with a {@link KmlConverter} to delegate to.
   *
   * @param kmlConverter A converter to use once the GeoJSON is converted to KML.
   */
  @Inject
  public GeoJsonConverter(KmlConverter kmlConverter) {
    super(kmlConverter);
  }

  public List<C3mlEntity> convert(Asset asset, boolean merge) throws ConversionException {
    log.debug("Converting GeoJSON asset " + asset + "...");
    return super.convert(asset, merge);
  }

}
