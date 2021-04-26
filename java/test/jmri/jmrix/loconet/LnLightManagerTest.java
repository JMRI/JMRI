package jmri.jmrix.loconet;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class LnLightManagerTest {

    @Test
    public void testCTor() {
        LocoNetSystemConnectionMemo memo = new LocoNetSystemConnectionMemo();
        LnTrafficController lnis = new LocoNetInterfaceScaffold(memo);
        memo.setLnTrafficController(lnis);
        LnLightManager t = new LnLightManager(memo);
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

    // private final static Logger log = LoggerFactory.getLogger(LnLightManagerTest.class);

}
