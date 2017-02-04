package jmri.jmrix.powerline.simulator;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for SpecificLightManager class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/

public class SpecificLightManagerTest {

   private SpecificSystemConnectionMemo memo = null;

   @Test
   public void ConstructorTest(){
      Assert.assertNotNull("SpecificLightManager constructor",new SpecificLightManager(new SpecificTrafficController(memo)));
   }

   @Before
   public void setUp(){
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
        memo = new SpecificSystemConnectionMemo();
   }

   @After
   public void tearDown(){
        apps.tests.Log4JFixture.tearDown();
        jmri.util.JUnitUtil.resetInstanceManager();
        memo = null;
   }

}
