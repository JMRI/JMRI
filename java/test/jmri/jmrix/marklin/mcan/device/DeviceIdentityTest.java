package jmri.jmrix.marklin.mcan.device;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link DeviceIdentity} and its inner types.
 *
 * @author Ralf Lang (ralf.lang@ralf-lang.de)
 */
public class DeviceIdentityTest {

    // --- NativeDeviceIdentity ---

    @Test
    public void testForNativeReturnsNativeIdentity() {
        DeviceIdentity id = DeviceIdentity.forNative(0x00112233L, 0x0060);
        assertTrue(id.isNative());
        assertFalse(id.isCdB());
    }

    @Test
    public void testNativeIdFragmentContainsUid() {
        DeviceIdentity id = DeviceIdentity.forNative(0x00112233L, 0x0060);
        assertTrue(id.idFragment().contains("00112233"),
                "idFragment should contain the UID hex: " + id.idFragment());
    }

    @Test
    public void testNativeDisplayNameContainsUidAndType() {
        DeviceIdentity.NativeDeviceIdentity id = DeviceIdentity.forNative(0xDEADBEEFL, 0x0042);
        String name = id.displayName();
        assertTrue(name.contains("DEADBEEF"), "displayName missing UID: " + name);
        assertTrue(name.contains("0042"),     "displayName missing type: " + name);
    }

    @Test
    public void testNativeAccessors() {
        DeviceIdentity.NativeDeviceIdentity id = DeviceIdentity.forNative(0xABCDL, 0x77);
        assertEquals(0xABCDL, id.uid());
        assertEquals(0x77,    id.deviceType());
    }

    @Test
    public void testNativeEqualityByUidAndType() {
        DeviceIdentity a = DeviceIdentity.forNative(0x01L, 0x10);
        DeviceIdentity b = DeviceIdentity.forNative(0x01L, 0x10);
        DeviceIdentity c = DeviceIdentity.forNative(0x01L, 0x11);
        assertEquals(a, b);
        assertNotEquals(a, c);
    }

    // --- CdBDeviceIdentity ---

    @Test
    public void testForCdBReturnsCdBIdentity() {
        DeviceIdentity id = DeviceIdentity.forCdB(0x1A, 3);
        assertFalse(id.isNative());
        assertTrue(id.isCdB());
    }

    @Test
    public void testCdBIdFragmentContainsClassAndModule() {
        DeviceIdentity id = DeviceIdentity.forCdB(0x1A, 3);
        String frag = id.idFragment();
        assertTrue(frag.contains("1a"), "idFragment missing class: " + frag);
        assertTrue(frag.contains("03"), "idFragment missing module: " + frag);
    }

    @Test
    public void testCdBAccessors() {
        DeviceIdentity.CdBDeviceIdentity id = DeviceIdentity.forCdB(0x20, 7);
        assertEquals(0x20, id.canClass());
        assertEquals(7,    id.moduleId());
    }

    @Test
    public void testCdBEqualityByClassAndModule() {
        DeviceIdentity a = DeviceIdentity.forCdB(0x10, 1);
        DeviceIdentity b = DeviceIdentity.forCdB(0x10, 1);
        DeviceIdentity c = DeviceIdentity.forCdB(0x10, 2);
        assertEquals(a, b);
        assertNotEquals(a, c);
    }

    @Test
    public void testNativeAndCdBIdentitiesAreNeverEqual() {
        // Same numeric values, different types
        DeviceIdentity native_ = DeviceIdentity.forNative(0x10L, 1);
        DeviceIdentity cdb     = DeviceIdentity.forCdB(0x10, 1);
        assertNotEquals(native_, cdb);
    }
}
