package jmri.jmrix.loconet;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for LocoNetException class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/

public class LocoNetExceptionTest {

   @Test
   public void StringConstructorTest(){
      Assert.assertNotNull("LocoNetException string constructor",new LocoNetException("test exception"));
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
