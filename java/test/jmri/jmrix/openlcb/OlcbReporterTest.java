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
        ti.sendMessage(":X195B4123N0102030405060100;");
        ti.flush();
        Assert.assertEquals("Report mismatch","RD256",r.getCurrentReport().toString());
        RailCom report = (RailCom) r.getCurrentReport();
        Assert.assertNotNull("Object type mismatch", report);
        Assert.assertEquals("Loco address mismatch",256, report.getLocoAddress().getNumber());

        // Exit.
        Message m = new ProducerIdentifiedMessage(ti.iface.getNodeId(), new EventID("01.02.03.04.05.06.C1.00"), EventState.Invalid);
        ti.iface.getOutputConnection().put(m, null);
        ti.flush();

        Assert.assertNull("Report should have disappeared", r.getCurrentReport());
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

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initDefaultUserMessagePreferences();
        l = new PropertyChangeListenerScaffold();
        // prepare an interface
        ti = new OlcbTestInterface();
        ti.waitForStartup();
        r = new OlcbReporter("M", "1.2.3.4.5.6.00.00", ti.iface);
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

    // private final static Logger log = LoggerFactory.getLogger(OlcbReporterTest.class);

}
