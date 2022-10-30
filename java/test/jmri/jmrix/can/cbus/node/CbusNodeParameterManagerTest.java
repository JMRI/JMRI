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

    // private final static Logger log = LoggerFactory.getLogger(CbusNodeEventManagerTest.class);

}
