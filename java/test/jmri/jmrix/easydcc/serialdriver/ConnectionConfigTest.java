package jmri.jmrix.easydcc.serialdriver;

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
        cc = null;
        JUnitUtil.tearDown();
   }

}
