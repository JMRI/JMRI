package jmri.jmrix.powerline.simulator;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Tests for SpecificLight class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/

public class SpecificX10LightTest {

   private SpecificTrafficController tc = null;

   @Test
   @Ignore("seems correct per the documentation, but results in a null light")
   public void ConstructorTest(){
      Assert.assertNotNull("SpecificLight constructor",new SpecificX10Light("PLA2",tc));
   }

   @Before
   public void setUp(){
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
        tc = new SpecificTrafficController(new SpecificSystemConnectionMemo());
   }

   @After
   public void tearDown(){
        apps.tests.Log4JFixture.tearDown();
        jmri.util.JUnitUtil.resetInstanceManager();
        tc = null;
   }

}
