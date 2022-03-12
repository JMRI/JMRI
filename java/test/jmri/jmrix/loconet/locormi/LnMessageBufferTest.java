package jmri.jmrix.loconet.locormi;

import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class LnMessageBufferTest {

    private LocoNetSystemConnectionMemo memo;

    @Test
    public void testCTor() throws java.rmi.RemoteException{
        LnMessageBuffer t = new LnMessageBuffer(memo.getLnTrafficController());
        Assert.assertNotNull("exists", t);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        memo = new LocoNetSystemConnectionMemo();
    }

    @AfterEach
    public void tearDown() {
        memo.dispose();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(LnMessageBufferTest.class);

}
