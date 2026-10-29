package jmri.jmrix.marklin.mcan.protocol;

/**
 * Builds CdB module select and deselect frames.
 * <p>
 * Select (connect): write frame, DLC=4, data = {0x00, 0xF7, 0x00, 0x01}.<br>
 * Deselect (disconnect): write frame, DLC=2, data = {0x00, 0xFE}.<br>
 * Factory reset: write frame, DLC=2, data = {0x00, 0xFF}.
 * <p>
 * In each frame byte[5]=subcmd (0x00), byte[6]=register code (0xF7/0xFE/0xFF).
 */
public final class CdBModuleSelect {

    static final int REG_SELECT        = 0xF7;
    static final int REG_DESELECT      = 0xFE;
    static final int REG_FACTORY_RESET = 0xFF;

    private CdBModuleSelect() {}

    /**
     * Select (connect to) a commissioned module.
     *
     * @param canClass CdB device class ID
     * @param moduleId module slot (1–127)
     * @return 13-byte CAN frame
     */
    public static int[] buildSelect(int canClass, int moduleId) {
        return CdBFrameEncoder.buildWriteFrame(canClass, moduleId, 4,
                new int[]{0x00, REG_SELECT, 0x00, 0x01});
    }

    /**
     * Select a factory-fresh module (module ID=0) using challenge bytes from its ping reply.
     *
     * @param canClass   CdB device class ID
     * @param challenge0 byte at data index 1 of the factory ping reply (frame byte[6])
     * @param challenge1 byte at data index 2 of the factory ping reply (frame byte[7])
     * @return 13-byte CAN frame
     */
    public static int[] buildFactorySelect(int canClass, int challenge0, int challenge1) {
        return CdBFrameEncoder.buildWriteFrame(canClass, 0, 6,
                new int[]{0x00, REG_SELECT, 0x00, 0x01, challenge0 & 0xFF, challenge1 & 0xFF});
    }

    /**
     * Deselect (disconnect from) a module.
     *
     * @param canClass CdB device class ID
     * @param moduleId module slot (1–127)
     * @return 13-byte CAN frame
     */
    public static int[] buildDeselect(int canClass, int moduleId) {
        return CdBFrameEncoder.buildWriteFrame(canClass, moduleId, 2,
                new int[]{0x00, REG_DESELECT});
    }

    /**
     * Factory-reset a selected module. Module remains selected after reset.
     *
     * @param canClass CdB device class ID
     * @param moduleId module slot (1–127)
     * @return 13-byte CAN frame
     */
    public static int[] buildFactoryReset(int canClass, int moduleId) {
        return CdBFrameEncoder.buildWriteFrame(canClass, moduleId, 2,
                new int[]{0x00, REG_FACTORY_RESET});
    }
}
