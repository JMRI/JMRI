package jmri.jmrix.pi;

import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioProvider;
import jmri.Turnout;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * <P>
 * Tests for RaspberryPiTurnoutManager
 * </P>
 * @author Paul Bender Copyright (C) 2016
 */
public class RaspberryPiTurnoutManagerTest extends jmri.managers.AbstractTurnoutMgrTestBase {

    @Override
    public String getSystemName(int i){
        return "PIT"+i;
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
    public void testTurnoutPutGet() {
        // create
        Turnout t = l.newTurnout(getSystemName(18), "mine");
        // check
        Assert.assertNotNull("real object returned ", t);
        Assert.assertEquals("user name correct ", t, l.getByUserName("mine"));
        Assert.assertEquals("system name correct ", t, l.getBySystemName(getSystemName(18)));
    }

    @Override
    @Test
    public void testDefaultSystemName() {
        // create
        Turnout t = l.provideTurnout("PIT" + getNumToTest1());
        // check
        Assert.assertTrue("real object returned ", t != null);
        Assert.assertTrue("system name correct ", t == l.getBySystemName(getSystemName(getNumToTest1())));
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
       apps.tests.Log4JFixture.setUp();
       GpioProvider myprovider = new PiGpioProviderScaffold();
       GpioFactory.setDefaultProvider(myprovider);
       l = new RaspberryPiTurnoutManager("Pi");

    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }


}
