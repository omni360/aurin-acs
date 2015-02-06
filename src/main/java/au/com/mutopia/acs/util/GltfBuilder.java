package au.com.mutopia.acs.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.log4j.Log4j;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import au.com.mutopia.acs.models.c3ml.C3mlEntity;
import au.com.mutopia.acs.models.c3ml.C3mlEntityType;
import flexjson.JSONDeserializer;

/**
 * Utility methods for constructing glTF meshes.
 */
@Log4j
public class GltfBuilder {

  /** The extensions expected to be seen on image files. */
  private static final String[] IMAGE_EXTS = new String[] {"jpg", "jpeg", "png", "gif", "tif",
      "JPG", "JPEG", "PNG", "GIF", "TIF"};

  /**
   * Creates a {@link C3mlEntity} with merged glTF geometry from all of the COLLADA nodes.
   * 
   * @param colladaFile The COLLADA file to convert.
   * @param rotation The rotation to be applied on the whole COLLADA model.
   * @param scale The scale to be applied on the whole COLLADA model.
   * @param geoLocation The geographic location to be applied on the whole COLLADA model.
   * @return An entity with all of the asset's geometry merged into a glTF mesh.
   * @throws IOException if the glTF file couldn't be created.
   */
  public C3mlEntity convertMerged(File colladaFile, List<Double> rotation, List<Double> scale,
      List<Double> geoLocation) throws IOException {
    C3mlEntity gltfEntity = new C3mlEntity();
    gltfEntity.setName(FilenameUtils.removeExtension(colladaFile.getName()));
    gltfEntity.setType(C3mlEntityType.MESH);
    String gltf = IOUtils.toString(Collada2Gltf.convertToGltf(colladaFile).toURI());
    Map<String, Object> gltfMap = new JSONDeserializer<Map<String, Object>>().deserialize(gltf);
    gltfEntity.setGltfData(gltfMap);

    replaceImageUris(gltfMap, buildImageMap(colladaFile));

    // Apply global transformations if they exist.
    if (rotation != null) gltfEntity.setRotation(rotation);
    if (scale != null) gltfEntity.setScale(scale);
    if (geoLocation != null) gltfEntity.setGeoLocation(geoLocation);
    return gltfEntity;
  }

  /**
   * Constructs a map of paths to image files (relative to the given COLLADA file) to Base64-encoded
   * data URI strings of their contents.
   * 
   * @param colladaFile The file to search for images relative to.
   * @return The constructed image map.
   */
  private Map<String, String> buildImageMap(File colladaFile) {
    Map<String, String> imageMap = new HashMap<>();

    for (File imageFile : FileUtils.listFiles(colladaFile.getParentFile(), IMAGE_EXTS, true)) {
      try {
        byte[] imageBytes = IOUtils.toByteArray(new FileInputStream(imageFile));
        String encodedData = Base64.encodeBase64String(imageBytes);
        String dataUri = "data:application/octet-stream;base64," + encodedData;
        String fileUrl = URLEncoder.encode(imageFile.getName(), "UTF-8").replace("+", "%20");
        imageMap.put("images/" + fileUrl, dataUri);
      } catch (IOException e) {
        log.warn("Couldn't read glTF image URI " + imageFile.getAbsolutePath(), e);
      }
    }
    return imageMap;
  }

  /**
   * Replaces relative image URIs with data URIs. The glTF map is updated in place.
   * 
   * @param gltfMap The glTF document deserialized into a map.
   * @param imageMap A map of image URIs to Base64-encoded data URIs.
   */
  private void replaceImageUris(Map<String, Object> gltfMap, Map<String, String> imageMap) {
    @SuppressWarnings("unchecked")
    Map<String, Map<String, String>> images =
        (Map<String, Map<String, String>>) gltfMap.get("images");
    if (images != null) {
      for (String imageId : images.keySet()) {
        String uri = images.get(imageId).get("uri");
        if (!uri.startsWith("data:") && imageMap.containsKey(uri)) {
          images.get(imageId).put("uri", imageMap.get(uri));
        }
      }
    }
  }

}
