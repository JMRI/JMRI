package jmri.jmrix.can.cbus.node;

import java.io.IOException;
import java.io.File;

import jmri.jmrix.can.*;
import jmri.jmrix.can.cbus.*;
import jmri.jmrix.can.cbus.simulator.moduletypes.MergCanpan;

import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;
import jmri.util.swing.JemmyUtil;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
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

        t = new CbusAllocateNodeNumber(memo,nodeModel);
        assertNotNull(t);
        assertEquals(3, tcis.numListeners(),"3 listeners " + tcis.getListeners());

        t.dispose();
        assertEquals(2, tcis.numListeners(),"2 listener after dispose " + tcis.getListeners());

    }

    @Test
    @DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
    public void testDisplayDialogue() {
        memo.get(CbusPreferences.class).setAddNodes(false);
        t = new CbusAllocateNodeNumber(memo,nodeModel);
        assertNotNull(t);

        CbusNode node789 = nodeModel.provideNodeByNodeNum(789);
        assertNotNull(node789);
        // configure dummy node as CANPAN so no parameters are requested for it
        new MergCanpan().configureDummyNode(node789);

        // create a thread that waits to close the dialog box opened later
        Thread dialog_thread = new Thread(() -> {
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
            t.reply(rsn);

            JLabelOperator labelOper3 = new JLabelOperator(jdo, "Manufacturer 1 CANCATFLAP");
            assertNotNull(labelOper3);

            JSpinnerOperator spinner = new JSpinnerOperator(jdo, 0);

            JTextFieldOperator jtfo = new JTextFieldOperator(spinner);
            assertEquals("1234",jtfo.getText());

            jtfo.setText("123");
            JLabelOperator labelOper4 = new JLabelOperator(jdo, CbusNodeConstants.getReservedModule(123));
            assertNotNull(labelOper4);


            jtfo.setText("789");
            JLabelOperator labelOper5 = new JLabelOperator(jdo,
                Bundle.getMessage("NdNumInUse",nodeModel.getNodeNumberName(789)));
            assertNotNull(labelOper5);

            CanMessage rmes = new CanMessage(tcis.getCanid());
            rmes.setNumDataElements(1);
            rmes.setElement(0, CbusConstants.CBUS_QNN); // All Modules respond to Query Node Numbers
            t.message(rmes); // ignored as dialogue already open

            jtfo.setText("65432");

            JemmyUtil.pressButton(jdo, "OK");
            jdo.waitClosed();

        });
        dialog_thread.setName("CbusAllocateNodeNumber Dialog Close Thread");
        dialog_thread.start();

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

        JUnitUtil.waitFor(()->{return !(dialog_thread.isAlive());}, "checkCbus Allocate Node Num Dialog finished");

        JUnitUtil.waitFor(()->{ return( tcis.outbound.size() >2); }, "Outbound did not increase: " + tcis.getTranslatedOutbound());
        assertEquals("[5f8] 42 FF 98", tcis.outbound.elementAt(2).toString(),"3rd message not right " + tcis.getTranslatedOutbound());

        t.dispose();

    }

    @Test
    public void testAddNodes() {

        t = new CbusAllocateNodeNumber(memo,nodeModel);
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
    @DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
    public void testCanMessage() {
        t = new CbusAllocateNodeNumber(memo,nodeModel);

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
    @DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
    public void testAllocateTimeout() {
        t = new CbusAllocateNodeNumber(memo,nodeModel);

        t.setTimeout(5); // default is reduced to speed up test

        // create a thread that waits to close the dialog box opened later
        Thread dialog_thread = new Thread(() -> {
            // constructor for jdo will wait until the dialog is visible
            JDialogOperator jdo = new JDialogOperator(Bundle.getMessage("NdEntrSlimTitle"));

            JSpinnerOperator spinner = new JSpinnerOperator(jdo, 0);

            JTextFieldOperator jtfo = new JTextFieldOperator(spinner);
            jtfo.setText("12345");

            JemmyUtil.pressButton(jdo, "OK");
            jdo.waitClosed();

        });

        dialog_thread.setName("CbusAllocateNodeNumber Allocate Timeout Dialog Close Thread");
        dialog_thread.start();

        Thread popup_thread = new Thread(() -> {
            // constructor for jdo will wait until the dialog is visible
            JDialogOperator jdoo = new JDialogOperator(Bundle.getMessage("WarningTitle"));

            JemmyUtil.pressButton(jdoo, "OK");
            jdoo.waitClosed();

        });

        popup_thread.setName("CbusAllocateNodeNumber Allocate Timeout Popup Close Thread");
        popup_thread.start();


        CanReply r = new CanReply();
        r.setHeader(tcis.getCanid());
        r.setNumDataElements(3);
        r.setElement(0, CbusConstants.CBUS_RQNN); // Node Requests Node Number
        r.setElement(1, 0x00); // node 0
        r.setElement(2, 0x00); // node 0

        t.reply(r);

        JUnitUtil.waitFor(()->{return tcis.outbound.size()>1;}, "2 outbound sent" + tcis.outbound);

        JUnitUtil.waitFor(()->{return !(dialog_thread.isAlive());}, "checkCbus Allocate Node Num Dialog finished");

        JUnitUtil.waitFor(()->{return !(popup_thread.isAlive());}, "Cbus Allocate Node Num Timout Dialog finished");

        JUnitAppender.assertErrorMessageStartsWith("No confirmation from node when setting node number 12345");

        t.dispose();

    }

    private CanSystemConnectionMemo memo;
    private CbusTrafficControllerScaffold tcis;
    private CbusAllocateNodeNumber t;
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
        nodeModel = memo.get(CbusConfigurationManager.class)
            .provide(CbusNodeTableDataModel.class);

    }

    @AfterEach
    public void tearDown() {
        t = null;
        nodeModel.dispose();
        nodeModel = null;
        memo.dispose();
        memo = null;
        tcis.terminateThreads();
        tcis = null;
        JUnitUtil.tearDown();

    }

    // private final static Logger log = LoggerFactory.getLogger(CbusAllocateNodeNumberTest.class);

}
