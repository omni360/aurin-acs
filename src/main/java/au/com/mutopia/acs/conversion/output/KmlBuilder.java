package au.com.mutopia.acs.conversion.output;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.fest.util.Strings;

import lombok.Getter;
import au.com.mutopia.acs.models.c3ml.C3mlEntity;
import au.com.mutopia.acs.models.c3ml.Vertex3D;
import de.micromata.opengis.kml.v_2_2_0.AltitudeMode;
import de.micromata.opengis.kml.v_2_2_0.Boundary;
import de.micromata.opengis.kml.v_2_2_0.ColorMode;
import de.micromata.opengis.kml.v_2_2_0.ExtendedData;
import de.micromata.opengis.kml.v_2_2_0.Feature;
import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.LineString;
import de.micromata.opengis.kml.v_2_2_0.LinearRing;
import de.micromata.opengis.kml.v_2_2_0.Model;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import de.micromata.opengis.kml.v_2_2_0.Point;
import de.micromata.opengis.kml.v_2_2_0.Polygon;

/**
 * Constructs KML objects. Holds state about the current KML document, so create a new
 * {@link KmlBuilder} for each batch of entities.
 */
@Getter
public class KmlBuilder {

  /**
   * Base model for KML file.
   */
  private Kml kml;

  /**
   * The highest level folder that contains styles shared between placemarks.
   */
  private Folder topLevelFolder;

  /**
   * The list of style IDs created.
   */
  private List<String> styleIds = new ArrayList<>();

  public KmlBuilder() {
    this.kml = new Kml();
    this.topLevelFolder = kml.createAndSetFolder().withName("entities");
  }

  /**
   * Writes the point {@link C3mlEntity} as a {@link Placemark} within the {@link Folder}.
   */
  public Placemark writePoint(C3mlEntity entity, Folder parentFolder) {
    Placemark placemark = parentFolder.createAndAddPlacemark();
    setCommon(entity, placemark);

    Point point = placemark.createAndSetPoint();
    point.setAltitudeMode(AltitudeMode.RELATIVE_TO_GROUND);
    Vertex3D coord = entity.getCoordinates().get(0);
    point.addToCoordinates(coord.getLongitude(), coord.getLatitude(), coord.getAltitude());
    return placemark;
  }

  public Placemark writePoint(C3mlEntity entity) {
    return writePoint(entity, topLevelFolder);
  }

  /**
   * Writes the line {@link C3mlEntity} as a {@link Placemark} within the {@link Folder}.
   *
   * @param entity
   * @param parentFolder
   */
  public Placemark writeLine(C3mlEntity entity, Folder parentFolder) {
    Placemark placemark = parentFolder.createAndAddPlacemark();
    setCommon(entity, placemark);

    LineString lineString = placemark.createAndSetLineString();
    lineString.setExtrude(false);
    lineString.setAltitudeMode(AltitudeMode.RELATIVE_TO_GROUND);

    for (Vertex3D point : entity.getCoordinates()) {
      lineString.addToCoordinates(point.getLongitude(), point.getLatitude(), point.getAltitude());
    }

    String kmlColorString = getKmlColorString(entity.getColor());
    String lineStyleId = kmlColorString + "Line";
    if (!styleIds.contains(lineStyleId)) {
      topLevelFolder.createAndAddStyle().withId(lineStyleId).createAndSetLineStyle()
          .withColor(kmlColorString).withWidth(1.0);
      styleIds.add(lineStyleId);
    }
    placemark.withStyleUrl("#" + lineStyleId);
    return placemark;
  }

  public Placemark writeLine(C3mlEntity entity) {
    return writeLine(entity, topLevelFolder);
  }

  /**
   * Writes the {@link C3mlEntity} as a {@link Placemark} within the {@link Folder}.
   */
  public Placemark writePolygon(C3mlEntity entity, Folder parentFolder) {
    Placemark placemark = parentFolder.createAndAddPlacemark();
    setCommon(entity, placemark);

    Polygon polygon = placemark.createAndSetPolygon();
    polygon.setExtrude(entity.getHeight() > 0);
    polygon.setAltitudeMode(AltitudeMode.RELATIVE_TO_GROUND);
    double extrudedHeight = entity.getHeight() + entity.getAltitude();

    // Draw the polygon.
    Boundary outerBoundary = polygon.createAndSetOuterBoundaryIs();
    LinearRing linearRing = outerBoundary.createAndSetLinearRing();
    List<Vertex3D> points = entity.getCoordinates();
    for (Vertex3D point : points) {
      linearRing.addToCoordinates(point.getLongitude(), point.getLatitude(), extrudedHeight);
    }

    // Draw the holes.
    for (List<Vertex3D> hole : entity.getHoles()) {
      Boundary innerBoundary = polygon.createAndAddInnerBoundaryIs();
      LinearRing innerLinearRing = innerBoundary.createAndSetLinearRing();
      for (Vertex3D point : hole) {
        innerLinearRing.addToCoordinates(point.getLongitude(), point.getLatitude(), extrudedHeight);
      }
    }

    // Set the style.
    String kmlColorString = getKmlColorString(entity.getColor());
    String polygonStyleId = kmlColorString + "Polygon";
    if (!styleIds.contains(polygonStyleId)) {
      topLevelFolder.createAndAddStyle().withId(polygonStyleId).createAndSetPolyStyle()
          .withColorMode(ColorMode.NORMAL).withColor(kmlColorString).setFill(true);
      styleIds.add(polygonStyleId);
    }
    placemark.withStyleUrl("#" + polygonStyleId);
    return placemark;
  }

  public Placemark writePolygon(C3mlEntity entity) {
    return writePolygon(entity, topLevelFolder);
  }

  public Folder writeFolder(C3mlEntity entity, Folder parentFolder) {
    Folder folder = parentFolder.createAndAddFolder();
    setCommon(entity, folder);
    return folder;
  }

  public Folder writeFolder(C3mlEntity entity) {
    return writeFolder(entity, topLevelFolder);
  }

  /**
   * Creates a COLLADA model for the {@link GeoObject} hierarchy and references it by the model's
   * id.
   *
   * @param geoObject {@link GeoObject}
   */
  public Placemark writeModel(C3mlEntity entity, Folder parentFolder) throws IOException {
    Placemark placemark = parentFolder.createAndAddPlacemark();
    placemark.setName(entity.getName());
    Model model = placemark.createAndSetModel();
    model.withAltitudeMode(AltitudeMode.RELATIVE_TO_GROUND);

    // Vertex3D geoLocation = geoObject.getHierarchyGeoOrigin();
    // Vertex3D rotation = geoObject.getRotation();
    // Vertex3D scale = geoObject.getScale();
    // model.createAndSetLocation().withLongitude(kmzGeoLocation.getLongitude())
    // .withLatitude(kmzGeoLocation.getLatitude()).withAltitude(kmzGeoLocation.getAltitude());
    //
    // model.createAndSetOrientation().withHeading(rotation.getX()).withTilt(rotation.getY())
    // .withRoll(rotation.getZ());
    //
    // model.createAndSetScale().withX(scale.getX()).withY(scale.getY()).withZ(scale.getZ());
    //
    // String colladaModelHref = geoObject.getId() + ".dae";
    // model.createAndSetLink().withHref(colladaModelHref);
    //
    // // Collapse hierarchy by removing duplicated entities in KML and COLLADA.
    // if (geoObject.getChildren().size() == 1) {
    // geoObject = (GeoObject) geoObject.getChildren().get(0);
    // }
    // createTempColladaModelFile(colladaModelHref, geoObject);
    return placemark;
  }

  /**
   * Creates a {@link Model} placemark in the top level folder of the KML document referencing the
   * COLLADA file.
   * 
   * @param path The relative path to the COLLADA file with the 3D model data.
   * @return The created placemark.
   */
  public Placemark writeAggregateModel(String path, Vertex3D location) {
    Placemark placemark = topLevelFolder.createAndAddPlacemark();
    placemark.setName("models");
    Model model = placemark.createAndSetModel();
    model.withAltitudeMode(AltitudeMode.RELATIVE_TO_GROUND);
    model.createAndSetLocation().withLatitude(location.getLatitude())
        .withLongitude(location.getLongitude()).withAltitude(location.getAltitude());
    model.createAndSetLink().withHref(path);
    return placemark;
  }

  // /**
  // * Checks if the {@link GeoObject} hierarchy contains meshes only.
  // *
  // * @param geoObject
  // * @return True if the {@link GeoObject} hierarchy contains meshes only.
  // */
  // private boolean containsOnlyMesh(GeoObject geoObject) {
  // GeoLeaf geometry = geoObject.getGeometry();
  // if (geometry != null && !(geometry instanceof GeoMesh)) {
  // return false;
  // }
  // for (GeoComposite child : geoObject.getChildrenInHierarchy()) {
  // if (!(child instanceof GeoObject)) {
  // continue;
  // }
  // GeoObject childObject = (GeoObject) child;
  // GeoLeaf childGeometry = childObject.getGeometry();
  // if (childGeometry != null && !(childGeometry instanceof GeoMesh)) {
  // return false;
  // }
  // }
  // return true;
  // }

  /**
   * Sets {@link C3mlEntity} data common to all {@link Placemark}s on the given placemark.
   * 
   * @param entity The {@link C3mlEntity} to set data from.
   * @param feature The {@link Feature} to set data to.
   */
  private void setCommon(C3mlEntity entity, Feature feature) {
    feature.setId(entity.getId());
    feature.setName(Strings.isEmpty(entity.getName()) ? entity.getId() : entity.getName());
    feature.setVisibility(entity.isShow());
    if (entity.getProperties() != null && !entity.getProperties().isEmpty()) {
      ExtendedData xdata = feature.createAndSetExtendedData();
      for (String name : entity.getProperties().keySet()) {
        xdata.createAndAddData(entity.getProperties().get(name)).setName(name);
      }
    }
  }

  /**
   * @param color
   * @return The KML coded color string.
   */
  private String getKmlColorString(List<Integer> color) {
    // Order of color: AABBGGRR.
    return String
        .format("%02x%02x%02x%02x", color.get(3), color.get(2), color.get(1), color.get(0));
  }

}
