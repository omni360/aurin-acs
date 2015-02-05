package au.com.mutopia.acs.conversion.impl;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.vecmath.Matrix4d;

import lombok.extern.log4j.Log4j;

import org.apache.commons.io.IOUtils;
import org.xml.sax.SAXException;

import au.com.mutopia.acs.conversion.Converter;
import au.com.mutopia.acs.exceptions.ConversionException;
import au.com.mutopia.acs.exceptions.InvalidColladaException;
import au.com.mutopia.acs.models.Asset;
import au.com.mutopia.acs.models.c3ml.C3mlEntity;
import au.com.mutopia.acs.models.c3ml.C3mlEntityType;
import au.com.mutopia.acs.util.ColladaExtraReader;
import au.com.mutopia.acs.util.CollectionUtils;
import au.com.mutopia.acs.util.mesh.VecMathUtil;

import com.dddviewr.collada.Collada;
import com.dddviewr.collada.Input;
import com.dddviewr.collada.effects.Effect;
import com.dddviewr.collada.effects.EffectAttribute;
import com.dddviewr.collada.effects.EffectMaterial;
import com.dddviewr.collada.effects.LibraryEffects;
import com.dddviewr.collada.geometry.Geometry;
import com.dddviewr.collada.geometry.LibraryGeometries;
import com.dddviewr.collada.geometry.PolyList;
import com.dddviewr.collada.geometry.Primitives;
import com.dddviewr.collada.geometry.Triangles;
import com.dddviewr.collada.materials.LibraryMaterials;
import com.dddviewr.collada.materials.Material;
import com.dddviewr.collada.nodes.LibraryNodes;
import com.dddviewr.collada.nodes.Node;
import com.dddviewr.collada.visualscene.BaseXform;
import com.dddviewr.collada.visualscene.InstanceGeometry;
import com.dddviewr.collada.visualscene.InstanceMaterial;
import com.dddviewr.collada.visualscene.InstanceNode;
import com.dddviewr.collada.visualscene.Matrix;
import com.dddviewr.collada.visualscene.VisualScene;
import com.google.common.collect.Lists;
import com.google.common.primitives.Floats;

/**
 * Converts COLLADA files into collections of {@link C3mlEntity} objects.
 */
@Log4j
public class ColladaConverter implements Converter {

  /** Default Geographic location for COLLADA model. */
  private static final List<Double> defaultGeolocation = Lists.newArrayList(0.0, 0.0, 0.0);

  /** Float array of identity matrix used by COLLADA. */
  private static final float[] IDENTITY = {1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f,
      0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f};

  /** String identifier for vertex inputs. */
  private static final String VERTEX_STRING = "VERTEX";

  /** Constants for COLLADA up-axis field. */
  private static final String X_UP = "X_UP", Y_UP = "Y_UP", Z_UP = "Z_UP";

  /** String label specifying the axis of upward direction of COLLADA model. */
  private String upAxis;

  /** The unit of meter measurement to be applied to the COLLADA model. */
  private double unitMeter = 1.0;

  /** The top level hierarchy visual scene of the COLLADA model, contains hierarchy of nodes. */
  private VisualScene visualScene;

  /**
   * Map of COLLADA nodes IDs and respective nodes.
   */
  private Map<String, Node> nodeMap = new HashMap<>();

  /**
   * Map of COLLADA geometry IDs and respective geometries.
   */
  private Map<String, Geometry> geometryMap = new HashMap<>();

  /**
   * Map of COLLADA material IDs and respective materials.
   */
  private Map<String, Material> materialMap = new HashMap<>();

  /**
   * Map of COLLADA effect IDs and respective effects.
   */
  private Map<String, Effect> effectMap = new HashMap<>();

  /**
   * Map of COLLADA node IDs to maps of <code>{paramName: paramValue}</code> for custom properties
   * stored in the <code>&lt;extra&gt;</code> tags.
   */
  private Map<String, Map<String, String>> customParamMap = new HashMap<>();

  /**
   * The rotation to be applied on the whole COLLADA model.
   */
  private List<Double> rotation = null;

  /**
   * The scale to be applied on the whole COLLADA model.
   */
  private List<Double> scale = null;

  /**
   * The geographic location to be applied on the whole COLLADA model.
   */
  private List<Double> geoLocation = null;

  /**
   * Converts the COLLADA {@link Asset} into a list of {@link C3mlEntity}s.
   *
   * @param asset An {@link Asset} representing a COLLADA file.
   * @return A {@link C3mlEntity} containing the same information as the COLLADA file.
   * @throws ConversionException if the conversion failed.
   */
  public List<C3mlEntity> convert(Asset asset) throws ConversionException {
    log.debug("Converting COLLADA asset " + asset.getName() + "...");
    try {
      return convert(asset.getTemporaryFile());
    } catch (IOException e) {
      throw new ConversionException("Error reading content from COLLADA file.");
    }
  }

  /**
   * Converts the COLLADA {@link Asset} into a list of {@link C3mlEntity}s, and apply global
   * transformations on each converted geometries.
   *
   * @param colladaFile The COLLADA file containing geometries to be converted.
   * @param rotation The global rotation to be applied on the geometries, in X, Y and Z axis.
   * @param scale The global scale to be applied on the geometries, in X, Y and Z axis.
   * @param geoLocation The global geographic location to be applied (lon, lat, alt).
   * @return A {@link C3mlEntity} with global transformations applied to the COLLADA file.
   * @throws ConversionException if the conversion failed.
   */
  public List<C3mlEntity> convert(File colladaFile, List<Double> rotation, List<Double> scale,
      List<Double> geoLocation) throws ConversionException {
    this.rotation = rotation;
    this.scale = scale;
    this.geoLocation = geoLocation;
    return convert(colladaFile);
  }

  /**
   * Converts the COLLADA file into a list of {@link C3mlEntity}s.
   *
   * @param colladaFile The COLLADA file containing geometries to be converted.
   * @return A {@link C3mlEntity} containing the same information as the COLLADA file.
   * @throws ConversionException if the conversion failed.
   */
  public List<C3mlEntity> convert(File colladaFile) throws ConversionException {
    try {
      populateLibraryMaps(colladaFile.getPath());
      populateCustomParameterMap(colladaFile);
      return buildEntities();
    } catch (IOException | SAXException | InvalidColladaException e) {
      throw new ConversionException("Error reading content from COLLADA file.");
    }
  }

  /**
   * Populate all mappings of COLLADA element IDs with their respective element.
   *
   * @param filePath The path to COLLADA file.
   * @throws IOException if the COLLADA file cannot be read.
   * @throws SAXException if the XML in the COLLADA file cannot be parsed.
   */
  private void populateLibraryMaps(String filePath) throws IOException, SAXException {
    Collada collada = Collada.readFile(filePath);
    visualScene =
        collada.getLibraryVisualScenes().getScene(
            collada.getScene().getInstanceVisualScene().getUrl());
    unitMeter = collada.getUnit().getMeter();
    upAxis = collada.getUpAxis();
    LibraryEffects libraryEffects = collada.getLibraryEffects();
    if (libraryEffects != null) {
      List<Effect> effects = libraryEffects.getEffects();
      if (!CollectionUtils.isNullOrEmpty(effects)) {
        for (Effect effect : effects) {
          effectMap.put(effect.getId(), effect);
        }
      }
    }
    LibraryGeometries libraryGeometries = collada.getLibraryGeometries();
    if (libraryGeometries != null) {
      List<Geometry> geometries = libraryGeometries.getGeometries();
      if (!CollectionUtils.isNullOrEmpty(geometries)) {
        for (Geometry geometry : geometries) {
          geometryMap.put(geometry.getId(), geometry);
        }
      }
    }
    LibraryMaterials libraryMaterials = collada.getLibraryMaterials();
    if (libraryMaterials != null) {
      List<Material> materials = libraryMaterials.getMaterials();
      if (!CollectionUtils.isNullOrEmpty(materials)) {
        for (Material material : materials) {
          materialMap.put(material.getId(), material);
        }
      }
    }
    LibraryNodes libraryNodes = collada.getLibraryNodes();
    if (libraryNodes != null) {
      List<Node> nodes = libraryNodes.getNodes();
      if (!CollectionUtils.isNullOrEmpty(nodes)) {
        for (Node node : nodes) {
          nodeMap.put(node.getId(), node);
        }
      }
    }
  }

  /**
   * Populates entity with parameters from COLLADA's <code>&lt;extra&gt;</code> tag.
   *
   * @param entity The {@link C3mlEntity} object.
   * @param extra The COLLADA's <code>&lt;extra&gt;</code> element, mapping parameter names and
   *        values.
   * @return Map of node IDs to maps of <code>{paramName: paramValue}</code> for custom properties.
   * 
   * @see <a href="https://collada.org/mediawiki/index.php/Extension#Extension_by_addition">COLLADA
   *      docs for the &lt;extra&gt; tag</a>
   */
  private void populateCustomParameterMap(File daeFile) {
    try {
      String colladaXml = IOUtils.toString(new FileInputStream(daeFile));
      customParamMap = new ColladaExtraReader().getExtraProperties(colladaXml);
    } catch (IOException e) {
      log.error("Failed to populate custom parameters from DAE file " + daeFile.getAbsolutePath(),
          e);
      return;
    }
  }


  /**
   * Gets the node from the LibraryNodes that matches given node reference ID.
   *
   * @param id The node reference ID.
   * @return Node if the library contains a node with the given ID.
   * @throws InvalidColladaException if the given ID doesn't exist in the COLLADA nodes.
   */
  private Node getNodeFromLibraryNodes(String id) throws InvalidColladaException {
    Node node = nodeMap.get(id.replace("#", ""));
    if (node == null) {
      throw new InvalidColladaException("Unable to find node with ID: " + id);
    }
    return node;
  }

  /**
   * Gets the geometry from LibraryGeometries that matches given geometry reference ID.
   *
   * @param id The geometry reference ID.
   * @return GeoLeaf The GeoLeaf (geometry) mapped with the given ID.
   * @throws InvalidColladaException if the given ID doesn't exist in the COLLADA geometries.
   */
  private Geometry getGeomFromLibraryGeometries(String id) throws InvalidColladaException {
    Geometry geometry = geometryMap.get(id.replace("#", ""));
    if (geometry == null) {
      throw new InvalidColladaException("Unable to find geometry with ID: " + id);
    }
    return geometry;
  }

  /**
   * Gets the color from LibraryMaterials that matches given material reference ID.
   *
   * @param id The material reference ID.
   * @return The Color referenced by the material ID, DEFAULT_COLOR if no match was found.
   */
  private Color getColorFromLibraryMaterials(String id) {
    Material material = materialMap.get(id.replace("#", ""));
    float[] colorData = null;
    if (material.getInstanceEffect() != null) {
      colorData = getColorFromLibraryEffects(material.getInstanceEffect().getUrl());
    }
    if (colorData == null) {
      return DEFAULT_COLOR;
    }
    return getColorFromColorData(colorData);
  }

  /**
   * Gets the color from LibraryEffects that matches given effect reference ID.
   *
   * @param id The effect reference ID.
   * @return The Color referenced by the effect ID, null if no match was found.
   */
  private float[] getColorFromLibraryEffects(String id) {
    Effect effect = effectMap.get(id.replace("#", ""));
    EffectMaterial effectMaterial = effect.getEffectMaterial();
    if (effectMaterial != null) {
      EffectAttribute diffuse = effectMaterial.getDiffuse();
      if (diffuse != null) return diffuse.getData();
    }
    return null;
  }

  /**
   * Gets Color from float array color data.
   *
   * @param colorData The color data in float array.
   * @return The color converted from float array, DEFAULT_COLOR if colorData is invalid.
   */
  private Color getColorFromColorData(float[] colorData) {
    if (colorData.length == 4) {
      return new Color(colorData[0], colorData[1], colorData[2], colorData[3]);
    } else if (colorData.length == 3) {
      return new Color(colorData[0], colorData[1], colorData[2]);
    }
    return DEFAULT_COLOR;
  }

  /**
   * Builds a list of {@link C3mlEntity} from the COLLADA file. Each entity represents the top-level
   * entity in their respective hierarchy of the 3D object model.
   *
   * @return A list of {@link C3mlEntity} representing the model in COLLADA file.
   * @throws InvalidColladaException if the given ID doesn't exist in the COLLADA.
   */
  private List<C3mlEntity> buildEntities() throws InvalidColladaException {
    List<C3mlEntity> c3mlEntities = Lists.newArrayList();
    for (Node node : visualScene.getNodes()) {
      Matrix matrix = new Matrix("identity");
      matrix.setData(IDENTITY);
      C3mlEntity c3mlEntity = buildEntityFromNode(node, matrix);
      if (c3mlEntity != null) {
        c3mlEntities.add(c3mlEntity);
      }
    }
    return c3mlEntities;
  }

  /**
   * Builds a list of {@link C3mlEntity} from a node.
   *
   * @param node A node from COLLADA file, represents a point on the COLLADA scene.
   * @param parentMatrix The matrix transformation from parent node.
   * @return A list of {@link C3mlEntity} representing the COLLADA node.
   * @throws InvalidColladaException if required COLLADA data is missing.
   */
  private C3mlEntity buildEntityFromNode(Node node, Matrix parentMatrix)
      throws InvalidColladaException {
    C3mlEntity c3mlEntity = new C3mlEntity();
    c3mlEntity.setName(node.getName());
    Matrix currentMatrix = getCurrentMatrix(node, parentMatrix);

    // Calculate mesh from geometry attached to this node.
    List<InstanceGeometry> instanceGeoms = node.getInstanceGeometry();
    for (InstanceGeometry instanceGeom : instanceGeoms) {
      Geometry geom = getGeomFromLibraryGeometries(instanceGeom.getUrl());
      List<InstanceMaterial> instanceMaterials = instanceGeom.getInstanceMaterials();
      Color colorData;
      if (CollectionUtils.isNullOrEmpty(instanceMaterials)) {
        colorData = DEFAULT_COLOR;
      } else {
        colorData = getColorFromLibraryMaterials(instanceMaterials.get(0).getTarget());
      }
      c3mlEntity.addChild(buildEntityFromGeometry(geom, currentMatrix, colorData));
    }
    InstanceNode instanceNode = node.getInstanceNode();
    if (instanceNode != null) {
      Node childNode = getNodeFromLibraryNodes(instanceNode.getUrl());
      c3mlEntity.addChild(buildEntityFromNode(childNode, currentMatrix));
    }

    // Add any custom parameters that were extracted.
    if (customParamMap.containsKey(node.getId())) {
      for (Map.Entry<String, String> params : customParamMap.get(node.getId()).entrySet()) {
        c3mlEntity.addParameter(params.getKey(), params.getValue());
      }
    }

    // Propagate to children nodes.
    List<Node> childNodes = node.getChildNodes();
    for (Node childNode : childNodes) {
      c3mlEntity.addChild(buildEntityFromNode(childNode, currentMatrix));
    }
    return c3mlEntity;
  }

  /**
   * Builds a {@link C3mlEntity} from {@link Geometry}, {@link Matrix} and {@link Color}.
   *
   * @param geom The COLLADA geometry representing the shape of the model.
   * @param matrix The matrix transformation to be applied on the model.
   * @param color The color of the model.
   * @return The created {@link C3mlEntity}.
   */
  private C3mlEntity buildEntityFromGeometry(Geometry geom, Matrix matrix, Color color) {
    List<Primitives> primitives = geom.getMesh().getPrimitives();
    float[] positions = null;
    float[] normals = null;
    // input indices used by COLLADA.
    List<Integer> inputIndices = Lists.newArrayList();
    C3mlEntityType type = null;
    if (primitives.size() < 1) {
      return null;
    }
    for (Primitives primitive : geom.getMesh().getPrimitives()) {
      if (primitive.getClass().equals(Triangles.class)) {
        type = C3mlEntityType.MESH;
        positions = geom.getMesh().getPositionData();
        normals = geom.getMesh().getNormalData();
        inputIndices.addAll(getVerticesFromTriangles(primitive));
      } else if (primitive.getClass().equals(PolyList.class)) {
        // TODO(Brandon) extract from polylist, requires polygon triangulation.
      }
    }
    if (type == null) {
      return null;
    }
    C3mlEntity c3mlEntity = new C3mlEntity();
    String name = geom.getName();
    c3mlEntity.setName((name != null) ? name : geom.getId());
    c3mlEntity.setColorData(color);
    if (type.equals(C3mlEntityType.MESH)) {
      buildEntityFromMesh(c3mlEntity, positions, normals, inputIndices, matrix);
    }
    return c3mlEntity;
  }

  /**
   * Builds a {@link C3mlEntity} from Mesh data (positions, normals and triangle indices).
   *
   * @param entity The {@link C3mlEntity}.
   * @param positions The list of vertices representing the mesh.
   * @param normals The list of normals for each vertex.
   * @param inputIndices The list of triangle indices that builds the mesh.
   * @param matrix The matrix transformation to be applied on the mesh.
   */
  private void buildEntityFromMesh(C3mlEntity entity, float[] positions, float[] normals,
      List<Integer> inputIndices, Matrix matrix) {
    List<Double> positionsAsDoubles = CollectionUtils.doublesFromFloats(Floats.asList(positions));
    List<Double> normalAsDoubles = CollectionUtils.doublesFromFloats(Floats.asList(normals));

    Matrix4d matrix4d = VecMathUtil.matrix4dFromFloats(matrix.getData());
    List<Double> transformedPositions =
        VecMathUtil.transformMeshPositions(positionsAsDoubles, matrix4d);
    List<Double> transformedNormal = VecMathUtil.transformMeshNormals(normalAsDoubles, matrix4d);

    // Rotate mesh to ensure that positive Z axis is facing up. Mesh may have X or Y axis facing
    // upwards in COLLADA exporting tools.
    Matrix4d upAxisMatrix = convertUpAxisMatrix();
    List<Double> upAxisPositions =
        VecMathUtil.transformMeshPositions(transformedPositions, upAxisMatrix);
    List<Double> upAxisNormals = VecMathUtil.transformMeshNormals(transformedNormal, upAxisMatrix);

    Matrix4d scaleMatrix = VecMathUtil.createScaleMatrix(unitMeter);
    List<Double> scaledPositions = VecMathUtil.transformMeshPositions(upAxisPositions, scaleMatrix);
    List<Double> scaledNormals = VecMathUtil.transformMeshNormals(upAxisNormals, scaleMatrix);

    entity.setType(C3mlEntityType.MESH);
    entity.setPositions(scaledPositions);
    entity.setNormals(scaledNormals);
    entity.setTriangles(inputIndices);
    entity.setGeoLocation(defaultGeolocation);

    // Apply global transformations to the model if exists.
    if (rotation != null) entity.setRotation(rotation);
    if (scale != null) entity.setScale(scale);
    if (geoLocation != null) entity.setGeoLocation(geoLocation);
  }

  /**
   * Get the current matrix transformation for the node.
   *
   * @param node Current node to extract geometry from.
   * @param parentMatrix The matrix transformation inherited from parent node.
   * @return The final matrix transformation obtained from current and parent node.
   */
  private Matrix getCurrentMatrix(Node node, Matrix parentMatrix) {
    Matrix currentMatrix = null;
    // A bug from dae4j.
    // Currently node.getMatrix() returns null despite having matrix.
    for (BaseXform xform : node.getXforms()) {
      if (xform.getClass().equals(Matrix.class)) {
        currentMatrix = (Matrix) xform;
      }
    }
    currentMatrix = combineMatrices(currentMatrix, parentMatrix);

    return currentMatrix;
  }

  /**
   * Gets the matrix from combination of two matrices.
   *
   * @param matrix1 The first matrix.
   * @param matrix2 The second matrix.
   * @return The resultant matrix from combination of both matrices.
   */
  private Matrix combineMatrices(Matrix matrix1, Matrix matrix2) {
    if (matrix1 == null) {
      matrix1 = new Matrix("identity");
      matrix1.setData(IDENTITY);
    }
    if (matrix2 == null) {
      matrix2 = new Matrix("identity");
      matrix2.setData(IDENTITY);
    }
    Matrix4d matrix4d_1 = VecMathUtil.matrix4dFromFloats(matrix1.getData());
    Matrix4d matrix4d_2 = VecMathUtil.matrix4dFromFloats(matrix2.getData());
    matrix4d_1.transpose();
    matrix4d_2.transpose();
    matrix4d_1.mul(matrix4d_2);
    matrix4d_1.transpose();
    matrix1.setData(Floats.toArray(VecMathUtil.matrix4dToFloats(matrix4d_1)));
    return matrix1;
  }

  /**
   * @return A 4x4 matrix that can be used to transform the COLLADA geometry so that the positive Z
   *         axis is facing up.
   */
  private Matrix4d convertUpAxisMatrix() {
    if (upAxis.equals(X_UP)) {
      Matrix4d xAxisRotationMatrix = VecMathUtil.createYAxisRotationMatrix(-90);
      Matrix4d zAxisRotationMatrix = VecMathUtil.createZAxisRotationMatrix(180);
      xAxisRotationMatrix.mul(zAxisRotationMatrix);
      return xAxisRotationMatrix;
    } else if (upAxis.equals(Y_UP)) {
      Matrix4d xAxisRotationMatrix = VecMathUtil.createXAxisRotationMatrix(-90);
      Matrix4d zAxisRotationMatrix = VecMathUtil.createZAxisRotationMatrix(180);
      xAxisRotationMatrix.mul(zAxisRotationMatrix);
      return xAxisRotationMatrix;
    }
    return VecMathUtil.createIdentityMatrix();
  }

  /**
   * Gets the list of vertex indices from triangles primitive.
   *
   * @param triangles The COLLADA triangles primitive.
   * @return The list of vertex indices.
   */
  private List<Integer> getVerticesFromTriangles(Primitives triangles) {
    List<Integer> vertices = Lists.newArrayList();
    int stride = triangles.getInputs().size();
    int offset = getOffsetFromInputs(triangles.getInputs(), VERTEX_STRING);
    int[] data = triangles.getData();
    for (int i = 0; i < data.length; i += stride) {
      vertices.add(data[i + offset]);
    }
    return vertices;
  }

  /**
   * Gets the input offset from a list of inputs.
   *
   * @param inputs The list of inputs to COLLADA's primitive source.
   * @param semantic The string defining the type of input.
   * @return The input offset.
   */
  private int getOffsetFromInputs(List<Input> inputs, String semantic) {
    for (Input input : inputs) {
      if (input.getSemantic().equals(semantic)) {
        return input.getOffset();
      }
    }
    return 0;
  }

}
