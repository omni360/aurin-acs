package au.com.mutopia.acs.conversion.impl;

import au.com.mutopia.acs.conversion.Converter;
import au.com.mutopia.acs.exceptions.ConversionException;
import au.com.mutopia.acs.models.Asset;
import au.com.mutopia.acs.models.Format;
import au.com.mutopia.acs.models.c3ml.C3mlEntity;
import au.com.mutopia.acs.models.c3ml.C3mlEntityType;
import au.com.mutopia.acs.models.c3ml.Vertex3D;
import au.com.mutopia.acs.util.ZipUtils;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import de.micromata.opengis.kml.v_2_2_0.Boundary;
import de.micromata.opengis.kml.v_2_2_0.Coordinate;
import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.ExtendedData;
import de.micromata.opengis.kml.v_2_2_0.Feature;
import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.Geometry;
import de.micromata.opengis.kml.v_2_2_0.GroundOverlay;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.LineString;
import de.micromata.opengis.kml.v_2_2_0.LinearRing;
import de.micromata.opengis.kml.v_2_2_0.Location;
import de.micromata.opengis.kml.v_2_2_0.Model;
import de.micromata.opengis.kml.v_2_2_0.MultiGeometry;
import de.micromata.opengis.kml.v_2_2_0.Orientation;
import de.micromata.opengis.kml.v_2_2_0.Pair;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import de.micromata.opengis.kml.v_2_2_0.Point;
import de.micromata.opengis.kml.v_2_2_0.Polygon;
import de.micromata.opengis.kml.v_2_2_0.Scale;
import de.micromata.opengis.kml.v_2_2_0.SchemaData;
import de.micromata.opengis.kml.v_2_2_0.SimpleData;
import de.micromata.opengis.kml.v_2_2_0.Style;
import de.micromata.opengis.kml.v_2_2_0.StyleMap;
import de.micromata.opengis.kml.v_2_2_0.StyleSelector;
import de.micromata.opengis.kml.v_2_2_0.StyleState;
import lombok.extern.log4j.Log4j;
import org.apache.commons.io.FileUtils;
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Converts KML files into a collection of {@link C3mlEntity} objects.
 */
@Log4j
public class KmlConverter extends AbstractConverter {
  /**
   * Reference to the KML folder path, used to find the path to COLLADA (.dae) file and texture
   * files.
   */
  private String kmlFolderPath;

  /** KML namespace supported by JAK. */
  public static final String KML_DEFAULT_NAMESPACE = "http://www.opengis.net/kml/2.2";

  /**
   * Mapping style names to their color.
   */
  private Map<String, String> mapForStyleColor = new HashMap<>();

  /**
   * Mapping style names to their style maps which contains normal and highlighted colors.
   */
  private Map<String, Map<StyleState, String>> mapForStyleMap = new HashMap<>();

  /**
   * Converts the KML {@link Asset} into a list of {@link C3mlEntity}s.
   *
   * @param asset An {@link Asset} representing a KML file.
   * @param merge Whether to merge all of the content into a single entity.
   * @return A {@link C3mlEntity} containing the same information as the KML.
   */
  public List<C3mlEntity> convert(Asset asset, boolean merge) throws ConversionException {
    try {
      File kmlFile = asset.getTemporaryFile();
      return convert(kmlFile);
    } catch (IOException e) {
      throw new ConversionException("Error reading content from KML file.");
    }
  }

  /**
   * Converts the KML file into a list of {@link C3mlEntity}s.
   *
   * @param kmlFile The KML to be converted.
   * @return A {@link C3mlEntity} containing the same information as the KML.
   * @throws ConversionException if the conversion failed.
   */
  public List<C3mlEntity> convert(File kmlFile) throws ConversionException {
    fixXmlSchema(kmlFile);
    return getEntities(kmlFile);
  }

  /**
   * Converts the KMZ file into a list of {@link C3mlEntity} objects.
   *
   * @param asset An {@link Asset} representing a KMZ file.
   * @return A {@link C3mlEntity} containing the same information as the KMZ file.
   * @throws ConversionException if the conversion failed.
   */
  public List<C3mlEntity> convertKmz(Asset asset) throws ConversionException {
    try {
      // Extract all KML files to be converted. A KMZ file may have multiple KML.
      List<File> kmlFiles = new ArrayList<>();
      File assetTemporaryFile = asset.getTemporaryFile();
      List<File> unzippedFiles = ZipUtils.unzipToTempDirectory(assetTemporaryFile);
      for (File unzippedFile : unzippedFiles) {
        if (Files.getFileExtension(unzippedFile.getName()).equals(Format.KML.toString())) {
          kmlFiles.add(unzippedFile);
        }
      }
      if (kmlFiles.isEmpty()) {
        throw new ConversionException("Failed to find .kml file.");
      }
      List<C3mlEntity> c3mlEntities = new ArrayList<>();
      for (File kml : kmlFiles) {
        c3mlEntities.addAll(convert(kml));
      }
      return c3mlEntities;
    } catch (IOException e) {
      throw new ConversionException("Failed to read converted KMZ file", e);
    }
  }

  /**
   * Extracts and populates a {@link C3mlEntity} for each element in the given KML. If the immediate
   * {@link Feature} of the KML file is a {@link Folder} or {@link Document}, the contained children
   * entities are extracted as the top level hierarchy instead.
   *
   * @param kmlFile The KML to extract from.
   * @return A list of the extracted {@link C3mlEntity} objects.
   */
  private List<C3mlEntity> getEntities(File kmlFile) throws ConversionException {
    kmlFolderPath = kmlFile.getParentFile().getPath();
    Kml kml = Kml.unmarshal(kmlFile);
    generateStyleMaps(kml);
    List<C3mlEntity> c3mlEntities = new ArrayList<>();
    Feature kmlFeature = kml.getFeature();
    if (kmlFeature.getName() == null) {
      if (kmlFeature instanceof Folder) {
        Folder folder = (Folder) kmlFeature;
        List<Feature> features = folder.getFeature();
        if (features.size() == 1) {
          kmlFeature = features.get(0);
        }
      } else if (kmlFeature instanceof Document) {
        Document document = (Document) kmlFeature;
        List<Feature> features = document.getFeature();
        if (features.size() == 1) {
          kmlFeature = features.get(0);
        }
      }
    }
    if (kmlFeature instanceof Folder) {
      Folder folder = (Folder) kmlFeature;
      for (Feature feature : folder.getFeature()) {
        c3mlEntities.add(buildEntity(feature));
      }
    } else if (kmlFeature instanceof Document) {
      Document document = (Document) kmlFeature;
      for (Feature feature : document.getFeature()) {
        c3mlEntities.add(buildEntity(feature));
      }
    } else {
      c3mlEntities.add(buildEntity(kmlFeature));
    }
    return c3mlEntities;
  }

  /**
   * Creates a {@link C3mlEntity} from a KML feature.
   *
   * @param feature The KML feature element.
   * @return The constructed {@link C3mlEntity}.
   */
  private C3mlEntity buildEntity(Feature feature) throws ConversionException {
    if (feature instanceof Folder) {
      return buildEntity((Folder) feature);
    } else if (feature instanceof Document) {
      return buildEntity((Document) feature);
    } else if (feature instanceof Placemark) {
      return buildEntity((Placemark) feature);
    } else if (feature instanceof GroundOverlay) {
      log.debug("Image from ground overlay is not supported yet.");
    }
    return null;
  }

  /**
   * Creates a {@link C3mlEntity} from a KML document.
   *
   * @param document The KML document element.
   * @return The constructed {@link C3mlEntity}.
   */
  private C3mlEntity buildEntity(Document document) throws ConversionException {
    C3mlEntity entity = createEntity(document);
    List<Feature> features = document.getFeature();
    for (Feature feature : features) {
      C3mlEntity child = buildEntity(feature);
      entity.addChild(child);
    }
    return entity;
  }

  /**
   * Creates a {@link C3mlEntity} from a KML folder.
   *
   * @param folder The KML folder element.
   * @return The constructed {@link C3mlEntity}.
   */
  private C3mlEntity buildEntity(Folder folder) throws ConversionException {
    C3mlEntity entity = createEntity(folder);
    List<Feature> features = folder.getFeature();
    for (Feature feature : features) {
      C3mlEntity child = buildEntity(feature);
      entity.addChild(child);
    }
    return entity;
  }

  /**
   * Creates a {@link C3mlEntity} from a KML placemark.
   *
   * @param placemark The KML placemark element.
   * @return The constructed {@link C3mlEntity}.
   */
  private C3mlEntity buildEntity(Placemark placemark) throws ConversionException {
    C3mlEntity entity = createEntity(placemark);
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
  private void writeGeometry(C3mlEntity entity, Geometry geometry) throws ConversionException {
    if (!isMultiGeometryOrModel(geometry)) {
      writeSimpleGeometry(entity, geometry);
    }

    // Polygon includes children and each child should be an entity.
    if (geometry instanceof MultiGeometry) {
      writeMultiGeometry(entity, (MultiGeometry) geometry);
    } else if (geometry instanceof Model) {
      writeModel(entity, (Model) geometry);
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
      // if (!polygon.getInnerBoundaryIs().isEmpty()) {
      // for (int k = 0; k < polygon.getInnerBoundaryIs().size(); k++) {
      // Boundary innerBoundaryIs = polygon.getInnerBoundaryIs().get(k);
      // LinearRing linearRingInner = innerBoundaryIs.getLinearRing();
      // }
      // }
    }
  }

  /**
   * Writes the multi geometric values for the {@link C3mlEntity} object. A multi geometry may
   * contain a hierarchy of other geometries.
   *
   * @param entity The {@link C3mlEntity} object.
   * @param multiGeometry The multi geometry containing hierarchy of geometries.
   */
  private void writeMultiGeometry(C3mlEntity entity, MultiGeometry multiGeometry)
      throws ConversionException {
    for (int j = 0; j < multiGeometry.getGeometry().size(); j++) {
      C3mlEntity child = new C3mlEntity();
      child.setName(entity.getName() + "_child_" + j);
      child.setColor(entity.getColor());
      Geometry geometryFromMulti = multiGeometry.getGeometry().get(j);
      writeGeometry(entity, geometryFromMulti);
    }
  }

  /**
   * Writes the model for the {@link C3mlEntity} object. Objects containing the geometry of the
   * model are added to the {@link C3mlEntity} as children.
   *
   * @param entity The {@link C3mlEntity} object.
   * @param model The model containing complex geometry shapes.
   * @throws ConversionException if the conversion failed.
   */
  private void writeModel(C3mlEntity entity, Model model) throws ConversionException {
    try {
      String daeFilePath = kmlFolderPath + File.separator + model.getLink().getHref();

      File daeFile = new File(daeFilePath);
      ColladaConverter colladaConverter = new ColladaConverter();
      Location modelOrigin = model.getLocation();
      Scale modelScale = model.getScale();
      // KML's positive rotation is in the clockwise direction.
      Orientation modelOrientation = model.getOrientation();

      List<Double> rotation =
          Lists.newArrayList(-1 * modelOrientation.getTilt(), -1 * modelOrientation.getRoll(), -1
              * modelOrientation.getHeading());
      List<Double> scale =
          Lists.newArrayList(modelScale.getX(), modelScale.getY(), modelScale.getZ());
      List<Double> geoLocation =
          Lists.newArrayList(modelOrigin.getLongitude(), modelOrigin.getLatitude(),
              modelOrigin.getAltitude());
      List<C3mlEntity> modelEntities =
          colladaConverter.convert(daeFile, rotation, scale, geoLocation);
      for (C3mlEntity modelEntity : modelEntities) {
        entity.addChild(modelEntity);
      }
    } catch (ConversionException e) {
      throw new ConversionException("Error reading model from Collada file.", e);
    }
  }

  /**
   * @return The list of {@link Vertex3D} points from a list of coordinates.
   */
  private List<Vertex3D> getVertex3DPointsFromCoordinates(List<Coordinate> coordinates) {
    List<Vertex3D> points = Lists.newArrayList();
    for (Coordinate coord : coordinates) {
      points.add(new Vertex3D(coord.getLatitude(), coord.getLongitude(), coord.getAltitude()));
    }
    return points;
  }

  /**
   * @return True if the geometry is an instance of {@link MultiGeometry} or {@link Model}.
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
   * 
   * @param kml The KML document to generate style maps for.
   */
  public void generateStyleMaps(Kml kml) {
    mapForStyleColor = Maps.newHashMap();
    mapForStyleMap = Maps.newHashMap();
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
      if (!(selector instanceof StyleMap)) continue;

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
    Map<StyleState, String> map = mapForStyleMap.get(styleId);
    return map != null ? map.get(StyleState.NORMAL) : mapForStyleColor.get(styleId);
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
   * Creates a {@link C3mlEntity} from a KML Feature, populated with name, description and
   * parameters.
   *
   * @param feature The KML feature element.
   * @return The created {@link C3mlEntity}.
   */
  private C3mlEntity createEntity(Feature feature) {
    C3mlEntity entity = new C3mlEntity();
    entity.setName(feature.getName());
    populateParameters(entity, feature.getExtendedData());
    String description = feature.getDescription();
    if (!Strings.isNullOrEmpty(description)) {
      entity.addParameter("description", description);
    }
    return entity;
  }

  /**
   * Populates entity with parameters from KML's <code>&lt;ExtendedData&gt;</code> tag.
   *
   * @param entity The {@link C3mlEntity} object.
   * @param extendedData The KML extended data element, mapping parameter names and values.
   * 
   * @see <a href="https://developers.google.com/kml/documentation/extendeddata">KML docs for
   *      &lt;ExtendedData&gt;</a>
   */
  private void populateParameters(C3mlEntity entity, ExtendedData extendedData) {
    if (extendedData == null) return;
    for (SchemaData schemaData : extendedData.getSchemaData()) {
      for (SimpleData simpleData : schemaData.getSimpleData()) {
        entity.addParameter(simpleData.getName(), simpleData.getValue());
      }
    }
  }

  /**
   * Fix the XML schema of the KML file so that JAK can read the KML file without errors. The
   * namespace is rename to one supported by JAK. The element names of nodes are modified to reflect
   * the new XML namespace.
   * 
   * @param kmlFile The file to fix the schema of.
   */
  private void fixXmlSchema(File kmlFile) {
    File tmpKmlFile = new File("tmp.kml");

    try {
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      dbf.setNamespaceAware(true);
      dbf.setValidating(false);
      DocumentBuilder db = dbf.newDocumentBuilder();
      org.w3c.dom.Document doc = db.parse(new FileInputStream(kmlFile));
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

      FileUtils.copyFile(tmpKmlFile, kmlFile);
    } catch (ParserConfigurationException | SAXException | TransformerException | IOException e) {
      log.error(e);
    }

    // Once everything is complete, delete temp file.
    tmpKmlFile.delete();
  }

}
