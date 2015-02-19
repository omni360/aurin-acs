package au.com.mutopia.acs.conversion.output;

import au.com.mutopia.acs.models.Format;
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
   * List of style IDs created.
   */
  private List<String> styleIds;

  /**
   * List of files (models or icons) being referenced by KML.
   */
  private List<File> referencedFiles;

  /**
   * Map of file names to amount of times the same file name is used.
   */
  private Map<String, Integer> uniqueFileNameMap;

  /**
   * Map of entity IDs to objects to reconstruct the hierarchy.
   */
  private Map<String, C3mlEntity> entityIdMap;

  /**
   * List of entities converted to KML elements, to prevent multiple conversion of the same entity.
   */
  private List<String> convertedEntityIds;

  /**
   * Map of entity IDs to whether the entity's hierarchy is made up of meshes only.
   */
  private Map<String, Boolean> entityIdIsMeshOnlyHierarchyMap;

  /**
   * Initialises the KMZ write. Creates a new KML then creates a KML folder within it.
   */
  private void init() {
    kml = new Kml();
    topLevelFolder = kml.createAndSetFolder().withName("entities");
    styleIds = new ArrayList<>();
    kmlBuilder = new KmlBuilder(kml, topLevelFolder, styleIds);
    referencedFiles = new ArrayList<>();
    uniqueFileNameMap = new HashMap<>();
    entityIdMap = new HashMap<>();
    convertedEntityIds = new ArrayList<>();
    entityIdIsMeshOnlyHierarchyMap = new HashMap<>();
  }

  /**
   * Writes the content of {@link C3mlData} as a KMZ file.
   *
   * @param data The {@link C3mlData} document to convert.
   * @throws ConversionException if the conversion failed.
   */
  public File convert(C3mlData data) throws ConversionException {
    try {
      init();
      for (C3mlEntity entity : data.getC3mls()) {
        entityIdMap.put(entity.getId(), entity);
      }
      for (C3mlEntity entity : data.getC3mls()) {
        checkIsMeshOnlyHierarchy(entity);
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
   * @param entity The {@link C3mlEntity}.
   * @param parentFolder The {@link Folder} to contain the created {@link Placemark}
   */
  private void writeEntity(C3mlEntity entity, Folder parentFolder) {
    String entityId = entity.getId();
    if (convertedEntityIds.contains(entityId)) return;
    convertedEntityIds.add(entityId);
    if (entityIdIsMeshOnlyHierarchyMap.get(entityId)) {
      writeModel(entity, parentFolder, true);
      return;
    }
    if (entity.getType() == C3mlEntityType.MESH) {
      writeModel(entity, parentFolder, false);
    } else {
      kmlBuilder.writeEntity(entity, parentFolder);
    }
    if (!entity.getChildrenIds().isEmpty()) {
      parentFolder = parentFolder.createAndAddFolder().withName(entity.getName());
    }
    for (String childId : entity.getChildrenIds()) {
      writeEntity(entityIdMap.get(childId), parentFolder);
    }
  }

  /**
   * Checks if the {@link C3mlEntity} hierarchy is made up of meshes only.
   *
   * @param entity The {@link C3mlEntity}.
   * @return True if the {@link C3mlEntity} hierarchy is made up of meshes only.
   */
  private boolean checkIsMeshOnlyHierarchy(C3mlEntity entity) {
    boolean isMeshOnlyHierarchy = true;
    List<String> childrenIds = entity.getChildrenIds();
    if (!isMesh(entity)) {
      isMeshOnlyHierarchy = false;
    }
    for (String childId : childrenIds) {
      C3mlEntity childEntity = entityIdMap.get(childId);
      if (!checkIsMeshOnlyHierarchy(childEntity)) {
        isMeshOnlyHierarchy = false;
      }
    }
    entityIdIsMeshOnlyHierarchyMap.put(entity.getId(), isMeshOnlyHierarchy);
    return isMeshOnlyHierarchy;
  }

  /**
   * A {@link C3mlEntity} is potentially a mesh if it is of type mesh or it is a collection with
   * children.
   *
   * @param entity The {@link C3mlEntity}.
   * @return True if {@link C3mlEntity} is potentially a mesh.
   */
  private boolean isMesh(C3mlEntity entity) {
    C3mlEntityType type = entity.getType();
    return type == C3mlEntityType.MESH ||
        type == C3mlEntityType.COLLECTION && entity.getChildrenIds().size() > 0;
  }

  /**
   * Creates a COLLADA model for the {@link C3mlEntity} hierarchy and references it by the model's
   * id.
   *
   * @param entity The {@link C3mlEntity}.
   * @param parentFolder The {@link Folder} to contain the created {@link Placemark}.
   * @param writeHierarchy Boolean option to write the {@link C3mlEntity} as a single entity or
   * hierarchy.
   */
  private void writeModel(C3mlEntity entity, Folder parentFolder, boolean writeHierarchy) {
    Placemark placemark = kmlBuilder.createPlacemark(entity, parentFolder);

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

    String colladaModelHref = getUniqueFileHref(entity.getId(), Format.COLLADA.toString());
    model.createAndSetLink().withHref(colladaModelHref);

    // Collapse hierarchy by removing duplicated entities in KML and COLLADA.
    if (entity.getChildren().size() == 1) {
      entity = entity.getChildren().get(0);
    }
    createTempColladaModelFile(colladaModelHref, entity, writeHierarchy);
  }

  /**
   * Creates a temporary COLLADA file that models the {@link C3mlEntity} hierarchy.
   *
   * @param hrefString The string reference to the COLLADA file.
   * @param entity The {@link C3mlEntity}.
   * @param writeHierarchy Boolean option to write the {@link C3mlEntity} as a single entity or
   * hierarchy.
   */
  private void createTempColladaModelFile(String hrefString, C3mlEntity entity,
      boolean writeHierarchy) {
    ColladaWriter colladaWriter = new ColladaWriter();

    colladaWriter.startDocument();
    colladaWriter.writeAssets();
    colladaWriter.startWriteVisualScenes();

    if (writeHierarchy) {
      convertedEntityIds.addAll(getIdsInHieararchy(entity));
      colladaWriter.writeMeshHierarchy(entity, entityIdMap, 0);
    } else {
      colladaWriter.writeMeshNode(entity, 0);
    }

    colladaWriter.endWriteVisualScenes();
    colladaWriter.writeGeometries();
    colladaWriter.writeEffects();
    colladaWriter.writeMaterials();
    colladaWriter.writeScene();
    colladaWriter.endDocument();
    createTempHrefFile(hrefString, colladaWriter.getResultAsString().getBytes());
  }

  /**
   * @param entity The {@link C3mlEntity}.
   * @return The list of all {@link C3mlEntity} IDs within the hierarchy.
   */
  private List<String> getIdsInHieararchy(C3mlEntity entity) {
    List<String> ids = new ArrayList<>();
    ids.add(entity.getId());
    for (String childId : entity.getChildrenIds()) {
      ids.addAll(getIdsInHieararchy(entityIdMap.get(childId)));
    }
    return ids;
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

  /**
   * Creates a new filename if given filename is already in used.
   *
   * @param fileName The filename to be used.
   * @param fileExtension The file extension.
   * @return A new unique filename for referencing.
   */
  private String getUniqueFileHref(String fileName, String fileExtension) {
    if (!uniqueFileNameMap.containsKey(fileName)) {
      uniqueFileNameMap.put(fileName, 1);
      return fileName + "." + fileExtension;
    } else {
      Integer duplicateNameCount = uniqueFileNameMap.get(fileName) + 1;
      uniqueFileNameMap.put(fileName, duplicateNameCount);
      return fileName + "_" + duplicateNameCount + "." + fileExtension;
    }
  }
}
