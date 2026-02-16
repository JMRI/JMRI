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

import jmri.jmrit.logixng.actions.AbstractDigitalBooleanAction;
import jmri.jmrit.logixng.actions.DigitalBooleanLogixAction;

import jmri.jmrit.logixng.implementation.DefaultMaleDigitalBooleanActionSocket.DigitalBooleanActionDebugConfig;

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
public class DefaultMaleDigitalBooleanActionSocketTest extends MaleSocketTestBase{

    @Override
    protected String getNewSystemName() {
        return InstanceManager.getDefault(DigitalBooleanActionManager.class)
                .getAutoSystemName();
    }

    @Test
    public void testCtor() {
        DigitalBooleanActionBean action = new DigitalBooleanLogixAction("IQDB321", null, DigitalBooleanLogixAction.When.Either);
        assertNotNull( new DefaultMaleDigitalBooleanActionSocket(manager, action), "exists");
    }

    @Test
    public void testEvaluate() throws JmriException {
        ConditionalNG conditionalNG = new DefaultConditionalNGScaffold("IQC1", "A conditionalNG");  // NOI18N;

        MyDigitalBooleanAction action = new MyDigitalBooleanAction("IQDB321");
        action.setParent(conditionalNG);

        DefaultMaleDigitalBooleanActionSocket socket = new DefaultMaleDigitalBooleanActionSocket(manager, action);
        assertNotNull( socket, "exists");

        socket.setParent(conditionalNG);
        socket.setEnabled(true);
        socket.setErrorHandlingType(MaleSocket.ErrorHandlingType.ThrowException);

        action.je = null;
        action.re = null;
        socket.execute(false);
        assertFalse(action._value);
        socket.execute(true);
        assertTrue(action._value);
        socket.execute(false);
        assertFalse(action._value);

        action.je = new JmriException("Test JmriException");
        action.re = null;
        Throwable thrown = catchThrowable( () -> socket.execute(false));
        assertThat(thrown)
                .withFailMessage("Evaluate throws an exception")
                .isNotNull()
                .isInstanceOf(JmriException.class)
                .hasMessage("Test JmriException");

        action.je = null;
        action.re = new RuntimeException("Test RuntimeException");
        thrown = catchThrowable( () -> socket.execute(false));
        assertThat(thrown)
                .withFailMessage("Evaluate throws an exception")
                .isNotNull()
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Test RuntimeException");

        // If the socket is not enabled, it shouldn't do anything
        socket.setEnabled(false);
        action.re = new RuntimeException("Test RuntimeException");
        thrown = catchThrowable( () -> socket.execute(false));
        assertThat(thrown)
                .withFailMessage("Evaluate does nothing")
                .isNull();

        // Test debug config
        socket.setEnabled(true);
        DigitalBooleanActionDebugConfig config = new DigitalBooleanActionDebugConfig();
        socket.setDebugConfig(config);
        action.je = null;
        action.re = null;
        config._dontExecute = true;
        action._value = false;
        socket.execute(true);
        assertFalse(action._value);
        action._value = false;
        socket.execute(false);
        assertFalse(action._value);
        config._dontExecute = false;
        action._value = false;
        socket.execute(true);
        assertTrue(action._value);
        action._value = false;
        socket.execute(false);
        assertFalse(action._value);
    }

    @Test
    public void testVetoableChange() {
        MyDigitalBooleanAction action = new MyDigitalBooleanAction("IQDB321");
        DefaultMaleDigitalBooleanActionSocket socket = new DefaultMaleDigitalBooleanActionSocket(manager, action);
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
        MyDigitalBooleanAction action1 = new MyDigitalBooleanAction("IQDB1");
        DefaultMaleDigitalBooleanActionSocket socket1 = new DefaultMaleDigitalBooleanActionSocket(manager, action1);

        MyDigitalBooleanAction action2 = new MyDigitalBooleanAction("IQDB01");
        DefaultMaleDigitalBooleanActionSocket socket2 = new DefaultMaleDigitalBooleanActionSocket(manager, action2);

        assertEquals( -1, socket1.compareSystemNameSuffix("01", "1", socket2),
            "compareSystemNameSuffix returns correct value");
        assertEquals( 0, socket1.compareSystemNameSuffix("1", "1", socket2),
            "compareSystemNameSuffix returns correct value");
        assertEquals( 0, socket1.compareSystemNameSuffix("01", "01", socket2),
            "compareSystemNameSuffix returns correct value");
        assertEquals( +1, socket1.compareSystemNameSuffix("1", "01", socket2),
            "compareSystemNameSuffix returns correct value");
    }

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

        DigitalBooleanActionBean actionA = new DigitalBooleanLogixAction("IQDB321", null, DigitalBooleanLogixAction.When.Either);
        assertNotNull( actionA, "exists");
        DigitalBooleanActionBean actionB = new MyDigitalBooleanAction("IQDB322");
        assertNotNull( actionB, "exists");

        manager = InstanceManager.getDefault(DigitalBooleanActionManager.class);

        maleSocketA =
                InstanceManager.getDefault(DigitalBooleanActionManager.class)
                        .registerAction(actionA);
        assertNotNull( maleSocketA, "exists");

        maleSocketB =
                InstanceManager.getDefault(DigitalBooleanActionManager.class)
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
    private static class MyDigitalBooleanAction extends AbstractDigitalBooleanAction {

        JmriException je = null;
        RuntimeException re = null;
        boolean _value = false;
        boolean _vetoChange = false;

        MyDigitalBooleanAction(String sysName) {
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
            return LogixNG_Category.OTHER;
        }

        @Override
        public void setup() {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        @SuppressFBWarnings( value = "THROWS_METHOD_THROWS_RUNTIMEEXCEPTION",
            justification="testing exception types")
        public void execute(boolean value) throws JmriException {
            if (je != null) {
                throw je;
            }
            if (re != null) {
                throw re;
            }
           _value = value;
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
