package jmri.jmrix.loconet.sdf;

import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.loconet.sdf package.
 *
 * @author	Bob Jacobsen Copyright 2007
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
        TestSuite suite = new TestSuite("jmri.jmrix.loconet.sdf.SdfTest");  // no tests in this class itself
        suite.addTest(InitiateSoundTest.suite());
        suite.addTest(PlayTest.suite());
        suite.addTest(SdfBufferTest.suite());
        suite.addTest(new JUnit4TestAdapter(BranchToTest.class));
        suite.addTest(new JUnit4TestAdapter(ChannelStartTest.class));
        suite.addTest(new JUnit4TestAdapter(CommentMacroTest.class));
        suite.addTest(new JUnit4TestAdapter(DelaySoundTest.class));
        suite.addTest(new JUnit4TestAdapter(EndSoundTest.class));
        suite.addTest(new JUnit4TestAdapter(FourByteMacroTest.class));
        suite.addTest(new JUnit4TestAdapter(GenerateTriggerTest.class));
        suite.addTest(new JUnit4TestAdapter(LabelMacroTest.class));
        suite.addTest(new JUnit4TestAdapter(MaskCompareTest.class));
        suite.addTest(new JUnit4TestAdapter(LoadModifierTest.class));
        suite.addTest(new JUnit4TestAdapter(SdlVersionTest.class));
        suite.addTest(new JUnit4TestAdapter(SkemeStartTest.class));
        suite.addTest(new JUnit4TestAdapter(SkipOnTriggerTest.class));
        suite.addTest(new JUnit4TestAdapter(TwoByteMacroTest.class));
        return suite;
    }

}
