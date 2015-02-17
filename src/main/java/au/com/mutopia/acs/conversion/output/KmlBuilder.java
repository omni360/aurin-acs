package au.com.mutopia.acs.conversion.output;

import de.micromata.opengis.kml.v_2_2_0.Kml;

/**
 * Constructs KML objects.
 */
public class KmlBuilder {

  /**
   * Creates a new {@link Kml} object to populate with 2D entities and hierarchy.
   */
  public Kml initKml() {
    return new Kml();
  }

}
