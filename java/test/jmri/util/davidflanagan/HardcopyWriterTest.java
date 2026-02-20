package jmri.util.davidflanagan;

import javax.swing.JFrame;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
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
            Assertions.assertNotNull(hcw, "HardcopyWriter constructor");
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
            Assertions.assertNotNull(hcw, "HardcopyWriter constructor");
        } catch (HardcopyWriter.PrintCanceledException pce) {
            // this isn't an error for this test.
            return;
        }
        int width = hcw.getPrintablePagesizePoints().width;

        for (String fontName : new String[]{"Monospaced", "SansSerif", "Serif", "Dialog"}) {
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
        Rectangle boxes[] = {
                new Rectangle(0, 0, 850, 50),
                new Rectangle(50, 50, 120, 400),
                new Rectangle(291, 50, 190, 400),
                new Rectangle(538, 50, 275, 400)
        };

        int pixelInsideBox = 0;

        for (Rectangle box : boxes) {
            pixelInsideBox += totalPixelValue((BufferedImage) image, box);
        }

        if (totalPixelValue != pixelInsideBox) {
            // This is a bit of a hack, but it's the best we can do.
            // Split the page up into  blocks and then print all a '*' for each block (if it
            // has a non-zero pixel value)
            // The size must divide the image size evenly.
            dumpOutImage((BufferedImage) image, boxes, 5, 10);
        }

        Assertions.assertEquals(totalPixelValue,
                pixelInsideBox,
                "totalPixelValue should match the sum of areas");

        hcw.dispose();
    }

    @Test
    @DisabledIfHeadless
    public void testNoEmptyPage() throws IOException, HardcopyWriter.ColumnException {
        JFrame frame = new JFrame();
        HardcopyWriter hcw = null;
        try {
            hcw = new HardcopyWriter(frame, "test", null, null, 10, .5 * 72, .5 * 72, .5 * 72, .5 * 72, true, null,
                    null, null, null, new Dimension((int) (8.5 * 72), (int) (11.0 * 72)));
            Assertions.assertNotNull(hcw, "HardcopyWriter constructor");
        } catch (HardcopyWriter.PrintCanceledException pce) {
            // this isn't an error for this test.
            return;
        }

        int pageNumber = hcw.getPageNum();

        Assertions.assertEquals(1, pageNumber, "page number");
        while (hcw.getPageNum() == 1) {
            hcw.write("Hello World\n");
        }

        pageNumber = hcw.getPageNum();
        Assertions.assertEquals(2, pageNumber, "page number");

        Vector<Image> images = hcw.getPageImages();

        Assertions.assertNotNull(images, "getImages");
        Assertions.assertEquals(1, images.size(), "getImages");
        hcw.dispose();
    }

    @Test
    @DisabledIfHeadless
    public void testColumnWrap() throws IOException, HardcopyWriter.ColumnException {
        JFrame frame = new JFrame();
        HardcopyWriter hcw = null;
        try {
            hcw = new HardcopyWriter(frame, "test", null, null, 10, .5 * 72, .5 * 72, .5 * 72, .5 * 72, true, null,
                    null, null, null, new Dimension((int) (8.5 * 72), (int) (11.0 * 72)));
            Assertions.assertNotNull(hcw, "HardcopyWriter constructor");
        } catch (HardcopyWriter.PrintCanceledException pce) {
            // this isn't an error for this test.
            return;
        }

        int width = hcw.getPrintablePagesizePoints().width;

        for (String fontName : new String[]{"SansSerif"}) {
            hcw.setFont(fontName, Font.PLAIN, 12);

            // Make three columns that are 1/3 of the page width.
            hcw.setColumns(new HardcopyWriter.Column[]{
                    new HardcopyWriter.Column(0, width / 3, HardcopyWriter.Align.LEFT_WRAP),
                    new HardcopyWriter.Column(width / 3, width / 3, HardcopyWriter.Align.CENTER_WRAP),
                    new HardcopyWriter.Column(2 * width / 3, width / 3, HardcopyWriter.Align.RIGHT_WRAP)
            });
            hcw.write("A long string that should wrap around in the first column (thrice).\n");
            hcw.write("\tA long string that should wrap around in the second column (thrice).\n");
            hcw.write("\t\tA long string that should wrap around in the third column (thrice).\n");
            hcw.write(
                    "A long string that should wrap around in the first column (thrice).\tNot wrap\tAnd also wrap here in the third column.\n");

            hcw.leaveVerticalSpace(36); // half an inch
        }

        // This is what causes the page to get added to the vector of images.
        hcw.pageBreak();

        Vector<Image> images = hcw.getPageImages();
        Assertions.assertNotNull(images, "getImages");
        //Assertions.assertEquals(1, images.size(), "getImages");
        Image image = images.get(0);
        Assertions.assertNotNull(image, "getImage");

        // ISSUE: Not entirely clear how to test this... especially since we don't want to
        // do exact bitwise comparison of the image. 

        // Write out the image to /tmp/test.png
        // We *know* this is a BufferedImage, so we can cast it.
        try {
            ImageIO.write((BufferedImage) image, "png", new File("/tmp/jmri_testColumnWrap.png"));
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
        Rectangle boxes[] = {
                new Rectangle(0, 0, 850, 50),
                new Rectangle(50, 50, 220, 70),
                new Rectangle(300, 110, 250, 70),
                new Rectangle(570, 170, 230, 110),
                new Rectangle(50, 230, 500, 70)
        };

        int pixelInsideBox = 0;

        for (Rectangle box : boxes) {
            pixelInsideBox += totalPixelValue((BufferedImage) image, box);
        }

        if (totalPixelValue != pixelInsideBox) {
            // This is a bit of a hack, but it's the best we can do.
            // Split the page up into  blocks and then print all a '*' for each block (if it
            // has a non-zero pixel value)
            // The size must divide the image size evenly.
            dumpOutImage((BufferedImage) image, boxes, 5, 10);
        }

        Assertions.assertEquals(totalPixelValue,
                pixelInsideBox,
                "totalPixelValue should match the sum of areas");

        hcw.dispose();

    }

    void dumpOutImage(BufferedImage image, Rectangle[] boxes, int xSize, int ySize) {
        for (int y = 0; y < image.getHeight(null); y += ySize) {
            String row = String.format("%04d:", y);
            for (int x = 0; x < image.getWidth(null); x += xSize) {
                if ((x % 100) == 0) {
                    row += "|";
                }
                if (totalPixelValue(image, new Rectangle(x, y, xSize, ySize)) > 0) {
                    // If all the non-zero pixels are inside a box, then use '.' instead
                    // of '*'.
                    boolean allInsideBox = true;
                    for (int ypix = y; ypix < y + ySize && allInsideBox; ypix++) {
                        for (int xpix = x; xpix < x + xSize; xpix++) {
                            if (image.getRGB(xpix, ypix) != 0xffffff) {
                                // This pixel is not white. See if it is inside a box
                                boolean insideBox = false;
                                for (Rectangle box : boxes) {
                                    if (box.contains(xpix, ypix)) {
                                        insideBox = true;
                                        break;
                                    }
                                }
                                if (!insideBox) {
                                    allInsideBox = false;
                                    break;
                                }
                            }
                        }
                    }
                    if (allInsideBox) {
                        row += ".";
                    } else {
                        row += "*";
                    }
                } else {
                    row += " ";
                }
            }
            System.out.println(row);
        }
    }

    @Test
    public void testStretch() {
        List<HardcopyWriter.Column> columns = new ArrayList<>();
        columns.add(new HardcopyWriter.Column(0, 1));
        columns.add(new HardcopyWriter.Column(0, 2));
        columns.add(new HardcopyWriter.Column(0, 3));
        columns.add(new HardcopyWriter.Column(0, 4));

        int gap = 2;
        columns = HardcopyWriter.Column.stretchColumns(columns, 500, gap);

        int totalWidth = 0;
        int lastColEnd = -gap;
        for (HardcopyWriter.Column column : columns) {
            Assertions.assertEquals(lastColEnd + gap, column.position, "column position");
            totalWidth += column.width;
            lastColEnd = column.width + column.position;
        }

        totalWidth += (columns.size() - 1) * gap;
        Assertions.assertEquals(500, totalWidth, "total width");
        Assertions.assertEquals(500, lastColEnd, "lastColEnd");

        columns.get(1).setWidth(50);
        columns = HardcopyWriter.Column.stretchColumns(columns, 500, gap);

        totalWidth = 0;
        lastColEnd = -gap;
        for (HardcopyWriter.Column column : columns) {
            Assertions.assertEquals(lastColEnd + gap, column.position, "column position");
            totalWidth += column.width;
            lastColEnd = column.width + column.position;
        }

        totalWidth += (columns.size() - 1) * gap;
        Assertions.assertEquals(500, totalWidth, "total width");
        Assertions.assertEquals(500, lastColEnd, "lastColEnd");

        Assertions.assertTrue(50 <= columns.get(1).getWidth(), "column width");
        Assertions.assertTrue(60 >= columns.get(1).getWidth(), "column width");
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
