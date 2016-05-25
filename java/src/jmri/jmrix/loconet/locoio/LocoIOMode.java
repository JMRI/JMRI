/*
 * LocoIOMode.java
 *
 * Created on January 3, 2007, 9:29 PM
 *
 * Created by John Plocher
 */
package jmri.jmrix.loconet.locoio;

/**
 *
 * @author John Plocher
 */
public class LocoIOMode {

    /**
     * Creates a new instance of LocoIOMode
     */
    public LocoIOMode(int isOutput, int opcode, int sv0, int sv2, String mode) {
        this.isOutput = isOutput;
        this.opcode = opcode;
        this.sv0 = sv0;
        this.sv2 = sv2;
        this.mode = mode;
    }
    private int isOutput;
    private int opcode;
    private int sv0;
    private int sv2;
    private String mode;

    public String getMode() {
        return mode;
    }

    public String getFullMode() {
        return ((isOutput == 1) ? "Output: " : "Input: ") + mode + "  ";
    }

    public int getOutput() {
        return isOutput;
    }

    public int getOpcode() {
        return opcode;
    }

    public int getSV() {
        return sv0;
    }

    public int getV2() {
        return sv2;
    }
}
