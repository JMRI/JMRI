package jmri.script;

import jmri.InstanceManager;
import jmri.profile.NullProfile;
import jmri.util.JUnitUtil;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.io.IOException;

import javax.script.ScriptContext;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.python.util.PythonInterpreter;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class JmriScriptEngineManagerTest {

    private JmriScriptEngineManager jsem;

    @Test
    public void testGetDefault() {
        assertEquals("getDefault ==s InstanceManager instance", InstanceManager.getDefault(JmriScriptEngineManager.class), JmriScriptEngineManager.getDefault());
        assertNotEquals("getDefault !=s test object", jsem, JmriScriptEngineManager.getDefault());
    }

    @Test
    public void testInitializePython() {
        jsem.initializePython();
        assertNull("no non-engine python", jsem.getPythonInterpreter());
    }

    @Test
    public void testInitializePythonWithJython() throws IOException {
        // use profile that sets jython.exec=true
        JUnitUtil.resetProfileManager(new NullProfile(new File("java/test/jmri/script/jython-exec-profile")));
        jsem.initializePython();
        PythonInterpreter pi = jsem.getPythonInterpreter();
        assertNotNull("got non-engine python", pi);
        // now test that bindings are correct
        jsem.getDefaultContext().getBindings(ScriptContext.GLOBAL_SCOPE)
                .forEach((name, value) -> assertEquals("value in bindings is in non-engine python", value, pi.get(name, value.getClass())));
    }
    
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initDebugPowerManager();
        JUnitUtil.initDebugCommandStation();
        jsem = new JmriScriptEngineManager();
    }

    @After
    public void tearDown() {
        jsem = null;
        JUnitUtil.tearDown();
    }

}
