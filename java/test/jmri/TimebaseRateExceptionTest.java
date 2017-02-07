package jmri;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for TimebaseRateException class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/

public class TimebaseRateExceptionTest {

   @Test
   public void ConstructorTest(){
      Assert.assertNotNull("TimebaseRateException constructor",new TimebaseRateException());
   }

   @Test
   public void StringConstructorTest(){
      Assert.assertNotNull("TimebaseRateException string constructor",new TimebaseRateException("test exception"));
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
