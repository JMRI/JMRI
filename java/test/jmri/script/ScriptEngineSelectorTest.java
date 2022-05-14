package jmri.script;

import javax.script.*;

import jmri.*;
import jmri.jmrit.logixng.SocketAlreadyConnectedException;
import jmri.util.JUnitUtil;

import org.junit.*;

/**
 * Test ScriptEngineSelector
 * @author Daniel Bergqvist (C) 2022
 */
public class ScriptEngineSelectorTest {

    private static final String SCRIPT = String.format(
            "import jmri%n" +
            "turnouts.provide(\"IT1\").setState(jmri.Turnout.THROWN)");

    private final ScriptEngineSelector _scriptEngineSelector =
            new ScriptEngineSelector();
    private Turnout turnout;

    private void runScriptOldStyle() throws ScriptException {
        Bindings bindings = new SimpleBindings();
        JmriScriptEngineManager scriptEngineManager =
                jmri.script.JmriScriptEngineManager.getDefault();
        scriptEngineManager.getEngineByName(JmriScriptEngineManager.JYTHON)
                .eval(SCRIPT, bindings);
    }

    private void runScriptNewStyle() throws ScriptException {
        Bindings bindings = new SimpleBindings();
        ScriptEngineSelector.Engine engine =
                _scriptEngineSelector.getSelectedEngine();
        engine.getScriptEngine().eval(SCRIPT, bindings);
    }

    @Test
    public void testOldStyle1() throws JmriException, ScriptException {
        runScriptOldStyle();
        Assert.assertEquals(Turnout.THROWN, turnout.getState());
    }

    @Test
    public void testOldStyle2() throws JmriException, ScriptException {
        turnout.setState(Turnout.CLOSED);
        runScriptOldStyle();
        Assert.assertEquals(Turnout.THROWN, turnout.getState());
    }

    @Test
    public void testOldStyle3() throws JmriException, ScriptException {
        turnout.setState(Turnout.CLOSED);
        runScriptOldStyle();
        Assert.assertEquals(Turnout.THROWN, turnout.getState());
    }

    @Test
    public void testNewStyle1() throws JmriException, ScriptException {
        turnout.setState(Turnout.CLOSED);
        runScriptNewStyle();
        Assert.assertEquals(Turnout.THROWN, turnout.getState());
    }

    @Test
    public void testNewStyle2() throws JmriException, ScriptException {
        turnout.setState(Turnout.CLOSED);
        runScriptNewStyle();
        Assert.assertEquals(Turnout.THROWN, turnout.getState());
    }

    @Test
    public void testNewStyle3() throws JmriException, ScriptException {
        turnout.setState(Turnout.CLOSED);
        runScriptNewStyle();
        Assert.assertEquals(Turnout.THROWN, turnout.getState());
    }

    // The minimal setup for log4J
    @Before
    public void setUp() throws SocketAlreadyConnectedException {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalTurnoutManager();
        turnout = InstanceManager.getDefault(TurnoutManager.class).provide("IT1");
    }

    @After
    public void tearDown() {
        turnout = null;
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }
}
