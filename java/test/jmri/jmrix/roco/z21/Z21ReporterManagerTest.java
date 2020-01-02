package jmri.jmrix.roco.z21;

import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * This class provides tests for the Z21 Reporter Manager's ability to create
 * Z21Reporter objects.  There is only one Z21Reporter object possible, so 
 * some tests pulled down.
 *
 * @author Paul Bender Copyright (C) 2016
 **/

public class Z21ReporterManagerTest extends jmri.managers.AbstractReporterMgrTestBase {

    private Z21SystemConnectionMemo memo;
    private Z21InterfaceScaffold tc;

    @Override
    public String getSystemName(String i) {
        return "ZR" + i;
    }

   @Test
   public void testAutomaticCreateFromRailComReply(){
       Z21ReporterManager zr = (Z21ReporterManager) l;
       zr.enableInternalReporterCreationFromMessages();  // defautls to disabled.
       byte msg[]={(byte)0x11,(byte)0x00,(byte)0x88,(byte)0x00,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x05,(byte)0x06,(byte)0x07,(byte)0x08};
       Z21Reply reply = new Z21Reply(msg,17);
       Assert.assertNull("No reporter before message",zr.getReporter("ZR1"));
       jmri.util.ThreadingUtil.runOnLayout( () -> { zr.reply(reply); });
       JUnitUtil.waitFor( ()-> { return zr.getReporter("ZR1") != null; },"wait for reporter creation");
       Assert.assertNotNull("Reporter Created via message",zr.getReporter("ZR1"));
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
        JUnitUtil.clearShutDownManager(); // clears "Writing IdTags" from DefaultIdTagManager
        JUnitUtil.tearDown();
   }

    @Override
    protected int maxN() { return 1; }
    
    @Override
    protected String getNameToTest1() {
        return "1";
    }

}
