package jmri.jmrix.marklin.mcan.device;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link CdBDevice}.
 *
 * @author Ralf Lang (ralf.lang@ralf-lang.de)
 */
public class CdBDeviceTest {

    private static DeviceIdentity.CdBDeviceIdentity identity() {
        return DeviceIdentity.forCdB(0x1A, 3);
    }

    private static CdBDevice minimal() {
        return new CdBDevice(identity(), CdBDeviceClass.LAMPEN_CHEF, null, null);
    }

    // --- construction and accessors ---

    @Test
    public void testAccessorsReturnConstructorValues() {
        CdBDevice device = minimal();
        assertEquals(identity(),                device.identity());
        assertEquals(CdBDeviceClass.LAMPEN_CHEF, device.deviceClass());
    }

    @Test
    public void testHwFingerprintAbsentByDefault() {
        assertFalse(minimal().hwFingerprint().isPresent());
    }

    @Test
    public void testFactoryChallengeBytesAbsentByDefault() {
        assertFalse(minimal().factoryChallengeBytes().isPresent());
    }

    // --- immutability / defensive copies ---

    @Test
    public void testHwFingerprintIsStoredDefensively() {
        int[] fp = {0x01, 0x02, 0x03, 0x04};
        CdBDevice device = new CdBDevice(identity(), CdBDeviceClass.LAMPEN_CHEF, fp, null);
        fp[0] = 0xFF;
        assertEquals(0x01, device.hwFingerprint().get()[0]);
    }

    @Test
    public void testHwFingerprintGetterReturnsCopy() {
        int[] fp = {0x01, 0x02, 0x03, 0x04};
        CdBDevice device = new CdBDevice(identity(), CdBDeviceClass.LAMPEN_CHEF, fp, null);
        int[] out = device.hwFingerprint().get();
        out[0] = 0xFF;
        assertEquals(0x01, device.hwFingerprint().get()[0]);
    }

    @Test
    public void testFactoryChallengeBytesIsStoredDefensively() {
        int[] cb = {0xAA, 0xBB};
        CdBDevice device = new CdBDevice(identity(), CdBDeviceClass.LAMPEN_CHEF, null, cb);
        cb[0] = 0xFF;
        assertEquals(0xAA, device.factoryChallengeBytes().get()[0]);
    }

    // --- with* wither methods ---

    @Test
    public void testWithHwFingerprintReturnsNewInstanceWithFingerprint() {
        CdBDevice original = minimal();
        int[] fp           = {0x10, 0x20, 0x30, 0x40};
        CdBDevice withFp   = original.withHwFingerprint(fp);
        assertFalse(original.hwFingerprint().isPresent());
        assertArrayEquals(fp, withFp.hwFingerprint().get());
    }

    @Test
    public void testWithHwFingerprintRejectsWrongLength() {
        assertThrows(IllegalArgumentException.class,
                () -> minimal().withHwFingerprint(new int[]{0x01, 0x02, 0x03}));
    }

    @Test
    public void testWithFactoryChallengeBytesReturnsNewInstance() {
        CdBDevice original = minimal();
        int[] cb           = {0xCA, 0xFE};
        CdBDevice withCb   = original.withFactoryChallengeBytes(cb);
        assertFalse(original.factoryChallengeBytes().isPresent());
        assertArrayEquals(cb, withCb.factoryChallengeBytes().get());
    }

    @Test
    public void testWithFactoryChallengeBytesRejectsWrongLength() {
        assertThrows(IllegalArgumentException.class,
                () -> minimal().withFactoryChallengeBytes(new int[]{0x01}));
    }

    // --- hwFingerprint and factoryChallengeBytes are distinct concepts ---

    @Test
    public void testHwFingerprintAndFactoryChallengeBytesAreIndependent() {
        int[] fp = {0x01, 0x02, 0x03, 0x04};
        int[] cb = {0xAA, 0xBB};
        CdBDevice device = new CdBDevice(identity(), CdBDeviceClass.LAMPEN_CHEF, fp, cb);
        assertTrue(device.hwFingerprint().isPresent());
        assertTrue(device.factoryChallengeBytes().isPresent());
        assertEquals(4, device.hwFingerprint().get().length);
        assertEquals(2, device.factoryChallengeBytes().get().length);
    }

    // --- null guards ---

    @Test
    public void testConstructorRejectsNullIdentity() {
        assertThrows(NullPointerException.class, () ->
                new CdBDevice(null, CdBDeviceClass.LAMPEN_CHEF, null, null));
    }

    @Test
    public void testConstructorRejectsNullDeviceClass() {
        assertThrows(NullPointerException.class, () ->
                new CdBDevice(identity(), null, null, null));
    }
}
