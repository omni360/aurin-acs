package au.com.mutopia.acs.models.c3ml;

import lombok.Getter;
import lombok.Setter;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An object representing the contents of a C3ML document.
 * 
 * @see https://docs.google.com/document/d/1dNMQyOjIN6AGPIpUqw0ZdKRrLbrTjws6itGeo4dtHYU
 */
@Getter
@Setter
public class C3mlEntity {

  private String id;
  private String name;

  /** The name of the type of the entity. */
  private String type;

  /** The ID of this entity's parent entity. */
  private String parentId;

  /** The {@link C3mlEntity} objects that belong 'within' this one. */
  private List<C3mlEntity> children = new ArrayList<>();

  /** A map of parameter names to values of this entity. */
  private Map<String, String> parameters = new HashMap<>();

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


  public C3mlEntity(String id) {
    this.id = id;
  }

  public void addChild(C3mlEntity child) {
    children.add(child);
    child.setParentId(getId());
  }

  public void addParameter(String name, String value) {
    parameters.put(name, value);
  }

  public void setColorData(Color colorData) {
    color = new ArrayList<>();
    color.add(colorData.getRed());
    color.add(colorData.getGreen());
    color.add(colorData.getBlue());
    color.add(colorData.getAlpha());
  }
}
