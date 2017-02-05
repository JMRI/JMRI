package jmri.jmrix.dccpp;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for DCCppMessageException class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/

public class DCCppMessageExceptionTest {

   @Test
   public void ConstructorTest(){
      Assert.assertNotNull("DCCppMessageException constructor",new DCCppMessageException());
   }

   @Test
   public void StringConstructorTest(){
      Assert.assertNotNull("DCCppMessageException string constructor",new DCCppMessageException("test exception"));
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
