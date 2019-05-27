package jmri.jmrix.pi;

import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioProvider;
import jmri.Turnout;
import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.ToDo;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Tests for RaspberryPiTurnoutManager.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class RaspberryPiTurnoutManagerTest extends jmri.managers.AbstractTurnoutMgrTestBase {

    @Override
    public String getSystemName(int i){
        return l.getSystemPrefix() + "T" + i;
    }

   @Test
   public void ConstructorTest(){
       Assert.assertNotNull(l);
   }

   @Test
   public void checkPrefix(){
       Assert.assertEquals("Prefix", "Pi", l.getSystemPrefix());
   }

    @Override    
    @Test
    public void testTurnoutPutGet() {
        // create
        Turnout t = l.newTurnout(getSystemName(18), "mine");
        // check
        Assert.assertNotNull("real object returned ", t);
        Assert.assertEquals("user name correct ", t, l.getByUserName("mine"));
        Assert.assertEquals("system name correct ", t, l.getBySystemName(getSystemName(18)));
    }

    @Test
    public void testProvideName() {
        // create
        Turnout t = l.provide(getSystemName(20));
        // check
        Assert.assertTrue("real object returned ", t != null);
        Assert.assertEquals("system name correct ", t, l.getBySystemName(getSystemName(20)));
    }

    @Override
    @Test
    public void testDefaultSystemName() {
        // create
        Turnout t = l.provideTurnout(getSystemName(getNumToTest1()));
        // check
        Assert.assertTrue("real object returned ", t != null);
        Assert.assertEquals("system name correct ", t, l.getBySystemName(getSystemName(getNumToTest1())));
    }

    @Override
    @Test
    public void testSingleObject() {
        // test that you always get the same representation
        Turnout t1 = l.newTurnout(getSystemName(16), "mine");
        Assert.assertTrue("t1 real object returned ", t1 != null);
        Assert.assertTrue("same by user ", t1 == l.getByUserName("mine"));
        Assert.assertTrue("same by system ", t1 == l.getBySystemName(getSystemName(16)));

        Turnout t2 = l.newTurnout(getSystemName(16), "mine");
        Assert.assertTrue("t2 real object returned ", t2 != null);
        // check
        Assert.assertTrue("same new ", t1 == t2);
    }

    @Override
    @Test
    public void testRename() {
        // get turnout
        Turnout t1 = l.newTurnout(getSystemName(15),"before");
        Assert.assertNotNull("t1 real object ", t1);
        t1.setUserName("after");
        Turnout t2 = l.getByUserName("after");
        Assert.assertEquals("same object", t1, t2);
        Assert.assertEquals("no old object", null, l.getByUserName("before"));
    }

    @Test
    @Ignore("This test doesn't work for this class")
    @ToDo("RaspberryPiSensor.init throws the error: com.pi4j.io.gpio.exception.GpioPinExistsException: This GPIO pin already exists: GPIO 1")
    @Override
    public void testRegisterDuplicateSystemName() {
    }

    @Override
    protected int getNumToTest1() {
        return 19;
    }
 
    @Override
    protected int getNumToTest2() {
        return 5;
    }

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
       JUnitUtil.setUp();
       GpioProvider myprovider = new PiGpioProviderScaffold();
       GpioFactory.setDefaultProvider(myprovider);
       jmri.util.JUnitUtil.resetInstanceManager();
       l = new RaspberryPiTurnoutManager("Pi");
    }

    @After
    public void tearDown() {
        JUnitUtil.clearShutDownManager();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.tearDown();
    }

}
