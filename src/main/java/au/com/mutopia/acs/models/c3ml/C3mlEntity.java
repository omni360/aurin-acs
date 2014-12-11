package au.com.mutopia.acs.models.c3ml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.dddviewr.collada.states.param;

import lombok.Getter;
import lombok.Setter;

/**
 * An object representing the contents of a C3ML document.
 * 
 * @see https://docs.google.com/document/d/1dNMQyOjIN6AGPIpUqw0ZdKRrLbrTjws6itGeo4dtHYU
 */
@Getter
@Setter
public class C3mlEntity {

  private UUID id;
  private String name;
  private String type;
  private UUID parentId;
  private List<C3mlEntity> children = new ArrayList<>();
  private Map<String, String> parameters = new HashMap<>();

  /** A list of coordinates of points making up the entity geometry (2D entities only). */
  private List<Vertex3D> coordinates;
  private List<Integer> color;
  private List<Double> scale;
  private List<Double> rotation;

  private List<Double> positions;
  private List<Double> normals;
  private List<Integer> triangles;
  private List<Double> geoLocation;

  private String gltfUrl;
  private Byte[] gltfData;


  public C3mlEntity(UUID id) {
    this.id = id;
  }

  public void addChild(C3mlEntity child) {
    children.add(child);
    child.setParentId(getId());
  }

  public void addParameter(String name, String value) {
    parameters.put(name, value);
  }

}
