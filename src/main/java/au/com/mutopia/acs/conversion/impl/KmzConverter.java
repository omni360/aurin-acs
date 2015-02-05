package au.com.mutopia.acs.conversion.impl;

import au.com.mutopia.acs.conversion.Converter;
import au.com.mutopia.acs.exceptions.ConversionException;
import au.com.mutopia.acs.models.Asset;
import au.com.mutopia.acs.models.c3ml.C3mlEntity;
import com.google.inject.Inject;
import lombok.extern.log4j.Log4j;

import java.util.List;


/**
 * Converts KMZ files into collections of {@link C3mlEntity} objects.
 * 
 * Converts the embedded KML document with a {@link KmlConverter}.
 */
@Log4j
public class KmzConverter implements Converter {

  /** The {@link KmlConverter} to delegate the 2D conversion operation to. */
  private final KmlConverter kmlConverter;

  /**
   * Creates the converter with the delegate {@link Converter}s for KML and COLLADA.
   * 
   * @param kmlConverter A {@link Converter} to delegate KML entities to.
   */
  @Inject
  public KmzConverter(KmlConverter kmlConverter) {
    this.kmlConverter = kmlConverter;
  }

  public List<C3mlEntity> convert(Asset asset) throws ConversionException {
    log.debug("Converting KML asset " + asset.getName() + "...");
    return kmlConverter.convertKmz(asset);
  }

}
