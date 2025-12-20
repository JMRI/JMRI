package jmri;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.util.Collections;
import java.util.ArrayList;
import java.util.List;

import jmri.implementation.SignalSpeedMap;
import jmri.jmrix.internal.InternalSensorManager;
import jmri.jmrix.internal.InternalSystemConnectionMemo;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        assertNotNull( b, "Block Created");
    }

    @Test
    @SuppressWarnings({"unlikely-arg-type", "ObjectEqualsNull", "IncompatibleEquals"}) // String / StringBuffer seems to be unrelated to Block
    public void testEquals() {
        Block b1 = new Block("SystemName1");
        Block b2 = new Block("SystemName2");
        
        //multiple Block objects with same SystemName are really the same
        Block b1a = new Block("SystemName1");
        
        assertTrue(b1.equals(b1)); // identity
        assertFalse(b1.equals(b2)); // blocks are named objects

        assertTrue(b1a.equals(b1));
        assertTrue(b1.equals(b1a)); // commutes
        
        // check null
        assertFalse(b1.equals(null));

        // check another type
        assertFalse(b1.equals(new StringBuffer("foo")));
        assertFalse(b1.equals("foo"));
    }

    @Test
    public void testBlockHashCode() {
        Block b1 = new Block("SystemName1");
        
        //multiple Block objects with same SystemName are really the same
        Block b1a = new Block("SystemName1");
        
        assertTrue(b1.hashCode() == b1a.hashCode());

        b1a.setLength(120);
        b1a.setCurvature(21);
        assertTrue(b1.hashCode() == b1a.hashCode());
    }
    
    @Test
    public void testSensorAdd() {
        Block b = new Block("SystemName");
        b.setSensor("IS12");
    }

    private int count;

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
        assertEquals( 1, count, "count of detected changes");
    }

    @Test
    public void testValueField() {
        Block b = new Block("SystemName");
        b.setValue("string");
        assertEquals( "string", b.getValue(), "Returned Object matches");
    }

    @Test
    public void testSensorSequence() throws JmriException {
        SensorManager sm = new InternalSensorManager(InstanceManager.getDefault(InternalSystemConnectionMemo.class));
        count = 0;
        Block b = new Block("SystemName");
        Sensor s = sm.provideSensor("IS12");
        s.setState(Sensor.UNKNOWN);

        assertEquals( Block.UNDETECTED, b.getState(), "Initial state"); // state until sensor is set

        b.setSensor("IS12");
        s.setState(Sensor.ACTIVE);
        assertEquals( Block.OCCUPIED, s.getState(), "State with sensor active");
        s.setState(Sensor.INACTIVE);
        assertEquals( Block.UNOCCUPIED, s.getState(), "State with sensor inactive");
    }

    @Test
    public void testCoding() {
        assertTrue( Block.OCCUPIED != Block.UNOCCUPIED, "Block.OCCUPIED != Block.UNOCCUPIED");
        assertTrue( Block.OCCUPIED != Block.UNDETECTED, "Block.OCCUPIED != Block.UNDETECTED");
        assertTrue( Block.OCCUPIED != Block.UNKNOWN, "Block.OCCUPIED != Block.UNKNOWN");
        assertTrue( Block.OCCUPIED != Block.INCONSISTENT, "Block.OCCUPIED != Block.INCONSISTENT");
        assertTrue( Block.UNOCCUPIED != Block.UNDETECTED, "Block.UNOCCUPIED != Block.UNDETECTED");
        assertTrue( Block.UNOCCUPIED != Block.UNKNOWN, "Block.UNOCCUPIED != Block.UNKNOWN");
        assertTrue( Block.UNOCCUPIED != Block.INCONSISTENT, "Block.UNOCCUPIED != Block.INCONSISTENT");
        assertTrue( Block.UNDETECTED != Block.UNKNOWN, "Block.UNDETECTED != Block.UNKNOWN");
        assertTrue( Block.UNDETECTED != Block.INCONSISTENT, "Block.UNDETECTED != Block.INCONSISTENT");
        assertTrue( Block.UNKNOWN != Block.INCONSISTENT, "Block.UNKNOWN != Block.INCONSISTENT");
    }

    // test going active with only one neighbor
    @Test
    public void testFirstGoActive() throws JmriException {
        SensorManager sm = new InternalSensorManager(InstanceManager.getDefault(InternalSystemConnectionMemo.class));

        Block b1 = new Block("SystemName1");

        Block b2 = new Block("SystemName2");
        Sensor s2 = sm.provideSensor("IS2");
        b2.setNamedSensor(InstanceManager.getDefault(NamedBeanHandleManager.class).getNamedBeanHandle("IS2", s2));
        s2.setState(Sensor.ACTIVE);
        b2.setValue("b2 contents");

        Path p = new Path();
        p.setBlock(b2);

        b1.addPath(p);

        // actual test
        b1.goingActive();
        assertEquals( "b2 contents", b1.getValue(), "Value transferred");
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
        b1.setNamedSensor(InstanceManager.getDefault(NamedBeanHandleManager.class).getNamedBeanHandle("IS1", s1));
        s1.setState(Sensor.ACTIVE);
        b1.setValue("b1 contents");

        Sensor s2 = sm.provideSensor("IS2");
        b2.setNamedSensor(InstanceManager.getDefault(NamedBeanHandleManager.class).getNamedBeanHandle("IS2", s2));
        s2.setState(Sensor.INACTIVE);

        Sensor s3 = sm.provideSensor("IS3");
        b3.setNamedSensor(InstanceManager.getDefault(NamedBeanHandleManager.class).getNamedBeanHandle("IS3", s3));
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
        assertEquals( Block.OCCUPIED, b2.getState(), "State");
        assertEquals( "b1 contents", b2.getValue(), "Value transferred");
        assertEquals( Path.RIGHT, b2.getDirection(), "Direction");

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
        b1.setNamedSensor(InstanceManager.getDefault(NamedBeanHandleManager.class).getNamedBeanHandle("IS1", s1));
        s1.setState(Sensor.ACTIVE);
        b1.setValue("b1 contents");
        b1.setDirection(Path.RIGHT);

        Sensor s2 = sm.provideSensor("IS2");
        b2.setNamedSensor(InstanceManager.getDefault(NamedBeanHandleManager.class).getNamedBeanHandle("IS2", s2));
        s2.setState(Sensor.INACTIVE);

        Sensor s3 = sm.provideSensor("IS3");
        b3.setNamedSensor(InstanceManager.getDefault(NamedBeanHandleManager.class).getNamedBeanHandle("IS3", s3));
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
        assertEquals( Block.OCCUPIED, b2.getState(), "State");
        assertEquals( "b1 contents", b2.getValue(), "Value transferred");
        assertEquals( Path.RIGHT, b2.getDirection(), "Direction");

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
        b1.setNamedSensor(InstanceManager.getDefault(NamedBeanHandleManager.class).getNamedBeanHandle("IS1", s1));
        s1.setState(Sensor.ACTIVE);
        b1.setValue("b1 contents");
        b1.setDirection(Path.NORTH_WEST); //combination direction

        Sensor s2 = sm.provideSensor("IS2");
        b2.setNamedSensor(InstanceManager.getDefault(NamedBeanHandleManager.class).getNamedBeanHandle("IS2", s2));
        s2.setState(Sensor.INACTIVE);

        Sensor s3 = sm.provideSensor("IS3");
        b3.setNamedSensor(InstanceManager.getDefault(NamedBeanHandleManager.class).getNamedBeanHandle("IS3", s3));
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
        assertEquals( Block.OCCUPIED, b2.getState(), "State");
        assertEquals( "b1 contents", b2.getValue(), "Value transferred");
        assertEquals( Path.NORTH, b2.getDirection(), "Direction");

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
        assertEquals( 2, count, "count of detected changes");
    }

    @Test
    public void testReporterInvokeCurrent() {
        ReporterManager rm = InstanceManager.getDefault(ReporterManager.class);
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
        assertEquals( 1, count, "count of detected changes");

        rm.provideReporter("IR22").setReport(null);
        // Current report should change
        assertEquals( 2, count, "count of detected changes");
    }

    @Test
    public void testReporterInvokeLast() {
        ReporterManager rm = InstanceManager.getDefault(ReporterManager.class);
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
        assertEquals( 1, count, "count of detected changes");

        rm.provideReporter("IR22").setReport(null);
        // Last report should not change
        assertEquals( 1, count, "count of detected changes");
    }

    @Test
    public void testGetLocoAddress(){
        Block b = new Block("SystemName");
        assertEquals( new DccLocoAddress(1234,LocoAddress.Protocol.DCC), b.getLocoAddress("1234 enter"),
            "address");
    }

    @Test
    public void testGetLocoDirection(){
        Block b = new Block("SystemName");
        assertEquals( jmri.PhysicalLocationReporter.Direction.ENTER, b.getDirection("1234 enter"),
            "direction");
    }

    @Test
    public void testAddRemoveListener() throws JmriException {
        
        Block b = new Block("BlockSystemName");
        b.setUserName("Start user id");
        Listen listen = new Listen();
        assertEquals( 0, b.getNumPropertyChangeListeners(), "no listener at start of test");
        
        b.addPropertyChangeListener(listen);
        assertEquals( 1, b.getNumPropertyChangeListeners(), "1 listener added");
        
        b.setUserName("user id");
        assertEquals( "UserName", listen.getPropertyName(0), "prop ev name");
        assertEquals( "Start user id", listen.getOldValue(0), "old value");
        assertEquals( "user id", listen.getNewValue(0), "new value");

        b.removePropertyChangeListener(listen);
        assertEquals( 0, b.getNumPropertyChangeListeners(), "listener removed");

        b.setUserName("Changed user id");       
        assertEquals( 1, listen.getNumPropChanges(), "list size still 1");
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
        assertEquals( Block.UNDETECTED, b.getState(), "Block starts state undetected");
        assertNull( b.getSensor(), "Block starts no sensor");
        Listen listen = new Listen();
        b.addPropertyChangeListener(listen);
        
        b.setSensor(null);
        assertEquals( 0, listen.getNumPropChanges(), "no prop change null to null");

        b.setSensor("");
        assertEquals( 0, listen.getNumPropChanges(), "no prop change null to empty");

        b.setSensor("ISunknownState");
        assertEquals( 2, listen.getNumPropChanges(), "list size +2, state change, occ sense change");
        assertEquals( "state", listen.getPropertyName(0), "prop ev name");
        assertEquals(Block.UNDETECTED, (int)listen.getOldValue(0), "old value");
        assertEquals( Block.UNKNOWN, (int)listen.getNewValue(0), "new value");

        assertEquals( Block.OCC_SENSOR_CHANGE, listen.getPropertyName(1), "prop ev name");
        assertNull( listen.getOldValue(1), "old value");
        assertEquals( sensorUnknownState, listen.getNewValue(1), "new value");

        b.setSensor("ISunknownState");
        assertEquals( 2, listen.getNumPropChanges(), "same sensor, no prop change");

        b.setSensor("ISInactiveState");
        assertEquals( 4, listen.getNumPropChanges(), "list size +2, state change, occ sense change");
        assertEquals( "state", listen.getPropertyName(2), "prop ev name");
        assertEquals( Block.UNKNOWN, (int)listen.getOldValue(2), "old value");
        assertEquals( Block.UNOCCUPIED, (int)listen.getNewValue(2), "new value");

        assertEquals( Block.OCC_SENSOR_CHANGE, listen.getPropertyName(3), "prop ev name");
        assertEquals( sensorUnknownState, listen.getOldValue(3), "old value");
        assertEquals( sensorInactiveState, listen.getNewValue(3), "new value");

        b.setSensor(null);
        assertEquals( 6, listen.getNumPropChanges(), "list size +2, state change, occ sense change");
        assertEquals( "state", listen.getPropertyName(4), "prop ev name");
        assertEquals( Block.UNOCCUPIED, (int)listen.getOldValue(4), "old value");
        assertEquals( Block.UNDETECTED, (int)listen.getNewValue(4), "setSensor sets status unoccupied to undetected");

        assertEquals( Block.OCC_SENSOR_CHANGE, listen.getPropertyName(5), "prop ev name");
        assertEquals( sensorInactiveState, listen.getOldValue(5), "old value");
        assertNull( listen.getNewValue(5), "new value");
        
    }
    
    @Test
    public void testAddReporterPropertyChange() throws JmriException {
        
        ReporterManager rm = InstanceManager.getDefault(ReporterManager.class);
        Reporter rep = rm.provide("IR123");
        
        Block b = new Block("BlockSystemName");
        Listen listen = new Listen();
        b.addPropertyChangeListener(listen);
        
        b.setReporter(rep);
        assertEquals( 1, listen.getNumPropChanges(), "list size 1, reporter change");
        assertEquals( Block.BLOCK_REPORTER_CHANGE, listen.getPropertyName(0), "prop ev name");
        assertNull( listen.getOldValue(0), "old value");
        assertEquals( rep, listen.getNewValue(0), "new value");

        b.setReporter(rep);
        assertEquals( 1, listen.getNumPropChanges(), "list size still 1");
        
        b.setReporter(null);
        assertEquals( 2, listen.getNumPropChanges(), "+1 property change");
        assertEquals( Block.BLOCK_REPORTER_CHANGE, listen.getPropertyName(1), "prop ev name");
        assertEquals( rep, listen.getOldValue(1), "old value");
        assertNull( listen.getNewValue(1), "new value");
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
        assertEquals( 1, listen.getNumPropChanges(), "list size 1, property change");
        assertEquals( Block.BLOCK_REPORTING_CURRENT, listen.getPropertyName(0), "prop ev name");
        assertEquals( Boolean.FALSE, listen.getOldValue(0), "old value");
        assertEquals( Boolean.TRUE, listen.getNewValue(0), "new value");

        b.setReportingCurrent(true);
        assertEquals( 1, listen.getNumPropChanges(), "list size still 1");

        b.setReportingCurrent(false);
        assertEquals( 2, listen.getNumPropChanges(), "+1 property change");
        assertEquals( Block.BLOCK_REPORTING_CURRENT, listen.getPropertyName(1), "prop ev name");
        assertEquals( Boolean.TRUE, listen.getOldValue(1), "old value");
        assertEquals( Boolean.FALSE, listen.getNewValue(1), "new value");
        
    }
    
    @Test
    public void testSetStatePropertyChangeName() throws JmriException {
        
        Block b = new Block("BlockSystemName");
        Listen listen = new Listen();
        
        b.setState(Block.UNDETECTED); // set initial test state
        b.addPropertyChangeListener(listen);

        b.setState(Block.UNOCCUPIED);
        assertEquals( 1, listen.getNumPropChanges(), "1 property change");
        assertEquals( "state", listen.getPropertyName(0), "prop ev name");
        assertEquals( Block.UNOCCUPIED, (int)listen.getNewValue(0), "new value");

    }
    
    @Test
    public void testSetValuePropertyChange() throws JmriException {
        
        Block b = new Block("BlockSystemName");
        Listen listen = new Listen();
        
        b.setValue(null); // set initial test state
        b.addPropertyChangeListener(listen);
        
        b.setValue("String Block Value");
        assertEquals( 1, listen.getNumPropChanges(), "1 property change");
        assertEquals( "value", listen.getPropertyName(0), "prop ev name");
        assertNull( listen.getOldValue(0), "old value");
        assertEquals( "String Block Value", listen.getNewValue(0), "new value");

        b.setValue("String Block Value");
        assertEquals( 1, listen.getNumPropChanges(), "list size still 1");

        b.setValue("New Block Value");
        assertEquals( 2, listen.getNumPropChanges(), "+1 property change");
        assertEquals( "value", listen.getPropertyName(1), "prop ev name");
        assertEquals( "String Block Value", listen.getOldValue(1), "old value");
        assertEquals( "New Block Value", listen.getNewValue(1), "new value");

        b.setValue(null);
        assertEquals( 3, listen.getNumPropChanges(), "+1 property change");
        assertEquals( "value", listen.getPropertyName(2), "prop ev name");
        assertEquals( "New Block Value", listen.getOldValue(2), "old value");
        assertNull( listen.getNewValue(2), "new value");

    }

    @Test
    public void testSetDirectionPropertyChange() throws JmriException {
        
        Block b = new Block("BlockSystemName");
        Listen listen = new Listen();
        
        b.setDirection(Path.NORTH); // set initial test state
        b.addPropertyChangeListener(listen);
        
        b.setDirection(Path.EAST);
        assertEquals( 1, listen.getNumPropChanges(), "1 property change");
        assertEquals( Path.EAST, b.getDirection(), "Direction set");
        assertEquals( "direction", listen.getPropertyName(0), "prop ev name");
        assertEquals( Path.NORTH, (int)listen.getOldValue(0), "old value");
        assertEquals( Path.EAST, (int)listen.getNewValue(0), "new value");

        b.setDirection(Path.EAST);
        assertEquals( 1, listen.getNumPropChanges(), "list size still 1");

        b.setDirection(Path.WEST);
        assertEquals( 2, listen.getNumPropChanges(), "+1 property change");
        assertEquals( Path.WEST, b.getDirection(), "Direction set");
        assertEquals( "direction", listen.getPropertyName(1), "prop ev name");
        assertEquals( Path.EAST, (int)listen.getOldValue(1), "old value");
        assertEquals( Path.WEST, (int)listen.getNewValue(1), "new value");
        
    }
    
    @Test
    public void testSetGetPermissiveWorkingPropertyChange() throws JmriException {
        
        Block b = new Block("BlockSystemName");
        Listen listen = new Listen();
        
        assertFalse( b.getPermissiveWorking(), "block not permissive to start");
        b.addPropertyChangeListener(listen);
        
        b.setPermissiveWorking(true);
        assertEquals( 1, listen.getNumPropChanges(), "1 property change");
        assertTrue( b.getPermissiveWorking(), "block permissive set");
        assertEquals( Block.BLOCK_PERMISSIVE_CHANGE, listen.getPropertyName(0), "prop ev name");
        assertEquals( Boolean.FALSE, listen.getOldValue(0), "old value");
        assertEquals( Boolean.TRUE, listen.getNewValue(0), "new value");

        b.setPermissiveWorking(true);
        assertEquals( 1, listen.getNumPropChanges(), "list size still 1");

        b.setPermissiveWorking(false);
        assertEquals( 2, listen.getNumPropChanges(), "+1 property change");
        assertFalse( b.getPermissiveWorking(), "block not permissive when set");
        assertEquals( Block.BLOCK_PERMISSIVE_CHANGE, listen.getPropertyName(1), "prop ev name");
        assertEquals( Boolean.TRUE, listen.getOldValue(1), "old value");
        assertEquals( Boolean.FALSE, listen.getNewValue(1), "new value");

    }

    @Test
    public void testSetIsGhostPropertyChange() throws JmriException {

        Block b = new Block("BlockSystemName");
        Listen listen = new Listen();

        assertFalse( b.getIsGhost(), "block not a ghost to start");
        b.addPropertyChangeListener(listen);

        b.setIsGhost(true);
        assertEquals( 1, listen.getNumPropChanges(), "1 property change");
        assertTrue( b.getIsGhost(), "block permissive set");
        assertEquals( Block.GHOST_CHANGE, listen.getPropertyName(0), "prop ev name");
        assertEquals( Boolean.FALSE, listen.getOldValue(0), "old value");
        assertEquals( Boolean.TRUE, listen.getNewValue(0), "new value");

        b.setIsGhost(true);
        assertEquals( 1, listen.getNumPropChanges(), "list size still 1");

        b.setIsGhost(false);
        assertEquals( 2, listen.getNumPropChanges(), "+1 property change");
        assertFalse( b.getIsGhost(), "block not permissive when set");
        assertEquals( Block.GHOST_CHANGE, listen.getPropertyName(1), "prop ev name");
        assertEquals( Boolean.TRUE, listen.getOldValue(1), "old value");
        assertEquals( Boolean.FALSE, listen.getNewValue(1), "new value");

    }

    @Test
    public void testSetSpeedPropertyChange() throws JmriException {
        
        Block b = new Block("BlockSystemName");
        Listen listen = new Listen();
        
        String speedA = InstanceManager.getDefault(SignalSpeedMap.class).getValidSpeedNames().firstElement();
        String speedB = InstanceManager.getDefault(SignalSpeedMap.class).getValidSpeedNames().lastElement();
        assertFalse( speedA.equals(speedB), "Sample speed text needs changing");

        b.setBlockSpeed(speedA); // set initial test state
        assertEquals( speedA, b.getBlockSpeed(), "block speedA set");
        b.addPropertyChangeListener(listen);

        b.setBlockSpeed(speedB);
        assertEquals( 1, listen.getNumPropChanges(), "1 property change");
        assertEquals( speedB, b.getBlockSpeed(), "block speedB set");
        assertEquals( Block.BLOCK_SPEED_CHANGE, listen.getPropertyName(0), "prop ev name");
        assertEquals( speedA, listen.getOldValue(0), "old value");
        assertEquals( speedB, listen.getNewValue(0), "new value");

        b.setBlockSpeed(speedB);
        assertEquals( 1, listen.getNumPropChanges(), "list size still 1");
        
        b.setBlockSpeed(speedA);
        assertEquals( 2, listen.getNumPropChanges(), "+1 property change");
        assertEquals( speedA, b.getBlockSpeed(), "block speedA set");
        assertEquals( Block.BLOCK_SPEED_CHANGE, listen.getPropertyName(1), "prop ev name");
        assertEquals( speedB, listen.getOldValue(1), "old value");
        assertEquals( speedA, listen.getNewValue(1), "new value");

    }

    @Test
    public void testSetCurvaturePropertyChange() throws JmriException {
        
        Block b = new Block("BlockSystemName");
        Listen listen = new Listen();
        
        assertEquals( Block.NONE, b.getCurvature(), "no initial curvature");
        b.addPropertyChangeListener(listen);

        b.setCurvature(Block.TIGHT);
        assertEquals( 1, listen.getNumPropChanges(), "1 property change");
        assertEquals( Block.BLOCK_CURVATURE_CHANGE, listen.getPropertyName(0), "prop ev name");
        assertEquals( Block.NONE, (int)listen.getOldValue(0), "old value");
        assertEquals( Block.TIGHT, (int)listen.getNewValue(0), "new value");

        b.setCurvature(Block.TIGHT);
        assertEquals( 1, listen.getNumPropChanges(), "list size still 1");

        b.setCurvature(Block.GRADUAL);
        assertEquals( 2, listen.getNumPropChanges(), "+1 property change");
        assertEquals( Block.BLOCK_CURVATURE_CHANGE, listen.getPropertyName(1), "prop ev name");
        assertEquals( Block.TIGHT, (int)listen.getOldValue(1), "old value");
        assertEquals( Block.GRADUAL, (int)listen.getNewValue(1), "new value");
    }

    @Test
    public void testSetGetLengthPropertyChange() throws JmriException {
        
        Block b = new Block("BlockSystemName");
        Listen listen = new Listen();
        
        assertEquals( 0.0f, b.getLengthMm(), 0, "no initial length mm");
        assertEquals( 0.0f, b.getLengthCm(), 0, "no initial length cm");
        assertEquals( 0.0f, b.getLengthIn(), 0, "no initial length in");
        b.addPropertyChangeListener(listen);

        b.setLength(47f);
        assertEquals( 1, listen.getNumPropChanges(), "1 property change");
        assertEquals( 47.0f, b.getLengthMm(), 0.01, "mm set to 47");
        assertEquals( 4.7f, b.getLengthCm(), 0.01, "cm set to 4.7");
        assertEquals( 1.850f, b.getLengthIn(), 0.01, "Inches set to ");
        assertEquals( Block.BLOCK_LENGTH_CHANGE, listen.getPropertyName(0), "prop ev name");
        assertEquals( 0.0f, (float)listen.getOldValue(0),0.01, "old value");
        assertEquals( 47.0f, (float)listen.getNewValue(0),0.01, "new value");

        b.setLength(47f);
        assertEquals( 1, listen.getNumPropChanges(), "list size still 1");
        
        b.setLength(20f);
        assertEquals( 2, listen.getNumPropChanges(), "+1 property change");
        assertEquals( 20.0f, b.getLengthMm(), 0.001, "mm set to 20");
        assertEquals( 2.0f, b.getLengthCm(), 0.001, "cm set to 2.0");
        assertEquals( 0.787f, b.getLengthIn(), 0.001, "Inches set to ");
        assertEquals( Block.BLOCK_LENGTH_CHANGE, listen.getPropertyName(1), "prop ev name");
        assertEquals( 47.0f, (float)listen.getOldValue(1),0.01, "old value");
        assertEquals( 20.0f, (float)listen.getNewValue(1),0.01, "new value");

    }

    @Test
    public void testDescribeState() {
        Block t = new Block("testDescribeState");
        assertEquals("Unknown", t.describeState(Block.UNKNOWN));
        assertEquals("Inconsistent", t.describeState(Block.INCONSISTENT));
        assertEquals("Occupied", t.describeState(Block.OCCUPIED));
        assertEquals("UnOccupied", t.describeState(Block.UNOCCUPIED));
        assertEquals("Undetected", t.describeState(Block.UNDETECTED));
        assertEquals("Unexpected value: 777", t.describeState(777));
        t.dispose();
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
