package au.com.mutopia.acs.models.c3ml;

import java.util.ArrayList;
import java.util.HashMap;
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

  private List<C3mlEntity> c3mls = new ArrayList<>();

  private Map<String, Map<String, String>> params = new HashMap<>();

  public C3mlData(List<C3mlEntity> c3mls) {
    if (c3mls != null) {
      this.c3mls = c3mls;
      for (C3mlEntity entity : c3mls) {
        extractEntityParameters(entity);
      }
    }
  }

  /**
   * Updates the {@link #params} field with the parameter data for the given {@link C3mlEntity}.
   * 
   * @param entity The {@link C3mlEntity} to extract parameters from.
   */
  private void extractEntityParameters(C3mlEntity entity) {
    // TODO(orlade): Extract parameters from entities.
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof C3mlData)) return false;
    C3mlData otherData = (C3mlData) other;
    if (c3mls.size() == otherData.getC3mls().size() && params.equals(otherData.getParams())
        && c3mls.stream().allMatch(e -> otherData.getC3mls().contains(e))) {
      return true;
    }
    return false;
  }
}
