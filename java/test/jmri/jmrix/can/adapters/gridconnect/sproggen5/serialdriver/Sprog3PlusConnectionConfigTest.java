package jmri.jmrix.can.adapters.gridconnect.sproggen5.serialdriver;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;

/**
 * Tests for Sprog3PlusConnectionConfig class.
 *
 * @author Andrew Crosland (C) 2020
 **/
public class Sprog3PlusConnectionConfigTest extends jmri.jmrix.AbstractSerialConnectionConfigTestBase {

   @Before
   @Override
   public void setUp() {
        JUnitUtil.setUp();

        JUnitUtil.initDefaultUserMessagePreferences();
        cc = new Sprog3PlusConnectionConfig();
   }

   @After
   @Override
   public void tearDown(){
        cc = null;
        JUnitUtil.tearDown();
   }

}
