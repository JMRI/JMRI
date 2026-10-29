package jmri.jmrix.marklin.mcan.classify;

/**
 * Known CS2 native protocol commands, identified by command code and response flag.
 * <p>
 * Each logical operation appears twice: once as a request and once as a reply,
 * allowing callers to switch on the resolved command rather than checking both
 * the raw code and the response bit separately.
 * <p>
 * {@link #UNKNOWN} is the sentinel for any (code, response) pair not yet modelled.
 * Check {@link Cs2Header#response()} on the original header to determine direction
 * for unknown frames.
 */
public enum Cs2Command {

    // System (0x00)
    SYSTEM_COMMAND       (0x00, false),
    SYSTEM_COMMAND_REPLY (0x00, true),

    // Management / Decoder control (0x01–0x0A)
    // TODO: Lok Discovery (spec 0x01, DLC=0/1/5/6) is not yet implemented.
    //       Needs LOK_DISCOVERY / LOK_DISCOVERY_REPLY entries here and a
    //       Cs2LokDiscoveryCommands class with multi-form encoders and typed frames.
    //       Sequential programming command  -  each request must await a GFP response.
    LOCO_EMERGENCY_STOP       (0x03, false),
    LOCO_EMERGENCY_STOP_REPLY (0x03, true),
    LOCO_SPEED                (0x04, false),
    LOCO_SPEED_REPLY          (0x04, true),
    LOCO_DIRECTION            (0x05, false),
    LOCO_DIRECTION_REPLY      (0x05, true),
    LOCO_FUNCTION             (0x06, false),
    LOCO_FUNCTION_REPLY       (0x06, true),
    READ_CONFIG               (0x07, false),
    READ_CONFIG_REPLY         (0x07, true),
    WRITE_CONFIG              (0x08, false),
    WRITE_CONFIG_REPLY        (0x08, true),
    MFX_BIND                  (0x09, false),
    MFX_BIND_REPLY            (0x09, true),
    MFX_VERIFY                (0x0A, false),
    MFX_VERIFY_REPLY          (0x0A, true),

    // Accessory / Switch (0x0B–0x0D)
    ACCESSORY                 (0x0B, false),
    ACCESSORY_REPLY           (0x0B, true),
    MFX_BIND_EX              (0x0C, false),
    MFX_BIND_EX_REPLY        (0x0C, true),

    // Feedback (0x10–0x12)
    S88_POLL                  (0x10, false),
    S88_POLL_REPLY            (0x10, true),
    S88_EVENT                 (0x11, false),
    S88_EVENT_REPLY           (0x11, true),
    FEEDBACK_BROADCAST        (0x12, false),

    // Config / Update (0x18–0x1C)
    PING                      (0x18, false),
    PING_REPLY                (0x18, true),
    UPDATE_OFFER              (0x19, false),
    UPDATE_OFFER_REPLY        (0x19, true),
    READ_CONFIG_DATA          (0x1A, false),
    READ_CONFIG_DATA_REPLY    (0x1A, true),
    BOOT_CAN                  (0x1B, false),
    BOOT_CAN_REPLY            (0x1B, true),
    BOOT_REQUEST              (0x1C, false),
    BOOT_REQUEST_REPLY        (0x1C, true),

    // GUI information transfer (0x20–0x22)
    STATUS_DATA_CONFIG        (0x20, false),
    STATUS_DATA_CONFIG_REPLY  (0x20, true),
    REQUEST_CONFIG_DATA       (0x21, false),
    REQUEST_CONFIG_DATA_REPLY (0x21, true),
    CONFIG_DATA_STREAM        (0x22, false),
    CONFIG_DATA_STREAM_REPLY  (0x22, true),

    // Distributed automation (0x30+)
    AUTOMATION_TRIGGER        (0x30, false),
    AUTOMATION_TRIGGER_REPLY  (0x30, true),

    /** Sentinel for unrecognised frames. Check {@link Cs2Header#response()} for direction. */
    UNKNOWN                   (-1,   false);

    private final int     code;
    private final boolean response;

    Cs2Command(int code, boolean response) {
        this.code     = code;
        this.response = response;
    }

    public int     code()     { return code; }
    public boolean response() { return response; }

    /**
     * Resolves a raw command code and response flag to the matching enum constant,
     * or {@link #UNKNOWN} if no match exists.
     */
    public static Cs2Command from(int commandCode, boolean response) {
        for (Cs2Command c : values()) {
            if (c == UNKNOWN) continue;
            if (c.code == commandCode && c.response == response) return c;
        }
        return UNKNOWN;
    }
}
