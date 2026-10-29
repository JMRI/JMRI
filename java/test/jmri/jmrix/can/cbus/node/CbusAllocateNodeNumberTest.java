package jmri.jmrix.can.cbus.node;

import java.io.IOException;
import java.io.File;
import java.util.concurrent.atomic.AtomicReference;

import jmri.jmrix.can.*;
import jmri.jmrix.can.cbus.*;
import jmri.jmrix.can.cbus.simulator.moduletypes.MergCanpan;

import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;
import jmri.util.swing.JemmyUtil;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import org.netbeans.jemmy.operators.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Steve Young Copyright (C) 2019
 */
public class CbusAllocateNodeNumberTest {

    @Test
    public void testCTor() {

        assertEquals(2, tcis.numListeners(),"2 listener " + tcis.getListeners());

        CbusAllocateNodeNumber t = new CbusAllocateNodeNumber(memo,nodeModel);
        assertNotNull(t);
        assertEquals(3, tcis.numListeners(),"3 listeners " + tcis.getListeners());

        t.dispose();
        assertEquals(2, tcis.numListeners(),"2 listener after dispose " + tcis.getListeners());

    }

    @Test
    @DisabledIfHeadless
    public void testDisplayDialogue() {
        memo.get(CbusPreferences.class).setAddNodes(false);
        CbusAllocateNodeNumber t = new CbusAllocateNodeNumber(memo,nodeModel);
        assertNotNull(t);

        CbusNode node789 = nodeModel.provideNodeByNodeNum(789);
        assertNotNull(node789);
        // configure dummy node as CANPAN so no parameters are requested for it
        new MergCanpan().configureDummyNode(node789);

        // operate the dialog box opened later
        DialogueThread dialog_thread = new DialogueThread("CbusAllocateNodeNumber Dialog Close Thread", () -> {
            // constructor for jdo will wait until the dialog is visible
            JDialogOperator jdo = new JDialogOperator(Bundle.getMessage("NdEntrNumTitle",String.valueOf(1234)));

            JLabelOperator labelOper = new JLabelOperator(jdo, Bundle.getMessage("NdRqNdDetails"));
            assertNotNull(labelOper);

            JUnitUtil.waitFor(()->{ return( tcis.outbound.size() == 1); }, "1 outbound " + tcis.getTranslatedOutbound());
            assertEquals("[5f8] 10", tcis.outbound.elementAt(0).toString(),"Request node parameters(RQNP)");

            CanReply rs = new CanReply();
            rs.setNumDataElements(7);
            rs.setElement(0, CbusConstants.CBUS_PARAMS); // Node sends Parameters
            rs.setElement(1, 0x01); // param 1 - The manufacturer ID as a HEX numeric
            rs.setElement(2, 0x02); // param 2 - Minor code version
            rs.setElement(3, 0x03); // param 3 -
            rs.setElement(4, 0x04); // param 4 -
            rs.setElement(5, 0x05); // param 5 -
            rs.setElement(6, 0x06); // param 6 -
            rs.setElement(7, 0x07); // param 7 -
            assertNotNull(t);
            t.reply(rs);

            JLabelOperator labelOper2 = new JLabelOperator(jdo, CbusNodeConstants.getManu(1));
            assertNotNull(labelOper2);

            JUnitUtil.waitFor(()->{ return( tcis.outbound.size() == 2); }, "2 outbound " + tcis.getTranslatedOutbound());
            assertEquals("[5f8] 11", tcis.outbound.elementAt(1).toString(),"Request module name (RQMN)");

            CanReply rsn = new CanReply();
            rsn.setNumDataElements(7);
            rsn.setElement(0, CbusConstants.CBUS_NAME); // Node sends Name
            rsn.setElement(1, 67); // C
            rsn.setElement(2, 65); // A
            rsn.setElement(3, 84); // T
            rsn.setElement(4, 70); // F
            rsn.setElement(5, 76); // L
            rsn.setElement(6, 65); // A
            rsn.setElement(7, 80); // P
            assertNotNull(t);
            t.reply(rsn);

            JLabelOperator labelOper3 = new JLabelOperator(jdo, "Manufacturer 1 CANCATFLAP");
            assertNotNull(labelOper3);

            JSpinnerOperator spinner = new JSpinnerOperator(jdo, 0);

            JTextFieldOperator jtfo = new JTextFieldOperator(spinner);
            assertEquals("1234",jtfo.getText());

            jtfo.setText("123");
            JLabelOperator labelOper4 = new JLabelOperator(jdo, CbusNodeConstants.getReservedModule(123));
            assertNotNull(labelOper4);
            // JUnitUtil.waitFor(1000);

            jtfo.setText("789");
            JLabelOperator labelOper5 = new JLabelOperator(jdo,
                Bundle.getMessage("NdNumInUse",nodeModel.getNodeNumberName(789)));
            assertNotNull(labelOper5);
            // JUnitUtil.waitFor(1000);

            jtfo.setText("120");
            // JUnitUtil.waitFor(1000);

            CanMessage rmes = new CanMessage(tcis.getCanid());
            rmes.setNumDataElements(1);
            rmes.setElement(0, CbusConstants.CBUS_QNN); // All Modules respond to Query Node Numbers
            t.message(rmes); // ignored as dialogue already open

            jtfo.setText("65432");
            // JUnitUtil.waitFor(1000);

            JemmyUtil.pressButton(jdo, "OK");
            jdo.waitClosed();

        });

        CanReply r = new CanReply();
        r.setHeader(tcis.getCanid());
        r.setNumDataElements(3);
        r.setElement(0, CbusConstants.CBUS_ERR); // Command Station Error, to be ignored.
        r.setElement(1, 0x04); // node 1234
        r.setElement(2, 0xd2); // node 1234

        t.reply(r); // ignored

        r.setElement(0, CbusConstants.CBUS_RQNN); // Node Requests Node Number

        r.setExtended(true);
        t.reply(r); // ignored

        r.setExtended(false);
        t.reply(r);

        dialog_thread.assertCompleted();

        JUnitUtil.waitFor(()->{ return( tcis.outbound.size() >2); }, "Outbound did not increase: " + tcis.getTranslatedOutbound());
        assertEquals("[5f8] 42 FF 98", tcis.outbound.elementAt(2).toString(),"3rd message not right "
                + tcis.getTranslatedOutbound());

        // The Node sends a message back acknowledging the Name.
        // If this message is not heard, presents user with JDialog / error in log.
        r = new CanReply();
        r.setHeader(tcis.getCanid());
        r.setNumDataElements(3);
        r.setElement(0, CbusConstants.CBUS_NNACK); // Node number acknowledge
        r.setElement(1, 65432 >> 8);
        r.setElement(2, 65432 & 0xff);
        t.reply(r);
        flushGuiQueue(); // let the NNACK be processed before dispose

        t.dispose();

    }

    @Test
    @DisabledIfHeadless
    public void testFramesQueuedBeforeDisposeAreIgnored() {
        CbusAllocateNodeNumber t = new CbusAllocateNodeNumber(memo,nodeModel);

        CanReply r = new CanReply();
        r.setHeader(tcis.getCanid());
        r.setNumDataElements(3);
        r.setElement(0, CbusConstants.CBUS_RQNN); // Node Requests Node Number
        r.setElement(1, 0x04); // node 1234
        r.setElement(2, 0xd2); // node 1234

        // on the GUI thread, the RQNN is queued behind this action, so dispose comes first
        ThreadingUtil.runOnGUI(() -> {
            t.reply(r);
            t.dispose();
        });
        flushGuiQueue();

        assertEquals(0, tcis.outbound.size(),
            "no dialogue opened, no parameter request sent " + tcis.getTranslatedOutbound());

    }

    @Test
    @DisabledIfHeadless
    public void testDisposeWhileDialogueOpenSendsNoNodeNumber() {
        CbusAllocateNodeNumber t = new CbusAllocateNodeNumber(memo,nodeModel);
        t.setTimeout(5); // a Set Node Number timeout would report within the test

        // dispose while the dialogue is open, then press OK
        DialogueThread dialog_thread = new DialogueThread("CbusAllocateNodeNumber Dispose Dialog Close Thread", () -> {
            JDialogOperator jdo = new JDialogOperator(Bundle.getMessage("NdEntrSlimTitle"));
            JTextFieldOperator jtfo = new JTextFieldOperator(new JSpinnerOperator(jdo, 0));
            jtfo.setText("12345");
            t.dispose();
            JemmyUtil.pressButton(jdo, "OK");
            jdo.waitClosed();
        });

        CanReply r = new CanReply();
        r.setHeader(tcis.getCanid());
        r.setNumDataElements(3);
        r.setElement(0, CbusConstants.CBUS_RQNN); // Node Requests Node Number
        r.setElement(1, 0x00); // node 0, SLiM
        r.setElement(2, 0x00);
        t.reply(r);

        dialog_thread.assertCompleted();
        JUnitUtil.waitFor(50); // well past the timeout
        flushGuiQueue();

        // only the parameter request sent when the dialogue opened, no Set Node Number
        assertEquals(1, tcis.outbound.size(), "no SNN sent " + tcis.getTranslatedOutbound());
        assertEquals("[5f8] 10", tcis.outbound.elementAt(0).toString(), "Request node parameters(RQNP)");
        // no "No confirmation from node" error, checked by tearDown

    }

    @Test
    public void testAddNodes() {

        CbusAllocateNodeNumber t = new CbusAllocateNodeNumber(memo,nodeModel);
        memo.get(CbusPreferences.class).setAddNodes(true);

        CanReply rsna = new CanReply();
        rsna.setNumDataElements(3);
        rsna.setElement(0, CbusConstants.CBUS_NNACK); // node number acknowledge
        rsna.setElement(1, 0xff); // 65432
        rsna.setElement(2, 0x98); // 65432
        t.reply(rsna);

        // check that RTSTAT sent to command stations
        JUnitUtil.waitFor(()->{ return( !tcis.outbound.isEmpty()); }, "TCIS count did not increase " + tcis.getTranslatedOutbound());
        assertEquals("[5f8] 0C", tcis.outbound.elementAt(0).toString());

        assertNotNull(nodeModel.getNodeByNodeNum(65432));

        t.dispose();

    }

    @Test
    @DisabledIfHeadless
    public void testCanMessage() {
        CbusAllocateNodeNumber t = new CbusAllocateNodeNumber(memo,nodeModel);

        CanMessage r = new CanMessage(tcis.getCanid());
        r.setNumDataElements(1);
        r.setElement(0, CbusConstants.CBUS_HLT); // Bus Halt Command, to be ignored.
        t.message(r); // ignored as not interested in this OPC
        assertEquals(0, tcis.outbound.size());
        r.setElement(0, CbusConstants.CBUS_QNN); // All Modules respond to Query Node Numbers
        r.setExtended(true);
        t.message(r); // ignored as Extended
        assertEquals(0, tcis.outbound.size());

        r.setExtended(false);
        t.message(r); // output expected to be a Request Params Setup

        assertEquals(1, tcis.outbound.size());
        assertEquals("[5f8] 10", tcis.outbound.elementAt(0).toString(), tcis.getTranslatedOutbound());

        t.dispose();

    }

    @Test
    @DisabledIfHeadless
    public void testAllocateTimeout() {
        CbusAllocateNodeNumber t = new CbusAllocateNodeNumber(memo,nodeModel);

        t.setTimeout(5); // default is reduced to speed up test

        // operate the dialog box opened later
        DialogueThread dialog_thread = new DialogueThread("CbusAllocateNodeNumber Allocate Timeout Dialog Close Thread", () -> {
            // constructor for jdo will wait until the dialog is visible
            JDialogOperator jdo = new JDialogOperator(Bundle.getMessage("NdEntrSlimTitle"));

            JSpinnerOperator spinner = new JSpinnerOperator(jdo, 0);

            JTextFieldOperator jtfo = new JTextFieldOperator(spinner);
            jtfo.setText("12345");

            JemmyUtil.pressButton(jdo, "OK");
            jdo.waitClosed();

        });

        DialogueThread popup_thread = new DialogueThread("CbusAllocateNodeNumber Allocate Timeout Popup Close Thread", () -> {
            // constructor for jdo will wait until the dialog is visible
            JDialogOperator jdoo = new JDialogOperator(Bundle.getMessage("WarningTitle"));

            JemmyUtil.pressButton(jdoo, "OK");
            jdoo.waitClosed();

        });


        CanReply r = new CanReply();
        r.setHeader(tcis.getCanid());
        r.setNumDataElements(3);
        r.setElement(0, CbusConstants.CBUS_RQNN); // Node Requests Node Number
        r.setElement(1, 0x00); // node 0
        r.setElement(2, 0x00); // node 0

        t.reply(r);

        JUnitUtil.waitFor(()->{return tcis.outbound.size()>1;}, "2 outbound sent" + tcis.outbound);

        dialog_thread.assertCompleted();

        popup_thread.assertCompleted();

        JUnitAppender.assertErrorMessageStartsWith("No confirmation from node when setting node number 12345");

        t.dispose();

    }

    /**
     * Operates a dialogue on its own thread while the GUI thread shows it.
     * A failure on that thread, eg. an assertion or a Jemmy timeout, would
     * otherwise be lost, so it is passed back to the test.
     */
    private static class DialogueThread {

        private final Thread thread;
        private final AtomicReference<Throwable> failure = new AtomicReference<>();

        DialogueThread(String name, Runnable steps) {
            thread = new Thread(() -> {
                try {
                    steps.run();
                } catch (AssertionError | RuntimeException e) {
                    failure.set(e);
                }
            }, name);
            thread.start();
        }

        void assertCompleted() {
            JUnitUtil.waitFor(() -> !thread.isAlive(), thread.getName() + " finished");
            Throwable t = failure.get();
            if (t != null) {
                fail(thread.getName() + " failed", t);
            }
        }
    }

    /**
     * Wait until the GUI thread has processed everything queued so far.
     */
    private static void flushGuiQueue() {
        ThreadingUtil.runOnGUI(() -> {});
    }

    private CanSystemConnectionMemo memo;
    private CbusTrafficControllerScaffold tcis;
    private CbusNodeTableDataModel nodeModel;

    @BeforeEach
    public void setUp(@TempDir File tempDir) throws IOException  {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager( new jmri.profile.NullProfile( tempDir));

        memo = new CanSystemConnectionMemo();
        tcis = new CbusTrafficControllerScaffold(memo);
        memo.setProtocol(jmri.jmrix.can.CanConfigurationManager.SPROGCBUS);
        memo.get(CbusPreferences.class).setAllocateNNListener(false);
        // No background fetch, which would send its own requests to nodes
        // and write node backup files to the profile from a timer thread.
        memo.get(CbusPreferences.class).setNodeBackgroundFetchDelay(0);
        nodeModel = memo.get(CbusConfigurationManager.class)
            .provide(CbusNodeTableDataModel.class);

    }

    @AfterEach
    public void tearDown() {

        nodeModel.dispose();
        nodeModel = null;
        memo.dispose();
        memo = null;
        tcis.terminateThreads();
        tcis = null;
        JUnitUtil.tearDown();

    }

    // private static final Logger log = LoggerFactory.getLogger(CbusAllocateNodeNumberTest.class);

}
