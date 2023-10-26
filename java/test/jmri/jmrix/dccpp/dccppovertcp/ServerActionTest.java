package jmri.jmrix.dccpp.dccppovertcp;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for ServerAction class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/

public class ServerActionTest {

   @Test
   public void testDccPpServerActionConstructor(){
      Assertions.assertNotNull( new ServerAction(), "ServerAction constructor");
   }

   @BeforeEach
   public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initDefaultUserMessagePreferences();
        jmri.jmrix.dccpp.DCCppSystemConnectionMemo memo = new jmri.jmrix.dccpp.DCCppSystemConnectionMemo();
        jmri.InstanceManager.setDefault(jmri.jmrix.dccpp.DCCppSystemConnectionMemo.class, memo);
   }

   @AfterEach
   public void tearDown(){
        JUnitUtil.tearDown();
   }

}
