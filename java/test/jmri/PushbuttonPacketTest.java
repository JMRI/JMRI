package jmri;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 * Tests for the PushbuttonPacket class
 *
 * @author Bob Jacobsen Copyright (C) 2010
 */
public class PushbuttonPacketTest {

    @Test
    public void testImmutableNames() {
        String[] c1 = PushbuttonPacket.getValidDecoderNames();
        String[] c2 = PushbuttonPacket.getValidDecoderNames();
        Assert.assertEquals(c1.length, c2.length);
        Assert.assertTrue(c1.length > 0);
        Assert.assertTrue(c1[0].equals(c2[0]));
        c1[0] = "foo";
        Assert.assertTrue(!c1[0].equals(c2[0]));
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
