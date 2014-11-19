package au.com.mutopia.acs.resources;

import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;

import lombok.extern.log4j.Log4j;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;

import au.com.mutopia.acs.conversion.Converter;
import au.com.mutopia.acs.conversion.ConverterMap;
import au.com.mutopia.acs.exceptions.ConversionException;
import au.com.mutopia.acs.models.Asset;
import au.com.mutopia.acs.models.c3ml.C3mlEntity;

import com.google.inject.Inject;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;

@Path("/convert")
@Log4j
public class ConversionResource {

  private ConverterMap converters;

  @Inject
  public ConversionResource(ConverterMap converters) {
    this.converters = converters;
  }

  /**
   * Converts the given file to the requested target format.
   * 
   * @param inputStream A stream of the uploaded file data.
   * @param fileDetail Metadata about the uploaded file.
   * @param targetFormat The format to convert the uploaded file to.
   * @return The generated C3ML document.
   */
  @POST
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Produces(MediaType.APPLICATION_JSON)
  public C3mlEntity synthesize(@FormDataParam("file") InputStream inputStream,
      @FormDataParam("file") FormDataContentDisposition fileDetail) {
    Byte[] data;
    try {
      data = ArrayUtils.toObject(IOUtils.toByteArray(inputStream));
    } catch (IOException e) {
      throw new WebApplicationException(new ConversionException("Failed to read file data", e));
    }

    Asset asset = new Asset(data, fileDetail);

    log.debug("Converting " + asset + "...");

    Converter converter = this.converters.get(asset.getMimeType());
    return converter.convert(asset);
  }

}
