package au.com.mutopia.acs.conversion.impl;

import org.junit.Before;

import au.com.mutopia.acs.conversion.ConverterTest;
import au.com.mutopia.acs.models.Format;

/**
 * Tests conversion logic for KMZ files.
 */
public class KmzConverterTest extends ConverterTest {

  @Before
  public void setUp() {
    converter = new KmlConverter();
  }

  @Override
  protected String getResourceFolder() {
    return Format.KMZ.toString();
  }

  @Override
  protected String getExtension() {
    return Format.KMZ.toString();
  }

}
