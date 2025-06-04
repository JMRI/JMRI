package jmri.jmrix.can.cbus.swing.nodeconfig;

import jmri.jmrix.can.cbus.node.CbusNode;
import jmri.jmrix.can.cbus.node.CbusNodeEvent;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

import org.netbeans.jemmy.operators.*;

/**
 * Test simple functioning of CbusNodeEventVarPane
 *
 * @author Paul Bender Copyright (C) 2016
 * @author Steve Young Copyright (C) 2019
 */
@jmri.util.junit.annotations.DisabledIfHeadless
public class CbusNodeEventVarPaneTest {

    private CbusNodeEventVarPane t;

    @Test
    public void testCtor() {
        t = new CbusNodeEventVarPane(null);
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testIntComponents() {
        t = new CbusNodeEventVarPane(null);
        t.initComponents(null);
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testSetNodeNull() {
        t = new CbusNodeEventVarPane(null);
        t.initComponents(null);
        t.setNode(null);
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testSetNodeInGui() {
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
        ThreadingUtil.runOnGUI( () -> {
            f.pack();
            f.setVisible(true);
        });

        // Find new window by name
        JFrameOperator jfo = new JFrameOperator("Test CbusNodeEventVarPane");

        Assert.assertTrue("Button Enabled",getNewEventButtonEnabled(jfo));

        CbusNode newNode = new CbusNode(null,12346);
        // set node to -1 ev vars per event, para 5
        newNode.getNodeParamManager().setParameters(new int[]{8,1,2,3,4,-1,6,7,8});
        t.setNode(newNode);
        Assert.assertFalse("Button Not Enabled",getNewEventButtonEnabled(jfo));

        JUnitUtil.dispose(f);

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
