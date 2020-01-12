package jmri.jmrit.jython;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;

import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

/**
 * Invokes Python-language scripts in jython/tests
 *
 * @author	Bob Jacobsen Copyright 2016
 * @author	Paul Bender Copyright (C) 2017
 * @since JMRI 4.3.6
 */
@RunWith(Parameterized.class)
public class SampleScriptTest {

    /**
     * Create the tests from each sample script in test directory
     */
    @Parameters
    public static Collection<File> testsFromDirectory() {
        if (System.getProperty("jmri.skipjythontests", "false").equals("true")) return null; // skipping check

        java.io.File dir = new java.io.File("jython/test");
        java.io.File[] files = dir.listFiles((File a, String b) -> { return b.endsWith(".py");});
        if (files == null) {
            return null;
        }

        else {
            return Arrays.asList(files);
        }

    }

    @Parameter
    public File file = null;

    @Test 
    public void runTest() throws javax.script.ScriptException, java.io.IOException {
        try {
            jmri.script.JmriScriptEngineManager.getDefault().eval(file);
        } catch (javax.script.ScriptException ex1) {
            log.error("ScriptException during test of {}", file, ex1);
            Assert.fail("ScriptException during test of "+file);
        } catch (java.io.IOException ex2) {
            log.error("IOException during test of {}", file, ex2);
            Assert.fail("IOException during test of "+file);
        }
    }
    
    @BeforeClass
    static public void startTests() {
        // this is to System.out because that's where the test output goes
        System.out.println("\njmri.jmrit.jython.SampleScriptTest starts, following output is from script tests");
    }
    
    @Before
    public void setUp() throws Exception {
        jmri.util.JUnitUtil.setUp();
        
        // it's not really understood why, but doing these inside of the 
        // sample Python script doesn't always work; it's as if that
        // is working with a different InstanceManager. So we 
        // include a comprehensive set here.
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.resetProfileManager();
        jmri.util.JUnitUtil.initConfigureManager();
        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
        jmri.util.JUnitUtil.initDebugPowerManager();
        jmri.util.JUnitUtil.initInternalSensorManager();
        jmri.util.JUnitUtil.initInternalTurnoutManager();
        jmri.util.JUnitUtil.initDebugThrottleManager();
    }
        
    @After 
    public void tearDown() throws Exception {
        jmri.util.JUnitUtil.resetWindows(false,false);
        jmri.util.JUnitUtil.tearDown();
    }

    @AfterClass
    static public void endTests() {
        // this is to System.out because that's where the test output goes
        System.out.println("jmri.jmrit.jython.SampleScriptTest ends, above output was from script tests");
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SampleScriptTest.class);

}
