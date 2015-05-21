package au.com.mutopia.acs.util.mesh;

import com.vividsolutions.jts.geom.Coordinate;
import java.awt.geom.Point2D;

public class Triangle {
  private Point2D vertex1;
  private Point2D vertex2;
  private Point2D vertex3;
  
  public Triangle(Point2D vertex1, Point2D vertex2, Point2D vertex3) {
    super();
    this.vertex1 = vertex1;
    this.vertex2 = vertex2;
    this.vertex3 = vertex3;
  }
  
  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    Triangle triangle = (Triangle) o;
    Triangle temp = new Triangle(getVertex1(), getVertex2(), getVertex3());
    triangle.ascendingOrder();
    temp.ascendingOrder();
    return ((triangle.getVertex1().distance(temp.getVertex1()) == 0) && 
            (triangle.getVertex2().distance(temp.getVertex2()) == 0) && 
            (triangle.getVertex3().distance(temp.getVertex3()) == 0));
  }
  
  public void ascendingOrder() {
    Point2D tempX;
    if (vertex1.getX() < vertex2.getX()) {
      if (vertex1.getX() < vertex3.getX()) {
        if (vertex2.getX() < vertex3.getX()) {
          // Already in ascending order.
          return;
        } else {
          tempX = vertex2;
          vertex2 = vertex3;
          vertex3 = tempX;
        }
      } else {
        tempX = vertex1;
        vertex1 = vertex3;
        vertex3 = vertex2;
        vertex2 = tempX;
      }
    } else {
      if (vertex1.getX() < vertex3.getX()) {
        tempX = vertex1;
        vertex1 = vertex2;
        vertex2 = tempX;
      } else {
        if (vertex2.getX() < vertex3.getX()) {
          tempX = vertex1;
          vertex1 = vertex2;
          vertex2 = vertex3;
          vertex3 = tempX;
        } else {
          tempX = vertex1;
          vertex1 = vertex3;
          vertex3 = tempX;
        }
      }
    }
  }
  
  public Coordinate[] getCoordinates() {
    Coordinate[] coords = new Coordinate[4];
    coords[0] = new Coordinate(vertex1.getX(), vertex1.getY(), 0);
    coords[1] = new Coordinate(vertex2.getX(), vertex2.getY(), 0);
    coords[2] = new Coordinate(vertex3.getX(), vertex3.getY(), 0);
    coords[3] = new Coordinate(vertex1.getX(), vertex1.getY(), 0);
    return coords;
  }
  
  public Point2D getVertex1() {
    return vertex1;
  }
  public void setVertex1(Point2D vertex1) {
    this.vertex1 = vertex1;
  }
  public Point2D getVertex2() {
    return vertex2;
  }
  public void setVertex2(Point2D vertex2) {
    this.vertex2 = vertex2;
  }
  public Point2D getVertex3() {
    return vertex3;
  }
  public void setVertex3(Point2D vertex3) {
    this.vertex3 = vertex3;
  }


}
