package au.com.mutopia.acs.conversion.impl;

import java.util.Map;

import au.com.mutopia.acs.conversion.Converter;
import au.com.mutopia.acs.conversion.ConverterMap;
import au.com.mutopia.acs.models.Format;

/**
 * A simple implementation of a {@link ConverterMap} mapping file formats to appropriate
 * {@link Converter}s.
 */
public class ConverterMapImpl implements ConverterMap {

  /** The map from which {@link Converter}s are retrieved. */
  private Map<Format, Converter> map;

  public ConverterMapImpl(Map<Format, Converter> map) {
    this.map = map;
  }

  public Converter get(Format format) {
    return map.get(format);
  }

}
