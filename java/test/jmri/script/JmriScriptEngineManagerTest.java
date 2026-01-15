package jmri.script;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

import jmri.InstanceManager;
import jmri.Memory;
import jmri.MemoryManager;
import jmri.TurnoutManager;
import jmri.profile.NullProfile;
import jmri.profile.ProfileManager;
import jmri.util.FileUtil;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
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
        TurnoutManager manager = InstanceManager.getDefault(TurnoutManager.class);
        assertNull(manager.getBySystemName("IT1"));
        ScriptEngine engine = jsem.getEngine(JmriScriptEngineManager.JYTHON);
        Object result = jsem.eval("turnouts.provideTurnout(\"1\")", engine);
        assertNotNull(result);
        assertNotNull(manager.getBySystemName("IT1"));
        assertEquals(manager.getBySystemName("IT1"), result);
    }

    @Test
    public void testEval_File() throws IOException, ScriptException {
        TurnoutManager manager = InstanceManager.getDefault(TurnoutManager.class);
        assertNull(manager.getBySystemName("IT1"));
        Object result = jsem.eval(FileUtil.getFile("program:java/test/jmri/script/exec-file-profile/turnout.py"));
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

        ProfileManager manager = ProfileManager.getDefault();
        assertNotNull(manager);
        Object result = jsem.eval(FileUtil.getFile("program:java/test/jmri/script/exec-file-profile/profile.py"), bindings);
        assertNotNull(result);
        assertEquals(manager.getAutoStartActiveProfileTimeout(), result);
    }

    @Test
    public void testEval_File_Imports() throws IOException, ScriptException {
        Memory result = InstanceManager.getDefault(MemoryManager.class).provide("result");
        jsem.eval(FileUtil.getFile("program:java/test/jmri/script/import/imports.py"));
        assertEquals("foo", result.getValue());
    }

    @Test
    public void testGetDefault() {
        assertEquals( InstanceManager.getDefault(JmriScriptEngineManager.class),
            JmriScriptEngineManager.getDefault(), "getDefault ==s InstanceManager instance");
        assertNotEquals( jsem, JmriScriptEngineManager.getDefault(), "getDefault !=s test object");
    }

    @Test
    public void testGetDefaultContext() {
        assertNotNull(jsem.getDefaultContext());
    }

    @Test
    public void testInitializePython() {
        jsem.initializePython();
        assertNull( jsem.getPythonInterpreter(), "no non-engine python");
    }

    @Test
    public void testInitializePythonWithJython() throws IOException {
        // use profile that sets jython.exec=true
        JUnitUtil.resetProfileManager(new NullProfile(new File("java/test/jmri/script/jython-exec-profile")));
        jsem.initializePython();
        PythonInterpreter pi = jsem.getPythonInterpreter();
        assertNotNull( pi, "got non-engine python");
        // now test that bindings are correct
        jsem.getDefaultContext().getBindings(ScriptContext.GLOBAL_SCOPE).forEach((name, value) -> {
            assertNotNull(value);
            var piGet = pi.get(name, value.getClass());
            assertEquals( value, piGet, "value in bindings is in non-engine python");
        });
    }

    @Test
    public void testGetEnginePython() {
        assertNotNull(jsem.getEngine(JmriScriptEngineManager.JYTHON));
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
        ScriptException ex = assertThrows( ScriptException.class, () ->
            jsem.getEngineByExtension("invalid"),
            "Expected exception not thrown");
        assertNotNull(ex);
        // not asserting full error message because ends with list of known extensions
        // which can vary based on JVM
        JUnitAppender.assertErrorMessageStartsWith("Could not find script engine for extension \"invalid\", expected one of ");
    }

    @Test
    public void testGetEngineByMimeTypeInvalidMimeType() {
        ScriptException ex = assertThrows( ScriptException.class, () ->
            jsem.getEngineByMimeType("invalid"),
            "Expected exception not thrown");
        assertNotNull(ex);
        // not asserting full error message because ends with list of known extensions
        // which can vary based on JVM
        JUnitAppender.assertErrorMessageStartsWith("Could not find script engine for mime type \"invalid\", expected one of ");
    }

    @Test
    public void testGetEngineByNameInvalidName() {
        ScriptException ex = assertThrows( ScriptException.class, () ->
            jsem.getEngineByName("invalid"),
            "Expected exception not thrown");
        assertNotNull(ex);
        // not asserting full error message because ends with list of known extensions
        // which can vary based on JVM
        JUnitAppender.assertErrorMessageStartsWith("Could not find script engine for name \"invalid\", expected one of ");
    }

    @Test
    public void testGetFactoryPython() {
        assertNotNull(jsem.getFactory(JmriScriptEngineManager.JYTHON));
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
        ScriptException ex = assertThrows( ScriptException.class, () ->
            jsem.getFactoryByExtension("invalid"),
            "Expected exception not thrown");
        assertNotNull(ex);
        // not asserting full error message because ends with list of known extensions
        // which can vary based on JVM
        JUnitAppender.assertErrorMessageStartsWith("Could not find script engine factory for extension \"invalid\", expected one of ");
    }

    @Test
    public void testGetFactoryByMimeTypeInvalidMimeType() {
        ScriptException ex = assertThrows( ScriptException.class, () ->
            jsem.getFactoryByMimeType("invalid"),
            "Expected exception not thrown");
        assertNotNull(ex);
        // not asserting full error message because ends with list of known extensions
        // which can vary based on JVM
        JUnitAppender.assertErrorMessageStartsWith("Could not find script engine factory for mime type \"invalid\", expected one of ");
    }

    @Test
    public void testGetFactoryByNameInvalidName() {
        ScriptException ex = assertThrows( ScriptException.class, () ->
            jsem.getFactoryByName("invalid"),
            "Expected exception not thrown");
        assertNotNull(ex);
        // not asserting full error message because ends with list of known extensions
        // which can vary based on JVM
        JUnitAppender.assertErrorMessageStartsWith("Could not find script engine factory for name \"invalid\", expected one of ");
    }

    @Test
    public void testInitializeAllEngines() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        Field field = jsem.getClass().getDeclaredField("factories");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        HashMap<String, ScriptEngineFactory> factories = (HashMap<String, ScriptEngineFactory>) field.get(jsem);
        assertNotNull(factories);
        field = jsem.getClass().getDeclaredField("engines");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        HashMap<String, ScriptEngine> engines = (HashMap<String, ScriptEngine>) field.get(jsem);
        assertNotEquals("factories is not empty", factories.size());
        assertNotEquals( factories.size(), engines.size(), "engines is empty");
        jsem.initializeAllEngines();
        assertEquals( factories.size(), engines.size(), "one engine per factory");
        factories.keySet().forEach(name ->
            assertNotNull( engines.get(name), () -> "factory " + name + " has engine"));
    }

    @BeforeEach
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

    @AfterEach
    public void tearDown() {
        jsem = null;
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
