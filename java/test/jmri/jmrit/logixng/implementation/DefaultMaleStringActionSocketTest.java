package jmri.jmrit.logixng.implementation;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.util.Locale;
import java.util.Map;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.AbstractStringAction;
import jmri.jmrit.logixng.actions.StringActionMemory;
import jmri.util.JUnitUtil;

import jmri.jmrit.logixng.implementation.DefaultMaleStringActionSocket.StringActionDebugConfig;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

/**
 * Test ExpressionTimer
 *
 * @author Daniel Bergqvist 2018
 */
public class DefaultMaleStringActionSocketTest extends MaleSocketTestBase {

    @Override
    protected String getNewSystemName() {
        return InstanceManager.getDefault(StringActionManager.class)
                .getAutoSystemName();
    }

    @Test
    public void testCtor() {
        StringActionBean action = new StringActionMemory("IQSA321", null);
        assertNotNull( new DefaultMaleStringActionSocket(manager, action), "exists");
    }

    @Test
    @SuppressFBWarnings( value = "NP_NONNULL_PARAM_VIOLATION",
        justification="testing setting socket to null")
    public void testEvaluate() throws JmriException {
        ConditionalNG conditionalNG = new DefaultConditionalNGScaffold("IQC1", "A conditionalNG");  // NOI18N;

        MyStringAction action = new MyStringAction("IQSA321");
        action.setParent(conditionalNG);

        DefaultMaleStringActionSocket socket = new DefaultMaleStringActionSocket(manager, action);
        assertNotNull( socket, "exists");

        socket.setParent(conditionalNG);
        socket.setEnabled(true);
        socket.setErrorHandlingType(MaleSocket.ErrorHandlingType.ThrowException);

        action.je = null;
        action.re = null;
        socket.setValue("Something");
        assertEquals("Something", action._value);
        socket.setValue("Something else");
        assertEquals("Something else", action._value);
        socket.setValue("");
        assertEquals("", action._value);
        socket.setValue(null);
        assertNull(action._value);

        action.je = new JmriException("Test JmriException");
        action.re = null;
        Throwable thrown = catchThrowable( () -> socket.setValue("Something"));
        assertThat(thrown)
                .withFailMessage("Evaluate throws an exception")
                .isNotNull()
                .isInstanceOf(JmriException.class)
                .hasMessage("Test JmriException");

        action.je = null;
        action.re = new RuntimeException("Test RuntimeException");
        thrown = catchThrowable( () -> socket.setValue("Something"));
        assertThat(thrown)
                .withFailMessage("Evaluate throws an exception")
                .isNotNull()
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Test RuntimeException");

        // If the socket is not enabled, it shouldn't do anything
        socket.setEnabled(false);
        action.re = new RuntimeException("Test RuntimeException");
        thrown = catchThrowable( () -> socket.setValue("Something"));
        assertThat(thrown)
                .withFailMessage("Evaluate does nothing")
                .isNull();

        // Test debug config
        socket.setEnabled(true);
        StringActionDebugConfig config = new StringActionDebugConfig();
        socket.setDebugConfig(config);
        action.je = null;
        action.re = null;
        config._dontExecute = true;
        action._value = "Hello";
        socket.setValue("Something");
        assertEquals("Hello", action._value);
        config._dontExecute = false;
        action._value = "Hello";
        socket.setValue("Something else");
        assertEquals("Something else", action._value);
    }

    @Test
    public void testVetoableChange() {
        MyStringAction action = new MyStringAction("IQSA321");
        DefaultMaleStringActionSocket socket = new DefaultMaleStringActionSocket(manager, action);
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
        MyStringAction action1 = new MyStringAction("IQSA1");
        DefaultMaleStringActionSocket socket1 = new DefaultMaleStringActionSocket(manager, action1);

        MyStringAction action2 = new MyStringAction("IQSA01");
        DefaultMaleStringActionSocket socket2 = new DefaultMaleStringActionSocket(manager, action2);

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

        StringActionBean actionA = new StringActionMemory("IQSA321", null);
        assertNotNull( actionA, "exists");
        StringActionBean actionB = new MyStringAction("IQSA322");
        assertNotNull( actionB, "exists");

        manager = InstanceManager.getDefault(StringActionManager.class);

        maleSocketA =
                InstanceManager.getDefault(StringActionManager.class)
                        .registerAction(actionA);
        assertNotNull( maleSocketA, "exists");

        maleSocketB =
                InstanceManager.getDefault(StringActionManager.class)
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
     * This action is different from MyAnalogAction and is used to test the male socket.
     */
    private static class MyStringAction extends AbstractStringAction {

        JmriException je = null;
        RuntimeException re = null;
        String _value = "";
        boolean _vetoChange = false;

        MyStringAction(String sysName) {
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
        public void setValue(String value) throws JmriException {
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
