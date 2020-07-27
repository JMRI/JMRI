package jmri.jmrix.can.cbus.swing.nodeconfig;

import java.awt.GraphicsEnvironment;

import jmri.jmrix.can.cbus.node.CbusNode;
import jmri.jmrix.can.cbus.node.CbusNodeEvent;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.Assume;
import org.netbeans.jemmy.operators.*;

/**
 * Test simple functioning of CbusNodeEventVarPane
 *
 * @author Paul Bender Copyright (C) 2016
 * @author Steve Young Copyright (C) 2019
 */
public class CbusNodeEventVarPaneTest {
    
    private CbusNodeEventVarPane t;

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        t = new CbusNodeEventVarPane(null);
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testIntComponents() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        t = new CbusNodeEventVarPane(null);
        t.initComponents(null);
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testSetNodeNull() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        t = new CbusNodeEventVarPane(null);
        t.initComponents(null);
        t.setNode(null);
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testSetNodeInGui() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        t = new CbusNodeEventVarPane(null);
        t.initComponents(null);
        
        CbusNode node = new CbusNode(null,12345);
        // set node to 4 ev vars per event, para 5
        node.getNodeParamManager().setParameters(new int[]{8,1,2,3,4,4,6,7,8});
        
        CbusNodeEvent ev = new CbusNodeEvent(null,0,7,12345,-1,4);  // nn, en, thisnode, index, maxevvar
        CbusNodeEvent eva = new CbusNodeEvent(null,257,111,12345,-1,4);  // nn, en, thisnode, index, maxevvar
        ev.setEvArr(new int[]{1,2,3,4});
        
        node.getNodeEventManager().addNewEvent(ev);
        node.getNodeEventManager().addNewEvent(eva);
        
        t.setNode(node);
        
        jmri.util.JmriJFrame f = new jmri.util.JmriJFrame();
        f.add(t);
        f.setTitle("Test CbusNodeEventVarPane");
        f.pack();
        f.setVisible(true);
        
        // Find new window by name
        JFrameOperator jfo = new JFrameOperator("Test CbusNodeEventVarPane");
        
        Assert.assertTrue("Button Enabled",getNewEventButtonEnabled(jfo));
        
        // JemmyUtil.pressButton(new JFrameOperator(f),("Pause Test"));
        
        CbusNode newNode = new CbusNode(null,12346);
        // set node to -1 ev vars per event, para 5
        newNode.getNodeParamManager().setParameters(new int[]{8,1,2,3,4,-1,6,7,8});
        t.setNode(newNode);
        Assert.assertFalse("Button Not Enabled",getNewEventButtonEnabled(jfo));
        
    }
    
    private boolean getNewEventButtonEnabled( JFrameOperator jfo ){
        return ( new JButtonOperator(jfo,Bundle.getMessage("AddNodeEvent")).isEnabled() );
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        t = null;
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(CbusNodeEventVarPaneTest.class);

}
