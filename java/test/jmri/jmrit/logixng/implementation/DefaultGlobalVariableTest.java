package jmri.jmrit.logixng.implementation;

import jmri.JmriException;
import jmri.jmrit.logixng.SymbolTable.InitialValueType;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test DefaultGlobalVariable
 *
 * @author Daniel Bergqvist 2022
 */
public class DefaultGlobalVariableTest {

    @Test
    public void testCtor() {
        Assert.assertNotNull("exists", new DefaultGlobalVariable("IQGV10", "MyGlobal"));
    }

    private Object getVariableValue(InitialValueType type, String data)
            throws JmriException {
        var v = new DefaultGlobalVariable("IQGV10", "MyGlobal");
        v.setInitialValueType(type);
        v.setInitialValueData(data);
        v.initialize();
        return v.getValue();
    }

    @Test
    public void testInitialValue() throws JmriException {
        Assert.assertEquals("java.util.concurrent.CopyOnWriteArrayList",
                getVariableValue(InitialValueType.Array, "").getClass().getName());
        Assert.assertEquals("java.util.concurrent.ConcurrentHashMap",
                getVariableValue(InitialValueType.Map, "").getClass().getName());
        Assert.assertEquals(25, (long)getVariableValue(InitialValueType.Formula, "12*2+1"));
        Assert.assertEquals(352, (long)getVariableValue(InitialValueType.Integer, "352"));
        Assert.assertNull(getVariableValue(InitialValueType.None, ""));
        Assert.assertEquals("Hello", getVariableValue(InitialValueType.String, "Hello"));
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initLogixNGManager();
    }

    @After
    public void tearDown() {
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.tearDown();
    }

}
