package au.com.mutopia.acs.conversion.impl;

import org.junit.Before;
import org.junit.Ignore;

import au.com.mutopia.acs.conversion.ConverterTest;
import au.com.mutopia.acs.models.Format;

/**
 * Tests conversion logic for Shapefiles.
 */
@Ignore
public class ShapefileConverterTest extends ConverterTest {

  public ShapefileConverterTest() {
    // Remove meshes from the expected output fixtures, since the Shapefile inputs won't have them.
    BROAD_DATA = withoutMeshes(BROAD_DATA);
  }

  @Before
  public void setUp() {
    converter = new KmlConverter();
  }

  @Override
  protected String getExtension() {
    return Format.SHP.toString();
  }

}
