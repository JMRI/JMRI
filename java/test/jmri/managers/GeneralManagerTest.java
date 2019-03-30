package jmri.managers;

import jmri.DigitalIO;
import jmri.DigitalIOManager;
import jmri.InstanceManager;
import jmri.Light;
import jmri.LightManager;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.Turnout;
import jmri.TurnoutManager;
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

    @Test
    public void testDigitalIO() {
        DigitalIO d;
        
        d = InstanceManager.getDefault(DigitalIOManager.class).getNamedBean("IT1");
        Assert.assertNotNull("turnout exists", d);
        Assert.assertTrue("bean is a turnout", d instanceof Turnout);
        
        d = InstanceManager.getDefault(TurnoutManager.class).provide("IT3");
        Assert.assertNotNull("turnout exists", d);
        Assert.assertTrue("bean is a turnout", d instanceof Turnout);
        
        d = InstanceManager.getDefault(SensorManager.class).provide("IS1");
        System.out.format("Bean: %s, %s%n", d.getSystemName(), d.getClass().getName());
        Assert.assertNotNull("sensor exists", d);
        Assert.assertTrue("bean is a sensor", d instanceof Sensor);
        
        d = InstanceManager.getDefault(LightManager.class).provide("IL1");
        Assert.assertNotNull("light exists", d);
        Assert.assertTrue("bean is a light", d instanceof Light);
    }
    
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initConnectionConfigManager();
        
        InstanceManager.getDefault(TurnoutManager.class).provide("IT1");
        InstanceManager.getDefault(TurnoutManager.class).provide("IT2");
        InstanceManager.getDefault(TurnoutManager.class).provide("IT3");
        InstanceManager.getDefault(SensorManager.class).provide("IS1");
        InstanceManager.getDefault(LightManager.class).provide("IL1");
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
