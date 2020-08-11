package jmri.jmrix.rps.trackingpanel;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

import jmri.jmrix.rps.RpsSystemConnectionMemo;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class RpsTrackingControlPaneTest {

    RpsSystemConnectionMemo memo = null;

    @Test
    public void testCTor() {
        RpsTrackingPanel p = new RpsTrackingPanel(memo);
        RpsTrackingControlPane t = new RpsTrackingControlPane(p);
        Assert.assertNotNull("exists",t);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        memo = new RpsSystemConnectionMemo();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(RpsTrackingControlPaneTest.class);

}
