package jmri.managers;

import jmri.*;
import org.junit.Test;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

/**
 * Overall tests of Logix operation, including operation of 
 * conditionals.  To ease setup, reads from XML files.
 *
 * @author	Bob Jacobsen Copyright (C) 2015
 */
public class LogixSystemTest {

    /** 
     * Test of inter-Logix references for Conditionals
     * 
     * Creates a sensor, which is watched by a Conditional in Logix 1, 
     * which in turn is watched by two Conditionals in Logix 2 
     *     (once by system name and once by user name), 
     * which in turn each toggle one of two internal Turnouts on changes
     * 
     * @throws jmri.JmriException if unable to load test XML
     */
    @Test
    public void testLogixReferenceSetup() throws jmri.JmriException {        

        // load and activate sample file
        java.io.File f = new java.io.File("java/test/jmri/managers/LogixSystemTestConditionaReferenceCheck.xml");
        jmri.configurexml.ConfigXmlManager cm = new jmri.configurexml.ConfigXmlManager() {};
        cm.load(f);
        InstanceManager.getDefault(jmri.LogixManager.class).activateAllLogixs();
        
        // get references, in process checking load
        Sensor is1 = InstanceManager.getDefault(jmri.SensorManager.class).getSensor("IS1");
        Turnout it1 = InstanceManager.getDefault(jmri.TurnoutManager.class).getTurnout("IT1");
        Turnout it2 = InstanceManager.getDefault(jmri.TurnoutManager.class).getTurnout("IT2");
        Assert.assertNotNull(is1);
        Assert.assertNotNull(it1);
        Assert.assertNotNull(it2);
        
        // remember startup state for Turnouts, so can detect change
        int oldIt1 = it1.getState();
        int oldIt2 = it2.getState();
        
        // drive the sensor change that should cause the Conditionals to change
        is1.setState(jmri.Sensor.ACTIVE);
        
        // check for propagation (maybe needs a wait someday?)
        Assert.assertTrue("IT1", oldIt1 != it1.getState());
        Assert.assertTrue("IT2", oldIt2 != it2.getState());
    }

    @Before
    public void setUp() throws Exception {
        jmri.util.JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initInternalTurnoutManager();
        jmri.util.JUnitUtil.initInternalLightManager();
        jmri.util.JUnitUtil.initInternalSensorManager();
        jmri.util.JUnitUtil.initIdTagManager();
    }

    @After
    public void tearDown() throws Exception {
        jmri.util.JUnitUtil.tearDown();
    }
}
