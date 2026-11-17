package jmri.jmrix.loconet.uhlenbrock.usb_63860;

import jmri.util.JUnitUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

/**
 * Tests for ConnectionConfig class.
 *
 * @author Paul Bender Copyright (C) 2016, 2020
 **/
public class ConnectionConfigTest extends jmri.jmrix.AbstractSerialConnectionConfigTestBase  {

   @BeforeEach
   @Override
   public void setUp() {
        JUnitUtil.setUp();

        JUnitUtil.initDefaultUserMessagePreferences();
        cc = new ConnectionConfig();
        ((ConnectionConfig)cc).setInstance(); // create an adapter assumed to exist in tests
   }

   @AfterEach
   @Override
   public void tearDown(){
        cc = null;
        JUnitUtil.tearDown();
   }

}
