package au.com.mutopia.acs.models.c3ml;


/**
 * Types of {@link C3mlEntity} representing the entity's geometry.
 */
public enum C3mlEntityType {
  /** A point in space. */
  POINT,
  /** A line string between a sequence of points. */
  LINE,
  /** An area enclosed within a ring of points. */
  POLYGON,
  /** A 3D mesh defined by a collection of vertices connected in triangles. */
  MESH,
  /** If C3mlEntity is a container element that does not have any geometry. */
  COLLECTION
}
