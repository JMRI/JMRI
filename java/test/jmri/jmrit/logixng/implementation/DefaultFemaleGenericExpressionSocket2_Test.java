package jmri.jmrit.logixng.implementation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.FemaleGenericExpressionSocket.SocketType;
import jmri.jmrit.logixng.expressions.AnalogExpressionConstant;
import jmri.jmrit.logixng.expressions.ExpressionMemory;
import jmri.jmrit.logixng.expressions.StringExpressionConstant;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

        assertNull( socket.evaluateGeneric(), "evaluateGeneric() returns correct value");

        socket.connect(analogMaleSocket);
        analogExpression._value = 0.0;
        assertEquals( 0.0, (Double)socket.evaluateGeneric(), "evaluateGeneric() returns correct value");
        analogExpression._value = 1.0;
        assertEquals( 1.0, (Double)socket.evaluateGeneric(), "evaluateGeneric() returns correct value");
        analogExpression._value = -1.0;
        assertEquals( -1.0, (Double)socket.evaluateGeneric(), "evaluateGeneric() returns correct value");
        socket.disconnect();


        socket = new DefaultFemaleGenericExpressionSocket(SocketType.GENERIC, _conditionalNG, _listener, "E");

        assertNull( socket.evaluateGeneric(), "evaluateGeneric() returns correct value");

        socket.connect(digitalMaleSocket);
        digitalExpression._value = false;
        assertFalse( (Boolean)socket.evaluateGeneric(), "evaluateGeneric() returns correct value");
        digitalExpression._value = true;
        assertTrue( (Boolean)socket.evaluateGeneric(), "evaluateGeneric() returns correct value");
        socket.disconnect();


        socket = new DefaultFemaleGenericExpressionSocket(SocketType.GENERIC, _conditionalNG, _listener, "E");

        assertNull( socket.evaluateGeneric(), "evaluateGeneric() returns correct value");

        socket.connect(stringMaleSocket);
        stringExpression._value = "";
        assertEquals( "", socket.evaluateGeneric(), "evaluateGeneric() returns correct value");
        stringExpression._value = "Hello";
        assertEquals( "Hello", socket.evaluateGeneric(), "evaluateGeneric() returns correct value");
        stringExpression._value = "1.0";
        assertEquals( "1.0", socket.evaluateGeneric(), "evaluateGeneric() returns correct value");
        socket.disconnect();
    }

    @BeforeEach
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

    @AfterEach
    public void tearDown() {
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }



    private static class MyAnalogExpression extends AnalogExpressionConstant {

        double _value;

        MyAnalogExpression(String sys, String user) {
            super(sys, user);
        }

        /** {@inheritDoc} */
        @Override
        public double evaluate() {
            return _value;
        }

    }


    private static class MyDigitalExpression extends ExpressionMemory {

        boolean _value;

        MyDigitalExpression(String sys, String user) {
            super(sys, user);
        }

        /** {@inheritDoc} */
        @Override
        public boolean evaluate() {
            return _value;
        }

    }


    private static class MyStringExpression extends StringExpressionConstant {

        String _value;

        MyStringExpression(String sys, String user) {
            super(sys, user);
        }

        /** {@inheritDoc} */
        @Override
        public String evaluate() {
            return _value;
        }

    }

}
