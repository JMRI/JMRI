package jmri.jmrix.can.cbus.node;

import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Steve Young Copyright (C) 2019
 */
public class CbusNodeParameterManagerTest {

    private CanSystemConnectionMemo memo;
    private CbusBasicNodeWithManagers node;
    private int [] parameters = {23, 0, 'e', 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0};
    private CbusNodeParameterManager manager = null;
        
    @Test
    public void testCTor() {
        CbusNodeParameterManager t = new CbusNodeParameterManager(null);
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void newFirmware() {
        Assertions.assertNotNull(manager);
        manager.setParameters(parameters);
        Assert.assertTrue("New firmware", manager.isFwEqualOrNewer(1, 'a', 0));
    }

    @Test
    public void sameFirmware() {
        Assertions.assertNotNull(manager);
        manager.setParameters(parameters);
        Assert.assertTrue("Same firmware", manager.isFwEqualOrNewer(2, 'e', 2));
    }

    @Test
    public void oldFirmware() {
        Assertions.assertNotNull(manager);
        manager.setParameters(parameters);
        Assert.assertFalse("Old firmware", manager.isFwEqualOrNewer(3, 'c', 7));
    }

    // parameter 8, the flags, with bit 6 set: a VLCB node
    private int[] vlcb(int patch) {
        int[] p = parameters.clone();
        p[CbusNodeConstants.FLAGS_IDX] = CbusNodeConstants.FLAG_VLCB | 0x0D;
        p[CbusNodeConstants.BETA_REV_IDX] = patch;
        return p;
    }

    @Test
    public void testCbusNodeShowsBeta() {
        manager.setParameters(parameters);
        Assertions.assertFalse(manager.isVlcb());
        Assertions.assertEquals(Bundle.getMessage("FWBeta") + "2", manager.getFirmwareBuildText());
        Assertions.assertTrue(manager.getNodeTypeString().contains(Bundle.getMessage("FWBeta") + "2"),
                manager.getNodeTypeString());
    }

    @Test
    public void testVlcbNodeShowsPatch() {
        manager.setParameters(vlcb(2));
        Assertions.assertTrue(manager.isVlcb());
        Assertions.assertEquals(Bundle.getMessage("FWPatch") + "2", manager.getFirmwareBuildText());
        Assertions.assertFalse(manager.getNodeTypeString().contains(Bundle.getMessage("FWBeta").trim()),
                manager.getNodeTypeString());

        manager.setParameters(vlcb(0));
        Assertions.assertEquals("", manager.getFirmwareBuildText(), "patch 0: nothing added");
    }

    @Test
    public void testVlcbPatchNumbersCompareInOrder() {
        manager.setParameters(vlcb(2));
        Assertions.assertTrue(manager.isFwEqualOrNewer(2, 'e', 0), "patch 2 is newer than patch 0");
        Assertions.assertTrue(manager.isFwEqualOrNewer(2, 'e', 2), "patch 2 is patch 2");
        Assertions.assertFalse(manager.isFwEqualOrNewer(2, 'e', 3), "patch 2 is older than patch 3");
        // for a CBUS node, 0 is the release, newer than any beta
        manager.setParameters(parameters);
        Assertions.assertTrue(manager.isFwEqualOrNewer(2, 'e', 1));
        Assertions.assertFalse(manager.isFwEqualOrNewer(2, 'e', 3));
    }

    @Test
    public void testVlcbFromPnnFlagsBeforeParameters() {
        CbusNode pnnNode = new CbusNode(memo, 24);
        pnnNode.setNodeFlags(CbusNodeConstants.FLAG_VLCB | 0x0D);
        Assertions.assertTrue(pnnNode.getNodeParamManager().isVlcb());
        pnnNode.setNodeFlags(0x0D);
        Assertions.assertFalse(pnnNode.getNodeParamManager().isVlcb());
        pnnNode.dispose();
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        memo = new CanSystemConnectionMemo();
        node = new CbusBasicNodeWithManagers(memo, 23);
        manager = new CbusNodeParameterManager(node);
    }

    @AfterEach
    public void tearDown() {
        manager = null;
        node = null;
        memo = null;
        JUnitUtil.tearDown();
    }

    // private static final Logger log = LoggerFactory.getLogger(CbusNodeEventManagerTest.class);

}
