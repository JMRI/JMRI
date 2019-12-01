package jmri.jmrix.can.cbus.swing.nodeconfig;

import java.awt.GraphicsEnvironment;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.jmrix.can.cbus.node.CbusNode;
import jmri.jmrix.can.cbus.node.CbusNodeEvent;
import jmri.jmrix.can.cbus.node.CbusNodeTableDataModel;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JFrameOperator;

/**
 * Test simple functioning of CbusNodeEditEventFrame
 *
 * @author Paul Bender Copyright (C) 2016
 * @author Steve Young Copyright (C) 2019
 */
public class CbusNodeEditEventFrameTest extends jmri.util.JmriJFrameTestBase {
 
    @Test
    public void testCtorWithMainPane() {
        
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        
        NodeConfigToolPane mainpane = new NodeConfigToolPane();
        
        CbusNodeEditEventFrame t = new CbusNodeEditEventFrame(mainpane);
        Assert.assertNotNull("exists",t);
        
        t.initComponents(null,null); // memo, event to edit
        JUnitAppender.assertErrorMessageStartsWith("Unable to get Node Table from Instance Manag");
        
        t.dispose();
        t = null;
        
    }
    
    @Test
    public void testInitComponentsWithMainPaneAndMemo() {
        
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        
        CbusNodeTableDataModel nodeModel = new CbusNodeTableDataModel(memo, 3,CbusNodeTableDataModel.MAX_COLUMN);
        jmri.InstanceManager.setDefault(CbusNodeTableDataModel.class,nodeModel );
        CbusNode nodeWithEventToEdit = nodeModel.provideNodeByNodeNum(256);

        // short event 7 on node 256, no index, 4 ev vars
        CbusNodeEvent eventToEdit = new CbusNodeEvent(0,7,256,-1,4);
        nodeWithEventToEdit.addNewEvent(eventToEdit);
        
        NodeConfigToolPane mainpane = new NodeConfigToolPane();
        
        CbusNodeEditEventFrame t = new CbusNodeEditEventFrame(mainpane);
        
        t.initComponents(memo,nodeWithEventToEdit.getNodeEventByArrayID(0)); // memo, event to edit
        
        Assert.assertEquals("title","Edit Event EN:7 on Node 256",t.getTitle());
        Assert.assertFalse("node / event select spinners not dirty",t.spinnersDirty() );
        Assert.assertTrue("event 7 ",t.getEventVal()==7);
        Assert.assertTrue("node 0 ",t.getNodeVal()==0);
        
        
        // Find new window by name
        JFrameOperator jfo = new JFrameOperator( t.getTitle() );
        
        Assert.assertFalse(getEditButtonEnabled(jfo));
        Assert.assertTrue(getDeleteButtonEnabled(jfo));
        
        t.dispose();
        t = null;
        
        jfo = null;
        eventToEdit = null;
        nodeModel.dispose();
        nodeModel = null;
        
    }
    
    private boolean getEditButtonEnabled( JFrameOperator jfo ){
        return ( new JButtonOperator(jfo,Bundle.getMessage("EditEvent")).isEnabled() );
    }
    
    private boolean getDeleteButtonEnabled( JFrameOperator jfo ){
        return ( new JButtonOperator(jfo,Bundle.getMessage("ButtonDelete")).isEnabled() );
    }
    
    private CanSystemConnectionMemo memo;
    private TrafficControllerScaffold tcis;

    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        memo = new CanSystemConnectionMemo();
        tcis = new TrafficControllerScaffold();
        memo.setTrafficController(tcis);
        if(!GraphicsEnvironment.isHeadless()){
           frame = new CbusNodeEditEventFrame(null);
        }
    }

    @After
    @Override
    public void tearDown() {
        
        memo = null;
        tcis = null;
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        super.tearDown();    
    }

    // private final static Logger log = LoggerFactory.getLogger(CbusNodeEditEventFrameTest.class);

}
