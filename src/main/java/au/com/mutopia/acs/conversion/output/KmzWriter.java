package au.com.mutopia.acs.conversion.output;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.log4j.Log4j;
import au.com.mutopia.acs.exceptions.ConversionException;
import au.com.mutopia.acs.models.c3ml.C3mlData;
import au.com.mutopia.acs.models.c3ml.C3mlEntity;
import au.com.mutopia.acs.models.c3ml.C3mlEntityType;
import au.com.mutopia.acs.models.c3ml.Vertex3D;
import au.com.mutopia.acs.util.FileUtils;
import au.com.mutopia.acs.util.ZipUtils;

import com.google.common.collect.Lists;

import de.micromata.opengis.kml.v_2_2_0.AltitudeMode;
import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.Model;
import de.micromata.opengis.kml.v_2_2_0.Placemark;

/**
 * Writes {@link C3mlData} to KMZ.
 */
@Log4j
public class KmzWriter {

  /**
   * Base model for KML file.
   */
  private Kml kml;

  private KmlBuilder kmlBuilder;

  /**
   * The highest level folder that contains styles shared between placemarks.
   */
  private Folder topLevelFolder;

  /**
   * The list of style IDs created.
   */
  private List<String> styleIds;

  /**
   * The list of files (models or icons) being referenced by KML.
   */
  private List<File> referencedFiles = new ArrayList<>();

  /**
   * Map of entity IDs to objects to reconstruct the hierarchy.
   */
  private Map<String, C3mlEntity> entityIdMap;

  /**
   * Starts the KML document, create a KML folder within the document.
   */
  public KmzWriter() {
    kml = new Kml();
    topLevelFolder = kml.createAndSetFolder().withName("entities");
    styleIds = new ArrayList<>();
    kmlBuilder = new KmlBuilder(kml, topLevelFolder, styleIds);

    referencedFiles = new ArrayList<>();
  }

  /**
   * Writes the content of {@link C3mlData} as a KMZ file.
   *
   * @param data The {@link C3mlData} document to convert.
   * @throws ConversionException if the conversion failed.
   */
  public File convert(C3mlData data) throws ConversionException {
    try {
      entityIdMap = new HashMap<>();
      for (C3mlEntity entity : data.getC3mls()) {
        entityIdMap.put(entity.getId(), entity);
      }

      for (C3mlEntity entity : data.getC3mls()) {
        writeEntity(entity, topLevelFolder);
      }
      return writeOutput();
    } catch (Exception e) {
      throw new ConversionException("Failed to convert to KMZ", e);
    }
  }

  /**
   * Writes the {@link C3mlEntity} hierarchy as {@link Placemark}s within the {@link Folder}. If the
   * hierarchy is made up of meshes only, convert the hierarchy as a COLLADA model.
   *
   * @param entity
   * @param parentFolder
   */
  private void writeEntity(C3mlEntity entity, Folder parentFolder) {
    if (entity.getType() == C3mlEntityType.COLLECTION) {
      parentFolder = kmlBuilder.writeFolder(entity, parentFolder);
    } else {
      if (entity.getType() == C3mlEntityType.MESH) {
        writeModel(entity, parentFolder);
      } else {
        kmlBuilder.writeEntity(entity, parentFolder);
      }
    }

    if (!entity.getChildrenIds().isEmpty()) {
      for (String childId : entity.getChildrenIds()) {
        Folder entityFolder = parentFolder.createAndAddFolder().withName(entity.getName());
        writeEntity(entityIdMap.get(childId), entityFolder);
      }
    }
  }

  /**
   * Creates a COLLADA model for the {@link C3mlEntity} hierarchy and references it by the model's
   * id.
   *
   * @param entity {@link C3mlEntity}
   */
  private void writeModel(C3mlEntity entity, Folder parentFolder) {
    Placemark placemark = parentFolder.createAndAddPlacemark();
    placemark.setName(entity.getName());
    Model model = placemark.createAndSetModel();
    model.withAltitudeMode(AltitudeMode.RELATIVE_TO_GROUND);

    Vertex3D location = new Vertex3D(entity.getGeoLocation(), true);
    Vertex3D rotation = new Vertex3D(entity.getRotation(), false);
    Vertex3D scale = new Vertex3D(entity.getScale(), false);

    model.createAndSetLocation().withLongitude(location.getLongitude())
        .withLatitude(location.getLatitude()).withAltitude(location.getAltitude());

    model.createAndSetOrientation().withHeading(rotation.getX()).withTilt(rotation.getY())
        .withRoll(rotation.getZ());

    model.createAndSetScale().withX(scale.getX()).withY(scale.getY()).withZ(scale.getZ());

    String colladaModelHref = entity.getId() + ".dae";
    model.createAndSetLink().withHref(colladaModelHref);

    // Collapse hierarchy by removing duplicated entities in KML and COLLADA.
    if (entity.getChildren().size() == 1) {
      entity = entity.getChildren().get(0);
    }
    createTempColladaModelFile(colladaModelHref, entity);
  }

  /**
   * Creates a temporary COLLADA file that models the {@link C3mlEntity} hierarchy.
   *
   * @param hrefString The string reference to the COLLADA file.
   * @param entity
   */
  private void createTempColladaModelFile(String hrefString, C3mlEntity entity) {
    ColladaWriter colladaWriter = new ColladaWriter();

    colladaWriter.startDocument();
    colladaWriter.writeAssets();
    colladaWriter.startWriteVisualScenes();

    colladaWriter.writeMeshNode(entity, 0);

    colladaWriter.endWriteVisualScenes();
    colladaWriter.writeLibraryNodes();
    colladaWriter.writeGeometries();
    colladaWriter.writeEffects();
    colladaWriter.writeMaterials();
    colladaWriter.writeScene();
    colladaWriter.endDocument();
    createTempHrefFile(hrefString, colladaWriter.getResultAsString().getBytes());
  }

  /**
   * Creates a temporary href file that is used by model or image references.
   *
   * @param hrefString The string reference to the file.
   * @param data
   */
  private void createTempHrefFile(String hrefString, byte[] data) {
    try {
      referencedFiles.add(FileUtils.createTemporaryFileWithContent(hrefString, data));
    } catch (IOException e) {
      log.error(e);
    }
  }

  /**
   * Zips the KML and resources into a KMZ file.
   *
   * @throws ConversionException
   */
  private File writeOutput() throws ConversionException {
    try {
      String kmlDoc = kmlBuilder.marshall();
      List<File> filesToZip = Lists.newArrayList();
      File tmpKmlFile = FileUtils.createTemporaryFileWithContent("doc.kml", kmlDoc.getBytes());
      filesToZip.add(tmpKmlFile);
      filesToZip.addAll(referencedFiles);

      File tmpKmzFile = FileUtils.createTemporaryFileWithContent("project.kmz", null);
      ZipUtils.zipFilesToDirectory(filesToZip, tmpKmzFile);
      return tmpKmzFile;
    } catch (IOException e) {
      throw new ConversionException("Failed to save files to KMZ", e);
    }
  }

}
