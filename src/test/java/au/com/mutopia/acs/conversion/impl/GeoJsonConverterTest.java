package au.com.mutopia.acs.conversion.impl;

import org.junit.Before;

import au.com.mutopia.acs.conversion.ConverterTest;
import au.com.mutopia.acs.models.Format;

/**
 * Tests conversion logic for GeoJSON files.
 */
public class GeoJsonConverterTest extends ConverterTest {

  @Before
  public void setUp() {
    converter = new KmlConverter();
  }

  @Override
  protected String getExtension() {
    return Format.GEOJSON.toString();
  }

}
