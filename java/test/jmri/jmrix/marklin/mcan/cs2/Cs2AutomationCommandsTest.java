package jmri.jmrix.marklin.mcan.cs2;
import jmri.jmrix.marklin.mcan.FrameProtocol;
import jmri.util.JUnitUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import jmri.jmrix.marklin.mcan.classify.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Roundtrip tests for {@link Cs2AutomationCommands}.
 */
public class Cs2AutomationCommandsTest {

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    private static Cs2Frame roundtrip(int[] frame) {
        Cs2Frame cf = (Cs2Frame) ProtocolClassifier.classify(frame);
        assertEquals(FrameProtocol.CS2_NATIVE, cf.protocol(),
                "Encoded frame must classify as CS2_NATIVE");
        return cf;
    }

    // --- buildAutomationTrigger ---

    @Test
    public void testBuildAutomationTriggerProduces13Bytes() {
        assertEquals(13, Cs2AutomationCommands.buildAutomationTrigger(1, 0x4101, true).length);
    }

    @Test
    public void testBuildAutomationTriggerHasCommandCode0x30() {
        Cs2Frame cf = (Cs2Frame) ProtocolClassifier.classify(
                Cs2AutomationCommands.buildAutomationTrigger(1, 0x4101, true));
        assertEquals(0x30, cf.header().commandCode());
    }

    @Test
    public void testBuildAutomationTriggerHasDlc6() {
        int[] frame = Cs2AutomationCommands.buildAutomationTrigger(1, 0x4101, true);
        assertEquals(6, frame[4]);
    }

    @Test
    public void testBuildAutomationTriggerEncodesDeviceId() {
        int[] frame = Cs2AutomationCommands.buildAutomationTrigger(0x1234, 0x0000, true);
        assertEquals(0x12, frame[5]);
        assertEquals(0x34, frame[6]);
    }

    @Test
    public void testBuildAutomationTriggerEncodesFunction() {
        int[] frame = Cs2AutomationCommands.buildAutomationTrigger(1, 0x4108, true);
        assertEquals(0x41, frame[7]);
        assertEquals(0x08, frame[8]);
    }

    @Test
    public void testBuildAutomationTriggerActivate() {
        int[] frame = Cs2AutomationCommands.buildAutomationTrigger(1, 0x4101, true);
        assertEquals(1, frame[9]);
    }

    @Test
    public void testBuildAutomationTriggerDeactivate() {
        int[] frame = Cs2AutomationCommands.buildAutomationTrigger(1, 0x4101, false);
        assertEquals(0, frame[9]);
    }

    @Test
    public void testBuildAutomationTriggerReservedByte() {
        int[] frame = Cs2AutomationCommands.buildAutomationTrigger(1, 0x4101, true);
        assertEquals(0, frame[10]);
    }

    @Test
    public void testBuildAutomationTriggerIsNotResponse() {
        Cs2Frame cf = (Cs2Frame) ProtocolClassifier.classify(
                Cs2AutomationCommands.buildAutomationTrigger(1, 0x4101, true));
        assertFalse(cf.header().response());
    }

    @Test
    public void testBuildAutomationTriggerClassifiesAsCs2Native() {
        Cs2Frame cf = (Cs2Frame) ProtocolClassifier.classify(
                Cs2AutomationCommands.buildAutomationTrigger(1, 0x4101, true));
        assertEquals(FrameProtocol.CS2_NATIVE, cf.protocol());
    }

    // --- AutomationStatusFrame roundtrip ---

    private static int[] buildAutomationStatusReply(int deviceId, int function, int status) {
        int[] data = new int[]{
            (deviceId  >> 8) & 0xFF, deviceId & 0xFF,
            (function  >> 8) & 0xFF, function & 0xFF,
            status & 0xFF,
            0x00
        };
        return Cs2FrameBuilder.buildFrame(0, Cs2AutomationCommands.COMMAND_AUTOMATION_TRIGGER,
                true, Cs2AutomationCommands.BROADCAST_HASH, 6, data);
    }

    @Test
    public void testAutomationStatusReplyRoundtripsToAutomationStatusFrame() {
        Cs2Frame parsed = roundtrip(buildAutomationStatusReply(1, 0x4101, 1));
        assertInstanceOf(Cs2AutomationCommands.AutomationStatusFrame.class, parsed);
    }

    @Test
    public void testAutomationStatusFrameDeviceId() {
        Cs2Frame parsed = roundtrip(buildAutomationStatusReply(0x0042, 0x4101, 1));
        assertEquals(0x0042, ((Cs2AutomationCommands.AutomationStatusFrame) parsed).deviceId());
    }

    @Test
    public void testAutomationStatusFrameFunction() {
        Cs2Frame parsed = roundtrip(buildAutomationStatusReply(1, 0x4108, 2));
        assertEquals(0x4108, ((Cs2AutomationCommands.AutomationStatusFrame) parsed).function());
    }

    @Test
    public void testAutomationStatusFrameStatusRunning() {
        Cs2Frame parsed = roundtrip(buildAutomationStatusReply(1, 0x4101,
                Cs2AutomationCommands.STATUS_RUNNING));
        assertEquals(Cs2AutomationCommands.STATUS_RUNNING,
                ((Cs2AutomationCommands.AutomationStatusFrame) parsed).status());
    }

    @Test
    public void testAutomationStatusFrameStatusError() {
        Cs2Frame parsed = roundtrip(buildAutomationStatusReply(1, 0x4101,
                Cs2AutomationCommands.STATUS_ERROR));
        assertEquals(Cs2AutomationCommands.STATUS_ERROR,
                ((Cs2AutomationCommands.AutomationStatusFrame) parsed).status());
    }

    @Test
    public void testAutomationStatusFrameIsResponse() {
        Cs2Frame parsed = roundtrip(buildAutomationStatusReply(1, 0x4101, 1));
        assertTrue(parsed.header().response());
    }
}
