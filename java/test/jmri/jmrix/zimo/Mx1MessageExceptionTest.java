package jmri.jmrix.zimo;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for Mx1MessageException class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/

public class Mx1MessageExceptionTest {

   @Test
   public void ConstructorTest(){
      Assert.assertNotNull("Mx1MessageException constructor",new Mx1MessageException());
   }

   @Test
   public void StringConstructorTest(){
      Assert.assertNotNull("Mx1MessageException string constructor",new Mx1MessageException("test exception"));
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
