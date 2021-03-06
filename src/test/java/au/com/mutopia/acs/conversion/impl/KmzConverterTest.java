package au.com.mutopia.acs.conversion.impl;

import org.junit.Before;
import org.junit.experimental.categories.Category;

import au.com.mutopia.acs.annotation.IntegrationTest;
import au.com.mutopia.acs.conversion.ConverterTest;
import au.com.mutopia.acs.models.Format;

/**
 * Tests conversion logic for KMZ files.
 */
@Category(IntegrationTest.class)
public class KmzConverterTest extends ConverterTest {

  /**
   * Sets up the {@link KmzConverter} with a {@link KmlConverter} and {@link ColladaConverter} to
   * delegate to.
   */
  @Before
  public void setUp() {
    KmlConverter kmlConverter = new KmlConverter();
    converter = new KmzConverter(kmlConverter);
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
