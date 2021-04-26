package jmri.jmrix.mrc;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for MrcMessageException class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/

public class MrcMessageExceptionTest {

   @Test
   public void ConstructorTest(){
      Assert.assertNotNull("MrcMessageException constructor",new MrcMessageException());
   }

   @Test
   public void StringConstructorTest(){
      Assert.assertNotNull("MrcMessageException string constructor",new MrcMessageException("test exception"));
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
