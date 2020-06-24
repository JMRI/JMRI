package jmri.jmrit.logixng.implementation;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.FemaleGenericExpressionSocket.SocketType;
import jmri.jmrit.logixng.analog.expressions.AnalogExpressionConstant;
import jmri.jmrit.logixng.digital.expressions.ExpressionMemory;
import jmri.jmrit.logixng.string.expressions.StringExpressionConstant;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test DefaultFemaleGenericExpressionSocket
 * 
 * @author Daniel Bergqvist 2018
 */
public class DefaultFemaleGenericExpressionSocket3_Test {

    @Test
    public void testEvaluateBoolean() throws JmriException {
        DefaultFemaleGenericExpressionSocket socket;
        
        MyAnalogExpression analogExpression = new MyAnalogExpression("IQAE351", null);
        MaleSocket analogMaleSocket =
                InstanceManager.getDefault(AnalogExpressionManager.class).registerExpression(analogExpression);
        
        MyDigitalExpression digitalExpression = new MyDigitalExpression("IQDE351", null);
        MaleSocket digitalMaleSocket =
                InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(digitalExpression);
        
        MyStringExpression stringExpression = new MyStringExpression("IQSE351", null);
        MaleSocket stringMaleSocket =
                InstanceManager.getDefault(StringExpressionManager.class).registerExpression(stringExpression);
        
        socket = new DefaultFemaleGenericExpressionSocket(SocketType.GENERIC, null, null, "E");
        Assert.assertFalse("evaluateBoolean() returns false", socket.evaluateBoolean());
        
        socket.connect(analogMaleSocket);
        analogExpression._value = 0.0;
        Assert.assertFalse("evaluateBoolean() returns false", socket.evaluateBoolean());
        analogExpression._value = 1.0;
        Assert.assertTrue("evaluateBoolean() returns true", socket.evaluateBoolean());
        analogExpression._value = -1.0;
        Assert.assertTrue("evaluateBoolean() returns true", socket.evaluateBoolean());
        socket.disconnect();
        
        
        socket = new DefaultFemaleGenericExpressionSocket(SocketType.GENERIC, null, null, "E");
        Assert.assertFalse("evaluateBoolean() returns false", socket.evaluateBoolean());
        
        socket.connect(digitalMaleSocket);
        digitalExpression._value = false;
        Assert.assertFalse("evaluateBoolean() returns false", socket.evaluateBoolean());
        digitalExpression._value = true;
        Assert.assertTrue("evaluateBoolean() returns true", socket.evaluateBoolean());
        socket.disconnect();
        
        
        socket = new DefaultFemaleGenericExpressionSocket(SocketType.GENERIC, null, null, "E");
        Assert.assertFalse("evaluateBoolean() returns false", socket.evaluateBoolean());
        
        socket.connect(stringMaleSocket);
        stringExpression._value = "";
        Assert.assertFalse("evaluateBoolean() returns false", socket.evaluateBoolean());
        stringExpression._value = "Hello";
        Assert.assertTrue("evaluateBoolean() returns true", socket.evaluateBoolean());
        stringExpression._value = "1.0";
        Assert.assertTrue("evaluateBoolean() returns true", socket.evaluateBoolean());
        socket.disconnect();
    }
    
    @Test
    public void testEvaluateDouble() throws JmriException {
        DefaultFemaleGenericExpressionSocket socket;
        
        MyAnalogExpression analogExpression = new MyAnalogExpression("IQAE351", null);
        MaleSocket analogMaleSocket =
                InstanceManager.getDefault(AnalogExpressionManager.class).registerExpression(analogExpression);
        
        MyDigitalExpression digitalExpression = new MyDigitalExpression("IQDE351", null);
        MaleSocket digitalMaleSocket =
                InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(digitalExpression);
        
        MyStringExpression stringExpression = new MyStringExpression("IQSE351", null);
        MaleSocket stringMaleSocket =
                InstanceManager.getDefault(StringExpressionManager.class).registerExpression(stringExpression);
        
        socket = new DefaultFemaleGenericExpressionSocket(SocketType.GENERIC, null, null, "E");
        Assert.assertTrue("evaluateBoolean() returns 0.0", 0.0 == socket.evaluateDouble());
        
        socket.connect(analogMaleSocket);
        analogExpression._value = 0.0;
        Assert.assertTrue("evaluateDouble() returns 0.0", 0.0 == socket.evaluateDouble());
        analogExpression._value = 1.0;
        Assert.assertTrue("evaluateDouble() returns 1.0", 1.0 == socket.evaluateDouble());
        analogExpression._value = -1.0;
        Assert.assertTrue("evaluateDouble() returns -1.0", -1.0 == socket.evaluateDouble());
        socket.disconnect();
        
        
        socket = new DefaultFemaleGenericExpressionSocket(SocketType.GENERIC, null, null, "E");
        Assert.assertTrue("evaluateDouble() returns 0.0", 0.0 == socket.evaluateDouble());
        
        socket.connect(digitalMaleSocket);
        digitalExpression._value = false;
        Assert.assertTrue("evaluateDouble() returns 0.0", 0.0 == socket.evaluateDouble());
        digitalExpression._value = true;
        Assert.assertTrue("evaluateDouble() returns 1.0", 1.0 == socket.evaluateDouble());
        socket.disconnect();
        
        
        socket = new DefaultFemaleGenericExpressionSocket(SocketType.GENERIC, null, null, "E");
        Assert.assertTrue("evaluateDouble() returns 0.0", 0.0 == socket.evaluateDouble());
        
        socket.connect(stringMaleSocket);
        stringExpression._value = "";
        Assert.assertTrue("evaluateDouble() returns 0.0", 0.0 == socket.evaluateDouble());
        stringExpression._value = "Hello";
        Assert.assertTrue("evaluateDouble() returns 0.0", 0.0 == socket.evaluateDouble());
        stringExpression._value = "1.0";
        Assert.assertTrue("evaluateDouble() returns 1.0", 1.0 == socket.evaluateDouble());
        socket.disconnect();
    }
    
    @Test
    public void testEvaluateString() throws JmriException {
        DefaultFemaleGenericExpressionSocket socket;
        
        MyAnalogExpression analogExpression = new MyAnalogExpression("IQAE351", null);
        MaleSocket analogMaleSocket =
                InstanceManager.getDefault(AnalogExpressionManager.class).registerExpression(analogExpression);
        
        MyDigitalExpression digitalExpression = new MyDigitalExpression("IQDE351", null);
        MaleSocket digitalMaleSocket =
                InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(digitalExpression);
        
        MyStringExpression stringExpression = new MyStringExpression("IQSE351", null);
        MaleSocket stringMaleSocket =
                InstanceManager.getDefault(StringExpressionManager.class).registerExpression(stringExpression);
        
        socket = new DefaultFemaleGenericExpressionSocket(SocketType.GENERIC, null, null, "E");
        Assert.assertEquals("evaluateString() returns empty string", "", socket.evaluateString());
        
        socket.connect(analogMaleSocket);
        analogExpression._value = 0.0;
        Assert.assertEquals("evaluateString() returns 0.0", "0.0", socket.evaluateString());
        analogExpression._value = 1.0;
        Assert.assertEquals("evaluateString() returns 1.0", "1.0", socket.evaluateString());
        analogExpression._value = -1.0;
        Assert.assertEquals("evaluateString() returns -1.0", "-1.0", socket.evaluateString());
        socket.disconnect();
        
        
        socket = new DefaultFemaleGenericExpressionSocket(SocketType.GENERIC, null, null, "E");
        Assert.assertEquals("evaluateString() returns empty string", "", socket.evaluateString());
        
        socket.connect(digitalMaleSocket);
        digitalExpression._value = false;
        Assert.assertEquals("evaluateString() returns false", "false", socket.evaluateString());
        digitalExpression._value = true;
        Assert.assertEquals("evaluateString() returns true", "true", socket.evaluateString());
        socket.disconnect();
        
        
        socket = new DefaultFemaleGenericExpressionSocket(SocketType.GENERIC, null, null, "E");
        Assert.assertEquals("evaluateString() returns empty string", "", socket.evaluateString());
        
        socket.connect(stringMaleSocket);
        stringExpression._value = "";
        Assert.assertEquals("evaluateString() returns empty string", "", socket.evaluateString());
        stringExpression._value = "Hello";
        Assert.assertEquals("evaluateString() returns Hello", "Hello", socket.evaluateString());
        stringExpression._value = "1.0";
        Assert.assertEquals("evaluateString() returns 1.0", "1.0", socket.evaluateString());
        socket.disconnect();
    }
    
    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
    
    
    private static class MyAnalogExpression extends AnalogExpressionConstant {
        
        private double _value;
        
        public MyAnalogExpression(String sys, String user) {
            super(sys, user);
        }
        
        /** {@inheritDoc} */
        @Override
        public double evaluate() {
            return _value;
        }
        
    }
    
    
    private static class MyDigitalExpression extends ExpressionMemory {
        
        private boolean _value;
        
        public MyDigitalExpression(String sys, String user) {
            super(sys, user);
        }
        
        /** {@inheritDoc} */
        @Override
        public boolean evaluate() {
            return _value;
        }
        
    }
    
    
    private static class MyStringExpression extends StringExpressionConstant {
        
        private String _value;
        
        public MyStringExpression(String sys, String user) {
            super(sys, user);
        }
        
        /** {@inheritDoc} */
        @Override
        public String evaluate() {
            return _value;
        }
        
    }
    
}
