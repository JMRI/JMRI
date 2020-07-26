package jmri;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 * Tests for TimebaseRateException class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/

public class TimebaseRateExceptionTest {

   @Test
   public void ConstructorTest(){
      Assert.assertNotNull("TimebaseRateException constructor",new TimebaseRateException());
   }

   @Test
   public void StringConstructorTest(){
      Assert.assertNotNull("TimebaseRateException string constructor",new TimebaseRateException("test exception"));
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
