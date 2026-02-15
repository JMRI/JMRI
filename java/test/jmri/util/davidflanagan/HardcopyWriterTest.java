package jmri.util.davidflanagan;

import javax.swing.JFrame;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.Vector;

import jmri.util.junit.annotations.DisabledIfHeadless;
import org.junit.jupiter.api.*;
import jmri.util.JUnitUtil;
import java.io.IOException;

import javax.imageio.ImageIO;
import java.io.File;

public class HardcopyWriterTest {
    @Test
    @DisabledIfHeadless
    public void testCtor() {
        JFrame frame = new JFrame();
        try {
            HardcopyWriter hcw = new HardcopyWriter(frame, "test", null, null, 10, .5 * 72, .5 * 72, .5 * 72, .5 * 72,
                    true, null, null, null, null, null);
            Assertions.assertNotNull(hcw, "OriginalHardcopyWriter constructor");
            hcw.dispose();
        } catch (HardcopyWriter.PrintCanceledException pce) {
            // this isn't an error for this test.
        }
    }

    @Test
    @DisabledIfHeadless
    public void testPrintSomething() throws IOException, HardcopyWriter.ColumnException {
        JFrame frame = new JFrame();
        HardcopyWriter hcw = null;
        try {
            hcw = new HardcopyWriter(frame, "test", null, null, 10, .5 * 72, .5 * 72, .5 * 72, .5 * 72, true, null,
                    null, null, null, new Dimension((int) (8.5 * 72), (int) (11.0 * 72)));
            Assertions.assertNotNull(hcw, "OriginalHardcopyWriter constructor");
        } catch (HardcopyWriter.PrintCanceledException pce) {
            // this isn't an error for this test.
            return;
        }
        int width = hcw.getPrintablePagesizePoints().width;

        for (String fontName : new String[]{"Monospaced", "SansSerif", "Serif", "Courier New"}) {
            hcw.setFont(fontName, Font.PLAIN, 10);

            // Make three columns that are 1/3 of the page width.
            hcw.setColumns(new HardcopyWriter.Column[]{
                    new HardcopyWriter.Column(0, width / 3),
                    new HardcopyWriter.Column(width / 3, width / 3),
                    new HardcopyWriter.Column(2 * width / 3, width / 3)
            });
            hcw.write("Hello World\tHello World\n");
            hcw.write("Col1\tCol2\t\n");
            hcw.write("\t\tHello from col 3\n");

            hcw.setColumns(new HardcopyWriter.Column[]{
                    new HardcopyWriter.Column(0, width / 3, HardcopyWriter.Align.LEFT),
                    new HardcopyWriter.Column(width / 3, width / 3, HardcopyWriter.Align.CENTER),
                    new HardcopyWriter.Column(2 * width / 3, width / 3, HardcopyWriter.Align.RIGHT)
            });
            hcw.write("Hello World\tHello World\tHello World\n");
            hcw.write("\t\tHello from col 3\n");

        }

        // This is what causes the page to get added to the vector of images.
        hcw.pageBreak();

        Vector<Image> images = hcw.getPageImages();
        Assertions.assertNotNull(images, "getImages");
        Assertions.assertEquals(1, images.size(), "getImages");
        Image image = images.get(0);
        Assertions.assertNotNull(image, "getImage");

        // ISSUE: Not entirely clear how to test this... especially since we don't want to
        // do exact bitwise comparison of the image. 

        // Write out the image to /tmp/test.png
        // We *know* this is a BufferedImage, so we can cast it.
        try {
            ImageIO.write((BufferedImage) image, "png", new File("/tmp/jmri_testPrintSomething.png"));
        } catch (Exception e) {
            log.warn("Failed to save test image");
        }

        Assertions.assertEquals(850, image.getWidth(null), "image width");
        Assertions.assertEquals(1100, image.getHeight(null), "image height");

        Assertions.assertEquals(0, totalPixelValue((BufferedImage) image, new Rectangle(0, 0, 10, 10)),
                "image top left area");
        Assertions.assertEquals(0, totalPixelValue((BufferedImage) image, new Rectangle(840, 1090, 10, 10)),
                "image bottom right area");

        int totalPixelValue = totalPixelValue((BufferedImage) image, new Rectangle(0, 0, 850, 1100));

        // Now we get boxes around bits
        int headerPixelValue = totalPixelValue((BufferedImage) image, new Rectangle(0, 0, 850, 50));
        int col1PixelValue = totalPixelValue((BufferedImage) image, new Rectangle(50, 50, 120, 400));
        int col2PixelValue = totalPixelValue((BufferedImage) image, new Rectangle(291, 50, 190, 400));
        int col3PixelValue = totalPixelValue((BufferedImage) image, new Rectangle(538, 50, 275, 400));

        Assertions.assertEquals(totalPixelValue,
                headerPixelValue + col1PixelValue + col2PixelValue + col3PixelValue,
                "totalPixelValue should match the sum of areas");

        hcw.dispose();
    }

    // A utility method that takes a BufferedImage and Bounds and returns the
    // total pixel value as an integer representing *darkness* i.e. white is 0 and

    private int totalPixelValue(BufferedImage image, Rectangle bounds) {
        int totalRed = 0;
        int totalGreen = 0;
        int totalBlue = 0;
        for (int x = bounds.x; x < bounds.x + bounds.width; x++) {
            for (int y = bounds.y; y < bounds.y + bounds.height; y++) {
                int rgb = image.getRGB(x, y) ^ 0xffffff;
                totalRed += (rgb >> 16) & 0xff;
                totalGreen += (rgb >> 8) & 0xff;
                totalBlue += rgb & 0xff;
            }
        }
        return totalRed + totalGreen + totalBlue;
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(HardcopyWriterTest.class);
}
