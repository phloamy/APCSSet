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
        //img = floodSearchDisplayer(cleanse(img));
        DImage floodSearchedImg = floodSearchDisplayer(BWFilter(img, 200));
        cardNumberDetector(floodSearchedImg, cards);
        detectFilled(floodSearchAll(BWFilter(img, 150)), cards, 10);
        addIndicators(img, cards);
        return img;
    }

    private void detectFilled(DImage img, ArrayList<Card> cards, int margin) {
        short[][] red = img.getRedChannel();
        short[][] green = img.getGreenChannel();
        short[][] blue = img.getBlueChannel();

        int padding = 20;

        for (Card card : cards) {
            boolean wasWhite = false;
            int count = 0;

            int j = 0;

            if (card.getNumber() == 2) {
                j = (int) (card.getTlCorner().getY() + ((card.getCenter().getY() - card.getTlCorner().getY()) * 0.6));
            } else {
                j = (int) card.getCenter().getY();
            }
            for (int i = (int) card.getTlCorner().getX() + padding; i < card.getBlCorner().getX() - padding; i++) {
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

    private void cardNumberDetector(DImage img, ArrayList<Card> cards) {
        short[][] grid = img.getBWPixelGrid();

        for (Card card: cards) {

            boolean isBlack = false;
            int count = 0;

            for (int i = (int) card.getTlCorner().getY() + 20; i < card.getTrCorner().getY() - 20; i++) {
                int j = (int) (card.getTlCorner().getX() + card.getBlCorner().getX())/2;

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
        }
    }

    private DImage addIndicators(DImage img, ArrayList<Card> cards) {
        for (Card card : cards) {

            addDot(img, (int) card.getTlCorner().getX(), (int) card.getTlCorner().getY(), 3, 255, 255, 255);
            addDot(img, (int) card.getTrCorner().getX(), (int) card.getTrCorner().getY(), 3, 255, 255, 255);
            addDot(img, (int) card.getBlCorner().getX(), (int) card.getBlCorner().getY(), 3, 255, 255, 255);
            addDot(img, (int) card.getBrCorner().getX(), (int) card.getBrCorner().getY(), 3, 255, 255, 255);

            switch (card.getColor()) {
                case RED:
                    addDot(img, (int) card.getCenter().getX(), (int) card.getCenter().getY(), 3, 200, 40, 40);
                    break;
                case GREEN:
                    addDot(img, (int) card.getCenter().getX(), (int) card.getCenter().getY(), 3, 0, 255, 0);
                    break;
                case PURPLE:
                    addDot(img, (int) card.getCenter().getX(), (int) card.getCenter().getY(), 3, 120, 0, 120);
                    break;
            }

            for (int i = 0; i < card.getNumber(); i++) {
                addDot(img, (int) card.getCenter().getX() + (i * 20), (int) card.getCenter().getY() - 50, 5, 0, 0, 0);
            }

            if (card.getConsistency() != null) {
                switch (card.getConsistency()) {
                    case FILLED:
                        addDot(img, (int) card.getCenter().getX() + 50, (int) card.getCenter().getY(), 7, 0, 0, 0);
                        break;
                    case HOLLOW:
                        addDot(img, (int) card.getCenter().getX() + 50, (int) card.getCenter().getY(), 7, 255, 255, 255);
                        break;
                    case STRIPED:
                        addDot(img, (int) card.getCenter().getX() + 50, (int) card.getCenter().getY(), 7, 150, 150, 150);
                        break;
                }
            }
        }

        return img;
    }

    private void cardColorDetector(DImage img, ArrayList<Card> cards) {
        for (Card card : cards) {
            colorDetector(img, card, card.getTlCorner(), card.getBrCorner());

            //System.out.println(card.getColor());
        }
    }

    private void colorDetector(DImage img, Card card, Point2D startXY, Point2D endXY) {
        short[][] red = img.getRedChannel();
        short[][] green = img.getGreenChannel();
        short[][] blue = img.getBlueChannel();

        double margin = 0.3;
        int padding = 10;

        for (int i = (int) startXY.getX() + padding; i < endXY.getX() - padding; i++) {
            for (int j = (int) startXY.getY() + padding; j < endXY.getY() - padding; j++) {
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

        for (int i = 0; i <= medianCardIndex; i++) {
            cards.remove(0);
        }

        System.out.println(cards.size() + " cards found! (you probably want 12)");
        return cards;
    }

    private DImage floodSearchAll(short[][] pixels) {
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
            for (int r = 0; r < card.length; r++) {
                for (int c = 0; c < card[0].length; c++) {
                    if (card[r][c] == 255) {
                        out[r][c] = 255;
                    }
                }
            }
        }

        DImage image = new DImage(pixels[0].length, pixels.length);
        image.setPixels(out);
        return image;
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

                    double averageX = 0;
                    double averageY = 0;
                    int count = 0;
                    for (int i = 0; i < searchedPixels.length; i++) {
                        for (int j = 0; j < searchedPixels[0].length; j++) {
                            if (searchedPixels[i][j] == 255) {
                                averageX += i;
                                averageY += j;
                                count++;
                            }
                        }
                    }

                    averageX = averageX / count;
                    averageY = averageY / count;
                    Point topLeft = new Point((int) averageX, (int) averageY);
                    Point topRight = new Point((int) averageX, (int) averageY);
                    Point bottomLeft = new Point((int) averageX, (int) averageY);
                    Point bottomRight = new Point((int) averageX, (int) averageY);

                    for (int i = 0; i < searchedPixels.length; i++) {
                        for (int j = 0; j < searchedPixels[0].length; j++) {
                            if (searchedPixels[i][j] == 255) {
                                if (i > averageX && j > averageY) {
                                    if (bottomRight.distance(averageX, averageY) < Point.distance(averageX, averageY, i, j)) {
                                        bottomRight = new Point(i, j);
                                    }
                                }
                                if (i > averageX && j < averageY) {
                                    if (bottomLeft.distance(averageX, averageY) < Point.distance(averageX, averageY, i, j)) {
                                        bottomLeft = new Point(i, j);
                                    }
                                }
                                if (i < averageX && j > averageY) {
                                    if (topRight.distance(averageX, averageY) < Point.distance(averageX, averageY, i, j)) {
                                        topRight = new Point(i, j);
                                    }
                                }
                                if (i < averageX && j < averageY) {
                                    if (topLeft.distance(averageX, averageY) < Point.distance(averageX, averageY, i, j)) {
                                        topLeft = new Point(i, j);
                                    }
                                }
                            }
                        }
                    }

                    Card card = new Card(new Point((int) averageX, (int) averageY), count);
                    card.setCorners(topLeft, topRight, bottomLeft, bottomRight);
                    cards.add(card);
                }
            }
        }

        return cards;
    }

    private short[][] floodSearch(short[][] pixels, int x, int y) {
        short[][] out = new short[pixels.length][pixels[0].length];
        ArrayList<Point> q = new ArrayList<>();
        q.add(new Point(x, y));

        while (q.size() > 0) {
            int pX = (int) q.get(0).getX();
            int pY = (int) q.get(0).getY();
            q.remove(0);

            if (pX >= 0 && pX < pixels.length && pY >= 0 && pY < pixels[0].length) {
                if (pixels[pX][pY] == 255) {
                    out[pX][pY] = 255;
                    pixels[pX][pY] = 0;

                    q.add(new Point(pX + 1, pY));
                    q.add(new Point(pX - 1, pY));
                    q.add(new Point(pX, pY + 1));
                    q.add(new Point(pX, pY - 1));
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

    private short[][] BWBlur(short[][] pixels, int radius) {
        for (int r = 0; r < pixels.length; r++) {
            for (int c = 0; c < pixels[0].length; c++) {
                int sum = 0;
                int count = 0;

                for (int i = -radius; i <= radius; i++) {
                    for (int j = -radius; j <= radius; j++) {
                        int pX = r + i;
                        int pY = c + j;

                        if (pX >= 0 && pX < pixels.length && pY >= 0 && pY < pixels[0].length) {
                            sum += pixels[pX][pY];
                            count++;
                        }
                    }
                }

                if (count > 0) sum /= count;

                sum = Math.max(0, Math.min(sum, 255));

                pixels[r][c] = (short) sum;
            }
        }

        return pixels;
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

    private double colorDistance(int red1, int green1, int blue1, int red2, int green2, int blue2) {
        double dr = red1 - red2;
        double dg = green1 - green2;
        double db = blue1 - blue2;

        return Math.sqrt(dr * dr + dg * dg + db * db);
    }
}

