package jmri.jmrix.can.cbus.simulator;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Steve Young Copyright (c) 2019
 */
public class CbusSimCanListenerTest {

    @Test
    public void testCTor() {
        CbusSimCanListener t = new CbusSimCanListener(null,null);
        Assert.assertNotNull("exists",t);
        t.dispose();
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(CbusSimCanListenerTest.class);

}
