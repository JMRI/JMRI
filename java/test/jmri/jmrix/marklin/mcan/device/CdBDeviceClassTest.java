package jmri.jmrix.marklin.mcan.device;

import org.junit.jupiter.api.Test;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link CdBDeviceClass}.
 *
 * @author Ralf Lang (ralf.lang@ralf-lang.de)
 */
public class CdBDeviceClassTest {

    @Test
    public void testForClassIdReturnsKnownClass() {
        Optional<CdBDeviceClass> result = CdBDeviceClass.forClassId(0x4A);
        assertTrue(result.isPresent());
        assertEquals(CdBDeviceClass.LAMPEN_CHEF, result.get());
    }

    @Test
    public void testForClassIdReturnsEmptyForUnknownId() {
        Optional<CdBDeviceClass> result = CdBDeviceClass.forClassId(0xFF);
        assertFalse(result.isPresent());
    }

    @Test
    public void testForClassIdReturnsEmptyForZero() {
        Optional<CdBDeviceClass> result = CdBDeviceClass.forClassId(0x00);
        assertFalse(result.isPresent());
    }

    @Test
    public void testNewProtocolClassesHaveCorrectGeneration() {
        assertEquals(CdBDeviceClass.ProtocolGeneration.NEW,
                CdBDeviceClass.WEICHEN_CHEF_KOMBI.getProtocolGeneration());
        assertEquals(CdBDeviceClass.ProtocolGeneration.NEW,
                CdBDeviceClass.GLEIS_REPORTER.getProtocolGeneration());
    }

    @Test
    public void testOldProtocolClassesHaveCorrectGeneration() {
        assertEquals(CdBDeviceClass.ProtocolGeneration.OLD,
                CdBDeviceClass.LAMPEN_CHEF.getProtocolGeneration());
    }

    @Test
    public void testGetClassIdMatchesLookup() {
        for (CdBDeviceClass dc : CdBDeviceClass.values()) {
            Optional<CdBDeviceClass> found = CdBDeviceClass.forClassId(dc.getClassId());
            assertTrue(found.isPresent(), dc.name() + " not found by its own classId");
            assertEquals(dc, found.get(), dc.name() + " lookup returned wrong entry");
        }
    }

    @Test
    public void testAllClassIdsAreDistinct() {
        long distinctIds = java.util.Arrays.stream(CdBDeviceClass.values())
                .mapToInt(CdBDeviceClass::getClassId)
                .distinct()
                .count();
        assertEquals(CdBDeviceClass.values().length, distinctIds,
                "Two CdBDeviceClass entries share the same classId");
    }

    @Test
    public void testWeichenChefMagnetIsAmbiguous() {
        assertEquals(CdBDeviceClass.HardwareCapability.AMBIGUOUS,
                CdBDeviceClass.WEICHEN_CHEF_MAGNET.getHardwareCapability());
    }

    @Test
    public void testAllOtherClassesAreSingleGen() {
        for (CdBDeviceClass dc : CdBDeviceClass.values()) {
            if (dc != CdBDeviceClass.WEICHEN_CHEF_MAGNET) {
                assertEquals(CdBDeviceClass.HardwareCapability.SINGLE_GEN,
                        dc.getHardwareCapability(),
                        dc.name() + " should be SINGLE_GEN");
            }
        }
    }
}
