package jmri.jmrit.logixng.digital.implementation;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.util.Locale;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.analog.implementation.DefaultMaleAnalogActionSocketTest;
import jmri.util.JUnitUtil;

import jmri.jmrit.logixng.digital.boolean_actions.AbstractDigitalBooleanAction;
import jmri.jmrit.logixng.digital.boolean_actions.OnChange;

import jmri.jmrit.logixng.digital.implementation.DefaultMaleDigitalBooleanActionSocket.DigitalBooleanActionDebugConfig;

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
public class DefaultMaleDigitalBooleanActionSocketTest extends MaleSocketTestBase{

    @Override
    protected String getNewSystemName() {
        return InstanceManager.getDefault(DigitalBooleanActionManager.class)
                .getAutoSystemName();
    }
    
    @Test
    public void testCtor() {
        DigitalBooleanActionBean action = new OnChange("IQDB321", null, OnChange.ChangeType.CHANGE);
        Assert.assertNotNull("exists", new DefaultMaleDigitalBooleanActionSocket(action));
    }
    
    @Test
    public void testEvaluate() throws JmriException {
        MyDigitalBooleanAction action = new MyDigitalBooleanAction("IQDB321");
        DefaultMaleDigitalBooleanActionSocket socket = new DefaultMaleDigitalBooleanActionSocket(action);
        Assert.assertNotNull("exists", socket);
        
        socket.setEnabled(true);
        socket.setErrorHandlingType(MaleSocket.ErrorHandlingType.THROW);
        
        action.je = null;
        action.re = null;
        socket.execute(false);
        Assert.assertFalse(action._value);
        socket.execute(true);
        Assert.assertTrue(action._value);
        
        action.je = new JmriException("Test JmriException");
        action.re = null;
        Throwable thrown = catchThrowable( () -> socket.execute(false));
        assertThat(thrown)
                .withFailMessage("Evaluate throws an exception")
                .isNotNull()
                .isInstanceOf(JmriException.class)
                .hasMessage("Test JmriException");
        
        action.je = null;
        action.re = new RuntimeException("Test RuntimeException");
        thrown = catchThrowable( () -> socket.execute(false));
        assertThat(thrown)
                .withFailMessage("Evaluate throws an exception")
                .isNotNull()
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Test RuntimeException");
        
        // If the socket is not enabled, it shouldn't do anything
        socket.setEnabled(false);
        action.re = new RuntimeException("Test RuntimeException");
        thrown = catchThrowable( () -> socket.execute(false));
        assertThat(thrown)
                .withFailMessage("Evaluate does nothing")
                .isNull();
        
        // Test debug config
        socket.setEnabled(true);
        DigitalBooleanActionDebugConfig config = new DigitalBooleanActionDebugConfig();
        socket.setDebugConfig(config);
        action.je = null;
        action.re = null;
        config._dontExecute = true;
        action._value = false;
        socket.execute(true);
        Assert.assertFalse(action._value);
        action._value = true;
        socket.execute(false);
        Assert.assertTrue(action._value);
        config._dontExecute = false;
        action._value = false;
        socket.execute(true);
        Assert.assertTrue(action._value);
    }
    
    @Test
    public void testVetoableChange() {
        MyDigitalBooleanAction action = new MyDigitalBooleanAction("IQDB321");
        DefaultMaleDigitalBooleanActionSocket socket = new DefaultMaleDigitalBooleanActionSocket(action);
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
        MyDigitalBooleanAction action1 = new MyDigitalBooleanAction("IQDB1");
        DefaultMaleDigitalBooleanActionSocket socket1 = new DefaultMaleDigitalBooleanActionSocket(action1);
        
        MyDigitalBooleanAction action2 = new MyDigitalBooleanAction("IQDB01");
        DefaultMaleDigitalBooleanActionSocket socket2 = new DefaultMaleDigitalBooleanActionSocket(action2);
        
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
        
        DigitalBooleanActionBean actionA = new OnChange("IQDB321", null, OnChange.ChangeType.CHANGE);
        Assert.assertNotNull("exists", actionA);
        DigitalBooleanActionBean actionB = new MyDigitalBooleanAction("IQDB322");
        Assert.assertNotNull("exists", actionA);
        
        maleSocketA =
                InstanceManager.getDefault(DigitalBooleanActionManager.class)
                        .registerAction(actionA);
        Assert.assertNotNull("exists", maleSocketA);
        
        maleSocketB =
                InstanceManager.getDefault(DigitalBooleanActionManager.class)
                        .registerAction(actionB);
        Assert.assertNotNull("exists", maleSocketB);
    }

    @AfterEach
    @Override
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
    
    /**
     * This action is different from MyStringAction and is used to test the
     * male socket.
     */
    private class MyDigitalBooleanAction extends AbstractDigitalBooleanAction {
        
        JmriException je = null;
        RuntimeException re = null;
        boolean _value = false;
        boolean _vetoChange = false;
        
        MyDigitalBooleanAction(String sysName) {
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
            return Category.OTHER;
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
        public void execute(boolean hasChangedToTrue) throws JmriException {
            if (je != null) throw je;
            if (re != null) throw re;
           _value = hasChangedToTrue;
        }
        
        @Override
        public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
            if (_vetoChange) throw new java.beans.PropertyVetoException("Veto change", evt);
        }
        
    }
    
}
