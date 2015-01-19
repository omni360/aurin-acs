package au.com.mutopia.acs.conversion.impl;

import au.com.mutopia.acs.annotation.IntegrationTest;
import au.com.mutopia.acs.conversion.BroadC3mlFixture;
import au.com.mutopia.acs.models.c3ml.C3mlData;
import au.com.mutopia.acs.models.c3ml.C3mlEntity;
import au.com.mutopia.acs.models.c3ml.C3mlEntityType;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.experimental.categories.Category;

import au.com.mutopia.acs.conversion.ConverterTest;
import au.com.mutopia.acs.models.Format;
import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Tests conversion logic for GeoJSON files.
 * 
 * Note: This is an integration test because it depends on <code>ogr2ogr</code>.
 */
@Category(IntegrationTest.class)
public class GeoJsonConverterTest extends ConverterTest {

  public GeoJsonConverterTest() {
    // Remove meshes from the expected output fixtures, since the GeoJson inputs won't have them.
    BROAD_DATA = filter(BROAD_DATA,
        Lists.newArrayList(C3mlEntityType.POINT, C3mlEntityType.LINE, C3mlEntityType.POLYGON));
  }

  @Before
  public void setUp() {
    KmlConverter kmlConverter = new KmlConverter();
    converter = new GeoJsonConverter(kmlConverter);
  }

  @Override
  protected String getResourceFolder() {
    return Format.GEOJSON.toString();
  }

  @Override
  protected String getExtension() {
    return Format.GEOJSON.toString();
  }

  /**
   * Meshes are ignored since GeoJson don't have them.
   *
   * @param actual The {@link C3mlData} converted from {@link BroadC3mlFixture}.
   * @param expected The {@link BroadC3mlFixture}.
   */
  @Override
  public void assertThatC3mlBroadDataIsEqual(C3mlData actual, C3mlData expected) {
    assertThatC3mlBroadDataIsEqualByComparingTypes(actual, expected,
        ImmutableList.of(C3mlEntityType.POINT, C3mlEntityType.LINE, C3mlEntityType.POLYGON));
  }

  /**
   * Color field of {@link C3mlEntity} is ignored because GeoJson don't have colors.
   */
  @Override
  public void assertThatC3mlEntityIsLenientlyEqual(C3mlEntity actual, C3mlEntity expected) {
    assertThat(actual).isLenientEqualsToByAcceptingFields(expected, "name", "parameters");
    assertThat(isSameCoordinates(actual.getCoordinates(), expected.getCoordinates())).isTrue();
  }
}
