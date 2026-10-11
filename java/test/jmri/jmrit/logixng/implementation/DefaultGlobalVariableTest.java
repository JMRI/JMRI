package jmri.jmrit.logixng.implementation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import jmri.*;
import jmri.jmrit.logixng.NamedTable;
import jmri.jmrit.logixng.NamedTableManager;
import jmri.jmrit.logixng.SymbolTable.InitialValueType;
import jmri.util.JUnitUtil;

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
        assertNotNull(new DefaultGlobalVariable("IQGV10", "MyGlobal"), "exists");
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
        assertEquals("java.util.concurrent.CopyOnWriteArrayList",
                getVariableValue(InitialValueType.Array, "").getClass().getName());
        assertEquals("java.util.concurrent.ConcurrentHashMap",
                getVariableValue(InitialValueType.Map, "").getClass().getName());
        assertTrue((boolean)getVariableValue(InitialValueType.Formula, "1 == 1"));
        assertFalse((boolean)getVariableValue(InitialValueType.Formula, "1 == 2"));
        assertTrue((boolean)getVariableValue(InitialValueType.Boolean, "true"));
        assertFalse((boolean)getVariableValue(InitialValueType.Boolean, "false"));
        assertTrue((boolean)getVariableValue(InitialValueType.Boolean, "True"));
        assertFalse((boolean)getVariableValue(InitialValueType.Boolean, "False"));

        Exception exception = assertThrows(Exception.class, () -> getVariableValue(InitialValueType.Boolean, "trueAaa"));
        assertEquals("Value \"trueAaa\" can't be converted to a boolean", exception.getMessage());
        exception = assertThrows(Exception.class, () -> getVariableValue(InitialValueType.Boolean, "falseAaa"));
        assertEquals("Value \"falseAaa\" can't be converted to a boolean", exception.getMessage());
        exception = assertThrows(Exception.class, () -> getVariableValue(InitialValueType.Boolean, ""));
        assertEquals("Initial data is empty string for global variable \"MyGlobal\". Can't set value to boolean.", exception.getMessage());
        exception = assertThrows(Exception.class, () -> getVariableValue(InitialValueType.Boolean, null));
        assertEquals("Initial data is null for global variable \"MyGlobal\". Can't set value to boolean.", exception.getMessage());

        assertEquals(25, (long)getVariableValue(InitialValueType.Formula, "12*2+1"));
        assertEquals(352, (long)getVariableValue(InitialValueType.Integer, "352"));
        assertNull(getVariableValue(InitialValueType.None, ""));
        assertEquals("Hello", getVariableValue(InitialValueType.String, "Hello"));

        // Assign a copy of a table to a global variable
        NamedTable csvTable = InstanceManager.getDefault(NamedTableManager.class)
                        .loadTableFromCSV("IQT1", null, "scripts:LogixNG/LogixNG_ExampleTable.csv");
        assertNotNull(csvTable);
        Object value = getVariableValue(InitialValueType.LogixNG_Table, csvTable.getSystemName());
        assertNotNull(value);
        assertEquals("java.util.concurrent.ConcurrentHashMap", value.getClass().getName());
        Map<String,Map<String,String>> map = (Map)value;
        assertEquals("Left turnout", map.get("Left").get("Turnouts"));
        assertEquals("Right turnout", map.get("Right").get("Turnouts"));
        assertEquals("IT15", map.get("First siding").get("Turnouts"));
        assertEquals("IT22", map.get("Second siding").get("Turnouts"));
        assertEquals("Left sensor", map.get("Left").get("Sensors"));
        assertEquals("Right sensor", map.get("Right").get("Sensors"));
        assertEquals("IS15", map.get("First siding").get("Sensors"));
        assertEquals("IS22", map.get("Second siding").get("Sensors"));
    }

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
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
