package jmri.jmrit.progsupport;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for ProgModeException class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/

public class ProgModeExceptionTest {

   @Test
   public void ConstructorTest(){
      Assert.assertNotNull("ProgModeException constructor",new ProgModeException());
   }

   @Test
   public void StringConstructorTest(){
      Assert.assertNotNull("ProgModeException string constructor",new ProgModeException("test exception"));
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
