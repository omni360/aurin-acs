package au.com.mutopia.acs.conversion.impl;

import au.com.mutopia.acs.models.Asset;
import au.com.mutopia.acs.models.c3ml.C3mlData;
import au.com.mutopia.acs.models.c3ml.C3mlEntity;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.List;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import au.com.mutopia.acs.annotation.IntegrationTest;
import au.com.mutopia.acs.conversion.ConverterTest;
import au.com.mutopia.acs.models.Format;
import au.com.mutopia.acs.util.BimServerAuthenticator;

/**
 * Tests conversion logic for IFC files.
 * 
 * Note: This is an integration test because it depends on <code>BIMserver</code>.
 */
@Category(IntegrationTest.class)
@RunWith(MockitoJUnitRunner.class)
public class IfcConverterTest extends ConverterTest {

  private BimServerAuthenticator bimAuth =
      new BimServerAuthenticator("http://localhost:8888", "admin@bimserver.org", "admin");

  /**
   * Sets up the test case with an {@link IfcConverter}.
   */
  @Before
  public void setUp() {
    converter = new IfcConverter(bimAuth);
  }

  @Override
  protected String getResourceFolder() {
    return Format.IFC.toString();
  }

  @Override
  protected String getExtension() {
    return Format.IFC.toString();
  }

  @Ignore
  @Test
  public void testSimple() {
  }

  @Ignore
  @Test
  public void testBroad() {
  }

  @Ignore
  @Test
  public void could_extract_geometric_value_with_bim_server() throws Exception {
    Asset asset =
        createResourceAsset("/fixtures/" + getResourceFolder() +
            "/140013_Architectural_A13_MAB_Roof." + getExtension());
    List<C3mlEntity> entities = converter.convert(asset);
  }
}
