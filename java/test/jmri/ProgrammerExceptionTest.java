package jmri;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 * Tests for ProgrammerException class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/

public class ProgrammerExceptionTest {

   @Test
   public void ConstructorTest(){
      Assert.assertNotNull("ProgrammerException constructor",new ProgrammerException());
   }

   @Test
   public void StringConstructorTest(){
      Assert.assertNotNull("ProgrammerException string constructor",new ProgrammerException("test exception"));
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
