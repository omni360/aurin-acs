package au.com.mutopia.acs.conversion.output;

import au.com.mutopia.acs.models.Format;
import com.google.common.base.Strings;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.extern.log4j.Log4j;

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
@Log4j
@Getter
public class KmlBuilder {

  /** Base model for KML file. */
  private Kml kml;

  /** The highest level folder that contains styles shared between placemarks. */
  private Folder topLevelFolder;

  /** The list of style IDs created. */
  private List<String> styleIds;

  public KmlBuilder() {
    this.kml = new Kml();
    this.topLevelFolder = kml.createAndSetFolder().withName("entities");
    this.styleIds = new ArrayList<>();
  }

  public KmlBuilder(Kml kml, Folder topLevelFolder, List<String> styleIds) {
    this.kml = kml;
    this.topLevelFolder = topLevelFolder;
    this.styleIds = styleIds;
  }

  /**
   * Writes the entity using a writer based on the entity's type.
   *
   * @param entity The entity to write.
   * @param parentFolder The parent folder to write to.
   * @return The feature that was written.
   */
  public Feature writeEntity(C3mlEntity entity, Folder parentFolder) {
    switch (entity.getType()) {
      case POINT:
        return writePoint(entity, parentFolder);
      case LINE:
        return writeLine(entity, parentFolder);
      case POLYGON:
        return writePolygon(entity, parentFolder);
      case COLLECTION:
      case FEATURE:
        return writeFolder(entity, parentFolder);
      default:
        log.warn("Unknown entity type: " + entity.getType());
        return null;
    }
  }

  /**
   * Writes the point {@link C3mlEntity} as a {@link Placemark} within the {@link Folder}.
   */
  public Placemark writePoint(C3mlEntity entity, Folder parentFolder) {
    Placemark placemark = createPlacemark(entity, parentFolder);

    Point point = placemark.createAndSetPoint();
    point.setAltitudeMode(AltitudeMode.RELATIVE_TO_GROUND);
    Vertex3D coord = entity.getCoordinates().get(0);
    point.addToCoordinates(coord.getLongitude(), coord.getLatitude(), coord.getAltitude());

    String kmlColorString = getKmlColorString(entity.getColor());
    String pointStyleId = kmlColorString + "Point";
    if (!styleIds.contains(pointStyleId)) {
      topLevelFolder.createAndAddStyle().withId(pointStyleId).createAndSetIconStyle()
          .withColor(kmlColorString);
      styleIds.add(pointStyleId);
    }
    placemark.withStyleUrl("#" + pointStyleId);
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
    Placemark placemark = createPlacemark(entity, parentFolder);

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
    Placemark placemark = createPlacemark(entity, parentFolder);

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
   * Creates a COLLADA model for the {@link C3mlEntity} hierarchy and references it by the model's
   * id.
   *
   * @param entity {@link C3mlEntity}
   */
  public Placemark writeModel(C3mlEntity entity, Folder parentFolder) throws IOException {
    Placemark placemark = parentFolder.createAndAddPlacemark();
    setCommon(entity, placemark);

//    placemark.setName(entity.getName());
//    Model model = placemark.createAndSetModel();
//    model.withAltitudeMode(AltitudeMode.RELATIVE_TO_GROUND);
//
//    Vertex3D location = new Vertex3D(entity.getGeoLocation(), true);
//    Vertex3D rotation = new Vertex3D(entity.getRotation(), false);
//    Vertex3D scale = new Vertex3D(entity.getScale(), false);
//
//    model.createAndSetLocation().withLongitude(location.getLongitude())
//        .withLatitude(location.getLatitude()).withAltitude(location.getAltitude());
//
//    model.createAndSetOrientation().withHeading(rotation.getX()).withTilt(rotation.getY())
//        .withRoll(rotation.getZ());
//
//    model.createAndSetScale().withX(scale.getX()).withY(scale.getY()).withZ(scale.getZ());
//
//    String colladaModelHref = getUniqueFileHref(entity.getId(), Format.COLLADA.toString());
//    model.createAndSetLink().withHref(colladaModelHref);

    return placemark;
  }

  /**
   * Creates a {@link Placemark} for the {@link C3mlEntity}.
   *
   * @param entity {@link C3mlEntity}
   * @param parentFolder The parent folder to write to.
   */
  public Placemark createPlacemark(C3mlEntity entity, Folder parentFolder) {
    Placemark placemark = parentFolder.createAndAddPlacemark();
    setCommon(entity, placemark);
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

  /**
   * Marshalls the KML document into a string without animations.
   */
  public String marshall() {
    StringWriter stringWriter = new StringWriter();
    kml.marshal(stringWriter);
    return stringWriter.toString().replaceAll(
        "[\\s]*<refreshInterval>[\\S]{3}</refreshInterval>"
            + "[\\s]*<viewRefreshTime>[\\S]{3}</viewRefreshTime>"
            + "[\\s]*<viewBoundScale>[\\S]{3}</viewBoundScale>", "");
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
    feature.setName(Strings.isNullOrEmpty(entity.getName()) ? entity.getId() : entity.getName());
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
