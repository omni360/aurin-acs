package au.com.mutopia.acs.conversion.impl;

import org.junit.Before;
import org.junit.Ignore;

import au.com.mutopia.acs.conversion.ConverterTest;
import au.com.mutopia.acs.models.Format;

/**
 * Tests conversion logic for CityGML files.
 */
@Ignore
public class CityGmlConverterTest extends ConverterTest {

  @Before
  public void setUp() {
    converter = new KmlConverter();
  }

  @Override
  protected String getExtension() {
    return Format.CITYGML.toString();
  }

}
