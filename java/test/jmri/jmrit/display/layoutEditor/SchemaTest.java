// SchemaTest.java
package jmri.jmrit.display.layoutEditor;

import junit.framework.Test;
import junit.framework.TestSuite;

//import jmri.InstanceManager;
/**
 * Checks of JMRI XML Schema
 *
 * @author Bob Jacobsen Copyright 2009
 * @since 2.5.5
 * @version $Revision$
 */
public class SchemaTest extends jmri.configurexml.SchemaTestBase {

    // from here down is testing infrastructure
    public SchemaTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", SchemaTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite("jmri.jmrit.display.layoutEditor.SchemaTest");

        validateDirectory(suite, "java/test/jmri/jmrit/display/layoutEditor/valid");
        validateDirectoryFail(suite, "java/test/jmri/jmrit/display/layoutEditor/invalid");

        return suite;
    }
}
