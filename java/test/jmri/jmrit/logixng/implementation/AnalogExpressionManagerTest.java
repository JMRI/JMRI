package jmri.jmrit.logixng.implementation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Locale;
import java.util.Map;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.expressions.AnalogExpressionMemory;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test AnalogExpressionManager
 *
 * @author Daniel Bergqvist 2020
 */
public class AnalogExpressionManagerTest extends AbstractManagerTestBase {

    private AnalogExpressionManager _m;

    @Test
    public void testRegisterExpression() {
        MyExpression myExpression = new MyExpression(_m.getSystemNamePrefix()+"BadSystemName");

        IllegalArgumentException e = assertThrows( IllegalArgumentException.class, () ->
            _m.registerExpression(myExpression), "Exception thrown");
        assertEquals( "System name is invalid: IQBadSystemName", e.getMessage(),
                "Error message is correct");
        JUnitAppender.assertWarnMessage("SystemName IQBadSystemName is not in the correct format");


        // We need a male socket to test with, so we register the action and then unregister the socket
        AnalogExpressionBean action = new AnalogExpressionMemory("IQAE321", null);
        MaleAnalogExpressionSocket maleSocket = _m.registerExpression(action);
        _m.deregister(maleSocket);

        e = assertThrows( IllegalArgumentException.class, () ->
            _m.registerExpression(maleSocket), "Exception thrown");
        assertEquals( "registerExpression() cannot register a MaleAnalogExpressionSocket. Use the method register() instead.",
                e.getMessage(), "Error message is correct");
    }

    @Test
    public void testCreateFemaleSocket() {
        FemaleSocket socket;
        AnalogExpressionManagerTest.MyExpression myExpression = new AnalogExpressionManagerTest.MyExpression("IQSA1");
        FemaleSocketListener listener = new AnalogExpressionManagerTest.MyFemaleSocketListener();

        socket = _m.createFemaleSocket(myExpression, listener, "E1");
        assertEquals( "jmri.jmrit.logixng.implementation.DefaultFemaleAnalogExpressionSocket",
                socket.getClass().getName(), "Class is correct");
    }

    @Test
    public void testGetBeanTypeHandled() {
        assertEquals( "Analog expression", _m.getBeanTypeHandled(), "getBeanTypeHandled() returns correct value");
        assertEquals( "Analog expression", _m.getBeanTypeHandled(false), "getBeanTypeHandled() returns correct value");
        assertEquals( "Analog expressions", _m.getBeanTypeHandled(true), "getBeanTypeHandled() returns correct value");
    }

    @Test
    public void testInstance() {
        assertNotNull( DefaultAnalogExpressionManager.instance(), "instance() is not null");
        JUnitAppender.assertWarnMessage("instance() called on wrong thread");
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

        _m = InstanceManager.getDefault(AnalogExpressionManager.class);
        _manager = _m;
    }

    @AfterEach
    public void tearDown() {
        _m = null;
        _manager = null;
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }


    private static class MyExpression extends AbstractBase implements AnalogExpressionBean {

        MyExpression(String sys) throws BadSystemNameException {
            super(sys);
        }

        @Override
        protected void registerListenersForThisClass() {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        protected void unregisterListenersForThisClass() {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        protected void disposeMe() {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public void setState(int s) throws JmriException {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public int getState() {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public String getBeanType() {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public String getShortDescription(Locale locale) {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public String getLongDescription(Locale locale) {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public Base getParent() {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public void setParent(Base parent) {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public FemaleSocket getChild(int index) throws IllegalArgumentException, UnsupportedOperationException {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public int getChildCount() {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public LogixNG_Category getCategory() {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public void setup() {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public double evaluate() throws JmriException {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public void setTriggerOnChange(boolean triggerOnChange) {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public boolean getTriggerOnChange() {
            throw new UnsupportedOperationException("Not supported");
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


    private static class MyFemaleSocketListener implements FemaleSocketListener {
        @Override
        public void connected(FemaleSocket socket) {
            // Do nothing
        }

        @Override
        public void disconnected(FemaleSocket socket) {
            // Do nothing
        }
    }

}
