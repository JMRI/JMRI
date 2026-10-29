package jmri.jmrix.marklin;

import jmri.jmrix.marklin.mcan.protocol.NativeDeviceInfo;
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
        McanDeviceIdentity identity = McanDeviceIdentity.forNative(0x00112233L, NativeDeviceInfo.TYPE_MS2);
        McanDevice device = new McanDevice("M", identity);

        assertEquals("M:dev:uid-00112233", device.getSystemName());
        assertEquals(identity, device.getIdentity());
        assertEquals(McanDeviceState.DISCOVERED, device.getDeviceState());
        assertEquals(-1, device.getSoftwareVersion());
        assertEquals(-1, device.getHardwareVersion());
        assertNotNull(device.getLastSeen());
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
        McanDeviceIdentity identity = McanDeviceIdentity.forNative(0x12345678L, 0x0010);
        McanDevice device = new McanDevice("M", identity);

        device.setSoftwareVersion(0x0102);
        device.setHardwareVersion(0x0200);

        assertEquals(0x0102, device.getSoftwareVersion());
        assertEquals(0x0200, device.getHardwareVersion());

        device.touch();
        assertNotNull(device.getLastSeen());
    }
}
