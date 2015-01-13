package au.com.mutopia.acs.models.c3ml;

/**
 * Types of {@link C3mlEntity} representing the entity's geometry.
 */
public enum C3mlEntityType {
  POINT, LINE, POLYGON, MESH,
  /** If C3mlEntity is a container element that does not have any geometry. */
  CONTAINER
}
