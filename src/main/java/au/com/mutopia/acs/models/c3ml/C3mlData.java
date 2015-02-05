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

  private Map<String, Map<String, String>> properties = new HashMap<>();

  /**
   * Creates an empty {@link C3mlData} container.
   */
  public C3mlData() {}

  /**
   * Creates a {@link C3mlData} container populated with the given entities.
   * 
   * @param c3mls Entities to contain in the {@link C3mlData}.
   */
  public C3mlData(List<C3mlEntity> c3mls) {
    setEntities(c3mls);
  }

  /**
   * Sets the given {@link C3mlEntity} objects as the content of the {@link C3mlData} collection,
   * and extracts the parameters from each.
   * 
   * @param c3mls The entities to set on the data container.
   */
  public void setEntities(List<C3mlEntity> c3mls) {
    this.c3mls = new ArrayList<>();
    if (c3mls != null) {
      for (C3mlEntity entity : c3mls) {
        addEntity(entity);
        extractEntityParameters(entity);
      }
    }
  }

  /**
   * Adds the given entity and all of its children to the {@link C3mlData} container.
   * 
   * @param entity The entity to add.
   * @return True if the entity was added, false otherwise.
   */
  public boolean addEntity(C3mlEntity entity) {
    boolean result = c3mls.add(entity);
    for (C3mlEntity child : entity.getChildren()) {
      result |= addEntity(child);
    }
    return result;
  }

  /**
   * Removes the given entity and all of its children from the {@link C3mlData} container.
   * 
   * @param entity The entity to remove.
   * @return True if the entity was added, false otherwise.
   */
  public boolean removeEntity(C3mlEntity entity) {
    boolean result = c3mls.remove(entity);
    for (C3mlEntity child : entity.getChildren()) {
      result |= removeEntity(child);
    }
    return result;
  }

  /**
   * Updates the {@link #properties} field with the parameter data for the given {@link C3mlEntity}.
   * 
   * @param entity The {@link C3mlEntity} to extract parameters from.
   */
  private void extractEntityParameters(C3mlEntity entity) {
    for (String name : entity.getProperties().keySet()) {
      Map<String, String> param = properties.get(name);
      if (param == null) {
        param = new HashMap<>();
        properties.put(name, param);
      }
      param.put(entity.getId().toString(), entity.getProperties().get(name));
    }
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof C3mlData)) return false;
    C3mlData otherData = (C3mlData) other;
    if (c3mls.size() == otherData.getC3mls().size() && properties.equals(otherData.getProperties())) {
      for (C3mlEntity entity : c3mls) {
        if (!otherData.getC3mls().contains(entity)) return false;
      }
    }
    return true;
  }

}
