package jmri.jmrit.logixng.implementation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Locale;
import java.util.Map;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.ActionMemory;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test DigitalActionManager
 *
 * @author Daniel Bergqvist 2020
 */
public class DigitalActionManagerTest extends AbstractManagerTestBase {

    private DigitalActionManager _m;

    @Test
    public void testRegisterAction() {
        MyAction myAction = new MyAction(_m.getSystemNamePrefix()+"BadSystemName");

        IllegalArgumentException e = assertThrows( IllegalArgumentException.class, () ->
            _m.registerAction(myAction), "Exception thrown");
        assertEquals( "System name is invalid: IQBadSystemName", e.getMessage(), "Error message is correct");
        JUnitAppender.assertWarnMessage("SystemName IQBadSystemName is not in the correct format");


        // We need a male socket to test with, so we register the action and then unregister the socket
        DigitalActionBean action = new ActionMemory("IQDA321", null);
        MaleDigitalActionSocket maleSocket = _m.registerAction(action);
        _m.deregister(maleSocket);

        e = assertThrows( IllegalArgumentException.class, () ->
            _m.registerAction(maleSocket), "Exception thrown");
        assertEquals( "registerAction() cannot register a MaleDigitalActionSocket. Use the method register() instead.", e.getMessage(), "Error message is correct");
    }

    @Test
    public void testGetBeanTypeHandled() {
        assertEquals( "Digital action", _m.getBeanTypeHandled(), "getBeanTypeHandled() returns correct value");
        assertEquals( "Digital action", _m.getBeanTypeHandled(false), "getBeanTypeHandled() returns correct value");
        assertEquals( "Digital actions", _m.getBeanTypeHandled(true), "getBeanTypeHandled() returns correct value");
    }

    @Test
    public void testInstance() {
        assertNotNull( DefaultDigitalActionManager.instance(), "instance() is not null");
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

        _m = InstanceManager.getDefault(DigitalActionManager.class);
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


    private static class MyAction extends AbstractBase implements DigitalActionBean {

        MyAction(String sys) throws BadSystemNameException {
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
        public void execute() throws JmriException {
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
