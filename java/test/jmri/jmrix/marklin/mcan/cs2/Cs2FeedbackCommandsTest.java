package jmri.jmrix.marklin.mcan.cs2;
import jmri.util.JUnitUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import jmri.jmrix.marklin.mcan.classify.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link Cs2FeedbackCommands}.
 */
public class Cs2FeedbackCommandsTest {

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    private static Cs2ClassifiedFrame roundtrip(int[] frame) {
        ClassifiedFrame cf = ProtocolClassifier.classify(frame);
        assertEquals(FrameProtocol.CS2_NATIVE, cf.protocol(),
                "Encoded frame must classify as CS2_NATIVE");
        return Cs2FrameParser.parse(cf);
    }

    // --- buildS88Poll ---

    @Test
    public void testBuildS88PollProduces13Bytes() {
        assertEquals(13, Cs2FeedbackCommands.buildS88Poll(0).length);
    }

    @Test
    public void testBuildS88PollClassifiesAsCs2Native() {
        ClassifiedFrame cf = ProtocolClassifier.classify(Cs2FeedbackCommands.buildS88Poll(1));
        assertEquals(FrameProtocol.CS2_NATIVE, cf.protocol());
    }

    @Test
    public void testBuildS88PollHasCommandCode0x10() {
        ClassifiedFrame cf = ProtocolClassifier.classify(Cs2FeedbackCommands.buildS88Poll(1));
        assertEquals(0x10, cf.cs2Header().commandCode());
    }

    @Test
    public void testBuildS88PollIsNotResponse() {
        ClassifiedFrame cf = ProtocolClassifier.classify(Cs2FeedbackCommands.buildS88Poll(1));
        assertFalse(cf.cs2Header().response());
    }

    @Test
    public void testBuildS88PollHasDlc5() {
        int[] frame = Cs2FeedbackCommands.buildS88Poll(3);
        assertEquals(5, frame[4]);
    }

    @Test
    public void testBuildS88PollEncodesModuleNumber() {
        int[] frame = Cs2FeedbackCommands.buildS88Poll(7);
        assertEquals(7, frame[9]);
    }

    @Test
    public void testBuildS88PollResolvesToS88PollEnum() {
        ClassifiedFrame cf = ProtocolClassifier.classify(Cs2FeedbackCommands.buildS88Poll(1));
        assertEquals(Cs2Command.S88_POLL, cf.cs2Header().resolvedCommand());
    }

    // --- S88PollReplyFrame ---

    private static int[] buildS88PollReply(int module) {
        int[] data = new int[]{
            (int)((Cs2FeedbackCommands.MY_UID >> 24) & 0xFF),
            (int)((Cs2FeedbackCommands.MY_UID >> 16) & 0xFF),
            (int)((Cs2FeedbackCommands.MY_UID >>  8) & 0xFF),
            (int)( Cs2FeedbackCommands.MY_UID        & 0xFF),
            module & 0xFF
        };
        return Cs2FrameBuilder.buildFrame(0, 0x10, true, 0x0300, 5, data);
    }

    @Test
    public void testS88PollReplyRoundtripsToS88PollReplyFrame() {
        Cs2ClassifiedFrame parsed = roundtrip(buildS88PollReply(2));
        assertInstanceOf(Cs2FeedbackCommands.S88PollReplyFrame.class, parsed);
    }

    @Test
    public void testS88PollReplyFrameModule() {
        Cs2ClassifiedFrame parsed = roundtrip(buildS88PollReply(5));
        Cs2FeedbackCommands.S88PollReplyFrame reply =
                (Cs2FeedbackCommands.S88PollReplyFrame) parsed;
        assertEquals(5, reply.module());
    }

    // --- S88PollReplyFrame state bitfield (DLC=7) ---

    private static int[] buildS88PollReplyWithState(int module, int state) {
        int[] data = new int[]{
            (int)((Cs2FeedbackCommands.MY_UID >> 24) & 0xFF),
            (int)((Cs2FeedbackCommands.MY_UID >> 16) & 0xFF),
            (int)((Cs2FeedbackCommands.MY_UID >>  8) & 0xFF),
            (int)( Cs2FeedbackCommands.MY_UID        & 0xFF),
            module & 0xFF,
            (state >> 8) & 0xFF,
            state & 0xFF
        };
        return Cs2FrameBuilder.buildFrame(0, 0x10, true, 0x0300, 7, data);
    }

    @Test
    public void testS88PollReplyWithStateParsesToS88PollReplyFrame() {
        Cs2ClassifiedFrame parsed = roundtrip(buildS88PollReplyWithState(3, 0xA5B6));
        assertInstanceOf(Cs2FeedbackCommands.S88PollReplyFrame.class, parsed);
    }

    @Test
    public void testS88PollReplyFrameStateField() {
        Cs2ClassifiedFrame parsed = roundtrip(buildS88PollReplyWithState(3, 0xA5B6));
        assertEquals(0xA5B6,
                ((Cs2FeedbackCommands.S88PollReplyFrame) parsed).state());
    }

    @Test
    public void testS88PollReplyFrameStateZeroForDlc5() {
        Cs2ClassifiedFrame parsed = roundtrip(buildS88PollReply(1));
        assertEquals(0, ((Cs2FeedbackCommands.S88PollReplyFrame) parsed).state());
    }

    // --- buildS88Event ---

    @Test
    public void testBuildS88EventHasCommandCode0x11() {
        ClassifiedFrame cf = ProtocolClassifier.classify(
                Cs2FeedbackCommands.buildS88Event(100, 0, 1, 500));
        assertEquals(0x11, cf.cs2Header().commandCode());
    }

    @Test
    public void testBuildS88EventHasDlc8() {
        int[] frame = Cs2FeedbackCommands.buildS88Event(100, 0, 1, 500);
        assertEquals(8, frame[4]);
    }

    @Test
    public void testBuildS88EventClassifiesAsCs2Native() {
        ClassifiedFrame cf = ProtocolClassifier.classify(
                Cs2FeedbackCommands.buildS88Event(100, 0, 1, 500));
        assertEquals(FrameProtocol.CS2_NATIVE, cf.protocol());
    }

    // --- S88EventFrame ---

    @Test
    public void testS88EventRoundtripsToS88EventFrame() {
        Cs2ClassifiedFrame parsed = roundtrip(
                Cs2FeedbackCommands.buildS88Event(200, 0, 1, 100));
        assertInstanceOf(Cs2FeedbackCommands.S88EventFrame.class, parsed);
    }

    @Test
    public void testS88EventFrameContactId() {
        Cs2ClassifiedFrame parsed = roundtrip(
                Cs2FeedbackCommands.buildS88Event(300, 0, 1, 0));
        Cs2FeedbackCommands.S88EventFrame event =
                (Cs2FeedbackCommands.S88EventFrame) parsed;
        assertEquals(300, event.contactId());
    }

    @Test
    public void testS88EventFrameStates() {
        Cs2ClassifiedFrame parsed = roundtrip(
                Cs2FeedbackCommands.buildS88Event(1, 0, 1, 50));
        Cs2FeedbackCommands.S88EventFrame event =
                (Cs2FeedbackCommands.S88EventFrame) parsed;
        assertEquals(0, event.oldState());
        assertEquals(1, event.newState());
    }

    @Test
    public void testS88EventFrameTime() {
        Cs2ClassifiedFrame parsed = roundtrip(
                Cs2FeedbackCommands.buildS88Event(1, 0, 1, 750));
        Cs2FeedbackCommands.S88EventFrame event =
                (Cs2FeedbackCommands.S88EventFrame) parsed;
        assertEquals(750, event.time());
    }
}
