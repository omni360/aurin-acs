package au.com.mutopia.acs.conversion;

import java.util.UUID;

import au.com.mutopia.acs.models.c3ml.C3mlData;
import au.com.mutopia.acs.models.c3ml.C3mlEntity;
import au.com.mutopia.acs.models.c3ml.Vertex3D;

import com.google.common.collect.ImmutableList;

/**
 * A {@link C3mlEntity} fixture with a broad range of features for testing. Contains one of each
 * type of entity (point, line, polygon, mesh), each with different styles and custom parameters.
 */
public class BroadC3mlFixture extends C3mlData {
  
  public BroadC3mlFixture() {
    setEntities(ImmutableList.of(buildPoint(), buildLine(), buildPolygon(), buildMesh()));
  }

  private C3mlEntity buildPoint() {
    C3mlEntity entity = new C3mlEntity(UUID.randomUUID().toString());
    entity.setName("Etihad Stadium");
    entity.addParameter("description", "Etihad Stadium in Docklands.");
    entity.setCoordinates(ImmutableList.of(new Vertex3D(37.81666683012415, 144.947531183432, 0.0)));
    entity.setColor(ImmutableList.of(255, 0, 0, 255));
    return entity;
  }

  private C3mlEntity buildLine() {
    C3mlEntity entity = new C3mlEntity(UUID.randomUUID().toString());
    entity.setName("Train Tracks");
    entity.addParameter("description",
        "A test segment of train track ending in Southern Cross Station.");
    Vertex3D a = new Vertex3D(-37.81168134092717, 144.9457316659238, 0);
    Vertex3D b = new Vertex3D(-37.81400168495323, 144.9487326064075, 0);
    Vertex3D c = new Vertex3D(-37.81611997711855, 144.9506817996335, 0);
    Vertex3D d = new Vertex3D(-37.81736651679314, 144.951826258271, 0);
    Vertex3D e = new Vertex3D(-37.81856014007646, 144.9527318758136, 0);
    entity.setCoordinates(ImmutableList.of(a, b, c, d, e));
    entity.setColor(ImmutableList.of(255, 127, 0, 255));
    return entity;
  }

  private C3mlEntity buildPolygon() {
    C3mlEntity entity = new C3mlEntity(UUID.randomUUID().toString());
    entity.setName("Melbourne CBD");
    entity.addParameter("description", "Footprint of the Melbourne CBD.");
    Vertex3D a = new Vertex3D(-37.81548625281237, 144.9750826126445, 0);
    Vertex3D b = new Vertex3D(-37.80735973465846, 144.9710060006769, 0);
    Vertex3D c = new Vertex3D(-37.81305802727754, 144.9512328118604, 0);
    Vertex3D d = new Vertex3D(-37.82116100732942, 144.9551041535812, 0);
    entity.setCoordinates(ImmutableList.of(a, b, c, d, a));
    entity.setColor(ImmutableList.of(0, 0, 255, 255));
    return entity;
  }

  private C3mlEntity buildMesh() {
    C3mlEntity entity = new C3mlEntity(UUID.randomUUID().toString());
    entity.setName("St Patrick's Cathedral");
    entity.addParameter("description", "St Patrick's Cathedral in Melbourne.");
    
    return entity;

  }

}
