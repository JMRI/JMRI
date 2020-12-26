package jmri.jmrix.can.cbus;

import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Paul Bender Copyright (C) 2019
 */
public class CbusCabSignalManagerTest {

    @Test
    public void testCtor() {
        
        CanSystemConnectionMemo memo = new CanSystemConnectionMemo();
        CbusCabSignalManager t = new CbusCabSignalManager(memo);
        Assert.assertNotNull("exists",t);
    }


    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(CbusCabSignalManagerTest.class);

}
