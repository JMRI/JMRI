package jmri.jmrix.loconet.loconetovertcp;

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
public class LnTcpDriverAdapterTest {

    private LocoNetSystemConnectionMemo memo;

    @Test
    public void testCTor() {
        LnTcpDriverAdapter t = new LnTcpDriverAdapter();
        Assert.assertNotNull("exists", t);
    }

    @Test
    public void testMemoCTor() {
        LnTcpDriverAdapter tm = new LnTcpDriverAdapter(memo);
        Assert.assertNotNull("exists", tm);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        memo = new LocoNetSystemConnectionMemo();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(LnTcpDriverAdapterTest.class);

}
