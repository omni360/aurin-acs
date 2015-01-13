package au.com.mutopia.acs.conversion.impl;

import au.com.mutopia.acs.conversion.Converter;
import au.com.mutopia.acs.exceptions.ConversionException;
import au.com.mutopia.acs.models.Asset;
import au.com.mutopia.acs.models.c3ml.C3mlEntity;
import com.google.inject.Inject;

import java.util.List;

public class KmzConverter implements Converter {
  private final KmlConverter kmlConverter;

  @Inject
  public KmzConverter(KmlConverter kmlConverter) {
    this.kmlConverter = kmlConverter;
  }

  public List<C3mlEntity> convert(Asset asset) throws ConversionException {
    return kmlConverter.convertKmz(asset);
  }

}
