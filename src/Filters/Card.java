package Filters;

import java.awt.geom.Point2D;

public class Card {
    private Point2D tlCorner, trCorner, blCorner, brCorner, center;
    private int area;

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
}
