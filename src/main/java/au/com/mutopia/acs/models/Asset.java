package au.com.mutopia.acs.models;

import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;

import lombok.Getter;
import lombok.Setter;

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
  private Byte[] data;

  /** The MIME type of the underlying data. */
  private String mimeType;

  /** The filename of the asset uploaded or created. */
  private String fileName;

  /** The file size in bytes of the file. */
  private Long fileSize;


  public Asset() {}

  public Asset(Byte[] data, FormDataContentDisposition fileDetails) {
    setData(data);
    setFileName(fileDetails.getFileName());
    setFileSize(fileDetails.getSize());
  }

  public void setData(Byte[] data) {
    setDataValue(data);
  }

  private void setDataValue(Byte[] data) {
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
