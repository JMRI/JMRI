package jmri.jmrix.loconet.sdf;

import jmri.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implement the LOAD_MODIFIER macro from the Digitrax sound definition language
 *
 * Arg1: Upper 4 bits - math modifiers FMATH_LODE et al Arg2: Arg3:
 *
 *
 * @author Bob Jacobsen Copyright (C) 2007
 */
public class LoadModifier extends SdfMacro {

    public LoadModifier(int byte0, int arg1, int arg2, int arg3) {
        this.modType = byte0 & 0x0F;
        this.byte0 = byte0;
        this.arg1 = arg1;
        this.arg2 = arg2;
        this.arg3 = arg3;
    }

    @Override
    public String name() {
        return "LOAD_MODIFIER"; // NOI18N
    }

    int byte0;
    int modType;
    int arg1, arg2, arg3;

    @Override
    public int length() {
        return 4;
    }

    static public SdfMacro match(SdfBuffer buff) {
        // course match
        if ((buff.getAtIndex() & 0xF0) != 0xE0) {
            return null;
        }
        int byte1 = buff.getAtIndexAndInc() & 0xFF;
        int byte2 = buff.getAtIndexAndInc() & 0xFF;
        int byte3 = buff.getAtIndexAndInc() & 0xFF;
        int byte4 = buff.getAtIndexAndInc() & 0xFF;
        return new LoadModifier(byte1, byte2, byte3, byte4);
    }

    String modTypeVal() {
        return jmri.util.StringUtil.getNameFromState(modType, modControlCodes, modControlNames);
    }

    /**
     * Format the three bytes as simple numbers, for lack of anything better
     * right now
     * @return 3 digit string
     */
    String argVal() {
        String arg1Val = "" + arg1;
        String arg2Val = "" + arg2;
        String arg3Val = "" + arg3;
        return arg1Val + "," + arg2Val + "," + arg3Val;
    }

    /**
     * Store into a buffer.
     */
    @Override
    public void loadByteArray(SdfBuffer buffer) {
        // data
        buffer.setAtIndexAndInc(byte0);
        buffer.setAtIndexAndInc(arg1);
        buffer.setAtIndexAndInc(arg2);
        buffer.setAtIndexAndInc(arg3);

        // store children
        super.loadByteArray(buffer);
    }

    @Override
    public String toString() {
        return "Set Modifier " + modTypeVal() + '\n'; // NOI18N
    }

    @Override
    public String oneInstructionString() {
        String args;
        String arg1Val;
        String arg2Val;
        String arg3Val;
        String temp1, temp2;

        switch (modType) {
            case MTYPE_TIME:
                args = argVal();
                return name() + ' ' + modTypeVal() + "," + args + '\n';

            case MTYPE_GAIN:
                // arg1 is IMMED_GAIN_MODIFY or ANALOG_GAIN_MODIFY
                // plus possible 5 bit modifier
                if ((arg1 & 0xE0) == IMMED_GAIN_MODIFY) {
                    if (arg1 == IMMED_GAIN_MODIFY) {
                        arg1Val = "IMMED_GAIN_MODIFY"; // NOI18N
                    } else {
                        arg1Val = "IMMED_GAIN_MODIFY+0x" + StringUtil.twoHexFromInt(arg1 & 0x1f); // NOI18N
                    }
                } else if ((arg1 & 0xE0) == ANALOG_GAIN_MODIFY) {
                    if (arg1 == ANALOG_GAIN_MODIFY) {
                        arg1Val = "ANALOG_GAIN_MODIFY"; // NOI18N
                    } else {
                        arg1Val = "ANALOG_GAIN_MODIFY+" // NOI18N
                                + StringUtil.getNameFromState(arg1 & 0x1f, workRegCodes, workRegNames);
                    }
                } else {
                    arg1Val = StringUtil.twoHexFromInt(arg1);
                }
                arg2Val = StringUtil.getNameFromState(arg2, fixedCVCodes, fixedCVNames);
                if (arg2Val == null) {
                    arg2Val = "0x" + StringUtil.twoHexFromInt(arg2); // NOI18N
                }
                arg3Val = decodeFlags(arg3, arg3ModCodes, arg3ModMasks, arg3ModNames);
                if (arg3Val == null) {
                    arg3Val = "0x" + StringUtil.twoHexFromInt(arg3); // NOI18N
                }
                return name() + ' ' + modTypeVal() + "," + arg1Val + "," + arg2Val + "," + arg3Val + '\n'; // NOI18N

            case MTYPE_PITCH:
                // arg1 is CV_PITCH_MODIFY or ANALOG_PITCH_MODIFY
                // plus possible 5 bit modifier
                if ((arg1 & 0xE0) == CV_PITCH_MODIFY) {
                    if (arg1 == CV_PITCH_MODIFY) {
                        arg1Val = "CV_PITCH_MODIFY"; // NOI18N
                    } else {
                        arg1Val = "CV_PITCH_MODIFY+0x" + StringUtil.twoHexFromInt(arg1 & 0x1f); // NOI18N
                    }
                } else if ((arg1 & 0xE0) == ANALOG_PITCH_MODIFY) {
                    if (arg1 == ANALOG_PITCH_MODIFY) {
                        arg1Val = "ANALOG_PITCH_MODIFY"; // NOI18N
                    } else {
                        arg1Val = "ANALOG_PITCH_MODIFY+" // NOI18N
                                + StringUtil.getNameFromState(arg1 & 0x1f, workRegCodes, workRegNames);
                    }
                } else {
                    arg1Val = StringUtil.twoHexFromInt(arg1);
                }
                arg2Val = StringUtil.getNameFromState(arg2, maxPCodes, maxPNames);
                if (arg2Val == null) {
                    arg2Val = "0x" + StringUtil.twoHexFromInt(arg2); // NOI18N
                }
                arg3Val = StringUtil.getNameFromState(arg3, ditherPCodes, ditherPNames);
                if (arg3Val == null) {
                    arg3Val = "0x" + StringUtil.twoHexFromInt(arg3); // NOI18N
                }
                return name() + ' ' + modTypeVal() + "," + arg1Val + "," + arg2Val + "," + arg3Val + '\n';

            case MTYPE_BLEND:
                arg1Val = decodeFlags(arg1, blendArg1Codes, blendArg1Masks, blendArg1Names);

                arg2Val = StringUtil.getNameFromState(arg2, blendArg2Codes, blendArg2Names);
                if (arg2Val == null) {
                    arg2Val = "0x" + StringUtil.twoHexFromInt(arg2); // NOI18N
                }

                arg3Val = StringUtil.getNameFromState(arg3, blendArg3Codes, blendArg3Names);
                if (arg3Val == null) {
                    arg3Val = "0x" + StringUtil.twoHexFromInt(arg3); // NOI18N
                }

                return name() + ' ' + modTypeVal() + "," + arg1Val + "," + arg2Val + "," + arg3Val + '\n';

            case MTYPE_SCATTER:
                arg1Val = StringUtil.getNameFromState(arg1 & 0x38, scatCommandCodes, scatCommandNames)
                        + "+" + StringUtil.getNameFromState(arg1 & 0x03, scatChannelCodes, scatChannelNames);

                arg2Val = StringUtil.getNameFromState(arg2, fixedCVCodes, fixedCVNames);
                if (arg2Val == null) {
                    arg2Val = "0x" + StringUtil.twoHexFromInt(arg2); // NOI18N
                }

                arg3Val = StringUtil.getNameFromState(arg3, sintenCodes, sintenNames);
                if (arg3Val == null) {
                    arg3Val = "0x" + StringUtil.twoHexFromInt(arg3); // NOI18N
                }

                return name() + ' ' + modTypeVal() + "," + arg1Val + "," + arg2Val + "," + arg3Val + '\n';

            case MTYPE_SNDCV:
                arg1Val = StringUtil.getNameFromState(arg1, fixedCVCodes, fixedCVNames);
                if (arg1Val == null) {
                    arg1Val = "0x" + StringUtil.twoHexFromInt(arg1); // NOI18N
                }
                arg2Val = "" + arg2;
                arg3Val = "" + arg3;
                return name() + ' ' + modTypeVal() + "," + arg1Val + "," + arg2Val + "," + arg3Val + '\n';

            case MTYPE_WORK_IMMED:
                // math operations w immediate operands
                temp1 = StringUtil.getNameFromState(arg1 & 0xE0, arg1ModCodes, arg1ModNames);
                temp2 = StringUtil.getNameFromState(arg1 & 0x1F, workRegCodes, workRegNames);
                if (temp1 != null && temp2 != null) {
                    arg1Val = temp1 + "+" + temp2;
                } else if (temp1 != null && temp2 == null) {
                    arg1Val = temp1;
                } else if (temp1 == null && temp2 != null) {
                    arg1Val = temp2;
                } else {
                    arg1Val = "0"; // an odd error, actually
                }
                arg2Val = StringUtil.getNameFromState(arg2, maxPCodes, maxPNames);
                if (arg2Val == null) {
                    arg2Val = "0x" + StringUtil.twoHexFromInt(arg2); // NOI18N
                }

                // occasionally see MERGE_ALL_MASK in arg3, but that's zero
                arg3Val = "" + arg3;

                // special cases
                //   status bit register
                if ((arg1 & 0x1F) == WORK_STATUS_BITS) {
                    arg2Val = decodeFlags(arg2, workStatusBitCodes, workStatusBitCodes, workStatusBitNames);
                }
                if ((arg1 & 0x1F) == WORK_GLBL_GAIN && arg2 == DEFAULT_GLBL_GAIN) {
                    arg2Val = "DEFAULT_GLBL_GAIN"; // NOI18N
                }
                return name() + ' ' + modTypeVal() + "," + arg1Val + "," + arg2Val + "," + arg3Val + '\n';

            case MTYPE_WORK_INDIRECT:
                // math operations from one reg to another
                temp1 = StringUtil.getNameFromState(arg1 & 0xE0, arg1ModCodes, arg1ModNames);
                temp2 = StringUtil.getNameFromState(arg1 & 0x1F, workRegCodes, workRegNames);
                if (temp1 != null && temp2 != null) {
                    arg1Val = temp1 + "+" + temp2;
                } else if (temp1 != null && temp2 == null) {
                    arg1Val = temp1;
                } else if (temp1 == null && temp2 != null) {
                    arg1Val = temp2;
                } else {
                    arg1Val = "0"; // an odd error, actually // NOI18N
                }
                arg2Val = StringUtil.getNameFromState(arg2 & 0x1F, workRegCodes, workRegNames);
                if (arg2Val == null) {
                    arg2Val = "0x" + StringUtil.twoHexFromInt(arg2); // NOI18N
                }

                // occasionally see MERGE_ALL_MASK in arg3, but that's zero
                arg3Val = "" + arg3;

                return name() + ' ' + modTypeVal() + "," + arg1Val + "," + arg2Val + "," + arg3Val + '\n'; // NOI18N
            default:
                log.warn("Unhandled modifyer type code: {}", modType);
                break;
        }
        return "<could not parse, should not happen>"; // NOI18N
    }

    @Override
    public String allInstructionString(String indent) {
        return indent + oneInstructionString();
    }
    
    private final static Logger log = LoggerFactory.getLogger(LoadModifier.class);
}
