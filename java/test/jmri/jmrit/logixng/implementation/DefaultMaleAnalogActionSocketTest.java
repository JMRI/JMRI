package jmri.jmrit.logixng.implementation;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.util.Locale;
import java.util.Map;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.jmrit.logixng.*;
import jmri.util.JUnitUtil;

import jmri.jmrit.logixng.actions.AbstractAnalogAction;
import jmri.jmrit.logixng.actions.AnalogActionMemory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


/**
 * Test DefaultMaleAnalogSocket
 * 
 * @author Daniel Bergqvist 2018
 */
public class DefaultMaleAnalogActionSocketTest extends MaleSocketTestBase {

    @Override
    protected String getNewSystemName() {
        return InstanceManager.getDefault(AnalogActionManager.class)
                .getAutoSystemName();
    }
    
    @Test
    public void testCtor() {
        AnalogActionBean action = new AnalogActionMemory("IQAA321", null);
        Assert.assertNotNull("object exists", new DefaultMaleAnalogActionSocket(manager, action));
    }
    
    @Test
    public void testSetValue() throws JmriException {
        ConditionalNG conditionalNG = new DefaultConditionalNGScaffold("IQC1", "A conditionalNG");  // NOI18N;
        
        MyAnalogAction action = new MyAnalogAction("IQAA321");
        action.setParent(conditionalNG);
        
        DefaultMaleAnalogActionSocket socket = new DefaultMaleAnalogActionSocket(manager, action);
        Assert.assertNotNull("exists", socket);
        
        socket.setParent(conditionalNG);
        socket.setEnabled(true);
        socket.setErrorHandlingType(MaleSocket.ErrorHandlingType.ThrowException);
        
        action.je = null;
        action.re = null;
        socket.setValue(9.121);
        Assert.assertTrue(9.121 == action._value);
        socket.setValue(572.1);
        Assert.assertTrue(572.1 == action._value);
        socket.setValue(0.0);
        Assert.assertTrue(0.0 == action._value);
        
        action.je = new JmriException("Test JmriException");
        action.re = null;
        Throwable thrown = catchThrowable( () -> socket.setValue(9.121));
        assertThat(thrown)
                .withFailMessage("Evaluate throws an exception")
                .isNotNull()
                .isInstanceOf(JmriException.class)
                .hasMessage("Test JmriException");
        
        action.je = null;
        action.re = new RuntimeException("Test RuntimeException");
        thrown = catchThrowable( () -> socket.setValue(32.11));
        assertThat(thrown)
                .withFailMessage("Evaluate throws an exception")
                .isNotNull()
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Test RuntimeException");
        
        // If the socket is not enabled, it shouldn't do anything
        socket.setEnabled(false);
        action.re = new RuntimeException("Test RuntimeException");
        thrown = catchThrowable( () -> socket.setValue(9.23));
        assertThat(thrown)
                .withFailMessage("Evaluate does nothing")
                .isNull();
        
        // Test debug config
        socket.setEnabled(true);
        DefaultMaleAnalogActionSocket.AnalogActionDebugConfig config = new DefaultMaleAnalogActionSocket.AnalogActionDebugConfig();
        socket.setDebugConfig(config);
        action.je = null;
        action.re = null;
        config._dontExecute = true;
        action._value = 19.23;
        socket.setValue(32.11);
        Assert.assertTrue(19.23 == action._value);
        config._dontExecute = false;
        action._value = 23.111;
        socket.setValue(9.23);
        Assert.assertTrue(9.23 == action._value);
    }
    
    @Test
    public void testEvaluateErrors() {
        ConditionalNG conditionalNG = new DefaultConditionalNGScaffold("IQC1", "A conditionalNG");  // NOI18N;
        
        MyAnalogAction action = new MyAnalogAction("IQAA321");
        action.setParent(conditionalNG);
        
        DefaultMaleAnalogActionSocket socket = new DefaultMaleAnalogActionSocket(manager, action);
        Assert.assertNotNull("exists", socket);
        
        socket.setParent(conditionalNG);
        socket.setEnabled(true);
        socket.setErrorHandlingType(MaleSocket.ErrorHandlingType.ThrowException);
        
        Throwable thrown = catchThrowable( () -> socket.setValue(Double.NaN));
        assertThat(thrown)
                .withFailMessage("Evaluate throws an exception")
                .isNotNull()
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("The value is NaN");
        
        thrown = catchThrowable( () -> socket.setValue(Double.NEGATIVE_INFINITY));
        assertThat(thrown)
                .withFailMessage("Evaluate throws an exception")
                .isNotNull()
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("The value is negative infinity");
        
        thrown = catchThrowable( () -> socket.setValue(Double.POSITIVE_INFINITY));
        assertThat(thrown)
                .withFailMessage("Evaluate throws an exception")
                .isNotNull()
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("The value is positive infinity");
    }
    
    @Test
    public void testVetoableChange() {
        MyAnalogAction action = new MyAnalogAction("IQAA321");
        DefaultMaleAnalogActionSocket socket = new DefaultMaleAnalogActionSocket(manager, action);
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
        MyAnalogAction action1 = new MyAnalogAction("IQAA1");
        DefaultMaleAnalogActionSocket socket1 = new DefaultMaleAnalogActionSocket(manager, action1);
        
        MyAnalogAction action2 = new MyAnalogAction("IQAA01");
        DefaultMaleAnalogActionSocket socket2 = new DefaultMaleAnalogActionSocket(manager, action2);
        
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
        
        AnalogActionBean actionA = new AnalogActionMemory("IQAA321", null);
        Assert.assertNotNull("exists", actionA);
        AnalogActionBean actionB = new MyAnalogAction("IQAA322");
        Assert.assertNotNull("exists", actionA);
        
        manager = InstanceManager.getDefault(AnalogActionManager.class);
        
        maleSocketA =
                InstanceManager.getDefault(AnalogActionManager.class)
                        .registerAction(actionA);
        Assert.assertNotNull("exists", maleSocketA);
        
        maleSocketB =
                InstanceManager.getDefault(AnalogActionManager.class)
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
     * This action is different from AnalogActionMemory and is used to test the
     * male socket.
     */
    private class MyAnalogAction extends AbstractAnalogAction {
        
        JmriException je = null;
        RuntimeException re = null;
        double _value = 0.0;
        boolean _vetoChange = false;
        
        MyAnalogAction(String sysName) {
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
        public void setup() {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public void setValue(double value) throws JmriException {
            if (je != null) throw je;
            if (re != null) throw re;
            _value = value;
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
