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
public class CbusBasicNodeTest {

    @Test
    public void testCTor() {
        Assert.assertNotNull("exists",new CbusBasicNode(null,123));
    }

    @Test
    public void testCTorWithCanMemo() {
        Assert.assertNotNull("exists",new CbusBasicNode(memo,123));
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
        
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(CbusBasicNodeTest.class);

}
