package jmri.jmrix.can.adapters.gridconnect.sproggen5.serialdriver;

import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * Tests for Sprog3PlusConnectionConfig class.
 *
 * @author Andrew Crosland (C) 2020
 **/
public class Sprog3PlusConnectionConfigTest extends jmri.jmrix.AbstractSerialConnectionConfigTestBase {

    @Test
    public void testCTor() {
        Sprog3PlusConnectionConfig c = new Sprog3PlusConnectionConfig();
        Assert.assertNotNull("exists",c);
    }
    
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
