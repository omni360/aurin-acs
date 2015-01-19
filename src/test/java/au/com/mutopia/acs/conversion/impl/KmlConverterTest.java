package au.com.mutopia.acs.conversion.impl;

import org.junit.Before;

import au.com.mutopia.acs.conversion.BroadC3mlFixture;
import au.com.mutopia.acs.conversion.ConverterTest;
import au.com.mutopia.acs.models.Format;
import au.com.mutopia.acs.models.c3ml.C3mlData;
import au.com.mutopia.acs.models.c3ml.C3mlEntityType;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

/**
 * Tests conversion logic for KML files.
 */
public class KmlConverterTest extends ConverterTest {

  public KmlConverterTest() {
    // Remove meshes from the expected output fixtures, since the KML inputs won't have them.
    BROAD_DATA = filter(BROAD_DATA,
        Lists.newArrayList(C3mlEntityType.POINT, C3mlEntityType.LINE, C3mlEntityType.POLYGON));
  }

  @Before
  public void setUp() {
    converter = new KmlConverter();
  }

  @Override
  protected String getResourceFolder() {
    return Format.KML.toString();
  }

  @Override
  protected String getExtension() {
    return Format.KML.toString();
  }

  /**
   * Meshes are ignored since KML don't have them.
   *
   * @param actual The {@link C3mlData} converted from {@link BroadC3mlFixture}.
   * @param expected The {@link BroadC3mlFixture}.
   */
  @Override
  public void assertThatC3mlBroadDataIsEqual(C3mlData actual, C3mlData expected) {
    assertThatC3mlBroadDataIsEqualByComparingTypes(actual, expected,
        ImmutableList.of(C3mlEntityType.POINT, C3mlEntityType.LINE, C3mlEntityType.POLYGON));
  }
}
