package jmri.jmrix.powerline.cp290;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for SpecificLight class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/

public class SpecificLightTest {

   private SpecificTrafficController tc = null;

   @Test
   public void ConstructorTest(){
      Assert.assertNotNull("SpecificLight constructor",new SpecificLight("PLA2",tc));
   }

   @Before
   public void setUp() {
        JUnitUtil.setUp();

        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
        SpecificSystemConnectionMemo memo = new SpecificSystemConnectionMemo();
        tc = new SpecificTrafficController(memo);
        memo.setTrafficController(tc);
        memo.configureManagers();
        memo.setSerialAddress(new jmri.jmrix.powerline.SerialAddress(memo));
   }

   @After
   public void tearDown(){
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

        tc = null;
   }

}
