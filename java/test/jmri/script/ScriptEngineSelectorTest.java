package jmri.script;

import javax.script.*;

import jmri.*;
import jmri.jmrit.logixng.SocketAlreadyConnectedException;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Test ScriptEngineSelector
 * @author Daniel Bergqvist (C) 2022
 */
public class ScriptEngineSelectorTest {

    private static final String JYTHON_SCRIPT = String.format(
            "import jmri%n" +
            "turnouts.provide(\"IT1\").setState(jmri.Turnout.THROWN)");

    private static final String ECMA_SCRIPT = String.format(
            "var Turnout = Java.type(\"jmri.Turnout\");%n" +
            "turnouts.provide(\"IT1\").setState(Turnout.THROWN);");

    private ScriptEngineSelector _scriptEngineSelector;
    private Turnout _turnout;

    private void runJythonScriptOldStyle() throws ScriptException {
        Bindings bindings = new SimpleBindings();
        JmriScriptEngineManager scriptEngineManager =
                jmri.script.JmriScriptEngineManager.getDefault();
        scriptEngineManager.getEngineByName(JmriScriptEngineManager.JYTHON)
                .eval(JYTHON_SCRIPT, bindings);
    }

    private void runEcmaScriptOldStyle() throws ScriptException {
        Bindings bindings = new SimpleBindings();
        JmriScriptEngineManager scriptEngineManager =
                jmri.script.JmriScriptEngineManager.getDefault();
        scriptEngineManager.getEngineByName("ecmascript")
                .eval(ECMA_SCRIPT, bindings);
    }

    private void runJythonScriptNewStyle() throws ScriptException {
        Bindings bindings = new SimpleBindings();
        ScriptEngineSelector.Engine engine =
                _scriptEngineSelector.getSelectedEngine();
        Assertions.assertNotNull(engine);
        engine.getScriptEngine().eval(JYTHON_SCRIPT, bindings);
    }

    private void runEcmaScriptNewStyle() throws ScriptException {
        Bindings bindings = new SimpleBindings();
        _scriptEngineSelector.setSelectedEngine(ScriptEngineSelector.ECMA_SCRIPT);
        ScriptEngineSelector.Engine engine =
                _scriptEngineSelector.getSelectedEngine();
        Assertions.assertNotNull(engine);
        engine.getScriptEngine().eval(ECMA_SCRIPT, bindings);
    }

    @Test
    public void testJythonOldStyle1() throws JmriException, ScriptException {
        runJythonScriptOldStyle();
        Assert.assertEquals(Turnout.THROWN, _turnout.getState());
    }

    @Test
    public void testJythonOldStyle2() throws JmriException, ScriptException {
        _turnout.setState(Turnout.CLOSED);
        runJythonScriptOldStyle();
        Assert.assertEquals(Turnout.THROWN, _turnout.getState());
    }

    @Test
    public void testEcmaOldStyle1() throws JmriException, ScriptException {
        _turnout.setState(Turnout.CLOSED);
        runEcmaScriptOldStyle();
        Assert.assertEquals(Turnout.THROWN, _turnout.getState());
    }

    @Test
    public void testEcmaOldStyle2() throws JmriException, ScriptException {
        _turnout.setState(Turnout.CLOSED);
        runEcmaScriptOldStyle();
        Assert.assertEquals(Turnout.THROWN, _turnout.getState());
    }

    @Test
    public void testJythonNewStyle1() throws JmriException, ScriptException {
        _turnout.setState(Turnout.CLOSED);
        runJythonScriptNewStyle();
        Assert.assertEquals(Turnout.THROWN, _turnout.getState());
    }

    @Test
    public void testJythonNewStyle2() throws JmriException, ScriptException {
        _turnout.setState(Turnout.CLOSED);
        runJythonScriptNewStyle();
        Assert.assertEquals(Turnout.THROWN, _turnout.getState());
    }

    @Test
    public void testEcmaNewStyle1() throws JmriException, ScriptException {
        _turnout.setState(Turnout.CLOSED);
        runEcmaScriptNewStyle();
        Assert.assertEquals(Turnout.THROWN, _turnout.getState());
    }

    @Test
    public void testEcmaNewStyle2() throws JmriException, ScriptException {
        _turnout.setState(Turnout.CLOSED);
        runEcmaScriptNewStyle();
        Assert.assertEquals(Turnout.THROWN, _turnout.getState());
    }

    // The minimal setup for log4J
    @BeforeEach
    public void setUp() throws SocketAlreadyConnectedException {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalTurnoutManager();
        _turnout = InstanceManager.getDefault(TurnoutManager.class).provide("IT1");
        _scriptEngineSelector = new ScriptEngineSelector();
    }

    @AfterEach
    public void tearDown() {
        _turnout = null;
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }
}
