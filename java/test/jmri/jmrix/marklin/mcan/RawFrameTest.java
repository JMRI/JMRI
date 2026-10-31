package jmri.jmrix.marklin.mcan.classify;
import jmri.util.JUnitUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link RawFrame}.
 *
 * @author Ralf Lang (ralf.lang@ralf-lang.de)
 */
public class RawFrameTest {

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    @Test
    public void testConstructorClonesInput() {
        int[] raw = new int[13];
        raw[0] = 0x12;
        RawFrame frame = new RawFrame(raw);
        raw[0] = 0xFF;  // mutate original
        assertEquals(0x12, frame.byteAt(0));
    }

    @Test
    public void testToArrayReturnsCopy() {
        int[] raw = new int[13];
        raw[5] = 0x42;
        RawFrame frame = new RawFrame(raw);
        int[] out = frame.toArray();
        out[5] = 0x00;  // mutate returned copy
        assertEquals(0x42, frame.byteAt(5));
    }

    @Test
    public void testByteAtMasksToUnsigned() {
        int[] raw = new int[13];
        raw[3] = 0xFF;
        RawFrame frame = new RawFrame(raw);
        assertEquals(0xFF, frame.byteAt(3));
    }

    @Test
    public void testDlcReadsLowNibbleOfByte4() {
        int[] raw = new int[13];
        raw[4] = 0xF4;  // high nibble 0xF should be masked away
        RawFrame frame = new RawFrame(raw);
        assertEquals(4, frame.dlc());
    }

    @Test
    public void testConstructorRejectsNull() {
        assertThrows(IllegalArgumentException.class, () -> new RawFrame(null));
    }

    @Test
    public void testConstructorRejectsWrongLength() {
        assertThrows(IllegalArgumentException.class, () -> new RawFrame(new int[12]));
        assertThrows(IllegalArgumentException.class, () -> new RawFrame(new int[14]));
    }
}
