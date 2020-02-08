package jmri.jmrix.can.adapters.gridconnect.sproggen5.serialdriver;

import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * Tests for CanisbConnectionConfig class.
 *
 * @author Andrew Crosland (C) 2020
 **/
public class CanisbConnectionConfigTest extends jmri.jmrix.AbstractSerialConnectionConfigTestBase {

    @Test
    public void testCTor() {
        CanisbConnectionConfig c = new CanisbConnectionConfig();
        Assert.assertNotNull("exists",c);
    }
    
   @Before
   @Override
   public void setUp() {
        JUnitUtil.setUp();

        JUnitUtil.initDefaultUserMessagePreferences();
        cc = new CanisbConnectionConfig();
   }

   @After
   @Override
   public void tearDown(){
        cc = null;
        JUnitUtil.tearDown();
   }

}
