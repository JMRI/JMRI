package jmri.jmrit.logixng.implementation;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import jmri.*;
import jmri.jmrit.logixng.NamedTable;
import jmri.jmrit.logixng.NamedTableManager;
import jmri.jmrit.logixng.SymbolTable.InitialValueType;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

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
    @SuppressWarnings("unchecked")
    public void testInitialValue() throws JmriException, NamedBean.BadUserNameException, NamedBean.BadSystemNameException, IOException {
        Assert.assertEquals("java.util.concurrent.CopyOnWriteArrayList",
                getVariableValue(InitialValueType.Array, "").getClass().getName());
        Assert.assertEquals("java.util.concurrent.ConcurrentHashMap",
                getVariableValue(InitialValueType.Map, "").getClass().getName());
        Assert.assertEquals(25, (long)getVariableValue(InitialValueType.Formula, "12*2+1"));
        Assert.assertEquals(352, (long)getVariableValue(InitialValueType.Integer, "352"));
        Assert.assertNull(getVariableValue(InitialValueType.None, ""));
        Assert.assertEquals("Hello", getVariableValue(InitialValueType.String, "Hello"));

        // Assign a copy of a table to a global variable
        NamedTable csvTable = InstanceManager.getDefault(NamedTableManager.class)
                        .loadTableFromCSV("IQT1", null, "scripts:LogixNG/LogixNG_ExampleTable.csv");
        Assert.assertNotNull(csvTable);
        Object value = getVariableValue(InitialValueType.LogixNG_Table, csvTable.getSystemName());
        Assert.assertNotNull(value);
        Assert.assertEquals("java.util.concurrent.ConcurrentHashMap", value.getClass().getName());
        Map<String,Map<String,String>> map = (Map)value;
        Assert.assertEquals("Left turnout", map.get("Left").get("Turnouts"));
        Assert.assertEquals("Right turnout", map.get("Right").get("Turnouts"));
        Assert.assertEquals("IT15", map.get("First siding").get("Turnouts"));
        Assert.assertEquals("IT22", map.get("Second siding").get("Turnouts"));
        Assert.assertEquals("Left sensor", map.get("Left").get("Sensors"));
        Assert.assertEquals("Right sensor", map.get("Right").get("Sensors"));
        Assert.assertEquals("IS15", map.get("First siding").get("Sensors"));
        Assert.assertEquals("IS22", map.get("Second siding").get("Sensors"));
    }

    // The minimal setup for log4J
    @BeforeEach
    public void setUp(@TempDir File folder) throws java.io.IOException {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager(new jmri.profile.NullProfile(folder));
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initLogixNGManager();
    }

    @AfterEach
    public void tearDown() {
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.tearDown();
    }

}
