package jmri.jmrix.zimo;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for Mx1Exception class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/

public class Mx1ExceptionTest {

   @Test
   public void StringConstructorTest(){
      Assert.assertNotNull("Mx1Exception string constructor",new Mx1Exception("test exception"));
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
