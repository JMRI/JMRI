package jmri.jmrix.powerline.cp290;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for SpecificSensorManager class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/

public class SpecificSensorManagerTest {

   private SpecificSystemConnectionMemo memo = null;

   @Test
   public void ConstructorTest(){
      Assert.assertNotNull("SpecificSensorManager constructor",new SpecificSensorManager(new SpecificTrafficController(memo)));
   }

   @BeforeEach
   public void setUp() {
        JUnitUtil.setUp();

        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
        memo = new SpecificSystemConnectionMemo();
   }

   @AfterEach
   public void tearDown(){
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

        memo = null;
   }

}
