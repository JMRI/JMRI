package jmri.jmrit.logixng.implementation;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.util.Locale;
import java.util.Map;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.jmrit.logixng.*;
import jmri.util.JUnitUtil;

import jmri.jmrit.logixng.actions.AbstractDigitalAction;
import jmri.jmrit.logixng.actions.ActionTurnout;
import jmri.jmrit.logixng.actions.DigitalMany;
import jmri.jmrit.logixng.implementation.DefaultMaleDigitalActionSocket.DigitalActionDebugConfig;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test ExpressionTimer
 *
 * @author Daniel Bergqvist 2018
 */
public class DefaultMaleDigitalActionSocketTest extends MaleSocketTestBase{

    @Override
    protected String getNewSystemName() {
        return InstanceManager.getDefault(DigitalActionManager.class)
                .getAutoSystemName();
    }

    @Test
    public void testCtor() {
        DigitalActionBean action = new DigitalMany("IQDA321", null);
        assertNotNull( new DefaultMaleDigitalActionSocket(manager, action), "exists");
    }

    @Test
    public void testExecute() throws JmriException {
        ConditionalNG conditionalNG = new DefaultConditionalNGScaffold("IQC1", "A conditionalNG");  // NOI18N;

        MyDigitalAction action = new MyDigitalAction("IQDA321");
        action.setParent(conditionalNG);

        DefaultMaleDigitalActionSocket socket = new DefaultMaleDigitalActionSocket(manager, action);
        assertNotNull( socket, "exists");

        socket.setParent(conditionalNG);
        socket.setEnabled(true);
        socket.setErrorHandlingType(MaleSocket.ErrorHandlingType.ThrowException);

        action.je = null;
        action.re = null;
        action._hasExecuted = false;
        socket.execute();
        assertTrue(action._hasExecuted);

        action.je = new JmriException("Test JmriException");
        action.re = null;
        Throwable thrown = catchThrowable( () -> socket.execute());
        assertThat(thrown)
                .withFailMessage("Evaluate throws an exception")
                .isNotNull()
                .isInstanceOf(JmriException.class)
                .hasMessage("Test JmriException");

        action.je = null;
        action.re = new RuntimeException("Test RuntimeException");
        thrown = catchThrowable( () -> socket.execute());
        assertThat(thrown)
                .withFailMessage("Evaluate throws an exception")
                .isNotNull()
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Test RuntimeException");

        // If the socket is not enabled, it shouldn't do anything
        socket.setEnabled(false);
        action.re = new RuntimeException("Test RuntimeException");
        thrown = catchThrowable( () -> socket.execute());
        assertThat(thrown)
                .withFailMessage("Evaluate does nothing")
                .isNull();

        // Test debug config
        socket.setEnabled(true);
        DigitalActionDebugConfig config = new DigitalActionDebugConfig();
        socket.setDebugConfig(config);
        action.je = null;
        action.re = null;
        config._dontExecute = true;
        action._hasExecuted = false;
        socket.execute();
        assertFalse(action._hasExecuted);
        config._dontExecute = false;
        action._hasExecuted = false;
        socket.execute();
        assertTrue(action._hasExecuted);
    }

    @Test
    public void testVetoableChange() {
        MyDigitalAction action = new MyDigitalAction("IQDA321");
        DefaultMaleDigitalActionSocket socket = new DefaultMaleDigitalActionSocket(manager, action);
        assertNotNull( socket, "exists");

        PropertyChangeEvent evt = new PropertyChangeEvent("Source", "Prop", null, null);

        action._vetoChange = true;
        Throwable thrown = catchThrowable( () -> socket.vetoableChange(evt));
        assertThat(thrown)
                .withFailMessage("vetoableChange() does throw")
                .isNotNull()
                .isInstanceOf(PropertyVetoException.class)
                .hasMessage("Veto change");

        action._vetoChange = false;
        thrown = catchThrowable( () -> socket.vetoableChange(evt));
        assertThat(thrown)
                .withFailMessage("vetoableChange() does not throw")
                .isNull();
    }

    @Test
    public void testCompareSystemNameSuffix() {
        MyDigitalAction action1 = new MyDigitalAction("IQDA1");
        DefaultMaleDigitalActionSocket socket1 = new DefaultMaleDigitalActionSocket(manager, action1);

        MyDigitalAction action2 = new MyDigitalAction("IQDA01");
        DefaultMaleDigitalActionSocket socket2 = new DefaultMaleDigitalActionSocket(manager, action2);

        assertEquals( -1, socket1.compareSystemNameSuffix("01", "1", socket2),
            "compareSystemNameSuffix returns correct value");
        assertEquals( 0, socket1.compareSystemNameSuffix("1", "1", socket2),
            "compareSystemNameSuffix returns correct value");
        assertEquals( 0, socket1.compareSystemNameSuffix("01", "01", socket2),
            "compareSystemNameSuffix returns correct value");
        assertEquals( +1, socket1.compareSystemNameSuffix("1", "01", socket2),
            "compareSystemNameSuffix returns correct value");
    }

    // The minimal setup for log4J
    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initLogixNGManager();

        DigitalActionBean actionA = new ActionTurnout("IQDA321", null);
        assertNotNull( actionA, "exists");
        DigitalActionBean actionB = new MyDigitalAction("IQDA322");
        assertNotNull( actionB, "exists");

        manager = InstanceManager.getDefault(DigitalActionManager.class);

        maleSocketA =
                InstanceManager.getDefault(DigitalActionManager.class)
                        .registerAction(actionA);
        assertNotNull( maleSocketA, "exists");

        maleSocketB =
                InstanceManager.getDefault(DigitalActionManager.class)
                        .registerAction(actionB);
        assertNotNull( maleSocketB, "exists");
    }

    @AfterEach
    @Override
    public void tearDown() {
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }


    /**
     * This action is different from MyStringAction and is used to test the
     * male socket.
     */
    private static class MyDigitalAction extends AbstractDigitalAction {

        JmriException je = null;
        RuntimeException re = null;
        boolean _hasExecuted = false;
        boolean _vetoChange = false;

        MyDigitalAction(String sysName) {
            super(sysName, null);
        }

        @Override
        protected void registerListenersForThisClass() {
            // Do nothing
        }

        @Override
        protected void unregisterListenersForThisClass() {
            // Do nothing
        }

        @Override
        protected void disposeMe() {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public String getShortDescription(Locale locale) {
            return "My short description";
        }

        @Override
        public String getLongDescription(Locale locale) {
            return "My long description";
        }

        @Override
        public FemaleSocket getChild(int index) throws IllegalArgumentException, UnsupportedOperationException {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public int getChildCount() {
            return 0;
        }

        @Override
        public LogixNG_Category getCategory() {
            return LogixNG_Category.COMMON;
        }

        @Override
        public void setup() {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        @SuppressFBWarnings( value = "THROWS_METHOD_THROWS_RUNTIMEEXCEPTION",
            justification="testing exception types")
        public void execute() throws JmriException {
            if (je != null) {
                throw je;
            }
            if (re != null) {
                throw re;
            }
            _hasExecuted = true;
        }

        @Override
        public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
            if (_vetoChange) {
                throw new java.beans.PropertyVetoException("Veto change", evt);
            }
        }

        @Override
        public Base getDeepCopy(Map<String, String> map, Map<String, String> map1) throws JmriException {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public Base deepCopyChildren(Base base, Map<String, String> map, Map<String, String> map1) throws JmriException {
            throw new UnsupportedOperationException("Not supported");
        }

    }

}
