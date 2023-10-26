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
public class CbusBasicNodeWithManagersTest {

    @Test
    public void testCTor() {
        Assert.assertNotNull("exists",new CbusBasicNodeWithManagers(null,123));
    }

    @Test
    public void testCbBnWMcTorWithMemo() {
        Assert.assertNotNull("exists",new CbusBasicNodeWithManagers(memo,123));
    }

    private CanSystemConnectionMemo memo = null;

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        
        memo = new CanSystemConnectionMemo();
        
    }

    @AfterEach
    public void tearDown() {
        Assertions.assertNotNull(memo);
        memo.dispose();
        memo = null;
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(CbusBasicNodeWithManagersTest.class);

}
