package au.com.mutopia.acs.conversion.output;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.log4j.Log4j;
import au.com.mutopia.acs.conversion.impl.KmzConverter;
import au.com.mutopia.acs.exceptions.ConversionException;
import au.com.mutopia.acs.models.c3ml.C3mlData;
import au.com.mutopia.acs.models.c3ml.C3mlEntity;
import au.com.mutopia.acs.models.c3ml.C3mlEntityType;
import au.com.mutopia.acs.models.c3ml.Vertex3D;
import au.com.mutopia.acs.util.ZipUtils;

import com.dddviewr.collada.Collada;
import com.dddviewr.collada.visualscene.VisualScene;
import com.google.common.collect.Lists;
import com.google.common.io.Files;

/**
 * Converts C3ML data into a KMZ file (the reverse of {@link KmzConverter}).
 */
@Log4j
public class KmzWriter {

  private static final ColladaBuilder colladaBuilder = new ColladaBuilder();

  /**
   * Converts the given {@link C3mlData} into a KMZ ZIP file.
   * 
   * @param data The {@link C3mlData} to write.
   * @param outFilename The name of file to write the output into.
   * @throws ConversionException if the conversion fails.
   */
  public File convert(C3mlData data, String outFilename) throws ConversionException {
    KmlBuilder kmlBuilder = new KmlBuilder();
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

        switch (entity.getType()) {
          case POINT:
            kmlBuilder.writePoint(entity);
            break;
          case LINE:
            kmlBuilder.writeLine(entity);
            break;
          case POLYGON:
            kmlBuilder.writePolygon(entity);
            break;
          case COLLECTION:
          case FEATURE:
            kmlBuilder.writeFolder(entity);
            break;
          default:
            log.warn("Unknown entity type: " + entity.getType());
        }
      }
    }

    // Zip the KML and resources into a KMZ file.
    File tempKmlFile;

    try {
      File outDir = Files.createTempDir();
      List<File> outFiles = new ArrayList<>();

      // Write the COLLADA to the KML.
      if (!scene.getNodes().isEmpty()) {
        String daePath = "models/untitled.dae";
        // File daeFile =
        // new File(String.format("%s/%s/%s", FileUtils.TEMP_DIR, UUID.randomUUID().toString(),
        // daePath));
        File modelsDir = new File(outDir, "models");
        modelsDir.mkdirs();
        File daeFile = new File(modelsDir, "untitled.dae");

        FileOutputStream fos = new FileOutputStream(daeFile);
        collada.dump(new PrintStream(fos), 2);
        fos.close();

        kmlBuilder.writeAggregateModel(daePath, new Vertex3D());
        outFiles.add(modelsDir);
      }

      // Write the KML.
      tempKmlFile = new File(outDir, "doc.kml");
      kmlBuilder.getKml().marshal(tempKmlFile);
      outFiles.add(tempKmlFile);

      // Zip the files.
      File outFile = new File(outDir, outFilename);
      ZipUtils.zipFilesToDirectory(outFiles, outFile);
      return outFile;
    } catch (IOException e) {
      throw new ConversionException("Failed to save files to KMZ", e);
    }
  }

  /**
   * Converts the data to KMZ and writes to the given file.
   * 
   * @throws ConversionException
   */
  public File convert(C3mlData data) throws ConversionException, IOException {
    return convert(data, "out.kmz");
  }

}
