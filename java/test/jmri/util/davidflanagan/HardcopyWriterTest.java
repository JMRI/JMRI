package jmri.util.davidflanagan;

import javax.swing.JFrame;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import jmri.util.junit.annotations.DisabledIfHeadless;
import org.junit.jupiter.api.*;
import jmri.util.JUnitUtil;
import java.io.IOException;

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

    private void performStandardDrawing(HardcopyWriter hcw, int width)
            throws IOException, HardcopyWriter.ColumnException {
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

            int width = hcw.getPrintablePagesizePoints().width;
            performStandardDrawing(hcw, width);

            // This is what causes the page to get added to the vector of images.
            hcw.pageBreak();

            Vector<Image> images = hcw.getPageImages();
            Assertions.assertNotNull(images, "getImages");
            Assertions.assertEquals(1, images.size(), "getImages");
            Image image = images.get(0);
            Assertions.assertNotNull(image, "getImage");

            Assertions.assertEquals(850, image.getWidth(null), "image width");
            Assertions.assertEquals(1100, image.getHeight(null), "image height");

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

            Assertions.assertEquals(totalPixelValue, pixelInsideBox, "totalPixelValue should match the sum of areas");

        } catch (HardcopyWriter.PrintCanceledException pce) {
            // OK
        } finally {
            if (hcw != null)
                hcw.dispose();
        }
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
        } catch (HardcopyWriter.PrintCanceledException pce) {
            // OK
        } finally {
            if (hcw != null)
                hcw.dispose();
        }
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

            int width = hcw.getPrintablePagesizePoints().width;
            hcw.setFont("Monospaced", Font.PLAIN, 10);

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

            hcw.pageBreak();

            Vector<Image> images = hcw.getPageImages();
            Assertions.assertEquals(1, images.size(), "getImages");
            Image image = images.get(0);

            int totalPixelValue = totalPixelValue((BufferedImage) image, new Rectangle(0, 0, 850, 1100));

            Rectangle boxes[] = {
                    new Rectangle(0, 0, 850, 50),
                    new Rectangle(50, 50, 250, 70),
                    new Rectangle(300, 100, 250, 70),
                    new Rectangle(570, 160, 230, 110),
                    new Rectangle(50, 210, 500, 80)
            };

            int pixelInsideBox = 0;
            for (Rectangle box : boxes) {
                pixelInsideBox += totalPixelValue((BufferedImage) image, box);
            }

            Assertions.assertEquals(totalPixelValue, pixelInsideBox, "totalPixelValue should match the sum of areas");
        } catch (HardcopyWriter.PrintCanceledException pce) {
            // OK
        } finally {
            if (hcw != null)
                hcw.dispose();
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
    }

    @Test
    @DisabledIfHeadless
    public void testCommandRecording() throws IOException, HardcopyWriter.ColumnException {
        JFrame frame = new JFrame();
        HardcopyWriter hcwPreview = null;
        HardcopyWriter hcwPrint = null;
        try {
            Dimension pagesize = new Dimension((int) (8.5 * 72), (int) (11.0 * 72));

            hcwPreview = new HardcopyWriter(frame, "test-preview", null, null, 10, .5 * 72, .5 * 72, .5 * 72, .5 * 72,
                    true, null, null, null, null, pagesize);
            hcwPrint = new HardcopyWriter(frame, "test-print", null, null, 10, .5 * 72, .5 * 72, .5 * 72, .5 * 72,
                    false, "SkipDialog", null, null, null, pagesize);

            String text = "Test line 1";
            hcwPreview.write(text + "\n");
            hcwPrint.write(text + "\n");

            // Verifying v_pos
            Assertions.assertEquals(hcwPreview.getCurrentVPos(), hcwPrint.getCurrentVPos(),
                    "v_pos should match between preview and print");

            // Verifying measure
            Rectangle2D boundsPreview = hcwPreview.measure(text);
            Rectangle2D boundsPrint = hcwPrint.measure(text);
            Assertions.assertEquals(boundsPreview.getWidth(), boundsPrint.getWidth(), 0.01,
                    "measure width should match");
            Assertions.assertEquals(boundsPreview.getHeight(), boundsPrint.getHeight(), 0.01,
                    "measure height should match");

            List<List<HardcopyWriter.PrintCommand>> pages = hcwPrint.getPageCommands();
            Assertions.assertEquals(1, pages.size(), "Should have 1 page recorded");

            List<HardcopyWriter.PrintCommand> firstPage = pages.get(0);

            long drawStringCount = firstPage.stream()
                    .filter(cmd -> cmd instanceof HardcopyWriter.DrawString)
                    .count();

            Assertions.assertTrue(drawStringCount >= 2,
                    "Should have recorded at least content strings (header + content)");
        } catch (HardcopyWriter.PrintCanceledException pce) {
            // OK
        } finally {
            if (hcwPreview != null)
                hcwPreview.dispose();
            if (hcwPrint != null)
                hcwPrint.dispose();
        }
    }

    @Test
    @DisabledIfHeadless
    public void testComparePreviewAndPrintRecording() throws Exception {
        JFrame frame = new JFrame();
        HardcopyWriter hcwPreview = null;
        HardcopyWriter hcwPrint = null;
        try {
            Dimension pagesize = new Dimension((int) (8.5 * 72), (int) (11.0 * 72));

            // 1. Preview mode
            hcwPreview = new HardcopyWriter(frame, "test-compare", null, null, 10, .5 * 72, .5 * 72, .5 * 72, .5 * 72,
                    true, null,
                    null, false, null, pagesize);
            int width = hcwPreview.getPrintablePagesizePoints().width;
            performStandardDrawing(hcwPreview, width);
            hcwPreview.pageBreak();

            Vector<Image> previewImages = hcwPreview.getPageImages();
            BufferedImage previewImg = (BufferedImage) previewImages.get(0);

            // 2. Print mode (bypass dialog)
            hcwPrint = new HardcopyWriter(frame, "test-compare", null, null, 10, .5 * 72, .5 * 72, .5 * 72, .5 * 72,
                    false, "SkipDialog",
                    null, false, null, pagesize);
            performStandardDrawing(hcwPrint, width);
            hcwPrint.pageBreak();

            // Replay commands from print mode into a new image
            BufferedImage replayedImg = new BufferedImage(850, 1100, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = replayedImg.createGraphics();
            g2.setColor(Color.WHITE);
            g2.fillRect(0, 0, 850, 1100);

            double scale = 100.0 / 72.0;
            g2.scale(scale, scale);

            PageFormat pf = new PageFormat();
            Paper paper = new Paper();
            paper.setSize(8.5 * 72, 11 * 72);
            paper.setImageableArea(0, 0, 8.5 * 72, 11 * 72);
            pf.setPaper(paper);

            hcwPrint.print(g2, pf, 0);
            g2.dispose();

            // Compare images
            Assertions.assertEquals(totalPixelValue(previewImg, new Rectangle(0, 0, 850, 1100)),
                    totalPixelValue(replayedImg, new Rectangle(0, 0, 850, 1100)),
                    "Total pixel value should be identical for same drawing operations");

        } catch (HardcopyWriter.PrintCanceledException pce) {
            Assertions.fail("Print job should not have been cancelled");
        } finally {
            if (hcwPreview != null)
                hcwPreview.dispose();
            if (hcwPrint != null)
                hcwPrint.dispose();
        }
    }

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

}
