package jmri.jmrit.logixng.implementation;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.util.Locale;
import java.util.Map;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.expressions.AbstractStringExpression;
import jmri.jmrit.logixng.expressions.StringExpressionMemory;
import jmri.jmrit.logixng.implementation.DefaultMaleStringExpressionSocket.StringExpressionDebugConfig;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Test ExpressionTimer
 *
 * @author Daniel Bergqvist 2018
 */
public class DefaultMaleStringExpressionSocketTest extends MaleSocketTestBase {

    @Override
    protected String getNewSystemName() {
        return InstanceManager.getDefault(StringExpressionManager.class)
                .getAutoSystemName();
    }

    @Test
    public void testCtor() {
        StringExpressionBean expression = new StringExpressionMemory("IQSE321", null);
        assertNotNull( new DefaultMaleStringExpressionSocket(manager, expression), "exists");
    }

    @Test
    public void testEvaluate() throws JmriException {
        ConditionalNG conditionalNG = new DefaultConditionalNGScaffold("IQC1", "A conditionalNG");  // NOI18N;

        MyStringExpression expression = new MyStringExpression("IQSE321");
        expression.setParent(conditionalNG);

        DefaultMaleStringExpressionSocket socket = new DefaultMaleStringExpressionSocket(manager, expression);
        assertNotNull( socket, "exists");

        socket.setParent(conditionalNG);
        socket.setEnabled(true);
        socket.setErrorHandlingType(MaleSocket.ErrorHandlingType.ThrowException);

        expression.je = null;
        expression.re = null;
        expression.result = "Something";
        assertEquals("Something", socket.evaluate());
        expression.result = "Something else";
        assertEquals("Something else", socket.evaluate());
        expression.result = "";
        assertEquals("", socket.evaluate());
        expression.result = null;
        assertNull(socket.evaluate());

        expression.je = new JmriException("Test JmriException");
        expression.re = null;
        Throwable thrown = catchThrowable( () -> socket.evaluate());
        assertThat(thrown)
                .withFailMessage("Evaluate throws an exception")
                .isNotNull()
                .isInstanceOf(JmriException.class)
                .hasMessage("Test JmriException");

        expression.je = null;
        expression.re = new RuntimeException("Test RuntimeException");
        thrown = catchThrowable( () -> socket.evaluate());
        assertThat(thrown)
                .withFailMessage("Evaluate throws an exception")
                .isNotNull()
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Test RuntimeException");

        // If the socket is not enabled, it shouldn't do anything
        socket.setEnabled(false);
        expression.re = new RuntimeException("Test RuntimeException");
        thrown = catchThrowable( () -> socket.evaluate());
        assertThat(thrown)
                .withFailMessage("Evaluate does nothing")
                .isNull();

        // Test debug config
        socket.setEnabled(true);
        StringExpressionDebugConfig config = new StringExpressionDebugConfig();
        socket.setDebugConfig(config);
        expression.je = null;
        expression.re = null;
        config._forceResult = true;
        config._result = "Hello";
        expression.result = "Something";
        assertEquals("Hello", socket.evaluate());
        config._forceResult = false;
        assertEquals("Something", socket.evaluate());
    }

    @Test
    public void testVetoableChange() {
        MyStringExpression action = new MyStringExpression("IQSE321");
        DefaultMaleStringExpressionSocket socket = new DefaultMaleStringExpressionSocket(manager, action);
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
        MyStringExpression expression1 = new MyStringExpression("IQSE1");
        DefaultMaleStringExpressionSocket socket1 = new DefaultMaleStringExpressionSocket(manager, expression1);

        MyStringExpression expression2 = new MyStringExpression("IQSE01");
        DefaultMaleStringExpressionSocket socket2 = new DefaultMaleStringExpressionSocket(manager, expression2);

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

        StringExpressionBean actionA = new StringExpressionMemory("IQSE321", null);
        assertNotNull( actionA, "exists");
        StringExpressionBean actionB = new MyStringExpression("IQSE322");
        assertNotNull( actionB, "exists");

        manager = InstanceManager.getDefault(StringExpressionManager.class);

        maleSocketA =
                InstanceManager.getDefault(StringExpressionManager.class)
                        .registerExpression(actionA);
        assertNotNull( maleSocketA, "exists");

        maleSocketB =
                InstanceManager.getDefault(StringExpressionManager.class)
                        .registerExpression(actionB);
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
     * This expression is different from StringExpressionMemory and is used to test the
     * male socket.
     */
    private static class MyStringExpression extends AbstractStringExpression {

        JmriException je = null;
        RuntimeException re = null;
        String result = "";
        boolean _vetoChange = false;

        MyStringExpression(String sysName) {
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
        public String evaluate() throws JmriException {
            if (je != null) {
                throw je;
            }
            if (re != null) {
                throw re;
            }
            return result;
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
