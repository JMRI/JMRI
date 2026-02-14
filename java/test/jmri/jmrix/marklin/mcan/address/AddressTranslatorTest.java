package jmri.jmrix.marklin.mcan.address;

import org.junit.jupiter.api.Test;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link AddressTranslator}.
 *
 * @author Ralf Lang (ralf.lang@ralf-lang.de)
 */
public class AddressTranslatorTest {

    // --- DCC accessory ---

    @Test
    public void testDccAccessoryAddress1MapsToBaseOffset() {
        Optional<Integer> result = AddressTranslator.dccAccessoryToCanOffset(1);
        assertTrue(result.isPresent());
        assertEquals(AddressTranslator.BASE_DCC_ACCESSORY, result.get());
    }

    @Test
    public void testDccAccessoryMaxAddressMapsToLastOffset() {
        int max = AddressTranslator.MAX_DCC_ACCESSORY;
        Optional<Integer> result = AddressTranslator.dccAccessoryToCanOffset(max);
        assertTrue(result.isPresent());
        assertEquals(AddressTranslator.BASE_DCC_ACCESSORY + max - 1, result.get());
    }

    @Test
    public void testDccAccessoryZeroAddressIsOutOfRange() {
        assertFalse(AddressTranslator.dccAccessoryToCanOffset(0).isPresent());
    }

    @Test
    public void testDccAccessoryOverMaxIsOutOfRange() {
        int overMax = AddressTranslator.MAX_DCC_ACCESSORY + 1;
        assertFalse(AddressTranslator.dccAccessoryToCanOffset(overMax).isPresent());
    }

    @Test
    public void testCanOffsetToDccAccessoryRoundTrips() {
        for (int addr = 1; addr <= 10; addr++) {
            int offset = AddressTranslator.dccAccessoryToCanOffset(addr).get();
            Optional<Integer> back = AddressTranslator.canOffsetToDccAccessory(offset);
            assertTrue(back.isPresent());
            assertEquals(addr, back.get());
        }
    }

    @Test
    public void testCanOffsetBelowDccRangeReturnsEmpty() {
        assertFalse(AddressTranslator.canOffsetToDccAccessory(
                AddressTranslator.BASE_DCC_ACCESSORY - 1).isPresent());
    }

    @Test
    public void testCanOffsetAboveDccRangeReturnsEmpty() {
        int aboveRange = AddressTranslator.BASE_DCC_ACCESSORY + AddressTranslator.MAX_DCC_ACCESSORY;
        assertFalse(AddressTranslator.canOffsetToDccAccessory(aboveRange).isPresent());
    }

    // --- MM accessory ---

    @Test
    public void testMmAccessoryAddress1MapsToBaseOffset() {
        Optional<Integer> result = AddressTranslator.mmAccessoryToCanOffset(1);
        assertTrue(result.isPresent());
        assertEquals(AddressTranslator.BASE_MM_ACCESSORY, result.get());
    }

    @Test
    public void testMmAccessoryRoundTrips() {
        for (int addr = 1; addr <= 10; addr++) {
            int offset = AddressTranslator.mmAccessoryToCanOffset(addr).get();
            Optional<Integer> back = AddressTranslator.canOffsetToMmAccessory(offset);
            assertTrue(back.isPresent());
            assertEquals(addr, back.get());
        }
    }

    @Test
    public void testMmAccessoryZeroAddressIsOutOfRange() {
        assertFalse(AddressTranslator.mmAccessoryToCanOffset(0).isPresent());
    }

    // --- detectAddressSpace ---

    @Test
    public void testDetectAddressSpaceReturnsDccForDccOffset() {
        int offset = AddressTranslator.BASE_DCC_ACCESSORY + 5;
        Optional<AddressSpace> space = AddressTranslator.detectAddressSpace(offset);
        assertTrue(space.isPresent());
        assertEquals(AddressSpace.DCC_ACCESSORY, space.get());
    }

    @Test
    public void testDetectAddressSpaceReturnsMmForMmOffset() {
        int offset = AddressTranslator.BASE_MM_ACCESSORY + 5;
        Optional<AddressSpace> space = AddressTranslator.detectAddressSpace(offset);
        assertTrue(space.isPresent());
        assertEquals(AddressSpace.MM_ACCESSORY, space.get());
    }

    @Test
    public void testDetectAddressSpaceReturnsEmptyForUnknownOffset() {
        // 0x3400–0x37FF is a gap between MM_ACCESSORY (ends 0x33FF) and DCC_ACCESSORY (starts 0x3800)
        assertFalse(AddressTranslator.detectAddressSpace(0x3600).isPresent());
    }
}
