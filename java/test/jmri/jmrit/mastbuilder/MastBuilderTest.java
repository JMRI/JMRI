package jmri.jmrit.mastbuilder;

import javax.swing.JFrame;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmrit.mastbuilder package & jmrit.mastbuilder.MastBuilder
 * class.
 *
 * @author	Bob Jacobsen Copyright (C) 2010
 */
public class MastBuilderTest extends TestCase {

    public void testShow() {
        MastBuilderPane p = new MastBuilderPane();
        JFrame j = new JFrame();
        j.add(p);
        j.pack();
        j.setVisible(true);
    }

    // from here down is testing infrastructure
    public MastBuilderTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", MastBuilderTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(MastBuilderTest.class);
        //suite.addTest(...);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

}
