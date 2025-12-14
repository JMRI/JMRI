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
import jmri.jmrit.display.logixng.CategoryDisplay;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.ActionTurnout;
import jmri.jmrit.operations.logixng.CategoryOperations;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Before;
import org.junit.jupiter.api.*;

/**
 * Test ExpressionTimer
 *
 * @author Daniel Bergqvist 2018
 */
public class DefaultFemaleDigitalActionSocketTest extends FemaleSocketTestBase {

    private MyActionTurnout _action;

    @Override
    protected Manager<? extends NamedBean> getManager() {
        return InstanceManager.getDefault(DigitalActionManager.class);
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
        assertTrue( "!".equals(_femaleSocket.getShortDescription()), "String matches");
        assertTrue( "! A1".equals(_femaleSocket.getLongDescription()), "String matches 2");
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
    public void testSetValue() throws JmriException {
        // Every test method should have an assertion
        assertNotNull( _femaleSocket, "femaleSocket is not null");
        assertFalse( _femaleSocket.isConnected(), "femaleSocket is not connected");
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
        classes.add(jmri.jmrit.logixng.actions.ActionClockRate.class);
        classes.add(jmri.jmrit.logixng.actions.ActionDispatcher.class);
        classes.add(jmri.jmrit.logixng.actions.ActionEntryExit.class);
        classes.add(jmri.jmrit.logixng.actions.ActionLight.class);
        classes.add(jmri.jmrit.logixng.actions.ActionLightIntensity.class);
        classes.add(jmri.jmrit.logixng.actions.ActionLocalVariable.class);
        classes.add(jmri.jmrit.logixng.actions.ActionMemory.class);
        classes.add(jmri.jmrit.logixng.actions.ActionOBlock.class);
        classes.add(jmri.jmrit.logixng.actions.ActionPower.class);
        classes.add(jmri.jmrit.logixng.actions.ActionRequestUpdateAllSensors.class);
        classes.add(jmri.jmrit.logixng.actions.ActionRequestUpdateOfSensor.class);
        classes.add(jmri.jmrit.logixng.actions.ActionRequestUpdateOfTurnout.class);
        classes.add(jmri.jmrit.logixng.actions.ActionReporter.class);
        classes.add(jmri.jmrit.logixng.actions.ActionScript.class);
        classes.add(jmri.jmrit.logixng.actions.ActionSensor.class);
        classes.add(jmri.jmrit.logixng.actions.ActionSetReporter.class);
        classes.add(jmri.jmrit.logixng.actions.ActionSignalHead.class);
        classes.add(jmri.jmrit.logixng.actions.ActionSignalMast.class);
        classes.add(jmri.jmrit.logixng.actions.ActionSound.class);
        classes.add(jmri.jmrit.logixng.actions.ActionTable.class);
        classes.add(jmri.jmrit.logixng.actions.ActionThrottle.class);
        classes.add(jmri.jmrit.logixng.actions.ActionThrottleFunction.class);
        classes.add(jmri.jmrit.logixng.actions.ActionTurnout.class);
        classes.add(jmri.jmrit.logixng.actions.ActionTurnoutLock.class);
        classes.add(jmri.jmrit.logixng.actions.ActionWarrant.class);
        classes.add(jmri.jmrit.logixng.actions.EnableLogix.class);
        classes.add(jmri.jmrit.logixng.actions.EnableLogixNG.class);
        classes.add(jmri.jmrit.logixng.actions.ProgramOnMain.class);
        classes.add(jmri.jmrit.logixng.actions.TriggerRoute.class);
        map.put(LogixNG_Category.ITEM, classes);

        classes = new ArrayList<>();
        classes.add(jmri.jmrit.logixng.actions.ActionTimer.class);
        classes.add(jmri.jmrit.logixng.actions.DigitalFormula.class);
        classes.add(jmri.jmrit.logixng.actions.DoAnalogAction.class);
        classes.add(jmri.jmrit.logixng.actions.DoStringAction.class);
        classes.add(jmri.jmrit.logixng.actions.ExecuteDelayed.class);
        classes.add(jmri.jmrit.logixng.actions.DigitalMany.class);
        map.put(LogixNG_Category.COMMON, classes);

        classes = new ArrayList<>();
        classes.add(jmri.jmrit.logixng.actions.Break.class);
        classes.add(jmri.jmrit.logixng.actions.Continue.class);
        classes.add(jmri.jmrit.logixng.actions.DigitalCallModule.class);
        classes.add(jmri.jmrit.logixng.actions.Error.class);
        classes.add(jmri.jmrit.logixng.actions.Exit.class);
        classes.add(jmri.jmrit.logixng.actions.For.class);
        classes.add(jmri.jmrit.logixng.actions.ForEach.class);
        classes.add(jmri.jmrit.logixng.actions.ForEachWithDelay.class);
        classes.add(jmri.jmrit.logixng.actions.IfThenElse.class);
        classes.add(jmri.jmrit.logixng.actions.Return.class);
        classes.add(jmri.jmrit.logixng.actions.RunOnce.class);
        classes.add(jmri.jmrit.logixng.actions.Sequence.class);
        classes.add(jmri.jmrit.logixng.actions.TableForEach.class);
        classes.add(jmri.jmrit.logixng.actions.ValidationError.class);
        map.put(LogixNG_Category.FLOW_CONTROL, classes);

        classes = new ArrayList<>();
        classes.add(jmri.jmrit.display.logixng.ActionAudioIcon.class);
        classes.add(jmri.jmrit.display.logixng.ActionLayoutTurnout.class);
        classes.add(jmri.jmrit.display.logixng.ActionPositionable.class);
        classes.add(jmri.jmrit.display.logixng.ActionPositionableByClass.class);
        classes.add(jmri.jmrit.display.logixng.WindowManagement.class);
        map.put(CategoryDisplay.DISPLAY, classes);

        classes = new ArrayList<>();
        classes.add(jmri.jmrit.operations.logixng.OperationsProStartAutomation.class);
        map.put(CategoryOperations.OPERATIONS, classes);

        classes = new ArrayList<>();
        classes.add(jmri.jmrit.logixng.actions.ActionCreateBeansFromTable.class);
        classes.add(jmri.jmrit.logixng.actions.ActionFindTableRowOrColumn.class);
        classes.add(jmri.jmrit.logixng.actions.ActionListenOnBeans.class);
        classes.add(jmri.jmrit.logixng.actions.ActionListenOnBeansLocalVariable.class);
        classes.add(jmri.jmrit.logixng.actions.ActionListenOnBeansTable.class);
        classes.add(jmri.jmrit.logixng.actions.ActionShutDownTask.class);
        classes.add(jmri.jmrit.logixng.actions.ExecuteAction.class);
        classes.add(jmri.jmrit.logixng.actions.ExecuteProgram.class);
        classes.add(jmri.jmrit.logixng.actions.JsonDecode.class);
        classes.add(jmri.jmrit.logixng.actions.Logix.class);
        classes.add(jmri.jmrit.logixng.actions.LogData.class);
        classes.add(jmri.jmrit.logixng.actions.LogLocalVariables.class);
        classes.add(jmri.jmrit.logixng.actions.Timeout.class);
        classes.add(jmri.jmrit.logixng.actions.ShowDialog.class);
        classes.add(jmri.jmrit.logixng.actions.ShutdownComputer.class);
        classes.add(jmri.jmrit.logixng.actions.SimulateTurnoutFeedback.class);
        classes.add(jmri.jmrit.logixng.actions.WebBrowser.class);
        classes.add(jmri.jmrit.logixng.actions.WebRequest.class);
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
        JUnitUtil.initDebugThrottleManager();
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
    @AfterEach
    public void tearDown() {
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }



    private static class MyActionTurnout extends ActionTurnout {

        boolean _hasBeenSetup = false;

        MyActionTurnout(String systemName) {
            super(systemName, null);
        }

        /** {@inheritDoc} */
        @Override
        public void setup() {
            _hasBeenSetup = true;
        }
    }

}
