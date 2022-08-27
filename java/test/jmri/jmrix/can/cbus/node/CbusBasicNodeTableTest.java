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
public class CbusBasicNodeTableTest {

    @Test
    public void testCTor() {
        CanSystemConnectionMemo memo = new CanSystemConnectionMemo();
        Assert.assertNotNull("exists",new CbusBasicNodeTable(memo,1,2));
        memo.dispose();
    }
    
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(CbusBasicNodeTest.class);

}
