package au.com.mutopia.acs.resources;

import au.com.mutopia.acs.conversion.Converter;
import au.com.mutopia.acs.conversion.ConverterMap;
import au.com.mutopia.acs.conversion.output.KmzWriter;
import au.com.mutopia.acs.exceptions.ConversionException;
import au.com.mutopia.acs.models.Asset;
import au.com.mutopia.acs.models.c3ml.C3mlData;
import au.com.mutopia.acs.models.c3ml.C3mlEntity;
import com.google.inject.Inject;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;
import lombok.extern.log4j.Log4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.time.StopWatch;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * Implements the REST API for file conversion.
 */
@Path("/convert")
@Log4j
public class ConversionResource {

  /** A map of {@link Converter} objects for each available input format. */
  private ConverterMap converters;
  private static final String KMZ_MIME_TYPE = "application/vnd.google-earth.kmz";

  /**
   * Creates the resource with the map of available converters injected.
   *
   * @param converters A map of each supported format to the {@link Converter} used to convert files
   * of that format.
   */
  @Inject
  public ConversionResource(ConverterMap converters) {
    this.converters = converters;
  }

  /**
   * Converts the given file to the requested target format.
   *
   * @param inputStream A stream of the uploaded file data.
   * @param fileDetail Metadata about the uploaded file.
   * @param merge Whether to merge all entities into one (if possible).
   * @return The generated C3ML document.
   * @throws ConversionException if the conversion failed.
   */
  @POST
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Produces(MediaType.APPLICATION_JSON)
  public C3mlData convert(@FormDataParam("file") InputStream inputStream,
      @FormDataParam("file") FormDataContentDisposition fileDetail,
      @DefaultValue("false") @FormDataParam("merge") boolean merge) throws ConversionException {
    Byte[] data;
    try {
      data = ArrayUtils.toObject(IOUtils.toByteArray(inputStream));
    } catch (IOException e) {
      throw new WebApplicationException(new ConversionException("Failed to read file data", e));
    }

    Asset asset = new Asset(data, fileDetail);

    log.debug("Converting " + asset + "...");
    StopWatch stopWatch = new StopWatch();
    stopWatch.start();

    // Convert the data.
    Converter converter = this.converters.get(asset.getFormat());
    List<C3mlEntity> entities = converter.convert(asset, merge);

    stopWatch.stop();
    log.debug("Conversion of " + asset + " complete (" + stopWatch.getTime() / 1000.0 + " secs)");

    C3mlData c3ml = new C3mlData(entities);
    return c3ml;
  }

  /**
   * Converts the given {@link C3mlData} document into a KMZ file, and returns the bytes.
   *
   * @param data The C3ML to export.
   * @return The bytes of the resulting KMZ file.
   * @throws ConversionException if the export fails.
   */
  @POST
  @Path("/export")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  public Response export(C3mlData data) throws ConversionException {
    File kmzFile = new KmzWriter().convert(data);
    try {
      final byte[] byteData = IOUtils.toByteArray(new FileInputStream(kmzFile));
      StreamingOutput output = new StreamingOutput() {
        @Override
        public void write(OutputStream output) throws IOException, WebApplicationException {
          output.write(byteData);
        }
      };
      return Response.ok(output).type(KMZ_MIME_TYPE)
          .header("content-disposition", "attachment; filename = " + kmzFile.getName()).build();
    } catch (IOException e) {
      throw new ConversionException("Failed to read KMZ file", e);
    }
  }

}
