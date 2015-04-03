package au.com.mutopia.acs.util;

import au.com.mutopia.acs.exceptions.ConversionException;
import com.google.common.collect.ImmutableMap;
import lombok.extern.log4j.Log4j;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

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

    // Build the command.
    CommandLine cmdLine = new CommandLine("ogr2ogr");
    cmdLine.addArgument("-f");
    cmdLine.addArgument("KML");
    cmdLine.addArgument("${out}");
    // TODO(aramk) Without the false argument, quotes are added if the path contains spaces, and
    // this causes ogr2ogr to fail since double quotes are passed.
    cmdLine.addArgument("${in}", false);
    cmdLine.setSubstitutionMap(ImmutableMap.of("in", file, "out", outPath));

    // Execute the command with the system environment, and handle errors.
    try {
      log.debug("Executing command: " + cmdLine.toString());
      int exitValue = new DefaultExecutor().execute(cmdLine, System.getenv());
      if (exitValue != 0) {
        throw new RuntimeException("ogr2ogr returned exit code " + exitValue);
      }
    } catch (IOException e) {
      throw new RuntimeException("Failed to convert " + file.getAbsolutePath()
          + " with ogr2ogr", e);
    }
    return new File(outPath);
  }

}
