package jmri.jmrix.marklin.mcan.classify;

import java.util.Optional;

/**
 * Observable operating mode of a CdB device, deducible from a single incoming frame.
 * <p>
 * Only three modes are directly observable from protocol frames without external context:
 * <ul>
 *   <li>{@link #FACTORY}      PING_REPLY with moduleId=0 and DLC=6: device has no assigned
 *       module ID and is waiting for commissioning.</li>
 *   <li>{@link #OPERATIONAL}  PING_REPLY with moduleId&gt;0 and DLC=3: device has an assigned
 *       module ID and is not currently selected for configuration.</li>
 *   <li>{@link #SELECTED}     READ_REPLY: device accepted a prior SELECT command and is
 *       actively responding to configuration requests.</li>
 * </ul>
 * All other frame types (PING, READ, WRITE, UNKNOWN, unrecognised PING_REPLY shapes) do not
 * unambiguously identify the device mode. {@link #fromFrame} returns empty for those.
 */
public enum CdBDeviceMode {

    /** No module ID assigned. Device is waiting to be commissioned. */
    FACTORY,

    /** Module ID assigned. Device is not currently in SELECTED configuration mode. */
    OPERATIONAL,

    /** Device accepted a SELECT command. Device is ready for configuration commands. */
    SELECTED;

    /**
     * Derives the device mode from a second-pass classified CdB frame, where the
     * mode is unambiguously deducible from the frame alone.
     *
     * @param frame second-pass decoded CdB frame
     * @return the mode or empty if the frame does not unambiguously identify one
     */
    public static Optional<CdBDeviceMode> fromFrame(CdBClassifiedFrame frame) {
        switch (frame.frameType()) {
            case PING_REPLY:
                int dlc = frame.parent().raw().dlc();
                if (frame.moduleId() == 0 && dlc == 6) {
                    return Optional.of(FACTORY);
                }
                if (frame.moduleId() > 0 && dlc == 3) {
                    return Optional.of(OPERATIONAL);
                }
                return Optional.empty();
            case READ_REPLY:
                return Optional.of(SELECTED);
            default:
                return Optional.empty();
        }
    }
}
