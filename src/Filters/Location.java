package Filters;

public class Location {
    private int row, col;

    public Location(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public Location(double row, double col) {
        this.row = (int) row;
        this.col = (int) col;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public void setCol(int col) {
        this.col = col;
    }

    public double signedDistance(boolean rowNegative, boolean colNegative, Location location) {
        int row = this.row;
        int col = this.col;

        if (rowNegative) row = -row;
        if (colNegative) col = -col;

        return (row - location.getRow()) + (col - location.getCol());
    }
}
