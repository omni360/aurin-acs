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
import au.com.mutopia.acs.conversion.impl.ZipConverter;
import au.com.mutopia.acs.models.Format;

import com.google.inject.AbstractModule;

/**
 * Configures the injection context with a map of format converters.
 */
public class AcsModule extends AbstractModule {

  @Override
  protected void configure() {
    final KmlConverter kmlConverter = new KmlConverter();
    final ShapefileConverter shpConverter = new ShapefileConverter(kmlConverter);
    Map<Format, Converter> converters = new HashMap<>();

    converters.put(Format.COLLADA, new ColladaConverter());
    converters.put(Format.GEOJSON, new GeoJsonConverter(kmlConverter));
    converters.put(Format.IFC, new IfcConverter());
    converters.put(Format.KML, kmlConverter);
    converters.put(Format.KMZ, new KmzConverter(kmlConverter));
    converters.put(Format.SHP, shpConverter);
    converters.put(Format.ZIP, new ZipConverter(shpConverter));

    ConverterMap converterMap = new ConverterMapImpl(converters);
    bind(ConverterMap.class).toInstance(converterMap);
  }

}
