package jmri.jmrit.logixng.implementation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.AnalogActionMemory;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Before;
import org.junit.jupiter.api.*;

/**
 * Test ExpressionTimer
 *
 * @author Daniel Bergqvist 2018
 */
public class DefaultFemaleAnalogActionSocketTest extends FemaleSocketTestBase {

    private String _memorySystemName;
    private Memory _memory;
    private MyAnalogActionMemory _action;

    @Override
    protected Manager<? extends NamedBean> getManager() {
        return InstanceManager.getDefault(AnalogActionManager.class);
    }

    @Test
    public void testBundleClass() {
        assertEquals( "Test Bundle bb aa cc", Bundle.getMessage("TestBundle", "aa", "bb", "cc"), "bundle is correct");
        assertEquals( "Generic", Bundle.getMessage(Locale.US, "SocketTypeGeneric"), "bundle is correct 2");
        assertEquals( "Test Bundle bb aa cc", Bundle.getMessage(Locale.US, "TestBundle", "aa", "bb", "cc"), "bundle is correct 3");
    }

    @Test
    public void testGetName() {
        assertTrue( "A1".equals(_femaleSocket.getName()), "String matches");
    }

    @Test
    public void testGetDescription() {
        assertEquals( "!~", _femaleSocket.getShortDescription(), "String matches");
        assertEquals( "!~ A1", _femaleSocket.getLongDescription(), "String matches 2");
    }

    @Override
    protected FemaleSocket getFemaleSocket(String name) {
        return new DefaultFemaleAnalogActionSocket(null, new FemaleSocketListener() {
            @Override
            public void connected(FemaleSocket socket) {
            }

            @Override
            public void disconnected(FemaleSocket socket) {
            }
        }, name);
    }

    @Override
    protected boolean hasSocketBeenSetup() {
        return _action._hasBeenSetup;
    }

    @Test
    public void testSetValue() throws JmriException {
        // Every test method should have an assertion
        assertNotNull( _femaleSocket, "femaleSocket is not null");
        assertFalse( _femaleSocket.isConnected(), "femaleSocket is not connected");
        // Test setValue() when not connected
        ((DefaultFemaleAnalogActionSocket)_femaleSocket).setValue(0.0);
    }

    @Test
    public void testGetConnectableClasses() {
        Map<Category, List<Class<? extends Base>>> map = new HashMap<>();

        List<Class<? extends Base>> classes = new ArrayList<>();
        classes.add(jmri.jmrit.logixng.actions.AnalogActionLightIntensity.class);
        classes.add(jmri.jmrit.logixng.actions.AnalogActionMemory.class);
        map.put(LogixNG_Category.ITEM, classes);

        classes = new ArrayList<>();
        classes.add(jmri.jmrit.logixng.actions.AnalogMany.class);
        map.put(LogixNG_Category.COMMON, classes);

        classes = new ArrayList<>();
        map.put(LogixNG_Category.OTHER, classes);

        assertTrue( isConnectionClassesEquals(map, _femaleSocket.getConnectableClasses()),
            "maps are equal");
    }
/*
    @Test
    public void testCategory() {
        // Test that the classes method getCategory() returns the same value as
        // the factory.
        Map<Category, List<Class<? extends Base>>> map = femaleSocket.getConnectableClasses();

        for (Map.Entry<Category, List<Class<? extends Base>>> entry : map.entrySet()) {

            for (Class<? extends Base> clazz : entry.getValue()) {
                // The class SwingToolsTest does not have a swing configurator
                SwingConfiguratorInterface iface = SwingTools.getSwingConfiguratorForClass(clazz);
                iface.getConfigPanel();
                Base obj = iface.createNewObject(iface.getAutoSystemName(), null);
                Assert.assertEquals("category is correct", entry.getKey(), obj.getCategory());
            }
        }
    }
*/
    // The minimal setup for log4J
    @Before
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initLogixNGManager();

        LogixNG logixNG = InstanceManager.getDefault(LogixNG_Manager.class)
                .createLogixNG("A new logix for test");  // NOI18N

        ConditionalNG conditionalNG = InstanceManager.getDefault(ConditionalNG_Manager.class)
                .createConditionalNG(logixNG, "A conditionalNG");  // NOI18N
        flag = new AtomicBoolean();
        errorFlag = new AtomicBoolean();
        _memorySystemName = "IM1";
        _memory = InstanceManager.getDefault(MemoryManager.class).provide(_memorySystemName);
        _action = new MyAnalogActionMemory("IQAA321");
        _action.getSelectNamedBean().setNamedBean(_memory);
        AnalogActionBean otherAction = new AnalogActionMemory("IQAA322", null);
        manager = InstanceManager.getDefault(AnalogActionManager.class);
        maleSocket = ((AnalogActionManager)manager).registerAction(_action);
        otherMaleSocket = ((AnalogActionManager)manager).registerAction(otherAction);
        _femaleSocket = new DefaultFemaleAnalogActionSocket(conditionalNG, new FemaleSocketListener() {
            @Override
            public void connected(FemaleSocket socket) {
                flag.set(true);
            }

            @Override
            public void disconnected(FemaleSocket socket) {
                flag.set(true);
            }
        }, "A1");
    }

    @After
    @AfterEach
    public void tearDown() {
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }



    private static class MyAnalogActionMemory extends AnalogActionMemory {

        boolean _hasBeenSetup = false;

        MyAnalogActionMemory(String systemName) {
            super(systemName, null);
        }

        /** {@inheritDoc} */
        @Override
        public void setup() {
            _hasBeenSetup = true;
        }
    }

}
