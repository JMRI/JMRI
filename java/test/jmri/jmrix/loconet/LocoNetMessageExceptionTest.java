package jmri.jmrix.loconet;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for LocoNetMessageException class.
 *
 * @author Paul Bender Copyright (C) 2016
 */

public class LocoNetMessageExceptionTest {

   @Test
   public void ConstructorTest(){
      Assert.assertNotNull("LocoNetMessageException constructor",new LocoNetMessageException());
   }

   @Test
   public void StringConstructorTest(){
      Assert.assertNotNull("LocoNetMessageException string constructor",new LocoNetMessageException("test exception"));
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
