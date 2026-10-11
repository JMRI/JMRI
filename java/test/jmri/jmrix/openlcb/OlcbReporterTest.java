package jmri.jmrix.openlcb;

import jmri.DccLocoAddress;
import jmri.IdTag;
import jmri.InstanceManager;
import jmri.RailCom;
import jmri.RailComManager;
import jmri.util.JUnitUtil;
import jmri.util.PropertyChangeListenerScaffold;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openlcb.EventID;
import org.openlcb.EventState;
import org.openlcb.Message;
import org.openlcb.ProducerIdentifiedMessage;
import org.openlcb.implementations.EventTable;

import java.util.regex.Pattern;

/**
 *
 * @author Bob Jacobsen Coyright (C) 2023
 * @author Balazs Racz Coyright (C) 2023
 */
public class OlcbReporterTest extends jmri.implementation.AbstractReporterTestBase {

    OlcbTestInterface ti;
    PropertyChangeListenerScaffold l;

    // Helper method for base class tests.
    @Override
    protected Object generateObjectToReport() {
        return InstanceManager.getDefault(RailComManager.class).provideIdTag("123");
    }

    @Test
    public void testPacketReceived() {
        // Entry.
        ti.sendMessage(":X195B4123N010203040506C100;");
        ti.flush();
        Assert.assertEquals("Report mismatch","RD256",r.getCurrentReport().toString());
        RailCom report = (RailCom) r.getCurrentReport();
        Assert.assertNotNull("Object type mismatch", report);
        Assert.assertEquals("Loco address mismatch",256, report.getLocoAddress().getNumber());
        Assert.assertEquals("Orientation mismatch", RailCom.Orientation.UNKNOWN, report.getOrientation());
        Assert.assertEquals("Direction should not be set", RailCom.Direction.UNKNOWN, report.getDirection());

        // Exit.
        Message m = new ProducerIdentifiedMessage(ti.iface.getNodeId(), new EventID("01.02.03.04.05.06.C1.00"), EventState.Invalid);
        ti.iface.getOutputConnection().put(m, null);
        ti.flush();

        Assert.assertNull("Report should have disappeared", r.getCurrentReport());
    }

    @Test
    public void testOrientationForward() {
        // FORWARD entry (0x4100 -> bit 14 set, address 256)
        ti.sendMessage(":X195B4123N0102030405064100;");
        ti.flush();
        Assert.assertEquals("Report mismatch", "RD256", r.getCurrentReport().toString());
        RailCom report = (RailCom) r.getCurrentReport();
        Assert.assertNotNull("Object type mismatch", report);
        Assert.assertEquals("Loco address mismatch", 256, report.getLocoAddress().getNumber());
        Assert.assertEquals("Orientation mismatch", RailCom.Orientation.WEST, report.getOrientation());
        Assert.assertEquals("Direction should not be set", RailCom.Direction.UNKNOWN, report.getDirection());
    }

    @Test
    public void testOrientationReverse() {
        // REVERSE entry (0x8100 -> bit 15 set, address 256)
        ti.sendMessage(":X195B4123N0102030405068100;");
        ti.flush();
        Assert.assertEquals("Report mismatch", "RD256", r.getCurrentReport().toString());
        RailCom report = (RailCom) r.getCurrentReport();
        Assert.assertNotNull("Object type mismatch", report);
        Assert.assertEquals("Loco address mismatch", 256, report.getLocoAddress().getNumber());
        Assert.assertEquals("Orientation mismatch", RailCom.Orientation.EAST, report.getOrientation());
        Assert.assertEquals("Direction should not be set", RailCom.Direction.UNKNOWN, report.getDirection());
    }

    @Test
    public void testEventTable() {
        EventTable.EventTableEntry[] elist = ti.iface.getEventTable()
                .getEventInfo(new EventID("1.2.3.4.5.6.00.00")).getAllEntries();

        Assert.assertEquals(1, elist.length);
        Assert.assertTrue("Incorrect name: " + elist[0].getDescription(),
                Pattern.compile("Reporter.*Report").matcher(elist[0].getDescription()).matches());

        r.setUserName("MyInput");

        elist = ti.iface.getEventTable()
                .getEventInfo(new EventID("1.2.3.4.5.6.00.00")).getAllEntries();

        Assert.assertEquals(1, elist.length);
        Assert.assertEquals("Reporter MyInput Report", elist[0].getDescription());

        r.setUserName("Changed");

        Assert.assertEquals("Reporter Changed Report", elist[0].getDescription());
    }

    @Test
    public void testIdentified() {
        // Upon construction, a consumer range identified message and an identify producers message were sent out.
        ti.assertSentMessages(":X194a4c4cN010203040506ffff;", ":X19914c4cN0102030405060000;");
        ti.assertNoSentMessages();
    }

    @Test
    public void testAccumulationAfterMoveToAnother() {
        // 256 enters
        ti.sendMessage(":X195B4123N010203040506C100;");
        ti.flush();
        Assert.assertEquals("Report mismatch","RD256",r.getCurrentReport().toString());
        RailCom report = (RailCom) r.getCurrentReport();
        Assert.assertNotNull("Object type mismatch", report);
        Assert.assertEquals("Loco address mismatch",256, report.getLocoAddress().getNumber());

        Assert.assertEquals("expect 1 in reporter", 1, ((OlcbReporter)r).getCollection().size());

        // create another reporter and send 256 to it
        var rman = ti.configurationManager.getReporterManager();
        var r2 = rman.provideReporter("01.02.03.04.05.07.00.00");

        ti.sendMessage(":X195B4123N010203040507C100;");
        ti.flush();

        // Moving to another reporter does not remove 256 from r's collection since OpenLCB has explicit exit events
        Assert.assertEquals("expect 1 in reporter", 1, ((OlcbReporter)r).getCollection().size());
        Assert.assertEquals("expect 1 in r2", 1, ((OlcbReporter)r2).getCollection().size());

        // Explicit exit from r removes 256 from r
        Message m = new ProducerIdentifiedMessage(ti.iface.getNodeId(), new EventID("01.02.03.04.05.06.C1.00"), EventState.Invalid);
        ti.iface.getOutputConnection().put(m, null);
        ti.flush();

        Assert.assertEquals("expect 0 in reporter", 0, ((OlcbReporter)r).getCollection().size());
        Assert.assertNull("Report should have cleared on exit", r.getCurrentReport());
        // r2 still holds 256
        Assert.assertEquals("expect 1 in r2", 1, ((OlcbReporter)r2).getCollection().size());
        Assert.assertNotNull("r2 should still report 256", r2.getCurrentReport());
    }

    @Test
    public void testBridgingBlocks() {
        // Loco 256 enters reporter 1
        ti.sendMessage(":X195B4123N010203040506C100;");
        ti.flush();
        Assert.assertEquals("RD256", r.getCurrentReport().toString());
        Assert.assertEquals(1, ((OlcbReporter) r).getCollection().size());

        // Loco 256 enters reporter 2 (bridging both blocks)
        var rman = ti.configurationManager.getReporterManager();
        var r2 = rman.provideReporter("01.02.03.04.05.07.00.00");

        ti.sendMessage(":X195B4123N010203040507C100;");
        ti.flush();

        // Both reporters hold loco 256 simultaneously
        Assert.assertEquals(1, ((OlcbReporter) r).getCollection().size());
        Assert.assertEquals(1, ((OlcbReporter) r2).getCollection().size());
        Assert.assertEquals("RD256", r.getCurrentReport().toString());
        Assert.assertEquals("RD256", r2.getCurrentReport().toString());

        // Exit from reporter 1
        Message m = new ProducerIdentifiedMessage(ti.iface.getNodeId(), new EventID("01.02.03.04.05.06.C1.00"), EventState.Invalid);
        ti.iface.getOutputConnection().put(m, null);
        ti.flush();

        Assert.assertEquals(0, ((OlcbReporter) r).getCollection().size());
        Assert.assertNull(r.getCurrentReport());
        Assert.assertEquals(1, ((OlcbReporter) r2).getCollection().size());
        Assert.assertEquals("RD256", r2.getCurrentReport().toString());
    }

    @Test
    public void testMultipleLocosOrderedFallback() {
        // Loco 256 enters r
        ti.sendMessage(":X195B4123N010203040506C100;");
        ti.flush();
        Assert.assertEquals("RD256", r.getCurrentReport().toString());

        // Loco 257 enters r
        ti.sendMessage(":X195B4123N010203040506C101;");
        ti.flush();
        Assert.assertEquals("RD257", r.getCurrentReport().toString());
        Assert.assertEquals(2, ((OlcbReporter) r).getCollection().size());

        // Exit loco 257
        Message m = new ProducerIdentifiedMessage(ti.iface.getNodeId(), new EventID("01.02.03.04.05.06.C1.01"), EventState.Invalid);
        ti.iface.getOutputConnection().put(m, null);
        ti.flush();

        // Collection has 1 left, and report falls back to 256
        Assert.assertEquals(1, ((OlcbReporter) r).getCollection().size());
        Assert.assertEquals("RD256", r.getCurrentReport().toString());
    }

    @Test
    public void testFallbackWhereLastSeen() {
        // 100 enters r
        ti.sendMessage(":X195B4123N010203040506C064;"); // 0x64 = 100
        ti.flush();
        // 200 enters r
        ti.sendMessage(":X195B4123N010203040506C0C8;"); // 0xc8 = 200
        ti.flush();
        // 300 enters r
        ti.sendMessage(":X195B4123N010203040506C12C;"); // 0x12c = 300
        ti.flush();
        Assert.assertEquals("RD300", r.getCurrentReport().toString());
        Assert.assertEquals(3, ((OlcbReporter) r).getCollection().size());

        // 200 is seen at r2 (whereLastSeen becomes r2)
        var rman = ti.configurationManager.getReporterManager();
        var r2 = rman.provideReporter("01.02.03.04.05.07.00.00");
        ti.sendMessage(":X195B4123N010203040507C0C8;");
        ti.flush();

        // Now 300 exits r
        Message m = new ProducerIdentifiedMessage(ti.iface.getNodeId(), new EventID("01.02.03.04.05.06.C1.2C"), EventState.Invalid);
        ti.iface.getOutputConnection().put(m, null);
        ti.flush();

        // r should fall back to 100 because 100 still has whereLastSeen == r, whereas 200 was seen at r2
        Assert.assertEquals("RD100", r.getCurrentReport().toString());
        Assert.assertEquals(2, ((OlcbReporter) r).getCollection().size());

        // Now 100 also exits r, leaving only 200 (which was seen at r2)
        Message m2 = new ProducerIdentifiedMessage(ti.iface.getNodeId(), new EventID("01.02.03.04.05.06.C0.64"), EventState.Invalid);
        ti.iface.getOutputConnection().put(m2, null);
        ti.flush();

        // r should fall back to reporting 200, but 200's whereLastSeen must remain r2
        Assert.assertEquals("RD200", r.getCurrentReport().toString());
        Assert.assertEquals(1, ((OlcbReporter) r).getCollection().size());
        RailCom report200 = (RailCom) r.getCurrentReport();
        Assert.assertEquals(r2, report200.getWhereLastSeen());
    }

    @Test
    public void testBoosterAndLocalDetector() {
        var rman = ti.configurationManager.getReporterManager();
        var localReporter = rman.provideReporter("01.02.03.04.05.07.00.00");

        // Global booster detector (r) receives 256
        ti.sendMessage(":X195B4123N010203040506C100;");
        ti.flush();

        // Local block detector receives 256
        ti.sendMessage(":X195B4123N010203040507C100;");
        ti.flush();

        // Both reporters simultaneously report loco 256 in collection and non-empty current report
        Assert.assertEquals(1, ((OlcbReporter) r).getCollection().size());
        Assert.assertEquals(1, ((OlcbReporter) localReporter).getCollection().size());
        Assert.assertEquals("RD256", r.getCurrentReport().toString());
        Assert.assertEquals("RD256", localReporter.getCurrentReport().toString());
    }

    @Test
    public void testAccumulationAfterRecevice0S() {
        // 256 enters
        ti.sendMessage(":X195B4123N010203040506C100;");
        ti.flush();
        Assert.assertEquals("Report mismatch","RD256",r.getCurrentReport().toString());
        RailCom report = (RailCom) r.getCurrentReport();
        Assert.assertNotNull("Object type mismatch", report);
        Assert.assertEquals("Loco address mismatch",256, report.getLocoAddress().getNumber());

        Assert.assertEquals("expect 1 in reporter", 1, ((OlcbReporter)r).getCollection().size());

        // 257 enters
        ti.sendMessage(":X195B4123N010203040506C101;");
        ti.flush();
        Assert.assertEquals("Report mismatch","RD257",r.getCurrentReport().toString());
        report = (RailCom) r.getCurrentReport();
        Assert.assertNotNull("Object type mismatch", report);
        Assert.assertEquals("Loco address mismatch",257, report.getLocoAddress().getNumber());

        Assert.assertEquals("expect 2 in reporter", 2, ((OlcbReporter)r).getCollection().size());

        // 0x3800 unknown enters
        ti.sendMessage(":X195B4123N010203040506F800;");
        ti.flush();
        RailCom report2 = (RailCom) r.getCurrentReport();
        Assert.assertNull("Expected no report", report2);
        
        Assert.assertEquals("expect 0 in reporter", 0, ((OlcbReporter)r).getCollection().size());
        
    }

    @Test
    public void testMetadataUpdateNotification() {
        java.util.List<java.beans.PropertyChangeEvent> events = new java.util.ArrayList<>();
        r.addPropertyChangeListener(events::add);

        // 256 enters with UNKNOWN orientation
        ti.sendMessage(":X195B4123N010203040506C100;");
        ti.flush();
        Assert.assertEquals(1, ((OlcbReporter) r).getCollection().size());
        // Verify PROPERTY_REPORT_METADATA was fired on initial string creation
        Assert.assertTrue(events.stream().anyMatch(e -> jmri.Reporter.PROPERTY_REPORT_METADATA.equals(e.getPropertyName())));
        Assert.assertTrue(events.stream().anyMatch(e -> jmri.Reporter.PROPERTY_CURRENT_REPORT.equals(e.getPropertyName())));

        events.clear();
        // 256 updates orientation to WEST
        ti.sendMessage(":X195B4123N0102030405064100;");
        ti.flush();
        // Because 256 was already currentReport, currentReport does not fire, but metadata update MUST fire
        Assert.assertTrue("Metadata update should fire on orientation change",
                events.stream().anyMatch(e -> jmri.Reporter.PROPERTY_REPORT_METADATA.equals(e.getPropertyName())));

        events.clear();
        // 256 sends repeat packet with same orientation (WEST)
        ti.sendMessage(":X195B4123N0102030405064100;");
        ti.flush();
        // No metadata change, so metadata update should NOT fire
        Assert.assertFalse("Metadata update should not fire when string rendering is unchanged",
                events.stream().anyMatch(e -> jmri.Reporter.PROPERTY_REPORT_METADATA.equals(e.getPropertyName())));
    }

    @Test
    public void testCollectionNotificationOnNonCurrentExit() {
        java.util.List<java.beans.PropertyChangeEvent> events = new java.util.ArrayList<>();

        // 256 enters
        ti.sendMessage(":X195B4123N010203040506C100;");
        ti.flush();

        // 257 enters (now currentReport is 257)
        ti.sendMessage(":X195B4123N010203040506C101;");
        ti.flush();
        Assert.assertEquals("RD257", r.getCurrentReport().toString());
        Assert.assertEquals(2, ((OlcbReporter) r).getCollection().size());

        r.addPropertyChangeListener(events::add);

        // 256 exits (256 was NOT currentReport, 257 was)
        Message m = new ProducerIdentifiedMessage(ti.iface.getNodeId(), new EventID("01.02.03.04.05.06.C1.00"), EventState.Invalid);
        ti.iface.getOutputConnection().put(m, null);
        ti.flush();

        Assert.assertEquals(1, ((OlcbReporter) r).getCollection().size());
        Assert.assertEquals("RD257", r.getCurrentReport().toString());
        // Collection update MUST have fired even though currentReport didn't change
        Assert.assertTrue("Collection update must fire when non-current loco exits",
                events.stream().anyMatch(e -> jmri.Reporter.PROPERTY_COLLECTION.equals(e.getPropertyName())));
        // Verify oldValue is null as required by specification
        java.beans.PropertyChangeEvent collEvt = events.stream()
                .filter(e -> jmri.Reporter.PROPERTY_COLLECTION.equals(e.getPropertyName()))
                .findFirst().orElseThrow();
        Assert.assertNull(collEvt.getOldValue());
        Assert.assertNotNull(collEvt.getNewValue());
    }

    @Test
    public void testInitializationFromQuery() {
        ti.clearSentMessages();
        var rman = ti.configurationManager.getReporterManager();
        var r2 = rman.provideReporter("01.02.03.04.05.08.00.00");
        ti.assertSentMessages(":X194a4c4cN010203040508ffff;", ":X19914c4cN0102030405080000;");
        ti.assertNoSentMessages();

        Assert.assertNull("Reporter should have no report yet", r2.getCurrentReport());

        // Layout node replies with ProducerIdentified(Valid) for loco 256
        ti.sendMessage(":X19544123N010203040508C100;");
        ti.flush();

        Assert.assertNotNull("Report should be populated from query reply", r2.getCurrentReport());
        Assert.assertEquals("RD256", r2.getCurrentReport().toString());
        RailCom report = (RailCom) r2.getCurrentReport();
        Assert.assertEquals(256, report.getLocoAddress().getNumber());
        Assert.assertEquals(IdTag.SEEN, r2.getState());
        Assert.assertEquals(1, ((OlcbReporter) r2).getCollection().size());
    }

    @Test
    public void testInitializationFromQueryUnoccupied() {
        ti.clearSentMessages();
        var rman = ti.configurationManager.getReporterManager();
        var r2 = rman.provideReporter("01.02.03.04.05.08.00.00");
        ti.assertSentMessages(":X194a4c4cN010203040508ffff;", ":X19914c4cN0102030405080000;");
        ti.assertNoSentMessages();

        // Layout replies with unoccupied report (address 0x3800, unknown entry bits 0xC000 -> 0xF800)
        ti.sendMessage(":X19544123N010203040508F800;");
        ti.flush();

        Assert.assertNull("Reporter should remain empty on unoccupied report", r2.getCurrentReport());
        Assert.assertEquals(IdTag.UNSEEN, r2.getState());
        Assert.assertEquals(0, ((OlcbReporter) r2).getCollection().size());
    }

    @Test
    public void testInitializationFromQueryMultipleLocos() {
        ti.clearSentMessages();
        var rman = ti.configurationManager.getReporterManager();
        var r2 = rman.provideReporter("01.02.03.04.05.08.00.00");
        ti.assertSentMessages(":X194a4c4cN010203040508ffff;", ":X19914c4cN0102030405080000;");
        ti.assertNoSentMessages();

        // Layout replies with loco 256 and loco 257 present in block
        ti.sendMessage(":X19544123N010203040508C100;");
        ti.flush();
        ti.sendMessage(":X19544123N010203040508C101;");
        ti.flush();
        // Layout also sends message with 0000 suffix and Producer Identified Valid (address 0 not in the block)
        ti.sendMessage(":X19544123N0102030405080000;");
        ti.flush();

        Assert.assertEquals("RD257", r2.getCurrentReport().toString());
        Assert.assertEquals(IdTag.SEEN, r2.getState());
        var coll = ((OlcbReporter) r2).getCollection();
        Assert.assertEquals(2, coll.size());
        RailCom tag256 = (RailCom) InstanceManager.getDefault(RailComManager.class).provideIdTag("256");
        RailCom tag257 = (RailCom) InstanceManager.getDefault(RailComManager.class).provideIdTag("257");
        Assert.assertEquals(java.util.List.of(tag256, tag257), new java.util.ArrayList<>(coll));
        Assert.assertTrue(coll.contains(tag256));
        Assert.assertTrue(coll.contains(tag257));
    }

    @Test
    public void testInitializationFromQueryLocoZero() {
        ti.clearSentMessages();
        var rman = ti.configurationManager.getReporterManager();
        var r2 = rman.provideReporter("01.02.03.04.05.08.00.00");
        ti.assertSentMessages(":X194a4c4cN010203040508ffff;", ":X19914c4cN0102030405080000;");
        ti.assertNoSentMessages();

        // Hardware sends producer identified valid for address 0 (unknown direction: 0xC000)
        ti.sendMessage(":X19544123N010203040508C000;");
        ti.flush();
        // Hardware sends producer identified invalid for suffix 0000 (address 0 has not exited)
        ti.sendMessage(":X19545123N0102030405080000;");
        ti.flush();

        Assert.assertNotNull("Report should be populated for loco 0", r2.getCurrentReport());
        Assert.assertEquals("RD0", r2.getCurrentReport().toString());
        RailCom report = (RailCom) r2.getCurrentReport();
        Assert.assertEquals(0, report.getLocoAddress().getNumber());
        Assert.assertEquals(DccLocoAddress.Protocol.DCC_LONG, report.getDccAddress().getProtocol());
        Assert.assertEquals(IdTag.SEEN, r2.getState());
        var coll = ((OlcbReporter) r2).getCollection();
        Assert.assertEquals(1, coll.size());
        RailCom tag0 = (RailCom) InstanceManager.getDefault(RailComManager.class).provideIdTag("0");
        Assert.assertEquals(java.util.List.of(tag0), new java.util.ArrayList<>(coll));
        Assert.assertTrue(coll.contains(tag0));
    }

    @Test
    public void testProducerIdentifiedUnknownNoExitEvents() {
        ti.clearSentMessages();
        // Detectors that do not produce Exit events reply with Producer Identified Unknown.
        // Send Producer Identified Unknown (MTI 0x547) for exit event of loco 256.
        ti.sendMessage(":X19547123N0102030405060100;");
        ti.flush();
        Assert.assertNull(r.getCurrentReport());
        Assert.assertEquals(-1, r.getState());
        Assert.assertEquals(0, ((OlcbReporter) r).getCollection().size());

        // Now put loco 256 in the block
        ti.sendMessage(":X19544123N010203040506C100;");
        ti.flush();
        Assert.assertEquals("RD256", r.getCurrentReport().toString());
        Assert.assertEquals(1, ((OlcbReporter) r).getCollection().size());

        // Detector replies with Producer Identified Unknown for exit of loco 256
        ti.sendMessage(":X19547123N0102030405060100;");
        ti.flush();
        // Reporter should ignore Unknown state; loco 256 should not be removed
        Assert.assertEquals("RD256", r.getCurrentReport().toString());
        Assert.assertEquals(IdTag.SEEN, r.getState());
        Assert.assertEquals(1, ((OlcbReporter) r).getCollection().size());
    }

    @Test
    public void testProducerIdentifiedUnknownNoOrientation() {
        ti.clearSentMessages();
        // Put loco 256 into the block with Unknown orientation
        ti.sendMessage(":X19544123N010203040506C100;");
        ti.flush();
        RailCom report = (RailCom) r.getCurrentReport();
        Assert.assertEquals(RailCom.Orientation.UNKNOWN, report.getOrientation());

        // Detectors that do not detect orientation reply with Producer Identified Unknown
        // when queried with definite orientation (West: 0x4100, East: 0x8100)
        ti.sendMessage(":X19547123N0102030405064100;");
        ti.flush();
        ti.sendMessage(":X19547123N0102030405068100;");
        ti.flush();

        // Reporter should ignore Unknown responses; loco 256 remains with unchanged orientation
        Assert.assertEquals("RD256", r.getCurrentReport().toString());
        Assert.assertEquals(RailCom.Orientation.UNKNOWN, report.getOrientation());
        Assert.assertEquals(1, ((OlcbReporter) r).getCollection().size());
    }

    @Test
    public void testProducerIdentifiedEntryOrientationUnknown() {
        ti.clearSentMessages();
        // Locomotive Entry with Orientation Unknown Event ID (0xC000 | addr):
        // Producer Identified Valid iff the locomotive is currently in the block.
        ti.sendMessage(":X19544123N010203040506C100;");
        ti.flush();
        Assert.assertEquals("RD256", r.getCurrentReport().toString());
        RailCom report = (RailCom) r.getCurrentReport();
        Assert.assertEquals(256, report.getLocoAddress().getNumber());
        Assert.assertEquals(RailCom.Orientation.UNKNOWN, report.getOrientation());
        Assert.assertEquals(IdTag.SEEN, r.getState());
        Assert.assertEquals(1, ((OlcbReporter) r).getCollection().size());

        // Producer Identified Invalid iff the locomotive is currently NOT in the block.
        ti.sendMessage(":X19545123N010203040506C100;");
        ti.flush();
        Assert.assertNull(r.getCurrentReport());
        Assert.assertEquals(IdTag.UNSEEN, r.getState());
        Assert.assertEquals(0, ((OlcbReporter) r).getCollection().size());

        // Receiving Invalid for an absent locomotive should be a no-op
        ti.sendMessage(":X19545123N010203040506C105;");
        ti.flush();
        Assert.assertNull(r.getCurrentReport());
        Assert.assertEquals(0, ((OlcbReporter) r).getCollection().size());
    }

    @Test
    public void testProducerIdentifiedExitEvent() {
        ti.clearSentMessages();
        // Put loco 256 in block first
        ti.sendMessage(":X19544123N010203040506C100;");
        ti.flush();
        Assert.assertEquals(1, ((OlcbReporter) r).getCollection().size());

        // Exit Event ID (0x0000 | addr):
        // Producer Identified Invalid means locomotive IS in the block (exit is false).
        ti.sendMessage(":X19545123N0102030405060100;");
        ti.flush();
        Assert.assertEquals("RD256", r.getCurrentReport().toString());
        Assert.assertEquals(1, ((OlcbReporter) r).getCollection().size());

        // Producer Identified Valid means locomotive is NOT in the block (has exited).
        ti.sendMessage(":X19544123N0102030405060100;");
        ti.flush();
        Assert.assertNull(r.getCurrentReport());
        Assert.assertEquals(IdTag.UNSEEN, r.getState());
        Assert.assertEquals(0, ((OlcbReporter) r).getCollection().size());
    }

    @Test
    public void testProducerIdentifiedEntryOrientationWest() {
        ti.clearSentMessages();
        // Entry Orientation West (0x4000 | addr):
        // Producer Identified Valid iff locomotive is in the block with orientation West.
        ti.sendMessage(":X19544123N0102030405064100;");
        ti.flush();
        Assert.assertEquals("RD256", r.getCurrentReport().toString());
        RailCom report = (RailCom) r.getCurrentReport();
        Assert.assertEquals(RailCom.Orientation.WEST, report.getOrientation());
        Assert.assertEquals(IdTag.SEEN, r.getState());
        Assert.assertEquals(1, ((OlcbReporter) r).getCollection().size());

        // Producer Identified Invalid if locomotive does not match orientation West (e.g. absent or East).
        // Invalid does not mean the locomotive is absent from the block, so it is not removed.
        ti.sendMessage(":X19545123N0102030405064100;");
        ti.flush();
        Assert.assertEquals("RD256", r.getCurrentReport().toString());
        Assert.assertEquals(1, ((OlcbReporter) r).getCollection().size());
    }

    @Test
    public void testProducerIdentifiedEntryOrientationEast() {
        ti.clearSentMessages();
        // Entry Orientation East (0x8000 | addr):
        // Producer Identified Valid iff locomotive is in the block with orientation East.
        ti.sendMessage(":X19544123N0102030405068100;");
        ti.flush();
        Assert.assertEquals("RD256", r.getCurrentReport().toString());
        RailCom report = (RailCom) r.getCurrentReport();
        Assert.assertEquals(RailCom.Orientation.EAST, report.getOrientation());
        Assert.assertEquals(IdTag.SEEN, r.getState());
        Assert.assertEquals(1, ((OlcbReporter) r).getCollection().size());

        // Producer Identified Invalid does not remove locomotive
        ti.sendMessage(":X19545123N0102030405068100;");
        ti.flush();
        Assert.assertEquals("RD256", r.getCurrentReport().toString());
        Assert.assertEquals(1, ((OlcbReporter) r).getCollection().size());
    }

    @Test
    public void testProducerIdentifiedAddressZeroAllCases() {
        ti.clearSentMessages();
        // Address 0 entry West (0x4000)
        ti.sendMessage(":X19544123N0102030405064000;");
        ti.flush();
        Assert.assertEquals("RD0", r.getCurrentReport().toString());
        RailCom report = (RailCom) r.getCurrentReport();
        Assert.assertEquals(0, report.getLocoAddress().getNumber());
        Assert.assertEquals(DccLocoAddress.Protocol.DCC_LONG, report.getDccAddress().getProtocol());
        Assert.assertEquals(RailCom.Orientation.WEST, report.getOrientation());

        // Address 0 entry East (0x8000)
        ti.sendMessage(":X19544123N0102030405068000;");
        ti.flush();
        report = (RailCom) r.getCurrentReport();
        Assert.assertEquals(RailCom.Orientation.EAST, report.getOrientation());

        // Address 0 exit Invalid (0x0000) -> loco 0 is still in block
        ti.sendMessage(":X19545123N0102030405060000;");
        ti.flush();
        Assert.assertEquals("RD0", r.getCurrentReport().toString());
        Assert.assertEquals(1, ((OlcbReporter) r).getCollection().size());

        // Address 0 exit Valid (0x0000) -> loco 0 has exited
        ti.sendMessage(":X19544123N0102030405060000;");
        ti.flush();
        Assert.assertNull(r.getCurrentReport());
        Assert.assertEquals(0, ((OlcbReporter) r).getCollection().size());

        // Address 0 entry Unknown (0xC000)
        ti.sendMessage(":X19544123N010203040506C000;");
        ti.flush();
        Assert.assertEquals("RD0", r.getCurrentReport().toString());
        report = (RailCom) r.getCurrentReport();
        Assert.assertEquals(RailCom.Orientation.UNKNOWN, report.getOrientation());

        // Address 0 entry Unknown Invalid (0xC000) -> loco 0 not in block
        ti.sendMessage(":X19545123N010203040506C000;");
        ti.flush();
        Assert.assertNull(r.getCurrentReport());
        Assert.assertEquals(0, ((OlcbReporter) r).getCollection().size());
    }

    @Test
    public void testProducerIdentifiedEnumerationAndSuffix0000() {
        ti.clearSentMessages();
        // Hardware responds to query with:
        // 1. Loco 256 West
        ti.sendMessage(":X19544123N0102030405064100;");
        ti.flush();
        // 2. Loco 257 East
        ti.sendMessage(":X19544123N0102030405068101;");
        ti.flush();
        // 3. Suffix 0000 Valid (Loco 0 not in block)
        ti.sendMessage(":X19544123N0102030405060000;");
        ti.flush();
        // 4. Producer Range Identified for 64k range (MTI 0x524)
        ti.sendMessage(":X19524123N010203040506ffff;");
        ti.flush();

        Assert.assertEquals(2, ((OlcbReporter) r).getCollection().size());
        RailCom tag256 = (RailCom) InstanceManager.getDefault(RailComManager.class).provideIdTag("256");
        RailCom tag257 = (RailCom) InstanceManager.getDefault(RailComManager.class).provideIdTag("257");
        Assert.assertEquals(RailCom.Orientation.WEST, tag256.getOrientation());
        Assert.assertEquals(RailCom.Orientation.EAST, tag257.getOrientation());

        // Clear reporter with unoccupied block message (0xF800 Valid)
        ti.sendMessage(":X19544123N010203040506F800;");
        ti.flush();
        Assert.assertNull(r.getCurrentReport());
        Assert.assertEquals(0, ((OlcbReporter) r).getCollection().size());

        // Now test where loco 0 is in the block:
        // 1. Loco 0 West
        ti.sendMessage(":X19544123N0102030405064000;");
        ti.flush();
        // 2. Suffix 0000 Invalid (Loco 0 has not exited)
        ti.sendMessage(":X19545123N0102030405060000;");
        ti.flush();
        // 3. Producer Range Identified (MTI 0x524)
        ti.sendMessage(":X19524123N010203040506ffff;");
        ti.flush();

        Assert.assertEquals("RD0", r.getCurrentReport().toString());
        Assert.assertEquals(1, ((OlcbReporter) r).getCollection().size());
        RailCom tag0 = (RailCom) InstanceManager.getDefault(RailComManager.class).provideIdTag("0");
        Assert.assertEquals(RailCom.Orientation.WEST, tag0.getOrientation());
    }


    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initDefaultUserMessagePreferences();
        l = new PropertyChangeListenerScaffold();
        // prepare an interface
        ti = new OlcbTestInterface(new OlcbTestInterface.CreateConfigurationManager());
        ti.waitForStartup();
        var rman = ti.configurationManager.getReporterManager();
        r = rman.provideReporter("01.02.03.04.05.06.00.00");
    }

    @Override
    @AfterEach
    public void tearDown() {
        InstanceManager.getDefault(RailComManager.class).dispose();
        r.dispose();
        r = null;
        l.resetPropertyChanged();
        l = null;
        ti.dispose();
        ti = null;
        JUnitUtil.clearShutDownManager();
        JUnitUtil.tearDown();
    }

    // private static final Logger log = LoggerFactory.getLogger(OlcbReporterTest.class);

}
