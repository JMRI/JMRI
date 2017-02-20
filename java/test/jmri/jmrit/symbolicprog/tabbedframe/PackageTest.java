package jmri.jmrit.symbolicprog.tabbedframe;

import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Invokes complete set of tests in the jmri.jmrit.symbolicprog.tabbedframe tree
 *
 * @author	Bob Jacobsen Copyright 2001, 2003, 2012
 * 
 */
public class PackageTest extends TestCase {

    // from here down is testing infrastructure
    public PackageTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", PackageTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite("jmri.jmrit.symbolicprog.tabbedframe.PackageTest");   // no tests in this class itself

        suite.addTest(new JUnit4TestAdapter(PaneProgPaneTest.class));
        suite.addTest(jmri.jmrit.symbolicprog.tabbedframe.PaneProgFrameTest.suite());
        suite.addTest(new JUnit4TestAdapter(jmri.jmrit.symbolicprog.tabbedframe.CheckProgrammerNamesTest.class));
        suite.addTest(new JUnit4TestAdapter(jmri.jmrit.symbolicprog.tabbedframe.SchemaTest.class));
        suite.addTest(jmri.jmrit.symbolicprog.tabbedframe.QualifiedVarTest.suite());
        suite.addTest(new JUnit4TestAdapter(PaneEditActionTest.class));
        suite.addTest(new JUnit4TestAdapter(PaneNewProgActionTest.class));
        suite.addTest(new JUnit4TestAdapter(PaneOpsProgActionTest.class));
        suite.addTest(new JUnit4TestAdapter(PaneProgActionTest.class));
        return suite;
    }
}
