package jmri.jmrix.powerline.insteon2412s;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for SpecificLightManager class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/

public class SpecificLightManagerTest {

   private SpecificSystemConnectionMemo memo = null;

   @Test
   public void ConstructorTest(){
      Assert.assertNotNull("SpecificLightManager constructor",new SpecificLightManager(new SpecificTrafficController(memo)));
   }

   @Before
   public void setUp() {
        JUnitUtil.setUp();

        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
        memo = new SpecificSystemConnectionMemo();
   }

   @After
   public void tearDown(){
        JUnitUtil.tearDown();
        memo = null;
   }

}
