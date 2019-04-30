package jmri.jmrit.display.layoutEditor;

import java.io.File;
import jmri.configurexml.LoadAndStoreTestBase;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

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
@RunWith(Parameterized.class)
public class LayoutEditorLoadAndStoreTest extends LoadAndStoreTestBase {

    @Parameterized.Parameters(name = "{0} (pass={1})")
    public static Iterable<Object[]> data() {
        return getFiles(new File("java/test/jmri/jmrit/display/layoutEditor"), false, true);
    }

    public LayoutEditorLoadAndStoreTest(File inFile, boolean inPass) {
        super(inFile, inPass, SaveType.User, true);
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
