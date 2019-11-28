package jmri.jmrix.roco.z21;

import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * Tests for Z21Reporter class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/

public class Z21ReporterTest extends jmri.implementation.AbstractRailComReporterTest {
        
   private Z21SystemConnectionMemo memo;
   private Z21InterfaceScaffold tc;

   @Test
   public void testRailComReply(){
       Z21Reporter zr = (Z21Reporter) r;
       byte msg[]={(byte)0x11,(byte)0x00,(byte)0x88,(byte)0x00,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x05,(byte)0x06,(byte)0x07,(byte)0x08};
       Z21Reply reply = new Z21Reply(msg,17);
       zr.reply(reply);
       // Check that both CurrentReport and LastReport are not null
       Assert.assertNotNull("CurrentReport Object exists", r.getCurrentReport());
       Assert.assertNotNull("LastReport Object exists", r.getLastReport());
       // Check the value of both CurrentReport and LastReport
       Assert.assertEquals("CurrentReport equals LastReport",r.getLastReport(), r.getCurrentReport());

   }

   @Override
   @Before
   public void setUp() {
        JUnitUtil.setUp();

        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
        memo = new Z21SystemConnectionMemo();
        tc = new Z21InterfaceScaffold();
        memo.setTrafficController(tc);
        r = new Z21Reporter("Z21R1","hello world",memo);

   }

   @Override
   @After
   public void tearDown(){
	    r = null;
        tc.terminateThreads();
        memo = null;
        tc = null;
        JUnitUtil.clearShutDownManager(); // clears "Writing IdTags" from DefaultIdTagManager
        JUnitUtil.tearDown();
   }

}
