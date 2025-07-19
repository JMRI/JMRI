package jmri.jmrix.can.cbus.swing.nodeconfig;

import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.jmrix.can.cbus.CbusConfigurationManager;
import jmri.jmrix.can.cbus.CbusPreferences;
import jmri.jmrix.can.cbus.node.CbusNode;
import jmri.jmrix.can.cbus.node.CbusNodeEvent;
import jmri.jmrix.can.cbus.node.CbusNodeTableDataModel;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.netbeans.jemmy.operators.*;

/**
 * Test simple functioning of CbusNodeEditEventFrame
 *
 * @author Paul Bender Copyright (C) 2016
 * @author Steve Young Copyright (C) 2019
 */
@DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
public class CbusNodeEditEventFrameTest extends jmri.util.JmriJFrameTestBase {

    @Test
    public void testInitComponentsWithMainPaneAndMemo() {

        Assertions.assertNotNull(nodeModel);
        CbusNode nodeWithEventToEdit = nodeModel.provideNodeByNodeNum(256);

        // short event 7 on node 256, no index, 4 ev vars
        CbusNodeEvent eventToEdit = new CbusNodeEvent(memo,0,7,256,-1,4);
        nodeWithEventToEdit.getNodeEventManager().addNewEvent(eventToEdit);

        ((CbusNodeEditEventFrame)frame).initComponents(memo,nodeWithEventToEdit.getNodeEventManager().getNodeEventByArrayID(0)); // memo, event to edit

        Assert.assertEquals("title","Edit Event EN:7 on Node 256",frame.getTitle());
        Assert.assertFalse("node / event select spinners not dirty",((CbusNodeEditEventFrame)frame).spinnersDirty() );
        Assert.assertTrue("event 7 ",((CbusNodeEditEventFrame)frame).getEventVal()==7);
        Assert.assertTrue("node 0 ",((CbusNodeEditEventFrame)frame).getNodeVal()==0);


        // Find new window by name
        JFrameOperator jfo = new JFrameOperator( frame.getTitle() );

        Assert.assertFalse(getEditButtonEnabled(jfo));
        Assert.assertTrue(getDeleteButtonEnabled(jfo));


        Thread t = new Thread(() -> {
            // delete event? yes / no
            JDialogOperator jdo = new JDialogOperator(Bundle.getMessage("DelEvPopTitle"));
            JButtonOperator jbo = new JButtonOperator(jdo, Bundle.getMessage("ButtonYes"));
            jbo.pushNoBlock();
        });
        t.setName("Confirm Delete Event Dialog Thread");
        t.start();

        new JButtonOperator(jfo,Bundle.getMessage("ButtonDelete")).doClick();

        JUnitUtil.waitFor(()-> !t.isAlive(), "Confirm Delete Event Dialog finished");

        JUnitUtil.waitFor(()-> !tcis.outbound.isEmpty(), "at least 1 frame sent");

        Assertions.assertEquals("[5f8] 53 01 00", tcis.outbound.get(0).toString()
            ,"Node Enter Learn Mode");

        JUnitUtil.waitFor(()-> tcis.outbound.size() > 1, "2 frames sent");
        Assertions.assertEquals("[5f8] 95 00 00 00 07", tcis.outbound.get(1).toString()
            ,"Unlearn Event 7");

        JUnitUtil.waitFor(()-> tcis.outbound.size() > 2, "3 frames sent");
        Assertions.assertEquals("[5f8] 54 01 00", tcis.outbound.get(2).toString()
            ,"Node Exit Learn Mode");

        // window auto-closes as Event no longer exists
        jfo.waitClosed();
    }

    private boolean getEditButtonEnabled( JFrameOperator jfo ){
        return ( new JButtonOperator(jfo,Bundle.getMessage("EditEvent")).isEnabled() );
    }

    private boolean getDeleteButtonEnabled( JFrameOperator jfo ){
        return ( new JButtonOperator(jfo,Bundle.getMessage("ButtonDelete")).isEnabled() );
    }

    private CanSystemConnectionMemo memo = null;
    private TrafficControllerScaffold tcis = null;
    private NodeConfigToolPane mainpane = null;
    private CbusNodeTableDataModel nodeModel = null;

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();

        memo = new CanSystemConnectionMemo();

        tcis = new TrafficControllerScaffold();
        memo.setTrafficController(tcis);
        memo.setProtocol(jmri.jmrix.can.CanConfigurationManager.MERGCBUS);

        memo.get(CbusPreferences.class).setNodeBackgroundFetchDelay(0);
        nodeModel = memo.get(CbusConfigurationManager.class)
            .provide(CbusNodeTableDataModel.class);
        mainpane = new NodeConfigToolPane();
        mainpane.initComponents(memo);
        frame = new CbusNodeEditEventFrame(mainpane);

    }

    @AfterEach
    @Override
    public void tearDown() {

        Assertions.assertNotNull(memo);
        memo.dispose();
        Assertions.assertNotNull(frame);
        frame.dispose();
        Assertions.assertNotNull(mainpane);
        mainpane.dispose();
        Assertions.assertNotNull(nodeModel);
        nodeModel.dispose();
        Assertions.assertNotNull(tcis);
        tcis.terminateThreads();

        mainpane = null;
        frame = null;
        nodeModel = null;
        memo = null;
        tcis = null;
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(CbusNodeEditEventFrameTest.class);

}
