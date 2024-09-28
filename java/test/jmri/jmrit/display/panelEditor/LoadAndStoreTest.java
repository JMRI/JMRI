package jmri.jmrit.display.panelEditor;

import java.io.*;
import java.util.stream.Stream;

import jmri.util.*;
import jmri.jmrit.display.Editor;

import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Test that configuration files can be read and then stored again consistently.
 * When done across various versions of schema, this checks ability to read
 * older files in newer versions; completeness of reading code; etc.
 * <p>
 * Functional checks, that e.g. check the details of a specific type are being
 * read properly, should go into another type-specific test class.
 * <p>
 * The functionality comes from the common base class, this is just here to
 * insert the test suite into the JUnit hierarchy at the right place.
 *
 * @author Bob Jacobsen Copyright 2009, 2014
 * @since 2.5.5 (renamed & reworked in 3.9 series)
 */
public class LoadAndStoreTest extends jmri.configurexml.LoadAndStoreTestBase {

    public static Stream<Arguments> data() {
        return getFiles(new File("java/test/jmri/jmrit/display/panelEditor"), false, true);
    }

    @ParameterizedTest(name = "{index}: {0} (pass={1})")
    @MethodSource("data")
    public void loadAndStoreTest(File file, boolean pass) throws Exception {
        this.loadLoadStoreFileCheck(file);
    }

    public LoadAndStoreTest() {
        super(SaveType.User, true);
    }

    private boolean done;

    /**
     * Also writes out image files from these for later offline checking.This
     * can't be (easily) automated, as the images vary from platform to
     * platform.
     *
     * @param file the file to check
     * @throws Exception on any unexpected exceptional condition
     */
    @Override
    public void loadLoadStoreFileCheck(File file) throws Exception {
        super.loadLoadStoreFileCheck(file);

        done = false;
        ThreadingUtil.runOnGUIDelayed(() -> done = true, 2000);
        JUnitUtil.waitFor(() -> done,"2secs GUI wait did not complete");

        storeAndCompareImage(file);
    }

    // store image(s) of any JFrames
    //  inFile is an XML file
    public void storeAndCompareImage(File inFile) throws Exception {
        int index = 0;
        for (JmriJFrame frame : JmriJFrame.getFrameList()) {
            index++;
            if (frame instanceof Editor) {
                Editor le = (Editor) frame;

                String name = inFile.getName();
                FileUtil.createDirectory(FileUtil.getUserFilesPath() + "temp");
                File outFile = new File(FileUtil.getUserFilesPath() + "temp/" + name + "." + index + ".png");

                java.awt.Dimension size = new java.awt.Dimension(Math.min(le.getTargetPanel().getSize().width, 2000),
                        Math.min(le.getTargetPanel().getSize().height, 1000));
                JUnitSwingUtil.writeDisplayedContentToFile(le.getTargetPanel(),
                        size, new java.awt.Point(0, 0),
                        outFile);

                boolean first = true;
                // and compare that file to a reference
                // right now, we have only macOS reference files
                if (SystemType.isMacOSX()) {
                    findAndComparePngFiles(name, inFile, outFile, index, "macos");
                } else if (SystemType.isWindows()) {
                    findAndComparePngFiles(name, inFile, outFile, index, "windows");
                } else if (SystemType.isLinux()) {
                    if (Boolean.getBoolean("jmri.migrationtests")) {
                        findAndComparePngFiles(name, inFile, outFile, index, "linux");
                    } else {
                        // skip test that does match from one linux (Jenkins) to another (Travis), but remind about it
                        if (first) {
                            log.info("Skipping tricky comparison of panelEditor graphics because jmri.migrationtests not set true");
                        }
                        first = false;
                    }
                }
            }
        }
    }

    protected void findAndComparePngFiles(String name, File inFile, File outFile, int index, String subdir) throws IOException {
        File compFile = new File(inFile.getCanonicalFile().getParentFile().
                getParent() + "/loadref/" + subdir + "/" + name + "." + index + ".png");

        int checkVal = compareImageFiles(compFile, outFile);
        if (checkVal != 0) {
            log.error("Fail to compare new: {}", outFile);
            log.error("Fail to compare ref: {}", compFile);
            Assert.assertEquals("Screenshots didn't compare", 0, checkVal);
        }
    }

    /**
     * @param fileA first image to compare
     * @param fileB second image to compare
     * @return 0 if both image files are equal, -1 if exception, else count of
     *         different pixels; 0 is good.
     */
    public static int compareImageFiles(File fileA, File fileB) {
        try {
            log.info("FileA: " + fileA.toString());
            log.info("FileB: " + fileB.toString());

            // check comparison file exists
            if (!fileA.exists()) {
                log.warn("Comparison file {} doesn't exist, test skipped", fileA.getName());
                return 0;  // consider this passed with message
            }
            // get buffer data from both files
            java.awt.image.BufferedImage biA = javax.imageio.ImageIO.read(fileA);
            java.awt.image.DataBuffer dbA = biA.getData().getDataBuffer();

            java.awt.image.BufferedImage biB = javax.imageio.ImageIO.read(fileB);
            java.awt.image.DataBuffer dbB = biB.getData().getDataBuffer();

            // check sizes
            int sizeA = dbA.getSize();
            int sizeB = dbB.getSize();
            if (sizeA != sizeB) {
                log.warn("Sizes don't match:  {} != {}", sizeA, sizeB);
            }

            int size = Math.min(sizeA, sizeB);
            // compare pixels in buffers
            int retval = 0;
            for (int i = 0; i < size; i++) {
                if (dbA.getElem(i) != dbB.getElem(i)) {
                    retval++;
                    // log.warn("{} {} {} {}", retval, i, dbA.getElem(i), dbB.getElem(i));
                }
            }
            return retval;
        } catch (IOException e) {
            log.error("Exception prevented comparing image files {} {}", fileA, fileB, e);
            return -1;
        }
    }

    @BeforeEach
    @Override
    public void setUp(@TempDir java.io.File tempDir) throws IOException  {
        // This test should not use tempDir because if we do, we cannot download
        // the screenshots of failing tests from GitHub Windows CI.
        super.setUp(FileUtil.getFile(FileUtil.SETTINGS));
        JUnitUtil.initLayoutBlockManager();
    }

    @AfterEach
    @Override
    public void tearDown() {
        // since each file tested will open its own windows, just close any
        // open windows since we can't accurately list them here
        JUnitUtil.resetWindows(false, false);
        super.tearDown();
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LoadAndStoreTest.class);

}
