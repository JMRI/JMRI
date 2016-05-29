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
 */
public class SampleScriptTest extends TestCase {

    // This is just a placeholder now.
    public void testFirstOne() throws javax.script.ScriptException, java.io.IOException {
        String name = "jmri_bindings_test.py";
        jmri.script.JmriScriptEngineManager.getDefault().eval(new File("jython/test/"+name));
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
        TestSuite suite = new TestSuite(SampleScriptTest.class);
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
