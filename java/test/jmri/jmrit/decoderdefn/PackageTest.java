package jmri.jmrit.decoderdefn;

import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmrit.decoderdefn package
 *
 * @author	Bob Jacobsen
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
        suite.addTest(new JUnit4TestAdapter(IdentifyDecoderTest.class));
        suite.addTest(DecoderIndexFileTest.suite());
        suite.addTest(DecoderFileTest.suite());
        suite.addTest(new JUnit4TestAdapter(SchemaTest.class));
        suite.addTest(new JUnit4TestAdapter(DecoderIndexBuilderTest.class));
        suite.addTest(new JUnit4TestAdapter(NameCheckActionTest.class));
        suite.addTest(new JUnit4TestAdapter(DecoderIndexCreateActionTest.class));
        suite.addTest(new JUnit4TestAdapter(InstallDecoderFileActionTest.class));
        suite.addTest(new JUnit4TestAdapter(InstallDecoderURLActionTest.class));
        suite.addTest(new JUnit4TestAdapter(PrintDecoderListActionTest.class));
        suite.addTest(new JUnit4TestAdapter(BundleTest.class));
        // Disabled until #2601 is resolved
        // suite.addTest(new JUnit4TestAdapter(DuplicateTest.class));

        return suite;
    }

}
