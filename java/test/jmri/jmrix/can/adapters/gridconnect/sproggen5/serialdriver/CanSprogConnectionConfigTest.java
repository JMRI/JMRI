package jmri.jmrix.can.adapters.gridconnect.sproggen5.serialdriver;

import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * Tests for CanSprogConnectionConfig class.
 *
 * @author Andrew Crosland (C) 2020
 **/
public class CanSprogConnectionConfigTest extends jmri.jmrix.AbstractSerialConnectionConfigTestBase {

    @Test
    public void testCTor() {
        CanSprogConnectionConfig c = new CanSprogConnectionConfig();
        Assert.assertNotNull("exists",c);
    }
    
   @Before
   @Override
   public void setUp() {
        JUnitUtil.setUp();

        JUnitUtil.initDefaultUserMessagePreferences();
        cc = new CanSprogConnectionConfig();
   }

   @After
   @Override
   public void tearDown(){
        cc = null;
        JUnitUtil.tearDown();
   }

}
