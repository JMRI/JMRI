package jmri;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 * Tests for ProgReadException class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/

public class ProgReadExceptionTest {

   @Test
   public void ConstructorTest(){
      Assert.assertNotNull("ProgReadException constructor",new ProgReadException());
   }

   @Test
   public void StringConstructorTest(){
      Assert.assertNotNull("ProgReadException string constructor",new ProgReadException("test exception"));
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
