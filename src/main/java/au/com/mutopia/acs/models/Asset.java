package au.com.mutopia.acs.models;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;

/**
 * A file that has been uploladed or converted from another file.
 */
@Getter
@Setter
public class Asset {

  /** The {@link UUID} string that uniquely identifies the asset. */
  private UUID id = UUID.randomUUID();

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


  public void setData(Byte[] data) {
    setDataValue(data);
  }

  private void setDataValue(Byte[] data) {
    this.data = data;
    if (data != null) {
      setFileSize((long) getData().length);
    }
  }

}
