package jmri.jmrix.dccpp.dccppovertcp;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for ServerAction class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/

public class ServerActionTest {

   @Test
   public void ConstructorTest(){
      Assert.assertNotNull("ServerAction constructor",new ServerAction());
   }

   @BeforeEach
   public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
        jmri.jmrix.dccpp.DCCppSystemConnectionMemo memo = new jmri.jmrix.dccpp.DCCppSystemConnectionMemo();
        jmri.InstanceManager.setDefault(jmri.jmrix.dccpp.DCCppSystemConnectionMemo.class, memo);
   }

   @AfterEach
   public void tearDown(){
        JUnitUtil.tearDown();
   }

}
