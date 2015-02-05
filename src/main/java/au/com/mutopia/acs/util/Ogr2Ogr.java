package au.com.mutopia.acs.util;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import lombok.extern.log4j.Log4j;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;

import au.com.mutopia.acs.exceptions.ConversionException;

/**
 * Wrapper for invoking the ogr2ogr tool via the command line.
 */
@Log4j
public class Ogr2Ogr {

  private static final String TEMP_DIR = FileUtils.createTempDir().getAbsolutePath();

  /**
   * Converts the given file to KML, so long as the input is a file supported by ogr2ogr.
   * 
   * @param file The input file, expected to be SHP or GeoJSON.
   * @return The output KML file.
   * @throws ConversionException If the conversion fails.
   */
  public static File convertToKml(File file) throws ConversionException {
    String outPath = TEMP_DIR + "/" + UUID.randomUUID() + ".kml";
    String command = String.format("ogr2ogr -f KML \"%s\" \"%s\"", outPath, file.getAbsolutePath());

    CommandLine cmdLine = CommandLine.parse(command);
    DefaultExecutor executor = new DefaultExecutor();
    try {
      log.debug("Executing command: " + command);
      int exitValue = executor.execute(cmdLine);
      if (exitValue != 0) {
        throw new ConversionException("ogr2ogr returned exit code " + exitValue);
      }
    } catch (IOException e) {
      throw new ConversionException(
          "Failed to convert " + file.getAbsolutePath() + " with ogr2ogr", e);
    }
    return new File(outPath);
  }

}
