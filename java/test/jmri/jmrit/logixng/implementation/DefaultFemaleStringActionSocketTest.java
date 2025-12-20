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
import jmri.jmrit.logixng.actions.StringActionMemory;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Before;
import org.junit.jupiter.api.*;

/**
 * Test ExpressionTimer
 *
 * @author Daniel Bergqvist 2018
 */
public class DefaultFemaleStringActionSocketTest extends FemaleSocketTestBase {

    private String _memorySystemName;
    private Memory _memory;
    private MyStringActionMemory _action;

    @Override
    protected Manager<? extends NamedBean> getManager() {
        return InstanceManager.getDefault(StringActionManager.class);
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
        assertTrue( "!s".equals(_femaleSocket.getShortDescription()), "String matches");
        assertTrue( "!s A1".equals(_femaleSocket.getLongDescription()), "String matches 2");
    }

    @Override
    protected FemaleSocket getFemaleSocket(String name) {
        return new DefaultFemaleStringActionSocket(null, new FemaleSocketListener() {
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
        ((DefaultFemaleStringActionSocket)_femaleSocket).setValue("");
    }

    @Test
    public void testGetConnectableClasses() {
        Map<Category, List<Class<? extends Base>>> map = new HashMap<>();

        List<Class<? extends Base>> classes = new ArrayList<>();
        classes.add(jmri.jmrit.logixng.actions.StringActionMemory.class);
        classes.add(jmri.jmrit.logixng.actions.StringActionStringIO.class);
        map.put(LogixNG_Category.ITEM, classes);

        classes = new ArrayList<>();
        classes.add(jmri.jmrit.logixng.actions.StringMany.class);
        map.put(LogixNG_Category.COMMON, classes);

        classes = new ArrayList<>();
        map.put(LogixNG_Category.OTHER, classes);

        assertTrue( isConnectionClassesEquals(map, _femaleSocket.getConnectableClasses()),
            "maps are equal");
    }

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
                .createConditionalNG(logixNG, "An empty conditionalNG");

        flag = new AtomicBoolean();
        errorFlag = new AtomicBoolean();
        _memorySystemName = "IM1";
        _memory = InstanceManager.getDefault(MemoryManager.class).provide(_memorySystemName);
        _action = new MyStringActionMemory("IQSA321");
        _action.getSelectNamedBean().setNamedBean(_memory);
        StringActionMemory otherAction = new StringActionMemory("IQSA322", null);
        manager = InstanceManager.getDefault(StringActionManager.class);
        maleSocket = ((StringActionManager)manager).registerAction(_action);
        otherMaleSocket = ((StringActionManager)manager).registerAction(otherAction);
        _femaleSocket = new DefaultFemaleStringActionSocket(conditionalNG, new FemaleSocketListener() {
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



    private static class MyStringActionMemory extends StringActionMemory {

        boolean _hasBeenSetup = false;

        MyStringActionMemory(String systemName) {
            super(systemName, null);
        }

        /** {@inheritDoc} */
        @Override
        public void setup() {
            _hasBeenSetup = true;
        }
    }

}
