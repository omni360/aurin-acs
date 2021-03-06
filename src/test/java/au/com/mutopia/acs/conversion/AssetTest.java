package au.com.mutopia.acs.conversion;

import au.com.mutopia.acs.models.Asset;
import au.com.mutopia.acs.models.c3ml.C3mlData;
import au.com.mutopia.acs.models.c3ml.C3mlEntity;
import au.com.mutopia.acs.models.c3ml.C3mlEntityType;
import au.com.mutopia.acs.models.c3ml.Vertex3D;
import com.google.common.collect.ImmutableList;
import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.fest.assertions.api.Assertions.assertThat;

public abstract class AssetTest {
  /**
   * Creates an {@link au.com.mutopia.acs.models.Asset} from a file in the project resources directory.
   *
   * @param filePath The path to the file within the resources directory.
   * @return An {@link au.com.mutopia.acs.models.Asset} created from the file.
   * @throws java.io.IOException If the file cannot be read.
   */
  public Asset createResourceAsset(String filePath) throws IOException {
    String absPath = URLDecoder.decode(Class.class.getResource(filePath).getFile(), "utf-8");
    return new Asset(new File(absPath));
  }

  /**
   * Returns the name of the folder containing the test fixtures for the test class.
   *
   * @return Tested folder name.
   */
  protected abstract String getResourceFolder();

  /**
   * Returns the extension of the file type handle by the test class.
   *
   * @return Tested file extension.
   */
  protected abstract String getExtension();


  /**
   * Assert that {@link au.com.mutopia.acs.models.c3ml.C3mlData} converted from {@link SimpleC3mlFixture} is the same as the
   * fixture with the exception of {@link au.com.mutopia.acs.models.c3ml.C3mlEntity} IDs.
   *
   * @param actual The {@link au.com.mutopia.acs.models.c3ml.C3mlData} converted from {@link SimpleC3mlFixture}.
   * @param expected The {@link SimpleC3mlFixture}.
   */
  public void assertThatC3mlSimpleDataIsEqual(C3mlData actual, C3mlData expected) {
    List<C3mlEntity> actualC3mls = actual.getC3mls();
    List<C3mlEntity> expectedC3mls = expected.getC3mls();
    assertThat(actualC3mls.size()).isEqualTo(expectedC3mls.size());

    C3mlEntity expectedPolygon = getFirstOfType(C3mlEntityType.POLYGON, expectedC3mls);
    C3mlEntity actualPolygon = getFirstOfType(C3mlEntityType.POLYGON, actualC3mls);

    assertThatC3mlEntityIsLenientlyEqual(actualPolygon, expectedPolygon);
    assertThatParametersAreEqual(actual.getProperties(), expected.getProperties());
  }

  /**
   * Assert that {@link C3mlData} converted from {@link BroadC3mlFixture} is the same as the fixture
   * with the exception of {@link C3mlEntity} IDs. All {@link C3mlEntityType}s should be converted.
   *
   * @param actual The {@link C3mlData} converted from {@link BroadC3mlFixture}.
   * @param expected The {@link BroadC3mlFixture}.
   */
  public void assertThatC3mlBroadDataIsEqual(C3mlData actual, C3mlData expected) {
    assertThatC3mlBroadDataIsEqualByComparingTypes(actual, expected,
        ImmutableList.of(C3mlEntityType.POINT, C3mlEntityType.LINE, C3mlEntityType.POLYGON,
            C3mlEntityType.COLLECTION));
  }

  /**
   * Assert that {@link C3mlData} converted from {@link BroadC3mlFixture} is the same as the fixture
   * with the exception of {@link C3mlEntity} IDs. All {@link C3mlEntityType}s given should be
   * converted.
   *
   * @param actual The {@link C3mlData} converted from {@link BroadC3mlFixture}.
   * @param expected The {@link BroadC3mlFixture}.
   * @param c3mlEntityTypes The list of {@link C3mlEntityType}s that should be converted.
   */
  public void assertThatC3mlBroadDataIsEqualByComparingTypes(C3mlData actual, C3mlData expected,
      List<C3mlEntityType> c3mlEntityTypes) {
    List<C3mlEntity> actualC3mls = actual.getC3mls();
    List<C3mlEntity> expectedC3mls = expected.getC3mls();
    assertThat(actualC3mls.size()).isEqualTo(expectedC3mls.size());
    for (C3mlEntityType type : c3mlEntityTypes) {
      C3mlEntity actualEntity = getFirstOfType(type, actualC3mls);
      C3mlEntity expectedEntity = getFirstOfType(type, expectedC3mls);
      if (type.equals(C3mlEntityType.COLLECTION)) {
        // Check that the mesh container entity is equal to expected.
        assertThatBroadDataContainerMeshAreEqual(actualEntity, expectedEntity);
        continue;
      } else if (type.equals(C3mlEntityType.MESH)) {
        assertThatBroadDataMeshAreEqual(actualEntity, expectedEntity);
        continue;
      }
      assertThatC3mlEntityIsLenientlyEqual(actualEntity, expectedEntity);
    }
    assertThatParametersAreEqual(actual.getProperties(), expected.getProperties());
  }

  /**
   * Asserts that the converted Broad Data mesh is leniently equals to the expected data.
   *
   * @param actual The converted {@link C3mlEntity} containing mesh.
   * @param expected The expected {@link C3mlEntity}.
   */
  public void assertThatBroadDataContainerMeshAreEqual(C3mlEntity actual, C3mlEntity expected) {
    assertThat(actual).isLenientEqualsToByAcceptingFields(expected, "name", "properties");
    assertThat(actual.getChildren().size()).isEqualTo(expected.getChildren().size());
    C3mlEntity actualMeshEntity = actual.getChildren().get(0);
    C3mlEntity expectedMeshEntity = expected.getChildren().get(0);
    assertThatBroadDataMeshAreEqual(actualMeshEntity, expectedMeshEntity);
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
        "positions", "triangles", "geoLocation");
  }

  /**
   * Assert that the converted parameters is equal to the expected parameters.
   *
   * @param actualParams The converted map of parameters.
   * @param expectedParams The expected map of parameters.
   */
  public void assertThatParametersAreEqual(Map<String, Map<String, String>> actualParams,
      Map<String, Map<String, String>> expectedParams) {
    assertThat(actualParams.keySet()).containsAll(expectedParams.keySet());
  }

  /**
   * Assert that the {@link C3mlEntity} converted is leniently equal to the expected
   * {@link C3mlEntity} by comparing name, parameters, coordinates and colors fields only.
   *
   * @param actual The {@link C3mlEntity} converted.
   * @param expected The expected {@link C3mlEntity}.
   */
  public void assertThatC3mlEntityIsLenientlyEqual(C3mlEntity actual, C3mlEntity expected) {
    assertThat(actual).isLenientEqualsToByAcceptingFields(expected, "name", "properties", "color");
    assertThat(isSameCoordinates(actual.getCoordinates(), expected.getCoordinates())).isTrue();
  }

  /**
   * Returns the first {@link C3mlEntity} from the list that is of the given type.
   *
   * @param type The type of {@link C3mlEntity} to search for.
   * @param c3mls The list to search through.
   * @return The first {@link C3mlEntity} of the type.
   */
  private C3mlEntity getFirstOfType(C3mlEntityType type, Iterable<C3mlEntity> c3mls) {
    for (C3mlEntity entity : c3mls) {
      if (entity.getType().equals(type)) {
        return entity;
      }
    }
    return null;
  }


  /**
   * Check that two list of coordinates are the same. Two coordinates are the same if they are
   * exactly equal or one is exactly equal to the reversed order of another.
   *
   * @param actualCoordinates The converted coordinates.
   * @param expectedCoordinates The expected coordinates.
   * @return True if both list of coordinates are equals.
   */
  public boolean isSameCoordinates(List<Vertex3D> actualCoordinates,
      List<Vertex3D> expectedCoordinates) {
    List<Vertex3D> reversedCoordinates = new ArrayList<>(actualCoordinates);
    Collections.reverse(reversedCoordinates);
    return actualCoordinates.equals(expectedCoordinates)
        || reversedCoordinates.equals(expectedCoordinates);
  }
}
