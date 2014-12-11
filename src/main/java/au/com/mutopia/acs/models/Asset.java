package au.com.mutopia.acs.models;

import java.io.File;
import java.io.IOException;

import lombok.Getter;
import lombok.Setter;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sun.jersey.core.header.FormDataContentDisposition;

/**
 * A file that has been uploladed or converted from another file.
 */
@Getter
@Setter
public class Asset {

  /** A name describing the asset. */
  private String name;

  /** The binary content of the file. */
  @JsonIgnore
  private byte[] data;

  /** The MIME type of the underlying data. */
  private String mimeType;

  /** The filename of the asset uploaded or created. */
  private String fileName;

  /** The file size in bytes of the file. */
  private Long fileSize;


  public Asset() {}

  public Asset(byte[] data, FormDataContentDisposition fileDetails) {
    setData(data);
    setFileName(fileDetails.getFileName());
    setFileSize(fileDetails.getSize());
  }

  public Asset(Byte[] data, FormDataContentDisposition fileDetails) {
    this(ArrayUtils.toPrimitive(data), fileDetails);
  }

  public Asset(File file) throws IOException {
    setData(FileUtils.readFileToByteArray(file));
    setFileName(file.getName());
    setFileSize(file.length());
  }

  public void setData(byte[] data) {
    setDataValue(data);
  }

  private void setDataValue(byte[] data) {
    this.data = data;
    if (data != null) {
      setFileSize((long) getData().length);
    }
  }

  @Override
  public String toString() {
    return String.format("Asset[%s (%s)]", getFileName(),
        FileUtils.byteCountToDisplaySize(getFileSize()));
  }
}
