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
import jmri.jmrit.logixng.actions.DigitalBooleanLogixAction;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Before;
import org.junit.jupiter.api.*;

/**
 * Test ExpressionTimer
 *
 * @author Daniel Bergqvist 2018
 */
public class DefaultFemaleDigitalBooleanActionSocketTest extends FemaleSocketTestBase {

    private MyOnChangeAction _action;

    @Override
    protected Manager<? extends NamedBean> getManager() {
        return InstanceManager.getDefault(DigitalBooleanActionManager.class);
    }

    @Test
    public void testBundleClass() {
        assertEquals( "Test Bundle bb aa cc", Bundle.getMessage("TestBundle", "aa", "bb", "cc"), "bundle is correct");
        assertEquals( "Generic", Bundle.getMessage(Locale.US, "SocketTypeGeneric"), "bundle is correct");
        assertEquals( "Test Bundle bb aa cc", Bundle.getMessage(Locale.US, "TestBundle", "aa", "bb", "cc"), "bundle is correct");
    }

    @Test
    public void testGetName() {
        assertTrue( "A1".equals(_femaleSocket.getName()), "String matches");
    }

    @Test
    public void testGetDescription() {
        assertTrue( "!b".equals(_femaleSocket.getShortDescription()), "String matches");
        assertTrue( "!b A1".equals(_femaleSocket.getLongDescription()), "String matches");
    }

    @Override
    protected FemaleSocket getFemaleSocket(String name) {
        return new DefaultFemaleDigitalBooleanActionSocket(null, new FemaleSocketListener() {
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
        // Test execute() when not connected
        ((DefaultFemaleDigitalBooleanActionSocket)_femaleSocket).execute(false);
    }

    @Test
    public void testGetConnectableClasses() {
        Map<Category, List<Class<? extends Base>>> map = new HashMap<>();

        List<Class<? extends Base>> classes = new ArrayList<>();
//        classes.add(jmri.jmrit.logixng.actions.ActionLight.class);
        map.put(LogixNG_Category.ITEM, classes);

        classes = new ArrayList<>();
        classes.add(jmri.jmrit.logixng.actions.DigitalBooleanMany.class);
        classes.add(jmri.jmrit.logixng.actions.DigitalBooleanLogixAction.class);
        map.put(LogixNG_Category.COMMON, classes);

        classes = new ArrayList<>();
//        classes.add(jmri.jmrit.logixng.actions.ShutdownComputer.class);
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
                .createConditionalNG(logixNG, "A conditionalNG");  // NOI18N
        flag = new AtomicBoolean();
        errorFlag = new AtomicBoolean();
        _action = new MyOnChangeAction("IQDB321");
        DigitalBooleanLogixAction otherAction = new MyOnChangeAction("IQDB322");
        manager = InstanceManager.getDefault(DigitalBooleanActionManager.class);
        maleSocket = ((DigitalBooleanActionManager)manager).registerAction(_action);
        otherMaleSocket = ((DigitalBooleanActionManager)manager).registerAction(otherAction);
        _femaleSocket = new DefaultFemaleDigitalBooleanActionSocket(conditionalNG, new FemaleSocketListener() {
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



    private static class MyOnChangeAction extends DigitalBooleanLogixAction {

        boolean _hasBeenSetup = false;

        MyOnChangeAction(String systemName) {
            super(systemName, null, DigitalBooleanLogixAction.When.Either);
        }

        /** {@inheritDoc} */
        @Override
        public void setup() {
            _hasBeenSetup = true;
        }
    }

}
