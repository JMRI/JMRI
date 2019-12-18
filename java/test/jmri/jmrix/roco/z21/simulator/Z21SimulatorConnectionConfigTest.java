package jmri.jmrix.roco.z21.simulator;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for Z21SimulatorZ21SimulatorConnectionConfig class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/

public class Z21SimulatorConnectionConfigTest extends jmri.jmrix.AbstractSimulatorConnectionConfigTestBase {

   @Before
   @Override
   public void setUp() {
        JUnitUtil.setUp();

        JUnitUtil.initDefaultUserMessagePreferences();
        cc = new Z21SimulatorConnectionConfig();
   }

   @After
   @Override
   public void tearDown(){
        cc = null;
        JUnitUtil.tearDown();
   }

}
