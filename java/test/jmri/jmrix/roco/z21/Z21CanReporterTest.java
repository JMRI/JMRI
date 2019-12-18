package jmri.jmrix.roco.z21;

import java.util.ArrayList;
import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * Tests for Z21CanReporter class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/

public class Z21CanReporterTest extends jmri.implementation.AbstractRailComReporterTest {

   private Z21SystemConnectionMemo memo = null;
   private Z21InterfaceScaffold tc = null; 

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

    @Test
    public void testPropertyChangeAfterMessage() {
        currentReportSeen = false;
        lastReportSeen = false;
        Z21CanReporter zr = (Z21CanReporter) r;
        zr.addPropertyChangeListener(new TestReporterListener());
        byte msg[]={(byte)0x0E,(byte)0x00,(byte)0xC4,(byte)0x00,(byte)0xcd,(byte)0xab,(byte)0x01,(byte)0x00,(byte)0x01,(byte)0x11,(byte)0x01,(byte)0x00,(byte)0x00,(byte)0x00};
        Z21Reply reply = new Z21Reply(msg,14);
        zr.reply(reply);
        // Check that both CurrentReport and LastReport were seen
        Assert.assertTrue("CurrentReport seen", currentReportSeen);
        Assert.assertTrue("LastReport seen", lastReportSeen);

        currentReportSeen = false;
        lastReportSeen = false;
        byte msg2[]={(byte)0x0E,(byte)0x00,(byte)0xC4,(byte)0x00,(byte)0xcd,(byte)0xab,(byte)0x01,(byte)0x00,(byte)0x01,(byte)0x11,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00};
        reply = new Z21Reply(msg2,14);
        zr.reply(reply);
        // Check that both CurrentReport was seen
        Assert.assertTrue("CurrentReport seen after empty list", currentReportSeen);
        // and last Report was not seen
        Assert.assertFalse("LastReport seen after empty list", lastReportSeen);
    }

    @Test
    public void testCollectionAfterMessage() {
       Z21CanReporter zr = (Z21CanReporter) r;
       zr.addPropertyChangeListener(new TestReporterListener());
       byte msg[]={(byte)0x0E,(byte)0x00,(byte)0xC4,(byte)0x00,(byte)0xcd,(byte)0xab,(byte)0x01,(byte)0x00,(byte)0x01,(byte)0x11,(byte)0x01,(byte)0x00,(byte)0x00,(byte)0x00};
       Z21Reply reply = new Z21Reply(msg,14);
       zr.reply(reply);
       // Check that the collection has one element.
       Assert.assertEquals("Collection Size 1 after message", 1, zr.getCollection().size());
       Assert.assertEquals("Current Report = last entry",zr.getCurrentReport(),((ArrayList)zr.getCollection()).get(0));

       byte msg2[]={(byte)0x0E,(byte)0x00,(byte)0xC4,(byte)0x00,(byte)0xcd,(byte)0xab,(byte)0x01,(byte)0x00,(byte)0x01,(byte)0x11,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00};
       reply = new Z21Reply(msg2,14);
       zr.reply(reply);
       // Check that the collection wass cleared.
       Assert.assertEquals("Collection Size 0 after clear message", 0, zr.getCollection().size());
       Assert.assertTrue("Collection Empty", zr.getCollection().isEmpty());
       Assert.assertNull("Current Report Null",zr.getCurrentReport());
   }
    
   @Test
   public void testCollectionAfterMessageWith2Tags() {
      Z21CanReporter zr = (Z21CanReporter) r;
      zr.addPropertyChangeListener(new TestReporterListener());
      byte msg[]={(byte)0x0E,(byte)0x00,(byte)0xC4,(byte)0x00,(byte)0xcd,(byte)0xab,(byte)0x01,(byte)0x00,(byte)0x01,(byte)0x11,(byte)0x52,(byte)0xCC,(byte)0x1B,(byte)0xCC};
      Z21Reply reply = new Z21Reply(msg,14);
      zr.reply(reply);
      byte msg2[]={(byte)0x0E,(byte)0x00,(byte)0xC4,(byte)0x00,(byte)0xcd,(byte)0xab,(byte)0x01,(byte)0x00,(byte)0x01,(byte)0x12,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00}; // end of list, but second pair.
      reply = new Z21Reply(msg2,14);
      zr.reply(reply);
      // Check that the collection has two element.
      Assert.assertEquals("Collection Size 2 after message", 2, zr.getCollection().size());
      Assert.assertNotNull("Current Report NotNull",zr.getCurrentReport());
      Assert.assertEquals("Current Report = last entry",zr.getCurrentReport(),((ArrayList)zr.getCollection()).get(1));
      Assert.assertEquals("Current State",jmri.IdTag.SEEN,zr.getState());
      // Check that both CurrentReport and LastReport were seen by the listener
      Assert.assertTrue("CurrentReport seen", currentReportSeen);
      Assert.assertTrue("LastReport seen", lastReportSeen);

      currentReportSeen = false;
      lastReportSeen = false;

      byte msg3[]={(byte)0x0E,(byte)0x00,(byte)0xC4,(byte)0x00,(byte)0xcd,(byte)0xab,(byte)0x01,(byte)0x00,(byte)0x01,(byte)0x11,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00};
      reply = new Z21Reply(msg3,14);
      zr.reply(reply);
      // Check that the collection wass cleared.
      Assert.assertEquals("Collection Size 0 after clear message", 0, zr.getCollection().size());
      Assert.assertTrue("Collection Empty", zr.getCollection().isEmpty());
      Assert.assertNull("Current Report Null",zr.getCurrentReport());
      Assert.assertEquals("Current State",jmri.IdTag.UNSEEN,zr.getState());
      // Check that both CurrentReport was seen
      Assert.assertTrue("CurrentReport seen after empty list", currentReportSeen);
      // and last Report was not seen
      Assert.assertFalse("LastReport seen after empty list", lastReportSeen);
   }

   @Test
   public void testCollectionAfterMessageWith3Tags() {
      Z21CanReporter zr = (Z21CanReporter) r;
      zr.addPropertyChangeListener(new TestReporterListener());
      byte msg[]={(byte)0x0E,(byte)0x00,(byte)0xC4,(byte)0x00,(byte)0xcd,(byte)0xab,(byte)0x01,(byte)0x00,(byte)0x01,(byte)0x11,(byte)0x01,(byte)0x00,(byte)0x02,(byte)0x00};
      Z21Reply reply = new Z21Reply(msg,14);
      zr.reply(reply);
      byte msg2[]={(byte)0x0E,(byte)0x00,(byte)0xC4,(byte)0x00,(byte)0xcd,(byte)0xab,(byte)0x01,(byte)0x00,(byte)0x01,(byte)0x12,(byte)0x03,(byte)0x00,(byte)0x00,(byte)0x00}; // end of list, but second pair.
      reply = new Z21Reply(msg2,14);
      zr.reply(reply);
      // Check that the collection has two element.
      JUnitUtil.waitFor( () -> { return ( zr.getCollection().size() > 2); });
      Assert.assertEquals("Collection Size 3 after message", 3, zr.getCollection().size());

      byte msg3[]={(byte)0x0E,(byte)0x00,(byte)0xC4,(byte)0x00,(byte)0xcd,(byte)0xab,(byte)0x01,(byte)0x00,(byte)0x01,(byte)0x11,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00};
      reply = new Z21Reply(msg3,14);
      zr.reply(reply);
      // Check that the collection wass cleared.
      Assert.assertEquals("Collection Size 0 after clear message", 0, zr.getCollection().size());
      Assert.assertTrue("Collection Empty", zr.getCollection().isEmpty());
   }

   @Override
   @Before
   public void setUp() {
        JUnitUtil.setUp();

        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
        memo = new Z21SystemConnectionMemo();
        tc = new Z21InterfaceScaffold();
        memo.setTrafficController(tc);
        r = new Z21CanReporter("ZRabcd:1","hello world",memo);

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
