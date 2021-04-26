package jmri.jmrix.can.cbus.swing.nodeconfig;

import java.awt.GraphicsEnvironment;

import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.jmrix.can.cbus.CbusPreferences;
import jmri.jmrix.can.cbus.node.CbusNode;
import jmri.jmrix.can.cbus.node.CbusNodeEvent;
import jmri.jmrix.can.cbus.node.CbusNodeTableDataModel;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.Assume;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JFrameOperator;

/**
 * Test simple functioning of CbusNodeEditEventFrame
 *
 * @author Paul Bender Copyright (C) 2016
 * @author Steve Young Copyright (C) 2019
 */
public class CbusNodeEditEventFrameTest {
 
    @Test
    public void testInitComponentsWithMainPaneAndMemo() {
        
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        
        
        CbusNode nodeWithEventToEdit = nodeModel.provideNodeByNodeNum(256);

        // short event 7 on node 256, no index, 4 ev vars
        CbusNodeEvent eventToEdit = new CbusNodeEvent(memo,0,7,256,-1,4);
        nodeWithEventToEdit.getNodeEventManager().addNewEvent(eventToEdit);
        
        t.initComponents(memo,nodeWithEventToEdit.getNodeEventManager().getNodeEventByArrayID(0)); // memo, event to edit
        
        Assert.assertEquals("title","Edit Event EN:7 on Node 256",t.getTitle());
        Assert.assertFalse("node / event select spinners not dirty",t.spinnersDirty() );
        Assert.assertTrue("event 7 ",t.getEventVal()==7);
        Assert.assertTrue("node 0 ",t.getNodeVal()==0);
        
        
        // Find new window by name
        JFrameOperator jfo = new JFrameOperator( t.getTitle() );
        
        Assert.assertFalse(getEditButtonEnabled(jfo));
        Assert.assertTrue(getDeleteButtonEnabled(jfo));
        
        
    }
    
    private boolean getEditButtonEnabled( JFrameOperator jfo ){
        return ( new JButtonOperator(jfo,Bundle.getMessage("EditEvent")).isEnabled() );
    }
    
    private boolean getDeleteButtonEnabled( JFrameOperator jfo ){
        return ( new JButtonOperator(jfo,Bundle.getMessage("ButtonDelete")).isEnabled() );
    }
    
    private CbusNodeEditEventFrame t;
    private CanSystemConnectionMemo memo;
    private TrafficControllerScaffold tcis;
    private NodeConfigToolPane mainpane;
    private CbusNodeTableDataModel nodeModel;

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        
        if (!GraphicsEnvironment.isHeadless()){
            
            memo = new CanSystemConnectionMemo();
            
            tcis = new TrafficControllerScaffold();
            memo.setTrafficController(tcis);
        
            nodeModel = new CbusNodeTableDataModel(memo, 3,CbusNodeTableDataModel.MAX_COLUMN);
            jmri.InstanceManager.setDefault(CbusNodeTableDataModel.class,nodeModel );
            jmri.InstanceManager.store(new CbusPreferences(),CbusPreferences.class );
            mainpane = new NodeConfigToolPane();
            mainpane.initComponents(memo);
            t = new CbusNodeEditEventFrame(mainpane);
        }
    }

    @AfterEach
    public void tearDown() {
        if (!GraphicsEnvironment.isHeadless()) {
            memo.dispose();
            t.dispose();
            mainpane.dispose();
            nodeModel.dispose();
            tcis.terminateThreads();
        }
        mainpane = null;
        t = null;
        nodeModel = null;
        memo = null;
        tcis = null;
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(CbusNodeEditEventFrameTest.class);

}
