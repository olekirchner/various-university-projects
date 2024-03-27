package ue2;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.ImageCanvas;
import ij.gui.ImageWindow;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Panel;

import javax.swing.BorderFactory;
import javax.swing.JSlider;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 Opens an image window and adds a panel below the image
 */
public class GDM_U2_S0589100 implements PlugIn {
    ImagePlus imp; // ImagePlus object
    private int[] origPixels;
    private int width;
    private int height;

    public static void main(String args[]) {
        new ImageJ();
        IJ.open("/Users/ole_kirchner/Documents/studium/grundlagen-digitaler-medien/practice/GDM_UEBUNG_WS23/src/ue2/orchid.jpg");

        GDM_U2_S0589100 pw = new GDM_U2_S0589100();
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


    class CustomWindow extends ImageWindow implements ChangeListener {
        private JSlider jSliderBrightness;
        private JSlider jSliderContrast;
        private JSlider jSliderSaturation;
        private JSlider jSliderHue;
        private double brightness;

        /*
         Initialize contrast and saturation with 1, so that the image doesn't turn grayscale when e.g. moving the brightness slider in the beginning, which doesn't
         update the otherwise initial contrast and saturation value of 0
         */
        private double contrast = 1;
        private double saturation = 1;
        private double hue;

        CustomWindow(ImagePlus imp, ImageCanvas ic) {
            super(imp, ic);
            addPanel();
        }

        void addPanel() {
            Panel panel = new Panel();

            panel.setLayout(new GridLayout(4, 1));

            // Slider for brightness
            jSliderBrightness = makeTitledSlider("Helligkeit", 0, 256, 128);
            jSliderBrightness.setMajorTickSpacing(16); // Overwrite the major tick spacing to fix the offset at 0

            // Slider for contrast
            jSliderContrast = makeTitledSlider("Kontrast", 0, 10, 5);

            // Slider for saturation
            jSliderSaturation = makeTitledSlider("Saturation", 0, 8, 4);
            jSliderSaturation.setMajorTickSpacing(1); // Overwrite the major tick spacing to even show any ticks

            // Slider for hue rotation
            jSliderHue = makeTitledSlider("Hue", 0, 360, 0);

            // Add the sliders to the panel
            panel.add(jSliderBrightness);
            panel.add(jSliderContrast);
            panel.add(jSliderSaturation);
            panel.add(jSliderHue);

            add(panel);
            pack();
        }

        private JSlider makeTitledSlider(String string, int minVal, int maxVal, int val) {
            JSlider slider = new JSlider(JSlider.HORIZONTAL, minVal, maxVal, val );
            Dimension preferredSize = new Dimension(width, 50);
            slider.setPreferredSize(preferredSize);
            TitledBorder tb = new TitledBorder(BorderFactory.createEtchedBorder(),
                    string, TitledBorder.LEFT, TitledBorder.ABOVE_BOTTOM,
                    new Font("Sans", Font.PLAIN, 11));
            slider.setBorder(tb);

            slider.setMajorTickSpacing((maxVal - minVal)/10 );
            slider.setPaintTicks(true);
            slider.addChangeListener(this);

            return slider;
        }

        private void setSliderTitle(JSlider slider, String str) {
            TitledBorder tb = new TitledBorder(BorderFactory.createEtchedBorder(),
                    str, TitledBorder.LEFT, TitledBorder.ABOVE_BOTTOM,
                    new Font("Sans", Font.PLAIN, 11));
            slider.setBorder(tb);
        }

        public void stateChanged( ChangeEvent e ){
            JSlider slider = (JSlider)e.getSource();

            if (slider == jSliderBrightness) {
                brightness = slider.getValue() - 128;

                String str = "Brightness " + brightness;
                setSliderTitle(jSliderBrightness, str);
            }

            if (slider == jSliderContrast) {
                /* The saturation slider has 10 different values. If the returned value is lower than or 5, multiply it by 0.2
                   to set the correct intervals and turn the value 5 into the value 1 on the slider. In the calculation the value gets
                   multiplied by 100, rounded, and then divided by 100 again, to round the second decimal place correctly. This is to
                   fix an unpleasant decimal place (0.6000000000000001). If the returned value is higher than 5, multiply it by 2 to get
                   the values 2, 4, 8 and 10 on the slider.
                 */
                if (slider.getValue() <= 5) {
                    contrast = Math.round(slider.getValue() * 0.2 * 100) / (double)100;
                } else {
                    contrast = (slider.getValue() - 5) * 2;
                }

                String str = "Contrast " + contrast;
                setSliderTitle(jSliderContrast, str);
            }

            if (slider == jSliderSaturation) {
                /* The saturation slider has 8 different values. If the returned value is lower than or 4, multiply it by 0.25
                   to set the correct intervals and turn the value 4 into the value 1 on the slider. If the returned value is higher
                   than 4, subtract 3 from it to get the values 2, 3, 4 and 5 on the slider.
                 */
                if (slider.getValue() <= 4) {
                    saturation = slider.getValue() * 0.25;
                } else {
                    saturation = (slider.getValue() - 3);
                }

                String str = "Saturation " + saturation;
                setSliderTitle(jSliderSaturation, str);
            }

            if (slider == jSliderHue) {
                hue = slider.getValue();
                String str = "Hue " + hue;
                setSliderTitle(jSliderHue, str);
            }

            changePixelValues(imp.getProcessor());

            imp.updateAndDraw();
        }

        private void changePixelValues(ImageProcessor ip) {
            // Array for accessing the pixel values
            int[] pixels = (int[])ip.getPixels();

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int pos = y * width + x;
                    int argb = origPixels[pos];  // Reading the original values

                    int r = (argb >> 16) & 0xff;
                    int g = (argb >>  8) & 0xff;
                    int b =  argb        & 0xff;

                    int rn = (int) (r + brightness);
                    int gn = (int) (g + brightness);
                    int bn = (int) (b + brightness);

                    // RGB to YUV
                    double yLuminance = 0.299 * rn + 0.587 * gn + 0.114 * bn;
                    double uChannel = (bn - yLuminance) * 0.493;
                    double vChannel = (rn - yLuminance) * 0.877;

                    // Brightness
                    yLuminance += brightness;

                    // Contrast
                    yLuminance = contrast * (yLuminance - 127.5) + 127.5; // if the luminance value is lower than 127.5, it gets lower, otherwise it gets higher
                    uChannel *= contrast;
                    vChannel *= contrast;

                    // Saturation
                    uChannel *= saturation;
                    vChannel *= saturation;

                    // Hue
                    double hueInRadians = Math.toRadians(hue); // convert to radians because math.cos/sin take radians as input
                    double uChannelTemp = (Math.cos(hueInRadians) * uChannel - Math.sin(hueInRadians) * vChannel); /* use a temporary variable for the u channel value to not overwrite
                                                                                                                      the uChannel variable used in the vChannel calculation */
                    vChannel = (Math.sin(hueInRadians) * uChannel + Math.cos(hueInRadians) * vChannel);
                    uChannel = uChannelTemp;

                    // YUV to RGB
                    rn = (int)(yLuminance + vChannel / 0.877);
                    bn = (int)(yLuminance + uChannel / 0.493);
                    gn = (int)(1 / 0.587 * yLuminance - 0.299 / 0.587 * rn - 0.114 / 0.587 * bn);

                    // Limitation
                    rn = Math.max(0, Math.min(255, rn));
                    gn = Math.max(0, Math.min(255, gn));
                    bn = Math.max(0, Math.min(255, bn));

                    pixels[pos] = (0xFF<<24) | (rn<<16) | (gn<<8) | bn;
                }
            }
        }
    } // CustomWindow inner class
}