package jmri.jmrix.can.adapters.gridconnect.net;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class MergNetworkDriverAdapterTest {

    @Test
    public void testCTor() {
        MergNetworkDriverAdapter t = new MergNetworkDriverAdapter();
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

    // private final static Logger log = LoggerFactory.getLogger(MergNetworkDriverAdapterTest.class);

}
