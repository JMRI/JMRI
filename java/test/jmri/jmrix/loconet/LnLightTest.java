package jmri.jmrix.loconet;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class LnLightTest {

    @Test
    public void testCTor() {
        LocoNetSystemConnectionMemo memo = new LocoNetSystemConnectionMemo();
        LnTrafficController lnis = new LocoNetInterfaceScaffold(memo);
        memo.setLnTrafficController(lnis);
        LnLightManager lm = new LnLightManager(memo);
        LnLight t = new LnLight("LL1", lnis, lm);
        Assert.assertNotNull("exists", t);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(LnLightTest.class);
}
