package jmri.jmrix.sprog.simulator;

import jmri.jmrix.sprog.SprogSystemConnectionMemo;
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
        if (cc != null) {
            if (cc.getAdapter() != null) {
                if (cc.getAdapter().getSystemConnectionMemo() != null) {
                    if (((SprogSystemConnectionMemo)cc.getAdapter().getSystemConnectionMemo()).getSprogTrafficController() != null) {
                        ((SprogSystemConnectionMemo)cc.getAdapter().getSystemConnectionMemo()).getSprogTrafficController().dispose();
                    }
                }
            }
        }
        cc = null;
        JUnitUtil.tearDown();
   }

}
