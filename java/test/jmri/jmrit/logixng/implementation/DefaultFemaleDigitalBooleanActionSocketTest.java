package jmri.jmrit.logixng.implementation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.DigitalBooleanOnChange;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Test ExpressionTimer
 *
 * @author Daniel Bergqvist 2018
 */
public class DefaultFemaleDigitalBooleanActionSocketTest extends FemaleSocketTestBase {

    private MyOnChangeAction _action;

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Override
    protected Manager<? extends NamedBean> getManager() {
        return InstanceManager.getDefault(DigitalBooleanActionManager.class);
    }

    @Test
    public void testBundleClass() {
        Assert.assertEquals("bundle is correct", "Test Bundle bb aa cc", Bundle.getMessage("TestBundle", "aa", "bb", "cc"));
        Assert.assertEquals("bundle is correct", "Generic", Bundle.getMessage(Locale.US, "SocketTypeGeneric"));
        Assert.assertEquals("bundle is correct", "Test Bundle bb aa cc", Bundle.getMessage(Locale.US, "TestBundle", "aa", "bb", "cc"));
    }

    @Test
    public void testGetName() {
        Assert.assertTrue("String matches", "A1".equals(_femaleSocket.getName()));
    }

    @Test
    public void testGetDescription() {
        Assert.assertTrue("String matches", "!b".equals(_femaleSocket.getShortDescription()));
        Assert.assertTrue("String matches", "!b A1".equals(_femaleSocket.getLongDescription()));
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
    public void testSetValue() throws Exception {
        // Every test method should have an assertion
        Assert.assertNotNull("femaleSocket is not null", _femaleSocket);
        Assert.assertFalse("femaleSocket is not connected", _femaleSocket.isConnected());
        // Test execute() when not connected
        ((DefaultFemaleDigitalBooleanActionSocket)_femaleSocket).execute(false, false);
    }

    @Test
    public void testGetConnectableClasses() {
        Map<Category, List<Class<? extends Base>>> map = new HashMap<>();

        List<Class<? extends Base>> classes = new ArrayList<>();
//        classes.add(jmri.jmrit.logixng.actions.ActionLight.class);
        map.put(Category.ITEM, classes);

        classes = new ArrayList<>();
        classes.add(jmri.jmrit.logixng.actions.DigitalBooleanMany.class);
        classes.add(jmri.jmrit.logixng.actions.DigitalBooleanOnChange.class);
        map.put(Category.COMMON, classes);

        classes = new ArrayList<>();
//        classes.add(jmri.jmrit.logixng.actions.ShutdownComputer.class);
        map.put(Category.OTHER, classes);

        Assert.assertTrue("maps are equal",
                isConnectionClassesEquals(map, _femaleSocket.getConnectableClasses()));
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

        LogixNG logixNG = InstanceManager.getDefault(LogixNG_Manager.class)
                .createLogixNG("A new logix for test");  // NOI18N

        ConditionalNG conditionalNG = InstanceManager.getDefault(ConditionalNG_Manager.class)
                .createConditionalNG(logixNG, "A conditionalNG");  // NOI18N
        flag = new AtomicBoolean();
        errorFlag = new AtomicBoolean();
        _action = new MyOnChangeAction("IQDB321");
        DigitalBooleanOnChange otherAction = new MyOnChangeAction("IQDB322");
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
    public void tearDown() {
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.tearDown();
    }



    private class MyOnChangeAction extends DigitalBooleanOnChange {

        private boolean _hasBeenSetup = false;

        public MyOnChangeAction(String systemName) {
            super(systemName, null, DigitalBooleanOnChange.Trigger.CHANGE);
        }

        /** {@inheritDoc} */
        @Override
        public void setup() {
            _hasBeenSetup = true;
        }
    }

}
