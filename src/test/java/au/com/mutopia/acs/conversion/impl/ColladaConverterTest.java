package au.com.mutopia.acs.conversion.impl;

import au.com.mutopia.acs.conversion.BroadC3mlFixture;
import au.com.mutopia.acs.conversion.ConverterTest;
import au.com.mutopia.acs.models.Format;
import au.com.mutopia.acs.models.c3ml.C3mlData;
import au.com.mutopia.acs.models.c3ml.C3mlEntity;
import au.com.mutopia.acs.models.c3ml.C3mlEntityType;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.junit.Before;

import java.util.stream.Collectors;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Tests conversion logic for COLLADA (DAE) files.
 */
public class ColladaConverterTest extends ConverterTest {

  public ColladaConverterTest() {
    // Remove KMZ specific container entity for mesh.
    BROAD_DATA.setC3mls(BROAD_DATA.getC3mls().stream()
        .map(e -> e.getType().equals(C3mlEntityType.CONTAINER) ? e.getChildren().get(0) : e)
        .collect(Collectors.toList()));
    // Remove non meshes from the expected output fixtures, since the COLLADA inputs only have
    // meshes.
    BROAD_DATA = filter(BROAD_DATA,
        Lists.newArrayList(C3mlEntityType.MESH, C3mlEntityType.CONTAINER));
  }

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

  @Override
  public void testSimple() {
  }

  /**
   * Only mesh containers are checked.
   *
   * @param actual The {@link C3mlData} converted from {@link BroadC3mlFixture}.
   * @param expected The {@link BroadC3mlFixture}.
   */
  @Override
  public void assertThatC3mlBroadDataIsEqual(C3mlData actual, C3mlData expected) {
    assertThatC3mlBroadDataIsEqualByComparingTypes(actual, expected,
        ImmutableList.of(C3mlEntityType.CONTAINER));
  }

  /**
   * Asserts that the converted Broad Data mesh is leniently equals to the expected data.
   * Geographic location is ignored because COLLADA doesn't have it.
   *
   * @param actual The converted {@link C3mlEntity} containing mesh.
   * @param expected The expected {@link C3mlEntity}.
   */
  @Override public void assertThatBroadDataContainerMeshAreEqual(C3mlEntity actual,
      C3mlEntity expected) {
    assertThat(actual).isLenientEqualsToByAcceptingFields(expected, "name", "parameters");
    assertThat(actual.getChildren().size()).isEqualTo(expected.getChildren().size());
    C3mlEntity actualMeshEntity = actual.getChildren().get(0);
    C3mlEntity expectedMeshEntity = expected.getChildren().get(0);
    assertThat(actualMeshEntity.getType()).isEqualTo(C3mlEntityType.MESH);
    assertThat(actualMeshEntity).
        isLenientEqualsToByAcceptingFields(expectedMeshEntity, "name", "color",
            "positions", "triangles");
  }
}
