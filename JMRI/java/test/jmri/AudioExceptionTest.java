package jmri;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
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
   public void setUp() {
        JUnitUtil.setUp();

        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
   }

   @After
   public void tearDown(){
        JUnitUtil.tearDown();
   }

}
