package au.com.mutopia.acs.conversion.impl;

import org.junit.Before;
import org.junit.Ignore;

import au.com.mutopia.acs.conversion.ConverterTest;
import au.com.mutopia.acs.models.Format;

/**
 * Tests conversion logic for IFC files.
 */
@Ignore
public class IfcConverterTest extends ConverterTest {

  @Before
  public void setUp() {
    converter = new KmlConverter();
  }

  @Override
  protected String getExtension() {
    return Format.IFC.toString();
  }

}
