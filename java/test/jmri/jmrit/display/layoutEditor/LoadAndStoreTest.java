package jmri.jmrit.display.layoutEditor;

import java.io.File;
import jmri.util.*;
import org.junit.runners.Parameterized;
import org.junit.*;
import jmri.util.JUnitUtil;

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

    @Parameterized.Parameters(name = "{0} (pass={1})")
    public static Iterable<Object[]> data() {
        return getFiles(new File("java/test/jmri/jmrit/display/layoutEditor"), false, true);
    }

    public LoadAndStoreTest(File file, boolean pass) {
        super(file, pass, SaveType.User, true); // isGUEonly, as these contain panels, no not headless
    }

    static boolean done;

    /**
     * Also writes out image files from these
     * for later offline checking.  This can't be 
     * (easily) automated, as the images vary from platform
     * to platform.
     */
    @Test
    @Override
    public void loadLoadStoreFileCheck() throws Exception {
        super.loadLoadStoreFileCheck();

        done = false;
        jmri.util.ThreadingUtil.runOnGUIDelayed(()->{ 
                done = true;
            }, 2500);
        jmri.util.JUnitUtil.waitFor(()->{return done;});
        storeImage(this.file);
    }
    
    // store image(s) of any JFrames
    public static void storeImage(java.io.File inFile) throws Exception {
        int index = 0;
        for (jmri.util.JmriJFrame frame : jmri.util.JmriJFrame.getFrameList() ) {
            index++;
            System.out.println("frame "+frame.getTitle());
            String name = inFile.getName();
            FileUtil.createDirectory(FileUtil.getUserFilesPath() + "temp");
            File outFile = new File(FileUtil.getUserFilesPath() + "temp/" + name+"."+index+".png");
            System.out.println(outFile);
            jmri.util.JUnitSwingUtil.writeDisplayedContentToFile(frame, 
                                        frame.size(), new java.awt.Point(0, 0),
                                        outFile);
        }
    }
    
    @Before
    @Override
    public void setUp() {
        super.setUp();
        JUnitUtil.initLayoutBlockManager();
    }

    @After
    @Override
    public void tearDown() {
        // since each file tested will open its own windows, just close any
        // open windows since we can't accurately list them here
        JUnitUtil.resetWindows(false, false);
        super.tearDown();
    }
}
