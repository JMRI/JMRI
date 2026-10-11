package jmri.jmrix.powerline.insteon2412s;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for SpecificLight class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/
public class SpecificX10LightTest {

   private SpecificTrafficController tc = null;

   @Test
   public void testSpecificX10LightConstructor(){
      Assertions.assertNotNull( new SpecificX10Light("PLA2",tc), "SpecificLight constructor");
   }

   @BeforeEach
   public void setUp() {
        JUnitUtil.setUp();

        JUnitUtil.initDefaultUserMessagePreferences();
        SpecificSystemConnectionMemo memo = new SpecificSystemConnectionMemo();
        tc = new SpecificTrafficController(memo);
        memo.setTrafficController(tc);
        memo.configureManagers();
        memo.setSerialAddress(new jmri.jmrix.powerline.SerialAddress(memo));
   }

   @AfterEach
   public void tearDown(){
        tc.terminateThreads();
        tc = null;
        JUnitUtil.tearDown();
   }

}
