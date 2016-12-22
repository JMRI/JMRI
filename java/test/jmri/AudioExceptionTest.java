package jmri;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Tests for AudioException class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/

public class AudioExceptionTest {

   @Test
   public void ConstructorTest(){
      Assert.assertNotNull("AudioException constructor",new AudioException());
   }

   @Test
   public void StringConstructorTest(){
      Assert.assertNotNull("AudioException string constructor",new AudioException("test exception"));
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
