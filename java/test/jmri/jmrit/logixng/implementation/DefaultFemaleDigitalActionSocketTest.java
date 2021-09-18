package jmri.jmrit.logixng.implementation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import jmri.*;
import jmri.jmrit.display.logixng.CategoryDisplay;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.ActionTurnout;
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
public class DefaultFemaleDigitalActionSocketTest extends FemaleSocketTestBase {

    private MyActionTurnout _action;

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Override
    protected Manager<? extends NamedBean> getManager() {
        return InstanceManager.getDefault(DigitalActionManager.class);
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
        Assert.assertTrue("String matches", "!".equals(_femaleSocket.getShortDescription()));
        Assert.assertTrue("String matches", "! A1".equals(_femaleSocket.getLongDescription()));
    }

    @Override
    protected FemaleSocket getFemaleSocket(String name) {
        return new DefaultFemaleDigitalActionSocket(null, new FemaleSocketListener() {
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
        ((DefaultFemaleDigitalActionSocket)_femaleSocket).execute();
    }

    @Test
    public void testGetConnectableClasses() {
        Map<Category, List<Class<? extends Base>>> map = new HashMap<>();

        List<Class<? extends Base>> classes = new ArrayList<>();
        classes.add(jmri.jmrit.logixng.actions.ActionAudio.class);
        classes.add(jmri.jmrit.logixng.actions.ActionBlock.class);
        classes.add(jmri.jmrit.logixng.actions.ActionClock.class);
        classes.add(jmri.jmrit.logixng.actions.ActionEntryExit.class);
        classes.add(jmri.jmrit.logixng.actions.ActionLight.class);
        classes.add(jmri.jmrit.logixng.actions.ActionLightIntensity.class);
        classes.add(jmri.jmrit.logixng.actions.ActionLocalVariable.class);
        classes.add(jmri.jmrit.logixng.actions.ActionMemory.class);
        classes.add(jmri.jmrit.logixng.actions.ActionOBlock.class);
        classes.add(jmri.jmrit.logixng.actions.ActionPower.class);
        classes.add(jmri.jmrit.logixng.actions.ActionReporter.class);
        classes.add(jmri.jmrit.logixng.actions.ActionScript.class);
        classes.add(jmri.jmrit.logixng.actions.ActionSensor.class);
        classes.add(jmri.jmrit.logixng.actions.ActionSignalHead.class);
        classes.add(jmri.jmrit.logixng.actions.ActionSignalMast.class);
        classes.add(jmri.jmrit.logixng.actions.ActionSound.class);
        classes.add(jmri.jmrit.logixng.actions.ActionThrottle.class);
        classes.add(jmri.jmrit.logixng.actions.ActionTurnout.class);
        classes.add(jmri.jmrit.logixng.actions.ActionTurnoutLock.class);
        classes.add(jmri.jmrit.logixng.actions.ActionWarrant.class);
        classes.add(jmri.jmrit.logixng.actions.EnableLogix.class);
        classes.add(jmri.jmrit.logixng.actions.TriggerRoute.class);
        map.put(Category.ITEM, classes);

        classes = new ArrayList<>();
        classes.add(jmri.jmrit.logixng.actions.ActionTimer.class);
        classes.add(jmri.jmrit.logixng.actions.DoAnalogAction.class);
        classes.add(jmri.jmrit.logixng.actions.DoStringAction.class);
        classes.add(jmri.jmrit.logixng.actions.ExecuteDelayed.class);
        classes.add(jmri.jmrit.logixng.actions.For.class);
        classes.add(jmri.jmrit.logixng.actions.IfThenElse.class);
        classes.add(jmri.jmrit.logixng.actions.DigitalMany.class);
        classes.add(jmri.jmrit.logixng.actions.Sequence.class);
        classes.add(jmri.jmrit.logixng.actions.TableForEach.class);
        map.put(Category.COMMON, classes);

        classes = new ArrayList<>();
        classes.add(jmri.jmrit.display.logixng.ActionPositionable.class);
        map.put(CategoryDisplay.DISPLAY, classes);

        classes = new ArrayList<>();
        classes.add(jmri.jmrit.logixng.actions.ActionListenOnBeans.class);
        classes.add(jmri.jmrit.logixng.actions.ActionListenOnBeansTable.class);
        classes.add(jmri.jmrit.logixng.actions.AddToPriorityFIFOQueue.class);
        classes.add(jmri.jmrit.logixng.actions.Delay.class);
        classes.add(jmri.jmrit.logixng.actions.DigitalCallModule.class);
        classes.add(jmri.jmrit.logixng.actions.Logix.class);
        classes.add(jmri.jmrit.logixng.actions.LogData.class);
        classes.add(jmri.jmrit.logixng.actions.LogLocalVariables.class);
        classes.add(jmri.jmrit.logixng.actions.PriorityFIFOQueue.class);
        classes.add(jmri.jmrit.logixng.actions.ShutdownComputer.class);
        classes.add(jmri.jmrit.logixng.actions.WebBrowser.class);
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
        _action = new MyActionTurnout("IQDA321");
        ActionTurnout otherAction = new MyActionTurnout("IQDA322");
        manager = InstanceManager.getDefault(DigitalActionManager.class);
        maleSocket = ((DigitalActionManager)manager).registerAction(_action);
        otherMaleSocket = ((DigitalActionManager)manager).registerAction(otherAction);
        _femaleSocket = new DefaultFemaleDigitalActionSocket(conditionalNG, new FemaleSocketListener() {
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
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.deregisterEditorManagerShutdownTask();
        JUnitUtil.tearDown();
    }



    private class MyActionTurnout extends ActionTurnout {

        private boolean _hasBeenSetup = false;

        public MyActionTurnout(String systemName) {
            super(systemName, null);
        }

        /** {@inheritDoc} */
        @Override
        public void setup() {
            _hasBeenSetup = true;
        }
    }

}
