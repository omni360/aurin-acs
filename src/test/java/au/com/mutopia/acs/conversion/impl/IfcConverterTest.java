package au.com.mutopia.acs.conversion.impl;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.experimental.categories.Category;

import au.com.mutopia.acs.annotation.IntegrationTest;
import au.com.mutopia.acs.conversion.ConverterTest;
import au.com.mutopia.acs.models.Format;

/**
 * Tests conversion logic for IFC files.
 * 
 * Note: This is an integration test because it depends on <code>BIMserver</code>.
 */
@Ignore
@Category(IntegrationTest.class)
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
