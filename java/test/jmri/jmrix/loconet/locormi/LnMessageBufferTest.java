package jmri.jmrix.loconet.locormi;

import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        memo = new LocoNetSystemConnectionMemo();
    }

    @After
    public void tearDown() {
        memo.dispose();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(LnMessageBufferTest.class);

}
