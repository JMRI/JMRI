package jmri.jmrix.marklin.mcan.cs2;

import jmri.jmrix.marklin.mcan.RawFrame;

/**
 * Parser for CS2 native frames.
 * <p>
 * Builds {@link Cs2Data} from the raw bytes, resolves the {@link Cs2Command} from the
 * header and returns the appropriate typed {@link Cs2Frame} subclass.
 * <p>
 * Unknown commands return a base {@link Cs2Frame} with {@link Cs2Command#UNKNOWN}.
 * Check {@link Cs2Header#response()} for direction.
 */
public final class Cs2FrameParser {

    private Cs2FrameParser() {}

    /**
     * Parses a frame whose CS2 header has already been recognised.
     *
     * @param raw    the 13-byte frame
     * @param header header parsed from {@code raw}
     * @return typed frame, a base Cs2Frame at worst for unknown or unhandled commands.
     */
    public static Cs2Frame parse(RawFrame raw, Cs2Header header) {
        Cs2Data data = Cs2Data.from(raw);

        switch (header.resolvedCommand()) {
            case SYSTEM_COMMAND:
            case SYSTEM_COMMAND_REPLY:
                return Cs2SystemCommands.parse(header, data, raw);
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
                return Cs2DecoderCommands.parse(header, data, raw);
            case ACCESSORY:
            case ACCESSORY_REPLY:
            case MFX_BIND_EX:
            case MFX_BIND_EX_REPLY:
                return Cs2AccessoryCommands.parse(header, data, raw);
            case S88_POLL:
            case S88_POLL_REPLY:
            case S88_EVENT:
            case S88_EVENT_REPLY:
            case FEEDBACK_BROADCAST:
                return Cs2FeedbackCommands.parse(header, data, raw);
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
                return Cs2ConfigCommands.parse(header, data, raw);
            case STATUS_DATA_CONFIG:
            case STATUS_DATA_CONFIG_REPLY:
            case REQUEST_CONFIG_DATA:
            case REQUEST_CONFIG_DATA_REPLY:
            case CONFIG_DATA_STREAM:
            case CONFIG_DATA_STREAM_REPLY:
                return Cs2GuiCommands.parse(header, data, raw);
            case AUTOMATION_TRIGGER:
            case AUTOMATION_TRIGGER_REPLY:
                return Cs2AutomationCommands.parse(header, data, raw);
            default:
                // TODO: Connect 6021 Stream (spec command 0x22, CAN-ID 0x44, DLC=5/6) is not
                //       implemented. The spec lists it in the command overview table but provides
                //       no payload description. Add enum entries and a handler class once the
                //       byte layout is sourced from traffic captures or Märklin firmware sources.
                return new Cs2Frame(header, data, raw);
        }
    }
}
