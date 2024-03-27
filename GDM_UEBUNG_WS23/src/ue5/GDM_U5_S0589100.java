package ue5;

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
public class GDM_U5_S0589100 implements PlugIn {
    ImagePlus imp; // ImagePlus object
    private int[] origPixels;
    private int width;
    private int height;

    String[] items = {"Original", "Weichgezeichnetes Bild", "Hochpassgefiltertes Bild", "Bild mit verstärkten Kanten"};

    public static void main(String args[]) {
        IJ.open("/Users/ole_kirchner/Documents/studium/grundlagen-digitaler-medien/practice/GDM_UEBUNG_WS23/src/ue5/sail.jpg");

        GDM_U5_S0589100 pw = new GDM_U5_S0589100();
        pw.imp = IJ.getImage();
        pw.run("");
    }

    public void run(String arg) {
        if (imp==null) {
            imp = WindowManager.getCurrentImage();
        }

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
            int[] pixels = (int[])ip.getPixels();

            if (method.equals("Original")) {
                // 1x1 kernel that does nothing
                double[][] kernel = new double[][] {{1}};

                filter(pixels, kernel, 0);
            }

            if (method.equals("Weichgezeichnetes Bild")) {
                // 3x3 blur kernel
                double[][] kernel = new double[][] {
                        {1.0 / 9, 1.0 / 9, 1.0 / 9},
                        {1.0 / 9, 1.0 / 9, 1.0 / 9},
                        {1.0 / 9, 1.0 / 9, 1.0 / 9}
                };

                // 5x5 blur kernel
                // double[][] kernel = new double[][] {
                //         {1.0 / 25, 1.0 / 25, 1.0 / 25, 1.0 / 25, 1.0 / 25},
                //         {1.0 / 25, 1.0 / 25, 1.0 / 25, 1.0 / 25, 1.0 / 25},
                //         {1.0 / 25, 1.0 / 25, 1.0 / 25, 1.0 / 25, 1.0 / 25},
                //         {1.0 / 25, 1.0 / 25, 1.0 / 25, 1.0 / 25, 1.0 / 25},
                //         {1.0 / 25, 1.0 / 25, 1.0 / 25, 1.0 / 25, 1.0 / 25}
                // };

                // Call the filter method and pass the pixels array, the custom kernel and the necessary offset
                filter(pixels, kernel, 0);
            }

            if (method.equals("Hochpassgefiltertes Bild")) {
                // 3x3 highpass kernel
                double[][] kernel = new double[][] {
                        {-1.0 / 9, -1.0 / 9, -1.0 / 9},
                        {-1.0 / 9, 8.0 / 9, -1.0 / 9},
                        {-1.0 / 9, -1.0 / 9, -1.0 / 9}
                };

                // 5x5 highpass filter
                // double[][] kernel = new double[][] {
                //         {-1.0 / 25, -1.0 / 25, -1.0 / 25, -1.0 / 25, -1.0 / 25},
                //         {-1.0 / 25, -1.0 / 25, -1.0 / 25, -1.0 / 25, -1.0 / 25},
                //         {-1.0 / 25, -1.0 / 25, 24.0 / 25, -1.0 / 25, -1.0 / 25},
                //         {-1.0 / 25, -1.0 / 25, -1.0 / 25, -1.0 / 25, -1.0 / 25},
                //         {-1.0 / 25, -1.0 / 25, -1.0 / 25, -1.0 / 25, -1.0 / 25}
                // };

                // Call the filter method and pass the pixels array, the custom kernel and the necessary offset
                filter(pixels, kernel, 128);
            }

            if (method.equals("Bild mit verstärkten Kanten")) {
                // 3x3 unsharp masking kernel
                double[][] kernel = new double[][] {
                        {-1.0 / 9, -1.0 / 9, -1.0 / 9},
                        {-1.0 / 9, 17.0 / 9, -1.0 / 9},
                        {-1.0 / 9, -1.0 / 9, -1.0 / 9}
                };

                // 8x3 unsharp masking kernel
                // double[][] kernel = new double[][] {
                //         {-1.0 / 24, -1.0 / 24, -1.0 / 24, -1.0 / 24, -1.0 / 24, -1.0 / 24, -1.0 / 24, -1.0 / 24},
                //         {-1.0 / 24, -1.0 / 24, -1.0 / 24, 47.0 / 24, -1.0 / 24, -1.0 / 24, -1.0 / 24, -1.0 / 24},
                //         {-1.0 / 24, -1.0 / 24, -1.0 / 24, -1.0 / 24, -1.0 / 24, -1.0 / 24, -1.0 / 24, -1.0 / 24}
                // };

                // Call the filter method and pass the pixels array, the custom kernel and the necessary offset
                filter(pixels, kernel, 0);
            }
        }

        private void filter(int[] pixels, double[][] kernel, int offset) {
            // Store the kernels width and height in variables
            int knlWidth = kernel[0].length;
            int knlHeight = kernel.length;

            // Calculate the offset of the kernel in relation to the current x and y values
            int xKnlOff = knlWidth % 2 == 0 ? knlWidth / -2 + 1 : knlWidth / -2;
            int yKnlOff = knlHeight % 2 == 0 ? knlHeight / -2 + 1 : knlHeight / -2;

            // Loop over every pixel of the original image
            for (int yImg = 0; yImg < height; yImg++) {
                for (int xImg = 0; xImg < width; xImg++) {
                    // Calculate the current pos in the image
                    int pos = yImg * width + xImg;

                    // Create variables for the newly calculated rgb values
                    int r = 0, g = 0, b = 0;

                    // Loop over each kernel value and update the offset used to determine which pixel to use from the original image
                    for (int yKnl = 0, yOff = yKnlOff; yKnl < knlHeight; yKnl++, yOff++) {
                        for (int xKnl = 0, xOff = xKnlOff; xKnl < knlWidth; xKnl++, xOff++) {
                            // Use mirror padding as an edge treatment to have "fake" pixel values to calculate with if the kernel overlaps the image boundaries
                            int xImgWithOff = Math.min(Math.max(xImg + xOff, 0), width - 1);
                            int yImgWithOff = Math.min(Math.max(yImg + yOff, 0), height - 1);

                            // Calculate the position in the original array
                            int imgOffPos = yImgWithOff * width + xImgWithOff;

                            // Accumulate the new rgb-values for the current pixel
                            r += (int)((origPixels[imgOffPos] >> 16 & 0xff) * kernel[yKnl][xKnl]);
                            g += (int)((origPixels[imgOffPos] >> 8 & 0xff) * kernel[yKnl][xKnl]);
                            b += (int)((origPixels[imgOffPos] & 0xff) * kernel[yKnl][xKnl]);
                        }
                    }

                    // Normalize the rgb-values after applying the offset to them
                    r = Math.max(0, Math.min(255, r + offset));
                    g = Math.max(0, Math.min(255, g + offset));
                    b = Math.max(0, Math.min(255, b + offset));

                    // Put the rgb-values into argb format and assign the argb-value to the output array at position pos
                    pixels[pos] = 0xFF000000 + ((r & 0xff) << 16) + ((g & 0xff) << 8) + (b & 0xff);
                }
            }
        }
    } // CustomWindow inner class
}