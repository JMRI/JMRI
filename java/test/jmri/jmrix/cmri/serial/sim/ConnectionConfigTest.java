package jmri.jmrix.cmri.serial.sim;

import jmri.jmrix.cmri.CMRISystemConnectionMemo;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for ConnectionConfig class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/
public class ConnectionConfigTest extends jmri.jmrix.AbstractSerialConnectionConfigTestBase  {

   @BeforeEach
   @Override
   public void setUp() {
        JUnitUtil.setUp();

        JUnitUtil.initDefaultUserMessagePreferences();
        cc = new ConnectionConfig();
   }

   @AfterEach
   @Override
   public void tearDown(){
       if (cc.getAdapter() != null) {
            ((CMRISystemConnectionMemo)cc.getAdapter().getSystemConnectionMemo()).getTrafficController().terminateThreads();
        }
        cc.dispose();
        cc = null;
       JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
       JUnitUtil.tearDown();
   }

}
