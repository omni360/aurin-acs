package au.com.mutopia.acs.conversion.output;

import java.util.List;

import au.com.mutopia.acs.models.c3ml.C3mlEntity;
import au.com.mutopia.acs.models.c3ml.C3mlEntityType;

import com.dddviewr.collada.Collada;
import com.dddviewr.collada.FloatArray;
import com.dddviewr.collada.Input;
import com.dddviewr.collada.Source;
import com.dddviewr.collada.effects.Effect;
import com.dddviewr.collada.effects.EffectAttribute;
import com.dddviewr.collada.effects.Lambert;
import com.dddviewr.collada.effects.LibraryEffects;
import com.dddviewr.collada.geometry.Geometry;
import com.dddviewr.collada.geometry.LibraryGeometries;
import com.dddviewr.collada.geometry.Mesh;
import com.dddviewr.collada.geometry.Triangles;
import com.dddviewr.collada.geometry.Vertices;
import com.dddviewr.collada.materials.InstanceEffect;
import com.dddviewr.collada.materials.LibraryMaterials;
import com.dddviewr.collada.materials.Material;
import com.dddviewr.collada.nodes.LibraryNodes;
import com.dddviewr.collada.nodes.Node;
import com.dddviewr.collada.visualscene.InstanceGeometry;
import com.dddviewr.collada.visualscene.LibraryVisualScenes;

/**
 * Constructs COLLADA objects.
 */
public class ColladaBuilder {

  /**
   * Creates a new {@link Collada} object to populate with 3D geometries and hierarchy.
   */
  public Collada initCollada() {
    Collada collada = new Collada();
    // Initialise the libraries that may be used.
    collada.setAuthoringTool("AURIN ACS");
    collada.setLibraryGeometries(new LibraryGeometries());
    collada.setLibraryEffects(new LibraryEffects());
    collada.setLibraryMaterials(new LibraryMaterials());
    collada.setLibraryNodes(new LibraryNodes());
    collada.setLibraryVisualScenes(new LibraryVisualScenes());
    return collada;
  }

  /**
   * Creates a COLLADA node for the given {@link C3mlEntity}. Also adds the created {@link Node},
   * {@link Geometry}, {@link Material} and {@link Effect} to the relevant {@link Collada} document
   * libraries.
   *
   * @param entity The {@link C3mlEntity} of type {@link C3mlEntityType#MESH} to convert into
   *        COLLADA.
   * @param collada The {@link Collada} object to write the entity into.
   * @return The {@link Node} object created for the {@link Collada} document.
   */
  public Node buildColladaNode(C3mlEntity entity, Collada collada) {
    final String name = entity.getName();

    Geometry geometry = buildColladaGeometry(entity, name, collada);

    Node node = new Node("node-" + name, name, "sid", "type");
    node.addInstanceGeometry(new InstanceGeometry("#" + geometry.getId()));

    collada.getLibraryNodes().addNode(node);
    return node;
  }

  /**
   * Creates a COLLADA model for the mesh and properties {@link C3mlEntity}. Also adds the created
   * {@link Geometry}, {@link Material} and {@link Effect} to the relevant {@link Collada}
   * libraries.
   *
   * @param entity The {@link C3mlEntity} of type {@link C3mlEntityType#MESH} to convert into
   *        COLLADA.
   * @param name The name to use for the entity in the attributes.
   * @param collada The {@link Collada} object to write the entity into.
   * @return The {@link Geometry} object created for the {@link Collada} document. Note that other
   *         data may be written to the {@link Collada} document, such as scene nodes.
   */
  private Geometry buildColladaGeometry(C3mlEntity entity, String name, Collada collada) {
    // Build the basic geometry.
    Source positions = new Source("geom-" + name + "-positions", "positions");
    FloatArray positionsArray =
        new FloatArray("geom-" + name + "-positions-array", entity.getPositions().size());
    positions.setFloatArray(positionsArray);

    Source normals = new Source("geom-" + name + "-normals", "normals");
    FloatArray normalsArray =
        new FloatArray("geom-" + name + "-normals-array", entity.getNormals().size());
    normals.setFloatArray(normalsArray);

    Vertices vertices = new Vertices("geom-" + name + "-vertices");
    vertices.addInput(new Input("POSITION", "#" + positions.getId()));

    // Build the textured triangles.
    Material material = buildColorMaterial(entity.getColor(), name, collada);
    Triangles triangles = new Triangles(material.getName(), entity.getTriangles().size());

    // Create the mesh for the geometry.
    Mesh mesh = new Mesh();
    mesh.addSource(positions);
    mesh.addSource(normals);
    mesh.setVertices(vertices);
    mesh.addPrimitives(triangles);

    Geometry geometry = new Geometry("geom-" + name, name);
    geometry.setMesh(mesh);

    collada.getLibraryGeometries().addGeometry(geometry);
    return geometry;
  }

  /**
   * Creates a simple COLLADA {@link Effect} for a color.
   * 
   * @param rgba The color to build an {@link Effect} for as a list of RGBA values in the range [0,
   *        255].
   * @param name The name to use as the effect ID's suffix.
   * @param The {@link Collada} document containing the {@link LibraryEffects} to add the new
   *        {@link Effect} to.
   * @return The {@link Effect} object created.
   */
  private Effect buildColorEffect(List<Integer> rgba, String name, Collada collada) {
    EffectAttribute colorAttr = new EffectAttribute("color");
    colorAttr.setData(new float[] {rgba.get(0) / 255.0f, rgba.get(1) / 255.0f,
        rgba.get(2) / 255.0f, rgba.get(3) / 255.0f});
    Lambert lambert = new Lambert();
    lambert.setDiffuse(colorAttr);
    Effect effect = new Effect("fx-" + name);
    effect.setEffectMaterial(lambert);

    collada.getLibraryEffects().addEffect(effect);
    return effect;
  }

  /**
   * Creates a simple COLLADA {@link Material} for a color using
   * {@link #buildColorEffect(List, String)} to create the corresponding effect.
   * 
   * @param rgba The color to build an {@link Material} for as a list of RGBA values in the range
   *        [0, 255].
   * @param name The name to use as the material ID's suffix.
   * @param The {@link Collada} document containing the {@link LibraryMaterials} to add the new
   *        {@link Material} to.
   * @return The {@link Effect} object created.
   */
  private Material buildColorMaterial(List<Integer> rgba, String name, Collada collada) {
    Effect colorEffect = buildColorEffect(rgba, name, collada);
    Material material = new Material(name + "-material", name);
    material.setInstanceEffect(new InstanceEffect("#" + colorEffect.getId()));

    collada.getLibraryMaterials().addMaterial(material);
    return material;
  }

}
