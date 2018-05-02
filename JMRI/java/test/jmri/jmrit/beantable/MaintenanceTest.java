package jmri.jmrit.beantable;

import jmri.*;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class MaintenanceTest {

    @Test
    public void testCTor() {
        Maintenance t = new Maintenance();
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testGetTypeAndNames() {
        String[] result;
        // nothing present
        result = Maintenance.getTypeAndNames("foo");
        checkReturnString(result, "", "foo", "foo", "0");
        
        // hit on sensor via system name
        InstanceManager.getDefault(SensorManager.class).provideSensor("IS1");
        result = Maintenance.getTypeAndNames("IS1");
        checkReturnString(result, "Sensor", null, "IS1", "1");  //num listeners is empirical

        // hit on sensor via system name with user name
        InstanceManager.getDefault(SensorManager.class).provideSensor("IS2").setUserName("foo");
        result = Maintenance.getTypeAndNames("IS2");
        checkReturnString(result, "Sensor", "foo", "IS2", "1");  //num listeners is empirical

        // hit on sensor via user name
        InstanceManager.getDefault(SensorManager.class).provideSensor("IS3").setUserName("bar");
        result = Maintenance.getTypeAndNames("bar");
        checkReturnString(result, "Sensor", "bar", "IS3", "1");  //num listeners is empirical

        // hit on turnout via system name
        InstanceManager.getDefault(TurnoutManager.class).provideTurnout("IT1");
        result = Maintenance.getTypeAndNames("IT1");
        checkReturnString(result, "Turnout", null, "IT1", "1");  //num listeners is empirical

        // hit on sensor via system name with user name
        InstanceManager.getDefault(TurnoutManager.class).provideTurnout("IT3").setUserName("bar");
        result = Maintenance.getTypeAndNames("IT3");
        checkReturnString(result, "Turnout", "bar", "IT3", "1");  //num listeners is empirical

        // hit sensors before turnouts with same user name
        result = Maintenance.getTypeAndNames("bar");
        checkReturnString(result, "Sensor", "bar", "IS3", "1");  //num listeners is empirical   
    }

    @Test
    public void testGetTypeAndNamesObsoleteCase() {
        // This is checking the obsolete cases where UPPER CASE names are forced
        
        String[] result;
        
        // hit on sensor via to-capital system name
        InstanceManager.getDefault(SensorManager.class).provideSensor("IS1");
        result = Maintenance.getTypeAndNames("is1");
        checkReturnString(result, "Sensor", null, "IS1", "1");  //num listeners is empirical
    }

    void checkReturnString(String[] result, String compare, String username, String systemname, String listeners) {
        Assert.assertNotNull(result);
        Assert.assertEquals("Type", compare, result[0]);
        Assert.assertEquals("UserName", username, result[1]);
        Assert.assertEquals("SystemName", systemname, result[2]);
        Assert.assertEquals("Listeners", listeners, result[3]);
    }
    
    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalSensorManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(MaintenanceTest.class);

}
