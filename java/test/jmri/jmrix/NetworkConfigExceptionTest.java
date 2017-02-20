package jmri.jmrix;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for NetworkConfigException class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/

public class NetworkConfigExceptionTest {

   @Test
   public void ConstructorTest(){
      Assert.assertNotNull("NetworkConfigException constructor",new NetworkConfigException());
   }

   @Test
   public void StringConstructorTest(){
      Assert.assertNotNull("NetworkConfigException string constructor",new NetworkConfigException("test exception"));
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
