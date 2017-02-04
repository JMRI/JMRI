package jmri.jmrix.powerline.cp290;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for SpecificMessage class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/

public class SpecificMessageTest {

   @Test
   public void ConstructorTest(){
      Assert.assertNotNull("SpecificMessage constructor",new SpecificMessage(5));
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
