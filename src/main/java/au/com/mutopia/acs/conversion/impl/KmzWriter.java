package au.com.mutopia.acs.conversion.impl;

import java.util.zip.ZipFile;

import au.com.mutopia.acs.models.c3ml.C3mlData;
import au.com.mutopia.acs.models.c3ml.C3mlEntity;
import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.Kml;

/**
 * Converts C3ML data into a KMZ file (the reverse of {@link KmzConverter}).
 */
public class KmzWriter {

  /**
   * Converts the given {@link C3mlData} into a KMZ ZIP file.
   */
  public ZipFile convert(C3mlData data) {
    Kml kml = new Kml();
    Folder topFolder = kml.createAndSetFolder().withName("entities");

    for (C3mlEntity entity : data.getC3mls()) {

    }

    // for (GeoComposite child : geoData.getChildren()) {
    // if (child instanceof GeoObject) {
    // writeGeoObject((GeoObject) child, topLevelFolder);
    // }
    // }

    return null;
  }

}
