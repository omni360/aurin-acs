package au.com.mutopia.acs.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

/**
 * Utility functions for working with files.
 */
public class FileUtils {

  // TODO(aramk) Remove the files when JVM shuts down.

  /**
   * The directory used to store temporary files.
   */
  public static final String TEMP_DIR = System.getProperty("java.io.tmpdir");

  /**
   * Creates a temporary file given the file name and content.
   *
   * @param fileName The name of the file to be created.
   * @param contents The contents of the file.
   * @return The temporary file created.
   * @throws IOException If the file cannot be created or written to.
   */
  public static File createTemporaryFileWithContent(String fileName, byte[] contents)
      throws IOException {
    File tmpFile = createTempFile(fileName);
    FileOutputStream out = new FileOutputStream(tmpFile);
    if (contents != null) {
      out.write(contents);
    }
    out.flush();
    out.close();
    return tmpFile;
  }

  /**
   * Creates a temporary file with a UUID as the name.
   * 
   * @param contents The contents to write to the temporary file.
   * @return A handle to the create file.
   * @throws IOException if the file creation failed.
   * @see #createTemporaryFileWithContent(String, byte[])
   */
  public static File createTemporaryFileWithContent(byte[] contents) throws IOException {
    return createTemporaryFileWithContent(UUID.randomUUID().toString(), contents);
  }

  /**
   * Creates a temporary file with the given name.
   * 
   * @param fileName The name of the file to create.
   * @return A handle to the create file.
   * @throws IOException if the file creation failed.
   */
  public static File createTempFile(String fileName) throws IOException {
    // Generate a subdirectory to allow using the given filename without a random string.
    File subDir = createTempDir();
    return new File(subDir, fileName);
  }

  /**
   * Creates a file system directory for temporary files with a random UUID as the name.
   * 
   * @return A handle to the created directory.
   */
  public static File createTempDir() {
    String subDirName = UUID.randomUUID().toString();
    File subDir = new File(TEMP_DIR, subDirName);
    subDir.mkdirs();
    return subDir;
  }

  /**
   * Retrieves the contents from the given file as byte array.
   *
   * @param file The file to read.
   * @return The byte array of the file contents.
   * @throws IOException If the file cannot be read.
   */
  public static byte[] bytesFromFile(File file) throws IOException {
    return IOUtils.toByteArray(new FileInputStream(file));
  }
  
  /**
   * Finds the file at the given path ignoring case.
   *
   * @param path The case-insensitive path of the file to get.
   * @return The file matching the path, or null if not file exists.
   */
  public static File getFileCaseInsensitive(String path) {
    File daeFile = new File(path);
    if (!daeFile.exists()) {
      String ext = FilenameUtils.getExtension(path);
      path = path.substring(0, path.length() - ext.length()) + ext.toUpperCase();
      daeFile = new File(path);
    }
    return daeFile.exists() ? daeFile : null;
  }

}
