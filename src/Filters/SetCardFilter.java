package Filters;

import Interfaces.PixelFilter;
import core.DImage;

import java.awt.geom.Point2D;
import java.util.ArrayList;

public class SetCardFilter implements PixelFilter {


    @Override
    public DImage processImage(DImage img) {
        img = CardDetector(img);
        return img;
    }

    private DImage CardDetector(DImage img) {
        ArrayList<Card> cards = floodSearchHelper(BWFilter(img));

        short[][] red = img.getRedChannel();
        short[][] green = img.getGreenChannel();
        short[][] blue = img.getBlueChannel();

        // Do stuff with color channels here

        for (Card card: cards) {
            int x = (int) card.getCenter().getX();
            int y = (int) card.getCenter().getY();

            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    red[x+i][y+i] = 0;
                    green[x+i][y+i] = 255;
                    blue[x+i][y+i] = 0;

                }
            }
        }

        img.setColorChannels(red, green, blue);
        return img;

    }

    private ArrayList<Card> floodSearchHelper(short[][] pixels) {
        ArrayList<Card> cardList = new ArrayList<>();

        for (int i = 0; i < pixels.length; i++) {
            for (int j = 0; j < pixels[0].length; j++) {
                if (pixels[i][j] == 255) {
                    short[][] searchedPixels = new short[pixels.length][pixels[0].length];
                    tendrils(pixels, i, j, searchedPixels);
                    Point2D tlCorner, trCorner, blCorner, brCorner;
                    double averageX = 0;
                    double averageY = 0;
                    int count = 0;
                    for (int k = 0; k < searchedPixels.length; k++) {
                        for (int l = 0; l < searchedPixels[0].length; l++) {
                            if (searchedPixels[k][l] == 255) {
                                averageX += k;
                                averageY += l;
                                count++;
                            }
                        }
                    }

                    averageX = averageX / count;
                    averageY = averageY / count;

                    cardList.add(new Card(new Point2D.Double(averageX, averageY), count));
                }
            }
        }
        return cardList;
    }

    private void tendrils(short[][] pixels, int x, int y, short[][] searchedPixels) {
        pixels[x][y] = 0;
        searchedPixels[x][y] = 255;

        for (int i = -1; i <= 1 ; i++) {
            for (int j = -1; j <= 1 ; j++) {
                if (pixels[x+i][y+j] == 255) {
                    tendrils(pixels, x + i, y + j, searchedPixels);
                }
            }
        }
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

