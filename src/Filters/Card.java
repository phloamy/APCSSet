package Filters;

import java.awt.geom.Point2D;

public class Card {

    public enum Color {
        RED, GREEN, PURPLE
    }
    public enum Shape {
        ROUNDEDRECT, DIAMOND, BEAN
    }

    private Point2D tlCorner, trCorner, blCorner, brCorner, center;
    private int area;
    private int shapes;

    private Shape shape;
    private Color color;

    public Card(Point2D center, int area) {
        this.center = center;
        this.area = area;
    }


    public Point2D getTlCorner() {
        return tlCorner;
    }

    public Point2D getTrCorner() {
        return trCorner;
    }

    public Point2D getBlCorner() {
        return blCorner;
    }

    public Point2D getBrCorner() {
        return brCorner;
    }

    public Point2D getCenter() {
        return center;
    }

    public int getArea() {
        return area;
    }

    public void setCenter(Point2D center) {
        this.center = center;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public Shape getShape() {
        return shape;
    }

    public void setShape(Shape shape) {
        this.shape = shape;
    }


}
