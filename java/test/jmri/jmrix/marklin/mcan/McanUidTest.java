package jmri.jmrix.marklin.mcan;

import static org.junit.jupiter.api.Assertions.*;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link McanUid}.
 */
public class McanUidTest {

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }


    @Test
    public void testMasksToUnsigned32Bits() {
        McanUid uid = McanUid.of(-1);
        assertEquals(0xFFFFFFFFL, uid.toLong());
        assertTrue(uid.isUninitialized());
        assertEquals(McanUid.UNINITIALIZED, uid);
        assertEquals(McanUid.of(0x1_12345678L), McanUid.of(0x12345678L));
    }

    @Test
    public void testOrderingIsUnsigned() {
        assertTrue(McanUid.of(0x7FFFFFFFL).compareTo(McanUid.of(0x80000000L)) < 0);
        assertTrue(McanUid.of(0xFFFFFFFEL).compareTo(McanUid.of(0x00000001L)) > 0);
    }

    @Test
    public void testCalculateHash() {
        assertEquals(0x1234 ^ 0x5678, McanUid.of(0x12345678L).calculateHash());
    }

    @Test
    public void testToHexStringAndBroadcast() {
        assertEquals("0x0000ABCD", McanUid.of(0xABCDL).toHexString());
        assertTrue(McanUid.of(0).isBroadcast());
        assertFalse(McanUid.of(0).isUninitialized());
    }
}
