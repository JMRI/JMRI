package jmri.jmrix.powerline.cm11;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for SpecificDriverAdapter class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/

public class SpecificDriverAdapterTest {

   @Test
   public void ConstructorTest(){
      Assert.assertNotNull("SpecificDriverAdapter constructor",new SpecificDriverAdapter());
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
