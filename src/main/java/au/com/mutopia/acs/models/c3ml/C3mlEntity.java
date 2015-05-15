package au.com.mutopia.acs.models.c3ml;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;
import au.com.mutopia.acs.util.geometry.GeometryUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;

/**
 * An object representing the contents of a C3ML document.
 *
 * @see <a
 *      href="https://docs.google.com/document/d/1dNMQyOjIN6AGPIpUqw0ZdKRrLbrTjws6itGeo4dtHYU">Unofficial
 *      C3ML specification</a>
 */
@Getter
@Setter
@JsonInclude(Include.NON_NULL)
public class C3mlEntity {
  private String id;
  private String name;

  /** The name of the type of the entity, default to empty container without geometries. */
  private C3mlEntityType type = C3mlEntityType.COLLECTION;

  /** The ID of this entity's parent entity. */
  private String parentId;

  /** Whether to display the entity by default when rendering. */
  private boolean show = true;

  /** The type of form that should be displayed for a {@link C3mlEntityType#FEATURE} entity. */
  private String displayMode;

  /**
   * The {@link C3mlEntity} objects that belong 'within' this one. This is not serialized to JSON
   * C3ML.
   */
  @JsonIgnore
  private List<C3mlEntity> children = new ArrayList<>();

  /** Map of form type to the ID of another entity that provides that form of this feature. */
  private Map<String, String> forms = new HashMap<String, String>();

  /** The IDs of the child entities. This is what the children field is expected to contain. */
  @JsonProperty("children")
  private List<String> childrenIds = new ArrayList<>();

  /** A map of property names to values of this entity. */
  private Map<String, String> properties = new HashMap<>();

  /**
   * A list of coordinates of points making up the entity geometry (2D entities only). Each vertex
   * represents the longitude, latitude and elevation of a point.
   */
  private List<Vertex3D> coordinates = new ArrayList<>();

  /**
   * A list of polygons (each a list of coordinates) specifying the holes in the polygon as polygons
   * to cut out of the original shape.
   */
  private List<List<Vertex3D>> holes = new ArrayList<>();

  /**
   * The color of the entity, if any solid color should be applied. The list of numbers represents
   * an RGBA vector (0 to 255 for red, green, blue and alpha).
   */
  private List<Integer> color = new ArrayList<>();

  /**
   * The color of the border of an entity, if any solid color should be applied. The list of numbers
   * represents an RGBA vector (0 to 255 for red, green, blue and alpha).
   */
  private List<Integer> borderColor = new ArrayList<>();

  /** The scaling factors (x, y, z) by which all coordinates should be scaled when rendered. */
  private List<Double> scale = ImmutableList.of(1.0, 1.0, 1.0);

  /**
   * The angles (in degrees around the x, y, z axes, counterclockwise) by which the coordinates
   * should be rotated when rendered.
   */
  private List<Double> rotation = ImmutableList.of(0.0, 0.0, 0.0);

  /** The extrusion height of the entity. Polygons only. */
  private Double height;

  /** The elevation of the entity from the ground. */
  private Double altitude;

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
  private List<Double> geoLocation = ImmutableList.of(0.0, 0.0, 0.0);

  /** The URL to the glTF mesh data, if applicable. */
  private String gltfUrl;

  /** The glTF mesh data as a deserialized JSON document, if applicable. */
  @JsonProperty("gltf")
  private Map<String, Object> gltfData;

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
    childrenIds.add(child.getId());
    child.setParentId(getId());
  }

  public void setCoordinates(List<Vertex3D> coordinates) {
    this.coordinates = coordinates;
    double minAltitude = GeometryUtils.getMinHeight(coordinates);
    double maxAltitude = GeometryUtils.getMaxHeight(coordinates);
    setAltitude(minAltitude);
    setHeight(maxAltitude - minAltitude);
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

  private List<Integer> buildColorArray(Color colorData) {
    List<Integer> color = new ArrayList<>();
    color.add(colorData.getRed());
    color.add(colorData.getGreen());
    color.add(colorData.getBlue());
    color.add(colorData.getAlpha());
    return color;
  }

  /**
   * Sets the color of the entity.
   *
   * @param colorData The RGBA color values.
   */
  public void setColorData(Color colorData) {
    color = buildColorArray(colorData);
  }

  /**
   * Sets the border color of the entity.
   *
   * @param colorData The RGBA color values.
   */
  public void setBorderColorData(Color colorData) {
    if (colorData == null) {
      borderColor = null;
    } else {
      borderColor = buildColorArray(colorData);
    }
  }

  @Override
  public String toString() {
    return String.format("C3mlEntity[%s]", getName());
  }

}
