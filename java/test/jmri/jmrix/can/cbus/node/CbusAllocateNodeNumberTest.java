package jmri.jmrix.can.cbus.node;

import java.awt.GraphicsEnvironment;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.jmrix.can.cbus.CbusConstants;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;
import jmri.util.swing.JemmyUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JSpinnerOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Steve Young Copyright (C) 2019
 */
public class CbusAllocateNodeNumberTest {

    @Test
    public void testCTor() {
        
        Assert.assertEquals("node model listening",1,tcis.numListeners());
        
        t = new CbusAllocateNodeNumber(memo,nodeModel);
        
        Assert.assertNotNull("exists",t);
        Assert.assertEquals("listening",2,tcis.numListeners());
        
        t.dispose();
        
        Assert.assertEquals("not listening",1,tcis.numListeners());
        
    }
    
    @Test
    public void testDisplayDialogue() {
        
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        t = new CbusAllocateNodeNumber(memo,nodeModel);
        Assert.assertNotNull("exists",t);
        
        // create a thread that waits to close the dialog box opened later
        Thread dialog_thread = new Thread(() -> {
            // constructor for jdo will wait until the dialog is visible
            JDialogOperator jdo = new JDialogOperator(Bundle.getMessage("NdEntrNumTitle",1234));
            
            JLabelOperator labelOper = new JLabelOperator(jdo, Bundle.getMessage("NdRqNdDetails"));
            Assert.assertNotNull("labelOper exists",labelOper);
            
            CanReply rs = new CanReply();
            rs.setNumDataElements(7);
            rs.setElement(0, CbusConstants.CBUS_PARAMS); // Node sends Parameters
            rs.setElement(1, 0x01);
            rs.setElement(2, 0x02);
            rs.setElement(3, 0x03);
            rs.setElement(4, 0x04);
            rs.setElement(5, 0x05);
            rs.setElement(6, 0x06);
            rs.setElement(7, 0x07);
            t.reply(rs);
            
            JLabelOperator labelOper2 = new JLabelOperator(jdo, CbusNodeConstants.getManu(1));
            Assert.assertNotNull("labelOper2 exists",labelOper2);
            
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
            Assert.assertNotNull("labelOper3 exists",labelOper3);
            
            JSpinnerOperator spinner = new JSpinnerOperator(jdo, 0);
            
            JTextFieldOperator jtfo = new JTextFieldOperator(spinner);
            Assert.assertEquals("originalFieldValue", "256", jtfo.getText());
            
            jtfo.setText("123");
            JLabelOperator labelOper4 = new JLabelOperator(jdo, CbusNodeConstants.getReservedModule(123));
            Assert.assertNotNull("labelOper4 exists",labelOper4);
            
            Assert.assertNotNull(nodeModel.provideNodeByNodeNum(789));
            jtfo.setText("789");
            JLabelOperator labelOper5 = new JLabelOperator(jdo, 
                Bundle.getMessage("NdNumInUse",nodeModel.getNodeNumberName(789)));
            Assert.assertNotNull("labelOper5 exists",labelOper5);
            
            CanMessage rmes = new CanMessage(tcis.getCanid());
            rmes.setNumDataElements(1);
            rmes.setElement(0, CbusConstants.CBUS_QNN); // All Modules respond to Query Node Numbers
            t.message(rmes); // ignored as dialogue already open
            
            jtfo.setText("65432");
            
            JemmyUtil.pressButton(jdo, "OK");
            
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
        
        Assert.assertEquals("1st Message sent is request params", "[5f8] 10",
            tcis.outbound.elementAt(0).toString());
            
        Assert.assertEquals("2nd Message sent is name request", "[5f8] 11",
            tcis.outbound.elementAt(1).toString());
            
        Assert.assertEquals("3rd Message sent is new node num", "[5f8] 42 FF 98",
            tcis.outbound.elementAt(2).toString());
            
        jmri.jmrix.can.cbus.CbusPreferences pref = new jmri.jmrix.can.cbus.CbusPreferences();
        jmri.InstanceManager.setDefault(jmri.jmrix.can.cbus.CbusPreferences.class,pref );
        pref.setAddNodes(true);
        
        CanReply rsna = new CanReply();
        rsna.setNumDataElements(3);
        rsna.setElement(0, CbusConstants.CBUS_NNACK); // node number acknowledge
        rsna.setElement(1, 0xff); // 65432
        rsna.setElement(2, 0x98); // 65432
        t.reply(rsna);
        
        t.dispose();
        
    }
    
    @Test
    public void testCanMessage() {
        
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        t = new CbusAllocateNodeNumber(memo,nodeModel);
        
        CanMessage r = new CanMessage(tcis.getCanid());
        r.setNumDataElements(1);
        r.setElement(0, CbusConstants.CBUS_HLT); // Bus Halt Command, to be ignored.
        t.message(r); // ignored as not interested in this OPC
        Assert.assertEquals("has sent 0", 0 ,tcis.outbound.size() );
        
        r.setElement(0, CbusConstants.CBUS_QNN); // All Modules respond to Query Node Numbers
        r.setExtended(true);
        t.message(r); // ignored as Extended
        Assert.assertEquals("has sent 0", 0 ,tcis.outbound.size() );
        
        r.setExtended(false);
        t.message(r); // output expected to be a Request Params Setup
        
        Assert.assertEquals("has sent 1", 1 ,tcis.outbound.size() );
        Assert.assertEquals("Message sent is request params", "[5f8] 10",
            tcis.outbound.elementAt(0).toString());
        
        t.dispose();
        
    }
    
    @Test
    public void testAllocateTimeout() {
        
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
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
            
        });
        
        dialog_thread.setName("CbusAllocateNodeNumber Allocate Timeout Dialog Close Thread");
        dialog_thread.start();
        
        Thread popup_thread = new Thread(() -> {
            // constructor for jdo will wait until the dialog is visible
            JDialogOperator jdoo = new JDialogOperator(Bundle.getMessage("WarningTitle"));
            
            JemmyUtil.pressButton(jdoo, "OK");
            
        });
        
        popup_thread.setName("CbusAllocateNodeNumber Allocate Timeout Dialog Close Thread");
        popup_thread.start();
        
        
        CanReply r = new CanReply();
        r.setHeader(tcis.getCanid());
        r.setNumDataElements(3);
        r.setElement(0, CbusConstants.CBUS_RQNN); // Node Requests Node Number
        r.setElement(1, 0x00); // node 0
        r.setElement(2, 0x00); // node 0
        
        t.reply(r);
        
        JUnitUtil.waitFor(()->{return !(dialog_thread.isAlive());}, "checkCbus Allocate Node Num Dialog finished");
        
        JUnitUtil.waitFor(()->{return !(popup_thread.isAlive());}, "Cbus Allocate Node Num Timout Dialog finished");
        
        JUnitAppender.assertErrorMessageStartsWith("No confirmation from node when setting node number 12345");

        t.dispose();
        
    }        
    
    private CanSystemConnectionMemo memo;
    private TrafficControllerScaffold tcis;
    private CbusAllocateNodeNumber t;
    private CbusNodeTableDataModel nodeModel;

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        memo = new CanSystemConnectionMemo();
        tcis = new TrafficControllerScaffold();
        memo.setTrafficController(tcis);
        
        nodeModel = new CbusNodeTableDataModel(
            memo, 3,CbusNodeTableDataModel.MAX_COLUMN);
        
    }

    @After
    public void tearDown() {
        t = null;
        nodeModel.dispose();
        nodeModel = null;
        memo.dispose();
        memo = null;
        tcis.terminateThreads();
        tcis = null;
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }

    // private final static Logger log = LoggerFactory.getLogger(CbusAllocateNodeNumberTest.class);

}
