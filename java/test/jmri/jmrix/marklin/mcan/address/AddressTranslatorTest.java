package jmri.jmrix.marklin.mcan.address;
import jmri.util.JUnitUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import org.junit.jupiter.api.Test;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the accessory address convention methods on {@link AddressSpace}.
 *
 * @author Ralf Lang (ralf.lang@ralf-lang.de)
 */
public class AddressTranslatorTest {

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // --- DCC accessory ---

    @Test
    public void testDccAccessoryAddress1MapsToBaseOffset() {
        Optional<Integer> result = AddressSpace.DCC_ACCESSORY.fromUserAddress(1);
        assertTrue(result.isPresent());
        assertEquals(AddressSpace.DCC_ACCESSORY.base(), result.get());
    }

    @Test
    public void testDccAccessoryMaxAddressMapsToLastOffset() {
        int max = AddressSpace.DCC_ACCESSORY.size();
        Optional<Integer> result = AddressSpace.DCC_ACCESSORY.fromUserAddress(max);
        assertTrue(result.isPresent());
        assertEquals(AddressSpace.DCC_ACCESSORY.base() + max - 1, result.get());
    }

    @Test
    public void testDccAccessoryZeroAddressIsOutOfRange() {
        assertFalse(AddressSpace.DCC_ACCESSORY.fromUserAddress(0).isPresent());
    }

    @Test
    public void testDccAccessoryOverMaxIsOutOfRange() {
        int overMax = AddressSpace.DCC_ACCESSORY.size() + 1;
        assertFalse(AddressSpace.DCC_ACCESSORY.fromUserAddress(overMax).isPresent());
    }

    @Test
    public void testCanOffsetToDccAccessoryRoundTrips() {
        for (int addr = 1; addr <= 10; addr++) {
            int offset = AddressSpace.DCC_ACCESSORY.fromUserAddress(addr).get();
            Optional<Integer> back = AddressSpace.DCC_ACCESSORY.toUserAddress(offset);
            assertTrue(back.isPresent());
            assertEquals(addr, back.get());
        }
    }

    @Test
    public void testCanOffsetBelowDccRangeReturnsEmpty() {
        assertFalse(AddressSpace.DCC_ACCESSORY.toUserAddress(
                AddressSpace.DCC_ACCESSORY.base() - 1).isPresent());
    }

    @Test
    public void testCanOffsetAboveDccRangeReturnsEmpty() {
        int aboveRange = AddressSpace.DCC_ACCESSORY.base() + AddressSpace.DCC_ACCESSORY.size();
        assertFalse(AddressSpace.DCC_ACCESSORY.toUserAddress(aboveRange).isPresent());
    }

    // --- MM accessory ---

    @Test
    public void testMmAccessoryAddress1MapsToBaseOffset() {
        Optional<Integer> result = AddressSpace.MM_ACCESSORY.fromUserAddress(1);
        assertTrue(result.isPresent());
        assertEquals(AddressSpace.MM_ACCESSORY.base(), result.get());
    }

    @Test
    public void testMmAccessoryRoundTrips() {
        for (int addr = 1; addr <= 10; addr++) {
            int offset = AddressSpace.MM_ACCESSORY.fromUserAddress(addr).get();
            Optional<Integer> back = AddressSpace.MM_ACCESSORY.toUserAddress(offset);
            assertTrue(back.isPresent());
            assertEquals(addr, back.get());
        }
    }

    @Test
    public void testMmAccessoryZeroAddressIsOutOfRange() {
        assertFalse(AddressSpace.MM_ACCESSORY.fromUserAddress(0).isPresent());
    }

    // --- detectAddressSpace (now AddressSpace.forCanOffset) ---

    @Test
    public void testDetectAddressSpaceReturnsDccForDccOffset() {
        int offset = AddressSpace.DCC_ACCESSORY.base() + 5;
        Optional<AddressSpace> space = AddressSpace.forCanOffset(offset);
        assertTrue(space.isPresent());
        assertEquals(AddressSpace.DCC_ACCESSORY, space.get());
    }

    @Test
    public void testDetectAddressSpaceReturnsMmForMmOffset() {
        int offset = AddressSpace.MM_ACCESSORY.base() + 5;
        Optional<AddressSpace> space = AddressSpace.forCanOffset(offset);
        assertTrue(space.isPresent());
        assertEquals(AddressSpace.MM_ACCESSORY, space.get());
    }

    @Test
    public void testDetectAddressSpaceReturnsEmptyForUnknownOffset() {
        // 0x3400–0x37FF is a gap between MM_ACCESSORY (ends 0x33FF) and DCC_ACCESSORY (starts 0x3800)
        assertFalse(AddressSpace.forCanOffset(0x3600).isPresent());
    }
}
