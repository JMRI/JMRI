package jmri.jmrix.marklin;

import jmri.util.JUnitUtil;
import org.junit.jupiter.api.*;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link McanParameterManager}.
 *
 * @author Ralf Lang (ralf.lang@ralf-lang.de)
 */
public class McanParameterManagerTest {

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    @Test
    public void testCdBReadAndWriteParameter() {
        McanDeviceIdentity identity = McanDeviceIdentity.forCdB(0x22, 0x03);
        McanDevice device = new McanDevice("M", identity);
        McanParameterManager paramManager = new McanParameterManager(device);

        assertEquals(device, paramManager.getDevice());

        AtomicReference<int[]> sentFrame = new AtomicReference<>();
        McanFrameSender sender = sentFrame::set;

        // Read parameter 1  -  uses new-protocol read frame (buildReadFrame)
        paramManager.readParameter(1, sender);
        assertNotNull(sentFrame.get());
        int[] readFrame = sentFrame.get();
        assertEquals(13, readFrame.length);
        assertEquals(0x83, readFrame[3] & 0xFF); // module 3 + 0x80
        assertEquals(2, readFrame[4] & 0xFF);    // DLC=2
        assertEquals(0, readFrame[5] & 0xFF);    // slot 0 (global)
        assertEquals(1, readFrame[6] & 0xFF);    // reg = paramIndex

        // Write parameter 1 value 0x55  -  uses new-protocol write frame (buildWriteFrame)
        paramManager.writeParameter(1, 0x55, sender);
        assertNotNull(sentFrame.get());
        int[] writeFrame = sentFrame.get();
        assertEquals(13, writeFrame.length);
        assertEquals(0x83, writeFrame[3] & 0xFF); // module 3 + 0x80
        assertEquals(4, writeFrame[4] & 0xFF);    // DLC=4
        assertEquals(0, writeFrame[5] & 0xFF);    // slot 0
        assertEquals(1, writeFrame[6] & 0xFF);    // reg = paramIndex
        assertEquals(0, writeFrame[7] & 0xFF);    // value hi
        assertEquals(0x55, writeFrame[8] & 0xFF); // value lo
    }

    @Test
    public void testReadRegisterSendsReadRegFrame() {
        McanDeviceIdentity identity = McanDeviceIdentity.forCdB(0x27, 0x05);
        McanDevice device = new McanDevice("M", identity);
        McanParameterManager paramManager = new McanParameterManager(device);

        AtomicReference<int[]> sentFrame = new AtomicReference<>();
        McanFrameSender sender = sentFrame::set;

        paramManager.readRegister(3, 4, sender);

        assertNotNull(sentFrame.get());
        int[] frame = sentFrame.get();
        assertEquals(13, frame.length);
        // buildReadFrame encodes module as (0x05 + 0x80) = 0x85 in byte[3]
        assertEquals(0x85, frame[3] & 0xFF);
        // DLC=2 at byte[4]
        assertEquals(2, frame[4] & 0xFF);
        // slot=3 at byte[5], reg=4 at byte[6]
        assertEquals(3, frame[5] & 0xFF);
        assertEquals(4, frame[6] & 0xFF);
    }

    @Test
    public void testWriteRegisterSendsWriteRegFrame() {
        McanDeviceIdentity identity = McanDeviceIdentity.forCdB(0x27, 0x05);
        McanDevice device = new McanDevice("M", identity);
        McanParameterManager paramManager = new McanParameterManager(device);

        AtomicReference<int[]> sentFrame = new AtomicReference<>();
        McanFrameSender sender = sentFrame::set;

        paramManager.writeRegister(2, 6, 255, sender);

        assertNotNull(sentFrame.get());
        int[] frame = sentFrame.get();
        assertEquals(13, frame.length);
        assertEquals(0x85, frame[3] & 0xFF); // module 5
        assertEquals(4, frame[4] & 0xFF);    // DLC=4
        assertEquals(2, frame[5] & 0xFF);    // slot
        assertEquals(6, frame[6] & 0xFF);    // reg
        assertEquals(0, frame[7] & 0xFF);    // value hi
        assertEquals(255, frame[8] & 0xFF);  // value lo
    }

    @Test
    public void testReadParameterIsConvenienceWrapperForSlot0() {
        // readParameter(reg, sender) must behave like readRegister(0, reg, sender)
        McanDeviceIdentity identity = McanDeviceIdentity.forCdB(0x22, 0x03);
        McanDevice device = new McanDevice("M", identity);
        McanParameterManager paramManager = new McanParameterManager(device);

        AtomicReference<int[]> fromParam = new AtomicReference<>();
        AtomicReference<int[]> fromReg = new AtomicReference<>();

        paramManager.readParameter(5, fromParam::set);
        paramManager.readRegister(0, 5, fromReg::set);

        assertArrayEquals(fromParam.get(), fromReg.get(),
                "readParameter(5) must produce the same frame as readRegister(0, 5)");
    }
}
