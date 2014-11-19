package au.com.mutopia.acs.models.c3ml;

import java.util.List;
import java.util.UUID;

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
  private String type;
  private String parentId;
  private List<List<Double>> coordinates;
  private List<Integer> color;
  private List<Double> scale;
  private List<Double> rotation;

  private List<Double> positions;
  private List<Double> normals;
  private List<Integer> triangles;
  private List<Double> geoLocation;

}
