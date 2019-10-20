package jmri.jmrix.roco.z21;

import jmri.Reporter;
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

    @Test
    @Override
    public void testProvideName() {
        // create
        Reporter t = l.provide("ZRABCD:1");
        // check
        Assert.assertTrue("real object returned ", t != null);
        Assert.assertEquals("system name correct ", t,l.getBySystemName("ZRABCD:1"));
    }

    @Test
    public void testDefaultSystemNameLowerCase() {
        // create
        Reporter t = l.provideReporter("ZRabcd:1");
        // check
        Assert.assertNotNull("real object returned ", t);
        Assert.assertEquals("system name same input correct ", t,l.getBySystemName("ZRabcd:1"));
        Assert.assertEquals("system name same value correct ", t,l.getBySystemName("ZRABCD:1"));
    }

    @Test
    public void testDefaultSystemMixedDigit() {
        // create
        Reporter t = l.provideReporter("ZRa1c3:5");
        // check
        Assert.assertNotNull("real object returned ", t);
        Assert.assertEquals("system name same input correct ", t,l.getBySystemName("ZRa1c3:5"));
        Assert.assertEquals("system name same value correct ", t,l.getBySystemName("ZRA1C3:5"));
    }

    @Test
    public void testDefaultSystemMixedCase() {
        // create
        Reporter t = l.provideReporter("ZRaBcD:5");
        // check
        Assert.assertNotNull("real object returned ", t);
        Assert.assertEquals("system name same input correct", t, l.getBySystemName("ZRaBcD:5"));
        Assert.assertEquals("system name opposite input correct", t, l.getBySystemName("ZRAbCd:5"));
        Assert.assertEquals("system name same all lower", t, l.getBySystemName("ZRabcd:5"));
        Assert.assertEquals("system name same all upper", t, l.getBySystemName("ZRABCD:5"));
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
    protected String getNameToTest1() {
        return "ABCD:1";
    }

    @Override
    protected String getNameToTest2() {
        return "ABCD:2";
    }

}
