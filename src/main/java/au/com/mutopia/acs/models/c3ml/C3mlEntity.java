package au.com.mutopia.acs.models.c3ml;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

/**
 * An object representing the contents of a C3ML document.
 * 
 * @see <a
 *      href="https://docs.google.com/document/d/1dNMQyOjIN6AGPIpUqw0ZdKRrLbrTjws6itGeo4dtHYU">Unofficial
 *      C3ML specification</a>
 */
@Getter
@Setter
public class C3mlEntity {

  private String id;
  private String name;

  /** The name of the type of the entity, default to empty container without geometries. */
  private C3mlEntityType type = C3mlEntityType.CONTAINER;

  /** The ID of this entity's parent entity. */
  private String parentId;

  /** The {@link C3mlEntity} objects that belong 'within' this one. */
  private List<C3mlEntity> children = new ArrayList<>();

  /** A map of property names to values of this entity. */
  private Map<String, String> properties = new HashMap<>();

  /**
   * A list of coordinates of points making up the entity geometry (2D entities only).Each vertex
   * represents the longitude, latitude and elevation of a point.
   */
  private List<Vertex3D> coordinates;

  /**
   * The color of the entity, if any solid color should be applied. The list of numbers represents
   * an RGBA vector (0 to 255 for red, green, blue and alpha).
   */
  private List<Integer> color = new ArrayList<>();

  /** The scaling factors (x, y, z) by which all coordinates should be scaled when rendered. */
  private List<Double> scale;

  /**
   * The angles (in degrees around the x, y, z axes, counterclockwise) by which the coordinates
   * should be rotated when rendered.
   */
  private List<Double> rotation;

  /** The 3D coordinates of each of the points in the entity's mesh (3D entities only). */
  private List<Double> positions;

  /** The normal of each of the points in the {@link #positions} list. */
  private List<Double> normals;

  /**
   * A list of indices of the {@link #positions} array. Each sequence of three indices represents a
   * triangle formed between the three indexed position vectors.
   */
  private List<Integer> triangles;

  /** The aggregate location of the {@link #positions} of the mesh. */
  private List<Double> geoLocation;

  /** The URL to the glTF mesh data, if applicable. */
  private String gltfUrl;

  /** The glTF mesh data in binary format, if applicable. */
  private Byte[] gltfData;

  /**
   * Creates a new {@link C3mlEntity} with a random ID.
   */
  public C3mlEntity() {
    this.id = UUID.randomUUID().toString();
  }

  /**
   * Creates a new {@link C3mlEntity} with the specified ID.
   * 
   * @param id The ID to use for the entity.
   */
  public C3mlEntity(String id) {
    this.id = id;
  }

  /**
   * Adds a {@link C3mlEntity} as a child of this one
   * 
   * @param child The {@link C3mlEntity} to add.
   */
  public void addChild(C3mlEntity child) {
    if (child == null) {
      return;
    }
    children.add(child);
    child.setParentId(getId());
  }

  /**
   * Adds a parameter to the entity.
   * 
   * @param name The name of the parameter.
   * @param value The entity's parameter value.
   */
  public void addProperty(String name, String value) {
    properties.put(name, value);
  }

  /**
   * Sets the color of the entity.
   * 
   * @param colorData The RGBA color values.
   */
  public void setColorData(Color colorData) {
    color = new ArrayList<>();
    color.add(colorData.getRed());
    color.add(colorData.getGreen());
    color.add(colorData.getBlue());
    color.add(colorData.getAlpha());
  }

}
