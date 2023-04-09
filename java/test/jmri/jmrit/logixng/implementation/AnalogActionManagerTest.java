package jmri.jmrit.logixng.implementation;

import java.util.Locale;
import java.util.Map;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.AnalogActionMemory;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test AnalogActionManager
 *
 * @author Daniel Bergqvist 2020
 */
public class AnalogActionManagerTest extends AbstractManagerTestBase {

    private AnalogActionManager _m;

    @Test
    public void testRegisterAction() {
        MyAction myAction = new MyAction(_m.getSystemNamePrefix()+"BadSystemName");

        boolean hasThrown = false;
        try {
            _m.registerAction(myAction);
        } catch (IllegalArgumentException e) {
            hasThrown = true;
            Assert.assertEquals("Error message is correct", "System name is invalid: IQBadSystemName", e.getMessage());
            JUnitAppender.assertWarnMessage("SystemName IQBadSystemName is not in the correct format");
        }
        Assert.assertTrue("Exception thrown", hasThrown);


        // We need a male socket to test with, so we register the action and then unregister the socket
        AnalogActionBean action = new AnalogActionMemory("IQAA321", null);
        MaleAnalogActionSocket maleSocket = _m.registerAction(action);
        _m.deregister(maleSocket);

        hasThrown = false;
        try {
            _m.registerAction(maleSocket);
        } catch (IllegalArgumentException e) {
            hasThrown = true;
            Assert.assertEquals("Error message is correct", "registerAction() cannot register a MaleAnalogActionSocket. Use the method register() instead.", e.getMessage());
        }
        Assert.assertTrue("Exception thrown", hasThrown);
    }

    @Test
    public void testGetBeanTypeHandled() {
        Assert.assertEquals("getBeanTypeHandled() returns correct value", "Analog action", _m.getBeanTypeHandled());
        Assert.assertEquals("getBeanTypeHandled() returns correct value", "Analog action", _m.getBeanTypeHandled(false));
        Assert.assertEquals("getBeanTypeHandled() returns correct value", "Analog actions", _m.getBeanTypeHandled(true));
    }

    @Test
    public void testInstance() {
        Assert.assertNotNull("instance() is not null", DefaultAnalogActionManager.instance());
        JUnitAppender.assertWarnMessage("instance() called on wrong thread");
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

        _m = InstanceManager.getDefault(AnalogActionManager.class);
        _manager = _m;
    }

    @After
    public void tearDown() {
        _m = null;
        _manager = null;
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }


    private static class MyAction extends AbstractBase implements AnalogActionBean {

        public MyAction(String sys) throws BadSystemNameException {
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
        public Category getCategory() {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public void setup() {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public void setValue(double value) throws JmriException {
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

}
