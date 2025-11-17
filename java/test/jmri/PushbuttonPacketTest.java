package jmri;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        assertEquals(c1.length, c2.length);
        assertTrue(c1.length > 0);
        assertTrue(c1[0].equals(c2[0]));
        c1[0] = "foo";
        assertTrue(!c1[0].equals(c2[0]));
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
