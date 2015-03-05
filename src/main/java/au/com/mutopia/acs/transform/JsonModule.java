package au.com.mutopia.acs.transform;

import au.com.mutopia.acs.models.c3ml.Vertex3D;

import com.fasterxml.jackson.core.json.PackageVersion;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.inject.Inject;

/**
 * Registers serializers and deserializers for C3ML-related data.
 */
public class JsonModule extends SimpleModule {

  private static final long serialVersionUID = 1L;

  @Inject
  public JsonModule() {
    super(PackageVersion.VERSION);

    addDeserializer(Vertex3D.class, new VertexDeserializer());
  }
}
