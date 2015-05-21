package au.com.mutopia.acs.conversion.impl;

import au.com.mutopia.acs.util.mesh.VecMathUtil;
import com.google.common.base.Strings;
import com.google.common.primitives.Doubles;
import gov.nasa.worldwind.geom.Angle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.vecmath.Matrix4d;
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

  private static final String IFC_SITE = "IfcSite";

  /**
   * The scale to be applied on all entities contained within the site.
   */
  private double siteScale = 1;

  /**
   * The longitude of the geographic location for all entities within the site.
   */
  private double siteLongitude;

  /**
   * The latitude of the geographic location for all entities within the site.
   */
  private double siteLatitude;

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

  /**
   * Extracts a list of top level {@link C3mlEntity} for each map representing the IFC objects
   * hierarchy.
   *
   * @param ifcData The list of map representing each IFC objects hierarchy.
   * @return A list of the extracted {@link C3mlEntity} objects.
   */
  private List<C3mlEntity> getEntities(List<Map<String, Object>> ifcData) {
    List<C3mlEntity> entities = new ArrayList<>();
    for (Map<String, Object> ifcObject : ifcData) {
      entities.add(buildEntity(ifcObject));
    }
    return entities;
  }

  /**
   * Builds {@link C3mlEntity}s from the map representing the IFC objects hierarchy.
   *
   * @param ifcObjectMap The map representing the top level IFC object hierarchy.
   */
  private C3mlEntity buildEntity(Map<String, Object> ifcObjectMap) {
    C3mlEntity entity = new C3mlEntity();

    entity.setId((String) ifcObjectMap.get("id"));
    entity.setName((String) ifcObjectMap.get("name"));

    addBasicProperties(ifcObjectMap, entity);

    if (ifcObjectMap.containsKey("geometry")) {
      addGeometryValues(ifcObjectMap, entity);
    }
    if (ifcObjectMap.containsKey("parameters")) {
      Map<String, Object> parameters = (Map<String, Object>) ifcObjectMap.get("parameters");
      for (Map.Entry<String, Object> paramEntry : parameters.entrySet()) {
        entity.addProperty(paramEntry.getKey(), paramEntry.getValue().toString());
      }
    }

    if (ifcObjectMap.containsKey("decomposedBy")) {
      for (Map<String, Object> childData : (List<Map<String, Object>>) ifcObjectMap
          .get("decomposedBy")) {
        entity.addChild(buildEntity(childData));
      }
    }
    if (ifcObjectMap.containsKey("contains")) {
      for (Map<String, Object> childData : (List<Map<String, Object>>) ifcObjectMap.get("contains")) {
        entity.addChild(buildEntity(childData));
      }
    }

    return entity;
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

    if (!Strings.isNullOrEmpty(ifcType) && !ifcType.equals(IFC_SITE)) return;

    // Set the site specific uniform scale, if present.
    Object lengthUnitConversion = ifcObjectMap.get("lengthUnitConversion");
    if (lengthUnitConversion == null) {
      siteScale = 1;
    } else {
      siteScale = (double) lengthUnitConversion;
    }

    // Set site specific geolocation if present.
    Object latitudeObject = ifcObjectMap.get("latitude");
    Object longitudeObject = ifcObjectMap.get("longitude");

    if (latitudeObject == null || longitudeObject == null) {
      siteLatitude = 0;
      siteLongitude = 0;
      return;
    }

    String latitudeString = (String) latitudeObject;
    String longitudeString = (String) longitudeObject;

    Gson gson = new Gson();
    ArrayList<Double> latitudeList = gson.fromJson(latitudeString, ArrayList.class);
    ArrayList<Double> longitudeList = gson.fromJson(longitudeString, ArrayList.class);

    siteLatitude =
        Angle.fromDMS(latitudeList.get(0).intValue(), Math.abs(latitudeList.get(1).intValue()),
            Math.abs(latitudeList.get(2).intValue())).getDegrees();

    siteLongitude =
        Angle.fromDMS(longitudeList.get(0).intValue(), Math.abs(longitudeList.get(1).intValue()),
            Math.abs(longitudeList.get(2).intValue())).getDegrees();
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

    // Apply local matrix transformation if present.
    if (geometry.containsKey("matrix")) {
      List<Double> matrix = (List<Double>) geometry.get("matrix");
      // Apply local matrix transformation to the mesh.
      Matrix4d matrix4d = VecMathUtil.matrix4dFromDoubles(Doubles.toArray(matrix));
      positions = VecMathUtil.transformMeshPositions(positions, matrix4d);
      normals = VecMathUtil.transformMeshNormals(normals, matrix4d);
    }

    // Order of matrix transformation: translate, rotate, scale.
    Matrix4d rotationMatrix = VecMathUtil.createXYZAxisRotationMatrix(0, 0, 0, 0);
    Matrix4d scaleMatrix = VecMathUtil.createScaleMatrix(siteScale);
    Matrix4d translateMatrix = VecMathUtil.createTranslationMatrix(0, 0, 0);
    translateMatrix.mul(rotationMatrix);
    translateMatrix.mul(scaleMatrix);
    positions = VecMathUtil.transformMeshPositions(positions, translateMatrix);
    normals = VecMathUtil.transformMeshNormals(normals, translateMatrix);

    entity.setPositions(positions);
    entity.setNormals(normals);
    entity.setTriangles(indices);

    List<Integer> color = new ArrayList<>();
    for (int i : new int[] {0, 1, 2, 3}) {
      long value = Math.round(255.0 * colorData.get(i));
      color.add(Integer.valueOf(Long.toString(value)));
    }
    entity.setColor(color);

    entity.setGeoLocation(ImmutableList.of(siteLongitude, siteLatitude, 0.0));
  }

}
