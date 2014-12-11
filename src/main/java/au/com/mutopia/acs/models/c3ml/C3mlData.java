package au.com.mutopia.acs.models.c3ml;

import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

/**
 * A POJO containing a list of {@link C3mlEntity} meshes (each with an ID), as well as a map of
 * entity IDs to parameters maps, each mapping parameter names to values for an entity.
 */
@Getter
@Setter
public class C3mlData {

  private List<C3mlEntity> c3mls;

  private Map<String, Map<String, String>> params;

  public C3mlData(List<C3mlEntity> c3mls) {
    this.c3mls = c3mls;
  }

}
