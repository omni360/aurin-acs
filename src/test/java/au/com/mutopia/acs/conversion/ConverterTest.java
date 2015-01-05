package au.com.mutopia.acs.conversion;

import static org.fest.assertions.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

import au.com.mutopia.acs.models.Asset;
import au.com.mutopia.acs.models.c3ml.C3mlData;
import au.com.mutopia.acs.models.c3ml.C3mlEntity;


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
    assertThat(data).isEqualTo(SIMPLE_DATA);
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
    assertThat(data).isEqualTo(BROAD_DATA);
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
    List<C3mlEntity> nonMeshes =
        data.getC3mls().stream().filter(e -> "mesh".equals(e.getType()))
            .collect(Collectors.toList());
    return new C3mlData(nonMeshes);
  }

}
