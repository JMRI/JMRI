package jmri.jmrix.loconet;

import jmri.IdTag;
import jmri.Reportable;
import jmri.LocoAddress;
import jmri.PhysicalLocationReporter;
import jmri.util.JUnitUtil;
import jmri.util.PhysicalLocation;

import org.junit.jupiter.api.*;

/**
 * Tests for the jmri.jmrix.loconet.LnReporter class.
 *
 * @author Bob Jacobsen Copyright 2001, 2002
 */
public class LnReporterTest extends jmri.implementation.AbstractReporterTestBase {

    @Override
    protected Object generateObjectToReport() {
        return "3 enter";
    }

    @Test
    public void testLnReporterCreate() {
        LnReporter a1 = new LnReporter(1, tc, "L");
        Assertions.assertNotNull(a1, "exists");
    }

    @Test
    public void testTranspond3Enter146() {
        LnReporter a = new LnReporter(146, tc, "L");
        LocoNetMessage l = new LocoNetMessage(new int[]{0xD0, 0x21, 0x11, 0x7D, 0x03, 0x00});
        a.messageFromManager(l);
        Assertions.assertEquals("3 enter", ((Reportable) a.getLastReport()).toReportString(), "Transponding 3 enter 146");
    }

    @Test
    public void testTranspond257Enter146() {
        LnReporter a = new LnReporter(146, tc, "L");
        LocoNetMessage l = new LocoNetMessage(new int[]{0xD0, 0x21, 0x11, 0x02, 0x01, 0x00});
        a.messageFromManager(l);
        Assertions.assertEquals("257 enter", ((Reportable) a.getLastReport()).toReportString(), "Transponding 257 enter 146");
    }

    @Test
    public void testTranspond257Exit146() {
        LnReporter a = new LnReporter(146, tc, "L");
        LocoNetMessage l = new LocoNetMessage(new int[]{0xD0, 0x01, 0x11, 0x02, 0x01, 0x00});
        a.messageFromManager(l);
        Assertions.assertEquals("257 exits", ((Reportable) a.getLastReport()).toReportString(), "Transponding 257 exits 146");
    }

    @Test
    public void testTranspond3Exits146() {
        LnReporter a = new LnReporter(146, tc, "L");
        LocoNetMessage l = new LocoNetMessage(new int[]{0xD0, 0x01, 0x11, 0x7D, 0x03, 0x00});
        a.messageFromManager(l);
        Assertions.assertEquals("3 exits", ((Reportable) a.getLastReport()).toReportString(), "Transponding 3 exits 146");
    }

    @Test
    public void testTranspond1056Enter175() {
        LnReporter a = new LnReporter(175, tc, "L");
        LocoNetMessage l = new LocoNetMessage(new int[]{0xD0, 0x21, 0x2E, 0x08, 0x20, 0x04});
        a.messageFromManager(l);
        Assertions.assertEquals("1056 enter", ((Reportable) a.getLastReport()).toReportString(), "Transponding 1056 enter 175");
    }

    @Test
    public void testLnReporterLissy1() {
        LnReporter a1 = new LnReporter(1, tc, "L");
        LocoNetMessage l = new LocoNetMessage(new int[]{0xE4, 0x08, 0x00, 0x60, 0x01, 0x42, 0x35, 0x05});
        a1.messageFromManager(l);
        Assertions.assertEquals("8501:1 seen southbound", ((Reportable) a1.getLastReport()).toReportString(), "Lissy message 1");
    }

    @Test
    public void testLnReporterLissy2() {
        LnReporter a3 = new LnReporter(3, tc, "L");
        LocoNetMessage l = new LocoNetMessage(new int[]{0xE4, 0x08, 0x00, 0x40, 0x03, 0x42, 0x35, 0x05});
        a3.messageFromManager(l);
        Assertions.assertEquals("8501:1 seen northbound", ((Reportable) a3.getLastReport()).toReportString(), "Lissy message 2");
    }

    @Test
    public void testLnRfidReporterLissy() {
        LnReporter a4 = new LnReporter(4, tc, "L");
        LocoNetMessage l = new LocoNetMessage(new int[]{0xE4, 0x0E, 0x41, 0x00, 0x04, 0x53, 0x17, 0x78, 0x31, 0x00, 0x00, 0x00, 0x02, 0x5A});
        a4.messageFromManager(l);
        Assertions.assertEquals("53977831000000 4", ((Reportable) a4.getLastReport()).toReportString(), "Lissy message 3");
    }

    @Test
    public void testMessageFromManagerFindReport() {
        Assertions.assertEquals(-1, r.getState(), "MessageFromManagerFindReport- check initial state");
        Assertions.assertEquals(3, ((LnReporter) r).getNumber(), "check reporter number");

        LocoNetMessage m = new LocoNetMessage(new int[]{0xe5, 0x09, 0x00, 0x00, 0x07, 0x00, 0x00, 0x00, 0x00});
        ((LnReporter) r).messageFromManager(m);

        Assertions.assertEquals(7, r.getState(), "MessageFromManagerFindReport- check state after message 1");

        Assertions.assertEquals("7 enter", ((Reportable) r.getCurrentReport()).toReportString(), "MessageFromManagerFindReport- check report string after message 1");

        Assertions.assertEquals(7, ((LnReporter) r).lastLoco, "MessageFromManagerFindReport- check last loco after message 1");

        var location = ((IdTag) r.getCurrentReport()).getWhereLastSeen();
        Assertions.assertNotNull(location);
        Assertions.assertEquals("LR3", location.toString(), "MessageFromManagerFindReport- check location last seen after message 1");

        Assertions.assertEquals("LD7", ((IdTag) r.getCurrentReport()).getDisplayName(), "MessageFromManagerFindReport- check ID last seen after message 1");

        LnReporter r2 = new LnReporter(17, tc, "L");
        Assertions.assertEquals(-1, r2.getState(), "MessageFromManagerFindReport - check state of new reporter");

        LocoNetMessage m2 = new LocoNetMessage(new int[]{0xe5, 0x09, 0x00, 0x00, 0x03, 0x00, 0x00, 0x00, 0x00});
        r2.messageFromManager(m2);
        Assertions.assertEquals(3, r2.getState(), "MessageFromManagerFindReport- check state after message 1");

    }

    @Test
    public void testGetBeanPhysicalLocation() {
        // NOTE: it is unclear how JMRI makes use of the "physical location"
        // feature with respect to Reporters and IdTags, so testing here is
        // sketchy at best.
        LocoNetMessage m = new LocoNetMessage(new int[]{0xD0, 0x21, 0x2E, 0x08, 0x20, 0x04});
        ((LnReporter) r).messageFromManager(m);
        Assertions.assertEquals(PhysicalLocation.Origin, ((LnReporter) r).getPhysicalLocation(), "check physical location message 1");
        Assertions.assertEquals(PhysicalLocation.Origin, ((LnReporter) r).getPhysicalLocation("balderdash"), "check physical location string message 1");
    }

    @Test
    public void testLnReporterGetLocoAddress() {
        LocoAddress t = ((LnReporter) r).getLocoAddress("7413 enter");
        Assertions.assertEquals( 7413, t.getNumber(), "getLocoAddress 7431 enter");

        LocoAddress t2 = ((LnReporter) r).getLocoAddress(null);
        Assertions.assertNull(t2, "getLocoAddress <null>");

        LocoAddress t3 = ((LnReporter) r).getLocoAddress("abdc enter");
        Assertions.assertNull(t3, "getLocoAddress abcd enter");

    }

    @Test
    public void testIsTranspondingLocationReport() {
        LocoNetMessage m = new LocoNetMessage(new int[]{0x81, 0x7e});
        Assertions.assertFalse(((LnReporter) r).isTranspondingLocationReport(m), "isTranspondingLocationReport-0x81");

        m = new LocoNetMessage(new int[]{0xe5, 0x09, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        Assertions.assertFalse(((LnReporter) r).isTranspondingLocationReport(m), "isTranspondingLocationReport-0xE5 0x09 0x00");

        m = new LocoNetMessage(new int[]{0xD0, 0x49, 0x00, 0x00, 0x00, 0x00});
        Assertions.assertFalse(((LnReporter) r).isTranspondingLocationReport(m), "isTranspondingLocationReport-0xD0 0x49");

        m = new LocoNetMessage(new int[]{0xD0, 0x59, 0x00, 0x00, 0x00, 0x00});
        Assertions.assertFalse(((LnReporter) r).isTranspondingLocationReport(m), "isTranspondingLocationReport-0xD0 0x59");

        m = new LocoNetMessage(new int[]{0xD0, 0x69, 0x00, 0x00, 0x00, 0x00});
        Assertions.assertFalse(((LnReporter) r).isTranspondingLocationReport(m), "isTranspondingLocationReport-0xD0 0x69");

        m = new LocoNetMessage(new int[]{0xD0, 0x79, 0x00, 0x00, 0x00, 0x00});
        Assertions.assertFalse(((LnReporter) r).isTranspondingLocationReport(m), "isTranspondingLocationReport-0xD0 0x79");

        m = new LocoNetMessage(new int[]{0xD0, 0x01, 0x00, 0x00, 0x00, 0x00});
        Assertions.assertTrue(((LnReporter) r).isTranspondingLocationReport(m), "isTranspondingLocationReport-0xD0 0x01");
    }

    @Test
    public void testIsTranspondingFindReport() {
        LocoNetMessage m = new LocoNetMessage(new int[]{0x81, 0x7e});
        Assertions.assertFalse(((LnReporter) r).isTranspondingFindReport(m), "isTranspondingLocationReport-0x81");

        m = new LocoNetMessage(new int[]{0xe5, 0x09, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        Assertions.assertTrue(((LnReporter) r).isTranspondingFindReport(m), "isTranspondingFindReport-0xE5 0x09 0x00");

        m = new LocoNetMessage(new int[]{0xe5, 0x09, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        Assertions.assertFalse(((LnReporter) r).isTranspondingFindReport(m), "isTranspondingFindReport-0xE5 0x09 0x01");

        m = new LocoNetMessage(new int[]{0xe5, 0x09, 0x02, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        Assertions.assertFalse(((LnReporter) r).isTranspondingFindReport(m), "isTranspondingFindReport-0xE5 0x09 0x02");

        m = new LocoNetMessage(new int[]{0xe5, 0x09, 0x04, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        Assertions.assertFalse(((LnReporter) r).isTranspondingFindReport(m), "isTranspondingFindReport-0xE5 0x09 0x04");

        m = new LocoNetMessage(new int[]{0xe5, 0x09, 0x08, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        Assertions.assertFalse(((LnReporter) r).isTranspondingFindReport(m), "isTranspondingFindReport-0xE5 0x09 0x08");

        m = new LocoNetMessage(new int[]{0xe5, 0x09, 0x10, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        Assertions.assertFalse(((LnReporter) r).isTranspondingFindReport(m), "isTranspondingFindReport-0xE5 0x09 0x10");

        m = new LocoNetMessage(new int[]{0xe5, 0x09, 0x20, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        Assertions.assertFalse(((LnReporter) r).isTranspondingFindReport(m), "isTranspondingFindReport-0xE5 0x09 0x20");

        m = new LocoNetMessage(new int[]{0xe5, 0x09, 0x40, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        Assertions.assertFalse(((LnReporter) r).isTranspondingFindReport(m), "isTranspondingFindReport-0xE5 0x09 0x40");

        m = new LocoNetMessage(new int[]{0xe5, 0x09, 0x7f, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        Assertions.assertFalse(((LnReporter) r).isTranspondingFindReport(m), "isTranspondingFindReport-0xE5 0x09 0x7f");

        m = new LocoNetMessage(new int[]{0xe5, 0x08, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        Assertions.assertFalse(((LnReporter) r).isTranspondingFindReport(m), "isTranspondingFindReport-0xE5 0x08 0x00");

        m = new LocoNetMessage(new int[]{0xD0, 0x49, 0x00, 0x00, 0x00, 0x00});
        Assertions.assertFalse(((LnReporter) r).isTranspondingFindReport(m), "isTranspondingFindReport-0xD0 0x49");

        m = new LocoNetMessage(new int[]{0xD0, 0x59, 0x00, 0x00, 0x00, 0x00});
        Assertions.assertFalse(((LnReporter) r).isTranspondingFindReport(m), "isTranspondingFindReport-0xD0 0x59");

        m = new LocoNetMessage(new int[]{0xD0, 0x69, 0x00, 0x00, 0x00, 0x00});
        Assertions.assertFalse(((LnReporter) r).isTranspondingFindReport(m), "isTranspondingFindReport-0xD0 0x69");

        m = new LocoNetMessage(new int[]{0xD0, 0x79, 0x00, 0x00, 0x00, 0x00});
        Assertions.assertFalse(((LnReporter) r).isTranspondingFindReport(m), "isTranspondingFindReport-0xD0 0x79");

        m = new LocoNetMessage(new int[]{0xD0, 0x01, 0x00, 0x00, 0x00, 0x00});
        Assertions.assertFalse(((LnReporter) r).isTranspondingFindReport(m), "isTranspondingFindReport-0xD0 0x01");
    }

    @Test
    public void testGetLocoAddrFromTranspondingMsg() {
        LocoNetMessage m = new LocoNetMessage(new int[]{0, 0, 0, 0, 0, 0});
        Assertions.assertEquals(0, ((LnReporter) r).getLocoAddrFromTranspondingMsg(m), "getLocoAddrFromTranspondingMsg-0 0 0 0 0 0");

        m = new LocoNetMessage(new int[]{0, 0, 0, 1, 0, 0});
        Assertions.assertEquals(128, ((LnReporter) r).getLocoAddrFromTranspondingMsg(m), "getLocoAddrFromTranspondingMsg-0 0 0 1 0 0");

        m = new LocoNetMessage(new int[]{0, 0, 0, 2, 0, 0});
        Assertions.assertEquals(256, ((LnReporter) r).getLocoAddrFromTranspondingMsg(m), "getLocoAddrFromTranspondingMsg-0 0 0 2 0 0");

        m = new LocoNetMessage(new int[]{0, 0, 0, 4, 0, 0});
        Assertions.assertEquals(512, ((LnReporter) r).getLocoAddrFromTranspondingMsg(m), "getLocoAddrFromTranspondingMsg-0 0 0 4 0 0");

        m = new LocoNetMessage(new int[]{0, 0, 0, 8, 0, 0});
        Assertions.assertEquals(1024, ((LnReporter) r).getLocoAddrFromTranspondingMsg(m), "getLocoAddrFromTranspondingMsg-0 0 0 8 0 0");

        m = new LocoNetMessage(new int[]{0, 0, 0, 0x10, 0, 0});
        Assertions.assertEquals(2048, ((LnReporter) r).getLocoAddrFromTranspondingMsg(m), "getLocoAddrFromTranspondingMsg-0 0 0 0x10 0 0");

        m = new LocoNetMessage(new int[]{0, 0, 0, 0x20, 0, 0});
        Assertions.assertEquals(4096, ((LnReporter) r).getLocoAddrFromTranspondingMsg(m), "getLocoAddrFromTranspondingMsg-0 0 0 0x20 0 0");

        m = new LocoNetMessage(new int[]{0, 0, 0, 0x40, 0, 0});
        Assertions.assertEquals(8192, ((LnReporter) r).getLocoAddrFromTranspondingMsg(m), "getLocoAddrFromTranspondingMsg-0 0 0 0x40 0 0");

        m = new LocoNetMessage(new int[]{0, 0, 0, 0x40, 0x01, 0});
        Assertions.assertEquals(8193, ((LnReporter) r).getLocoAddrFromTranspondingMsg(m), "getLocoAddrFromTranspondingMsg-0 0 0 0x40 0x01 0");

        m = new LocoNetMessage(new int[]{0, 0, 0, 0x40, 0x02, 0});
        Assertions.assertEquals(8194, ((LnReporter) r).getLocoAddrFromTranspondingMsg(m), "getLocoAddrFromTranspondingMsg-0 0 0 0x40 0x02 0");

        m = new LocoNetMessage(new int[]{0, 0, 0, 0x40, 0x04, 0});
        Assertions.assertEquals(8196, ((LnReporter) r).getLocoAddrFromTranspondingMsg(m), "getLocoAddrFromTranspondingMsg-0 0 0 0x40 0x04 0");

        m = new LocoNetMessage(new int[]{0, 0, 0, 0x40, 0x08, 0});
        Assertions.assertEquals(8200, ((LnReporter) r).getLocoAddrFromTranspondingMsg(m), "getLocoAddrFromTranspondingMsg-0 0 0 0x40 0x08 0");

        m = new LocoNetMessage(new int[]{0, 0, 0, 0x40, 0x10, 0});
        Assertions.assertEquals(8208, ((LnReporter) r).getLocoAddrFromTranspondingMsg(m), "getLocoAddrFromTranspondingMsg-0 0 0 0x40 0x10 0");

        m = new LocoNetMessage(new int[]{0, 0, 0, 0x40, 0x20, 0});
        Assertions.assertEquals(8224, ((LnReporter) r).getLocoAddrFromTranspondingMsg(m), "getLocoAddrFromTranspondingMsg-0 0 0 0x40 0x20 0");

        m = new LocoNetMessage(new int[]{0, 0, 0, 0x40, 0x40, 0});
        Assertions.assertEquals(8256, ((LnReporter) r).getLocoAddrFromTranspondingMsg(m), "getLocoAddrFromTranspondingMsg-0 0 0 0x40 0x40 0");

        m = new LocoNetMessage(new int[]{0, 0, 0, 0x7D, 0, 0});
        Assertions.assertEquals(0, ((LnReporter) r).getLocoAddrFromTranspondingMsg(m), "getLocoAddrFromTranspondingMsg-0 0 0 0x7D 0 0");

        m = new LocoNetMessage(new int[]{0, 0, 0, 0x7D, 0x01, 0});
        Assertions.assertEquals(1, ((LnReporter) r).getLocoAddrFromTranspondingMsg(m), "getLocoAddrFromTranspondingMsg-0 0 0 0x7D 0x01 0");

        m = new LocoNetMessage(new int[]{0, 0, 0, 0x7D, 0x02, 0});
        Assertions.assertEquals(2, ((LnReporter) r).getLocoAddrFromTranspondingMsg(m), "getLocoAddrFromTranspondingMsg-0 0 0 0x7D 0x02 0");

        m = new LocoNetMessage(new int[]{0, 0, 0, 0x7D, 0x04, 0});
        Assertions.assertEquals(4, ((LnReporter) r).getLocoAddrFromTranspondingMsg(m), "getLocoAddrFromTranspondingMsg-0 0 0 0x7D 0x04 0");

        m = new LocoNetMessage(new int[]{0, 0, 0, 0x7D, 0x08, 0});
        Assertions.assertEquals(8, ((LnReporter) r).getLocoAddrFromTranspondingMsg(m), "getLocoAddrFromTranspondingMsg-0 0 0 0x7D 0x08 0");

        m = new LocoNetMessage(new int[]{0, 0, 0, 0x7D, 0x10, 0});
        Assertions.assertEquals(16, ((LnReporter) r).getLocoAddrFromTranspondingMsg(m), "getLocoAddrFromTranspondingMsg-0 0 0 0x7D 0x10 0");

        m = new LocoNetMessage(new int[]{0, 0, 0, 0x7D, 0x20, 0});
        Assertions.assertEquals(32, ((LnReporter) r).getLocoAddrFromTranspondingMsg(m), "getLocoAddrFromTranspondingMsg-0 0 0 0x7D 0x20 0");

        m = new LocoNetMessage(new int[]{0, 0, 0, 0x7D, 0x40, 0});
        Assertions.assertEquals(64, ((LnReporter) r).getLocoAddrFromTranspondingMsg(m), "getLocoAddrFromTranspondingMsg-0 0 0 0x7D 0x40 0");

        m = new LocoNetMessage(new int[]{0, 0, 0, 0x7D, 0x7f, 0});
        Assertions.assertEquals(127, ((LnReporter) r).getLocoAddrFromTranspondingMsg(m), "getLocoAddrFromTranspondingMsg-0 0 0 0x7D 0x7F 0");
    }

    @Test
    public void testGetNumber() {
        Assertions.assertEquals(3, ((LnReporter) r).getNumber(), "getNumber - A");

        r = new LnReporter(1, tc, "L");
        Assertions.assertEquals(1, ((LnReporter) r).getNumber(), "getNumber - B");

        r = new LnReporter(42, tc, "L");
        Assertions.assertEquals(42, ((LnReporter) r).getNumber(), "getNumber - C");
    }

    @Test
    public void testGetPhysicalLocationAndAddress() {
//        Assert.assertEquals("initial physical location",
//            PhysicalLocationReporter.Direction.UNKNOWN,
//            ((LnReporter)r).getDirection(((Reportable)r.getLastReport()).toReportString()));

        LocoNetMessage m = new LocoNetMessage(new int[]{0xe5, 0x09, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00});
        ((LnReporter) r).messageFromManager(m);
        Assertions.assertEquals(PhysicalLocationReporter.Direction.ENTER, ((LnReporter) r).getDirection(((Reportable) r.getLastReport()).toReportString()), "physical location after message 1");
        Assertions.assertEquals("128(D)", ((LnReporter) r).getLocoAddress(
                ((Reportable) r.getLastReport()).toReportString()).toString(), "loco addr after message 1");

        m = new LocoNetMessage(new int[]{0xD0, 0x01, 0x11, 0x02, 0x01, 0x00});
        ((LnReporter) r).messageFromManager(m);
        Assertions.assertEquals(PhysicalLocationReporter.Direction.EXIT, ((LnReporter) r).getDirection(((Reportable) r.getLastReport()).toReportString()), "physical location after message 2");
        Assertions.assertEquals("257(D)", ((LnReporter) r).getLocoAddress(
                ((Reportable) r.getLastReport()).toReportString()).toString(), "loco addr after message 2");

        m = new LocoNetMessage(new int[]{0xD0, 0x21, 0x11, 0x02, 0x01, 0x00});
        ((LnReporter) r).messageFromManager(m);
        Assertions.assertEquals(PhysicalLocationReporter.Direction.ENTER, ((LnReporter) r).getDirection(((Reportable) r.getLastReport()).toReportString()), "physical location after message 2");

        Assertions.assertEquals(PhysicalLocationReporter.Direction.UNKNOWN, ((LnReporter) r).getDirection("harrumph 84"), "getDirection- check nonsense string");

        Assertions.assertEquals(PhysicalLocationReporter.Direction.UNKNOWN, ((LnReporter) r).getDirection("harrumph"), "getDirection- check short string");

        Assertions.assertEquals(PhysicalLocationReporter.Direction.ENTER, ((LnReporter) r).getDirection("54 seen northbound"), "getDirection- check seen lissy 1");

        Assertions.assertEquals(PhysicalLocationReporter.Direction.ENTER, ((LnReporter) r).getDirection("54 seen southbound"), "getDirection- check seen lissy 2");

        Assertions.assertEquals(PhysicalLocationReporter.Direction.EXIT, ((LnReporter) r).getDirection("155 exits"), "getDirection- check exit");

    }

    @Test
    public void testLnReporterLissyBad() {
        LnReporter a1 = new LnReporter(1, tc, "L");

        LocoNetMessage l = new LocoNetMessage(new int[]{0xE4, 0x07, 0x00, 0x60, 0x01, 0x42, 0x35, 0x05});
        a1.messageFromManager(l);
        Assertions.assertNull(a1.getLastReport(), "bad Lissy message check report is null");
    }

    @Test
    @Disabled("Test requires further development")
    public void testGetDriectionString() {
    }

    @Test
    public void testCollectionAfterMessage() {
        LnReporter lr = ((LnReporter) r);

        LocoNetMessage l = new LocoNetMessage(new int[]{0xD0, 0x20, 0x02, 0x7D, 0x03, 0x73});
        lr.messageFromManager(l);

        // Check that the collection has one element.
        Assertions.assertEquals(1, lr.getCollection().size(), "Collection Size 1 after message");
        Assertions.assertTrue(lr.getCollection().contains(lr.getCurrentReport()), "Current Report contained in collection");

        l = new LocoNetMessage(new int[]{0xD0, 0x00, 0x02, 0x7D, 0x03, 0x53});
        lr.messageFromManager(l);

        // Check that the collection was cleared.
        Assertions.assertEquals(0, lr.getCollection().size(), "Collection Size 0 after clear message");
        Assertions.assertTrue(lr.getCollection().isEmpty(), "Collection Empty");
        // eventually, the current report should be changed to null on an exit.
        //Assert.assertNull("Current Report Null",lr.getCurrentReport());
    }

    private jmri.jmrix.loconet.LocoNetInterfaceScaffold tc;

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        tc = new jmri.jmrix.loconet.LocoNetInterfaceScaffold();
        TranspondingTagManager t = new TranspondingTagManager(); // this class registers itself.
        Assertions.assertNotNull(t);
        r = new LnReporter(3, tc, "L");
    }

    @AfterEach
    @Override
    public void tearDown() {
        r = null;
        tc = null;
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }

}
