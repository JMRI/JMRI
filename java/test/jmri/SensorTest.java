package jmri;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.Set;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.Assert;

/**
 * Tests for the Light class
 *
 * @author	Bob Jacobsen Copyright (C) 2008, 2010
 */
public class SensorTest extends TestCase {

    @SuppressWarnings("all")
    public void testStateConstants() {
        Assert.assertTrue("On and Off differ", (Sensor.ON & Sensor.OFF) == 0);
        Assert.assertTrue("On and Unknown differ", (Sensor.ON & Sensor.UNKNOWN) == 0);
        Assert.assertTrue("Off and Unknown differ", (Sensor.OFF & Sensor.UNKNOWN) == 0);
        Assert.assertTrue("On and Inconsistent differ", (Sensor.ON & Sensor.INCONSISTENT) == 0);
        Assert.assertTrue("Off and Inconsistent differ", (Sensor.OFF & Sensor.INCONSISTENT) == 0);
    }

    public void testSensor() throws JmriException {
        Sensor sensor = new MySensor();
        sensor.setState(Sensor.ON);
        Assert.assertTrue("Sensor is ON", sensor.getState() == Sensor.ON);
        sensor.setState(Sensor.OFF);
        Assert.assertTrue("Sensor is ON", sensor.getState() == Sensor.OFF);
        sensor.setCommandedState(Sensor.ON);
        Assert.assertTrue("Sensor is ON", sensor.getState() == Sensor.ON);
        sensor.setCommandedState(Sensor.OFF);
        Assert.assertTrue("Sensor is ON", sensor.getState() == Sensor.OFF);
        sensor.setState(Sensor.ON);
        Assert.assertTrue("Sensor is ON", sensor.getCommandedState() == Sensor.ON);
        sensor.setState(Sensor.OFF);
        Assert.assertTrue("Sensor is ON", sensor.getCommandedState() == Sensor.OFF);
    }

    // from here down is testing infrastructure
    public SensorTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {SensorTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(SensorTest.class);
        return suite;
    }

    
    
    private class MySensor implements Sensor {
        
        private int _state = NamedBean.UNKNOWN;

        @Override
        public void setState(int newState) {
            _state = newState;
        }

        @Override
        public int getState() {
            return _state;
        }

        @Override
        public String getUserName() {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public void setUserName(String s) throws BadUserNameException {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public String getSystemName() {
            throw new UnsupportedOperationException("Not supported.");
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
            throw new UnsupportedOperationException("Not supported.");
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
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public void setKnownState(int newState) throws JmriException {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public void setInverted(boolean inverted) {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public boolean getInverted() {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public boolean canInvert() {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public int getRawState() {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public void setSensorDebounceGoingActiveTimer(long timer) {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public long getSensorDebounceGoingActiveTimer() {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public void setSensorDebounceGoingInActiveTimer(long timer) {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public long getSensorDebounceGoingInActiveTimer() {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public void setUseDefaultTimerSettings(boolean flag) {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public boolean getUseDefaultTimerSettings() {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public void useDefaultTimerSettings(boolean flag) {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public boolean useDefaultTimerSettings() {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public void setReporter(Reporter re) {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public Reporter getReporter() {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public void setPullResistance(PullResistance r) {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public PullResistance getPullResistance() {
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
    
}
