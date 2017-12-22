package jmri.jmrit.jython;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;
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
        jmri.script.JmriScriptEngineManager.getDefault().eval(file);
    }
        
    @Before
    public void setUp() throws Exception {
        jmri.util.JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initConfigureManager();
        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
        jmri.util.JUnitUtil.initDebugPowerManager();
        jmri.util.JUnitUtil.initInternalSensorManager();
        jmri.util.JUnitUtil.initInternalTurnoutManager();
    }
        
    @After 
    public void tearDown() throws Exception {
        jmri.util.JUnitUtil.tearDown();
    }

}
