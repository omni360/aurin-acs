package au.com.mutopia.acs.conversion.impl;

import org.junit.Before;

import au.com.mutopia.acs.conversion.ConverterTest;
import au.com.mutopia.acs.models.Format;

/**
 * Tests conversion logic for KML files.
 */
public class KmlConverterTest extends ConverterTest {

  public KmlConverterTest() {
    // Remove meshes from the expected output fixtures, since the KML inputs won't have them.
    BROAD_DATA = withoutMeshes(BROAD_DATA);
  }

  @Before
  public void setUp() {
    converter = new KmlConverter();
  }

  @Override
  protected String getExtension() {
    return Format.KML.toString();
  }

}
