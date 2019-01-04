package jmri.jmrix.roco.z21;

import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * Tests for Z21CanReporter class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/

public class Z21CanReporterTest extends jmri.implementation.AbstractRailComReporterTest {

   @Test
   public void testRailComReply(){
       Z21CanReporter zr = (Z21CanReporter) r;
       byte msg[]={(byte)0x0E,(byte)0x00,(byte)0xC4,(byte)0x00,(byte)0xcd,(byte)0xab,(byte)0x01,(byte)0x00,(byte)0x01,(byte)0x11,(byte)0x01,(byte)0x00,(byte)0x00,(byte)0x00};
       Z21Reply reply = new Z21Reply(msg,14);
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
        Z21SystemConnectionMemo memo = new Z21SystemConnectionMemo();
        Z21InterfaceScaffold tc = new Z21InterfaceScaffold();
        memo.setTrafficController(tc);
        r = new Z21CanReporter("ZRabcd:1","hello world",memo);

   }

   @Override
   @After
   public void tearDown(){
	r = null;
        JUnitUtil.tearDown();
   }

}
