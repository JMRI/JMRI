package jmri.jmrix.marklin;

import java.util.Optional;
import jmri.jmrix.marklin.mcan.address.AddressSpace;
import jmri.jmrix.marklin.mcan.cdb.CdBFrameParser;
import jmri.jmrix.marklin.mcan.device.CdBDeviceClass;
import jmri.jmrix.marklin.mcan.classify.ClassifiedFrame;
import jmri.jmrix.marklin.mcan.classify.Cs2ClassifiedFrame;
import jmri.jmrix.marklin.mcan.classify.FrameProtocol;
import jmri.jmrix.marklin.mcan.classify.ProtocolClassifier;
import jmri.jmrix.marklin.mcan.cs2.Cs2AccessoryCommands;
import jmri.jmrix.marklin.mcan.cs2.Cs2AutomationCommands;
import jmri.jmrix.marklin.mcan.cs2.Cs2ConfigCommands;
import jmri.jmrix.marklin.mcan.cs2.Cs2DecoderCommands;
import jmri.jmrix.marklin.mcan.cs2.Cs2FeedbackCommands;
import jmri.jmrix.marklin.mcan.cs2.Cs2FrameParser;
import jmri.jmrix.marklin.mcan.cs2.Cs2GuiCommands;
import jmri.jmrix.marklin.mcan.cs2.Cs2SystemCommands;

/**
 * Formats MCAN protocol messages for display and logging.
 * <p>
 * This class provides message formatting with optional i18n support.
 * When used without i18n (standalone/bridge), it falls back to English text.
 * <p>
 * Supports multiple format styles (currently implements MarklinMon-compatible format).
 *
 * @author Ralf Lang (ralf.lang@ralf-lang.de) Copyright (C) 2026
 */
public class MarklinMessageFormatter {

    /**
     * Optional i18n provider interface for localized messages.
     */
    public interface I18nProvider {
        /**
         * Get localized message.
         * @param key message key
         * @param args optional arguments for message formatting
         * @return localized message or null if not available
         */
        String getMessage(String key, Object... args);
    }

    /**
     * Format a raw 13-byte MCAN frame in MarklinMon style.
     * <p>
     * Output format: [Priority] Command: [details] [Request/Reply] [Address] [Hex bytes]
     *
     * @param rawData 13-byte MCAN frame
     * @param i18n optional i18n provider (null for English-only)
     * @param includeDeviceInfo if true, prepend device identification from PING reply hash
     * @return formatted message string
     */
    public static String formatRaw(int[] rawData, I18nProvider i18n, boolean includeDeviceInfo) {
        StringBuilder sb = new StringBuilder();

        ClassifiedFrame cf = ProtocolClassifier.classify(rawData);

        if (includeDeviceInfo && cf.protocol() == FrameProtocol.CS2_NATIVE) {
            appendDeviceInfo(cf, sb);
        }

        appendPriority(cf, sb);
        appendCommand(cf, rawData, sb, i18n);
        appendResponse(cf, rawData, sb, i18n);
        appendAddress(rawData, sb);
        appendCanHex(rawData, sb);

        return sb.toString();
    }

    /**
     * Format from raw bytes (convenience method).
     *
     * @param rawData 13-byte MCAN message
     * @param i18n optional i18n provider (null for English-only)
     * @return formatted message string
     */
    public static String formatRaw(int[] rawData, I18nProvider i18n) {
        return formatRaw(rawData, i18n, false);
    }

    // ---------------------------------------------------------------------------
    // Private helpers
    // ---------------------------------------------------------------------------

    private static void appendDeviceInfo(ClassifiedFrame cf, StringBuilder sb) {
        // Only attempt device info for CS2 native frames
        if (cf.protocol() != FrameProtocol.CS2_NATIVE) return;
        Cs2ClassifiedFrame typed = Cs2FrameParser.parse(cf);
        if (typed instanceof Cs2ConfigCommands.PingReplyFrame) {
            Cs2ConfigCommands.PingReplyFrame ping = (Cs2ConfigCommands.PingReplyFrame) typed;
            sb.append(String.format("[0x%08X type=0x%04X] ", ping.uid(), ping.deviceType()));
        } else {
            int hash = cf.cs2Header().hash();
            sb.append(String.format("[Hash:0x%04X] ", hash));
        }
    }

    private static void appendPriority(ClassifiedFrame cf, StringBuilder sb) {
        if (cf.protocol() == FrameProtocol.UNKNOWN) {
            sb.append("Unknown Priority");
            return;
        }
        if (cf.protocol() == FrameProtocol.CDB_CONFIG) {
            sb.append("CdB Config");
            return;
        }
        switch (cf.cs2Header().busPriority()) {
            case 0:  sb.append("Priority 1: Stop/Go/Short"); break;
            case 1:  sb.append("Priority 2: Feedback"); break;
            case 2:  sb.append("Priority 3: Engine Stop"); break;
            case 3:  sb.append("Priority 4: Engine/Accessory Command"); break;
            default: sb.append("Unknown Priority"); break;
        }
    }

    private static void appendCommand(ClassifiedFrame cf, int[] rawData, StringBuilder sb, I18nProvider i18n) {
        sb.append(" Command: ");

        if (cf.protocol() == FrameProtocol.UNKNOWN) {
            sb.append("Raw MCAN frame");
            return;
        }

        if (cf.protocol() == FrameProtocol.CDB_CONFIG) {
            CdBFrameParser.CdBFrame cdb = CdBFrameParser.parse(cf);
            formatCdBCommand(cdb, sb);
            return;
        }

        // CS2_NATIVE  -  parse to typed frame and dispatch
        Cs2ClassifiedFrame frame = Cs2FrameParser.parse(cf);
        appendCs2Command(frame, sb, i18n);
    }

    private static void appendCs2Command(Cs2ClassifiedFrame frame, StringBuilder sb, I18nProvider i18n) {
        // System commands (0x00)
        if (frame instanceof Cs2SystemCommands.SystemStopRequestFrame) {
            sb.append("System Stop");
        } else if (frame instanceof Cs2SystemCommands.SystemStopReplyFrame) {
            sb.append("System Stop Reply");
        } else if (frame instanceof Cs2SystemCommands.SystemGoRequestFrame) {
            sb.append("System Go");
        } else if (frame instanceof Cs2SystemCommands.SystemGoReplyFrame) {
            sb.append("System Go Reply");
        } else if (frame instanceof Cs2SystemCommands.SystemHaltRequestFrame) {
            sb.append("System Halt");
        } else if (frame instanceof Cs2SystemCommands.SystemHaltReplyFrame) {
            sb.append("System Halt Reply");
        } else if (frame instanceof Cs2SystemCommands.LocoEmergencyStopRequestFrame) {
            Cs2SystemCommands.LocoEmergencyStopRequestFrame f = (Cs2SystemCommands.LocoEmergencyStopRequestFrame) frame;
            sb.append("System Loco Emergency Stop ").append(f.locId());
        } else if (frame instanceof Cs2SystemCommands.LocoEmergencyStopReplyFrame) {
            Cs2SystemCommands.LocoEmergencyStopReplyFrame f = (Cs2SystemCommands.LocoEmergencyStopReplyFrame) frame;
            sb.append("System Loco Emergency Stop Reply ").append(f.locId());
        } else if (frame instanceof Cs2SystemCommands.LocoCycleStopRequestFrame) {
            Cs2SystemCommands.LocoCycleStopRequestFrame f = (Cs2SystemCommands.LocoCycleStopRequestFrame) frame;
            sb.append("System Cycle Stop ").append(f.locId());
        } else if (frame instanceof Cs2SystemCommands.LocoCycleStopReplyFrame) {
            Cs2SystemCommands.LocoCycleStopReplyFrame f = (Cs2SystemCommands.LocoCycleStopReplyFrame) frame;
            sb.append("System Cycle Stop Reply ").append(f.locId());
        } else if (frame instanceof Cs2SystemCommands.LocoDataProtocolRequestFrame) {
            Cs2SystemCommands.LocoDataProtocolRequestFrame f = (Cs2SystemCommands.LocoDataProtocolRequestFrame) frame;
            sb.append("System Loco Data Protocol ").append(f.locId()).append(" prot=").append(f.protocol());
        } else if (frame instanceof Cs2SystemCommands.LocoDataProtocolReplyFrame) {
            Cs2SystemCommands.LocoDataProtocolReplyFrame f = (Cs2SystemCommands.LocoDataProtocolReplyFrame) frame;
            sb.append("System Loco Data Protocol Reply ").append(f.locId()).append(" prot=").append(f.protocol());
        } else if (frame instanceof Cs2SystemCommands.AccessorySwitchTimeRequestFrame) {
            Cs2SystemCommands.AccessorySwitchTimeRequestFrame f = (Cs2SystemCommands.AccessorySwitchTimeRequestFrame) frame;
            sb.append("System Accessory Switch Time ").append(f.targetUid()).append(" t=").append(f.time10ms()).append("0ms");
        } else if (frame instanceof Cs2SystemCommands.AccessorySwitchTimeReplyFrame) {
            Cs2SystemCommands.AccessorySwitchTimeReplyFrame f = (Cs2SystemCommands.AccessorySwitchTimeReplyFrame) frame;
            sb.append("System Accessory Switch Time Reply ").append(f.targetUid()).append(" t=").append(f.time10ms()).append("0ms");
        } else if (frame instanceof Cs2SystemCommands.FastReadMfxRequestFrame) {
            Cs2SystemCommands.FastReadMfxRequestFrame f = (Cs2SystemCommands.FastReadMfxRequestFrame) frame;
            sb.append("System Fast Read MFX ").append(f.senderUid()).append(" sid=").append(f.mfxSid());
        } else if (frame instanceof Cs2SystemCommands.FastReadMfxReplyFrame) {
            Cs2SystemCommands.FastReadMfxReplyFrame f = (Cs2SystemCommands.FastReadMfxReplyFrame) frame;
            sb.append("System Fast Read MFX Reply ").append(f.senderUid()).append(" sid=").append(f.mfxSid());
        } else if (frame instanceof Cs2SystemCommands.EnableTrackProtocolRequestFrame) {
            Cs2SystemCommands.EnableTrackProtocolRequestFrame f = (Cs2SystemCommands.EnableTrackProtocolRequestFrame) frame;
            sb.append(String.format("System Enable Track Protocol %d mask=0x%02X", f.targetUid(), f.protocolMask()));
        } else if (frame instanceof Cs2SystemCommands.EnableTrackProtocolReplyFrame) {
            Cs2SystemCommands.EnableTrackProtocolReplyFrame f = (Cs2SystemCommands.EnableTrackProtocolReplyFrame) frame;
            sb.append(String.format("System Enable Track Protocol Reply %d mask=0x%02X", f.targetUid(), f.protocolMask()));
        } else if (frame instanceof Cs2SystemCommands.MfxReregistrationRequestFrame) {
            Cs2SystemCommands.MfxReregistrationRequestFrame f = (Cs2SystemCommands.MfxReregistrationRequestFrame) frame;
            sb.append("System MFX Reregistration ").append(f.targetUid());
        } else if (frame instanceof Cs2SystemCommands.MfxReregistrationReplyFrame) {
            Cs2SystemCommands.MfxReregistrationReplyFrame f = (Cs2SystemCommands.MfxReregistrationReplyFrame) frame;
            sb.append("System MFX Reregistration Reply ").append(f.targetUid());
        } else if (frame instanceof Cs2SystemCommands.SystemOverloadFrame) {
            Cs2SystemCommands.SystemOverloadFrame f = (Cs2SystemCommands.SystemOverloadFrame) frame;
            sb.append("System Overload ").append(f.senderUid()).append(" ch=").append(f.channel());
        } else if (frame instanceof Cs2SystemCommands.SystemStatusRequestFrame) {
            Cs2SystemCommands.SystemStatusRequestFrame f = (Cs2SystemCommands.SystemStatusRequestFrame) frame;
            sb.append("System Status ").append(f.deviceUid()).append(" ch=").append(f.channel());
        } else if (frame instanceof Cs2SystemCommands.SystemStatusReplyFrame) {
            Cs2SystemCommands.SystemStatusReplyFrame f = (Cs2SystemCommands.SystemStatusReplyFrame) frame;
            sb.append("System Status Reply ").append(f.deviceUid()).append(" ch=").append(f.channel()).append(" val=").append(f.measuredValue());
        } else if (frame instanceof Cs2SystemCommands.DeviceIdentifierRequestFrame) {
            Cs2SystemCommands.DeviceIdentifierRequestFrame f = (Cs2SystemCommands.DeviceIdentifierRequestFrame) frame;
            sb.append("System Device Identifier Query ").append(f.targetUid());
        } else if (frame instanceof Cs2SystemCommands.DeviceIdentifierReplyFrame) {
            Cs2SystemCommands.DeviceIdentifierReplyFrame f = (Cs2SystemCommands.DeviceIdentifierReplyFrame) frame;
            sb.append(String.format("System Device Identifier %d id=0x%04X", f.targetUid(), f.identifier()));
        } else if (frame instanceof Cs2SystemCommands.MfxSeekRequestFrame) {
            Cs2SystemCommands.MfxSeekRequestFrame f = (Cs2SystemCommands.MfxSeekRequestFrame) frame;
            sb.append("System MFX Seek ").append(f.senderUid());
        } else if (frame instanceof Cs2SystemCommands.MfxSeekReplyFrame) {
            Cs2SystemCommands.MfxSeekReplyFrame f = (Cs2SystemCommands.MfxSeekReplyFrame) frame;
            sb.append("System MFX Seek Reply ").append(f.senderUid());
        } else if (frame instanceof Cs2SystemCommands.SystemResetRequestFrame) {
            Cs2SystemCommands.SystemResetRequestFrame f = (Cs2SystemCommands.SystemResetRequestFrame) frame;
            sb.append("System Reset ").append(f.targetUid());
        } else if (frame instanceof Cs2SystemCommands.SystemResetReplyFrame) {
            Cs2SystemCommands.SystemResetReplyFrame f = (Cs2SystemCommands.SystemResetReplyFrame) frame;
            sb.append("System Reset Reply ").append(f.targetUid());

        // Decoder commands (0x03–0x0A)
        } else if (frame instanceof Cs2DecoderCommands.LocoEmergencyStopFrame) {
            Cs2DecoderCommands.LocoEmergencyStopFrame f = (Cs2DecoderCommands.LocoEmergencyStopFrame) frame;
            sb.append("Loco Emergency Stop ").append(f.locoAddress());
        } else if (frame instanceof Cs2DecoderCommands.LocoSpeedFrame) {
            Cs2DecoderCommands.LocoSpeedFrame f = (Cs2DecoderCommands.LocoSpeedFrame) frame;
            if (f.speed() >= 0) sb.append("Change of speed ").append(f.speed());
            else sb.append("Loco Speed Query ").append(f.locoAddress());
        } else if (frame instanceof Cs2DecoderCommands.LocoSpeedReplyFrame) {
            Cs2DecoderCommands.LocoSpeedReplyFrame f = (Cs2DecoderCommands.LocoSpeedReplyFrame) frame;
            sb.append("Loco Speed ").append(f.locoAddress()).append(" speed=").append(f.speed());
        } else if (frame instanceof Cs2DecoderCommands.LocoDirectionFrame) {
            Cs2DecoderCommands.LocoDirectionFrame f = (Cs2DecoderCommands.LocoDirectionFrame) frame;
            if (f.direction() >= 0) sb.append("Change of direction ").append(f.direction());
            else sb.append("Loco Direction Query ").append(f.locoAddress());
        } else if (frame instanceof Cs2DecoderCommands.LocoDirectionReplyFrame) {
            Cs2DecoderCommands.LocoDirectionReplyFrame f = (Cs2DecoderCommands.LocoDirectionReplyFrame) frame;
            sb.append("Loco Direction ").append(f.locoAddress()).append(" dir=").append(f.direction());
        } else if (frame instanceof Cs2DecoderCommands.LocoFunctionFrame) {
            Cs2DecoderCommands.LocoFunctionFrame f = (Cs2DecoderCommands.LocoFunctionFrame) frame;
            sb.append("Function: ").append(f.function()).append(" state: ").append(f.active() ? 1 : 0);
        } else if (frame instanceof Cs2DecoderCommands.LocoFunctionReplyFrame) {
            Cs2DecoderCommands.LocoFunctionReplyFrame f = (Cs2DecoderCommands.LocoFunctionReplyFrame) frame;
            sb.append("Loco Function ").append(f.locoAddress())
              .append(" fn=").append(f.function()).append(" active=").append(f.active() ? "y" : "n");
        } else if (frame instanceof Cs2DecoderCommands.ReadConfigFrame) {
            Cs2DecoderCommands.ReadConfigFrame f = (Cs2DecoderCommands.ReadConfigFrame) frame;
            sb.append("Loco Read Config ").append(f.locoAddress())
              .append(" CV").append(f.cvNumber()).append(" count=").append(f.count());
        } else if (frame instanceof Cs2DecoderCommands.ReadConfigReplyFrame) {
            Cs2DecoderCommands.ReadConfigReplyFrame f = (Cs2DecoderCommands.ReadConfigReplyFrame) frame;
            sb.append("Loco Read Config ").append(f.locoAddress())
              .append(" CV").append(f.cvNumber()).append("=").append(f.value());
        } else if (frame instanceof Cs2DecoderCommands.WriteConfigFrame) {
            Cs2DecoderCommands.WriteConfigFrame f = (Cs2DecoderCommands.WriteConfigFrame) frame;
            sb.append("Loco Write Config ").append(f.locoAddress())
              .append(" CV").append(f.cvNumber()).append("=").append(f.value());
        } else if (frame instanceof Cs2DecoderCommands.WriteConfigReplyFrame) {
            Cs2DecoderCommands.WriteConfigReplyFrame f = (Cs2DecoderCommands.WriteConfigReplyFrame) frame;
            sb.append("Loco Write Config ").append(f.locoAddress())
              .append(" CV").append(f.cvNumber()).append("=").append(f.value())
              .append(f.writeAcknowledged() ? " ack" : " nack");
        } else if (frame instanceof Cs2DecoderCommands.MfxBindFrame) {
            Cs2DecoderCommands.MfxBindFrame f = (Cs2DecoderCommands.MfxBindFrame) frame;
            sb.append(String.format("MFX Bind 0x%08X sid=%d", f.mfxUid(), f.mfxSid()));
        } else if (frame instanceof Cs2DecoderCommands.MfxBindReplyFrame) {
            Cs2DecoderCommands.MfxBindReplyFrame f = (Cs2DecoderCommands.MfxBindReplyFrame) frame;
            sb.append(String.format("MFX Bind 0x%08X sid=%d", f.mfxUid(), f.mfxSid()));
        } else if (frame instanceof Cs2DecoderCommands.MfxVerifyFrame) {
            Cs2DecoderCommands.MfxVerifyFrame f = (Cs2DecoderCommands.MfxVerifyFrame) frame;
            sb.append(String.format("MFX Verify 0x%08X sid=%d", f.mfxUid(), f.mfxSid()));
        } else if (frame instanceof Cs2DecoderCommands.MfxVerifyReplyFrame) {
            Cs2DecoderCommands.MfxVerifyReplyFrame f = (Cs2DecoderCommands.MfxVerifyReplyFrame) frame;
            sb.append(String.format("MFX Verify 0x%08X sid=%d", f.mfxUid(), f.mfxSid()));

        // Accessory commands (0x0B–0x0C)
        } else if (frame instanceof Cs2AccessoryCommands.AccessoryFrame) {
            Cs2AccessoryCommands.AccessoryFrame f = (Cs2AccessoryCommands.AccessoryFrame) frame;
            int aspect = f.aspect();
            if (i18n != null && f.header().dlc() >= 5) {
                String stateKey = aspect == 0 ? "TurnoutStateThrown" : aspect == 1 ? "TurnoutStateClosed" : null;
                if (stateKey != null) {
                    String state = i18n.getMessage(stateKey);
                    String msg = i18n.getMessage("SetTurnoutState", state != null ? state : (aspect == 0 ? "Thrown" : "Closed"));
                    if (msg != null) { sb.append("Accessory").append(msg); return; }
                }
            }
            if (f.header().dlc() >= 5) {
                sb.append("Accessory").append(aspect == 0 ? " Set Turnout State Thrown" : aspect == 1 ? " Set Turnout State Closed" : " Unknown state command " + aspect);
            } else {
                sb.append("Accessory Query ").append(f.address());
            }
        } else if (frame instanceof Cs2AccessoryCommands.AccessoryReplyFrame) {
            Cs2AccessoryCommands.AccessoryReplyFrame f = (Cs2AccessoryCommands.AccessoryReplyFrame) frame;
            sb.append("Accessory ").append(f.address()).append(" aspect=").append(f.aspect())
              .append(" power=").append(f.power() ? "on" : "off");
        } else if (frame instanceof Cs2AccessoryCommands.MfxBindExReplyFrame) {
            Cs2AccessoryCommands.MfxBindExReplyFrame f = (Cs2AccessoryCommands.MfxBindExReplyFrame) frame;
            sb.append(String.format("MFX Bind-Ex 0x%08X sid=%d", f.mfxUid(), f.mfxSid()));

        // Feedback commands (0x10–0x12)
        } else if (frame instanceof Cs2FeedbackCommands.S88PollFrame) {
            Cs2FeedbackCommands.S88PollFrame f = (Cs2FeedbackCommands.S88PollFrame) frame;
            sb.append("S88 Poll module=").append(f.module());
        } else if (frame instanceof Cs2FeedbackCommands.S88PollReplyFrame) {
            Cs2FeedbackCommands.S88PollReplyFrame f = (Cs2FeedbackCommands.S88PollReplyFrame) frame;
            sb.append(String.format("S88 Poll Reply module=%d state=0x%04X", f.module(), f.state()));
        } else if (frame instanceof Cs2FeedbackCommands.S88EventFrame) {
            Cs2FeedbackCommands.S88EventFrame f = (Cs2FeedbackCommands.S88EventFrame) frame;
            sb.append("S88 Event contact=").append(f.contactId())
              .append(" old=").append(f.oldState()).append(" new=").append(f.newState());
        } else if (frame instanceof Cs2FeedbackCommands.FeedbackBroadcastFrame) {
            Cs2FeedbackCommands.FeedbackBroadcastFrame f = (Cs2FeedbackCommands.FeedbackBroadcastFrame) frame;
            sb.append(String.format("Feedback Broadcast module=%d state=0x%04X", f.moduleIndex(), f.state()));

        // Config commands (0x18–0x1C)
        } else if (frame instanceof Cs2ConfigCommands.PingRequestFrame) {
            sb.append("Ping Request");
        } else if (frame instanceof Cs2ConfigCommands.PingReplyFrame) {
            Cs2ConfigCommands.PingReplyFrame f = (Cs2ConfigCommands.PingReplyFrame) frame;
            sb.append(String.format("Ping Reply uid=0x%08X type=0x%04X sw=0x%04X", f.uid(), f.deviceType(), f.softwareVersion()));
        } else if (frame instanceof Cs2ConfigCommands.BootCanRequestFrame) {
            Cs2ConfigCommands.BootCanRequestFrame f = (Cs2ConfigCommands.BootCanRequestFrame) frame;
            sb.append(f.isGleisboxActivation() ? "CAN BOOT Gleisbox Activate" : "CAN BOOT Bootloader Mode");
        } else if (frame instanceof Cs2ConfigCommands.CanBootReplyFrame) {
            sb.append("CAN BOOT Reply");
        } else if (frame instanceof Cs2ConfigCommands.UpdateOfferRequestFrame) {
            Cs2ConfigCommands.UpdateOfferRequestFrame f = (Cs2ConfigCommands.UpdateOfferRequestFrame) frame;
            sb.append(String.format("Update Offer uid=0x%08X sw=0x%04X", f.uid(), f.version()));
        } else if (frame instanceof Cs2ConfigCommands.UpdateOfferReplyFrame) {
            Cs2ConfigCommands.UpdateOfferReplyFrame f = (Cs2ConfigCommands.UpdateOfferReplyFrame) frame;
            sb.append(String.format("Update Offer uid=0x%08X type=0x%04X", f.uid(), f.deviceType()));
        } else if (frame instanceof Cs2ConfigCommands.ReadConfigDataRequestFrame) {
            Cs2ConfigCommands.ReadConfigDataRequestFrame f = (Cs2ConfigCommands.ReadConfigDataRequestFrame) frame;
            sb.append(String.format("Read Config Data uid=0x%08X ch=%d", f.uid(), f.channelIndex()));
        } else if (frame instanceof Cs2ConfigCommands.ReadConfigDataReplyFrame) {
            Cs2ConfigCommands.ReadConfigDataReplyFrame f = (Cs2ConfigCommands.ReadConfigDataReplyFrame) frame;
            sb.append(String.format("Read Config Data uid=0x%08X", f.uid()));
        } else if (frame instanceof Cs2ConfigCommands.BootRequestFrame) {
            Cs2ConfigCommands.BootRequestFrame f = (Cs2ConfigCommands.BootRequestFrame) frame;
            sb.append(String.format("Boot Request uid=0x%08X", f.uid()));

        // GUI commands (0x20–0x22)
        } else if (frame instanceof Cs2GuiCommands.StatusDataConfigRequestFrame) {
            Cs2GuiCommands.StatusDataConfigRequestFrame f = (Cs2GuiCommands.StatusDataConfigRequestFrame) frame;
            sb.append(String.format("Status Data Config uid=0x%08X idx=%d", f.deviceUid(), f.index()));
        } else if (frame instanceof Cs2GuiCommands.StatusDataConfigReplyFrame) {
            sb.append("Status Data Config");
        } else if (frame instanceof Cs2GuiCommands.RequestConfigDataRequestFrame) {
            Cs2GuiCommands.RequestConfigDataRequestFrame f = (Cs2GuiCommands.RequestConfigDataRequestFrame) frame;
            sb.append("Request Config Data ").append(f.filename());
        } else if (frame instanceof Cs2GuiCommands.RequestConfigDataReplyFrame) {
            Cs2GuiCommands.RequestConfigDataReplyFrame f = (Cs2GuiCommands.RequestConfigDataReplyFrame) frame;
            sb.append("Request Config Data ").append(f.filename());
        } else if (frame instanceof Cs2GuiCommands.ConfigDataStreamHeaderFrame) {
            Cs2GuiCommands.ConfigDataStreamHeaderFrame f = (Cs2GuiCommands.ConfigDataStreamHeaderFrame) frame;
            sb.append(String.format("Config Data Stream %d bytes CRC=0x%04X", f.fileLength(), f.crc()));
        } else if (frame instanceof Cs2GuiCommands.ConfigDataStreamPacketFrame) {
            Cs2GuiCommands.ConfigDataStreamPacketFrame f = (Cs2GuiCommands.ConfigDataStreamPacketFrame) frame;
            sb.append(String.format("Config Data Stream hash=0x%04X", f.transactionHash()));

        // Automation commands (0x30)
        } else if (frame instanceof Cs2AutomationCommands.AutomationStatusFrame) {
            Cs2AutomationCommands.AutomationStatusFrame f = (Cs2AutomationCommands.AutomationStatusFrame) frame;
            sb.append("Automation ").append(f.deviceId()).append(" fn=").append(f.function())
              .append(" ").append(automationStatusName(f.status()));

        // Fallback for known command code, untyped (should not occur post-implementation)
        } else {
            sb.append(frame.command().name());
        }
    }

    private static void appendResponse(ClassifiedFrame cf, int[] rawData, StringBuilder sb, I18nProvider i18n) {
        sb.append(" ");
        boolean isReply;
        if (cf.protocol() == FrameProtocol.CS2_NATIVE) {
            isReply = cf.cs2Header().response();
        } else {
            // CdB and UNKNOWN: fall back to raw bit 16 of the 29-bit CAN ID (byte[1] bit 0)
            isReply = (rawData[1] & 0x01) != 0;
        }
        if (isReply) {
            if (i18n != null) { String m = i18n.getMessage("ReplyMessage"); if (m != null) { sb.append(m); return; } }
            sb.append("Reply");
        } else {
            if (i18n != null) { String m = i18n.getMessage("RequestMessage"); if (m != null) { sb.append(m); return; } }
            sb.append("Request");
        }
    }

    private static void appendAddress(int[] rawData, StringBuilder sb) {
        int addr = ((rawData[5] & 0xFF) << 24)
                 | ((rawData[6] & 0xFF) << 16)
                 | ((rawData[7] & 0xFF) << 8)
                 |  (rawData[8] & 0xFF);
        if (addr == 0) {
            sb.append(" Broadcast");
            return;
        }
        Optional<AddressSpace> space = AddressSpace.forCanOffset(addr);
        if (space.isPresent()) {
            int base = baseFor(space.get());
            sb.append(" to ").append(labelFor(space.get())).append(' ').append(addr - base);
        } else {
            sb.append(String.format(" to 0x%08X", addr));
        }
    }

    private static void appendCanHex(int[] rawData, StringBuilder sb) {
        sb.append(" 0x").append(Integer.toHexString(rawData[0]));
        for (int i = 1; i < rawData.length; i++) {
            sb.append(", 0x").append(Integer.toHexString(rawData[i]));
        }
    }

    private static void formatCdBCommand(CdBFrameParser.CdBFrame frame, StringBuilder sb) {
        sb.append("CdB ");
        switch (frame.frameType()) {
            case PING:       sb.append("PING"); break;
            case PING_REPLY: sb.append("PING Reply"); break;
            case READ:       sb.append("Read Request"); break;
            case READ_REPLY: sb.append("Read Reply"); break;
            case WRITE:      sb.append("Write/Control"); break;
            default:         sb.append("Config"); break;
        }
        int canClass = frame.classId();
        if (canClass > 0) {
            sb.append(String.format(" (Class 0x%02X", canClass));
            String deviceName = getCdBDeviceName(canClass);
            if (deviceName != null) sb.append(" ").append(deviceName);
            int module = frame.moduleId();
            if (module >= 0) sb.append(", Module ").append(module);
            sb.append(")");
        }
    }

    private static String getCdBDeviceName(int canClass) {
        for (CdBDeviceClass dc : CdBDeviceClass.values()) {
            if (dc.getClassId() == canClass) return dc.getDisplayName();
        }
        return null;
    }

    private static int baseFor(AddressSpace space) {
        return space.base();
    }

    private static String labelFor(AddressSpace space) {
        return space.label();
    }

    private static String automationStatusName(int status) {
        switch (status) {
            case Cs2AutomationCommands.STATUS_DEACTIVATED:    return "Deactivated";
            case Cs2AutomationCommands.STATUS_ACTIVATED:      return "Activated";
            case Cs2AutomationCommands.STATUS_RUNNING:        return "Running";
            case Cs2AutomationCommands.STATUS_BLOCK_FREE:     return "Block Free";
            case Cs2AutomationCommands.STATUS_BLOCK_OCCUPIED: return "Block Occupied";
            case Cs2AutomationCommands.STATUS_SUSPENDED:      return "Suspended";
            case Cs2AutomationCommands.STATUS_RESERVED:       return "Reserved";
            case Cs2AutomationCommands.STATUS_WAITING:        return "Waiting";
            case Cs2AutomationCommands.STATUS_ERROR:          return "Error";
            default: return "Status " + status;
        }
    }
}
