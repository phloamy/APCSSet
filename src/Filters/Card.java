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
    public enum Consistency {
        FILLED, HOLLOW, STRIPED
    }

    private Location tlCorner, trCorner, blCorner, brCorner, center;
    private int area;
    private int number;
    private Consistency consistency;

    private Shape shape;
    private Color color;

    public Card(Location center, int area) {
        this.center = center;
        this.area = area;
    }

    public Location getTlCorner() {
        return tlCorner;
    }

    public Location getTrCorner() {
        return trCorner;
    }

    public Location getBlCorner() {
        return blCorner;
    }

    public Location getBrCorner() {
        return brCorner;
    }

    public Location getCenter() {
        return center;
    }

    public int getArea() {
        return area;
    }

    public void setCenter(Location center) {
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

    public void setCorners(Location tlCorner, Location trCorner, Location blCorner, Location brCorner) {
        this.tlCorner = tlCorner;
        this.trCorner = trCorner;
        this.blCorner = blCorner;
        this.brCorner = brCorner;
    }

    public void setTlCorner(Location tlCorner) {
        this.tlCorner = tlCorner;
    }

    public void setTrCorner(Location trCorner) {
        this.trCorner = trCorner;
    }

    public void setBlCorner(Location blCorner) {
        this.blCorner = blCorner;
    }

    public void setBrCorner(Location brCorner) {
        this.brCorner = brCorner;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public Consistency getConsistency() {
        return consistency;
    }



    public void setConsistency(Consistency consistency) {
        this.consistency = consistency;
    }

    public int getConsistencyInt() {
        //System.out.println("consistency " + consistency + " is " + consistency.ordinal());
        return consistency.ordinal();

    }

    public void setConsistencyInt(int ordinal) {
        this.consistency = Consistency.values()[ordinal];
    }

    public int getShapeInt() {
        return shape.ordinal();

    }

    public void setShapeInt(int ordinal) {
        this.shape = Shape.values()[ordinal];
    }

    public int getColorInt() {
        return color.ordinal();

    }

    public void setColorInt(int ordinal) {
        this.color = Color.values()[ordinal];
    }
}
