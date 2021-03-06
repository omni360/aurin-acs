package au.com.mutopia.acs.conversion.impl;

import static org.fest.assertions.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
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
 * Tests conversion logic for COLLADA (DAE) files.
 */
@Category(IntegrationTest.class)
public class ColladaConverterTest extends ConverterTest {

  /**
   * Sets up the fixtures to compare the conversions against.
   */
  public ColladaConverterTest() {
    // Remove non meshes from the expected output fixtures, since the COLLADA inputs only have
    // meshes.
    BROAD_DATA = filter(BROAD_DATA, Lists.newArrayList(C3mlEntityType.MESH));
  }

  /**
   * Sets up the test case with a {@link ColladaConverter}.
   */
  @Before
  public void setUp() {
    converter = new ColladaConverter();
  }

  @Override
  protected String getResourceFolder() {
    return Format.COLLADA.toString();
  }

  @Override
  protected String getExtension() {
    return Format.COLLADA.toString();
  }

  @Ignore("Not applicable for 3D formats")
  @Override
  public void testSimple() {}

  /**
   * Only mesh containers are checked.
   *
   * @param actual The {@link C3mlData} converted from {@link BroadC3mlFixture}.
   * @param expected The {@link BroadC3mlFixture}.
   */
  @Override
  public void assertThatC3mlBroadDataIsEqual(C3mlData actual, C3mlData expected) {
    assertThatC3mlBroadDataIsEqualByComparingTypes(actual, expected,
        ImmutableList.of(C3mlEntityType.MESH));
  }

  /**
   * Asserts that the converted Broad Data mesh is leniently equals to the expected data.
   *
   * @param actual The converted {@link C3mlEntity} containing mesh.
   * @param expected The expected {@link C3mlEntity}.
   */
  public void assertThatBroadDataMeshAreEqual(C3mlEntity actual, C3mlEntity expected) {
    assertThat(actual.getType()).isEqualTo(C3mlEntityType.MESH);
    assertThat(actual).isLenientEqualsToByAcceptingFields(expected, "name", "color", "properties",
        "positions", "triangles");
  }
}
