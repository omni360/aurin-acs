package au.com.mutopia.acs.conversion;

import static org.fest.assertions.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import au.com.mutopia.acs.models.Asset;
import au.com.mutopia.acs.models.c3ml.C3mlData;
import au.com.mutopia.acs.models.c3ml.C3mlEntity;
import au.com.mutopia.acs.models.c3ml.C3mlEntityType;

import com.google.common.collect.ImmutableList;


/**
 * Tests a {@link Converter} implementation by converting a fixture and comparing it with a known
 * output. There are three pre-defined fixtures that test the converters in different ways:
 * 
 * <ol>
 * <li>Simple: A single polygon entity with a parameter.</li>
 * <li>Broad: One of each type of entity with different parameters.</li>
 * <li>Complex: A large hierarchy of entities, including textured meshes, with a variety of
 * parameters.</li>
 * </ol>
 */
public abstract class ConverterTest {

  /** The expected {@link C3mlData} structure of the simple test fixture. */
  protected C3mlData SIMPLE_DATA = new SimpleC3mlFixture();

  /** The expected {@link C3mlData} structure of the broad test fixture. */
  protected C3mlData BROAD_DATA = new BroadC3mlFixture();

  /** The {@link Converter} used to convert the fixture. Populated by the test subclass. */
  protected Converter converter;

  /**
   * Performs a conversion of a simple fixture in the appropriate format to an expected
   * {@link C3mlData} output.
   * 
   * @see SimpleC3mlFixture
   */
  @Test
  public void testSimple() throws IOException {
    Asset asset = createResourceAsset("/fixtures/" + getExtension() + "/simple." + getExtension());
    List<C3mlEntity> entities = converter.convert(asset);
    C3mlData data = new C3mlData(entities);
    assertThatC3mlSimpleDataIsEqual(data, SIMPLE_DATA);
  }

  /**
   * Performs a conversion of a broad fixture in the appropriate format to an expected
   * {@link C3mlData} output.
   * 
   * @see BroadC3mlFixture
   */
  @Test
  public void testBroad() throws IOException {
    Asset asset = createResourceAsset("/fixtures/" + getExtension() + "/broad." + getExtension());
    List<C3mlEntity> entities = converter.convert(asset);
    C3mlData data = new C3mlData(entities);
    assertThatC3mlBroadDataIsEqual(data, BROAD_DATA);
  }

  /**
   * Creates an {@link Asset} from a file in the project resources directory.
   * 
   * @param filePath The path to the file within the resources directory.
   * @return An {@link Asset} created from the file.
   * @throws IOException If the file cannot be read.
   */
  public Asset createResourceAsset(String filePath) throws IOException {
    String absPath = URLDecoder.decode(Class.class.getResource(filePath).getFile(), "utf-8");
    return new Asset(new File(absPath));
  }

  /**
   * Returns the extension of the file type handle by the test class.
   * 
   * @return Tested file extension.
   */
  protected abstract String getExtension();

  /**
   * Returns a copy of the given {@link C3mlData} object without any meshes contained in the input.
   * 
   * @param data The data to filter the mesh entities out of.
   * @return A copy of the input data without the mesh entities, and without any of the parameters
   *         and values that applied only to them.
   */
  protected C3mlData withoutMeshes(C3mlData data) {
    List<C3mlEntity> nonMeshes = new ArrayList<>();
    for (C3mlEntity entity : data.getC3mls()) {
      if (!entity.getType().equals(C3mlEntityType.MESH)) {
        nonMeshes.add(entity);
      }
    }
    return new C3mlData(nonMeshes);
  }

  /**
   * Assert that {@link C3mlData} converted from {@link SimpleC3mlFixture} is the same as the
   * fixture with the exception of {@link C3mlEntity} IDs.
   *
   * @param actual The {@link C3mlData} converted from {@link SimpleC3mlFixture}.
   * @param expected The {@link SimpleC3mlFixture}.
   */
  public void assertThatC3mlSimpleDataIsEqual(C3mlData actual, C3mlData expected) {
    List<C3mlEntity> actualC3mls = actual.getC3mls();
    List<C3mlEntity> expectedC3mls = expected.getC3mls();
    assertThat(actualC3mls.size()).isEqualTo(expectedC3mls.size());

    C3mlEntity expectedPolygon = getFirstOfType(C3mlEntityType.POLYGON, expectedC3mls);
    C3mlEntity actualPolygon = getFirstOfType(C3mlEntityType.POLYGON, actualC3mls);

    assertThatC3mlEntityIsLenientlyEqual(actualPolygon, expectedPolygon);
    assertThat(actual.getParams().size()).isEqualTo(expected.getParams().size());
  }

  /**
   * Assert that {@link C3mlData} converted from {@link BroadC3mlFixture} is the same as the fixture
   * with the exception of {@link C3mlEntity} IDs. All {@link C3mlEntityType}s should be converted.
   *
   * @param actual The {@link C3mlData} converted from {@link BroadC3mlFixture}.
   * @param expected The {@link BroadC3mlFixture}.
   */
  public void assertThatC3mlBroadDataIsEqual(C3mlData actual, C3mlData expected) {
    assertThatC3mlBroadDataIsEqualByComparingTypes(actual, expected, ImmutableList.of(
        C3mlEntityType.POINT, C3mlEntityType.LINE, C3mlEntityType.POLYGON, C3mlEntityType.MESH));
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
      assertThatC3mlEntityIsLenientlyEqual(actualEntity, expectedEntity);
    }
    assertThat(actual.getParams().size()).isEqualTo(expected.getParams().size());
  }

  /**
   * Assert that the {@link C3mlEntity} converted is leniently equal to the expected
   * {@link C3mlEntity} by comparing name, parameters, coordinates and colors fields only.
   *
   * @param actual The {@link C3mlEntity} converted.
   * @param expected The expected {@link C3mlEntity}.
   */
  public void assertThatC3mlEntityIsLenientlyEqual(C3mlEntity actual, C3mlEntity expected) {
    assertThat(actual).isLenientEqualsToByAcceptingFields(expected, "name", "parameters",
        "coordinates", "color");
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

}
