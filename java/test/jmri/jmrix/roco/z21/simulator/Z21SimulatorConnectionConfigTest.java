package jmri.jmrix.roco.z21.simulator;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for Z21SimulatorZ21SimulatorConnectionConfig class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/

public class Z21SimulatorConnectionConfigTest extends jmri.jmrix.AbstractSimulatorConnectionConfigTestBase {

   @BeforeEach
   @Override
   public void setUp() {
        JUnitUtil.setUp();

        JUnitUtil.initDefaultUserMessagePreferences();
        cc = new Z21SimulatorConnectionConfig();
   }

   @AfterEach
   @Override
   public void tearDown(){
        cc = null;
        JUnitUtil.tearDown();
   }

}
