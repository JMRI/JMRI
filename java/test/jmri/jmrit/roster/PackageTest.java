// PackageTest.java

package jmri.jmrit.roster;

import jmri.jmrit.XmlFile;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;

import jmri.jmrit.roster.swing.RosterEntryComboBox;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmrit.roster package 
 * @author	Bob Jacobsen     Copyright (C) 2001, 2002, 2012
 * @version     $Revision$
 */
public class PackageTest extends TestCase {


    // from here down is testing infrastructure

    public PackageTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", PackageTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite("jmri.jmrit.roster.PackageTest");
        suite.addTest(jmri.jmrit.roster.BundleTest.suite());
        suite.addTest(jmri.jmrit.roster.RosterEntryTest.suite());
        suite.addTest(jmri.jmrit.roster.RosterTest.suite());

        if (!System.getProperty("jmri.headlesstest","false").equals("true")) {
            suite.addTest(jmri.jmrit.roster.CopyRosterItemActionTest.suite());
            suite.addTest(jmri.jmrit.roster.RosterEntryPaneTest.suite());
            suite.addTest(jmri.jmrit.roster.FunctionLabelPaneTest.suite());
            suite.addTest(jmri.jmrit.roster.IdentifyLocoTest.suite());
        }
        
        suite.addTest(jmri.jmrit.roster.swing.PackageTest.suite());

        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }

}
