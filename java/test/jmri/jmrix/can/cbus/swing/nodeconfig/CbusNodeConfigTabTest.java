package jmri.jmrix.can.cbus.swing.nodeconfig;

import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.jmrix.can.cbus.CbusConfigurationManager;
import jmri.jmrix.can.cbus.CbusPreferences;
import jmri.jmrix.can.cbus.node.CbusNode;
import jmri.jmrix.can.cbus.node.CbusNodeTableDataModel;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

/**
 * Test simple functioning of CbusNodeConfigTab
 *
 * @author Paul Bender Copyright (C) 2016
 * @author Steve Young Copyright (C) 2020
 */
@DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
public class CbusNodeConfigTabTest {

    private static class TestClass extends CbusNodeConfigTab {

        private TestClass( NodeConfigToolPane pane ){
            super(pane);
        }

        @Override
        protected void changedNode( CbusNode node ) {}

    }

    @Test
    public void testCtor() {

        TestClass t = new TestClass(null);
        Assert.assertNotNull("exists",t);
        Assert.assertNotNull("exists",nodeToEdit);
        t.dispose();
    }

    @Test
    public void testInit() {

        TestClass t = new TestClass(null);
        t.setNode(nodeToEdit); // node num 256

        Assert.assertNotNull("exists",t);
        t.dispose();
    }

    private CanSystemConnectionMemo memo = null;
    private TrafficControllerScaffold tcis = null;
    private CbusNodeTableDataModel nodeModel = null;
    private CbusNode nodeToEdit = null;

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        memo = new CanSystemConnectionMemo();
        tcis = new TrafficControllerScaffold();
        memo.setTrafficController(tcis);
        memo.setProtocol(jmri.jmrix.can.CanConfigurationManager.SPROGCBUS);

        memo.get(CbusPreferences.class).setNodeBackgroundFetchDelay(0);
        nodeModel = memo.get(CbusConfigurationManager.class)
            .provide(CbusNodeTableDataModel.class);

        nodeToEdit = nodeModel.provideNodeByNodeNum(256);
        // set node to 3 node vars , param6
        nodeToEdit.getNodeParamManager().setParameters(new int[]{8,1,2,3,4,5,3,7,8});

    }

    @AfterEach
    public void tearDown() {
        Assertions.assertNotNull(nodeToEdit);
        nodeToEdit.dispose();
        Assertions.assertNotNull(nodeModel);
        nodeModel.dispose();
        Assertions.assertNotNull(tcis);
        tcis.terminateThreads();
        Assertions.assertNotNull(memo);
        memo.dispose();
        memo = null;
        tcis = null;

        JUnitUtil.tearDown();

    }

    // private final static Logger log = LoggerFactory.getLogger(CbusNodeConfigTabTest.class);

}
