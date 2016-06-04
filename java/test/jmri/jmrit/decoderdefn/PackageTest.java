package jmri.jmrit.decoderdefn;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmrit.decoderdefn package
 *
 * @author	Bob Jacobsen
 * @version	$Revision$
 */
public class PackageTest extends TestCase {

    // from here down is testing infrastructure
    public PackageTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {PackageTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite("jmri.jmrit.decoderdefn");
        suite.addTest(IdentifyDecoderTest.suite());
        suite.addTest(DecoderIndexFileTest.suite());
        suite.addTest(DecoderFileTest.suite());
        suite.addTest(SchemaTest.suite());
        return suite;
    }

}
