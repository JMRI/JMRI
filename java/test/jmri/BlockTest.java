package jmri;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.util.Collection;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;

import jmri.implementation.SignalSpeedMap;
import jmri.jmrix.internal.InternalSensorManager;
import jmri.jmrix.internal.InternalSystemConnectionMemo;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 * Tests for the Block class
 *
 * @author Bob Jacobsen Copyright (C) 2006
 */
public class BlockTest {

    /**
     * Normally, users create Block objects via a manager, but we test the
     * direct create here. If it works, we can use it for testing.
     */
    @Test
    public void testDirectCreate() {
        Block b = new Block("SystemName");
        Assert.assertNotNull("Block Created",b);
    }

    @Test
    @SuppressWarnings("unlikely-arg-type") // String / StringBuffer seems to be unrelated to Block
    public void testEquals() {
        Block b1 = new Block("SystemName1");
        Block b2 = new Block("SystemName2");
        
        //multiple Block objects with same SystemName are really the same
        Block b1a = new Block("SystemName1");
        
        Assert.assertTrue(b1.equals(b1)); // identity
        Assert.assertFalse(b1.equals(b2)); // blocks are named objects

        Assert.assertTrue(b1a.equals(b1));
        Assert.assertTrue(b1.equals(b1a)); // commutes
        
        // check null
        Assert.assertFalse(b1.equals(null));

        // check another type
        Assert.assertFalse(b1.equals(new StringBuffer("foo")));
        Assert.assertFalse(b1.equals("foo"));
    }

    @Test
    public void testHashCode() {
        Block b1 = new Block("SystemName1");
        
        //multiple Block objects with same SystemName are really the same
        Block b1a = new Block("SystemName1");
        
        Assert.assertTrue(b1.hashCode() == b1a.hashCode());

        b1a.setLength(120);
        b1a.setCurvature(21);
        Assert.assertTrue(b1.hashCode() == b1a.hashCode());
    }
    
    @Test
    public void testSensorAdd() {
        Block b = new Block("SystemName");
        b.setSensor("IS12");
    }

    static int count;

    @Test
    public void testSensorInvoke() throws JmriException {
        SensorManager sm = new InternalSensorManager(InstanceManager.getDefault(InternalSystemConnectionMemo.class));
        count = 0;
        Block b = new Block("SystemName") {
            @Override
            void handleSensorChange(java.beans.PropertyChangeEvent e) {
                count++;
            }
        };
        Sensor s = sm.provideSensor("IS12");
        b.setNamedSensor(jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle("IS12", s));
        sm.provideSensor("IS12").setState(jmri.Sensor.ACTIVE);
        Assert.assertEquals("count of detected changes", 1, count);
    }

    @Test
    public void testValueField() {
        Block b = new Block("SystemName");
        b.setValue("string");
        Assert.assertEquals("Returned Object matches", "string", b.getValue());
    }

    @Test
    public void testSensorSequence() throws JmriException {
        SensorManager sm = new InternalSensorManager(InstanceManager.getDefault(InternalSystemConnectionMemo.class));
        count = 0;
        Block b = new Block("SystemName");
        Sensor s = sm.provideSensor("IS12");
        s.setState(jmri.Sensor.UNKNOWN);
        
        Assert.assertEquals("Initial state", Block.UNDETECTED, b.getState()); // state until sensor is set
        
        b.setSensor("IS12");
        s.setState(jmri.Sensor.ACTIVE);
        Assert.assertEquals("State with sensor active", Block.OCCUPIED, s.getState());
        s.setState(jmri.Sensor.INACTIVE);
        Assert.assertEquals("State with sensor inactive", Block.UNOCCUPIED, s.getState());
    }

    @Test
    public void testCoding() {
        Assert.assertTrue("Block.OCCUPIED != Block.UNOCCUPIED", Block.OCCUPIED != Block.UNOCCUPIED);
        Assert.assertTrue("Block.OCCUPIED != Block.UNDETECTED", Block.OCCUPIED != Block.UNDETECTED);
        Assert.assertTrue("Block.OCCUPIED != Block.UNKNOWN", Block.OCCUPIED != Block.UNKNOWN);
        Assert.assertTrue("Block.OCCUPIED != Block.INCONSISTENT", Block.OCCUPIED != Block.INCONSISTENT);
        Assert.assertTrue("Block.UNOCCUPIED != Block.UNDETECTED", Block.UNOCCUPIED != Block.UNDETECTED);
        Assert.assertTrue("Block.UNOCCUPIED != Block.UNKNOWN", Block.UNOCCUPIED != Block.UNKNOWN);
        Assert.assertTrue("Block.UNOCCUPIED != Block.INCONSISTENT", Block.UNOCCUPIED != Block.INCONSISTENT);
        Assert.assertTrue("Block.UNDETECTED != Block.UNKNOWN", Block.UNDETECTED != Block.UNKNOWN);
        Assert.assertTrue("Block.UNDETECTED != Block.INCONSISTENT", Block.UNDETECTED != Block.INCONSISTENT);
        Assert.assertTrue("Block.UNKNOWN != Block.INCONSISTENT", Block.UNKNOWN != Block.INCONSISTENT);
    }

    // test going active with only one neighbor
    @Test
    public void testFirstGoActive() throws JmriException {
        SensorManager sm = new InternalSensorManager(InstanceManager.getDefault(InternalSystemConnectionMemo.class));

        Block b1 = new Block("SystemName1");

        Block b2 = new Block("SystemName2");
        Sensor s2 = sm.provideSensor("IS2");
        b2.setNamedSensor(jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle("IS2", s2));
        s2.setState(Sensor.ACTIVE);
        b2.setValue("b2 contents");

        Path p = new Path();
        p.setBlock(b2);

        b1.addPath(p);

        // actual test
        b1.goingActive();
        Assert.assertEquals("Value transferred", "b2 contents", b1.getValue());
    }

    // Test going active with two neighbors, one active.
    // b2 is between b1 and b3. b1 contains a train
    @Test
    public void testOneOfTwoGoesActive() throws JmriException {
        SensorManager sm = new InternalSensorManager(InstanceManager.getDefault(InternalSystemConnectionMemo.class));

        Block b1 = new Block("SystemName1");
        Block b2 = new Block("SystemName2");
        Block b3 = new Block("SystemName3");

        Sensor s1 = sm.provideSensor("IS1");
        b1.setNamedSensor(jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle("IS1", s1));
        s1.setState(Sensor.ACTIVE);
        b1.setValue("b1 contents");

        Sensor s2 = sm.provideSensor("IS2");
        b2.setNamedSensor(jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle("IS2", s2));
        s2.setState(Sensor.INACTIVE);

        Sensor s3 = sm.provideSensor("IS3");
        b3.setNamedSensor(jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle("IS3", s3));
        s3.setState(Sensor.INACTIVE);

        Path p21 = new Path();
        p21.setBlock(b1);
        p21.setFromBlockDirection(Path.RIGHT);
        p21.setToBlockDirection(Path.LEFT);
        b2.addPath(p21);

        Path p23 = new Path();
        p23.setBlock(b3);
        p23.setFromBlockDirection(Path.LEFT);
        p23.setToBlockDirection(Path.RIGHT);
        b2.addPath(p23);

        // actual test
        b2.goingActive();
        Assert.assertEquals("State", Block.OCCUPIED, b2.getState());
        Assert.assertEquals("Value transferred", "b1 contents", b2.getValue());
        Assert.assertEquals("Direction", Path.RIGHT, b2.getDirection());

    }

    // Test going active with two neighbors, both active.
    // b2 is between b1 and b3. 
    @Test
    public void testTwoOfTwoGoesActive() throws JmriException {
        SensorManager sm = new InternalSensorManager(InstanceManager.getDefault(InternalSystemConnectionMemo.class));

        Block b1 = new Block("SystemName1");
        Block b2 = new Block("SystemName2");
        Block b3 = new Block("SystemName3");

        Sensor s1 = sm.provideSensor("IS1");
        b1.setNamedSensor(jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle("IS1", s1));
        s1.setState(Sensor.ACTIVE);
        b1.setValue("b1 contents");
        b1.setDirection(Path.RIGHT);

        Sensor s2 = sm.provideSensor("IS2");
        b2.setNamedSensor(jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle("IS2", s2));
        s2.setState(Sensor.INACTIVE);

        Sensor s3 = sm.provideSensor("IS3");
        b3.setNamedSensor(jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle("IS3", s3));
        s3.setState(Sensor.ACTIVE);
        b3.setValue("b3 contents");
        b3.setDirection(Path.RIGHT);

        Path p21 = new Path();
        p21.setBlock(b1);
        p21.setFromBlockDirection(Path.RIGHT);
        p21.setToBlockDirection(Path.LEFT);
        b2.addPath(p21);

        Path p23 = new Path();
        p23.setBlock(b3);
        p23.setFromBlockDirection(Path.LEFT);
        p23.setToBlockDirection(Path.RIGHT);
        b2.addPath(p23);
        
        // actual test
        b2.goingActive();
        Assert.assertEquals("State", Block.OCCUPIED, b2.getState());
        Assert.assertEquals("Value transferred", "b1 contents", b2.getValue());
        Assert.assertEquals("Direction", Path.RIGHT, b2.getDirection());

    }

    // Test going active with two neighbors, both active, where FROM is a combination direction.
    // b2 is between b1 and b3. 
    @Test
    public void testTwoOfTwoGoesActiveCombination() throws JmriException {
        SensorManager sm = new InternalSensorManager(InstanceManager.getDefault(InternalSystemConnectionMemo.class));

        Block b1 = new Block("SystemName1");
        Block b2 = new Block("SystemName2");
        Block b3 = new Block("SystemName3");

        Sensor s1 = sm.provideSensor("IS1");
        b1.setNamedSensor(jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle("IS1", s1));
        s1.setState(Sensor.ACTIVE);
        b1.setValue("b1 contents");
        b1.setDirection(Path.NORTH_WEST); //combination direction

        Sensor s2 = sm.provideSensor("IS2");
        b2.setNamedSensor(jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle("IS2", s2));
        s2.setState(Sensor.INACTIVE);

        Sensor s3 = sm.provideSensor("IS3");
        b3.setNamedSensor(jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle("IS3", s3));
        s3.setState(Sensor.ACTIVE);
        b3.setValue("b3 contents");
        b3.setDirection(Path.NORTH);

        Path p21 = new Path();
        p21.setBlock(b1);
        p21.setFromBlockDirection(Path.NORTH);
        p21.setToBlockDirection(Path.SOUTH);
        b2.addPath(p21);

        Path p23 = new Path();
        p23.setBlock(b3);
        p23.setFromBlockDirection(Path.EAST);
        p23.setToBlockDirection(Path.NORTH);
        b2.addPath(p23);
        
        // actual test
        b2.goingActive();
        Assert.assertEquals("State", Block.OCCUPIED, b2.getState());
        Assert.assertEquals("Value transferred", "b1 contents", b2.getValue());
        Assert.assertEquals("Direction", Path.NORTH, b2.getDirection());

    }

    @Test
    public void testReporterAdd() {
        ReporterManager rm = jmri.InstanceManager.getDefault(ReporterManager.class);
        Block b = new Block("SystemName");
        b.setReporter(rm.provideReporter("IR22"));
    }

    @Test
    public void testReporterInvokeAll() {
        ReporterManager rm = jmri.InstanceManager.getDefault(ReporterManager.class);
        count = 0;
        Block b = new Block("SystemName") {
            @Override
            void handleReporterChange(java.beans.PropertyChangeEvent e) {
                count++;
            }
        };
        b.setReporter(rm.provideReporter("IR22"));
        rm.provideReporter("IR22").setReport("report");
        // For each report, there are two PropertyChangeEvents -
        // "currentReport" and "lastReport"
        Assert.assertEquals("count of detected changes", 2, count);
    }

    @Test
    public void testReporterInvokeCurrent() {
        ReporterManager rm = jmri.InstanceManager.getDefault(ReporterManager.class);
        count = 0;
        Block b = new Block("SystemName") {
            @Override
            void handleReporterChange(java.beans.PropertyChangeEvent e) {
                if (e.getPropertyName().equals("currentReport")) {
                    count++;
                }
            }
        };
        b.setReporter(rm.provideReporter("IR22"));
        rm.provideReporter("IR22").setReport("report");
        // Only detecting "currentReport" PropertyChangeEvent
        Assert.assertEquals("count of detected changes", 1, count);

        rm.provideReporter("IR22").setReport(null);
        // Current report should change
        Assert.assertEquals("count of detected changes", 2, count);
    }

    @Test
    public void testReporterInvokeLast() {
        ReporterManager rm = jmri.InstanceManager.getDefault(ReporterManager.class);
        count = 0;
        Block b = new Block("SystemName") {
            @Override
            void handleReporterChange(java.beans.PropertyChangeEvent e) {
                if (e.getPropertyName().equals("lastReport")) {
                    count++;
                }
            }
        };
        b.setReporter(rm.provideReporter("IR22"));
        rm.provideReporter("IR22").setReport("report");
        // Only detecting "lastReport" PropertyChangeEvent
        Assert.assertEquals("count of detected changes", 1, count);

        rm.provideReporter("IR22").setReport(null);
        // Last report should not change
        Assert.assertEquals("count of detected changes", 1, count);
    }

    @Test
    public void testGetLocoAddress(){
        Block b = new Block("SystemName");
        Assert.assertEquals("address", new DccLocoAddress(1234,LocoAddress.Protocol.DCC), b.getLocoAddress("1234 enter"));
    }

    @Test
    public void testGetLocoDirection(){
        Block b = new Block("SystemName");
        Assert.assertEquals("direction", jmri.PhysicalLocationReporter.Direction.ENTER, b.getDirection("1234 enter"));
    }

    @Test
    public void testAddRemoveListener() throws JmriException {
        
        Block b = new Block("BlockSystemName");
        b.setUserName("Start user id");
        Listen listen = new Listen();
        Assert.assertEquals("no listener at start of test",0, b.getNumPropertyChangeListeners());
        
        b.addPropertyChangeListener(listen);
        Assert.assertEquals("1 listener added",1, b.getNumPropertyChangeListeners());
        
        b.setUserName("user id");
        Assert.assertEquals("prop ev name","UserName", listen.getPropertyName(0));
        Assert.assertEquals("old value","Start user id", listen.getOldValue(0));
        Assert.assertEquals("new value","user id", listen.getNewValue(0));
                
        b.removePropertyChangeListener(listen);
        Assert.assertEquals("listener removed",0, b.getNumPropertyChangeListeners());
        
        b.setUserName("Changed user id");       
        Assert.assertEquals("list size still 1",1, listen.getNumPropChanges());
    }
    
    
    /**
     * Test Property Changes for Adding Sensors to a block.
     * 
     * Add a Sensor with UNKNOWN state to Block.
     * Prop change for Block state UNDETECTED to UNKNOWN
     * Prop Change Block added Sensor
     * 
     * Change the Block Sensor to new Sensor with INACTIVE state
     * Prop change for Block state UNKNOWN to UNOCCUPIED
     * Prop Change Block Sensor change
     * 
     * Remove the Block Sensor
     * Prop change for Block state UNOCCUPIED to UNDETECED
     * Prop Change Block Sensor change
     * 
     * @throws JmriException on test error.
     */
    @Test
    public void testAddSensorPropertyChange() throws JmriException {
        
        SensorManager sm = InstanceManager.getDefault(SensorManager.class);
        Sensor sensorUnknownState = sm.provide("ISunknownState");
        sensorUnknownState.setKnownState(Sensor.UNKNOWN);
        
        Sensor sensorInactiveState = sm.provide("ISInactiveState");
        sensorInactiveState.setKnownState(Sensor.INACTIVE);
        
        Block b = new Block("BlockSystemName");
        Assert.assertEquals("Block starts state undetected", Block.UNDETECTED, b.getState());
        Assert.assertEquals("Block starts no sensor", null, b.getSensor());
        Listen listen = new Listen();
        b.addPropertyChangeListener(listen);
        
        b.setSensor(null);
        Assert.assertEquals("no prop change null to null",0, listen.getNumPropChanges());
        
        b.setSensor("");
        Assert.assertEquals("no prop change null to empty",0, listen.getNumPropChanges());
        
        b.setSensor("ISunknownState");
        Assert.assertEquals("list size +2, state change, occ sense change",2, listen.getNumPropChanges());
        Assert.assertEquals("prop ev name","state", listen.getPropertyName(0));
        Assert.assertEquals("old value",Block.UNDETECTED, (int)listen.getOldValue(0));
        Assert.assertEquals("new value", Block.UNKNOWN, (int)listen.getNewValue(0));
        
        Assert.assertEquals("prop ev name",Block.OCC_SENSOR_CHANGE, listen.getPropertyName(1));
        Assert.assertEquals("old value",null, listen.getOldValue(1));
        Assert.assertEquals("new value",sensorUnknownState, listen.getNewValue(1));
        
        b.setSensor("ISunknownState");
        Assert.assertEquals("same sensor, no prop change",2, listen.getNumPropChanges());
        
        b.setSensor("ISInactiveState");
        Assert.assertEquals("list size +2, state change, occ sense change",4, listen.getNumPropChanges());
        Assert.assertEquals("prop ev name", "state", listen.getPropertyName(2));
        Assert.assertEquals("old value", Block.UNKNOWN, (int)listen.getOldValue(2));
        Assert.assertEquals("new value", Block.UNOCCUPIED, (int)listen.getNewValue(2));
        
        Assert.assertEquals("prop ev name", Block.OCC_SENSOR_CHANGE, listen.getPropertyName(3));
        Assert.assertEquals("old value",sensorUnknownState, listen.getOldValue(3));
        Assert.assertEquals("new value", sensorInactiveState, listen.getNewValue(3));
        
        b.setSensor(null);
        Assert.assertEquals("list size +2, state change, occ sense change",6, listen.getNumPropChanges());
        Assert.assertEquals("prop ev name","state", listen.getPropertyName(4));
        Assert.assertEquals("old value",Block.UNOCCUPIED, (int)listen.getOldValue(4));
        Assert.assertEquals("setSensor sets status unoccupied to undetected", Block.UNDETECTED, (int)listen.getNewValue(4));
        
        Assert.assertEquals("prop ev name",Block.OCC_SENSOR_CHANGE, listen.getPropertyName(5));
        Assert.assertEquals("old value", sensorInactiveState, listen.getOldValue(5));
        Assert.assertEquals("new value", null, listen.getNewValue(5));
        
    }
    
    @Test
    public void testAddReporterPropertyChange() throws JmriException {
        
        ReporterManager rm = jmri.InstanceManager.getDefault(ReporterManager.class);
        Reporter rep = rm.provide("IR123");
        
        Block b = new Block("BlockSystemName");
        Listen listen = new Listen();
        b.addPropertyChangeListener(listen);
        
        b.setReporter(rep);
        Assert.assertEquals("list size 1, reporter change",1, listen.getNumPropChanges());
        Assert.assertEquals("prop ev name", Block.BLOCK_REPORTER_CHANGE, listen.getPropertyName(0));
        Assert.assertEquals("old value", null, listen.getOldValue(0));
        Assert.assertEquals("new value", rep, listen.getNewValue(0));
        
        b.setReporter(rep);
        Assert.assertEquals("list size still 1",1, listen.getNumPropChanges());
        
        b.setReporter(null);
        Assert.assertEquals("+1 property change",2, listen.getNumPropChanges());
        Assert.assertEquals("prop ev name",Block.BLOCK_REPORTER_CHANGE, listen.getPropertyName(1));
        Assert.assertEquals("old value", rep, listen.getOldValue(1));
        Assert.assertEquals("new value", null, listen.getNewValue(1));
    }
    
    /**
     * Testing of the Reporting Current Flag being set, NOT of the Reporter mechanism.
     * @throws JmriException on test error.
     */
    @Test
    public void testSetReportingCurrentPropertyChange() throws JmriException {
        
        Block b = new Block("BlockSystemName");
        Listen listen = new Listen();
        
        b.setReportingCurrent(false); // set initial test state
        b.addPropertyChangeListener(listen);
        
        b.setReportingCurrent(true);
        Assert.assertEquals("list size 1, property change",1, listen.getNumPropChanges());
        Assert.assertEquals("prop ev name", Block.BLOCK_REPORTING_CURRENT, listen.getPropertyName(0));
        Assert.assertEquals("old value", Boolean.FALSE, listen.getOldValue(0));
        Assert.assertEquals("new value", Boolean.TRUE, listen.getNewValue(0));
        
        b.setReportingCurrent(true);
        Assert.assertEquals("list size still 1",1, listen.getNumPropChanges());
        
        b.setReportingCurrent(false);
        Assert.assertEquals("+1 property change",2, listen.getNumPropChanges());
        Assert.assertEquals("prop ev name", Block.BLOCK_REPORTING_CURRENT, listen.getPropertyName(1));
        Assert.assertEquals("old value", Boolean.TRUE, listen.getOldValue(1));
        Assert.assertEquals("new value", Boolean.FALSE, listen.getNewValue(1));
        
    }
    
    @Test
    public void testSetStatePropertyChangeName() throws JmriException {
        
        Block b = new Block("BlockSystemName");
        Listen listen = new Listen();
        
        b.setState(Block.UNDETECTED); // set initial test state
        b.addPropertyChangeListener(listen);
        
        b.setState(Block.UNOCCUPIED);
        Assert.assertEquals("1 property change",1, listen.getNumPropChanges());
        Assert.assertEquals("prop ev name", "state", listen.getPropertyName(0));
        Assert.assertEquals("new value", Block.UNOCCUPIED, (int)listen.getNewValue(0));
        
    }
    
    @Test
    public void testSetValuePropertyChange() throws JmriException {
        
        Block b = new Block("BlockSystemName");
        Listen listen = new Listen();
        
        b.setValue(null); // set initial test state
        b.addPropertyChangeListener(listen);
        
        b.setValue("String Block Value");
        Assert.assertEquals("1 property change",1, listen.getNumPropChanges());
        Assert.assertEquals("prop ev name", "value", listen.getPropertyName(0));
        Assert.assertEquals("old value", null, listen.getOldValue(0));
        Assert.assertEquals("new value", "String Block Value", listen.getNewValue(0));
        
        b.setValue("String Block Value");
        Assert.assertEquals("list size still 1",1, listen.getNumPropChanges());
        
        b.setValue("New Block Value");
        Assert.assertEquals("+1 property change",2, listen.getNumPropChanges());
        Assert.assertEquals("prop ev name", "value", listen.getPropertyName(1));
        Assert.assertEquals("old value", "String Block Value", listen.getOldValue(1));
        Assert.assertEquals("new value", "New Block Value", listen.getNewValue(1));
        
        b.setValue(null);
        Assert.assertEquals("+1 property change",3, listen.getNumPropChanges());
        Assert.assertEquals("prop ev name", "value", listen.getPropertyName(2));
        Assert.assertEquals("old value", "New Block Value", listen.getOldValue(2));
        Assert.assertEquals("new value", null, listen.getNewValue(2));
        
    }
    
    @Test
    public void testSetDirectionPropertyChange() throws JmriException {
        
        Block b = new Block("BlockSystemName");
        Listen listen = new Listen();
        
        b.setDirection(Path.NORTH); // set initial test state
        b.addPropertyChangeListener(listen);
        
        b.setDirection(Path.EAST);
        Assert.assertEquals("1 property change",1, listen.getNumPropChanges());
        Assert.assertEquals("Direction set",Path.EAST, b.getDirection());
        Assert.assertEquals("prop ev name", "direction", listen.getPropertyName(0));
        Assert.assertEquals("old value", Path.NORTH, (int)listen.getOldValue(0));
        Assert.assertEquals("new value", Path.EAST, (int)listen.getNewValue(0));
        
        b.setDirection(Path.EAST);
        Assert.assertEquals("list size still 1",1, listen.getNumPropChanges());
        
        b.setDirection(Path.WEST);
        Assert.assertEquals("+1 property change",2, listen.getNumPropChanges());
        Assert.assertEquals("Direction set",Path.WEST, b.getDirection());
        Assert.assertEquals("prop ev name", "direction", listen.getPropertyName(1));
        Assert.assertEquals("old value", Path.EAST, (int)listen.getOldValue(1));
        Assert.assertEquals("new value", Path.WEST, (int)listen.getNewValue(1));
        
    }
    
    @Test
    public void testSetGetPermissiveWorkingPropertyChange() throws JmriException {
        
        Block b = new Block("BlockSystemName");
        Listen listen = new Listen();
        
        Assert.assertFalse("block not permissive to start", b.getPermissiveWorking());
        b.addPropertyChangeListener(listen);
        
        b.setPermissiveWorking(true);
        Assert.assertEquals("1 property change",1, listen.getNumPropChanges());
        Assert.assertTrue("block permissive set", b.getPermissiveWorking());
        Assert.assertEquals("prop ev name",Block.BLOCK_PERMISSIVE_CHANGE, listen.getPropertyName(0));
        Assert.assertEquals("old value", Boolean.FALSE, listen.getOldValue(0));
        Assert.assertEquals("new value", Boolean.TRUE, listen.getNewValue(0));
        
        b.setPermissiveWorking(true);
        Assert.assertEquals("list size still 1",1, listen.getNumPropChanges());
        
        b.setPermissiveWorking(false);
        Assert.assertEquals("+1 property change",2, listen.getNumPropChanges());
        Assert.assertFalse("block not permissive when set", b.getPermissiveWorking());
        Assert.assertEquals("prop ev name", Block.BLOCK_PERMISSIVE_CHANGE, listen.getPropertyName(1));
        Assert.assertEquals("old value", Boolean.TRUE, listen.getOldValue(1));
        Assert.assertEquals("new value", Boolean.FALSE, listen.getNewValue(1));
        
    }
    
    @Test
    public void testSetSpeedPropertyChange() throws JmriException {
        
        Block b = new Block("BlockSystemName");
        Listen listen = new Listen();
        
        String speedA = InstanceManager.getDefault(SignalSpeedMap.class).getValidSpeedNames().firstElement();
        String speedB = InstanceManager.getDefault(SignalSpeedMap.class).getValidSpeedNames().lastElement();
        Assert.assertFalse("Sample speed text needs changing",speedA.equals(speedB));
        
        b.setBlockSpeed(speedA); // set initial test state
        Assert.assertEquals("block speedA set",speedA, b.getBlockSpeed());
        b.addPropertyChangeListener(listen);
        
        b.setBlockSpeed(speedB);
        Assert.assertEquals("1 property change",1, listen.getNumPropChanges());
        Assert.assertEquals("block speedB set",speedB, b.getBlockSpeed());
        Assert.assertEquals("prop ev name",Block.BLOCK_SPEED_CHANGE, listen.getPropertyName(0));
        Assert.assertEquals("old value", speedA, listen.getOldValue(0));
        Assert.assertEquals("new value", speedB, listen.getNewValue(0));
        
        b.setBlockSpeed(speedB);
        Assert.assertEquals("list size still 1",1, listen.getNumPropChanges());
        
        b.setBlockSpeed(speedA);
        Assert.assertEquals("+1 property change",2, listen.getNumPropChanges());
        Assert.assertEquals("block speedA set",speedA, b.getBlockSpeed());
        Assert.assertEquals("prop ev name",Block.BLOCK_SPEED_CHANGE, listen.getPropertyName(1));
        Assert.assertEquals("old value", speedB, listen.getOldValue(1));
        Assert.assertEquals("new value", speedA, listen.getNewValue(1));
        
    }
    
    @Test
    public void testSetCurvaturePropertyChange() throws JmriException {
        
        Block b = new Block("BlockSystemName");
        Listen listen = new Listen();
        
        Assert.assertEquals("no initial curvature",Block.NONE, b.getCurvature());
        b.addPropertyChangeListener(listen);
        
        b.setCurvature(Block.TIGHT);
        Assert.assertEquals("1 property change",1, listen.getNumPropChanges());
        Assert.assertEquals("prop ev name", Block.BLOCK_CURVATURE_CHANGE, listen.getPropertyName(0));
        Assert.assertEquals("old value", Block.NONE, (int)listen.getOldValue(0));
        Assert.assertEquals("new value", Block.TIGHT, (int)listen.getNewValue(0));
        
        b.setCurvature(Block.TIGHT);
        Assert.assertEquals("list size still 1",1, listen.getNumPropChanges());
        
        b.setCurvature(Block.GRADUAL);
        Assert.assertEquals("+1 property change",2, listen.getNumPropChanges());
        Assert.assertEquals("prop ev name",Block.BLOCK_CURVATURE_CHANGE, listen.getPropertyName(1));
        Assert.assertEquals("old value", Block.TIGHT, (int)listen.getOldValue(1));
        Assert.assertEquals("new value", Block.GRADUAL, (int)listen.getNewValue(1));
    }
    
    @Test
    public void testSetGetLengthPropertyChange() throws JmriException {
        
        Block b = new Block("BlockSystemName");
        Listen listen = new Listen();
        
        Assert.assertEquals("no initial length mm",0.0f, b.getLengthMm(), 0);
        Assert.assertEquals("no initial length cm",0.0f, b.getLengthCm(), 0);
        Assert.assertEquals("no initial length in",0.0f, b.getLengthIn(), 0);
        b.addPropertyChangeListener(listen);
        
        b.setLength(47f);
        Assert.assertEquals("1 property change",1, listen.getNumPropChanges());
        Assert.assertEquals("mm set to 47",47.0f, b.getLengthMm(), 0.01);
        Assert.assertEquals("cm set to 4.7",4.7f, b.getLengthCm(), 0.01);
        Assert.assertEquals("Inches set to ",1.850f, b.getLengthIn(), 0.01);
        Assert.assertEquals("prop ev name", Block.BLOCK_LENGTH_CHANGE, listen.getPropertyName(0));
        Assert.assertEquals("old value", 0.0f, (float)listen.getOldValue(0),0.01);
        Assert.assertEquals("new value", 47.0f, (float)listen.getNewValue(0),0.01);
        
        b.setLength(47f);
        Assert.assertEquals("list size still 1",1, listen.getNumPropChanges());
        
        b.setLength(20f);
        Assert.assertEquals("+1 property change",2, listen.getNumPropChanges());
        Assert.assertEquals("mm set to 20",20.0f, b.getLengthMm(), 0.001);
        Assert.assertEquals("cm set to 2.0",2.0f, b.getLengthCm(), 0.001);
        Assert.assertEquals("Inches set to ",0.787f, b.getLengthIn(), 0.001);
        Assert.assertEquals("prop ev name",Block.BLOCK_LENGTH_CHANGE, listen.getPropertyName(1));
        Assert.assertEquals("old value", 47.0f, (float)listen.getOldValue(1),0.01);
        Assert.assertEquals("new value", 20.0f, (float)listen.getNewValue(1),0.01);
        
    }
    
    /**
     * Class to log Property Changes.
     */
    private static class Listen implements PropertyChangeListener {
        
        List<PropertyChangeEvent> propChangeNames = Collections.synchronizedList(new ArrayList<>());
        
        @Override
        public void propertyChange(PropertyChangeEvent e) {
            propChangeNames.add(e);
        }
        
        public int getNumPropChanges(){
            return propChangeNames.size();
        }
        
        public String getPropertyName(int index){
            return propChangeNames.get(index).getPropertyName();
        }
        
        public Object getOldValue(int index){
            return propChangeNames.get(index).getOldValue();
        }
        
        public Object getNewValue(int index){
            return propChangeNames.get(index).getNewValue();
        }
        
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
