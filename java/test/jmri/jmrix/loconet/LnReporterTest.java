package jmri.jmrix.loconet;

import jmri.InstanceManager;
import jmri.Reportable;
import jmri.LocoAddress;
import jmri.jmrix.internal.InternalSystemConnectionMemo;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the jmri.jmrix.loconet.LnReporter class.
 *
 * @author	Bob Jacobsen Copyright 2001, 2002
 */
public class LnReporterTest extends jmri.implementation.AbstractReporterTestBase {

    @Override
    protected Object generateObjectToReport(){
        return "3 enter";
    }

    @Test
    public void testLnReporterCreate() {
        LnReporter a1 = new LnReporter(1, tc, "L");
        Assert.assertNotNull("exists", a1);
    }

    @Test
    public void testTranspond3Enter146() {
        LnReporter a = new LnReporter(146, tc, "L");
        LocoNetMessage l = new LocoNetMessage(new int[]{0xD0, 0x21, 0x11, 0x7D, 0x03, 0x00});
        a.messageFromManager(l);
        Assert.assertEquals("Transponding 3 enter 146", "3 enter", ((Reportable)a.getLastReport()).toReportString());
    }

    @Test
    public void testTranspond257Enter146() {
        LnReporter a = new LnReporter(146, tc, "L");
        LocoNetMessage l = new LocoNetMessage(new int[]{0xD0, 0x21, 0x11, 0x02, 0x01, 0x00});
        a.messageFromManager(l);
        Assert.assertEquals("Transponding 257 enter 146", "257 enter", ((Reportable)a.getLastReport()).toReportString());
    }

    @Test
    public void testTranspond257Exit146() {
        LnReporter a = new LnReporter(146, tc, "L");
        LocoNetMessage l = new LocoNetMessage(new int[]{0xD0, 0x01, 0x11, 0x02, 0x01, 0x00});
        a.messageFromManager(l);
        Assert.assertEquals("Transponding 257 exits 146", "257 exits", ((Reportable)a.getLastReport()).toReportString());
    }

    @Test
    public void testTranspond3Exits146() {
        LnReporter a = new LnReporter(146, tc, "L");
        LocoNetMessage l = new LocoNetMessage(new int[]{0xD0, 0x01, 0x11, 0x7D, 0x03, 0x00});
        a.messageFromManager(l);
        Assert.assertEquals("Transponding 3 exits 146", "3 exits", ((Reportable)a.getLastReport()).toReportString());
    }

    @Test
    public void testTranspond1056Enter175() {
        LnReporter a = new LnReporter(175, tc, "L");
        LocoNetMessage l = new LocoNetMessage(new int[]{0xD0, 0x21, 0x2E, 0x08, 0x20, 0x04});
        a.messageFromManager(l);
        Assert.assertEquals("Transponding 1056 enter 175", "1056 enter", ((Reportable)a.getLastReport()).toReportString());
    }

    @Test
    public void testLnReporterLissy1() {
        LnReporter a1 = new LnReporter(1, tc, "L");
        LocoNetMessage l = new LocoNetMessage(new int[]{0xE4, 0x08, 0x00, 0x60, 0x01, 0x42, 0x35, 0x05});
        a1.messageFromManager(l);
        Assert.assertEquals("Lissy message 1", "8501 seen southbound", ((Reportable)a1.getLastReport()).toReportString());
    }

    @Test
    public void testLnReporterLissy2() {
        LnReporter a3 = new LnReporter(3, tc, "L");
        LocoNetMessage l = new LocoNetMessage(new int[]{0xE4, 0x08, 0x00, 0x40, 0x03, 0x42, 0x35, 0x05});
        a3.messageFromManager(l);
        Assert.assertEquals("Lissy message 2", "8501 seen northbound", ((Reportable)a3.getLastReport()).toReportString());
    }

    @Test
    public void testLnReporterGetLocoAddress() {
        LocoAddress t = ((LnReporter)r).getLocoAddress("7413 enter");
        Assert.assertEquals("getLocoAddress", t.getNumber(), 7413);
    }

    @Test
    public void testIsTranspondingLocationReport() {
        LocoNetMessage m = new LocoNetMessage(new int[] {0x81, 0x7e});
        Assert.assertFalse("isTranspondingLocationReport-0x81", ((LnReporter)r).isTranspondingLocationReport(m));

        m = new LocoNetMessage(new int[] {0xe5, 0x09, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        Assert.assertFalse("isTranspondingLocationReport-0xE5 0x09 0x00", ((LnReporter)r).isTranspondingLocationReport(m));

        m = new LocoNetMessage(new int[] {0xD0, 0x49, 0x00, 0x00, 0x00, 0x00});
        Assert.assertFalse("isTranspondingLocationReport-0xD0 0x49", ((LnReporter)r).isTranspondingLocationReport(m));

        m = new LocoNetMessage(new int[] {0xD0, 0x59, 0x00, 0x00, 0x00, 0x00});
        Assert.assertFalse("isTranspondingLocationReport-0xD0 0x59", ((LnReporter)r).isTranspondingLocationReport(m));

        m = new LocoNetMessage(new int[] {0xD0, 0x69, 0x00, 0x00, 0x00, 0x00});
        Assert.assertFalse("isTranspondingLocationReport-0xD0 0x69", ((LnReporter)r).isTranspondingLocationReport(m));

        m = new LocoNetMessage(new int[] {0xD0, 0x79, 0x00, 0x00, 0x00, 0x00});
        Assert.assertFalse("isTranspondingLocationReport-0xD0 0x79", ((LnReporter)r).isTranspondingLocationReport(m));

        m = new LocoNetMessage(new int[] {0xD0, 0x01, 0x00, 0x00, 0x00, 0x00});
        Assert.assertTrue("isTranspondingLocationReport-0xD0 0x01", ((LnReporter)r).isTranspondingLocationReport(m));
    }


    @Test
    public void testIsTranspondingFindReport() {
        LocoNetMessage m = new LocoNetMessage(new int[] {0x81, 0x7e});
        Assert.assertFalse("isTranspondingLocationReport-0x81", ((LnReporter)r).isTranspondingFindReport(m));

        m = new LocoNetMessage(new int[] {0xe5, 0x09, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        Assert.assertTrue("isTranspondingFindReport-0xE5 0x09 0x00", ((LnReporter)r).isTranspondingFindReport(m));

        m = new LocoNetMessage(new int[] {0xe5, 0x09, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        Assert.assertFalse("isTranspondingFindReport-0xE5 0x09 0x01", ((LnReporter)r).isTranspondingFindReport(m));

        m = new LocoNetMessage(new int[] {0xe5, 0x09, 0x02, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        Assert.assertFalse("isTranspondingFindReport-0xE5 0x09 0x02", ((LnReporter)r).isTranspondingFindReport(m));

        m = new LocoNetMessage(new int[] {0xe5, 0x09, 0x04, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        Assert.assertFalse("isTranspondingFindReport-0xE5 0x09 0x04", ((LnReporter)r).isTranspondingFindReport(m));

        m = new LocoNetMessage(new int[] {0xe5, 0x09, 0x08, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        Assert.assertFalse("isTranspondingFindReport-0xE5 0x09 0x08", ((LnReporter)r).isTranspondingFindReport(m));

        m = new LocoNetMessage(new int[] {0xe5, 0x09, 0x10, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        Assert.assertFalse("isTranspondingFindReport-0xE5 0x09 0x10", ((LnReporter)r).isTranspondingFindReport(m));

        m = new LocoNetMessage(new int[] {0xe5, 0x09, 0x20, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        Assert.assertFalse("isTranspondingFindReport-0xE5 0x09 0x20", ((LnReporter)r).isTranspondingFindReport(m));

        m = new LocoNetMessage(new int[] {0xe5, 0x09, 0x40, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        Assert.assertFalse("isTranspondingFindReport-0xE5 0x09 0x40", ((LnReporter)r).isTranspondingFindReport(m));

        m = new LocoNetMessage(new int[] {0xe5, 0x09, 0x7f, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        Assert.assertFalse("isTranspondingFindReport-0xE5 0x09 0x7f", ((LnReporter)r).isTranspondingFindReport(m));

        m = new LocoNetMessage(new int[] {0xD0, 0x49, 0x00, 0x00, 0x00, 0x00});
        Assert.assertFalse("isTranspondingFindReport-0xD0 0x49", ((LnReporter)r).isTranspondingFindReport(m));

        m = new LocoNetMessage(new int[] {0xD0, 0x59, 0x00, 0x00, 0x00, 0x00});
        Assert.assertFalse("isTranspondingFindReport-0xD0 0x59", ((LnReporter)r).isTranspondingFindReport(m));

        m = new LocoNetMessage(new int[] {0xD0, 0x69, 0x00, 0x00, 0x00, 0x00});
        Assert.assertFalse("isTranspondingFindReport-0xD0 0x69", ((LnReporter)r).isTranspondingFindReport(m));

        m = new LocoNetMessage(new int[] {0xD0, 0x79, 0x00, 0x00, 0x00, 0x00});
        Assert.assertFalse("isTranspondingFindReport-0xD0 0x79", ((LnReporter)r).isTranspondingFindReport(m));

        m = new LocoNetMessage(new int[] {0xD0, 0x01, 0x00, 0x00, 0x00, 0x00});
        Assert.assertFalse("isTranspondingFindReport-0xD0 0x01", ((LnReporter)r).isTranspondingFindReport(m));
    }

    @Test
    public void testGetLocoAddrFromTranspondingMsg() {
        LocoNetMessage m = new LocoNetMessage(new int[] {0, 0, 0, 0, 0, 0});
        Assert.assertEquals("getLocoAddrFromTranspondingMsg-0 0 0 0 0 0", 0, ((LnReporter)r).getLocoAddrFromTranspondingMsg(m));

        m = new LocoNetMessage(new int[] {0, 0, 0, 1, 0, 0});
        Assert.assertEquals("getLocoAddrFromTranspondingMsg-0 0 0 1 0 0", 128, ((LnReporter)r).getLocoAddrFromTranspondingMsg(m));

        m = new LocoNetMessage(new int[] {0, 0, 0, 2, 0, 0});
        Assert.assertEquals("getLocoAddrFromTranspondingMsg-0 0 0 2 0 0", 256, ((LnReporter)r).getLocoAddrFromTranspondingMsg(m));

        m = new LocoNetMessage(new int[] {0, 0, 0, 4, 0, 0});
        Assert.assertEquals("getLocoAddrFromTranspondingMsg-0 0 0 4 0 0", 512, ((LnReporter)r).getLocoAddrFromTranspondingMsg(m));

        m = new LocoNetMessage(new int[] {0, 0, 0, 8, 0, 0});
        Assert.assertEquals("getLocoAddrFromTranspondingMsg-0 0 0 8 0 0", 1024, ((LnReporter)r).getLocoAddrFromTranspondingMsg(m));

        m = new LocoNetMessage(new int[] {0, 0, 0, 0x10, 0, 0});
        Assert.assertEquals("getLocoAddrFromTranspondingMsg-0 0 0 0x10 0 0", 2048, ((LnReporter)r).getLocoAddrFromTranspondingMsg(m));

        m = new LocoNetMessage(new int[] {0, 0, 0, 0x20, 0, 0});
        Assert.assertEquals("getLocoAddrFromTranspondingMsg-0 0 0 0x20 0 0", 4096, ((LnReporter)r).getLocoAddrFromTranspondingMsg(m));

        m = new LocoNetMessage(new int[] {0, 0, 0, 0x40, 0, 0});
        Assert.assertEquals("getLocoAddrFromTranspondingMsg-0 0 0 0x40 0 0", 8192, ((LnReporter)r).getLocoAddrFromTranspondingMsg(m));

        m = new LocoNetMessage(new int[] {0, 0, 0, 0x40, 0x01, 0});
        Assert.assertEquals("getLocoAddrFromTranspondingMsg-0 0 0 0x40 0x01 0", 8193, ((LnReporter)r).getLocoAddrFromTranspondingMsg(m));

        m = new LocoNetMessage(new int[] {0, 0, 0, 0x40, 0x02, 0});
        Assert.assertEquals("getLocoAddrFromTranspondingMsg-0 0 0 0x40 0x02 0", 8194, ((LnReporter)r).getLocoAddrFromTranspondingMsg(m));

        m = new LocoNetMessage(new int[] {0, 0, 0, 0x40, 0x04, 0});
        Assert.assertEquals("getLocoAddrFromTranspondingMsg-0 0 0 0x40 0x04 0", 8196, ((LnReporter)r).getLocoAddrFromTranspondingMsg(m));

        m = new LocoNetMessage(new int[] {0, 0, 0, 0x40, 0x08, 0});
        Assert.assertEquals("getLocoAddrFromTranspondingMsg-0 0 0 0x40 0x08 0", 8200, ((LnReporter)r).getLocoAddrFromTranspondingMsg(m));


        m = new LocoNetMessage(new int[] {0, 0, 0, 0x40, 0x10, 0});
        Assert.assertEquals("getLocoAddrFromTranspondingMsg-0 0 0 0x40 0x10 0", 8208, ((LnReporter)r).getLocoAddrFromTranspondingMsg(m));

        m = new LocoNetMessage(new int[] {0, 0, 0, 0x40, 0x20, 0});
        Assert.assertEquals("getLocoAddrFromTranspondingMsg-0 0 0 0x40 0x20 0", 8224, ((LnReporter)r).getLocoAddrFromTranspondingMsg(m));

        m = new LocoNetMessage(new int[] {0, 0, 0, 0x40, 0x40, 0});
        Assert.assertEquals("getLocoAddrFromTranspondingMsg-0 0 0 0x40 0x40 0", 8256, ((LnReporter)r).getLocoAddrFromTranspondingMsg(m));

        m = new LocoNetMessage(new int[] {0, 0, 0, 0x7D, 0, 0});
        Assert.assertEquals("getLocoAddrFromTranspondingMsg-0 0 0 0x7D 0 0", 0, ((LnReporter)r).getLocoAddrFromTranspondingMsg(m));

        m = new LocoNetMessage(new int[] {0, 0, 0, 0x7D, 0x01, 0});
        Assert.assertEquals("getLocoAddrFromTranspondingMsg-0 0 0 0x7D 0x01 0", 1, ((LnReporter)r).getLocoAddrFromTranspondingMsg(m));

        m = new LocoNetMessage(new int[] {0, 0, 0, 0x7D, 0x02, 0});
        Assert.assertEquals("getLocoAddrFromTranspondingMsg-0 0 0 0x7D 0x02 0", 2, ((LnReporter)r).getLocoAddrFromTranspondingMsg(m));

        m = new LocoNetMessage(new int[] {0, 0, 0, 0x7D, 0x04, 0});
        Assert.assertEquals("getLocoAddrFromTranspondingMsg-0 0 0 0x7D 0x04 0", 4, ((LnReporter)r).getLocoAddrFromTranspondingMsg(m));

        m = new LocoNetMessage(new int[] {0, 0, 0, 0x7D, 0x08, 0});
        Assert.assertEquals("getLocoAddrFromTranspondingMsg-0 0 0 0x7D 0x08 0", 8, ((LnReporter)r).getLocoAddrFromTranspondingMsg(m));

        m = new LocoNetMessage(new int[] {0, 0, 0, 0x7D, 0x10, 0});
        Assert.assertEquals("getLocoAddrFromTranspondingMsg-0 0 0 0x7D 0x10 0", 16, ((LnReporter)r).getLocoAddrFromTranspondingMsg(m));

        m = new LocoNetMessage(new int[] {0, 0, 0, 0x7D, 0x20, 0});
        Assert.assertEquals("getLocoAddrFromTranspondingMsg-0 0 0 0x7D 0x20 0", 32, ((LnReporter)r).getLocoAddrFromTranspondingMsg(m));

        m = new LocoNetMessage(new int[] {0, 0, 0, 0x7D, 0x40, 0});
        Assert.assertEquals("getLocoAddrFromTranspondingMsg-0 0 0 0x7D 0x40 0", 64, ((LnReporter)r).getLocoAddrFromTranspondingMsg(m));

        m = new LocoNetMessage(new int[] {0, 0, 0, 0x7D, 0x7f, 0});
        Assert.assertEquals("getLocoAddrFromTranspondingMsg-0 0 0 0x7D 0x7F 0", 127, ((LnReporter)r).getLocoAddrFromTranspondingMsg(m));
    }
    @Test
    public void testCollectionAfterMessage() {
        LnReporter lr = ((LnReporter)r);

        LocoNetMessage l = new LocoNetMessage(new int[]{0xD0, 0x20, 0x02, 0x7D, 0x03, 0x73});
        lr.messageFromManager(l);

       // Check that the collection has one element.
       Assert.assertEquals("Collection Size 1 after message", 1, lr.getCollection().size());
       Assert.assertTrue("Current Report contained in collection",lr.getCollection().contains(lr.getCurrentReport()));


       l = new LocoNetMessage(new int[]{0xD0, 0x00, 0x02, 0x7D, 0x03, 0x53});
       lr.messageFromManager(l);

       // Check that the collection was cleared.
       Assert.assertEquals("Collection Size 0 after clear message", 0, lr.getCollection().size());
       Assert.assertTrue("Collection Empty", lr.getCollection().isEmpty());
       // eventually, the current report should be changed to null on an exit.
       //Assert.assertNull("Current Report Null",lr.getCurrentReport());
    }

    private jmri.jmrix.loconet.LocoNetInterfaceScaffold tc;

    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        tc = new jmri.jmrix.loconet.LocoNetInterfaceScaffold();
        new TranspondingTagManager(); // this class registers itself.
        r = new LnReporter(3, tc, "L");
    }

    @After
    @Override
    public void tearDown() {
	r = null;
	tc = null;
        JUnitUtil.tearDown();
    }

}
