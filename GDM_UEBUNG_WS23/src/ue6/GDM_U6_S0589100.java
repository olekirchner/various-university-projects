package ue6;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.NewImage;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

public class GDM_U6_S0589100 implements PlugInFilter {
    public static void main(String[] args) {
        IJ.open("/Users/ole_kirchner/Documents/studium/grundlagen-digitaler-medien/practice/GDM_UEBUNG_WS23/src/ue6/component.jpg");
        GDM_U6_S0589100 pw = new GDM_U6_S0589100();
        ImagePlus imp = WindowManager.getCurrentImage();
        pw.run(imp.getProcessor());
    }

    public int setup(String arg, ImagePlus imp) {
        if (arg.equals("about"))
        {showAbout(); return DONE;}
        return DOES_RGB+NO_CHANGES;
    }

    public void run(ImageProcessor ip) {
        String[] dropdownmenue = {"Kopie", "Pixelwiederholung", "Bilinear"};

        GenericDialog gd = new GenericDialog("scale");
        gd.addChoice("Methode", dropdownmenue, dropdownmenue[0]);
        gd.addNumericField("Höhe: ", 500, 0);
        gd.addNumericField("Breite: ", 400, 0);

        gd.showDialog();

        int height_n = (int)gd.getNextNumber(); // _n for the new image
        int width_n = (int)gd.getNextNumber();

        int width_o  = ip.getWidth();  // Determine width
        int height_o = ip.getHeight(); // Determine height

        ImagePlus neu = NewImage.createRGBImage("Skaliertes Bild",
                width_n, height_n, 1, NewImage.FILL_BLACK);

        ImageProcessor ip_n = neu.getProcessor();

        int[] pix_o = (int[])ip.getPixels();
        int[] pix_n = (int[])ip_n.getPixels();

        // Retrieve the chosen method
        String choice = gd.getNextChoice();

        // Copy
        if (choice.equals("Kopie")) {
            // Loop over the new image
            for (int y_n = 0; y_n < height_n; y_n++) {
                for (int x_n = 0; x_n < width_n; x_n++) {
                    // Only copy pixel values as long as the x_n and y_n coordinates are within the old image
                    if (y_n < height_o && x_n < width_o) {
                        // Calculate the position in the new image and the corresponding position of that pixel in the old image
                        int pos_n = y_n * width_n + x_n;
                        int pos_o = y_n * width_o + x_n;

                        // Copy the value from the calculated position in the old image to the current position in the new image
                        pix_n[pos_n] = pix_o[pos_o];
                    }
                }
            }
        }

        // Pixel repetition
        if (choice.equals("Pixelwiederholung")) {
            // Calculate x and y ratios between the old image and the new image
            double xRatio = (double)width_o / width_n;
            double yRatio = (double)height_o / height_n;

            // Loop over the new image
            for (int y_n = 0; y_n < height_n; y_n++) {
                for (int x_n = 0; x_n < width_n; x_n++) {
                    // Calculate the position of the pixel in the new image
                    int pos_n = y_n * width_n + x_n; // this should work

                    // Calculate the x and y values where the pixel from the new image would be in the old image
                    int x_o = (int)(xRatio * x_n);
                    int y_o = (int)(yRatio * y_n);

                    // Calculate the position of the pixel in the old image to be copied
                    int pos_o = y_o * width_o + x_o;

                    // Copy the value from the calculated position in the old image to the current position in the new image
                    pix_n[pos_n] = pix_o[pos_o];
                }
            }
        }

        // Bilinear interpolation
        if (choice.equals("Bilinear")) {
            // Calculate x and y ratios between the old image and the new image
            double xRatio = (double)width_o / width_n;
            double yRatio = (double)height_o / height_n;

            // Loop over the new image
            for (int y_n = 0; y_n < height_n; y_n++) {
                for (int x_n = 0; x_n < width_n; x_n++) {
                    // Calculate the current position in the array of the new image
                    int pos_n = y_n * width_n + x_n;

                    // Calculate the x and y values where the pixel from the new image would be in the old image
                    int x_o = (int)(xRatio * x_n);
                    int y_o = (int)(yRatio * y_n);

                    // Calculate the positions of the 4 pixels to interpolate between and
                    // also clamp the x and/or y values, if they go out of bounds
                    int pos_A = y_o * width_o + x_o;
                    int pos_B = y_o * width_o + (Math.min(x_o + 1, width_o - 1));
                    int pos_C = (Math.min(y_o + 1, height_o - 1)) * width_o + x_o;
                    int pos_D = (Math.min(y_o + 1, height_o - 1)) * width_o + (Math.min(x_o + 1, width_o - 1));

                    // Calculate a h and a v value, which are in a way the distance from the left and top inside the current pixel from the old image
                    // and use these values as a weight for the interpolation later on
                    double h = (xRatio * x_n - x_o);
                    double v = (yRatio * y_n - y_o);

                    // Red values from the 4 pixels to interpolate between
                    int pos_A_red = pix_o[pos_A] >> 16 & 0xff;
                    int pos_B_red = pix_o[pos_B] >> 16 & 0xff;
                    int pos_C_red = pix_o[pos_C] >> 16 & 0xff;
                    int pos_D_red = pix_o[pos_D] >> 16 & 0xff;

                    // Green values from the 4 pixels to interpolate between
                    int pos_A_green = pix_o[pos_A] >> 8 & 0xff;
                    int pos_B_green = pix_o[pos_B] >> 8 & 0xff;
                    int pos_C_green = pix_o[pos_C] >> 8 & 0xff;
                    int pos_D_green = pix_o[pos_D] >> 8 & 0xff;

                    // Blue values from the 4 pixels to interpolate between
                    int pos_A_blue = pix_o[pos_A] & 0xff;
                    int pos_B_blue = pix_o[pos_B] & 0xff;
                    int pos_C_blue = pix_o[pos_C] & 0xff;
                    int pos_D_blue = pix_o[pos_D] & 0xff;

                    // 1:1 implementation of the formula P = A * (1 - h) * (1 - v) + B * h * (1 - v) + C * (1 - h) * v + D * h * v to interpolate between 4 values,
                    // used that implementation to calculate the new red, green and blue values separately
                    int r = (int)(pos_A_red * (1 - h) * (1 - v) + pos_B_red * h * (1 - v) + pos_C_red * (1 - h) * v + pos_D_red * h * v);
                    int g = (int)(pos_A_green * (1 - h) * (1 - v) + pos_B_green * h * (1 - v) + pos_C_green * (1 - h) * v + pos_D_green * h * v);
                    int b = (int)(pos_A_blue * (1 - h) * (1 - v) + pos_B_blue * h * (1 - v) + pos_C_blue * (1 - h) * v + pos_D_blue * h * v);

                    // Put the new rgb-values into argb format
                    int argb = (0xFF << 24) | (r << 16) | (g << 8) | b;

                    // Assign the argb value to the current position in the array for the new image
                    pix_n[pos_n] = argb;
                }
            }
        }

        // Show the new image
        neu.show();
        neu.updateAndDraw();
    }

    void showAbout() {
        IJ.showMessage("");
    }
}
