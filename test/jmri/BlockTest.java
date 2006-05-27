// BlockTest.java

package jmri;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the Block class
 * @author	Bob Jacobsen  Copyright (C) 2006
 * @version $Revision: 1.1 $
 */
public class BlockTest extends TestCase {

    /**
     * Normally, users create Block objects via a manager, 
     * but we test the direct create here.  If it works, we can 
     * use it for testing.
     */
	public void testDirectCreate() {
	    new Block("SystemName");
	}

	public void testSensorAdd() {
	    SensorManager sm = new jmri.managers.InternalSensorManager();
	    Block b = new Block("SystemName");
	    b.setSensor(sm.provideSensor("IS12"));
	}

    static int count; 
    
	public void testSensorInvoke() throws JmriException {
	    SensorManager sm = new jmri.managers.InternalSensorManager();
	    count = 0;
	    Block b = new Block("SystemName"){
            void handleSensorChange(java.beans.PropertyChangeEvent e) {
                count++;
            }
	    };
	    b.setSensor(sm.provideSensor("IS12"));
	    sm.provideSensor("IS12").setState(jmri.Sensor.ACTIVE);
	    Assert.assertEquals("count of detected changes", 1, count);
	}


	public void testValueField() {
	    Block b = new Block("SystemName");
	    b.setValue("string");
	    Assert.assertEquals("Returned Object matches", "string", b.getValue());
	}

	public void testSensorSequence() throws JmriException {
	    SensorManager sm = new jmri.managers.InternalSensorManager();
	    count = 0;
	    Block b = new Block("SystemName");
	    Sensor s = sm.provideSensor("IS12");
	    Assert.assertEquals("Initial state", Block.UNKNOWN, s.getState());
	    b.setSensor(s);
	    s.setState(jmri.Sensor.ACTIVE);
	    Assert.assertEquals("State with sensor active", Block.OCCUPIED, s.getState());
	    s.setState(jmri.Sensor.INACTIVE);
	    Assert.assertEquals("State with sensor inactive", Block.UNOCCUPIED, s.getState());
	}


    // test going active with only one neighbor
	public void testFirstGoActive() throws JmriException {
	    SensorManager sm = new jmri.managers.InternalSensorManager();

	    Block b1 = new Block("SystemName1");

	    Block b2 = new Block("SystemName2");
        Sensor s2 = sm.provideSensor("IS2");
        b2.setSensor(s2);
        s2.setState(Sensor.ACTIVE);
        b2.setValue("b2 contents");
        
        Path p = new Path();
        p.setBlock(b2);
        
        b1.addPath(p);
        
        // actual test
        b1.goingActive();
	    Assert.assertEquals("Value transferred", "b2 contents", b1.getValue());
	}
	
    
	// from here down is testing infrastructure

	public BlockTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {BlockTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(BlockTest.class);
		return suite;
	}

}
