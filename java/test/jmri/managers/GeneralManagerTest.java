package jmri.managers;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.Set;
import jmri.AnalogIO;
import jmri.AnalogIOManager;
import jmri.DigitalIO;
import jmri.DigitalIOManager;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Light;
import jmri.LightManager;
import jmri.NamedBean;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.StringIO;
import jmri.StringIOManager;
import jmri.Turnout;
import jmri.TurnoutManager;
import jmri.jmrix.ConnectionConfig;
import jmri.jmrix.ConnectionConfigManager;
import jmri.jmrix.SerialPortAdapter;
import jmri.jmrix.internal.InternalAdapter;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test the GeneralManager
 * 
 * @author Daniel Bergqvist 2019
 */
public class GeneralManagerTest {

    Turnout t1;
    Turnout t2;
    Sensor s1;
    Sensor s2;
    Light l1;
    Light l2;

    @Test
    public void testDigitalIO() {
        DigitalIO d;
        
        d = InstanceManager.getDefault(DigitalIOManager.class).getNamedBean("IT1");
        Assert.assertNotNull("turnout exists", d);
        Assert.assertTrue("bean is a turnout", d instanceof Turnout);
        Assert.assertTrue("bean is the expected bean", d == t1);
        
        d = InstanceManager.getDefault(DigitalIOManager.class).getNamedBean("IT2");
        Assert.assertNotNull("turnout exists", d);
        Assert.assertTrue("bean is a turnout", d instanceof Turnout);
        Assert.assertTrue("bean is the expected bean", d == t2);
        
        d = InstanceManager.getDefault(DigitalIOManager.class).getNamedBean("IS1");
        Assert.assertNotNull("sensor exists", d);
        Assert.assertTrue("bean is a sensor", d instanceof Sensor);
        Assert.assertTrue("bean is the expected bean", d == s1);
        
        d = InstanceManager.getDefault(DigitalIOManager.class).getNamedBean("IS2");
        Assert.assertNotNull("sensor exists", d);
        Assert.assertTrue("bean is a sensor", d instanceof Sensor);
        Assert.assertTrue("bean is the expected bean", d == s2);
        
        d = InstanceManager.getDefault(DigitalIOManager.class).getNamedBean("IL1");
        Assert.assertNotNull("light exists", d);
        Assert.assertTrue("bean is a light", d instanceof Light);
        Assert.assertTrue("bean is the expected bean", d == l1);
        
        d = InstanceManager.getDefault(DigitalIOManager.class).getNamedBean("IL2");
        Assert.assertNotNull("light exists", d);
        Assert.assertTrue("bean is a light", d instanceof Light);
        Assert.assertTrue("bean is the expected bean", d == l2);
        
        DigitalIO digitalIO = new MyDigitalIO("II1");
        InstanceManager.getDefault(DigitalIOManager.class).register(digitalIO);
        
        d = InstanceManager.getDefault(DigitalIOManager.class).getNamedBean("II1");
        Assert.assertNotNull("digitalIO exists", d);
        Assert.assertTrue("bean is the expected bean", d == digitalIO);
    }
    
    @Test
    public void testAnalogIO() {
        AnalogIO d;
        
        AnalogIO analogIO = new MyAnalogIO("IV1");
        InstanceManager.getDefault(AnalogIOManager.class).register(analogIO);
        
        d = InstanceManager.getDefault(AnalogIOManager.class).getNamedBean("IV1");
        Assert.assertNotNull("analogIO exists", d);
        Assert.assertTrue("bean is the expected bean", d == analogIO);
    }
    
    @Test
    public void testStringIO() {
        StringIO d;
        
        StringIO stringIO = new MyStringIO("IZ1");
        InstanceManager.getDefault(StringIOManager.class).register(stringIO);
        
        d = InstanceManager.getDefault(StringIOManager.class).getNamedBean("IZ1");
        Assert.assertNotNull("stringIO exists", d);
        Assert.assertTrue("bean is the expected bean", d == stringIO);
    }
    
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initConnectionConfigManager();
        
        SerialPortAdapter a = new InternalAdapter();
        ConnectionConfig c = new jmri.jmrix.internal.ConnectionConfig(a) {
            @Override
            public String getConnectionName() {
                return "I";
            }
        };
        
        ConnectionConfigManager ccm = InstanceManager.getDefault(ConnectionConfigManager.class);
        ccm.add(c);
        
        t1 = InstanceManager.getDefault(TurnoutManager.class).provide("IT1");
        t2 = InstanceManager.getDefault(TurnoutManager.class).provide("IT2");
        s1 = InstanceManager.getDefault(SensorManager.class).provide("IS1");
        s2 = InstanceManager.getDefault(SensorManager.class).provide("IS2");
        l1 = InstanceManager.getDefault(LightManager.class).provide("IL1");
        l2 = InstanceManager.getDefault(LightManager.class).provide("IL2");
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }


    private static abstract class AbstractIO implements NamedBean {

        private final String _systemName;

        public AbstractIO(String systemName) {
            this._systemName = systemName;
        }

        @Override
        public String getUserName() {
            return null;
        }

        @Override
        public void setUserName(String s) throws BadUserNameException {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public String getSystemName() {
            return _systemName;
        }

        @Override
        public String getDisplayName() {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public String getFullyFormattedDisplayName() {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener l, String name, String listenerRef) {
//            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener l) {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener l) {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public void updateListenerRef(PropertyChangeListener l, String newName) {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public String getListenerRef(PropertyChangeListener l) {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public ArrayList<String> getListenerRefs() {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public int getNumPropertyChangeListeners() {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public PropertyChangeListener[] getPropertyChangeListenersByReference(String name) {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public void dispose() {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public void setState(int s) throws JmriException {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public int getState() {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public String describeState(int state) {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public String getComment() {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public void setComment(String comment) {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public void setProperty(String key, Object value) {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public Object getProperty(String key) {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public void removeProperty(String key) {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public Set<String> getPropertyKeys() {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public String getBeanType() {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public int compareSystemNameSuffix(String suffix1, String suffix2, NamedBean n2) {
            return suffix1.compareTo(suffix2);
        }
        
    }

    private static class MyDigitalIO extends AbstractIO implements DigitalIO {
        
        public MyDigitalIO(String systemName) {
            super(systemName);
        }

        @Override
        public boolean isConsistentState() {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public void setCommandedState(int s) {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public int getCommandedState() {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public int getKnownState() {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public void requestUpdateFromLayout() {
            throw new UnsupportedOperationException("Not supported.");
        }

    }

    private static class MyAnalogIO extends AbstractIO implements AnalogIO {
        
        public MyAnalogIO(String systemName) {
            super(systemName);
        }

        @Override
        public void setCommandedAnalogValue(float value) throws JmriException {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public float getCommandedAnalogValue() {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public float getMin() {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public float getMax() {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public float getResolution() {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public AbsoluteOrRelative getAbsoluteOrRelative() {
            throw new UnsupportedOperationException("Not supported.");
        }

    }

    private static class MyStringIO extends AbstractIO implements StringIO {
        
        public MyStringIO(String systemName) {
            super(systemName);
        }

        @Override
        public void setCommandedStringValue(String value) throws JmriException {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public String getCommandedStringValue() {
            throw new UnsupportedOperationException("Not supported.");
        }

    }

}
