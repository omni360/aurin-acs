package au.com.mutopia.acs.conversion;

import static org.fest.assertions.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.List;
import java.util.UUID;

import org.junit.Test;

import au.com.mutopia.acs.models.Asset;
import au.com.mutopia.acs.models.c3ml.C3mlData;
import au.com.mutopia.acs.models.c3ml.C3mlEntity;
import au.com.mutopia.acs.models.c3ml.Vertex3D;

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
  protected final C3mlData SIMPLE_DATA = buildSimpleData();

  /** The {@link Converter} used to convert the fixture. Populated by the test subclass. */
  protected Converter converter;

  /**
   * Performs a conversion of a simple fixture in the appropriate format to an expected
   * {@link C3mlData} output.
   */
  @Test
  public void testSimple() throws IOException {
    Asset asset = createResourceAsset("/fixtures/" + getExtension() + "/simple." + getExtension());
    List<C3mlEntity> entities = converter.convert(asset);
    C3mlData data = new C3mlData(entities);
    assertThat(data).isEqualTo(SIMPLE_DATA);
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
   * Constructs a {@link C3mlData} object that represents the simple fixture.
   * 
   * @return The simple fixture as C3ML.
   */
  protected C3mlData buildSimpleData() {
    Vertex3D a = new Vertex3D(-37.81548625281237, 144.9750826126445, 0);
    Vertex3D b = new Vertex3D(-37.80735973465846, 144.9710060006769, 0);
    Vertex3D c = new Vertex3D(-37.81305802727754, 144.9512328118604, 0);
    Vertex3D d = new Vertex3D(-37.82116100732942, 144.9551041535812, 0);
    List<Vertex3D> coords = ImmutableList.of(a, b, c, d, a);

    C3mlEntity entity = new C3mlEntity(UUID.randomUUID());
    entity.setCoordinates(coords);
    return new C3mlData(ImmutableList.of(entity));
  }

}
