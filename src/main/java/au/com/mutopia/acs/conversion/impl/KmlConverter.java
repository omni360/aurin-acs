package au.com.mutopia.acs.conversion.impl;

import au.com.mutopia.acs.conversion.Converter;
import au.com.mutopia.acs.exceptions.ConversionException;
import au.com.mutopia.acs.models.Asset;
import au.com.mutopia.acs.models.c3ml.C3mlEntity;
import au.com.mutopia.acs.models.c3ml.C3mlEntityType;
import au.com.mutopia.acs.models.c3ml.Vertex3D;
import au.com.mutopia.acs.util.FileUtils;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import de.micromata.opengis.kml.v_2_2_0.Boundary;
import de.micromata.opengis.kml.v_2_2_0.Coordinate;
import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.Feature;
import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.Geometry;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.LineString;
import de.micromata.opengis.kml.v_2_2_0.LinearRing;
import de.micromata.opengis.kml.v_2_2_0.Model;
import de.micromata.opengis.kml.v_2_2_0.MultiGeometry;
import de.micromata.opengis.kml.v_2_2_0.Pair;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import de.micromata.opengis.kml.v_2_2_0.Point;
import de.micromata.opengis.kml.v_2_2_0.Polygon;
import de.micromata.opengis.kml.v_2_2_0.Style;
import de.micromata.opengis.kml.v_2_2_0.StyleMap;
import de.micromata.opengis.kml.v_2_2_0.StyleSelector;
import de.micromata.opengis.kml.v_2_2_0.StyleState;
import lombok.extern.log4j.Log4j;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Log4j
public class KmlConverter implements Converter {
  /**
   * Default white color if material color is absent.
   */
  protected static final Color DEFAULT_COLOR = Color.WHITE;

  /** KML namespace supported by JAK. */
  public static final String KML_DEFAULT_NAMESPACE = "http://www.opengis.net/kml/2.2";

  /**
   * Mapping style names to their color.
   */
  private Map<String, String> mapForStyleColor = Maps.newHashMap();

  /**
   * Mapping style names to their style maps which contains normal and highlighted colors.
   */
  private Map<String, Map<StyleState, String>> mapForStyleMap = Maps.newHashMap();

  /**
   * Converts the zipped Shapefile {@link Asset} into a {@link C3mlEntity}.
   *
   * @param asset An {@link Asset} representing a zipped bundle of Shapefile files.
   * @return A {@link C3mlEntity} containing the same information as the Shapefile.
   */
  public List<C3mlEntity> convert(Asset asset) throws ConversionException {
    try {
      File kmlFile = FileUtils.createTemporaryFileWithContent(asset.getData());
      fixXmlSchema(kmlFile);
      return getEntities(kmlFile);
    } catch (IOException e) {
      throw new ConversionException("Error reading content from KML file.");
    }
  }

  /**
   * Extracts and populates a {@link C3mlEntity} for each element in the given KML. If the
   * immediate {@link Feature} of the KML file is a {@link Folder} or {@link Document},
   * the contained children entities are extracted as the top level hierarchy instead.
   *
   * @param kmlFile The KML to extract from.
   * @return A list of the extracted {@link C3mlEntity} objects.
   */
  private List<C3mlEntity> getEntities(File kmlFile) {
    Kml kml = Kml.unmarshal(kmlFile);
    generateStyleMaps(kml);
    List<C3mlEntity> c3mlEntities = new ArrayList<>();
    Feature kmlFeature = kml.getFeature();
    if (kmlFeature instanceof Folder) {
      Folder folder = (Folder) kmlFeature;
      for (Feature feature : folder.getFeature()) {
        c3mlEntities.add(getGeoObjectFromFeature(feature));
      }
    } else if (kmlFeature instanceof Document) {
      Document document = (Document) kmlFeature;
      for (Feature feature : document.getFeature()) {
        c3mlEntities.add(getGeoObjectFromFeature(feature));
      }
    } else {
      c3mlEntities.add(getGeoObjectFromFeature(kmlFeature));
    }
    return c3mlEntities;
  }

  /**
   * Creates a {@link C3mlEntity} from a KML feature.
   *
   * @param feature The KML abstract element.
   * @return The constructed {@link C3mlEntity}.
   */
  private C3mlEntity getGeoObjectFromFeature(Feature feature) {
    if (feature instanceof Folder) {
      return getGeoObjectFromFolder((Folder) feature);
    } else if (feature instanceof Document) {
      return getEntityFromDocument((Document) feature);
    } else if (feature instanceof Placemark) {
      return getGeoObjectFromPlacemark((Placemark) feature);
    }
    return null;
  }

  /**
   * Creates a {@link C3mlEntity} from a KML document.
   *
   * @param document The KML document element.
   * @return The constructed {@link C3mlEntity}.
   */
  private C3mlEntity getEntityFromDocument(Document document) {
    C3mlEntity entity = new C3mlEntity();
    entity.setName(document.getName());
    List<Feature> features = document.getFeature();
    for (Feature feature : features) {
      C3mlEntity childGeoObject = getGeoObjectFromFeature(feature);
      if (childGeoObject != null) {
        entity.addChild(childGeoObject);
      }
    }
    return entity;
  }

  /**
   * Creates a {@link C3mlEntity} from a KML folder.
   *
   * @param folder The KML folder element.
   * @return The constructed {@link C3mlEntity}.
   */
  private C3mlEntity getGeoObjectFromFolder(Folder folder) {
    C3mlEntity entity = new C3mlEntity();
    entity.setName(folder.getName());
    List<Feature> features = folder.getFeature();
    for (Feature feature : features) {
      C3mlEntity childGeoObject = getGeoObjectFromFeature(feature);
      if (childGeoObject != null) {
        entity.addChild(childGeoObject);
      }
    }
    return entity;
  }

  /**
   * Creates a {@link C3mlEntity} from a KML placemark.
   *
   * @param placemark The KML placemark element.
   * @return The constructed {@link C3mlEntity}.
   */
  private C3mlEntity getGeoObjectFromPlacemark(Placemark placemark) {
    C3mlEntity entity = new C3mlEntity();
    entity.setName(placemark.getName());
    String description = placemark.getDescription();
    if (description != null) {
      entity.addParameter("description", description);
    }
    Color color = getColor(placemark);
    entity.setColorData(color);
    writeGeometry(entity, placemark.getGeometry());
    return entity;
  }

  /**
   * Writes the geometric values for the {@link C3mlEntity} object if the geometry is not empty.
   *
   * @param entity The {@link C3mlEntity} object.
   * @param geometry The geometry embedded in the KML element.
   */
  private void writeGeometry(C3mlEntity entity, Geometry geometry) {
    if (!isMultiGeometryOrModel(geometry)) {
      writeSimpleGeometry(entity, geometry);
    }

    //Polygon includes children and each child should be GeoObject
    if (geometry instanceof MultiGeometry) {
      writeMultiGeometry(entity, (MultiGeometry) geometry);
    } else if (geometry instanceof Model) {
      //      return getGeoObjectFromModel((Model) geometry, placemark);
    }
  }

  /**
   * Writes the simple geometric values for the {@link C3mlEntity} object. Simple geometries are
   * {@link Point}, {@link LineString}, {@link LinearRing} and {@link Polygon}.
   *
   * @param entity The {@link C3mlEntity} object.
   * @param geometry The geometry embedded in the KML element.
   */
  private void writeSimpleGeometry(C3mlEntity entity, Geometry geometry) {
    if (geometry instanceof Point) {
      Point point = (Point) geometry;
      entity.setCoordinates(getVertex3DPointsFromCoordinates(point.getCoordinates()));
      entity.setType(C3mlEntityType.POINT);
    } else if (geometry instanceof LineString) {
      LineString lineString = (LineString) geometry;
      entity.setCoordinates(getVertex3DPointsFromCoordinates(lineString.getCoordinates()));
      entity.setType(C3mlEntityType.LINE);
    } else if (geometry instanceof LinearRing) {
      LinearRing linearRing = (LinearRing) geometry;
      entity.setCoordinates(getVertex3DPointsFromCoordinates(linearRing.getCoordinates()));
      entity.setType(C3mlEntityType.LINE);
    } else if (geometry instanceof Polygon) {
      Polygon polygon = (Polygon) geometry;
      Boundary outerBoundaryIs = polygon.getOuterBoundaryIs();
      LinearRing linearRing = outerBoundaryIs.getLinearRing();
      entity.setCoordinates(getVertex3DPointsFromCoordinates(linearRing.getCoordinates()));
      entity.setType(C3mlEntityType.POLYGON);

      // Polygon holes
      // TODO(Brandon) add support for polygon with holes.
      if (!polygon.getInnerBoundaryIs().isEmpty()) {
        for (int k = 0; k < polygon.getInnerBoundaryIs().size(); k++) {
          Boundary innerBoundaryIs = polygon.getInnerBoundaryIs().get(k);
          LinearRing linearRingInner = innerBoundaryIs.getLinearRing();
        }
      }
    }
  }

  /**
   * Writes the multi geometric values for the {@link C3mlEntity} object. A multi geometry may
   * contain a hierarchy of other geometries.
   *
   * @param entity The {@link C3mlEntity} object.
   * @param multiGeometry The multi geometry containing hierarchy of geometries.
   * @return The constructed {@link C3mlEntity} representing the top level geometry in the hierarchy
   * extracted from a multi geometry.
   */
  private void writeMultiGeometry(C3mlEntity entity, MultiGeometry multiGeometry) {
    for (int j = 0; j < multiGeometry.getGeometry().size(); j++) {
      C3mlEntity childEntity = new C3mlEntity();
      childEntity.setName(entity.getName() + "_child_" + j);
      childEntity.setColor(entity.getColor());
      Geometry geometryFromMulti = multiGeometry.getGeometry().get(j);
      writeGeometry(entity, geometryFromMulti);
    }
  }

  /**
   * @return The list of {@link Vertex3D} points from a list of coordinates.
   */
  private List<Vertex3D> getVertex3DPointsFromCoordinates(List<Coordinate> coordinates) {
    List<Vertex3D> points = Lists.newArrayList();
    for (Coordinate coordinate : coordinates) {
      points.add(new Vertex3D(coordinate.getLatitude(), coordinate.getLongitude(),
          coordinate.getAltitude()));
    }
    return points;
  }

  /**
   * @return True if the geometry is an instance of Multi-Geometry or Model.
   */
  private Boolean isMultiGeometryOrModel(Geometry geometry) {
    return (geometry instanceof MultiGeometry || geometry instanceof Model);
  }

  /**
   * Extracts the color from {@link Style}s referenced by the {@link Placemark}. If not color is
   * present default to White.
   *
   * @param placemark The KML placemark element.
   * @return The Color extracted from KML placemark.
   */
  private Color getColor(Placemark placemark) {
    Color color = null;
    String colorString;
    List<StyleSelector> styleSelector = placemark.getStyleSelector();
    Iterator<StyleSelector> iterator = styleSelector.iterator();
    Style style = findStyleInStyleSelectors(iterator);
    if (style != null) {
      colorString = style.getPolyStyle().getColor();
      color = convertStringToColor(colorString);
    } else {
      String styleUrl = placemark.getStyleUrl();
      if (styleUrl != null) {
        if (styleUrl.startsWith("#")) {
          styleUrl = styleUrl.replace("#", "");
        }
        colorString = getNormalColor(styleUrl);
        color = convertStringToColor(colorString);
      }
    }
    if (color == null) {
      color = DEFAULT_COLOR;
    }
    return color;
  }

  /**
   * Generate style maps from KML file for CZML conversion.
   */
  public void generateStyleMaps(Kml kml) {
    // Check if feature is a document.
    // Style maps are absent if feature is not a document.
    if (kml.getFeature() instanceof Document) {
      Document document = (Document) kml.getFeature();
      List<StyleSelector> styleSelector = document.getStyleSelector();
      extractStyleColors(styleSelector.iterator());
      extractStyleMaps(styleSelector.iterator());
    }
  }

  /**
   * Extracts the style maps and maps the different style states (normal, highlighted) to colors.
   */
  private void extractStyleMaps(Iterator<StyleSelector> iterator) {
    while (iterator.hasNext()) {
      StyleSelector selector = iterator.next();
      if (selector instanceof StyleMap) {
        List<Pair> pairs = ((StyleMap) selector).getPair();

        for (Pair pair : pairs) {
          StyleState key = pair.getKey();
          String styleUrl = pair.getStyleUrl();
          if (styleUrl.startsWith("#")) {
            styleUrl = styleUrl.replace("#", "");
          }

          String color = mapForStyleColor.get(styleUrl);
          Map<StyleState, String> newMap = Maps.newHashMap();
          newMap.put(key, color);

          if (mapForStyleMap.get(selector.getId()) == null) {
            mapForStyleMap.put(selector.getId(), newMap);
          } else {
            mapForStyleMap.get(selector.getId()).put(key, color);
          }
        }
      }
    }
  }

  /**
   * Extracts the style colors and maps the style IDs to colors.
   */
  private void extractStyleColors(Iterator<StyleSelector> iterator) {
    while (iterator.hasNext()) {
      StyleSelector selector = iterator.next();
      if (selector instanceof Style) {
        Style style = ((Style) selector);
        String styleId = style.getId();
        // Find where the color is located.
        if (style.getPolyStyle() != null) {
          mapForStyleColor.put(styleId, style.getPolyStyle().getColor());
        } else if (style.getLabelStyle() != null) {
          mapForStyleColor.put(styleId, style.getLabelStyle().getColor());
        } else if (style.getLineStyle() != null) {
          mapForStyleColor.put(styleId, style.getLineStyle().getColor());
        } else if (style.getListStyle() != null) {
          mapForStyleColor.put(styleId, style.getListStyle().getBgColor());
        } else if (style.getBalloonStyle() != null) {
          mapForStyleColor.put(styleId, style.getBalloonStyle().getColor());
        } else if (style.getIconStyle() != null) {
          mapForStyleColor.put(styleId, style.getIconStyle().getColor());
        }
      }
    }
  }

  /**
   * Gets the normal color from the style, given style ID.
   *
   * @param styleId The style ID.
   * @return The normal color for the style.
   */
  public String getNormalColor(String styleId) {
    if (mapForStyleMap.get(styleId) != null) {
      return mapForStyleMap.get(styleId).get(StyleState.NORMAL);
    } else {
      return mapForStyleColor.get(styleId);
    }
  }

  /**
   * @param colorString The hex string representing the Color.
   * @return {@link Color} decoded from Color hex string.
   */
  private Color convertStringToColor(String colorString) {
    // Order of color: AABBGGRR.
    if (Strings.isNullOrEmpty(colorString)) {
      return DEFAULT_COLOR;
    }

    String a = "0x" + colorString.substring(0, 2);
    String b = "0x" + colorString.substring(2, 4);
    String g = "0x" + colorString.substring(4, 6);
    String r = "0x" + colorString.substring(6, 8);
    return new Color(Integer.decode(r), Integer.decode(g), Integer.decode(b), Integer.decode(a));
  }

  /**
   * @param iterator The {@link StyleSelector} iterator.
   * @return The instance of {@link StyleSelector} which is of type {@link Style};
   */
  private Style findStyleInStyleSelectors(Iterator<StyleSelector> iterator) {
    while (iterator.hasNext()) {
      StyleSelector tmp = iterator.next();
      if (tmp instanceof Style) {
        return (Style) tmp;
      }
    }
    return null;
  }

  /**
   * Fix the XML schema of the KML file so that JAK can read the KML file without errors. The
   * namespace is rename to one supported by JAK. The element names of nodes are modified to
   * reflect the new XML namespace.
   * @param kmlFile
   */
  private void fixXmlSchema(File kmlFile) {
    File tmpKmlFile = new File("tmp.kml");

    try {
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      dbf.setNamespaceAware(true);
      dbf.setValidating(false);
      DocumentBuilder db = dbf.newDocumentBuilder();
      org.w3c.dom.Document doc = db.parse(new FileInputStream(kmlFile));
      Node root = doc.getDocumentElement();
      Element originalDocumentElement = (Element) doc.getElementsByTagName("kml").item(0);
      if (originalDocumentElement == null) {
        return;
      }

      // Rename the KML namespace to one supported by JAK.
      Element newDocumentElement =
          doc.createElementNS(KML_DEFAULT_NAMESPACE, originalDocumentElement.getNodeName());
      // Copy all children
      NodeList list = originalDocumentElement.getChildNodes();
      while (list.getLength() != 0) {
        newDocumentElement.appendChild(list.item(0));
      }
      // Replace the original element
      doc.replaceChild(newDocumentElement, originalDocumentElement);

      // Find all KML's extended data, and rename all element tags.
      NodeList schemas = doc.getElementsByTagName("Schema");
      for (int i = 0; i < schemas.getLength(); i++) {
        Node item = schemas.item(i);
        Node parent = item.getAttributes().getNamedItem("parent");
        Node name = item.getAttributes().getNamedItem("name");
        if (parent != null && name != null) {
          String elementTag = name.getNodeValue();
          NodeList elementsByTagName = doc.getElementsByTagName(elementTag);
          for (int j = 0; j < elementsByTagName.getLength(); j++) {
            Node extendedElement = elementsByTagName.item(j);
            doc.renameNode(extendedElement, KML_DEFAULT_NAMESPACE, parent.getNodeValue());
          }
        }
      }

      TransformerFactory transformerFactory = TransformerFactory.newInstance();
      Transformer transformer = transformerFactory.newTransformer();
      DOMSource source = new DOMSource(doc);
      StreamResult result = new StreamResult(tmpKmlFile);

      transformer.transform(source, result);

      org.apache.commons.io.FileUtils.copyFile(tmpKmlFile, kmlFile);
    } catch (ParserConfigurationException | SAXException | TransformerException | IOException e) {
      log.error(e);
    }

    // Once everything is complete, delete temp file..
    tmpKmlFile.delete();
  }
}
