package au.com.mutopia.acs.models;

import java.io.File;
import java.io.IOException;

import lombok.Getter;
import lombok.Setter;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Strings;
import com.sun.jersey.core.header.FormDataContentDisposition;

/**
 * A file that has been uploaded or converted from another file.
 */
@Getter
@Setter
public class Asset {

  /** A name describing the asset. */
  private String name;

  /** The binary content of the file. */
  @JsonIgnore
  private byte[] data;

  /** The format of the underlying data. */
  private Format format;

  /** The filename of the asset uploaded or created. */
  private String fileName;

  /** The file size in bytes of the file. */
  private Long fileSize;

  /**
   * Creates an empty {@link Asset} object.
   */
  public Asset() {}

  /**
   * Creates an {@link Asset} populated with the given data and details.
   * 
   * @param data The binary data from the uploaded file.
   * @param fileDetails Metadata of the uploaded file.
   */
  public Asset(byte[] data, FormDataContentDisposition fileDetails) {
    setData(data);
    setFileName(fileDetails.getFileName());
    setFileSize(fileDetails.getSize());
    setFormat(determineFormat());
  }

  /**
   * Creates an {@link Asset} populated with the given data and details.
   * 
   * @param data The binary data from the uploaded file.
   * @param fileDetails Metadata of the uploaded file.
   */
  public Asset(Byte[] data, FormDataContentDisposition fileDetails) {
    this(ArrayUtils.toPrimitive(data), fileDetails);
  }

  /**
   * Creates an {@link Asset} populated with the data and details of the given file.
   * 
   * @param file The file as a Java object.
   * @throws IOException if the file cannot be read.
   */
  public Asset(File file) throws IOException {
    setData(FileUtils.readFileToByteArray(file));
    setFileName(file.getName());
    setFileSize(file.length());
    setFormat(determineFormat());
  }

  /**
   * Sets the asset data from a primitive byte array. Delegates to set the file size as well.
   * 
   * @param data The binary data to set.
   */
  public void setData(byte[] data) {
    setDataValue(data);
  }

  /**
   * Sets the binary data as well as its file size.
   * 
   * @param data The data to set.
   */
  private void setDataValue(byte[] data) {
    this.data = data;
    if (data != null) {
      setFileSize((long) getData().length);
    }
  }

  /**
   * Attempts to determine which format the {@link Asset} is.
   * 
   * @return A guess of the {@link Asset}'s format based on all set properties.
   */
  private Format determineFormat() {
    if (Strings.isNullOrEmpty(getFileName())) return null;
    String ext = FilenameUtils.getExtension(getFileName());
    return Format.getByValue(ext);
  }

  @Override
  public String toString() {
    return String.format("Asset[%s (%s)]", getFileName(),
        FileUtils.byteCountToDisplaySize(getFileSize()));
  }

  /**
   * @return A temporary file with the asset's filename and content.
   * @throws IOException
   */
  public File getTemporaryFile() throws IOException {
    return au.com.mutopia.acs.util.FileUtils.createTemporaryFileWithContent(fileName, data);
  }
}
