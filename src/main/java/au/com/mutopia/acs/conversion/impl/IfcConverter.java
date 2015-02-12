package au.com.mutopia.acs.conversion.impl;

import gov.nasa.worldwind.geom.Angle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.log4j.Log4j;

import org.apache.commons.collections.CollectionUtils;

import au.com.mutopia.acs.exceptions.ConversionException;
import au.com.mutopia.acs.models.Asset;
import au.com.mutopia.acs.models.c3ml.C3mlEntity;
import au.com.mutopia.acs.models.c3ml.C3mlEntityType;
import au.com.mutopia.acs.util.BimServerAuthenticator;
import au.com.mutopia.acs.util.IfcExtractor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gson.Gson;

/**
 * Converts IFC files into a collection of {@link C3mlEntity} objects.
 */
@Log4j
public class IfcConverter extends AbstractConverter {

  private BimServerAuthenticator auth;

  @SuppressWarnings("serial")
  private static final class IfcJson extends HashMap<String, List<Map<String, Object>>> {}

  public IfcConverter(BimServerAuthenticator auth) {
    this.auth = auth;
  }

  /**
   * Converts the given IFC asset to a list of {@link C3mlEntity} objects.
   */
  public List<C3mlEntity> convert(Asset asset, boolean merge) throws ConversionException {
    log.debug("Converting IFC asset " + asset + "...");
    try {
      byte[] json = new IfcExtractor(auth).extractJson(asset.getTemporaryFile());
      IfcJson ifc = new ObjectMapper().readValue(json, IfcJson.class);
      List<Map<String, Object>> data = ifc.get("data");
      return getEntities(data);
    } catch (IOException e) {
      throw new ConversionException("Failed to convert IFC asset " + asset + " as file", e);
    }
  }

  private List<C3mlEntity> getEntities(List<Map<String, Object>> ifcData) {
    List<C3mlEntity> entities = new ArrayList<>();
    for (Map<String, Object> ifcObject : ifcData) {
      entities.add(buildEntity(ifcObject, null));
    }
    return entities;
  }

  /**
   * Populates the {@link GeoData} from {@link C3mlEntity}s created from the map representing the
   * IFC objects hierarchy.
   *
   * @param geoData The top level hierarchy containing list of converted {@link C3mlEntity}s.
   * @param ifcObjectMap The map representing the top level IFC object hierarchy.
   * @param parent The parent of {@link C3mlEntity}s being created.
   */
  private C3mlEntity buildEntity(Map<String, Object> ifcObjectMap, C3mlEntity parent) {
    C3mlEntity entity = new C3mlEntity();

    entity.setName((String) ifcObjectMap.get("name"));

    addBasicProperties(ifcObjectMap, entity);

    if (ifcObjectMap.containsKey("geometry")) {
      addGeometryValues(ifcObjectMap, entity);
      if (entity.getGeoLocation() == null) {
        entity.setGeoLocation(ImmutableList.of(0.0, 0.0, 0.0));
      }

    }
    if (ifcObjectMap.containsKey("parameters")) {
      Map<String, Object> parameters = (Map<String, Object>) ifcObjectMap.get("parameters");
      for (Map.Entry<String, Object> paramEntry : parameters.entrySet()) {
        entity.addProperty(paramEntry.getKey(), paramEntry.getValue().toString());
      }
    }

    // GeoLeaf geometry = entity.getGeometry();
    // if (geometry != null) {
    // if (geometry instanceof GeoEntity) {
    // entity.deleteGeometry();
    // }
    // }
    if (ifcObjectMap.containsKey("decomposedBy")) {
      for (Map<String, Object> childData : (List<Map<String, Object>>) ifcObjectMap
          .get("decomposedBy")) {
        buildEntity(childData, entity);
      }
    }
    if (ifcObjectMap.containsKey("contains")) {
      for (Map<String, Object> childData : (List<Map<String, Object>>) ifcObjectMap.get("contains")) {
        buildEntity(childData, entity);
      }
    }

    // Return the parent if there is one.
    if (parent != null) {
      parent.addChild(entity);
      return parent;
    } else {
      return entity;
    }
  }

  /**
   * Sets basic property values on the given {@link C3mlEntity}.
   * 
   * @param ifcObjectMap The map of basic property data.
   * @param entity The entity to set the data on.
   */
  private void addBasicProperties(Map<String, Object> ifcObjectMap, C3mlEntity entity) {
    String ifcType = (String) ifcObjectMap.get("type");
    entity.addProperty("type", ifcType);

    // entity.setDescription(ifcType);
    // entity.setIfcType(ifcType);

    // Set the uniform scale, if present.
    Object lengthUnitConversion = ifcObjectMap.get("lengthUnitConversion");
    if (lengthUnitConversion != null) {
      double scale = (double) lengthUnitConversion;
      entity.setScale(ImmutableList.of(scale, scale, scale));
    }

    // Set geolocation if present.
    Object latitudeObject = ifcObjectMap.get("latitude");
    Object longitudeObject = ifcObjectMap.get("longitude");

    if (latitudeObject == null || longitudeObject == null) return;

    String latitudeString = (String) latitudeObject;
    String longitudeString = (String) longitudeObject;

    Gson gson = new Gson();
    ArrayList<Double> latitudeList = gson.fromJson(latitudeString, ArrayList.class);
    ArrayList<Double> longitudeList = gson.fromJson(longitudeString, ArrayList.class);

    double y =
        Angle.fromDMS(latitudeList.get(0).intValue(), Math.abs(latitudeList.get(1).intValue()),
            Math.abs(latitudeList.get(2).intValue())).getDegrees();

    double x =
        Angle.fromDMS(longitudeList.get(0).intValue(), Math.abs(longitudeList.get(1).intValue()),
            Math.abs(longitudeList.get(2).intValue())).getDegrees();

    entity.setGeoLocation(ImmutableList.of(x, y, 0.0));
  }

  /**
   * Sets the geometry values on the given {@link C3mlEntity}.
   * 
   * @param ifcObjectMap The map of geometry data.
   * @param entity The entity to set the data on.
   */
  private void addGeometryValues(Map<String, Object> ifcObjectMap, C3mlEntity entity) {
    Map<String, Object> geometry = (Map<String, Object>) ifcObjectMap.get("geometry");
    if (geometry.isEmpty()) return;

    String primitive = (String) geometry.get("primitive");
    String material = (String) geometry.get("material");
    List<Double> positions = (List<Double>) geometry.get("positions");
    List<Double> normals = (List<Double>) geometry.get("normals");
    List<Integer> triangles = (List<Integer>) geometry.get("triangles");
    List<Double> colorData = (List<Double>) geometry.get("color");

    if (CollectionUtils.isEmpty(positions) || CollectionUtils.isEmpty(normals)
        || triangles.size() < 3) {
      return;
    }
    entity.setType(C3mlEntityType.MESH);

    List<Integer> indices = Lists.newArrayList();
    for (Integer triangle : triangles) {
      indices.add(triangle.intValue());
    }

    entity.setPositions(positions);
    entity.setNormals(normals);
    entity.setTriangles(indices);

    List<Integer> color = new ArrayList<>();
    for (int i : new int[] {0, 1, 2, 3}) {
      long value = Math.round(255.0 * colorData.get(i));
      color.add(Integer.valueOf(Long.toString(value)));
    }
    entity.setColor(color);

    entity.setRotation(ImmutableList.of(0.0, 0.0, 0.0));

    // Transform the location.
    if (geometry.containsKey("matrix")) {
      List<Double> matrix = (List<Double>) geometry.get("matrix");
      // TODO(orlade): Transform.
      // entity.transform(Doubles.toArray(matrix));
    }
  }

}
