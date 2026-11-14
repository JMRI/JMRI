package jmri.jmrix.marklin.mcan.address;
import jmri.util.JUnitUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link CdBAddressCodec} and {@link AddressEncoding}.
 *
 * @author Ralf Lang (ralf.lang@ralf-lang.de)
 */
public class CdBAddressCodecTest {

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    private final CdBAddressCodec codec = CdBAddressCodec.INSTANCE;

    // --- type-2 encode ---

    @Test
    public void testEncodeType2DccAddress1() {
        // DCC address 1 → 14336 + 1 - 1 = 14336
        assertEquals(14336, codec.encodeType2("DCC", 1));
    }

    @Test
    public void testEncodeType2DccAddress100() {
        assertEquals(14336 + 99, codec.encodeType2("DCC", 100));
    }

    @Test
    public void testEncodeType2MmAddress1() {
        // MM address 1 → 12288 + 1 - 1 = 12288
        assertEquals(12288, codec.encodeType2("MM", 1));
    }

    @Test
    public void testEncodeType2MmAddress50() {
        assertEquals(12288 + 49, codec.encodeType2("MM", 50));
    }

    @Test
    public void testEncodeType2CanAddressIsPassthrough() {
        assertEquals(42, codec.encodeType2("CAN", 42));
        assertEquals(0,  codec.encodeType2("CAN", 0));
    }

    @Test
    public void testEncodeType2UnknownFormatThrows() {
        assertThrows(IllegalArgumentException.class, () -> codec.encodeType2("SX", 1));
    }

    // --- type-2 decode ---

    @Test
    public void testDecodeType2DccRange() {
        AddressEncoding ae = codec.decodeType2(14336);
        assertEquals("DCC", ae.format());
        assertEquals(1,     ae.address());
    }

    @Test
    public void testDecodeType2MmRange() {
        AddressEncoding ae = codec.decodeType2(12288);
        assertEquals("MM", ae.format());
        assertEquals(1,    ae.address());
    }

    @Test
    public void testDecodeType2CanRange() {
        AddressEncoding ae = codec.decodeType2(5);
        assertEquals("CAN", ae.format());
        assertEquals(5,     ae.address());
    }

    @Test
    public void testDecodeType2CanRangeZero() {
        AddressEncoding ae = codec.decodeType2(0);
        assertEquals("CAN", ae.format());
        assertEquals(0,     ae.address());
    }

    // --- type-2 round-trip ---

    @Test
    public void testType2RoundTripDcc() {
        for (int addr = 1; addr <= 20; addr++) {
            int raw = codec.encodeType2("DCC", addr);
            AddressEncoding back = codec.decodeType2(raw);
            assertEquals("DCC", back.format());
            assertEquals(addr,  back.address(), "DCC round-trip failed for address " + addr);
        }
    }

    @Test
    public void testType2RoundTripMm() {
        for (int addr = 1; addr <= 20; addr++) {
            int raw = codec.encodeType2("MM", addr);
            AddressEncoding back = codec.decodeType2(raw);
            assertEquals("MM", back.format());
            assertEquals(addr, back.address(), "MM round-trip failed for address " + addr);
        }
    }

    @Test
    public void testType2RoundTripCan() {
        for (int addr = 0; addr < 20; addr++) {
            int raw = codec.encodeType2("CAN", addr);
            AddressEncoding back = codec.decodeType2(raw);
            assertEquals("CAN", back.format());
            assertEquals(addr,  back.address(), "CAN round-trip failed for address " + addr);
        }
    }

    // --- type-3 encode ---

    @Test
    public void testEncodeType3DccAddress1() {
        // DCC type-3: 12288 + 1 - 1 = 12288
        assertEquals(12288, codec.encodeType3("DCC", 1));
    }

    @Test
    public void testEncodeType3DccExtAddress1() {
        // DCC ext. type-3: 12800 + 1 + 3 = 12804
        assertEquals(12804, codec.encodeType3("DCC ext.", 1));
    }

    @Test
    public void testEncodeType3UnknownFormatThrows() {
        assertThrows(IllegalArgumentException.class, () -> codec.encodeType3("MM", 1));
        assertThrows(IllegalArgumentException.class, () -> codec.encodeType3("CAN", 1));
    }

    // --- type-3 decode ---

    @Test
    public void testDecodeType3DccRange() {
        AddressEncoding ae = codec.decodeType3(12288);
        assertEquals("DCC", ae.format());
        assertEquals(1,     ae.address());
    }

    @Test
    public void testDecodeType3DccExtRange() {
        AddressEncoding ae = codec.decodeType3(12804);
        assertEquals("DCC ext.", ae.format());
        assertEquals(1,          ae.address());
    }

    // --- type-3 round-trip ---

    @Test
    public void testType3RoundTripDcc() {
        for (int addr = 1; addr <= 20; addr++) {
            int raw = codec.encodeType3("DCC", addr);
            AddressEncoding back = codec.decodeType3(raw);
            assertEquals("DCC", back.format());
            assertEquals(addr,  back.address(), "DCC type-3 round-trip failed for address " + addr);
        }
    }

    @Test
    public void testType3RoundTripDccExt() {
        for (int addr = 1; addr <= 20; addr++) {
            int raw = codec.encodeType3("DCC ext.", addr);
            AddressEncoding back = codec.decodeType3(raw);
            assertEquals("DCC ext.", back.format());
            assertEquals(addr,       back.address(), "DCC ext. type-3 round-trip failed for address " + addr);
        }
    }

    // --- AddressEncoding value semantics ---

    @Test
    public void testAddressEncodingEquality() {
        AddressEncoding a = new AddressEncoding("DCC", 5);
        AddressEncoding b = new AddressEncoding("DCC", 5);
        AddressEncoding c = new AddressEncoding("MM",  5);
        assertEquals(a, b);
        assertNotEquals(a, c);
    }

    @Test
    public void testAddressEncodingToString() {
        assertEquals("DCC:42", new AddressEncoding("DCC", 42).toString());
    }
}
