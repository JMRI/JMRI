package jmri.jmrit.logixng.string.implementation;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.util.Locale;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.analog.implementation.DefaultMaleAnalogActionSocketTest;
import jmri.jmrit.logixng.digital.implementation.DefaultMaleDigitalBooleanActionSocket;
import jmri.jmrit.logixng.digital.implementation.DefaultMaleDigitalBooleanActionSocketTest;
import jmri.jmrit.logixng.string.actions.AbstractStringAction;
import jmri.jmrit.logixng.string.actions.StringActionMemory;
import jmri.util.JUnitUtil;

import jmri.jmrit.logixng.string.implementation.DefaultMaleStringActionSocket.StringActionDebugConfig;

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
public class DefaultMaleStringActionSocketTest extends MaleSocketTestBase {

    @Override
    protected String getNewSystemName() {
        return InstanceManager.getDefault(StringActionManager.class)
                .getAutoSystemName();
    }
    
    @Test
    public void testCtor() {
        StringActionBean action = new StringActionMemory("IQSA321", null);
        Assert.assertNotNull("exists", new DefaultMaleStringActionSocket(action));
    }
    
    @Test
    public void testEvaluate() throws JmriException {
        MyStringAction expression = new MyStringAction("IQSA321");
        DefaultMaleStringActionSocket socket = new DefaultMaleStringActionSocket(expression);
        Assert.assertNotNull("exists", socket);
        
        socket.setEnabled(true);
        socket.setErrorHandlingType(MaleSocket.ErrorHandlingType.THROW);
        
        expression.je = null;
        expression.re = null;
        socket.setValue("Something");
        Assert.assertEquals("Something", expression._value);
        socket.setValue("Something else");
        Assert.assertEquals("Something else", expression._value);
        socket.setValue("");
        Assert.assertEquals("", expression._value);
        socket.setValue(null);
        Assert.assertNull(expression._value);
        
        expression.je = new JmriException("Test JmriException");
        expression.re = null;
        Throwable thrown = catchThrowable( () -> socket.setValue("Something"));
        assertThat(thrown)
                .withFailMessage("Evaluate throws an exception")
                .isNotNull()
                .isInstanceOf(JmriException.class)
                .hasMessage("Test JmriException");
        
        expression.je = null;
        expression.re = new RuntimeException("Test RuntimeException");
        thrown = catchThrowable( () -> socket.setValue("Something"));
        assertThat(thrown)
                .withFailMessage("Evaluate throws an exception")
                .isNotNull()
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Test RuntimeException");
        
        // If the socket is not enabled, it shouldn't do anything
        socket.setEnabled(false);
        expression.re = new RuntimeException("Test RuntimeException");
        thrown = catchThrowable( () -> socket.setValue("Something"));
        assertThat(thrown)
                .withFailMessage("Evaluate does nothing")
                .isNull();
        
        // Test debug config
        socket.setEnabled(true);
        StringActionDebugConfig config = new StringActionDebugConfig();
        socket.setDebugConfig(config);
        expression.je = null;
        expression.re = null;
        config._dontExecute = true;
        expression._value = "Hello";
        socket.setValue("Something");
        Assert.assertEquals("Hello", expression._value);
        config._dontExecute = false;
        expression._value = "Hello";
        socket.setValue("Something else");
        Assert.assertEquals("Something else", expression._value);
    }
    
    @Test
    public void testVetoableChange() {
        MyStringAction action = new MyStringAction("IQSA321");
        DefaultMaleStringActionSocket socket = new DefaultMaleStringActionSocket(action);
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
        MyStringAction action1 = new MyStringAction("IQSA1");
        DefaultMaleStringActionSocket socket1 = new DefaultMaleStringActionSocket(action1);
        
        MyStringAction action2 = new MyStringAction("IQSA01");
        DefaultMaleStringActionSocket socket2 = new DefaultMaleStringActionSocket(action2);
        
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
        
        StringActionBean actionA = new StringActionMemory("IQSA321", null);
        Assert.assertNotNull("exists", actionA);
        StringActionBean actionB = new MyStringAction("IQSA322");
        Assert.assertNotNull("exists", actionA);
        
        maleSocketA =
                InstanceManager.getDefault(StringActionManager.class)
                        .registerAction(actionA);
        Assert.assertNotNull("exists", maleSocketA);
        
        maleSocketB =
                InstanceManager.getDefault(StringActionManager.class)
                        .registerAction(actionB);
        Assert.assertNotNull("exists", maleSocketB);
    }

    @AfterEach
    @Override
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
    
    /**
     * This action is different from MyAnalogAction and is used to test the male socket.
     */
    private class MyStringAction extends AbstractStringAction {
        
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
        public void setValue(String value) throws JmriException {
            if (je != null) throw je;
            if (re != null) throw re;
            _value = value;
        }
        
        @Override
        public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
            if (_vetoChange) throw new java.beans.PropertyVetoException("Veto change", evt);
        }
        
    }
    
}
