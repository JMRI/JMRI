package jmri.jmrix;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for SerialConfigException class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/

public class SerialConfigExceptionTest {

   @Test
   public void ConstructorTest(){
      Assert.assertNotNull("SerialConfigException constructor",new SerialConfigException());
   }

   @Test
   public void StringConstructorTest(){
      Assert.assertNotNull("SerialConfigException string constructor",new SerialConfigException("test exception"));
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
