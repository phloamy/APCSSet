package Filters;

import Interfaces.PixelFilter;
import core.DImage;
import javafx.geometry.Point3D;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class SetCardFilter implements PixelFilter {

    private ArrayList<Card> cards;

    @Override
    public DImage processImage(DImage img) {
        cards = cardPositionDetector(img);
        cardColorDetector(img, cards);
        //img.setPixels(floodSearch(BWFilter(img), 230, 400));
        //img = floodSearchDisplayer(cleanse(img));
        DImage floodSearchedImg = floodSearchDisplayer(BWFilter(img, 200));
        cardNumberDetector(floodSearchedImg, cards);
        DImage thresholded = threshold(img.getBWPixelGrid(), 160);
        detectFilled(thresholded, cards, 20);
        cardShapeDetector(floodSearchedImg, cards);
        addIndicators(img, cards);
        return img;
    }

    private void detectFilled(DImage img, ArrayList<Card> cards, int margin) {
        short[][] red = img.getRedChannel();
        short[][] green = img.getGreenChannel();
        short[][] blue = img.getBlueChannel();

        int padding = 20;

        for (Card card : cards) {
            boolean wasWhite = true;
            int count = 0;

            int j;

            if (card.getNumber() == 2) {
                j = (int) (card.getTlCorner().getCol() + ((card.getCenter().getCol() - card.getTlCorner().getCol()) * 0.65));
            } else {
                j = card.getCenter().getCol();
            }

            for (int i = card.getTlCorner().getRow() + padding; i < card.getBlCorner().getRow() - padding; i++) {
                short r = red[i][j];
                short g = green[i][j];
                short b = blue[i][j];

                if (colorDistance(r, g, b, 255, 255, 255) > margin) {
                    if (wasWhite) {
                        count++;
                    }
                    wasWhite = false;
                } else {
                    wasWhite = true;
                }
            }

            if (count > 4) {
                card.setConsistency(Card.Consistency.STRIPED);
            } else if (count > 1) {
                card.setConsistency(Card.Consistency.HOLLOW);
            } else {
                card.setConsistency(Card.Consistency.FILLED);
            }
        }
    }

    private void cardShapeDetector(DImage img, ArrayList<Card> cards) {
        short[][] grid = img.getBWPixelGrid();

        int padding = 8;
        int topBotDiff = 23;
        for (Card card: cards) {

            int topCount = 0;
            int midCount = 0;

            int firstTop = -2;
            int firstMid = -2;

            for (int i = card.getTlCorner().getCol() + padding; i < card.getTrCorner().getCol() - padding; i++) {
                int middle = (card.getTlCorner().getRow() + card.getBlCorner().getRow())/2;
                int top = ((card.getTlCorner().getRow() + card.getBlCorner().getRow())/2) - topBotDiff;
                if (grid[top][i] == 0) {
                    topCount ++;
                    if (firstTop == -2) {
                        firstTop = i;
                    }
                }
                if (grid[middle][i] == 0) {
                    midCount ++;
                    if (firstMid == -2) {
                        firstMid = i;
                    }
                }
            }
            boolean isBlack = false;
            int count = 0;
            for (int i = card.getTlCorner().getRow() + padding/4; i < card.getBlCorner().getRow() - padding/4; i++) {
                if (!isBlack) {
                    if (grid[i][firstMid] == 0) {
                        isBlack = true;
                        count += 1;
                    }
                } else {
                    if (grid[i][firstMid] == 255) {
                        isBlack = false;

                    }
                }
            }

            midCount = midCount / card.getNumber();
            topCount = topCount / card.getNumber();


            System.out.println(firstMid - firstTop);
            if ((midCount - topCount) > 4) {
                card.setShape(Card.Shape.DIAMOND);

            } else if (count > 1) {
                card.setShape(Card.Shape.BEAN);
            } else {
                card.setShape(Card.Shape.ROUNDEDRECT);
            }
        }
    }

    private void cardNumberDetector(DImage img, ArrayList<Card> cards) {
        short[][] grid = img.getBWPixelGrid();

        for (Card card: cards) {

            boolean isBlack = false;
            int count = 0;

            for (int i = (int) card.getTlCorner().getCol() + 8; i < card.getTrCorner().getCol() - 8; i++) {
                int j = (int) (card.getTlCorner().getRow() + card.getBlCorner().getRow())/2;

                if (isBlack) {
                    if (grid[j][i] == 255) {
                        isBlack = false;
                    }
                } else {
                    if (grid[j][i] == 0) {
                        count += 1;
                        isBlack = true;
                    }
                }


            }
            card.setNumber(count);

            System.out.println(count);
        }
    }

    private DImage addIndicators(DImage img, ArrayList<Card> cards) {
        for (Card card : cards) {
            /*
            int padding = 8;
            int topBotDiff = 23;
            for (int i = (int) card.getTlCorner().getY() + padding; i < card.getTrCorner().getY() - padding; i++) {
                int middle = (int) (card.getTlCorner().getX() + card.getBlCorner().getX()) / 2;
                int top = (int) ((card.getTlCorner().getX() + card.getBlCorner().getX()) / 2) - topBotDiff;

                addDot(img, middle, i, 1, 0, 255, 0 );
                addDot(img, top, i, 1, 0, 255, 0 );
            }
             */

            //addDot(img,(int) card.getCenter().getX(), (int) (card.getTlCorner().getY() + ((card.getCenter().getY() - card.getTlCorner().getY()) * 0.7)), 10, 180, 45, 180);

            addDot(img, (int) card.getTlCorner().getRow(), (int) card.getTlCorner().getCol(), 3, 255, 255, 255);
            addDot(img, (int) card.getTrCorner().getRow(), (int) card.getTrCorner().getCol(), 3, 255, 255, 255);
            addDot(img, (int) card.getBlCorner().getRow(), (int) card.getBlCorner().getCol(), 3, 255, 255, 255);
            addDot(img, (int) card.getBrCorner().getRow(), (int) card.getBrCorner().getCol(), 3, 255, 255, 255);

            switch (card.getColor()) {
                case RED: // red
                    addDot(img, (int) card.getCenter().getRow(), (int) card.getCenter().getCol(), 3, 200, 40, 40);
                    break;
                case GREEN: //green
                    addDot(img, (int) card.getCenter().getRow(), (int) card.getCenter().getCol(), 3, 0, 255, 0);
                    break;
                case PURPLE: //purple
                    addDot(img, (int) card.getCenter().getRow(), (int) card.getCenter().getCol(), 3, 120, 0, 120);
                    break;
            }

            switch (card.getShape()) {
                case BEAN: // up
                    addDot(img, (int) card.getCenter().getRow() - 20, (int) card.getCenter().getCol() + 50, 3, 255, 0, 255);
                    break;
                case DIAMOND: // middle
                    addDot(img, (int) card.getCenter().getRow(), (int) card.getCenter().getCol() + 50, 3, 255, 255, 0);
                    break;
                case ROUNDEDRECT: //down
                    addDot(img, (int) card.getCenter().getRow() + 20, (int) card.getCenter().getCol() + 50, 3, 0, 255, 255);
                    break;
            }

            switch (card.getConsistency()) {
                case FILLED:
                    addDot(img, (int) card.getCenter().getRow() - 50, (int) card.getCenter().getCol(), 5, 0, 0, 0);
                    break;
                case HOLLOW:
                    addDot(img, (int) card.getCenter().getRow() - 50, (int) card.getCenter().getCol(), 5, 255, 255, 255);
                    break;
                case STRIPED:
                    addDot(img, (int) card.getCenter().getRow() - 50, (int) card.getCenter().getCol(), 5, 150, 150, 150);
                    break;
            }

            for (int i = 0; i < card.getNumber(); i++) {
                addDot(img, (int) card.getCenter().getRow() + (i * 20), (int) card.getCenter().getCol() - 50, 5, 0, 0, 0);
            }
        }

        return img;
    }

    private void cardColorDetector(DImage img, ArrayList<Card> cards) {
        for (Card card : cards) {

            colorDetector(img, card, card.getTlCorner(), card.getBrCorner());

            System.out.println(card.getColor());
        }
    }

    private void colorDetector(DImage img, Card card, Location startXY, Location endXY) {
        short[][] red = img.getRedChannel();
        short[][] green = img.getGreenChannel();
        short[][] blue = img.getBlueChannel();

        double margin = 0.3;
        int padding = 10;

        for (int i = startXY.getRow() + padding; i < endXY.getRow() - padding; i++) {
            for (int j = startXY.getCol() + padding; j < endXY.getCol() - padding; j++) {
                short r = red[i][j];
                short g = green[i][j];
                short b = blue[i][j];

                if (r > g * (1 + margin) && r > b * (1 + margin)) {
                    card.setColor(Card.Color.RED);
                    return;
                }
                if (g > r * (1 + margin) && g > b * (1 + margin)) {
                    card.setColor(Card.Color.GREEN);
                    return;
                }
                if ((r + b) / 2 > g * (1 + margin)) {
                    card.setColor(Card.Color.PURPLE);
                    return;
                }
            }
        }
    }

    private short[][] cleanse(DImage img) {
        DImage blur = blur(img, 4);
        short[][] bwPixels = BWFilter(blur, 240);
        DImage BWImage = new DImage(bwPixels[0].length, bwPixels.length);
        BWImage.setPixels(bwPixels);
        //blur = blur(BWImage, 2);

        return BWImage.getBWPixelGrid();
    }

    private ArrayList<Card> cardPositionDetector(DImage img) {
        ArrayList<Card> cards;

        short[][] out = cleanse(img);

        cards = floodSearchHelper(out);

        short[][] red = img.getRedChannel();
        short[][] green = img.getGreenChannel();
        short[][] blue = img.getBlueChannel();

        Collections.sort(cards, new Comparator<Card>() {
            @Override
            public int compare(Card o1, Card o2) {
                return o1.getArea() - o2.getArea();
            }
        });

        // remove "cards" that are too small to actually be cards;

        int medianCardIndex = 0;
        int maxJump = 0;

        for (int i = 0; i < cards.size() - 1; i++) {
            int jump = cards.get(i + 1).getArea() - cards.get(i).getArea();
            if (jump > maxJump && jump > cards.get(i + 1).getArea() * 0.5) { // magic numbers
                medianCardIndex = i;
                maxJump = jump;
            }
        }
        /*
        for (int i = 0; i <= medianCardIndex; i++) {
            cards.remove(0);
        }
        */


        int medianCardSize = cards.get(medianCardIndex + 1).getArea();

        for (int i = 0; i < cards.size(); i++) {
            if (cards.get(i).getArea() < medianCardSize * 0.3) {
                cards.remove(i);
                i--;
            }
        }


        System.out.println(cards.size() + " cards found! (you probably want 12)");
        return cards;
    }

    private DImage floodSearchDisplayer(short[][] pixels) {
        ArrayList<short[][]> cardPixels = new ArrayList<>();
        short[][] out = new short[pixels.length][pixels[0].length];

        for (int r = 0; r < pixels.length; r++) {
            for (int c = 0; c < pixels[0].length; c++) {
                if (pixels[r][c] == 255) {
                    short[][] searchedPixels = floodSearch(pixels, r, c);
                    cardPixels.add(searchedPixels);
                }
            }
        }

        for (short[][] card : cardPixels) {
            int count = 0;
            for (int r = 0; r < card.length; r++) {
                for (int c = 0; c < card[0].length; c++) {
                    if (card[r][c] == 255) {
                        count++;
                    }
                }
            }

            for (int r = 0; r < card.length; r++) {
                for (int c = 0; c < card[0].length; c++) {
                    if (card[r][c] == 255) {
                        if (count > 5000) {
                            out[r][c] = 255;
                        } else {
                            out[r][c] = 0;
                        }
                    }
                }
            }
        }

        DImage image = new DImage(pixels[0].length, pixels.length);
        image.setPixels(out);
        return image;
    }

    private ArrayList<Card> floodSearchHelper(short[][] pixels) {
        ArrayList<Card> cards = new ArrayList<>();

        for (int r = 0; r < pixels.length; r++) {
            for (int c = 0; c < pixels[0].length; c++) {
                if (pixels[r][c] == 255) {
                    short[][] searchedPixels = floodSearch(pixels, r, c);

                    double averageRow = 0;
                    double averageCol = 0;
                    int count = 0;
                    for (int i = 0; i < searchedPixels.length; i++) {
                        for (int j = 0; j < searchedPixels[0].length; j++) {
                            if (searchedPixels[i][j] == 255) {
                                averageRow += i;
                                averageCol += j;
                                count++;
                            }
                        }
                    }

                    averageRow = averageRow / count;
                    averageCol = averageCol / count;

                    Card card = new Card(new Location((int) averageRow, (int) averageCol), count);
                    findCorners(searchedPixels, card);
                    cards.add(card);
                }
            }
        }

        return cards;
    }

    private void findCorners(short[][] pixels, Card card) {
        Location center = card.getCenter();
        int centerRow = card.getCenter().getRow();
        int centerCol = card.getCenter().getCol();

        Location topLeft = new Location(center.getRow(), center.getCol());
        Location topRight = new Location(center.getRow(), center.getCol());
        Location bottomLeft = new Location(center.getRow(), center.getCol());
        Location bottomRight = new Location(center.getRow(), center.getCol());

        for (int row = 0; row < pixels.length; row++) {
            for (int col = 0; col < pixels[0].length; col++) {
                if (pixels[row][col] == 255) {
                    if ((row - centerRow) + (col - centerCol) > (bottomRight.getRow() - centerRow) + (bottomRight.getCol() - centerCol)) {
                        bottomRight = new Location(row, col);
                    }
                    if ((row - centerRow) + (centerCol - col) > (bottomLeft.getRow() - centerRow) + (centerCol - bottomLeft.getCol())) {
                        bottomLeft = new Location(row, col);
                    }
                    if ((centerRow - row) + (col - centerCol) > (centerRow - topRight.getRow()) + (topRight.getCol() - centerCol)) {
                        topRight = new Location(row, col);
                    }
                    if ((centerRow - row) + (centerCol - col) > (centerRow - topLeft.getRow()) + (centerCol - topLeft.getCol())) {
                        topLeft = new Location(row, col);
                    }

                    /*
                    if (row > averageRow && col > averageCol) {
                        if (bottomRight.distance(averageRow, averageCol) < Point.distance(averageRow, averageCol, row, col)) {
                            bottomRight = new Point(row, col);
                        }
                    }
                    if (row > averageRow && col < averageCol) {
                        if (bottomLeft.distance(averageRow, averageCol) < Point.distance(averageRow, averageCol, row, col)) {
                            bottomLeft = new Point(row, col);
                        }
                    }
                    if (row < averageRow && col > averageCol) {
                        if (topRight.distance(averageRow, averageCol) < Point.distance(averageRow, averageCol, row, col)) {
                            topRight = new Point(row, col);
                        }
                    }
                    if (row < averageRow && col < averageCol) {
                        if (topLeft.distance(averageRow, averageCol) < Point.distance(averageRow, averageCol, row, col)) {
                            topLeft = new Point(row, col);
                        }
                    }
                     */
                }
            }
        }

        card.setCorners(topLeft, topRight, bottomLeft, bottomRight);
    }

    private short[][] floodSearch(short[][] pixels, int row, int col) {
        short[][] out = new short[pixels.length][pixels[0].length];
        ArrayList<Location> q = new ArrayList<>();
        q.add(new Location(row, col));

        while (q.size() > 0) {
            int pRow = q.get(0).getRow();
            int pCol = q.get(0).getCol();
            q.remove(0);

            if (pRow >= 0 && pRow < pixels.length && pCol >= 0 && pCol < pixels[0].length) {
                if (pixels[pRow][pCol] == 255) {
                    out[pRow][pCol] = 255;
                    pixels[pRow][pCol] = 0;

                    q.add(new Location(pRow + 1, pCol));
                    q.add(new Location(pRow - 1, pCol));
                    q.add(new Location(pRow, pCol + 1));
                    q.add(new Location(pRow, pCol - 1));
                }
            }
        }

        return out;
    }

    private DImage addDot(DImage img, int x, int y, int radius, int r, int g, int b) {
        short[][] red = img.getRedChannel();
        short[][] green = img.getGreenChannel();
        short[][] blue = img.getBlueChannel();

        for (int i = -radius; i <= radius; i++) {
            for (int j = -radius; j <= radius; j++) {
                if (Math.abs(i) == radius || Math.abs(j) == radius) {
                    red[x + i][y + j] = 0;
                    green[x + i][y + j] = 0;
                    blue[x + i][y + j] = 0;
                } else {
                    red[x + i][y + j] = (short) r;
                    green[x + i][y + j] = (short) g;
                    blue[x + i][y + j] = (short) b;
                }
            }
        }

        img.setColorChannels(red, green, blue);
        return img;
    }

    private DImage blur(DImage img, int radius) {
        short[][] red = img.getRedChannel();
        short[][] green = img.getGreenChannel();
        short[][] blue = img.getBlueChannel();

        short[][] redOut = new short[red.length][red[0].length];
        short[][] greenOut = new short[red.length][red[0].length];
        short[][] blueOut = new short[red.length][red[0].length];

        for (int r = 0; r < red.length; r++) {
            for (int c = 0; c < red[0].length; c++) {
                long redSum = 0;
                long greenSum = 0;
                long blueSum = 0;
                int count = 0;
                for (int i = -radius; i <= radius; i++) {
                    for (int j = -radius; j <= radius; j++) {
                        int pX = r + i;
                        int pY = c + j;

                        if (pX >= 0 && pX < red.length && pY >= 0 && pY < red[0].length) {
                            redSum += red[pX][pY];
                            greenSum += green[pX][pY];
                            blueSum += blue[pX][pY];
                            count++;
                        }
                    }
                }

                if (count > 0) {
                    redSum /= count;
                    greenSum /= count;
                    blueSum /= count;
                }
                redSum = Math.max(0, Math.min(redSum, 255));
                greenSum = Math.max(0, Math.min(greenSum, 255));
                blueSum = Math.max(0, Math.min(blueSum, 255));

                redOut[r][c] = (short) redSum;
                greenOut[r][c] = (short) greenSum;
                blueOut[r][c] = (short) blueSum;
            }
        }

        DImage image = new DImage(redOut[0].length, redOut.length);
        image.setColorChannels(redOut, greenOut, blueOut);
        return image;
    }

    private short[][] BWFilter(DImage img, int threshold) {
        short[][] red = img.getRedChannel();
        short[][] green = img.getGreenChannel();
        short[][] blue = img.getBlueChannel();

        short[][] out = new short[red.length][red[0].length];

        // Do stuff with color channels here

        for (int r = 0; r < red.length; r++) {
            for (int c = 0; c < red[0].length; c++) {
                int redDist = 255 - red[r][c];
                int greenDist = 255 - green[r][c];
                int blueDist = 255 - blue[r][c];

                double dist = Math.sqrt(redDist * redDist + greenDist * greenDist + blueDist * blueDist);
                if (dist > threshold) {
                    out[r][c] = 0;
                } else {
                    out[r][c] = 255;
                }
            }
        }

        return out;
    }

    private DImage threshold(short[][] pixels, int threshold) {
        short[][] out = new short[pixels.length][pixels[0].length];

        // Do stuff with color channels here

        for (int r = 0; r < out.length; r++) {
            for (int c = 0; c < out[0].length; c++) {
                if (pixels[r][c] < threshold) {
                    out[r][c] = 0;
                } else {
                    out[r][c] = 255;
                }
            }
        }

        DImage image = new DImage(pixels[0].length, pixels.length);
        image.setPixels(out);
        return image;
    }

    private double colorDistance(int red1, int green1, int blue1, int red2, int green2, int blue2) {
        double dr = red1 - red2;
        double dg = green1 - green2;
        double db = blue1 - blue2;

        return Math.sqrt(dr * dr + dg * dg + db * db);
    }
}

