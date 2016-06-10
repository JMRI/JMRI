package jmri.jmrit.jython;

import java.io.File;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Invokes Python-language scripts in jython/tests
 *
 * @author	Bob Jacobsen Copyright 2016
 * @since JMRI 4.3.6
 */
public class SampleScriptTest extends TestCase {

    /**
     * Create the tests from each sample script in test directory
     */
    static protected void testsFromDirectory(TestSuite suite) {
        TestSuite subsuite = new TestSuite("Jython sample scripts");
        suite.addTest(subsuite);

        if (System.getProperty("jmri.skipjythontests", "false").equals("true")) return; // skipping check

        java.io.File dir = new java.io.File("jython/test");
        java.io.File[] files = dir.listFiles();
        if (files == null) {
            return;
        }

        for (int i = 0; i < files.length; i++) {
            if (files[i].getName().toLowerCase().endsWith("test.py")) {
                subsuite.addTest(new CheckOneScript(files[i]));
            }
        }
    }

    /**
     * Internal TestCase class to allow separate tests for every file
     */
    static protected class CheckOneScript extends TestCase {
        File file;
        
        public CheckOneScript(File file) {
            super("Test script: " + file);
            this.file = file;
        }

        @Override
        public void runTest() throws javax.script.ScriptException, java.io.IOException {
            jmri.script.JmriScriptEngineManager.getDefault().eval(file);
        }
        
        @Override
        protected void setUp() throws Exception {
            apps.tests.Log4JFixture.setUp();
            super.setUp();
        
            jmri.util.JUnitUtil.resetInstanceManager();
            jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
            jmri.util.JUnitUtil.initDebugPowerManager();
            jmri.util.JUnitUtil.initInternalSensorManager();
            jmri.util.JUnitUtil.initInternalTurnoutManager();
        }
        
        @Override
        protected void tearDown() throws Exception {
            jmri.util.JUnitUtil.resetInstanceManager();
            super.tearDown();
            apps.tests.Log4JFixture.tearDown();
        }
    }


    // from here down is testing infrastructure
    public SampleScriptTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", SampleScriptTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite("jmri.jmrit.jython.SampleScriptTest"); // no tests in this class itself
        testsFromDirectory(suite);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() throws Exception {
        apps.tests.Log4JFixture.setUp();
        super.setUp();
        
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
    }

    protected void tearDown() throws Exception {
        jmri.util.JUnitUtil.resetInstanceManager();
        super.tearDown();
        apps.tests.Log4JFixture.tearDown();
    }
}
