package jmri.jmrix.openlcb;

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
        // Upon construction, a consumer range identified message was sent out.
        ti.assertSentMessage(":X194a4c4cN010203040506ffff;");
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
        ((OlcbReporter) r2).finishLoad();

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
        ((OlcbReporter) r2).finishLoad();

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
        ((OlcbReporter) r2).finishLoad();
        ti.sendMessage(":X195B4123N010203040507C0C8;");
        ti.flush();

        // Now 300 exits r
        Message m = new ProducerIdentifiedMessage(ti.iface.getNodeId(), new EventID("01.02.03.04.05.06.C1.2C"), EventState.Invalid);
        ti.iface.getOutputConnection().put(m, null);
        ti.flush();

        // r should fall back to 100 because 100 still has whereLastSeen == r, whereas 200 was seen at r2
        Assert.assertEquals("RD100", r.getCurrentReport().toString());
        Assert.assertEquals(2, ((OlcbReporter) r).getCollection().size());
    }

    @Test
    public void testBoosterAndLocalDetector() {
        var rman = ti.configurationManager.getReporterManager();
        var localReporter = rman.provideReporter("01.02.03.04.05.07.00.00");
        ((OlcbReporter) localReporter).finishLoad();

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
        ((OlcbReporter) r).finishLoad();
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
