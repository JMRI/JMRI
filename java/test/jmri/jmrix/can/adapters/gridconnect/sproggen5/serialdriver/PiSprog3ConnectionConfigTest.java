package jmri.jmrix.can.adapters.gridconnect.sproggen5.serialdriver;

import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * Tests for PiSprog3ConnectionConfig class.
 *
 * @author Andrew Crosland (C) 2020
 **/
public class PiSprog3ConnectionConfigTest extends jmri.jmrix.AbstractSerialConnectionConfigTestBase {

    @Test
    public void testCTor() {
        PiSprog3ConnectionConfig c = new PiSprog3ConnectionConfig();
        Assert.assertNotNull("exists",c);
    }
    
   @Before
   @Override
   public void setUp() {
        JUnitUtil.setUp();

        JUnitUtil.initDefaultUserMessagePreferences();
        cc = new PiSprog3ConnectionConfig();
   }

   @After
   @Override
   public void tearDown(){
        cc = null;
        JUnitUtil.tearDown();
   }

}
