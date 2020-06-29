package jmri;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 * Tests for ProgWriteException class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/

public class ProgWriteExceptionTest {

   @Test
   public void ConstructorTest(){
      Assert.assertNotNull("ProgWriteException constructor",new ProgWriteException());
   }

   @Test
   public void StringConstructorTest(){
      Assert.assertNotNull("ProgWriteException string constructor",new ProgWriteException("test exception"));
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
