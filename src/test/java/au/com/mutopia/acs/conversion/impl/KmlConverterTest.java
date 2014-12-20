package au.com.mutopia.acs.conversion.impl;

import org.junit.Before;

import au.com.mutopia.acs.conversion.ConverterTest;
import au.com.mutopia.acs.models.Format;

/**
 * Tests conversion logic for KML files.
 */
public class KmlConverterTest extends ConverterTest {

  @Before
  public void setUp() {
    converter = new KmlConverter();
  }

  @Override
  protected String getExtension() {
    return Format.KML.toString();
  }

}
