package au.com.mutopia.acs.service;

import java.util.HashMap;
import java.util.Map;

import au.com.mutopia.acs.conversion.Converter;
import au.com.mutopia.acs.conversion.ConverterMap;
import au.com.mutopia.acs.conversion.impl.ColladaConverter;
import au.com.mutopia.acs.conversion.impl.ConverterMapImpl;
import au.com.mutopia.acs.conversion.impl.GeoJsonConverter;
import au.com.mutopia.acs.conversion.impl.IfcConverter;
import au.com.mutopia.acs.conversion.impl.KmlConverter;
import au.com.mutopia.acs.conversion.impl.KmzConverter;
import au.com.mutopia.acs.conversion.impl.ShapefileConverter;

import com.google.inject.AbstractModule;

public class AcsModule extends AbstractModule {

  @Override
  protected void configure() {
    Map<String, Converter> converters = new HashMap<String, Converter>();
    converters.put("c3ml", new ShapefileConverter());
    converters.put("dae", new ColladaConverter());
    converters.put("ifc", new IfcConverter());
    converters.put("json", new GeoJsonConverter());
    converters.put("kml", new KmlConverter());
    converters.put("kmz", new KmzConverter());
    converters.put("shp", new ShapefileConverter());

    ConverterMap converterMap = new ConverterMapImpl(converters);
    bind(ConverterMap.class).toInstance(converterMap);
  }

}
