package jmri.script;

import jmri.InstanceManager;
import jmri.Memory;
import jmri.MemoryManager;
import jmri.TurnoutManager;
import jmri.profile.NullProfile;
import jmri.profile.ProfileManager;
import jmri.util.FileUtil;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.python.util.PythonInterpreter;

/**
 *
 * @author Paul Bender Copyright 2017
 * @author Randall Wood Copyright 2019
 */
public class JmriScriptEngineManagerTest {

    private JmriScriptEngineManager jsem;

    @Test
    public void testEval_String_ScriptEngine() throws ScriptException {
        Object result = null;
        TurnoutManager manager = InstanceManager.getDefault(TurnoutManager.class);
        assertNull(result);
        assertNull(manager.getBySystemName("IT1"));
        ScriptEngine engine = jsem.getEngine(JmriScriptEngineManager.PYTHON);
        result = jsem.eval("turnouts.provideTurnout(\"1\")", engine);
        assertNotNull(result);
        assertNotNull(manager.getBySystemName("IT1"));
        assertEquals(manager.getBySystemName("IT1"), result);
    }

    @Test
    public void testEval_File() throws IOException, ScriptException {
        Object result = null;
        TurnoutManager manager = InstanceManager.getDefault(TurnoutManager.class);
        assertNull(result);
        assertNull(manager.getBySystemName("IT1"));
        result = jsem.eval(FileUtil.getFile("program:java/test/jmri/script/exec-file-profile/turnout.py"));
        assertNotNull(result);
        assertNotNull(manager.getBySystemName("IT1"));
        assertEquals(InstanceManager.getDefault(TurnoutManager.class).getBySystemName("IT1"), result);
    }

    @Test
    public void testEval_File_Bindings() throws IOException, ScriptException {
        // first test that test binding is not in default bindings
        // to ensure later part of tests are not obscured by default binding
        jsem.getDefaultContext().getBindings(ScriptContext.GLOBAL_SCOPE).forEach((name, value) -> assertNotEquals("profiles", name));
        // and now test
        JUnitUtil.resetProfileManager();
        Bindings bindings = new SimpleBindings();
        bindings.put("profiles", ProfileManager.getDefault());
        Object result = null;
        ProfileManager manager = ProfileManager.getDefault();
        assertNull(result);
        result = jsem.eval(FileUtil.getFile("program:java/test/jmri/script/exec-file-profile/profile.py"), bindings);
        assertNotNull(result);
        assertEquals(Integer.valueOf(manager.getAutoStartActiveProfileTimeout()), result);
    }

    @Test
    public void testEval_File_Imports() throws IOException, ScriptException {
        Memory result = InstanceManager.getDefault(MemoryManager.class).provide("result");
        jsem.eval(FileUtil.getFile("program:java/test/jmri/script/import/imports.py"));
        assertEquals("foo", result.getValue());
    }

    @Test
    public void testGetDefault() {
        assertEquals("getDefault ==s InstanceManager instance", InstanceManager.getDefault(JmriScriptEngineManager.class), JmriScriptEngineManager.getDefault());
        assertNotEquals("getDefault !=s test object", jsem, JmriScriptEngineManager.getDefault());
    }

    @Test
    public void testGetDefaultContext() {
        assertNotNull(jsem.getDefaultContext());
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

    @Test
    public void testGetEnginePython() {
        assertNotNull(jsem.getEngine(JmriScriptEngineManager.PYTHON));
    }

    @Test
    public void testGetEngineByExtensionPython() throws ScriptException {
        // see https://github.com/jythontools/jython/blob/master/src/org/python/jsr223/PyScriptEngineFactory.java#L22
        assertNotNull(jsem.getEngineByExtension("py"));
    }

    @Test
    public void testGetEngineByMimeTypePython() throws ScriptException {
        // see https://github.com/jythontools/jython/blob/master/src/org/python/jsr223/PyScriptEngineFactory.java#L88
        ScriptEngine engine = jsem.getEngineByMimeType("text/python");
        assertNotNull(engine);
        assertEquals(engine, jsem.getEngineByMimeType("application/python"));
        assertEquals(engine, jsem.getEngineByMimeType("text/x-python"));
        assertEquals(engine, jsem.getEngineByMimeType("application/x-python"));
    }

    @Test
    public void testGetEngineByNamePython() throws ScriptException {
        // see https://github.com/jythontools/jython/blob/master/src/org/python/jsr223/PyScriptEngineFactory.java#L93
        ScriptEngine engine = jsem.getEngineByName("python");
        assertNotNull(engine);
        assertEquals(engine, jsem.getEngineByName("jython"));
    }
    
    @Test
    public void testGetEngineInvalidName() {
        assertNull(jsem.getEngine("invalid"));
    }

    @Test
    public void testGetEngineByExtensionInvalidExtension() {
        try {
            jsem.getEngineByExtension("invalid");
            fail("Expected exception not thrown");
        } catch (ScriptException e) {
            // not asserting full error message because ends with list of known extensions
            // which can vary based on JVM
            JUnitAppender.assertErrorMessageStartsWith("Could not find script engine for extension \"invalid\", expected one of ");
        }
    }

    @Test
    public void testGetEngineByMimeTypeInvalidMimeType() {
        try {
            jsem.getEngineByMimeType("invalid");
            fail("Expected exception not thrown");
        } catch (ScriptException e) {
            // not asserting full error message because ends with list of known extensions
            // which can vary based on JVM
            JUnitAppender.assertErrorMessageStartsWith("Could not find script engine for mime type \"invalid\", expected one of ");
        }
    }

    @Test
    public void testGetEngineByNameInvalidName() {
        try {
            jsem.getEngineByName("invalid");
            fail("Expected exception not thrown");
        } catch (ScriptException e) {
            // not asserting full error message because ends with list of known extensions
            // which can vary based on JVM
            JUnitAppender.assertErrorMessageStartsWith("Could not find script engine for name \"invalid\", expected one of ");
        }
    }

    @Test
    public void testGetFactoryPython() {
        assertNotNull(jsem.getFactory(JmriScriptEngineManager.PYTHON));
    }

    @Test
    public void testGetFactoryByExtensionPython() throws ScriptException {
        // see https://github.com/jythontools/jython/blob/master/src/org/python/jsr223/PyScriptEngineFactory.java#L22
        assertNotNull(jsem.getFactoryByExtension("py"));
    }

    @Test
    public void testGetFactoryByMimeTypePython() throws ScriptException {
        // see https://github.com/jythontools/jython/blob/master/src/org/python/jsr223/PyScriptEngineFactory.java#L88
        ScriptEngineFactory factory = jsem.getFactoryByMimeType("text/python");
        assertNotNull(factory);
        assertEquals(factory, jsem.getFactoryByMimeType("application/python"));
        assertEquals(factory, jsem.getFactoryByMimeType("text/x-python"));
        assertEquals(factory, jsem.getFactoryByMimeType("application/x-python"));
    }

    @Test
    public void testGetFactoryByNamePython() throws ScriptException {
        // see https://github.com/jythontools/jython/blob/master/src/org/python/jsr223/PyScriptEngineFactory.java#L93
        ScriptEngineFactory factory = jsem.getFactoryByName("python");
        assertNotNull(factory);
        assertEquals(factory, jsem.getFactoryByName("jython"));
    }

    @Test
    public void testGetFactoryInvalidName() {
        assertNull(jsem.getFactory("invalid"));
    }

    @Test
    public void testGetFactoryByExtensionInvalidExtension() {
        try {
            jsem.getFactoryByExtension("invalid");
            fail("Expected exception not thrown");
        } catch (ScriptException e) {
            // not asserting full error message because ends with list of known extensions
            // which can vary based on JVM
            JUnitAppender.assertErrorMessageStartsWith("Could not find script engine factory for extension \"invalid\", expected one of ");
        }
    }

    @Test
    public void testGetFactoryByMimeTypeInvalidMimeType() {
        try {
            jsem.getFactoryByMimeType("invalid");
            fail("Expected exception not thrown");
        } catch (ScriptException e) {
            // not asserting full error message because ends with list of known extensions
            // which can vary based on JVM
            JUnitAppender.assertErrorMessageStartsWith("Could not find script engine factory for mime type \"invalid\", expected one of ");
        }
    }

    @Test
    public void testGetFactoryByNameInvalidName() {
        try {
            jsem.getFactoryByName("invalid");
            fail("Expected exception not thrown");
        } catch (ScriptException e) {
            // not asserting full error message because ends with list of known extensions
            // which can vary based on JVM
            JUnitAppender.assertErrorMessageStartsWith("Could not find script engine factory for name \"invalid\", expected one of ");
        }
    }

    @Test
    public void testInitializeAllEngines() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        Field field = jsem.getClass().getDeclaredField("factories");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        HashMap<String, ScriptEngineFactory> factories = (HashMap<String, ScriptEngineFactory>) field.get(jsem);
        field = jsem.getClass().getDeclaredField("engines");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        HashMap<String, ScriptEngine> engines = (HashMap<String, ScriptEngine>) field.get(jsem);
        assertNotEquals("factories is not empty", factories.size());
        assertNotEquals("engines is empty", factories.size(), engines.size());
        jsem.initializeAllEngines();
        assertEquals("one engine per factory", factories.size(), engines.size());
        factories.keySet().forEach(name -> assertNotNull("factory " + name + " has engine", engines.get(name)));
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        // ensure no bindings are null in tests
        JUnitUtil.initDebugPowerManager();
        JUnitUtil.initDebugCommandStation();
        // create an object for test scripts
        JUnitUtil.initInternalTurnoutManager();
        // create the tested object
        jsem = new JmriScriptEngineManager();
    }

    @After
    public void tearDown() {
        jsem = null;
        JUnitUtil.tearDown();
    }

}
