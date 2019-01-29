package jmri.util.node;

import static jmri.util.node.NodeIdentity.generateUuid;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class NodeIdentityTest {

    @Test
    public void testIdentity() {
        Assert.assertNotNull(NodeIdentity.networkIdentity());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testGenerateUuidNull() {
        // this test probably isn't required, since the parameter to 
        // generateUuid is now marked as Nonnull
        Assert.assertNotNull("UUID generated",generateUuid(null));
    }

    @Test
    public void testGenerateUuid() {
        byte mac[] = {(byte) 0x70, (byte) 0xcd, (byte) 0x60, (byte) 0xaa, (byte) 0xce, (byte) 0xa6};
        Assert.assertNotNull("UUID generated",generateUuid(mac));
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

    private final static Logger log = LoggerFactory.getLogger(NodeIdentityTest.class);
}
