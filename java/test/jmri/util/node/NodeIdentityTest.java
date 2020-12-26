package jmri.util.node;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

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

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetNodeIdentity();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.resetNodeIdentity();
        JUnitUtil.tearDown();
    }
}
