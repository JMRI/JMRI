package jmri.jmrix.sprog.pi.pisprogone;

import jmri.jmrix.sprog.SprogSystemConnectionMemo;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for ConnectionConfig class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/
public class ConnectionConfigTest extends jmri.jmrix.AbstractSerialConnectionConfigTestBase  {

   @Before
   @Override
   public void setUp() {
        JUnitUtil.setUp();

        JUnitUtil.initDefaultUserMessagePreferences();
        cc = new ConnectionConfig();
   }

   @After
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
