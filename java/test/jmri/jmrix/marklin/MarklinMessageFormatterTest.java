package jmri.jmrix.marklin;
import jmri.util.JUnitUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import jmri.jmrix.marklin.mcan.cs2.Cs2SystemCommands;
import jmri.jmrix.marklin.mcan.cs2.Cs2DecoderCommands;
import jmri.jmrix.marklin.mcan.cs2.Cs2FeedbackCommands;
import jmri.jmrix.marklin.mcan.cs2.Cs2ConfigCommands;
import jmri.jmrix.marklin.mcan.address.AddressSpace;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link MarklinMessageFormatter}.
 *
 * @author Ralf Lang (ralf.lang@ralf-lang.de) Copyright (C) 2026
 */
public class MarklinMessageFormatterTest {

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // ---------------------------------------------------------------------------
    // CS2 system commands
    // ---------------------------------------------------------------------------

    @Test
    public void testSystemGoFrameContainsSystemGo() {
        int[] raw = Cs2SystemCommands.buildSystemGo();
        String result = MarklinMessageFormatter.formatRaw(raw, null);
        assertTrue(result.contains("System Go"), "Expected 'System Go' in: " + result);
    }

    @Test
    public void testSystemStopFrameContainsSystemStop() {
        int[] raw = Cs2SystemCommands.buildSystemStop();
        String result = MarklinMessageFormatter.formatRaw(raw, null);
        assertTrue(result.contains("System Stop"), "Expected 'System Stop' in: " + result);
    }

    @Test
    public void testSystemGoFrameIsRequest() {
        int[] raw = Cs2SystemCommands.buildSystemGo();
        String result = MarklinMessageFormatter.formatRaw(raw, null);
        assertTrue(result.contains("Request"), "Expected 'Request' in: " + result);
    }

    @Test
    public void testSystemGoReplyIsReply() {
        int[] raw = Cs2SystemCommands.buildSystemGoReply();
        String result = MarklinMessageFormatter.formatRaw(raw, null);
        assertTrue(result.contains("Reply"), "Expected 'Reply' in: " + result);
    }

    // ---------------------------------------------------------------------------
    // CS2 decoder (loco) commands
    // ---------------------------------------------------------------------------

    @Test
    public void testLocoSpeedReplyContainsLocoSpeedAndDcc() {
        // DCC loco at address 3 → CAN address = BASE_DCC_LOCO + 3
        long canAddr = AddressSpace.DCC_LOCO.base() + 3;
        int[] raw = Cs2DecoderCommands.buildLocoSpeedReply(canAddr, 512);
        String result = MarklinMessageFormatter.formatRaw(raw, null);
        assertTrue(result.contains("Loco Speed"), "Expected 'Loco Speed' in: " + result);
        assertTrue(result.contains("DCC Loco"), "Expected 'DCC Loco' in: " + result);
    }

    @Test
    public void testLocoSpeedRequestContainsChangeOfSpeed() {
        long canAddr = AddressSpace.DCC_LOCO.base() + 1;
        int[] raw = Cs2DecoderCommands.buildLocoSpeed(canAddr, 300);
        String result = MarklinMessageFormatter.formatRaw(raw, null);
        assertTrue(result.contains("Change of speed"), "Expected 'Change of speed' in: " + result);
    }

    @Test
    public void testLocoFunctionRequestContainsFunction() {
        long canAddr = AddressSpace.DCC_LOCO.base() + 5;
        int[] raw = Cs2DecoderCommands.buildLocoFunction(canAddr, 0, true);
        String result = MarklinMessageFormatter.formatRaw(raw, null);
        assertTrue(result.contains("Function"), "Expected 'Function' in: " + result);
    }

    // ---------------------------------------------------------------------------
    // CS2 feedback commands
    // ---------------------------------------------------------------------------

    @Test
    public void testS88EventContainsS88Event() {
        int[] raw = Cs2FeedbackCommands.buildS88Event(42, 0, 1, 100);
        String result = MarklinMessageFormatter.formatRaw(raw, null);
        assertTrue(result.contains("S88 Event"), "Expected 'S88 Event' in: " + result);
        assertTrue(result.contains("contact=42"), "Expected 'contact=42' in: " + result);
    }

    @Test
    public void testS88PollContainsS88Poll() {
        int[] raw = Cs2FeedbackCommands.buildS88Poll(7);
        String result = MarklinMessageFormatter.formatRaw(raw, null);
        assertTrue(result.contains("S88 Poll"), "Expected 'S88 Poll' in: " + result);
        assertTrue(result.contains("module=7"), "Expected 'module=7' in: " + result);
    }

    // ---------------------------------------------------------------------------
    // Config commands
    // ---------------------------------------------------------------------------

    @Test
    public void testPingRequestContainsBroadcast() {
        int[] raw = Cs2ConfigCommands.buildPing();
        String result = MarklinMessageFormatter.formatRaw(raw, null);
        // Ping is a broadcast (address bytes 5-8 are 0x0300 region but DLC=0 so data bytes are 0)
        // The hash field is 0x0300, but address bytes 5-8 are the CAN data, not the hash.
        // DLC=0 means no data; address bytes 5-8 will be 0 → "Broadcast".
        assertTrue(result.contains("Broadcast"), "Expected 'Broadcast' in: " + result);
    }

    // ---------------------------------------------------------------------------
    // UNKNOWN protocol
    // ---------------------------------------------------------------------------

    @Test
    public void testAllZeroFrameProducesRawMcanFrame() {
        int[] raw = new int[13]; // all zeros  -  no CS2 fingerprint, not CdB
        String result = MarklinMessageFormatter.formatRaw(raw, null);
        assertTrue(result.contains("Raw MCAN frame"), "Expected 'Raw MCAN frame' in: " + result);
    }

    // ---------------------------------------------------------------------------
    // Address display
    // ---------------------------------------------------------------------------

    @Test
    public void testMmLocoAddressDisplayed() {
        long canAddr = AddressSpace.MM_LOCO.base() + 10;
        int[] raw = Cs2DecoderCommands.buildLocoSpeedReply(canAddr, 0);
        String result = MarklinMessageFormatter.formatRaw(raw, null);
        assertTrue(result.contains("MM Loco"), "Expected 'MM Loco' in: " + result);
    }

    @Test
    public void testDccAccessoryAddressDisplayed() {
        int canAddr = AddressSpace.DCC_ACCESSORY.base() + 4;
        // Build a minimal CS2-fingerprinted frame with accessory address in bytes 5-8
        // Easiest to use a loco speed reply (which puts canAddr in bytes 5-8) and check
        int[] raw = Cs2DecoderCommands.buildLocoSpeedReply(canAddr, 0);
        String result = MarklinMessageFormatter.formatRaw(raw, null);
        assertTrue(result.contains("DCC Accessory"), "Expected 'DCC Accessory' in: " + result);
    }

    @Test
    public void testUnknownAddressRangeDisplayedAsHex() {
        // 0x3600 is in the gap between MM_ACCESSORY (0x3000-0x33FF) and DCC_ACCESSORY (0x3800-0x3FFF)
        int gapAddr = 0x3600;
        int[] raw = Cs2DecoderCommands.buildLocoSpeedReply(gapAddr, 0);
        String result = MarklinMessageFormatter.formatRaw(raw, null);
        assertTrue(result.contains("0x00003600"), "Expected hex address in: " + result);
    }

    // ---------------------------------------------------------------------------
    // i18n provider
    // ---------------------------------------------------------------------------

    @Test
    public void testI18nProviderIsUsedForReply() {
        int[] raw = Cs2SystemCommands.buildSystemGoReply();
        String result = MarklinMessageFormatter.formatRaw(raw, (key, args) -> key.equals("ReplyMessage") ? "LOCALIZED_REPLY" : null);
        assertTrue(result.contains("LOCALIZED_REPLY"), "Expected i18n reply label in: " + result);
    }

    @Test
    public void testI18nProviderNullFallsBackToEnglish() {
        int[] raw = Cs2SystemCommands.buildSystemGoReply();
        String result = MarklinMessageFormatter.formatRaw(raw, null);
        assertTrue(result.contains("Reply"), "Expected 'Reply' in: " + result);
    }

    // ---------------------------------------------------------------------------
    // CAN hex dump always present
    // ---------------------------------------------------------------------------

    @Test
    public void testHexDumpAlwaysPresent() {
        int[] raw = Cs2SystemCommands.buildSystemGo();
        String result = MarklinMessageFormatter.formatRaw(raw, null);
        assertTrue(result.contains("0x"), "Expected hex dump in: " + result);
    }
}
