package Filters;

import java.awt.*;
import java.awt.geom.Point2D;

public class Card {

    public enum Color {
        RED, GREEN, PURPLE
    }
    public enum Shape {
        ROUNDEDRECT, DIAMOND, BEAN
    }

    private Point tlCorner, trCorner, blCorner, brCorner, center;
    private int area;
    private int shapes;

    private Shape shape;
    private Color color;

    public Card(Point center, int area) {
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

    public void setCenter(Point center) {
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

    public void setCorners(Point tlCorner, Point trCorner, Point blCorner, Point brCorner) {
        this.tlCorner = tlCorner;
        this.trCorner = trCorner;
        this.blCorner = blCorner;
        this.brCorner = brCorner;
    }

    public void setTlCorner(Point tlCorner) {
        this.tlCorner = tlCorner;
    }

    public void setTrCorner(Point trCorner) {
        this.trCorner = trCorner;
    }

    public void setBlCorner(Point blCorner) {
        this.blCorner = blCorner;
    }

    public void setBrCorner(Point brCorner) {
        this.brCorner = brCorner;
    }
}
