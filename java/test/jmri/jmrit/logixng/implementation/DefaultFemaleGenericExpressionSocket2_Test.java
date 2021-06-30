package jmri.jmrit.logixng.implementation;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.FemaleGenericExpressionSocket.SocketType;
import jmri.jmrit.logixng.expressions.AnalogExpressionConstant;
import jmri.jmrit.logixng.expressions.ExpressionMemory;
import jmri.jmrit.logixng.expressions.StringExpressionConstant;
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
public class DefaultFemaleGenericExpressionSocket2_Test {

    private ConditionalNG _conditionalNG;
    private FemaleSocketListener _listener;
    
    @Test
    public void testEvaluateGeneric() throws JmriException {
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
        
        socket = new DefaultFemaleGenericExpressionSocket(SocketType.GENERIC, _conditionalNG, _listener, "E");
        
        Assert.assertEquals("evaluateGeneric() returns correct value", null, socket.evaluateGeneric());
        
        socket.connect(analogMaleSocket);
        analogExpression._value = 0.0;
        Assert.assertTrue("evaluateGeneric() returns correct value", 0.0 == (Double)socket.evaluateGeneric());
        analogExpression._value = 1.0;
        Assert.assertTrue("evaluateGeneric() returns correct value", 1.0 == (Double)socket.evaluateGeneric());
        analogExpression._value = -1.0;
        Assert.assertTrue("evaluateGeneric() returns correct value", -1.0 == (Double)socket.evaluateGeneric());
        socket.disconnect();
        
        
        socket = new DefaultFemaleGenericExpressionSocket(SocketType.GENERIC, _conditionalNG, _listener, "E");
        
        Assert.assertEquals("evaluateGeneric() returns correct value", null, socket.evaluateGeneric());
        
        socket.connect(digitalMaleSocket);
        digitalExpression._value = false;
        Assert.assertFalse("evaluateGeneric() returns correct value", (Boolean)socket.evaluateGeneric());
        digitalExpression._value = true;
        Assert.assertTrue("evaluateGeneric() returns correct value", (Boolean)socket.evaluateGeneric());
        socket.disconnect();
        
        
        socket = new DefaultFemaleGenericExpressionSocket(SocketType.GENERIC, _conditionalNG, _listener, "E");
        
        Assert.assertEquals("evaluateGeneric() returns correct value", null, socket.evaluateGeneric());
        
        socket.connect(stringMaleSocket);
        stringExpression._value = "";
        Assert.assertEquals("evaluateGeneric() returns correct value", "", socket.evaluateGeneric());
        stringExpression._value = "Hello";
        Assert.assertEquals("evaluateGeneric() returns correct value", "Hello", socket.evaluateGeneric());
        stringExpression._value = "1.0";
        Assert.assertEquals("evaluateGeneric() returns correct value", "1.0", socket.evaluateGeneric());
        socket.disconnect();
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
        
        _conditionalNG = new DefaultConditionalNGScaffold("IQC1", "A conditionalNG");  // NOI18N;
        
        _listener = new FemaleSocketListener(){
            @Override
            public void connected(FemaleSocket socket) {
                // Do nothing
            }

            @Override
            public void disconnected(FemaleSocket socket) {
                // Do nothing
            }
        };
    }

    @After
    public void tearDown() {
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
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
