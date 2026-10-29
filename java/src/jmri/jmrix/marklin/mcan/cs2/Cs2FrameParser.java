package jmri.jmrix.marklin.mcan.cs2;

import jmri.jmrix.marklin.mcan.classify.ClassifiedFrame;
import jmri.jmrix.marklin.mcan.classify.Cs2ClassifiedFrame;
import jmri.jmrix.marklin.mcan.classify.Cs2Data;
import jmri.jmrix.marklin.mcan.classify.Cs2Header;
import jmri.jmrix.marklin.mcan.classify.FrameProtocol;

/**
 * Second-pass parser for CS2_NATIVE frames.
 * <p>
 * Takes a first-pass {@link ClassifiedFrame} (must be {@link FrameProtocol#CS2_NATIVE}),
 * builds {@link Cs2Data} from the raw bytes, resolves the {@link jmri.jmrix.marklin.mcan.classify.Cs2Command},
 * and returns the appropriate typed {@link Cs2ClassifiedFrame} subclass.
 * <p>
 * Unknown commands return a base {@link Cs2ClassifiedFrame} with
 * {@link jmri.jmrix.marklin.mcan.classify.Cs2Command#UNKNOWN}; check {@link Cs2Header#response()} for direction.
 */
public final class Cs2FrameParser {

    private Cs2FrameParser() {}

    /**
     * Parses a CS2_NATIVE classified frame into a typed result.
     *
     * @param frame first-pass result; must have {@code protocol() == CS2_NATIVE}
     * @return typed parse result, never null
     * @throws IllegalArgumentException if the frame is not CS2_NATIVE
     */
    public static Cs2ClassifiedFrame parse(ClassifiedFrame frame) {
        if (frame.protocol() != FrameProtocol.CS2_NATIVE) {
            throw new IllegalArgumentException(
                    "Cs2FrameParser requires a CS2_NATIVE frame, got " + frame.protocol());
        }
        Cs2Header header = frame.cs2Header();
        Cs2Data   data   = Cs2Data.from(frame.raw());

        switch (header.resolvedCommand()) {
            case SYSTEM_COMMAND:
            case SYSTEM_COMMAND_REPLY:
                return Cs2SystemCommands.parse(header, data, frame);
            case LOCO_EMERGENCY_STOP:
            case LOCO_EMERGENCY_STOP_REPLY:
            case LOCO_SPEED:
            case LOCO_SPEED_REPLY:
            case LOCO_DIRECTION:
            case LOCO_DIRECTION_REPLY:
            case LOCO_FUNCTION:
            case LOCO_FUNCTION_REPLY:
            case READ_CONFIG:
            case READ_CONFIG_REPLY:
            case WRITE_CONFIG:
            case WRITE_CONFIG_REPLY:
            case MFX_BIND:
            case MFX_BIND_REPLY:
            case MFX_VERIFY:
            case MFX_VERIFY_REPLY:
                return Cs2DecoderCommands.parse(header, data, frame);
            case ACCESSORY:
            case ACCESSORY_REPLY:
            case MFX_BIND_EX:
            case MFX_BIND_EX_REPLY:
                return Cs2AccessoryCommands.parse(header, data, frame);
            case S88_POLL:
            case S88_POLL_REPLY:
            case S88_EVENT:
            case S88_EVENT_REPLY:
            case FEEDBACK_BROADCAST:
                return Cs2FeedbackCommands.parse(header, data, frame);
            case PING:
            case PING_REPLY:
            case BOOT_CAN:
            case BOOT_CAN_REPLY:
            case UPDATE_OFFER:
            case UPDATE_OFFER_REPLY:
            case READ_CONFIG_DATA:
            case READ_CONFIG_DATA_REPLY:
            case BOOT_REQUEST:
            case BOOT_REQUEST_REPLY:
                return Cs2ConfigCommands.parse(header, data, frame);
            case STATUS_DATA_CONFIG:
            case STATUS_DATA_CONFIG_REPLY:
            case REQUEST_CONFIG_DATA:
            case REQUEST_CONFIG_DATA_REPLY:
            case CONFIG_DATA_STREAM:
            case CONFIG_DATA_STREAM_REPLY:
                return Cs2GuiCommands.parse(header, data, frame);
            case AUTOMATION_TRIGGER:
            case AUTOMATION_TRIGGER_REPLY:
                return Cs2AutomationCommands.parse(header, data, frame);
            default:
                // TODO: Connect 6021 Stream (spec command 0x22, CAN-ID 0x44, DLC=5/6) is not
                //       implemented. The spec lists it in the command overview table but provides
                //       no payload description. Add enum entries and a handler class once the
                //       byte layout is sourced from traffic captures or Märklin firmware sources.
                return new Cs2ClassifiedFrame(header, data, frame);
        }
    }
}
