package jmri.jmrix.maple.serialdriver;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for SerialDriverAdapter class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/

public class SerialDriverAdapterTest {

   @Test
   public void ConstructorTest(){
      Assert.assertNotNull("SerialDriverAdapter constructor",new SerialDriverAdapter());
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
