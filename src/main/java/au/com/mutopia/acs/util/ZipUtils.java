package au.com.mutopia.acs.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import lombok.extern.log4j.Log4j;
import au.com.mutopia.acs.models.Format;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.io.Files;

/**
 * Utility functions for working with ZIP archives.
 */
@Log4j
public class ZipUtils {

  // TODO(aramk) Refactor this and its test into ZipUtils in catalyst-model.

  /**
   * The size of byte array buffer.
   */
  private static final int BYTE_BUFFER_SIZE = 1024;

  /**
   * Zips the list of files to the given directory file.
   *
   * @param files The list of files to be zipped.
   * @param zipFile The destination zip file.
   */
  public static void zipFilesToDirectory(List<File> files, File zipFile) {
    byte[] buffer = new byte[BYTE_BUFFER_SIZE];

    try {
      FileOutputStream fos = new FileOutputStream(zipFile);
      ZipOutputStream zos = new ZipOutputStream(fos);

      for (File file : files) {
        ZipEntry ze = new ZipEntry(file.getName());
        zos.putNextEntry(ze);
        FileInputStream in = new FileInputStream(file);
        int len;
        while ((len = in.read(buffer)) > 0) {
          zos.write(buffer, 0, len);
        }
        in.close();
      }

      zos.closeEntry();
      zos.close();
    } catch (IOException e) {
      log.error("Failed to zip " + files.size() + " files", e);
    }
  }

  /**
   * Unzips the contents of a zip file into a temporary directory and returns the list of unzipped
   * files.
   *
   * @param zipFile The zip file to be extracted.
   * @return The list of files extracted from the zip file.
   */
  public static List<File> unzipToTempDirectory(File zipFile) {
    // Create a temporary directory for storing unzipped files.
    File tmpDir = FileUtils.createTempDir();
    try {
      ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
      ZipEntry ze = zis.getNextEntry();
      List<File> unzipFiles = Lists.newArrayList();
      while (ze != null) {
        String entryName = ze.getName();

        File file = new File(tmpDir, entryName);

        String fileExtension = Files.getFileExtension(file.getName());
        if (Strings.isNullOrEmpty(fileExtension)) {
          ze = zis.getNextEntry();
          continue;
        }
        Files.createParentDirs(file);

        File parentFile = file.getParentFile();
        parentFile.mkdirs();

        FileOutputStream fos = new FileOutputStream(file);
        int len;
        byte buffer[] = new byte[BYTE_BUFFER_SIZE];
        while ((len = zis.read(buffer)) > 0) {
          fos.write(buffer, 0, len);
        }
        fos.close();
        unzipFiles.add(file);
        ze = zis.getNextEntry();
      }
      zis.closeEntry();
      zis.close();
      return unzipFiles;
    } catch (IOException e) {
      log.error("{}", e);
    }
    return null;
  }

  /**
   * Extracts the files in the ZIP archive, and returns those with the specified file extension.
   * Note: does not clean up any of the extracted files, in case the matched files reference other
   * files.
   *
   * @param zipFile The ZIP archive to unzip.
   * @param ext The file extension to match files with.
   * @param allowDotfiles Whether to ignore hidden files (filename starts with a dot).
   * @return A list of the files in the ZIP archive with the extension.
   */
  public static List<File> extractByExtension(File zipFile, String ext, boolean allowDotfiles) {
    List<File> matchFiles = new ArrayList<>();
    List<File> unzippedFiles = ZipUtils.unzipToTempDirectory(zipFile);
    for (File unzippedFile : unzippedFiles) {
      String filename = unzippedFile.getName();
      if (Files.getFileExtension(filename).equals(ext)
          && (allowDotfiles || !filename.startsWith("."))) {
        matchFiles.add(unzippedFile);
      }
    }
    return matchFiles;
  }

  public static List<File> extractByExtension(File zipFile, String ext) {
    return extractByExtension(zipFile, ext, false);
  }

  /**
   * Compresses the given string into a byte array.
   *
   * @param text The text to compress.
   * @return The string compressed into a byte array.
   * @see <a href="http://stackoverflow.com/questions/10572398">Stack Overflow answer</a>
   */
  public static byte[] compressString(String text) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try {
      OutputStream out = new DeflaterOutputStream(baos);
      out.write(text.getBytes("UTF-8"));
      out.close();
    } catch (IOException e) {
      throw new AssertionError(e);
    }
    return baos.toByteArray();
  }

  /**
   * Decompresses the given bytes into a UTF-8 string.
   *
   * @param data The bytes to decompress.
   * @return The string encoded by the bytes.
   *
   * @see <a href="http://stackoverflow.com/questions/10572398">Stack Overflow answer</a>
   */
  public static String decompressString(byte[] data) {
    InputStream in = new InflaterInputStream(new ByteArrayInputStream(data));
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try {
      byte[] buffer = new byte[BYTE_BUFFER_SIZE];
      int len;
      while ((len = in.read(buffer)) > 0)
        baos.write(buffer, 0, len);
      return new String(baos.toByteArray(), "UTF-8");
    } catch (IOException e) {
      throw new AssertionError(e);
    }
  }

}
