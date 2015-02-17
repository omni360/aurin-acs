package au.com.mutopia.acs.conversion.impl;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import au.com.mutopia.acs.exceptions.ConversionException;
import au.com.mutopia.acs.models.c3ml.C3mlData;
import au.com.mutopia.acs.models.c3ml.C3mlEntity;
import au.com.mutopia.acs.util.FileUtils;
import au.com.mutopia.acs.util.ZipUtils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.Kml;

/**
 * Converts C3ML data into a KMZ file (the reverse of {@link KmzConverter}).
 */
public class KmzWriter {

  /**
   * Converts the given {@link C3mlData} into a KMZ ZIP file.
   * 
   * @throws ConversionException
   */
  public File convert(C3mlData data) throws ConversionException {
    Kml kml = new Kml();
    Folder topFolder = kml.createAndSetFolder().withName("entities");

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

    }

    // Zip the KML and resources into a KMZ file.
    File templKmlFile;
    try {
      templKmlFile = FileUtils.createTemporaryFileWithContent("doc.kml", "".getBytes());
      kml.marshal(templKmlFile);

      File kmzFile = FileUtils.createTempFile("out.kmz");
      ZipUtils.zipFilesToDirectory(ImmutableList.of(templKmlFile), kmzFile);

      return kmzFile;
    } catch (IOException e) {
      throw new ConversionException("Failed to save files to KMZ", e);
    }
  }

}
