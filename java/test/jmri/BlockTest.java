package jmri;

import jmri.jmrix.internal.InternalSensorManager;
import jmri.jmrix.internal.InternalSystemConnectionMemo;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the Block class
 *
 * @author	Bob Jacobsen Copyright (C) 2006
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


    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
