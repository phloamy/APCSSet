package Filters;

import Interfaces.PixelFilter;
import core.DImage;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;

public class SetCardFilter implements PixelFilter {


    @Override
    public DImage processImage(DImage img) {
        img = CardDetector(img);
        //img.setPixels(floodSearch(BWFilter(img), 50, 150));
        return img;
    }

    private DImage CardDetector(DImage img) {
        short[][] bwPixels = BWFilter(img);
        DImage BWImage = new DImage(bwPixels[0].length, bwPixels.length);
        BWImage.setPixels(bwPixels);
        DImage blur = blur(BWImage, 3);
        short[][] out = blur.getBWPixelGrid();

        ArrayList<Card> cards = floodSearchHelper(out);

        short[][] red = img.getRedChannel();
        short[][] green = img.getGreenChannel();
        short[][] blue = img.getBlueChannel();

        // Do stuff with color channels here

        for (Card card: cards) {
            int x = (int) card.getCenter().getX();
            int y = (int) card.getCenter().getY();

            for (int i = -3; i <= 3; i++) {
                for (int j = -3; j <= 3; j++) {
                    red[x+i][y+j] = 255;
                    green[x+i][y+j] = 0;
                    blue[x+j][y+j] = 0;
                }
            }
        }

        img.setColorChannels(red, green, blue);
        return img;

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

                    cards.add(new Card(new Point((int) averageX, (int) averageY), count));
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

    private DImage blur(DImage img, int radius) {
        short[][] red = img.getRedChannel();
        short[][] green = img.getGreenChannel();
        short[][] blue = img.getBlueChannel();

        short[][] redOut = new short[red.length][red[0].length];
        short[][] greenOut = new short[red.length][red[0].length];
        short[][] blueOut = new short[red.length][red[0].length];

        for (int r = 0; r < red.length; r++) {
            for (int c = 0; c < red[0].length; c++) {
                int redSum = 0;
                int greenSum = 0;
                int blueSum = 0;
                int count = 0;
                for (int i = -radius; i <= radius; i++) {
                    for (int j = -radius; j <= radius; j++) {
                        int pX = r + i;
                        int pY = c + j;

                        if (pX >= 0 && pX < red.length && pY >= 0 && pY < red[0].length) {
                            redSum += red[pX][pY];
                            greenSum += green[pX][pY];
                            blueSum += blue[pX][pX];
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
                greenOut[r][c] = (short) redSum;
                blueOut[r][c] = (short) redSum;
            }
        }

        DImage image = new DImage(redOut[0].length, redOut.length);
        image.setColorChannels(redOut, greenOut, blueOut);
        return image;
    }

    private short[][] BWFilter(DImage img) {
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
                double threshold = 130;
                if (dist > threshold) {
                    out[r][c] = 0;
                } else {
                    out[r][c] = 255;
                }
            }
        }

        return out;
    }
}

