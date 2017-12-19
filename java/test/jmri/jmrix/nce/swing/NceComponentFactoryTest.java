package jmri.jmrix.nce.swing;

import jmri.jmrix.nce.NceSystemConnectionMemo;
import jmri.jmrix.nce.NceTrafficControlScaffold;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class NceComponentFactoryTest {

    private NceTrafficControlScaffold tcis = null;
    private NceSystemConnectionMemo memo = null;

    @Test
    public void testCTor() {
        NceComponentFactory t = new NceComponentFactory(memo);
        Assert.assertNotNull("exists",t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        tcis = new NceTrafficControlScaffold();
        memo = new NceSystemConnectionMemo();
        memo.setNceTrafficController(tcis);
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(NceComponentFactoryTest.class);

}
