package jmri.util.node;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class NodeIdentityTest {

    @Test
    public void testNetworkIdentity() {
        assertNotNull(NodeIdentity.networkIdentity());
    }

    @Test
    public void testStorageIdentity() {
        assertNotNull(NodeIdentity.storageIdentity());
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
