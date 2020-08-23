package jmri;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

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

   @BeforeEach
   public void setUp() {
        JUnitUtil.setUp();

        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
   }

   @AfterEach
   public void tearDown(){
        JUnitUtil.tearDown();
   }

}
