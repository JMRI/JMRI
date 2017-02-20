package jmri;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for Transit class.
 *
 * @author Bob Jacobsen Copyright (C) 2017
 * @author Paul Bender Copyright (C) 2016
 **/

public class TransitTest {

   @Test
   public void SysNameConstructorTest(){
      Assert.assertNotNull("Constructor", new Transit("TT1"));
   }

   @Test
   public void TwoNameStringConstructorTest(){
      Assert.assertNotNull("Constructor", new Transit("TT1", "user name"));
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
