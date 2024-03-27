package ue4;

import ij.*;
import ij.io.*;
import ij.process.*;
import ij.gui.*;

import ij.plugin.filter.*;

public class GDM_U4_S0589100 implements PlugInFilter {
    protected ImagePlus imp;
    final static String[] choices = {"Wisch-Blende", "Weiche Blende", "Ineinanderkopieren", "Schieb-Blende", "Chroma-Keying", "Eigene Überblendung"};

    public int setup(String arg, ImagePlus imp) {
        this.imp = imp;
        return DOES_RGB+STACK_REQUIRED;
    }

    public static void main(String args[]) {
        ImageJ ij = new ImageJ();
        ij.exitWhenQuitting(true);

        IJ.open("/Users/ole_kirchner/Documents/studium/grundlagen-digitaler-medien/practice/GDM_UEBUNG_WS23/src/ue4/StackB.zip");

        GDM_U4_S0589100 sd = new GDM_U4_S0589100();
        sd.imp = IJ.getImage();
        ImageProcessor B_ip = sd.imp.getProcessor();
        sd.run(B_ip);
    }

    public void run(ImageProcessor B_ip) {
        ImageStack stack_B = imp.getStack();

        int length = stack_B.getSize();
        int width  = B_ip.getWidth();
        int height = B_ip.getHeight();

        Opener o = new Opener();

        ImagePlus A = o.openImage("/Users/ole_kirchner/Documents/studium/grundlagen-digitaler-medien/practice/GDM_UEBUNG_WS23/src/ue4", "StackA.zip");
        if (A == null) return;

        ImageProcessor A_ip = A.getProcessor();
        ImageStack stack_A  = A.getStack();

        if (A_ip.getWidth() != width || A_ip.getHeight() != height) {
            IJ.showMessage("Fehler", "Bildgrößen passen nicht zusammen");
            return;
        }

        length = Math.min(length,stack_A.getSize());

        ImagePlus Erg = NewImage.createRGBImage("Ergebnis", width, height, length, NewImage.FILL_BLACK);
        ImageStack stack_Erg  = Erg.getStack();

        GenericDialog gd = new GenericDialog("Überlagerung");
        gd.addChoice("Methode", choices, "");
        gd.showDialog();

        int methode = 0;
        String s = gd.getNextChoice();
        if (s.equals("Wisch-Blende")) methode = 1;
        if (s.equals("Weiche Blende")) methode = 2;
        if (s.equals("Ineinanderkopieren")) methode = 3;
        if (s.equals("Schieb-Blende")) methode = 4;
        if (s.equals("Chroma-Keying")) methode = 5;
        if (s.equals("Eigene Überblendung")) methode = 6;

        int[] pixels_B;
        int[] pixels_A;
        int[] pixels_Erg;

        // For method 5
        // Key color
        int rKeyColor = 224, gKeyColor = 168, bKeyColor = 64;

        // Threshold for the key color
        double lowThreshold = 0.12;
        double highThreshold = 0.42;

        // Maximum distance between two colors in rgb color space
        double maxColorDistanceRGB = Math.sqrt(3 * Math.pow(255, 2));

        // For method 6
        // Variable for the number of bars
        int barCount = 10;

        for (int z = 1; z <= length; z++) {
            pixels_B   = (int[]) stack_B.getPixels(z);
            pixels_A   = (int[]) stack_A.getPixels(z);
            pixels_Erg = (int[]) stack_Erg.getPixels(z);

            // Variables for the x and y progression per frame
            int xProgression = (int)((z - 1.0) * width / (length - 1));
            int yProgression = (int)((z - 1.0) * height / (length - 1));

            // For method 2
            // Calculate current alpha value
            int a = (int)((z - 1) * ((double)255 / (length - 1)));

            // For method 6
            // Variables for the width of each bar and the bar width which should be painted in this frame
            int barWidth = width / barCount;
            int barWidthToPaint = xProgression / barCount;

            int pos = 0;

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++, pos++) {
                    int cA = pixels_A[pos];
                    int rA = (cA & 0xff0000) >> 16;
                    int gA = (cA & 0x00ff00) >> 8;
                    int bA = (cA & 0x0000ff);

                    int cB = pixels_B[pos];
                    int rB = (cB & 0xff0000) >> 16;
                    int gB = (cB & 0x00ff00) >> 8;
                    int bB = (cB & 0x0000ff);

                    // Wipe aperture
                    if (methode == 1) {
                        // For all values of y that are greater or equal than the value of yProgression, show the underlying video, otherwise show the video which wipes in
                        pixels_Erg[pos] = y >= yProgression ? pixels_B[pos] : pixels_A[pos];
                    }

                    // Soft aperture
                    if (methode == 2) {
                        // Alpha overlay formula implementation
                        int r = (a * rA + (255 - a) * rB) / 255;
                        int g = (a * gA + (255 - a) * gB) / 255;
                        int b = (a * bA + (255 - a) * bB) / 255;

                        // Write the calculated colors in the result array
                        pixels_Erg[pos] = 0xFF000000 + ((r & 0xff) << 16) + ((g & 0xff) << 8) + (b & 0xff);
                    }

                    // Overlay
                    if (methode == 3) {
                        // Overlay (A, B)
                        int r = rB <= 128 ? rA * rB / 128 : 255 - ((255 - rA) * (255 - rB) / 128);
                        int g = gB <= 128 ? gA * gB / 128 : 255 - ((255 - gA) * (255 - gB) / 128);
                        int b = bB <= 128 ? bA * bB / 128 : 255 - ((255 - bA) * (255 - bB) / 128);

                        // Overlay (B, A)
                        // int r = rA <= 128 ? rB * rA / 128 : 255 - ((255 - rB) * (255 - rA) / 128);
                        // int g = gA <= 128 ? gB * gA / 128 : 255 - ((255 - gB) * (255 - gA) / 128);
                        // int b = bA <= 128 ? bB * bA / 128 : 255 - ((255 - bB) * (255 - bA) / 128);

                        // Write the calculated colors in the result array
                        pixels_Erg[pos] = 0xFF000000 + ((r & 0xff) << 16) + ((g & 0xff) << 8) + (b & 0xff);
                    }

                    // Slide aperture
                    if (methode == 4) {
                        // For all values of x, that are greater or equal than the value of xProgression, show pixels of the video which will slide out of the frame.
                        // Otherwise, show pixels of the video which will slide into the frame.
                        pixels_Erg[pos] = x >= xProgression ? pixels_B[pos - xProgression] : pixels_A[width + pos - xProgression];
                    }

                    // Chroma keying
                    if (methode == 5) {
                        // Calculate the color distance between the currently looked at pixel and the key color
                        double distance = Math.sqrt(Math.pow(rA - rKeyColor, 2) + Math.pow(gA - gKeyColor, 2) + Math.pow(bA - bKeyColor, 2));

                        // Caulculate a normalized distance between 0 and 1
                        double normalizedDistance = (distance / maxColorDistanceRGB);

                        if (normalizedDistance < lowThreshold) { // If the normalized distance is lower than the low-threshold, show pixels of the background video
                            pixels_Erg[pos] = pixels_B[pos];
                        } else if (normalizedDistance < highThreshold) { // If the normalized distance is higher than the low-threshold, but lower than the high-threshold,
                                                                         // blend the pixels of the background and foreground video together using a normalized weight
                            double normalizedWeight = (normalizedDistance - lowThreshold) / (highThreshold - lowThreshold);

                            // Calculate the blended rgb-colors
                            int blendedR = (int)((normalizedWeight * rA + (1 - normalizedWeight) * rB));
                            int blendedG = (int)((normalizedWeight * gA + (1 - normalizedWeight) * gB));
                            int blendedB = (int)((normalizedWeight * bA + (1 - normalizedWeight) * bB));

                            pixels_Erg[pos] = 0xFF000000 + ((blendedR & 0xff) << 16) + ((blendedG & 0xff) << 8) + (blendedB & 0xff);
                        } else { // Otherwise show pixels of the foreground video
                            pixels_Erg[pos] = pixels_A[pos];
                        }
                    }

                    // Custom crossfade
                    if (methode == 6) {
                        // The condition x % barWidth == 0 sets the start of a bar, from which some pixels from the bar are painted with pixels of video B and some with the pixels of video A
                        if (x % barWidth == 0) {
                            // Loop over the whole width of a bar
                            for (int i = 0; i < barWidth; i++) {
                                if (i < barWidthToPaint) { // As long as the iterator is smaller than the bar width to paint, paint pixels from the video which will be revealed
                                    pixels_Erg[pos + i] = pixels_A[pos + i];
                                } else { // Otherwise, paint pixels from the video which will be covered
                                    pixels_Erg[pos + i] = pixels_B[pos + i];
                                }
                            }
                        }
                    }
                }
            }
        }

        Erg.show();
        Erg.updateAndDraw();
    }
}
