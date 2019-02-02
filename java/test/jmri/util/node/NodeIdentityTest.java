package jmri.util.node;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class NodeIdentityTest {

    @Test
    public void testNetworkIdentity() {
        Assert.assertNotNull(NodeIdentity.networkIdentity());
    }

    @Test
    public void testStorageIdentity() {
        Assert.assertNotNull(NodeIdentity.storageIdentity());
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetNodeIdentity();
    }

    @After
    public void tearDown() {
        JUnitUtil.resetNodeIdentity();
        JUnitUtil.tearDown();
    }
}
