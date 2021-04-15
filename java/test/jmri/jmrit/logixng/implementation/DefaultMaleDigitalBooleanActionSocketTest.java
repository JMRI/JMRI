package jmri.jmrit.logixng.implementation;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.util.Locale;
import java.util.Map;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.jmrit.logixng.*;
import jmri.util.JUnitUtil;

import jmri.jmrit.logixng.actions.AbstractDigitalBooleanAction;
import jmri.jmrit.logixng.actions.DigitalBooleanOnChange;

import jmri.jmrit.logixng.implementation.DefaultMaleDigitalBooleanActionSocket.DigitalBooleanActionDebugConfig;

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
        DigitalBooleanActionBean action = new DigitalBooleanOnChange("IQDB321", null, DigitalBooleanOnChange.Trigger.CHANGE);
        Assert.assertNotNull("exists", new DefaultMaleDigitalBooleanActionSocket(manager, action));
    }
    
    @Test
    public void testEvaluate() throws JmriException {
        ConditionalNG conditionalNG = new DefaultConditionalNGScaffold("IQC1", "A conditionalNG");  // NOI18N;
        
        MyDigitalBooleanAction action = new MyDigitalBooleanAction("IQDB321");
        action.setParent(conditionalNG);
        
        DefaultMaleDigitalBooleanActionSocket socket = new DefaultMaleDigitalBooleanActionSocket(manager, action);
        Assert.assertNotNull("exists", socket);
        
        socket.setParent(conditionalNG);
        socket.setEnabled(true);
        socket.setErrorHandlingType(MaleSocket.ErrorHandlingType.ThrowException);
        
        action.je = null;
        action.re = null;
        socket.execute(false, false);
        Assert.assertFalse(action._hasChangedToTrue);
        Assert.assertFalse(action._hasChangedToFalse);
        socket.execute(true, false);
        Assert.assertTrue(action._hasChangedToTrue);
        Assert.assertFalse(action._hasChangedToFalse);
        socket.execute(false, true);
        Assert.assertFalse(action._hasChangedToTrue);
        Assert.assertTrue(action._hasChangedToFalse);
        
        action.je = new JmriException("Test JmriException");
        action.re = null;
        Throwable thrown = catchThrowable( () -> socket.execute(false, false));
        assertThat(thrown)
                .withFailMessage("Evaluate throws an exception")
                .isNotNull()
                .isInstanceOf(JmriException.class)
                .hasMessage("Test JmriException");
        
        action.je = null;
        action.re = new RuntimeException("Test RuntimeException");
        thrown = catchThrowable( () -> socket.execute(false, false));
        assertThat(thrown)
                .withFailMessage("Evaluate throws an exception")
                .isNotNull()
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Test RuntimeException");
        
        // If the socket is not enabled, it shouldn't do anything
        socket.setEnabled(false);
        action.re = new RuntimeException("Test RuntimeException");
        thrown = catchThrowable( () -> socket.execute(false, false));
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
        action._hasChangedToTrue = false;
        action._hasChangedToFalse = false;
        socket.execute(true, false);
        Assert.assertFalse(action._hasChangedToTrue);
        Assert.assertFalse(action._hasChangedToFalse);
        action._hasChangedToTrue = false;
        action._hasChangedToFalse = false;
        socket.execute(false, false);
        Assert.assertFalse(action._hasChangedToTrue);
        Assert.assertFalse(action._hasChangedToFalse);
        config._dontExecute = false;
        action._hasChangedToTrue = false;
        action._hasChangedToFalse = false;
        socket.execute(true, false);
        Assert.assertTrue(action._hasChangedToTrue);
        Assert.assertFalse(action._hasChangedToFalse);
        action._hasChangedToTrue = false;
        action._hasChangedToFalse = false;
        socket.execute(false, true);
        Assert.assertFalse(action._hasChangedToTrue);
        Assert.assertTrue(action._hasChangedToFalse);
    }
    
    @Test
    public void testVetoableChange() {
        MyDigitalBooleanAction action = new MyDigitalBooleanAction("IQDB321");
        DefaultMaleDigitalBooleanActionSocket socket = new DefaultMaleDigitalBooleanActionSocket(manager, action);
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
        DefaultMaleDigitalBooleanActionSocket socket1 = new DefaultMaleDigitalBooleanActionSocket(manager, action1);
        
        MyDigitalBooleanAction action2 = new MyDigitalBooleanAction("IQDB01");
        DefaultMaleDigitalBooleanActionSocket socket2 = new DefaultMaleDigitalBooleanActionSocket(manager, action2);
        
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
        
        DigitalBooleanActionBean actionA = new DigitalBooleanOnChange("IQDB321", null, DigitalBooleanOnChange.Trigger.CHANGE);
        Assert.assertNotNull("exists", actionA);
        DigitalBooleanActionBean actionB = new MyDigitalBooleanAction("IQDB322");
        Assert.assertNotNull("exists", actionA);
        
        manager = InstanceManager.getDefault(DigitalBooleanActionManager.class);
        
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
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.tearDown();
    }
    
    
    /**
     * This action is different from MyStringAction and is used to test the
     * male socket.
     */
    private class MyDigitalBooleanAction extends AbstractDigitalBooleanAction {
        
        JmriException je = null;
        RuntimeException re = null;
        boolean _hasChangedToTrue = false;
        boolean _hasChangedToFalse = false;
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
        public void execute(boolean hasChangedToTrue, boolean hasChangedToFalse) throws JmriException {
            if (je != null) throw je;
            if (re != null) throw re;
           _hasChangedToTrue = hasChangedToTrue;
           _hasChangedToFalse = hasChangedToFalse;
        }
        
        @Override
        public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
            if (_vetoChange) throw new java.beans.PropertyVetoException("Veto change", evt);
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
