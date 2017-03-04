package jmri.jmrix.zimo;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for Mx1Exception class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/

public class Mx1ExceptionTest {

   @Test
   public void StringConstructorTest(){
      Assert.assertNotNull("Mx1Exception string constructor",new Mx1Exception("test exception"));
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
