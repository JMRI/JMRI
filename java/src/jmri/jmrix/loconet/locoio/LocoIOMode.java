package jmri.jmrix.loconet.locoio;

/**
 *
 * @author John Plocher, January 3, 2007
 */
public class LocoIOMode {

    /**
     * Create a new instance of LocoIOMode.
     * @param isOutput isOutput value.
     * @param opcode opcode.
     * @param sv0 sv0 value.
     * @param sv2 sv2 value.
     * @param mode IO mode.
     */
    public LocoIOMode(int isOutput, int opcode, int sv0, int sv2, String mode) {
        this.isOutput = isOutput;
        this.opcode = opcode;
        this.sv0 = sv0;
        this.sv2 = sv2;
        this.mode = mode;
    }
    private final int isOutput;
    private final int opcode;
    private final int sv0;
    private final int sv2;
    private final String mode;

    public String getMode() {
        return mode;
    }

    public String getFullMode() {
        return ((isOutput == 1) ? "Output: " : "Input: ") + mode + "  ";
    }

    public int getOutput() {
        return isOutput;
    }

    public int getOpCode() {
        return opcode;
    }

    public int getSV() {
        return sv0;
    }

    public int getV2() {
        return sv2;
    }

}
