package au.com.mutopia.acs.conversion;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import au.com.mutopia.acs.models.Asset;
import au.com.mutopia.acs.models.c3ml.C3mlData;
import au.com.mutopia.acs.models.c3ml.C3mlEntity;
import au.com.mutopia.acs.models.c3ml.C3mlEntityType;

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
public abstract class ConverterTest extends AssetTest {

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
   * @throws IOException if the simple fixture file cannot be read.
   * 
   * @see SimpleC3mlFixture
   */
  @Test
  public void testSimple() throws IOException {
    Asset asset =
        createResourceAsset("/fixtures/" + getResourceFolder() + "/simple." + getExtension());
    List<C3mlEntity> entities = converter.convert(asset);
    C3mlData data = new C3mlData(entities);
    assertThatC3mlSimpleDataIsEqual(data, SIMPLE_DATA);
  }

  /**
   * Performs a conversion of a broad fixture in the appropriate format to an expected
   * {@link C3mlData} output.
   * 
   * @throws IOException if the broad fixture file cannot be read.
   * 
   * @see BroadC3mlFixture
   */
  @Test
  public void testBroad() throws IOException {
    Asset asset =
        createResourceAsset("/fixtures/" + getResourceFolder() + "/broad." + getExtension());
    List<C3mlEntity> entities = converter.convert(asset);
    C3mlData data = new C3mlData(entities);
    assertThatC3mlBroadDataIsEqual(data, BROAD_DATA);
  }

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
   * Returns a copy of the given {@link C3mlData} object containing only the selected types.
   *
   * @param data The data to filter.
   * @param includedTypes The {@link C3mlEntityType} to be included in the data.
   * @return A copy of the input data containing only entities with the filtered types.
   */
  protected C3mlData filter(C3mlData data, List<C3mlEntityType> includedTypes) {
    List<C3mlEntity> filteredEntities = new ArrayList<>();
    for (C3mlEntity entity : data.getC3mls()) {
      if (includedTypes.contains(entity.getType())) {
        filteredEntities.add(entity);
      }
    }
    return new C3mlData(filteredEntities);
  }
}
