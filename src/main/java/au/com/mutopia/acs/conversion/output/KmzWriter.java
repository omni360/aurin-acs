package au.com.mutopia.acs.conversion.output;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import au.com.mutopia.acs.conversion.impl.KmzConverter;
import au.com.mutopia.acs.exceptions.ConversionException;
import au.com.mutopia.acs.models.c3ml.C3mlData;
import au.com.mutopia.acs.models.c3ml.C3mlEntity;
import au.com.mutopia.acs.models.c3ml.C3mlEntityType;
import au.com.mutopia.acs.util.FileUtils;
import au.com.mutopia.acs.util.ZipUtils;

import com.dddviewr.collada.Collada;
import com.dddviewr.collada.visualscene.VisualScene;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.Kml;

/**
 * Converts C3ML data into a KMZ file (the reverse of {@link KmzConverter}).
 */
public class KmzWriter {

  private static final ColladaBuilder colladaBuilder = new ColladaBuilder();
  private static final KmlBuilder kmlBuilder = new KmlBuilder();

  /**
   * Converts the given {@link C3mlData} into a KMZ ZIP file.
   * 
   * @throws ConversionException
   */
  public File convert(C3mlData data) throws ConversionException {
    Kml kml = kmlBuilder.initKml();
    Folder topFolder = kml.createAndSetFolder().withName("entities");

    Collada collada = colladaBuilder.initCollada();
    VisualScene scene = new VisualScene("AcsScene", "AcsScene");
    collada.getLibraryVisualScenes().addVisualScene(scene);

    Map<String, C3mlEntity> entityIdMap = new HashMap<>();
    for (C3mlEntity entity : data.getC3mls()) {
      entityIdMap.put(entity.getId(), entity);
    }
    for (C3mlEntity entity : data.getC3mls()) {
      // Create a new list to avoid concurrent modification.
      List<String> childIds = Lists.newArrayList(entity.getChildrenIds());

      // Reconstruct the entity hierarchy.
      for (String childId : childIds) {
        entity.addChild(entityIdMap.get(childId));
      }

      // Convert each of the entities into a corresponding KML or COLLADA element.
      if (entity.getType() == C3mlEntityType.MESH) {
        scene.addNode(colladaBuilder.buildColladaNode(entity, collada));
      } else {

      }
    }

    // Zip the KML and resources into a KMZ file.
    File templKmlFile;
    try {
      // Write the KML.
      templKmlFile = FileUtils.createTemporaryFileWithContent("doc.kml", "".getBytes());
      kml.marshal(templKmlFile);

      // Write the COLLADA.
      if (collada.getLibraryGeometries().getGeometries().size() > 0) {

      }

      // Zip the files.
      File kmzFile = FileUtils.createTempFile("out.kmz");
      ZipUtils.zipFilesToDirectory(ImmutableList.of(templKmlFile), kmzFile);
      return kmzFile;
    } catch (IOException e) {
      throw new ConversionException("Failed to save files to KMZ", e);
    }
  }

}
