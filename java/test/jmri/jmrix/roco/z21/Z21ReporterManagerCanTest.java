package jmri.jmrix.roco.z21;

import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * This class provides tests for the Z21 Reporter Manager's ability to create
 * Z21CanReporter objects.  
 *
 * @author Paul Bender Copyright (C) 2016
 **/

public class Z21ReporterManagerCanTest extends jmri.managers.AbstractReporterMgrTestBase {
        
    private Z21SystemConnectionMemo memo;
    private Z21InterfaceScaffold tc;

    @Override
    public String getSystemName(String i) {
        return "ZR" + i;
    }

   @Test
   public void testAutomaticCreateFromCanReply(){
       Z21ReporterManager zr = (Z21ReporterManager) l;
       byte msg[]={(byte)0x0E,(byte)0x00,(byte)0xC4,(byte)0x00,(byte)0xcd,(byte)0xab,(byte)0x01,(byte)0x00,(byte)0x01,(byte)0x11,(byte)0x01,(byte)0x00,(byte)0x00,(byte)0x00};
       Z21Reply reply = new Z21Reply(msg,14);
       Assert.assertNull("No reporter before message",zr.getReporter("ZRabcd:1"));
       jmri.util.ThreadingUtil.runOnLayout( () -> { zr.reply(reply); });
       JUnitUtil.waitFor( ()-> { return zr.getReporter("ZRABCD:1") != null; },"wait for reporter creation");
       Assert.assertNotNull("Reporter Created via message",zr.getReporter("ZRABCD:1"));
   }

   @Before
    @Override
   public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
        memo = new Z21SystemConnectionMemo();
        tc = new Z21InterfaceScaffold();
        memo.setTrafficController(tc);
        memo.setRocoZ21CommandStation(new RocoZ21CommandStation());
        l = new Z21ReporterManager(memo);
   }

   @After
   public void tearDown(){
        l = null;
        tc.terminateThreads();
        memo = null;
        tc = null;
        JUnitUtil.tearDown();
   }

    @Override
    protected String getNameToTest1() {
        return "ABCD:1";
    }

    @Override
    protected String getNameToTest2() {
        return "ABCD:2";
    }

}
