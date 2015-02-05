package au.com.mutopia.acs.conversion.impl;

import org.junit.Before;
import org.junit.Ignore;
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
@Ignore
@Category(IntegrationTest.class)
@RunWith(MockitoJUnitRunner.class)
public class IfcConverterTest extends ConverterTest {

  @Mock
  private BimServerAuthenticator bimAuth;

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

}
