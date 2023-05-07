package jmri.jmrix.can.cbus.swing.nodeconfig;

import jmri.jmrix.can.*;
import jmri.jmrix.can.cbus.CbusConfigurationManager;
import jmri.jmrix.can.cbus.CbusPreferences;
import jmri.jmrix.can.cbus.node.CbusNode;
import jmri.jmrix.can.cbus.node.CbusNodeTableDataModel;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

/**
 * Test simple functioning of CbusNodeEditNVarFrame
 *
 * @author Paul Bender Copyright (C) 2016 2019
 * @author Steve Young Copyright (C) 2019
 */
@DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
public class CbusNodeEditNVarPaneTest {

    @Test
    public void testCtorWithMain() {
        NodeConfigToolPane mainpane = new NodeConfigToolPane();

        t = new CbusNodeEditNVarPane(mainpane);
        Assert.assertNotNull("exists",t);

    }

    @Test
    public void testInit() {
        NodeConfigToolPane mainpane = new NodeConfigToolPane();

        Assertions.assertNotNull(memo);
        memo.get(CbusPreferences.class).setNodeBackgroundFetchDelay(0);
        CbusNodeTableDataModel nodeModel = memo.get(CbusConfigurationManager.class)
            .provide(CbusNodeTableDataModel.class);

        mainpane.initComponents(memo);


        t = new CbusNodeEditNVarPane(mainpane);
        Assert.assertNotNull("exists",t);

        t.initComponents(memo);

        CbusNode nodeToEdit = nodeModel.provideNodeByNodeNum(256);
        // set node to 3 node vars , param6
        nodeToEdit.getNodeParamManager().setParameters(new int[]{8,1,2,3,4,5,3,7,8});

        t.setNode( nodeToEdit );

        nodeToEdit.dispose();

    }

    private CanSystemConnectionMemo memo = null;
    private TrafficControllerScaffold tcis = null;
    private CbusNodeEditNVarPane t;

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        memo = new CanSystemConnectionMemo();
        tcis = new TrafficControllerScaffold();
        memo.setTrafficController(tcis);
        memo.setProtocol(ConfigurationManager.MERGCBUS);
    }

    @AfterEach
    public void tearDown() {
        t = null;
        Assertions.assertNotNull(memo);
        memo.dispose();
        memo=null;
        Assertions.assertNotNull(tcis);
        tcis.terminateThreads();
        tcis=null;
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(CbusNodeEditNVarFrameTest.class);

}
