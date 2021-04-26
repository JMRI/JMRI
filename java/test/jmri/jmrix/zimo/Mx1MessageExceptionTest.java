package jmri.jmrix.zimo;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for Mx1MessageException class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/

public class Mx1MessageExceptionTest {

   @Test
   public void ConstructorTest(){
      Assert.assertNotNull("Mx1MessageException constructor",new Mx1MessageException());
   }

   @Test
   public void StringConstructorTest(){
      Assert.assertNotNull("Mx1MessageException string constructor",new Mx1MessageException("test exception"));
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
