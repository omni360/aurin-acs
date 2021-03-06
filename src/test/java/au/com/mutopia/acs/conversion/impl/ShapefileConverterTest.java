package au.com.mutopia.acs.conversion.impl;

import static org.fest.assertions.api.Assertions.assertThat;

import java.util.Map;

import org.junit.Before;
import org.junit.experimental.categories.Category;

import au.com.mutopia.acs.annotation.IntegrationTest;
import au.com.mutopia.acs.conversion.BroadC3mlFixture;
import au.com.mutopia.acs.conversion.ConverterTest;
import au.com.mutopia.acs.models.Format;
import au.com.mutopia.acs.models.c3ml.C3mlData;
import au.com.mutopia.acs.models.c3ml.C3mlEntity;
import au.com.mutopia.acs.models.c3ml.C3mlEntityType;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

/**
 * Tests conversion logic for Shapefiles.
 * 
 * Note: This is an integration test because it depends on <code>ogr2ogr</code>.
 */
@Category(IntegrationTest.class)
public class ShapefileConverterTest extends ConverterTest {

  /**
   * Sets up the test with the fixtures to test against.
   */
  public ShapefileConverterTest() {
    // Rename 'description' parameter for Shapefile expected fixtures, since Shapefile header has
    // a limit of 10 characters.
    for (C3mlEntity c3mlEntity : BROAD_DATA.getC3mls()) {
      Map<String, String> newParameters = c3mlEntity.getProperties();
      newParameters.put("Descriptio", newParameters.get("description"));
      newParameters.remove("description");
    }
    // Remove meshes from the expected output fixtures, since the Shapefile inputs won't have them.
    BROAD_DATA =
        filter(BROAD_DATA,
            Lists.newArrayList(C3mlEntityType.POINT, C3mlEntityType.LINE, C3mlEntityType.POLYGON));
  }

  /**
   * Sets up the test case with a {@link ShapefileConverter} and a {@link KmlConverter} to delegate
   * to.
   */
  @Before
  public void setUp() {
    KmlConverter kmlConverter = new KmlConverter();
    converter = new ShapefileConverter(kmlConverter);
  }

  @Override
  protected String getResourceFolder() {
    return Format.SHP.toString();
  }

  @Override
  protected String getExtension() {
    return Format.ZIP.toString();
  }

  /**
   * Meshes are ignored since Shapefile don't have them.
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
   * Color field of {@link C3mlEntity} is ignored because Shapefile don't have colors.
   */
  @Override
  public void assertThatC3mlEntityIsLenientlyEqual(C3mlEntity actual, C3mlEntity expected) {
    assertThat(actual).isLenientEqualsToByAcceptingFields(expected, "name", "properties");
    assertThat(isSameCoordinates(actual.getCoordinates(), expected.getCoordinates())).isTrue();
  }
}
