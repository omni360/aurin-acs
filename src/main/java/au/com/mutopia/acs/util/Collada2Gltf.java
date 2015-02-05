package au.com.mutopia.acs.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import com.google.common.collect.ImmutableMap;

/**
 * Wrapper for invoking the <code>COLLADA2GLTF</code> tool via the command line. Expects
 * <code>collada2gltf</code> to be available on the system path.
 */
public class Collada2Gltf {

  /**
   * Converts the given file to KML, so long as the input is a file supported by ogr2ogr.
   *
   * @param file The input file, expected to be COLLADA (.dae).
   * @param embed Whether to embed all of the data as URIs (true) or create separate files (false).
   * @return The output glTF file.
   */
  public static File convertToGltf(File file, boolean embed) {
    String inPath = file.getAbsolutePath();
    String outPath = FilenameUtils.removeExtension(inPath) + ".gltf";

    // Build the command.
    CommandLine cmdLine = new CommandLine("collada2gltf");
    cmdLine.addArgument("-f");
    cmdLine.addArgument("${in}");
    cmdLine.addArgument("-o");
    cmdLine.addArgument("${out}");
    if (embed) cmdLine.addArgument("-e");
    cmdLine.setSubstitutionMap(ImmutableMap.of("in", inPath, "out", outPath));

    // Execute the command with the system environment, and handle errors.
    try {
      int exitValue = new DefaultExecutor().execute(cmdLine, System.getenv());
      if (exitValue != 0) {
        throw new RuntimeException("COLLADA2GLTF returned exit code " + exitValue);
      }
    } catch (IOException e) {
      throw new RuntimeException("Failed to convert " + file.getAbsolutePath()
          + " with COLLADA2GLTF", e);
    }
    return new File(outPath);
  }

  /**
   * Converts the given file to glTF, embedding all binary data by default.
   *
   * @param file The COLLADA file to convert.
   * @return The converted glTF file.
   */
  public static File convertToGltf(File file) {
    return convertToGltf(file, true);
  }

  public static File convertToGltfFile(String collada) throws IOException {
    File daeFile = FileUtils.createTemporaryFileWithContent("tmp.dae", collada.getBytes());
    return convertToGltf(daeFile);
  }

  public static String convertToGltfString(String collada) throws IOException {
    return IOUtils.toString(new FileInputStream(convertToGltfFile(collada)));
  }

  public static byte[] convertToGltfBytes(String collada) throws IOException {
    return IOUtils.toByteArray(new FileInputStream(convertToGltfFile(collada)));
  }

}
