package au.com.mutopia.acs.conversion.output;

import java.awt.Color;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import lombok.extern.log4j.Log4j;
import au.com.mutopia.acs.models.c3ml.C3mlEntity;
import au.com.mutopia.acs.models.c3ml.Vertex3D;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

/**
 * A writer for COLLADA files.
 */
@Log4j
public class ColladaWriter {

  /**
   * The base indent level for entity nodes, used for XML pretty print.
   */
  private static final String NODE_INDENT = "      ";

  /**
   * The default color to use for geometries if color is missing.
   */
  private static final List<Integer> DEFAULT_COLOR = ImmutableList.of(255, 255, 255, 0);

  private StringWriter geomWriter = new StringWriter();
  private StringWriter writer = new StringWriter();

  /**
   * The map of color strings (#RRGGBB) to their respective colors, used to unsure materials are not
   * duplicated.
   */
  private Map<String, Color> materials = Maps.newHashMap();

  /**
   * The geographic location of the COLLADA model's origin.
   */
  private Vertex3D modelGeoOrigin;

  public void initWriter() {
    writer = new StringWriter();
    geomWriter = new StringWriter();
  }

  /**
   * Starts the COLLADA document. Starts writing COLLADA XML headers.
   */
  public void startDocument() {
    try {
      writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
      writer
          .write("<COLLADA xmlns=\"http://www.collada.org/2005/11/COLLADASchema\" version=\"1.4.1\">\n");

    } catch (Exception e) {
      log.error(e);
    }
  }

  /**
   * Ends the COLLADA document. Closes the XML element.
   */
  public void endDocument() {
    try {
      writer.write("</COLLADA>");
    } catch (Exception e) {
      log.error(e);
    }
    writer.flush();
  }

  /**
   * Writes the COLLADA asset, which is a description of the COLLADA file.
   */
  public void writeAssets() {
    writer.write("  <asset>\n");
    writer.write("    <contributor>\n");
    // out.write("      <author>" + (getProjectInfo() == null ? "" :
    // getProjectInfo().getAuthorName()) + "</author>");
    writer.write("      <authoring_tool>Synthesis</authoring_tool>\n");
    // out.write("      <comments>" + (getProjectInfo() == null ? "" :
    // getProjectInfo().getDescription()) + "</comments>");
    writer.write("    </contributor>\n");
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss'Z'");
    String date = dateFormat.format(new Date());
    writer.write("    <created>" + date + "</created>\n");
    writer.write("    <modified>" + date + "</modified>\n");
    // if (lengthUnitPrefix == null) {
    writer.write("    <unit meter=\"1\" name=\"meter\"/>\n");
    // } else {
    // out.write("    <unit meter=\"" + Math.pow(10.0, lengthUnitPrefix.getValue()) + "\" name=\""
    // + lengthUnitPrefix.name().toLowerCase() + "\"/>");
    // }
    writer.write("    <up_axis>Z_UP</up_axis>\n");
    writer.write("  </asset>\n");
  }

  /**
   * Writes the geometries to COLLADA file.
   */
  public void writeGeometries() {
    writer.write("  <library_geometries>\n");
    writer.write(geomWriter.toString());
    writer.write("  </library_geometries>\n");
  }

  /**
   * Writes the individual geometry to temporary String writer, before appending to final COLLADA
   * file.
   *
   * @param entity The entity with geometry to be written to COLLADA file.
   */
  private void writeGeometry(C3mlEntity entity) {
    // Geometry geometry = mesh.getGeometry();
    if (entity != null) {// if (geometry != null) {
      String id = entity.getId();

      String name = "NO_NAME";
      if (entity.getName() != null) {
        name = entity.getName();
      }
      if (entity.getColor() == null) {
        entity.setColor(DEFAULT_COLOR);
      }

      int posLength = entity.getPositions().size();

      geomWriter.write("    <geometry id=\"geom-" + id + "\" name=\"" + name + "\">\n");
      geomWriter.write("      <mesh>\n");
      geomWriter.write("        <source id=\"positions-" + id + "\" name=\"positions-" + id
          + "\">\n");
      geomWriter.write("          <float_array id=\"positions-array-" + id + "\" count=\""
          + posLength + "\">");
      for (int i = 0; i < posLength; i++) {
        if (i < posLength - 1) {
          geomWriter.write(entity.getPositions().get(i) + " ");
        } else {
          geomWriter.write(entity.getPositions().get(i) + "");
        }
      }
      geomWriter.write("</float_array>\n");
      geomWriter.write("          <technique_common>\n");
      geomWriter.write("            <accessor count=\"" + (posLength / 3)
          + "\" offset=\"0\" source=\"#positions-array-" + id + "\" stride=\"3\">\n");
      geomWriter.write("              <param name=\"X\" type=\"float\"></param>\n");
      geomWriter.write("              <param name=\"Y\" type=\"float\"></param>\n");
      geomWriter.write("              <param name=\"Z\" type=\"float\"></param>\n");
      geomWriter.write("            </accessor>\n");
      geomWriter.write("          </technique_common>\n");
      geomWriter.write("        </source>\n");

      geomWriter.write("        <source id=\"normals-" + id + "\" name=\"normals-" + id + "\">\n");
      geomWriter.write("          <float_array id=\"normals-array-" + id + "\" count=\""
          + posLength + "\">");
      for (int i = 0; i < posLength; i++) {
        double normal = entity.getNormals().get(i);
        if (i < posLength - 1) {
          geomWriter.write(normal + " ");
        } else {
          geomWriter.write(normal + "");
        }
      }
      geomWriter.write("</float_array>\n");
      geomWriter.write("          <technique_common>\n");
      geomWriter.write("            <accessor count=\"" + (posLength / 3)
          + "\" offset=\"0\" source=\"#normals-array-" + id + "\" stride=\"3\">\n");
      geomWriter.write("              <param name=\"X\" type=\"float\"></param>\n");
      geomWriter.write("              <param name=\"Y\" type=\"float\"></param>\n");
      geomWriter.write("              <param name=\"Z\" type=\"float\"></param>\n");
      geomWriter.write("            </accessor>\n");
      geomWriter.write("          </technique_common>\n");
      geomWriter.write("        </source>\n");

      geomWriter.write("        <vertices id=\"vertices-" + id + "\">\n");
      geomWriter.write("          <input semantic=\"POSITION\" source=\"#positions-" + id
          + "\"/>\n");
      geomWriter.write("          <input semantic=\"NORMAL\" source=\"#normals-" + id + "\"/>\n");
      geomWriter.write("        </vertices>\n");

      geomWriter.write("        <triangles count=\"" + entity.getTriangles().size() / 3
          + "\" material=\"material-" + getColorString(entity.getColor()) + "\">\n");
      geomWriter.write("          <input offset=\"0\" semantic=\"VERTEX\" source=\"#vertices-" + id
          + "\"/>\n");
      geomWriter.write("            <p>");
      for (int i = 0; i < entity.getTriangles().size(); i++) {
        int triangle = entity.getTriangles().get(i);
        if (i < entity.getTriangles().size() - 1) {
          geomWriter.write(triangle + " ");
        } else {
          geomWriter.write(triangle + "");
        }
      }
      geomWriter.write("</p>\n");
      geomWriter.write("        </triangles>\n");
      geomWriter.write("      </mesh>\n");
      geomWriter.write("    </geometry>\n");
    }
  }

  /**
   * Gets the color string from HexString.
   */
  private String getColorString(Color color) {
    return String.format("%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
  }

  private String getColorString(List<Integer> rgb) {
    return getColorString(new Color(rgb.get(0), rgb.get(1), rgb.get(2)));
  }

  private Color buildColor(C3mlEntity entity) {
    return new Color(entity.getColor().get(0), entity.getColor().get(1), entity.getColor().get(2));
  }

  /**
   * Writes the COLLADA instance scene.
   */
  public void writeScene() {
    writer.write("  <scene>\n");
    writer.write("    <instance_visual_scene url=\"#VisualSceneNode\"/>\n");
    writer.write("  </scene>\n");
  }

  /**
   * Writes the COLLADA visual scenes, which represents the hierarchy of nodes within the COLLADA
   * scene.
   */
  public void startWriteVisualScenes() {
    writer.write("  <library_visual_scenes>\n");
    writer.write("    <visual_scene id=\"VisualSceneNode\" name=\"VisualSceneNode\">\n");
  }

  /**
   * Ends the COLLADA visual scenes node.
   */
  public void endWriteVisualScenes() {
    writer.write("    </visual_scene>\n");
    writer.write("  </library_visual_scenes>\n");
  }

  /**
   * Starts writing the individual entity (node).
   *
   * @param id The id of the entity.
   * @param name The name of the entity.
   * @param level The hierarchy level of the entity.
   */
  public void startObjectNode(String id, String name, int level) {
    String indent = NODE_INDENT;
    for (int i = 1; i < level; i++) {
      indent += "  ";
    }

    writer.write(indent + "<node id=\"node-" + id + "\" name=\"" + name + "\">\n");
  }

  /**
   * Ends writing the entity, closes the entity node.
   */
  public void endObjectNode(int level) {
    String indent = NODE_INDENT;
    for (int i = 1; i < level; i++) {
      indent += "  ";
    }
    writer.write(indent + "</node>\n");
  }

  /**
   * Writes the mesh only hierarchy to COLLADA.
   *
   * @param entity The top level entity to be written to COLLADA.
   * @param entityIdMap The map of entity IDs to object to retrieve children entities.
   * @param level The hierarchy level of the entity.
   */
  public void writeMeshHierarchy(C3mlEntity entity, Map<String, C3mlEntity> entityIdMap,
      int level) {
    writeMeshNode(entity, level);
    level++;
    for (String childId : entity.getChildrenIds()) {
      writeMeshHierarchy(entityIdMap.get(childId), entityIdMap, level);
    }
  }

  /**
   * Writes the mesh instance node to COLLADA.
   *
   * @param entity The mesh to be written to COLLADA.
   * @param level The hierarchy level of the mesh/entity.
   */
  public void writeMeshNode(C3mlEntity entity, int level) {
    if (entity.getTriangles() != null) {
      writeGeometry(entity);
      writeNode(entity, level);
    }
  }

  /**
   * Repositions the mesh if the mesh has been moved from its original location. TODO(Brandon)
   * Calculate the new position based on new/old geolocation.
   *
   * @param entity The entity to be repositioned.
   */
  private void repositionMesh(C3mlEntity entity) {
    if (!entity.getGeoLocation().equals(modelGeoOrigin)) {
      log.info("Positioning mesh based on geo location.");
    }
  }

  /**
   * Gets the matrix data as string from mesh.
   *
   * @param entity The entity with matrix.
   * @return The matrix data as string.
   */
  private String getMatrixData(C3mlEntity entity) {
    Vertex3D scale = new Vertex3D(entity.getScale(), false);
    Vertex3D rotation = new Vertex3D(entity.getRotation(), false);

    Vertex3D transform = new Vertex3D(0, 0, 0);
    // Vertex3D transform = new Vertex3D(entity.getTranslation(), false);

    return "" + scale.getX() + " 0 0 " + transform.getX() + " 0 " + scale.getY() + " 0 "
        + transform.getY() + " 0 0 " + scale.getZ() + " " + transform.getZ() + " 0 0 0 1";
  }

  /**
   * Writes the list of nodes (library nodes) to COLLADA.
   */
  public void writeLibraryNodes() {
    writer.write("  <library_nodes>\n");
    writer.write(writer.toString());
    writer.write("  </library_nodes>\n");
  }

  /**
   * Writes the instance node to COLLADA.
   *
   * @param entity The entity to be written to COLLADA as node.
   * @param level The hierarchy level of the entity.
   */
  private void writeNode(C3mlEntity entity, int level) {
    String indent = NODE_INDENT;
    for (int i = 1; i < level; i++) {
      indent += "  ";
    }

    String entityId = entity.getId();
    if (entity.getColor() == null) {
      entity.setColor(DEFAULT_COLOR);
    }
    String colorString = getColorString(entity.getColor());
    if (!materials.containsKey(colorString)) {
      materials.put(colorString, buildColor(entity));
    }
    writer.write(indent + "<node id=\"instance_node-" + entityId + "\" name=\""
        + entity.getName() + "\">\n");
    writer.write(indent + "  <matrix>");
    writer.write(getMatrixData(entity));
    writer.write("</matrix>\n");
    writer.write(indent + "  <instance_geometry url=\"#geom-" + entityId + "\">\n");
    writer.write(indent + "   <bind_material>\n");
    writer.write(indent + "     <technique_common>\n");
    writer.write(indent + "        <instance_material symbol=\"material-"
        + getColorString(entity.getColor()) + "\" target=\"#material-"
        + getColorString(entity.getColor()) + "\"/>\n");
    writer.write(indent + "      </technique_common>\n");
    writer.write(indent + "    </bind_material>\n");
    writer.write(indent + "  </instance_geometry>\n");

    // Writes the entity's properties as extra element.
    Map<String, String> properties = entity.getProperties();
    if (properties.keySet().size() > 0) {
      writer.write(indent + "  <extra>\n");
      writer.write(indent + "    <technique profile=\"OpenCOLLADA\">\n");
      writer.write(indent + "      <user_properties>\n");
      for (String propertyName : properties.keySet()) {
        writer.write(indent + "        " + propertyName + " = " + properties.get(propertyName) +
            "\n");
      }
      writer.write(indent + "      </user_properties>\n");
      writer.write(indent + "    </technique>\n");
      writer.write(indent + "  </extra>\n");
    }

    writer.write(indent + "</node>\n");
  }

  /**
   * Writes the list of effects (library effects) to COLLADA.
   */
  public void writeEffects() {
    writer.write("  <library_effects>\n");
    for (Map.Entry<String, Color> material : materials.entrySet()) {
      writeEffect(material.getKey(), material.getValue());
    }
    writer.write("  </library_effects>\n");
  }

  /**
   * Write the effect to COLLADA, given the material name.
   *
   * @param materialName The string representation of the material.
   */
  private void writeEffect(String materialName, Color color) {
    writer.write("    <effect id=\"fx-" + materialName + "\">\n");
    writer.write("      <profile_COMMON>\n");
    writer.write("        <technique sid=\"common\">\n");
    writer.write("          <lambert>\n");
    writer.write("            <diffuse>\n");
    writer.write("              <color>" + color.getRed() / 255.0f + " " + color.getGreen()
        / 255.0f + " " + color.getBlue() / 255.0f + " " + color.getAlpha() / 255.0f + "</color>\n");
    writer.write("            </diffuse>\n");
    writer.write("          </lambert>\n");
    writer.write("        </technique>\n");
    writer.write("      </profile_COMMON>\n");
    writer.write("    </effect>\n");
  }

  /**
   * Writes the lighting to COLLADA.
   */
  private void writeLights() {
    writer.write("  <library_lights>\n");
    writer.write("    <light id=\"light-lib\" name=\"light\">\n");
    writer.write("      <technique_common>\n");
    writer.write("        <point>\n");
    writer.write("          <color>1 1 1</color>\n");
    writer.write("          <constant_attenuation>1</constant_attenuation>\n");
    writer.write("          <linear_attenuation>0</linear_attenuation>\n");
    writer.write("          <quadratic_attenuation>0</quadratic_attenuation>\n");
    writer.write("        </point>\n");
    writer.write("      </technique_common>\n");
    writer.write("      <technique profile=\"MAX3D\">\n");
    writer.write("        <intensity>1.000000</intensity>\n");
    writer.write("      </technique>\n");
    writer.write("    </light>\n");
    writer.write("    <light id=\"pointLightShape1-lib\" name=\"pointLightShape1\">\n");
    writer.write("      <technique_common>\n");
    writer.write("        <point>\n");
    writer.write("          <color>1 1 1</color>\n");
    writer.write("          <constant_attenuation>1</constant_attenuation>\n");
    writer.write("          <linear_attenuation>0</linear_attenuation>\n");
    writer.write("          <quadratic_attenuation>0</quadratic_attenuation>\n");
    writer.write("        </point>\n");
    writer.write("      </technique_common>\n");
    writer.write("    </light>\n");
    writer.write("  </library_lights>\n");
  }

  /**
   * Writes the library cameras to COLLADA.
   */
  private void writeCameras() {
    writer.write("  <library_cameras>\n");
    writer.write("    <camera id=\"PerspCamera\" name=\"PerspCamera\">\n");
    writer.write("      <optics>\n");
    writer.write("        <technique_common>\n");
    writer.write("          <perspective>\n");
    writer.write("            <yfov>37.8493</yfov>\n");
    writer.write("            <aspect_ratio>1</aspect_ratio>\n");
    writer.write("            <znear>10</znear>\n");
    writer.write("            <zfar>1000</zfar>\n");
    writer.write("          </perspective>\n");
    writer.write("        </technique_common>\n");
    writer.write("      </optics>\n");
    writer.write("    </camera>\n");
    writer.write("    <camera id=\"testCameraShape\" name=\"testCameraShape\">\n");
    writer.write("      <optics>\n");
    writer.write("        <technique_common>\n");
    writer.write("          <perspective>\n");
    writer.write("            <yfov>37.8501</yfov>\n");
    writer.write("            <aspect_ratio>1</aspect_ratio>\n");
    writer.write("            <znear>0.01</znear>\n");
    writer.write("            <zfar>1000</zfar>\n");
    writer.write("          </perspective>\n");
    writer.write("        </technique_common>\n");
    writer.write("      </optics>\n");
    writer.write("    </camera>\n");
    writer.write("  </library_cameras>\n");
  }

  /**
   * Writes the list of materials (library materials) to COLLADA.
   */
  public void writeMaterials() {
    writer.write("  <library_materials>\n");
    for (String materialName : materials.keySet()) {
      writeMaterial(materialName);
    }
    writer.write("  </library_materials>\n");
  }

  /**
   * Write the material to COLLADA, given the material name.
   *
   * @param materialName The string representation of the material.
   */
  private void writeMaterial(String materialName) {
    writer.write("    <material id=\"material-" + materialName + "\" name=\"material-"
        + materialName + "\">\n");
    writer.write("      <instance_effect url=\"#fx-" + materialName + "\"/>\n");
    writer.write("    </material>\n");
  }

  /**
   * Gets the converted COLLADA file contents as string.
   *
   * @return The string representation of the COLLADA file.
   */
  public String getResultAsString() {
    return writer.toString();
  }

  /**
   * Gets the converted COLLADA file as byte array. TODO(Brandon) rename the file.
   *
   * @return The converted COLLADA file.
   */
  public byte[] getFileAsBytes() {
    return getResultAsString().getBytes(StandardCharsets.UTF_8);
  }
}
