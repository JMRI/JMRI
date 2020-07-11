package jmri.jmrix.loconet;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for LocoNetException class.
 *
 * @author Paul Bender Copyright (C) 2016
 */

public class LocoNetExceptionTest {

   @Test
   public void StringConstructorTest(){
      Assert.assertNotNull("LocoNetException string constructor", new LocoNetException("test exception"));
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
