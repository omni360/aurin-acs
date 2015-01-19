package au.com.mutopia.acs.conversion.impl;

import java.util.List;

import au.com.mutopia.acs.conversion.Converter;
import au.com.mutopia.acs.exceptions.ConversionException;
import au.com.mutopia.acs.models.Asset;
import au.com.mutopia.acs.models.c3ml.C3mlEntity;

import com.google.inject.Inject;


/**
 * Converts KMZ files into collections of {@link C3mlEntity} objects.
 * 
 * Converts the embedded KML document with a {@link KmlConverter} and the COLLADA meshes with a
 * {@link ColladaConverter}.
 */
public class KmzConverter implements Converter {

  /** The {@link KmlConverter} to delegate the 2D conversion operation to. */
  private final KmlConverter kmlConverter;

  /** The {@link ColladaConverter} to delegate the mesh conversion operation to. */
  private final ColladaConverter colladaConverter;

  @Inject
  public KmzConverter(KmlConverter kmlConverter, ColladaConverter colladaConverter) {
    this.kmlConverter = kmlConverter;
    this.colladaConverter = colladaConverter;
  }

  public List<C3mlEntity> convert(Asset asset) throws ConversionException {
    return kmlConverter.convertKmz(asset);
  }

}
