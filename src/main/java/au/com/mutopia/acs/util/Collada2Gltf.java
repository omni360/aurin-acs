package au.com.mutopia.acs.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import lombok.extern.log4j.Log4j;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import com.google.common.collect.ImmutableMap;

/**
 * Wrapper for invoking the <code>COLLADA2GLTF</code> tool via the command line. Expects
 * <code>collada2gltf</code> to be available on the system path.
 */
@Log4j
public class Collada2Gltf {

  /** Whether to embed by default or not. */
  private static final boolean DEFAULT_EMBED = true;

  /** Whether to embed all of the data as URIs (true) or create separate files (false). */
  private final boolean embed;

  /** The file in which the COLLADA to convert is stored. */
  private final File colladaFile;

  public Collada2Gltf(final File colladaFile, boolean embed) {
    this.colladaFile = colladaFile;
    this.embed = embed;
  }

  public Collada2Gltf(final File colladaFile) {
    this(colladaFile, DEFAULT_EMBED);
  }

  public Collada2Gltf(String collada, boolean embed) throws IOException {
    this(FileUtils.createTemporaryFileWithContent("tmp.dae", collada.getBytes()), embed);
  }

  public Collada2Gltf(String collada) throws IOException {
    this(FileUtils.createTemporaryFileWithContent("tmp.dae", collada.getBytes()), DEFAULT_EMBED);
  }

  /**
   * Converts the given file to KML, so long as the input is a file supported by ogr2ogr.
   *
   * @param file The input file, expected to be COLLADA (.dae).
   * @param embed Whether to embed all of the data as URIs (true) or create separate files (false).
   * @return The output glTF file.
   */
  public File convertToGltfFile() {
    log.debug("Converting " + colladaFile.getAbsolutePath() + " to glTF...");
    String inPath = colladaFile.getAbsolutePath();
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
      log.debug("Executing command: " + cmdLine.toString());
      int exitValue = new DefaultExecutor().execute(cmdLine, System.getenv());
      if (exitValue != 0) {
        throw new RuntimeException("COLLADA2GLTF returned exit code " + exitValue);
      }
    } catch (IOException e) {
      throw new RuntimeException("Failed to convert " + colladaFile.getAbsolutePath()
          + " with COLLADA2GLTF", e);
    }
    return new File(outPath);
  }

  /**
   * Converts the COLLADA file to a glTF file, then reads the result as a string.
   * 
   * @return The string data from the glTF file.
   * @throws IOException if the glTF conversion fails.
   */
  public String convertToGltfString() throws IOException {
    return IOUtils.toString(new FileInputStream(convertToGltfFile()));
  }

  /**
   * Converts the COLLADA file to a glTF file, then reads the result as a byte array.
   * 
   * @return The byte array data from the glTF file.
   * @throws IOException if the glTF conversion fails.
   */
  public byte[] convertToGltfBytes() throws IOException {
    return IOUtils.toByteArray(new FileInputStream(convertToGltfFile()));
  }

}
