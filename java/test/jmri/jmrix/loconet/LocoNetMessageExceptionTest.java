package jmri.jmrix.loconet;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for LocoNetMessageException class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/

public class LocoNetMessageExceptionTest {

   @Test
   public void ConstructorTest(){
      Assert.assertNotNull("LocoNetMessageException constructor",new LocoNetMessageException());
   }

   @Test
   public void StringConstructorTest(){
      Assert.assertNotNull("LocoNetMessageException string constructor",new LocoNetMessageException("test exception"));
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
