package au.com.mutopia.acs.conversion.impl;

import java.util.Map;

import au.com.mutopia.acs.conversion.Converter;
import au.com.mutopia.acs.conversion.ConverterMap;

/**
 * A simple implementation of a {@link ConverterMap} mapping file formats to appropriate
 * {@link Converter}s.
 */
public class ConverterMapImpl implements ConverterMap {

  /** The map from which {@link Converter}s are retrieved. */
  private Map<String, Converter> map;

  public ConverterMapImpl(Map<String, Converter> map) {
    this.map = map;
  }

  public Converter get(String format) {
    return map.get(format);
  }

}
