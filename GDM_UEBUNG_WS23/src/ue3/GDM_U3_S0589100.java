package ue3;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.ImageCanvas;
import ij.gui.ImageWindow;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;

import java.awt.Panel;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JComboBox;

/**
 Opens an image window and adds a panel below the image
 */
public class GDM_U3_S0589100 implements PlugIn {
    ImagePlus imp; // ImagePlus object
    private int[] origPixels;
    private int width;
    private int height;

    String[] items = {"Original", "Rot-Kanal", "Negativ", "Graustufen", "Binärbild", "5 Graustufen ohne Schwarz und Weiß", "27 Graustufen mit Schwarz und Weiß", "Binärbild mit vertikaler Fehlerdiffusion", "Sepia-Färbung", "9 Farben"};

    public static void main(String args[]) {
        IJ.open("/Users/ole_kirchner/Documents/studium/grundlagen-digitaler-medien/practice/GDM_UEBUNG_WS23/src/ue3/Bear.jpg");

        GDM_U3_S0589100 pw = new GDM_U3_S0589100();
        pw.imp = IJ.getImage();
        pw.run("");
    }

    public void run(String arg) {
        if (imp==null)
            imp = WindowManager.getCurrentImage();
        if (imp==null) {
            return;
        }

        CustomCanvas cc = new CustomCanvas(imp);

        storePixelValues(imp.getProcessor());

        new CustomWindow(imp, cc);
    }

    private void storePixelValues(ImageProcessor ip) {
        width = ip.getWidth();
        height = ip.getHeight();

        origPixels = ((int []) ip.getPixels()).clone();
    }

    class CustomCanvas extends ImageCanvas {
        CustomCanvas(ImagePlus imp) {
            super(imp);
        }
    } // CustomCanvas inner class

    class CustomWindow extends ImageWindow implements ItemListener {
        private String method;

        CustomWindow(ImagePlus imp, ImageCanvas ic) {
            super(imp, ic);
            addPanel();
        }

        void addPanel() {
            Panel panel = new Panel();

            JComboBox cb = new JComboBox(items);
            panel.add(cb);
            cb.addItemListener(this);

            add(panel);
            pack();
        }

        public void itemStateChanged(ItemEvent evt) {
            // Get the affected item
            Object item = evt.getItem();

            if (evt.getStateChange() == ItemEvent.SELECTED) {
                System.out.println("Selected: " + item.toString());
                method = item.toString();
                changePixelValues(imp.getProcessor());
                imp.updateAndDraw();
            }
        }


        private void changePixelValues(ImageProcessor ip) {
            // Array for writing back pixel values
            int[] pixels = (int[])ip.getPixels();

            if (method.equals("Original")) {
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        int pos = y * width + x;

                        pixels[pos] = origPixels[pos];
                    }
                }
            }

            if (method.equals("Rot-Kanal")) {
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        int pos = y * width + x;
                        int argb = origPixels[pos];  // Reading the original values

                        int r = (argb >> 16) & 0xff;

                        pixels[pos] = (0xFF << 24) | (r << 16) | (0);
                    }
                }
            }

            if (method.equals("Negativ")) {
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        int pos = y * width + x;
                        int argb = origPixels[pos]; // Reading the original values

                        int r = (argb >> 16) & 0xff;
                        int g = (argb >> 8)  & 0xff;
                        int b =  argb        & 0xff;

                        r = 255 - r; // inverts r
                        g = 255 - g; // inverts g
                        b = 255 - b; // inverts b

                        pixels[pos] = (0xFF << 24) | (r << 16) | (g << 8) | b;
                    }
                }
            }

            if (method.equals("Graustufen")) {
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        int pos = y * width + x;
                        int argb = origPixels[pos]; // Reading the original values

                        int r = (argb >> 16) & 0xff;
                        int g = (argb >> 8)  & 0xff;
                        int b =  argb        & 0xff;

                        // Calculate the grayscale value
                        int grayscale = (int)(0.299 * r + 0.587 * g + 0.114 * b); // Visually weighted average

                        pixels[pos] = (0xFF << 24) | (grayscale << 16) | (grayscale << 8) | grayscale;
                    }
                }
            }

            if (method.equals("Binärbild")) {
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        int pos = y * width + x;
                        int argb = origPixels[pos]; // Reading the original values

                        int r = (argb >> 16) & 0xff;
                        int g = (argb >> 8)  & 0xff;
                        int b =  argb        & 0xff;

                        // Calculate the grayscale value
                        int grayscale = (int)(0.299 * r + 0.587 * g + 0.114 * b); // Visually weighted average

                        // If the pixels visually weighted average grayscale value is bigger than 127, paint it white,
                        // otherwise paint it black
                        r = g = b = (grayscale > 127) ? 255 : 0;

                        pixels[pos] = (0xFF << 24) | (r << 16) | (g << 8) | b;
                    }
                }
            }

            if (method.equals("5 Graustufen ohne Schwarz und Weiß")) {
                // Divide 255 in 5 parts, which is needed to determine in which part a grayscale value belongs
                double inputStepSize = (double)255 / 5;

                // Divide 255 in 7 values (5 values without 0 and 255), which calculates a factor to multiply the part number with to get the new value
                double outputStepSize = (double)255 / 6;

                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        int pos = y * width + x;
                        int argb = origPixels[pos]; // Reading the original values

                        int r = (argb >> 16) & 0xff;
                        int g = (argb >> 8)  & 0xff;
                        int b =  argb        & 0xff;

                        // Calculate the grayscale value
                        int grayscale = (int)(0.299 * r + 0.587 * g + 0.114 * b); // Visually weighted average

                        // Calculate in which part the grayscale value belongs (+ 1, because we don't want part number 0 here),
                        // and cap the part number at 5, because the grayscale value of 255 results in the part number 6, which we also don't want here
                        int part = (int)(Math.min((grayscale / inputStepSize) + 1, 5));

                        // Calculate the grayscale value by multiplying the part number with the output step size
                        r = g = b = (int)(part * outputStepSize);

                        pixels[pos] = (0xFF << 24) | (r << 16) | (g << 8) | b;
                    }
                }
            }

            if (method.equals("27 Graustufen mit Schwarz und Weiß")) {
                // Divide 255 in 27 parts, which is needed to determine in which part a grayscale value belongs
                double inputStepSize = (double)255 / 27;

                // Divide 255 in 26 values (27 values with 0), which calculates a factor to multiply the part number with to get the new value
                double outputStepSize = (double)255 / 26;

                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        int pos = y * width + x;
                        int argb = origPixels[pos]; // Reading the original values

                        int r = (argb >> 16) & 0xff;
                        int g = (argb >> 8)  & 0xff;
                        int b =  argb        & 0xff;

                        // Calculate the grayscale value
                        int grayscale = (int)(0.299 * r + 0.587 * g + 0.114 * b); // Visually weighted average

                        // Calculate in which part the grayscale value belongs and cap the part number at 26, because the grayscale value of 255
                        // results in the part number 27, which we don't want here
                        int part = (int)Math.min(grayscale / inputStepSize, 26);

                        // Calculate the grayscale value by multiplying the part number with the output step size
                        r = g = b = (int)(part * outputStepSize);

                        pixels[pos] = (0xFF << 24) | (r << 16) | (g << 8) | b;
                    }
                }
            }

            if (method.equals("Binärbild mit vertikaler Fehlerdiffusion")) {
                for (int x = 0; x < width; x++) {
                    int deviation = 0;

                    for (int y = 0; y < height; y++) {
                        int pos = y * width + x;

                        int argb = origPixels[pos]; // Reading the original values

                        int r = (argb >> 16) & 0xff;
                        int g = (argb >> 8)  & 0xff;
                        int b =  argb        & 0xff;

                        // Calculate the grayscale value
                        int grayscale = (int)(0.299 * r + 0.587 * g + 0.114 * b) + deviation; // Visually weighted average

                        // If the grayscale value is bigger than 127, change it to 255, otherwise change it to 0, and then assign it to r, g and b
                        r = g = b = (grayscale > 127) ? 255 : 0;

                        // Update deviation
                        deviation = grayscale - r;

                        pixels[pos] = (0xFF << 24) | (r << 16) | (g << 8) | b;
                    }
                }
            }

            if (method.equals("Sepia-Färbung")) {
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        int pos = y * width + x;
                        int argb = origPixels[pos]; // Reading the original values

                        int r = (argb >> 16) & 0xff;
                        int g = (argb >> 8)  & 0xff;
                        int b =  argb        & 0xff;

                        // Calculate the grayscale value
                        int grayscale = (int)(0.299 * r + 0.587 * g + 0.114 * b); // Visually weighted average

                        // Sepia coloration
                        r = (int)(grayscale * 1.5);
                        g = (int)(grayscale * 1.2);
                        b = (int)(grayscale * 0.8);

                        // Limitation for r and g only, because b can't get a higher value than 255 by multiplying it by 0.8
                        r = Math.max(0, Math.min(255, r));
                        g = Math.max(0, Math.min(255, g));

                        pixels[pos] = (0xFF << 24) | (r << 16) | (g << 8) | b;
                    }
                }
            }

            if (method.equals("9 Farben")) {
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        int pos = y * width + x;
                        int argb = origPixels[pos]; // Reading the original values

                        int r = (argb >> 16) & 0xff;
                        int g = (argb >> 8)  & 0xff;
                        int b =  argb        & 0xff;

                        int[][] colors = { // all 9 colors
                            {20, 25, 24},
                            {48, 50, 49},
                            {77, 75, 72},
                            {116, 101, 90},
                            {157, 149, 144},
                            {193, 193, 195},
                            {222, 218, 217},
                            {49, 102, 138},
                            {94, 128, 158}
                        };

                        // the initial minimum distance is an extremely high value, so that there is
                        // always a distance smaller than that, which results in the if condition at least
                        // being true once, so that rn, gn and bn will be set correctly
                        double minDistance = Integer.MAX_VALUE;
                        int rn = 0, gn = 0, bn = 0;

                        // loop over the nested arrays inside the 2d array
                        for (int[] color : colors) {

                            // calculate the distance
                            double distance = Math.pow(r - color[0], 2) +
                                              Math.pow(g - color[1], 2) +
                                              Math.pow(b - color[2], 2);

                            // each time a smaller distance was found, overwrite the color to be set
                            if (distance < minDistance) {
                                rn = color[0];
                                gn = color[1];
                                bn = color[2];

                                // update the smallest distance found thus far
                                minDistance = distance;
                            }
                        }

                        pixels[pos] = (0xFF << 24) | (rn << 16) | (gn << 8) | bn;
                    }
                }
            }
        }
    } // CustomWindow inner class
}