package au.com.mutopia.acs.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;

/**
 * Parses the XML of a COLLADA file to extract the custom properties attached to the nodes within
 * the <code>&lt;extra&gt;</code> tags.
 */
public class ColladaExtraReader {

  /**
   * Extracts a map of COLLADA node IDs to maps of <code>{paramName: paramValue}</code> for custom
   * properties stored in the <code>&lt;extra&gt;</code> tags.
   * 
   * @param colladaXml The COLLADA XML to parse.
   * @return Map of node IDs to maps of <code>{paramName: paramValue}</code> for custom properties.
   * @throws IOException if the COLLADA cannot be parsed.
   */
  public Map<String, Map<String, String>> getExtraProperties(String colladaXml) throws IOException {
    Map<String, Map<String, String>> nodePropMap = new HashMap<>();
    ColladaXml collada = buildMapper().readValue(colladaXml, ColladaXml.class);
    for (ColladaNode node : collada.library_visual_scenes.visual_scene) {
      Map<String, String> propMap = new HashMap<>();
      nodePropMap.put(node.id, propMap);
      ColladaExtra extra = node.extra;
      if (extra == null) continue;
      ColladaTechnique technique = extra.technique;
      if (technique == null) continue;
      ColladaUserProps userProperties = technique.user_properties;
      if (userProperties == null) continue;
      String[] rows = userProperties.value.split("\n");
      for (String row : rows) {
        String[] items = row.split("=");
        propMap.put(items[0].trim(), items[1].trim());
      }
    }
    return nodePropMap;
  }

  /**
   * Builds a configured {@link XmlMapper} for parsing the COLLADA XML.
   * 
   * @return The created {@link XmlMapper}.
   */
  protected XmlMapper buildMapper() {
    JacksonXmlModule module = new JacksonXmlModule();
    module.setDefaultUseWrapper(false);
    XmlMapper mapper = new XmlMapper(module);
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    return mapper;
  }

  /**
   * Represents the root node of a COLLADA XML document, into which Jackson deserializes the XML.
   */
  @JacksonXmlRootElement(localName = "COLLADA")
  private static class ColladaXml {
    public ColladaLibVisScenes library_visual_scenes;
  }

  private static class ColladaLibVisScenes {
    @JacksonXmlElementWrapper(localName = "visual_scene", useWrapping = true)
    public List<ColladaNode> visual_scene;
  }

  private static class ColladaNode {
    public String id;
    public ColladaExtra extra;
  }

  private static class ColladaExtra {
    public ColladaTechnique technique;
  }

  private static class ColladaTechnique {
    public ColladaUserProps user_properties;
  }

  private static class ColladaUserProps {
    @JacksonXmlText
    public String value;
  }

}
