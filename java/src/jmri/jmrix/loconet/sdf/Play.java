package jmri.jmrix.loconet.sdf;

import jmri.util.StringUtil;

/**
 * Implement the PLAY macro from the Digitrax sound definition language
 *
 * @author Bob Jacobsen Copyright (C) 2007
 */
public class Play extends SdfMacro {

    public Play(int byte1, int byte2) {
        this.byte1 = byte1;
        this.byte2 = byte2;
        this.handle = byte2 & 0x3F;
        this.wavebrkFlags = byte1 & 0x18;
        this.brk = ((byte1 & 0x7) << 2) + ((byte2 & 0xC0) >> 6);
        this.byte1 = byte1;
        this.byte2 = byte2;
    }

    @Override
    public String name() {
        return "PLAY"; // NOI18N
    }

    int handle;
    int brk;  // "break" is a reserved word
    int wavebrkFlags;

    int byte1, byte2;

    public String handleVal() {
        return "" + handle;
    }

    public void setHandle(int val) {
        this.handle = val;
        this.byte2 = (this.byte2 & 0xC0) | (val & 0x3f);
    }

    public String brkVal() {
        return decodeState(brk, loopCodes, loopNames);
    }

    public void setBrk(String name) {
        int val = jmri.util.StringUtil.getStateFromName(name, loopCodes, loopNames);
        if (val == -1) {
            val = 0; // no match found is defaulted to zero
        }
        setBrk(val);
    }

    public void setBrk(int n) {
        // argument is 0 - 31
        this.byte1 = (this.byte1 & 0xF8) | ((n >> 2) & 0x7);
        this.byte2 = (this.byte2 & 0x3F) | ((n << 6) & 0xC0);
        this.brk = ((byte1 & 0x7) << 2) + ((byte2 & 0xC0) >> 6);
    }

    public String wavebrkFlagsVal() {
        return decodeFlags(wavebrkFlags, wavebrkCodes, wavebrkMasks, wavebrkNames);
    }

    public int getWaveBrkFlags() {
        return this.wavebrkFlags >> 3;
    }

    // doesn't handle case of GLOBAL+INVERT!
    public void setWaveBrkFlags(String name) {
        int val = StringUtil.getStateFromName(name, wavebrkCodes, wavebrkNames);
        if (val == -1) {
            val = 0;  // no match found is defaulted to zero
        }
        setWaveBrkFlags(val >> 3);
    }

    public void setWaveBrkFlags(int n) {
        // argument is 0,1,2,3
        this.byte1 = (this.byte1 & 0xE7) | ((n << 3) & 0x18);
        this.wavebrkFlags = byte1 & 0x18;
    }

    @Override
    public int length() {
        return 2;
    }

    static public SdfMacro match(SdfBuffer buff) {
        // course match
        if ((buff.getAtIndex() & 0xC0) != 0x40) {
            return null;
        }
        int byte1 = buff.getAtIndexAndInc();
        int byte2 = buff.getAtIndexAndInc();
        return new Play(byte1, byte2);
    }

    /**
     * Store into a buffer.
     */
    @Override
    public void loadByteArray(SdfBuffer buffer) {
        // data
        buffer.setAtIndexAndInc(byte1);
        buffer.setAtIndexAndInc(byte2);

        // store children
        super.loadByteArray(buffer);
    }

    @Override
    public String toString() {
        return "Play Fragment " + handleVal() + '\n'; // NOI18N
    }

    @Override
    public String oneInstructionString() {
        return name() + ' ' + handleVal() + ',' + brkVal() + ',' + wavebrkFlagsVal() + '\n';
    }

    @Override
    public String allInstructionString(String indent) {
        return indent + oneInstructionString();
    }
}
