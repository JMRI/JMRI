// GuiUtilBaseTest.java
package jmri.util.swing;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Checks of JMRI XML Schema for GUI definition files.
 *
 * @author Bob Jacobsen Copyright 2011, 2012
 * @since 2.9.3
 * @version $Revision$
 */
public class GuiUtilBaseTest extends jmri.configurexml.SchemaTestBase {

    /**
     * Recursive
     */
    static protected void doDirectory(TestSuite suite, String pathName) {
        validateDirectory(suite, pathName);
        java.io.File dir = new java.io.File(pathName);
        java.io.File[] files = dir.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].getName().equals(".svn")) {
                continue;
            }
            if (files[i].isDirectory()) {
                doDirectory(suite, files[i].getPath());
            }
        }
    }

    // from here down is testing infrastructure
    public GuiUtilBaseTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", GuiUtilBaseTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite("GuiUtilBaseTest");
        return suite;
    }

    private final static Logger log = LoggerFactory.getLogger(GuiUtilBaseTest.class.getName());
}
