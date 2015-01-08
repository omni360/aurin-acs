package au.com.mutopia.acs.conversion.impl;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.experimental.categories.Category;

import au.com.mutopia.acs.annotation.IntegrationTest;
import au.com.mutopia.acs.conversion.ConverterTest;
import au.com.mutopia.acs.models.Format;

/**
 * Tests conversion logic for Shapefiles.
 * 
 * Note: This is an integration test because it depends on <code>ogr2ogr</code>.
 */
@Ignore
@Category(IntegrationTest.class)
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
