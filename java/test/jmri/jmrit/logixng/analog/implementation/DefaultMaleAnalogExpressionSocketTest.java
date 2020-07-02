package jmri.jmrit.logixng.analog.implementation;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.util.Locale;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.jmrit.logixng.*;
import jmri.util.JUnitUtil;

import jmri.jmrit.logixng.analog.expressions.AbstractAnalogExpression;
import jmri.jmrit.logixng.analog.expressions.AnalogExpressionMemory;
import jmri.jmrit.logixng.analog.implementation.DefaultMaleAnalogExpressionSocket.AnalogExpressionDebugConfig;
import jmri.jmrit.logixng.digital.implementation.DefaultMaleDigitalBooleanActionSocket;
import jmri.jmrit.logixng.digital.implementation.DefaultMaleDigitalBooleanActionSocketTest;
import jmri.util.JUnitAppender;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.catchThrowable;

/**
 * Test ExpressionTimer
 * 
 * @author Daniel Bergqvist 2018
 */
public class DefaultMaleAnalogExpressionSocketTest extends MaleSocketTestBase {

    @Override
    protected String getNewSystemName() {
        return InstanceManager.getDefault(AnalogExpressionManager.class)
                .getAutoSystemName();
    }
    
    @Test
    public void testCtor() {
        AnalogExpressionBean expression = new AnalogExpressionMemory("IQAE321", null);
        Assert.assertNotNull("object exists", new DefaultMaleAnalogExpressionSocket(expression));
    }
    
    @Test
    public void testEvaluate() throws JmriException {
        MyAnalogExpression expression = new MyAnalogExpression("IQAE321");
        DefaultMaleAnalogExpressionSocket socket = new DefaultMaleAnalogExpressionSocket(expression);
        Assert.assertNotNull("exists", socket);
        
        socket.setEnabled(true);
        socket.setErrorHandlingType(MaleSocket.ErrorHandlingType.THROW);
        
        expression.je = null;
        expression.re = null;
        expression.result = 94.27;
        Assert.assertTrue(94.27 == socket.evaluate());
        expression.result = 12.92;
        Assert.assertTrue(12.92 == socket.evaluate());
        expression.result = 0.0;
        Assert.assertTrue(0.0 == socket.evaluate());
        
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
        AnalogExpressionDebugConfig config = new AnalogExpressionDebugConfig();
        socket.setDebugConfig(config);
        expression.je = null;
        expression.re = null;
        config._forceResult = true;
        config._result = 12.34;
        expression.result = 93.23;
        Assert.assertTrue(12.34 == socket.evaluate());
        config._forceResult = false;
        Assert.assertTrue(93.23 == socket.evaluate());
    }
    
    @Test
    public void testEvaluateErrors() {
        MyAnalogExpression expression = new MyAnalogExpression("IQAE321");
        DefaultMaleAnalogExpressionSocket socket = new DefaultMaleAnalogExpressionSocket(expression);
        Assert.assertNotNull("exists", socket);
        
        socket.setEnabled(true);
        socket.setErrorHandlingType(MaleSocket.ErrorHandlingType.THROW);
        
        expression.result = Double.NaN;
        Throwable thrown = catchThrowable( () -> socket.evaluate());
        assertThat(thrown)
                .withFailMessage("Evaluate throws an exception")
                .isNotNull()
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("The result is NaN");
        
        expression.result = Double.NEGATIVE_INFINITY;
        thrown = catchThrowable( () -> socket.evaluate());
        assertThat(thrown)
                .withFailMessage("Evaluate throws an exception")
                .isNotNull()
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("The result is negative infinity");
        
        expression.result = Double.POSITIVE_INFINITY;
        thrown = catchThrowable( () -> socket.evaluate());
        assertThat(thrown)
                .withFailMessage("Evaluate throws an exception")
                .isNotNull()
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("The result is positive infinity");
    }
    
    @Test
    public void testVetoableChange() {
        MyAnalogExpression action = new MyAnalogExpression("IQAE321");
        DefaultMaleAnalogExpressionSocket socket = new DefaultMaleAnalogExpressionSocket(action);
        Assert.assertNotNull("exists", socket);
        
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
        MyAnalogExpression expression1 = new MyAnalogExpression("IQAE1");
        DefaultMaleAnalogExpressionSocket socket1 = new DefaultMaleAnalogExpressionSocket(expression1);
        
        MyAnalogExpression expression2 = new MyAnalogExpression("IQAE01");
        DefaultMaleAnalogExpressionSocket socket2 = new DefaultMaleAnalogExpressionSocket(expression2);
        
        Assert.assertEquals("compareSystemNameSuffix returns correct value",
                -1, socket1.compareSystemNameSuffix("01", "1", socket2));
        Assert.assertEquals("compareSystemNameSuffix returns correct value",
                0, socket1.compareSystemNameSuffix("1", "1", socket2));
        Assert.assertEquals("compareSystemNameSuffix returns correct value",
                0, socket1.compareSystemNameSuffix("01", "01", socket2));
        Assert.assertEquals("compareSystemNameSuffix returns correct value",
                +1, socket1.compareSystemNameSuffix("1", "01", socket2));
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
        
        AnalogExpressionBean expressionA = new AnalogExpressionMemory("IQAE321", null);
        Assert.assertNotNull("exists", expressionA);
        AnalogExpressionBean expressionB = new MyAnalogExpression("IQAE322");
        Assert.assertNotNull("exists", expressionA);
        
        maleSocketA =
                InstanceManager.getDefault(AnalogExpressionManager.class)
                        .registerExpression(expressionA);
        Assert.assertNotNull("exists", maleSocketA);
        
        maleSocketB =
                InstanceManager.getDefault(AnalogExpressionManager.class)
                        .registerExpression(expressionB);
        Assert.assertNotNull("exists", maleSocketB);
    }

    @AfterEach
    @Override
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
    
    /**
     * This action is different from AnalogExpressionMemory and is used to test the
     * male socket.
     */
    private class MyAnalogExpression extends AbstractAnalogExpression {
        
        JmriException je = null;
        RuntimeException re = null;
        double result = 0.0;
        boolean _vetoChange = false;
        
        MyAnalogExpression(String sysName) {
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
        public Category getCategory() {
            return Category.COMMON;
        }

        @Override
        public boolean isExternal() {
            return false;
        }

        @Override
        public void setup() {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public double evaluate() throws JmriException {
            if (je != null) throw je;
            if (re != null) throw re;
            return result;
        }
        
        @Override
        public void reset() {
            throw new UnsupportedOperationException("Not supported.");
        }
        
        @Override
        public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
            if (_vetoChange) throw new java.beans.PropertyVetoException("Veto change", evt);
        }
        
    }
    
}
