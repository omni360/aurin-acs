package au.com.mutopia.acs.conversion;

import au.com.mutopia.acs.models.c3ml.C3mlData;
import au.com.mutopia.acs.models.c3ml.C3mlEntity;
import au.com.mutopia.acs.models.c3ml.C3mlEntityType;
import au.com.mutopia.acs.models.c3ml.Vertex3D;
import com.google.common.collect.ImmutableList;

import java.util.UUID;

/**
 * A simple {@link C3mlEntity} fixture for testing. Contains a single entity with no style
 * information or non-standard parameters.
 */
public class SimpleC3mlFixture extends C3mlData {

  public SimpleC3mlFixture() {
    setEntities(ImmutableList.of(buildPolygon()));
  }

  private C3mlEntity buildPolygon() {
    C3mlEntity entity = new C3mlEntity(UUID.randomUUID().toString());
    entity.setName("melbourne_cbd");
    Vertex3D a = new Vertex3D(-37.81548625281237, 144.9750826126445, 0);
    Vertex3D b = new Vertex3D(-37.80735973465846, 144.9710060006769, 0);
    Vertex3D c = new Vertex3D(-37.81305802727754, 144.9512328118604, 0);
    Vertex3D d = new Vertex3D(-37.82116100732942, 144.9551041535812, 0);
    entity.setCoordinates(ImmutableList.of(a, b, c, d, a));
    entity.setColor(ImmutableList.of(255, 255, 255, 255));
    entity.setType(C3mlEntityType.POLYGON);
    return entity;
  }

}
