package au.com.mutopia.acs.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import lombok.extern.log4j.Log4j;

import org.apache.commons.io.IOUtils;
import org.bimserver.client.BimServerClient;
import org.bimserver.interfaces.objects.SDeserializerPluginConfiguration;
import org.bimserver.interfaces.objects.SProject;
import org.bimserver.interfaces.objects.SSerializerPluginConfiguration;
import org.bimserver.shared.PublicInterfaceNotFoundException;
import org.bimserver.shared.exceptions.ServiceException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.type.MapType;

import com.google.common.collect.Maps;
import com.google.inject.Inject;

import flexjson.JSONDeserializer;

/**
 * Sends requests to BIMserver to extract IFC geometries as COLLADA models.
 */
@Log4j
public class IfcExtractor {

  /** The name of BIMserver IFC serializer plugin. */
  private static final String JSON_SERIALIZER_NAME = "JsonIfcGeometryTreeSerializer";

  /**
   * Temporary solution for creating a JSON string usable by ACE IFC reader.
   * 
   * TODO(Brandon) Refactor/improve workaround solution if unable to find the IFC plugin.
   */
  Map<String, Object> jsonGeometries;

  /**
   * BIMserver client factory using pre-configured authentication information.
   */
  private BimServerAuthenticator authenticator;

  @Inject
  public IfcExtractor(BimServerAuthenticator authenticator) {
    this.authenticator = authenticator;
  }

  /**
   * Extracts the geometry from the IFC file.
   * 
   * @param ifcFile The IFC file to convert.
   * @return The extracted geometry as the bytes of a JSON string.
   */
  public byte[] extractJson(File ifcFile) {
    BimServerClient client = authenticator.connectToBimServer();
    return getJsonGeometry(client, ifcFile);
  }

  /**
   * Gets Json Geometry string from BimServer. Note: BimServer must be running for geometry
   * serializer to work. material color, parameters and object hierarchy.
   *
   * @param client The {@link BimServerClient} to access the BIMserver API with.
   * @param ifcFile The IFC file to be converted.
   * @return Json Geometry string converted from the IFC file.
   */
  private byte[] getJsonGeometry(BimServerClient client, File ifcFile) {
    try {
      // Create a temporary project to upload the IFC file to.
      SProject newProject = client.getBimsie1ServiceInterface().addProject("test" + Math.random());

      // Find the most suitable deserializer for IFC.
      SDeserializerPluginConfiguration deserializer =
          client.getBimsie1ServiceInterface().getSuggestedDeserializerForExtension("ifc");

      // Checkin - upload file to project.
      client.checkin(newProject.getOid(), "test", deserializer.getOid(), false, true, ifcFile);

      SSerializerPluginConfiguration jsonIfcGeomSerializer =
          client.getBimsie1ServiceInterface().getSerializerByName(JSON_SERIALIZER_NAME);
      if (jsonIfcGeomSerializer == null) {
        log.debug("jsonIfcGeomSerializer is null");
        return null;
      }

      // Find a serializer plugin.
      // Currently using JsonGeometry because it does most of what is required, new plugin is
      // required to
      // extract more specific data.
      SSerializerPluginConfiguration jsonGeometrySerializerSerializer = jsonIfcGeomSerializer;

      // Get the project details, seems redundant but it makes the api works.
      // Maybe it is required to get the latest 'image' of the project from BimServer.
      newProject = client.getBimsie1ServiceInterface().getProjectByPoid(newProject.getOid());

      // Download the latest revision (the one we just checked in).
      // Convert the data to Json string.
      Long downloadId =
          client.getBimsie1ServiceInterface().download(newProject.getLastRevisionId(),
              jsonGeometrySerializerSerializer.getOid(), true, false); // Note: sync: false
      InputStream downloadData =
          client.getDownloadData(downloadId, jsonGeometrySerializerSerializer.getOid());

      client.getBimsie1ServiceInterface().deleteProject(newProject.getOid());

      byte[] result = IOUtils.toByteArray(downloadData);
      log.debug("Size of Result: " + result.length);
      return result;
    } catch (PublicInterfaceNotFoundException | ServiceException | IOException e) {
      log.error(e);
    }

    return null;
  }

  /**
   * Gets JSON geometry string from BIMserver.
   * 
   * Note: BIMserver must be running for geometry serializer to work.
   *
   * @param client The {@link BimServerClient} to access the BIMserver API with.
   * @param ifcFile The IFC file to be converted.
   */
  public List<Map<String, Object>> getJsonGeometryAndSceneJs(BimServerClient client, File ifcFile) {
    try {
      jsonGeometries = Maps.newHashMap();

      // Create a temporary project to upload the IFC file to.
      SProject newProject = client.getBimsie1ServiceInterface().addProject("test" + Math.random());

      // Find the most suitable deserializer for IFC.
      SDeserializerPluginConfiguration deserializer =
          client.getBimsie1ServiceInterface().getSuggestedDeserializerForExtension("ifc");

      // Checkin - upload file to project.
      client.checkin(newProject.getOid(), "test", deserializer.getOid(), false, true, ifcFile);

      // Get the project details, seems redundant but it makes the api works.
      // Maybe it is required to get the latest 'image' of the project from BimServer.
      newProject = client.getBimsie1ServiceInterface().getProjectByPoid(newProject.getOid());

      // Currently using JsonGeometry because it does most of what is required, new plugin is
      // required to
      // extract more specific data.
      SSerializerPluginConfiguration jsonGeometrySerializerSerializer =
          client.getBimsie1ServiceInterface().getSerializerByName("JsonGeometrySerializer");

      // Currently using JsonGeometry because it does most of what is required, new plugin is
      // required to
      // extract more specific data.
      SSerializerPluginConfiguration sceneJsSerializerSerializer =
          client.getBimsie1ServiceInterface().getSerializerByName("SceneJsShellSerializer");

      // Download the latest revision (the one we just checked in).
      // Convert the data to Json string.
      Long jsonGeomId =
          client.getBimsie1ServiceInterface().download(newProject.getLastRevisionId(),
              jsonGeometrySerializerSerializer.getOid(), true, false); // Note: sync: false
      InputStream jsonGeometryInput =
          client.getDownloadData(jsonGeomId, jsonGeometrySerializerSerializer.getOid());
      String jsonGeometry = IOUtils.toString(jsonGeometryInput, Charset.defaultCharset().name());

      Long sceneJsId =
          client.getBimsie1ServiceInterface().download(newProject.getLastRevisionId(),
              sceneJsSerializerSerializer.getOid(), true, false); // Note: sync: false
      InputStream sceneJsInput =
          client.getDownloadData(sceneJsId, sceneJsSerializerSerializer.getOid());
      String sceneJs = IOUtils.toString(sceneJsInput, Charset.defaultCharset().name());

      List<Map<String, Object>> deserializedJsonContent =
          new JSONDeserializer<Map<String, List<Map<String, Object>>>>().deserialize(jsonGeometry)
              .get("geometry");
      if (deserializedJsonContent != null) {
        for (Map<String, Object> sample : deserializedJsonContent) {
          if (!sample.isEmpty()) {
            jsonGeometries.put((String) sample.get("coreId"), sample);
          }
        }
      }

      ObjectMapper mapper = new ObjectMapper();
      MapType mapType =
          mapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class);
      Map<String, Object> deserializedSceneJs = mapper.readValue(sceneJs, mapType);
      Map<String, Object> sceneJsData = (Map<String, Object>) deserializedSceneJs.get("data");
      for (Map<String, Object> ifcObject : (List<Map<String, Object>>) sceneJsData
          .get("relationships")) {
        addGeometries(ifcObject);
      }

      return (List<Map<String, Object>>) sceneJsData.get("relationships");
    } catch (PublicInterfaceNotFoundException | ServiceException | IOException e) {
      log.error(e);
    }

    return null;
  }

  /**
   * Add geometry data to scene js shell objects.
   */
  private void addGeometries(Map<String, Object> ifcObject) {
    if (jsonGeometries.containsKey("" + ifcObject.get("id"))) {
      ifcObject.put("geometry", jsonGeometries.get("" + ifcObject.get("id")));
    }
    if (ifcObject.containsKey("decomposedBy")) {
      for (Map<String, Object> childObject : (List<Map<String, Object>>) ifcObject
          .get("decomposedBy")) {
        addGeometries(childObject);
      }
    }
    if (ifcObject.containsKey("contains")) {
      for (Map<String, Object> childObject : (List<Map<String, Object>>) ifcObject.get("contains")) {
        addGeometries(childObject);
      }
    }
  }


}
