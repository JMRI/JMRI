package jmri.jmrix.cmri.serial.sim;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for SimDriverAdapter class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/

public class SimDriverAdapterTest {

   @Test
   public void ConstructorTest(){
      Assert.assertNotNull("SimDriverAdapter constructor",new SimDriverAdapter());
   }

   @Before
   public void setUp(){
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
   }

   @After
   public void tearDown(){
        apps.tests.Log4JFixture.tearDown();
        jmri.util.JUnitUtil.resetInstanceManager();
   }

}
