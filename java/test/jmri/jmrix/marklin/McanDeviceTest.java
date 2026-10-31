package jmri.jmrix.marklin;

import jmri.jmrix.marklin.mcan.McanUid;

import jmri.jmrix.marklin.mcan.cs2.NativeDeviceInfo;
import jmri.util.JUnitUtil;
import org.junit.jupiter.api.*;
import java.beans.PropertyChangeEvent;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link McanDevice}.
 *
 * @author Ralf Lang (ralf.lang@ralf-lang.de)
 */
public class McanDeviceTest {

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    @Test
    public void testDeviceInitialization() {
        McanDeviceIdentity identity = McanDeviceIdentity.forNative(McanUid.of(0x00112233L), NativeDeviceInfo.TYPE_MS2);
        McanDevice device = new McanDevice("M", identity);

        assertEquals("M:dev:uid-00112233", device.getSystemName());
        assertEquals(identity, device.getIdentity());
        assertEquals(McanDeviceState.DISCOVERED, device.getDeviceState());
        assertEquals(-1, device.getSoftwareVersion());
        assertEquals(-1, device.getHardwareVersion());
        assertNotNull(device.getLastSeen());
    }

    @Test
    public void testNativeRuntimeStateSeparatesDefaultAndObservedHash() throws Exception {
        McanDeviceIdentity.Native identity = McanDeviceIdentity.forNative(McanUid.of(0x4D5552E2), 0x0033);
        McanDevice device = new McanDevice("M", identity);
        assertNull(device.getDeviceService());
        NativeDeviceService state = new NativeDeviceService(device);
        device.setDeviceService(state);
        assertSame(state, NativeDeviceService.get(device).orElseThrow());
        assertEquals(0x1F37, state.getDefaultHash());
        assertTrue(state.getObservedHash().isEmpty());
        assertTrue(NativeDeviceService.get(new McanDevice("M", McanDeviceIdentity.forCdB(0x22, 1))).isEmpty());
        assertThrows(IllegalArgumentException.class, () -> new NativeDeviceService(
                new McanDevice("M", McanDeviceIdentity.forCdB(0x22, 1))));

        AtomicReference<PropertyChangeEvent> eventRef = new AtomicReference<>();
        state.addPropertyChangeListener(eventRef::set);
        javax.swing.SwingUtilities.invokeAndWait(() -> state.setObservedHash(0xFF37));
        assertEquals(0xFF37, state.getObservedHash().orElseThrow());
        assertEquals(0x1F37, state.getDefaultHash());
        assertEquals(identity, device.getIdentity());
        assertSame(state, eventRef.get().getSource());
        assertEquals(-1, eventRef.get().getOldValue());
        assertEquals(0xFF37, eventRef.get().getNewValue());
        javax.swing.SwingUtilities.invokeAndWait(() -> state.setObservedHash(0xFB21));
        assertEquals(0xFF37, eventRef.get().getOldValue());
        assertEquals(0xFB21, state.getObservedHash().orElseThrow());
        assertThrows(IllegalArgumentException.class, () -> state.setObservedHash(0x10000));
        assertThrows(IllegalArgumentException.class, () -> state.setObservedHash(0x0380));
        assertEquals(0xFB21, state.getObservedHash().orElseThrow());
    }

    @Test
    public void testStateChangeFiresPropertyChangeEvent() {
        McanDeviceIdentity identity = McanDeviceIdentity.forCdB(0x22, 0x01);
        McanDevice device = new McanDevice("M", identity);

        AtomicReference<PropertyChangeEvent> eventRef = new AtomicReference<>();
        device.addPropertyChangeListener(McanDevice.PROPERTY_DEVICE_STATE, eventRef::set);

        device.setDeviceState(McanDeviceState.ONLINE);

        JUnitUtil.waitFor(() -> eventRef.get() != null, "State change event fired");
        PropertyChangeEvent event = eventRef.get();
        assertNotNull(event);
        assertEquals(McanDevice.PROPERTY_DEVICE_STATE, event.getPropertyName());
        assertEquals(McanDeviceState.DISCOVERED, event.getOldValue());
        assertEquals(McanDeviceState.ONLINE, event.getNewValue());
    }

    @Test
    public void testVersionAndTouch() {
        McanDeviceIdentity identity = McanDeviceIdentity.forNative(McanUid.of(0x12345678L), 0x0010);
        McanDevice device = new McanDevice("M", identity);

        device.setSoftwareVersion(0x0102);
        device.setHardwareVersion(0x0200);

        assertEquals(0x0102, device.getSoftwareVersion());
        assertEquals(0x0200, device.getHardwareVersion());

        device.touch();
        assertNotNull(device.getLastSeen());
    }
}
