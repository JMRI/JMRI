package jmri.jmrix.mrc;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for MrcMessageException class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/

public class MrcMessageExceptionTest {

   @Test
   public void ConstructorTest(){
      Assert.assertNotNull("MrcMessageException constructor",new MrcMessageException());
   }

   @Test
   public void StringConstructorTest(){
      Assert.assertNotNull("MrcMessageException string constructor",new MrcMessageException("test exception"));
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
