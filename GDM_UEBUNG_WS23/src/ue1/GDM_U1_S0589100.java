package ue1;

import ij.ImageJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.gui.NewImage;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;

public class GDM_U1_S0589100 implements PlugIn {

    final static String[] choices = {
            "Schwarzes Bild",
            "Belgische Fahne",
            "Schwarz/Weiss Verlauf",
            "Diagon. Weiss/Schwarz Verlauf",
            "Horiz. Schwarz/Rot vert. Schwarz/Blau Verlauf",
            "USA Fahne",
            "Tschechische Fahne"
    };

    private String choice;

    public static void main(String args[]) {
        ImageJ ij = new ImageJ();
        ij.exitWhenQuitting(true);

        GDM_U1_S0589100 imageGeneration = new GDM_U1_S0589100();
        imageGeneration.run("");
    }

    public void run(String arg) {
        int width  = 566;
        int height = 400;

        ImagePlus imagePlus = NewImage.createRGBImage("GLDM_U1", width, height, 1, NewImage.FILL_BLACK);
        ImageProcessor ip = imagePlus.getProcessor();

        int[] pixels = (int[])ip.getPixels();

        dialog();

        if ( choice.equals("Schwarzes Bild") ) {
            generateBlackImage(width, height, pixels);
        }

        if (choice.equals("Belgische Fahne")) {
            generateBelgianFlagImage(width, height, pixels);
        }

        if (choice.equals("Schwarz/Weiss Verlauf")) {
            generateBlackWhiteGradientImage(width, height, pixels);
        }

        if (choice.equals("Diagon. Weiss/Schwarz Verlauf")) {
            generateDiagonalWhiteBlackGradientImage(width, height, pixels);
        }

        if (choice.equals("Horiz. Schwarz/Rot vert. Schwarz/Blau Verlauf")) {
            generateHorizontalBlackRedVerticalBlackBlueImage(width, height, pixels);
        }

        if (choice.equals("USA Fahne")) {
            generateUsaFlagImage(width, height, pixels);
        }

        if (choice.equals("Tschechische Fahne")) {
            generateCzechFlagImage(width, height, pixels);
        }

        imagePlus.show();
        imagePlus.updateAndDraw();
    }

    private void generateBlackImage(int width, int height, int[] pixels) {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pos = y * width + x;

                int r = 0;
                int g = 0;
                int b = 0;

                pixels[pos] = 0xFF000000 | (r << 16) | (g << 8) |  b;
            }
        }
    }

    private void generateBelgianFlagImage(int width, int height, int[] pixels) {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pos = y * width + x;

                // paint everything black
                int r = 0, g = 0, b = 0;

                // add yellow between 1/3 and 2/3 of the width
                if (x > (width / 3) && x < (width / 3) * 2) { // would maybe be slightly more accurate by making a double division and then rounding to the closest number
                    r = 253;
                    g = 218;
                    b = 36;
                }

                // add red for the last third of the width
                if (x > (width / 3) * 2) {
                    r = 239;
                    g = 51;
                    b = 64;
                }

                pixels[pos] = 0xFF000000 | (r << 16) | (g << 8) | b;
            }
        }
    }

    private void generateBlackWhiteGradientImage(int width, int height, int[] pixels) {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pos = y * width + x;

                // calculate a factor which determines the pixels color value depending on the position of the pixel on the x-axis
                double fac = ((double)(x + 1) / width);

                // multiply the factor with 255 for r, g and b to get the correct graylevel
                int r = (int)(fac * 255);
                int b = (int)(fac * 255);
                int g = (int)(fac * 255);

                pixels[pos] = 0xFF000000 | (r << 16) | (g << 8) | b;
            }
        }
    }

    private void generateDiagonalWhiteBlackGradientImage(int width, int height, int[] pixels) {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pos = y * width + x;

                // calculate a divider for the factor because adding the factors for the x- and y-axis result in too large values
                // this divider would therefore bring the maximum factor down to 1, so the all values from white to full black can be calculated accordingly
                double divider = (double)(width - 1) / width + (double)(height - 1) / width;

                // calculate a factor which determines the pixels color value depending on the position of the pixel on the x- and y-axis
                double fac = 1 - (((double)x / width + (double)y / width) / divider);

                // multiply the factor with 255 for r, g and b to get the correct graylevel
                int r = (int)(fac * 255);
                int b = (int)(fac * 255);
                int g = (int)(fac * 255);

                pixels[pos] = 0xFF000000 | (r << 16) | (g << 8) | b;
            }
        }
    }

    private void generateHorizontalBlackRedVerticalBlackBlueImage(int width, int height, int[] pixels) {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pos = y * width + x;

                // calculate factors for the x- and y-axis which determine the pixels color value depending on the position of the pixel on the x- and y-axis
                double facX = ((double)(x + 1) / width);
                double facY = ((double)(y + 1) / height);

                // multiply the factor with 255 for r and b to get the correct color
                int r = (int)(facX * 255);
                int g = 0;
                int b = (int)(facY * 255);

                pixels[pos] = 0xFF000000 | (r << 16) | (g << 8) | b;
            }
        }
    }

    private void generateUsaFlagImage(int width, int height, int[] pixels) {
        // calculate the height of each stripe
        double stripeHeight = (double)height / 13;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pos = y * width + x;

                // paint everything white
                int r = 255;
                int g = 255;
                int b = 255;

                // paint each pixel in every other stripe red
                if ((int)(y / stripeHeight) % 2 == 0) {
                    r = 178;
                    g = 34;
                    b = 52;
                }

                // paint the top left corner blue
                if (x < width * 0.4 && y < height / ((double)13 / 7)) {
                    r = 60;
                    g = 59;
                    b = 110;
                }

                pixels[pos] = 0xFF000000 | (r << 16) | (g << 8) | b;
            }
        }
    }

    private void generateCzechFlagImage(int width, int height, int[] pixels) {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pos = y * width + x;

                // paint everything white
                int r = 255;
                int g = 255;
                int b = 255;

                // calculate the slope
                double slope = (double)height / width;

                // paint a blue triangle in the lower left corner
                if (y > slope * x) {
                    r = 17;
                    g = 69;
                    b = 126;
                }

                // start painting a red triangle in the lower right corner until half of the height is reached
                if (y > height - (slope * x) && y > (double)height / 2) {
                    r = 215;
                    g = 20;
                    b = 26;
                }

                pixels[pos] = 0xFF000000 | (r << 16) | (g << 8) | b;
            }
        }
    }

    private void dialog() {
        GenericDialog gd = new GenericDialog("Bildart");

        gd.addChoice("Bildtyp", choices, choices[0]);
        gd.showDialog();

        choice = gd.getNextChoice();

        if (gd.wasCanceled())
            System.exit(0);
    }
}
