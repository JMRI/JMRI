// SignalSpeedMapTest.java
package jmri.implementation;

import java.util.Calendar;
import java.util.Date;
import jmri.IdTag;
import jmri.Reporter;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.jdom2.*;

/**
 * Tests for the SignalSpeedMap class
 *
 * @author Bob Jacobsen Copyright (C) 2015
 * @version $Revision$
 */
public class SignalSpeedMapTest extends TestCase {

    public void testLoadMap() {
        Assert.assertNotNull(SignalSpeedMap.getMap());
    }
    
    static final String[] speeds = new String[]{
        "Cab",
        "Maximum",
        "Normal",
        "Sixty",
        "Fifty",
        "Limited",
        "Medium",
        "Slow",
        "Restricted",
        "RestrictedSlow",
        "Stop"
    };

    public void testMapMonoticity() {
        SignalSpeedMap m = SignalSpeedMap.getMap();
        
        // check for monotonic values
        for (int i = 0; i < speeds.length-1; i++) {
            Assert.assertTrue(speeds[i+1]+" ("+m.getSpeed(speeds[i+1])+") must be less than "+speeds[i]+" ("+m.getSpeed(speeds[i])+")", 
                m.getSpeed(speeds[i+1])<m.getSpeed(speeds[i]));
        }
         
    }

    String getFilename() {
        return "xml/signals/signalSpeeds.xml";
    }
    
    Element getSignalSpeedsXml() {
        return null;
    }
    
    public void testAllSpeedsPresent() {
    }
    
    public void testNoExtraSpeedsPresent() {
    }

    public void testAppearanceSpeedsOK() {
        SignalSpeedMap m = SignalSpeedMap.getMap();
        // check that every speed in <appearanceSpeeds> is defined
        java.util.Enumeration<String> e = m.getAppearanceIterator();
        String name;
        while (e.hasMoreElements()) {
            name = e.nextElement();
            Assert.assertNotNull("appearanceSpeed \""+name+"\" is defined", m.getAppearanceSpeed(name));
            Assert.assertTrue("appearanceSpeed \""+name+"\" has value", m.getSpeed(m.getAppearanceSpeed(name)) >= 0.);
        }        
    }

    // from here down is testing infrastructure
    public SignalSpeedMapTest(String s) {
        super(s);
    }

    // The minimal setup for log4J
    @Override
    protected void setUp() throws Exception {
        apps.tests.Log4JFixture.setUp();
        super.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initInternalTurnoutManager();
        jmri.util.JUnitUtil.initInternalLightManager();
        jmri.util.JUnitUtil.initInternalSensorManager();
        jmri.util.JUnitUtil.initIdTagManager();
    }

    @Override
    protected void tearDown() throws Exception {
        jmri.util.JUnitUtil.resetInstanceManager();
        super.tearDown();
        apps.tests.Log4JFixture.tearDown();
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {SignalSpeedMapTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(SignalSpeedMapTest.class);
        return suite;
    }

}
