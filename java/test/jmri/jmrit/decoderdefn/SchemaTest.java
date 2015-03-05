// SchemaTest.java
package jmri.jmrit.decoderdefn;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import jmri.InstanceManager;
/**
 * Checks of JMRI XML Schema for decoder definition files.
 *
 * @author Bob Jacobsen Copyright 2010
 * @since 2.9.3
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
        TestSuite suite = new TestSuite("jmri.jmrit.decoderdefn.SchemaTest"); // no tests in this class itself

        // Some specific files for early tests
        validateDirectory(suite, "java/test/jmri/jmrit/decoderdefn/");

        validateSubdirectories(suite, "xml/decoders/");
        validateDirectory(suite, "xml/decoders/");

        return suite;
    }

    static Logger log = LoggerFactory.getLogger(SchemaTest.class.getName());
}
