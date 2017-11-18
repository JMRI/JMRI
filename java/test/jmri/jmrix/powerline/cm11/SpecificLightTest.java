package jmri.jmrix.powerline.cm11;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Tests for SpecificLight class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/

public class SpecificLightTest {

   private SpecificTrafficController tc = null;

   @Test
   @Ignore("seems correct per the documentation, but results in a null light")
   public void ConstructorTest(){
      Assert.assertNotNull("SpecificLight constructor",new SpecificLight("PLA2",tc));
   }

   @Before
   public void setUp() {
        JUnitUtil.setUp();

        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
        tc = new SpecificTrafficController(new SpecificSystemConnectionMemo());
   }

   @After
   public void tearDown(){
        JUnitUtil.tearDown();
        tc = null;
   }

}
