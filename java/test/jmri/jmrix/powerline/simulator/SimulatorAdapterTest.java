package jmri.jmrix.powerline.simulator;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for SimulatorAdapter class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/

public class SimulatorAdapterTest {

   @Test
   public void ConstructorTest(){
      Assert.assertNotNull("SimulatorAdapter constructor",new SimulatorAdapter());
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
