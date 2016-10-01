package jmri.jmrix.pi;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioProvider;
import com.pi4j.io.gpio.WiringPiGpioProviderBase;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinMode;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.PinPullResistance;

import jmri.Sensor;


/**
 * <P>
 * Tests for RaspberryPiSensorManager
 * </P>
 * @author Paul Bender Copyright (C) 2016
 */
public class RaspberryPiSensorManagerTest extends jmri.managers.AbstractSensorMgrTest {

    private GpioProvider myprovider = null;

    @Override
    public String getSystemName(int i) {
        return "PIS" + i;
    }

    @Test
    public void ConstructorTest(){
        Assert.assertNotNull(l);
    }

    @Test
    public void checkPrefix(){
        Assert.assertEquals("Prefix","PI",l.getSystemPrefix());
    }

    @Override
    @Test
    public void testSensorPutGet() {
        // create
        Sensor t = l.newSensor(getSystemName(10), "mine");
        // check
        Assert.assertTrue("real object returned ", t != null);
        Assert.assertTrue("user name correct ", t == l.getByUserName("mine"));
        Assert.assertTrue("system name correct ", t == l.getBySystemName(getSystemName(10)));
    }

    @Override
    @Test
    public void testSingleObject() {
        // test that you always get the same representation
        Sensor t1 = l.newSensor(getSystemName(11), "mine");
        Assert.assertTrue("t1 real object returned ", t1 != null);
        Assert.assertTrue("same by user ", t1 == l.getByUserName("mine"));
        Assert.assertTrue("same by system ", t1 == l.getBySystemName(getSystemName(11)));

        Sensor t2 = l.newSensor(getSystemName(11), "mine");
        Assert.assertTrue("t2 real object returned ", t2 != null);
        // check
        Assert.assertTrue("same new ", t1 == t2);
    }

    @Override
    @Test
    public void testRename() {
        // get light
        Sensor t1 = l.newSensor(getSystemName(12), "before");
        Assert.assertNotNull("t1 real object ", t1);
        t1.setUserName("after");
        Sensor t2 = l.getByUserName("after");
        Assert.assertEquals("same object", t1, t2);
        Assert.assertEquals("no old object", null, l.getByUserName("before"));
    }

    @Override
    @Test
    public void testDefaultSystemName() {
        // create
        Sensor t = l.provideSensor("" + 13);
        // check
        Assert.assertTrue("real object returned ", t != null);
        Assert.assertTrue("system name correct ", t == l.getBySystemName(getSystemName(13)));
    }



    @Override
    @Before
    public void setUp() {
       apps.tests.Log4JFixture.setUp();
       GpioProvider myprovider = new WiringPiGpioProviderBase(){
           @Override
           public String getName(){
              return "RaspberryPi GPIO Provider";
           }

           @Override
           public boolean hasPin(Pin pin) {
              return false;
           }

           @Override
           public void export(Pin pin, PinMode mode, PinState defaultState) {
           }

           @Override
           public void setPullResistance(Pin pin, PinPullResistance resistance) {
           }
            
           @Override
           protected void updateInterruptListener(Pin pin) {
           }

           @Override
           public PinState getState(Pin pin) {
                  return PinState.HIGH;
           }
       };
 
       GpioFactory.setDefaultProvider(myprovider);

       jmri.util.JUnitUtil.resetInstanceManager();
       l = new RaspberryPiSensorManager("Pi");
    }

    @After
    public void tearDown() {
       jmri.util.JUnitUtil.resetInstanceManager();
       myprovider = null;
       apps.tests.Log4JFixture.tearDown();
    }

}
