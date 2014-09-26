package au.com.mutopia.acs.resources;

import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;

import au.com.mutopia.acs.models.Asset;

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;

@Path("/assets")
public class AssetsResource {

  /**
   * Uploads data and creates a new asset.
   *
   * @param uploadedInputStream An input stream to read the upload data.
   * @param fileDetail The details of the file being uploaded.
   * @return The asset that was constructed after the upload.
   * @throws IOException
   */
  @POST
  @Path("/upload")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Produces(MediaType.APPLICATION_JSON)
  public Asset uploadAndCreate(@FormDataParam("file") InputStream uploadedInputStream,
      @FormDataParam("file") FormDataContentDisposition fileDetail) throws IOException {
    Asset asset = new Asset();

    asset.setData(ArrayUtils.toObject(IOUtils.toByteArray(uploadedInputStream)));
    asset.setFileName(fileDetail.getFileName());
    asset.setName(fileDetail.getFileName());

    // TODO(orlade): Create and save.

    return asset;

    // import shapefile
    // only request certain parameters
    // send file to server
    // convert async
    // poll -> progress update
    //
  }

  /**
   * Performs the given ACE request.
   *
   * @param json The request which contains a set of ACE actions to perform, such as loads,
   *        transformations and conversions.
   * @return A response with the job ID corresponding to the Synthesis request.
   * @throws DaoException
   */
  // @POST
  // @Path("/synthesize")
  // public String synthesize(String json) {
  // log.debug("json: " + json);
  // Map<String, Object> request = toMap(json);
  // log.debug("Convert request: " + request);
  //
  // String userId = authenticationService.getCurrentUser().getIdString();
  // String jobId = assetService.synthesize(request, null, userId);
  //
  // Map<String, Object> response = Maps.newHashMap();
  // response.put("jobId", jobId);
  // return getSerializer().deepSerialize(response);
  // }

}
