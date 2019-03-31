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
    }
    
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initConnectionConfigManager();
        
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

}
