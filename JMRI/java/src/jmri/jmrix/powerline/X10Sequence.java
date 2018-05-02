package jmri.jmrix.powerline;

/**
 * Represent a sequence of one or more X10 commands (addresses and functions).
 * <p>
 * These are X10 specific, but not device/interface specific.
 * <p>
 * A sequence should consist of addressing (1 or more), and then one or more
 * commands. It can address multiple devices, but not more than one house-code.
 * <p>
 * House codes and devices within this class are sequential numbers (1-16 for
 * house code, 1-16 for device code). These must be translated to line coding by
 * other code that converts the sequence to adapter-specific messages. The
 * {@link #encode} and {@link #decode} functions are provided to make that
 * easier by converting to and from the standard line-code sequences, but you
 * should check the coding of your new specific adapter before using them.
 *
 * @author Bob Jacobsen Copyright (C) 2008
 */
public class X10Sequence {

    public static final int FUNCTION_ALL_UNITS_OFF = 0;
    public static final int FUNCTION_ALL_LIGHTS_ON = 1;
    public static final int FUNCTION_ON = 2;
    public static final int FUNCTION_OFF = 3;
    public static final int FUNCTION_DIM = 4;
    public static final int FUNCTION_BRIGHT = 5;
    public static final int FUNCTION_ALL_LIGHTS_OFF = 6;
    public static final int FUNCTION_EXTENDED_CODE = 7;
    public static final int FUNCTION_HAIL_REQUEST = 8;
    public static final int FUNCTION_HAIL_ACKNOWLEDGE = 9;
    public static final int FUNCTION_PRESET_DIM_1 = 10;
    public static final int FUNCTION_PRESET_DIM_2 = 11;
    public static final int FUNCTION_EXTENDED_DATA_TRANSFER = 12;
    public static final int FUNCTION_STATUS_ON = 13;
    public static final int FUNCTION_STATUS_OFF = 14;
    public static final int FUNCTION_STATUS_REQUEST = 15;

    public static final int EXTCMD_DIM = 0x31;

    // First implementation of this class uses a fixed length
    // array to hold the sequence; there's a practical limit to how
    // many X10 commands anybody would want to send at once!
    private static final int MAXINDEX = 32;
    int index = 0;
    Command[] cmds = new Command[MAXINDEX];  // doesn't scale, but that's for another day

    /**
     * Append a new "do function" operation to the sequence
     * @param house    house code
     * @param function function
     * @param dimcount dimming step count
     */
    public void addFunction(int house, int function, int dimcount) {
        if (index >= MAXINDEX) {
            throw new IllegalArgumentException("Sequence too long");
        }
        cmds[index] = new Function(house, function, dimcount);
        index++;
    }

    /**
     * Append a new "set address" operation to the sequence
     * @param house  house code A-P
     * @param device device 1-16
     */
    public void addAddress(int house, int device) {
        if (index >= MAXINDEX) {
            throw new IllegalArgumentException("Sequence too long");
        }
        cmds[index] = new Address(house, device);
        index++;
    }

    /**
     * Append a new "do function" operation to the sequence
     * @param house  A-P
     * @param device 1-16
     * @param cmd    command code
     * @param data   additional data
     */
    public void addExtData(int house, int device, int cmd, int data) {
        if (index >= MAXINDEX) {
            throw new IllegalArgumentException("Sequence too long");
        }
        cmds[index] = new ExtData(house, device, cmd, data);
        index++;
    }

    /**
     * Next getCommand will be the first in the sequence
     */
    public void reset() {
        index = 0;
    }

    /**
     * Retrieve the next command in the sequence
     * @return next available command
     */
    public Command getCommand() {
        return cmds[index++];
    }

    /**
     * Represent a single X10 command, which is either a "set address" or "do
     * function" operation
     */
    public interface Command {

        public boolean isAddress();

        public boolean isFunction();

        public int getHouseCode();
    }

    /**
     * Represent a single "set address" X10 command
     */
    public static class Address implements Command {

        public Address(int house, int device) {
            this.house = house;
            this.device = device;
        }
        int house;
        int device;

        @Override
        public int getHouseCode() {
            return house;
        }

        public int getAddress() {
            return device;
        }

        @Override
        public boolean isAddress() {
            return true;
        }

        @Override
        public boolean isFunction() {
            return false;
        }
    }

    /**
     * Represent a single "do function" X10 command
     */
    public static class Function implements Command {

        public Function(int house, int function, int dimcount) {
            this.house = house;
            this.function = function;
            this.dimcount = dimcount;
        }
        int house;
        int function;
        int dimcount;

        @Override
        public int getHouseCode() {
            return house;
        }

        public int getFunction() {
            return function;
        }

        public int getDimCount() {
            return dimcount;
        }

        @Override
        public boolean isAddress() {
            return false;
        }

        @Override
        public boolean isFunction() {
            return true;
        }
    }

    /**
     * Represent a single "Extended Data" X10 command
     */
    public static class ExtData implements Command {

        public ExtData(int house, int device, int cmd, int data) {
            this.house = house;
            this.device = device;
            this.cmd = cmd;
            this.data = data;
        }
        int house;
        int device;
        int cmd;
        int data;

        public int getExtData() {
            return data;
        }

        public int getExtCmd() {
            return cmd;
        }

        @Override
        public int getHouseCode() {
            return house;
        }

        public int getAddress() {
            return device;
        }

        @Override
        public boolean isAddress() {
            return false;
        }

        @Override
        public boolean isFunction() {
            return false;
        }
    }

    /**
     * Array of human readable names for X10 commands, indexed by the command
     * numbers that are constants in this class.
     */
    static String[] functionNames = new String[]{
        "All Off", "All Lights On", "On", "Off",
        "Dim", "Bright", "All Lights Off", "Extended Code",
        "Hail Request", "Hail Ack", "Preset Dim 1", "Preset Dim 2",
        "Ext Data Trnsfr", "Status On", "Status Off", "Status Req"
    };

    /**
     * Return a human-readable name for a function code
     * @param i value of function code
     * @return  string translation
     */
    public static String functionName(int i) {
        return functionNames[i];
    }

    /**
     * For the house (A-P) and device (1-16) codes, get the line-coded value.
     * Argument is from 1 to 16 only.
     * @param i house or device code value
     * @return  line code value
     */
    public static int encode(int i) {
        if (i < 1 || i > 16) {
            throw new IllegalArgumentException("Encode outside 1-16: " + i);
        }
        return encoder[i];
    }
    static final int[] encoder = new int[]{-1,
        0x6, 0xE, 0x2, 0xA, 0x1, 0x9, 0x5, 0xD, // 1-8
        0x7, 0xF, 0x3, 0xB, 0x0, 0x8, 0x4, 0xC};

    /**
     * Get house (A-P as 1-16) or device (1-16) from line-coded value.
     * @param i line code value
     * @return  house or device code value
     */
    public static int decode(int i) {
        if (i < 0 || i > 15) {
            throw new IllegalArgumentException("Decode outside 1-16: " + i);
        }
        return decoder[i];
    }
    static final int[] decoder = new int[]{13,
        5, 3, 11, 15, 7, 1, 9, 14, // 1-8
        6, 4, 12, 16, 8, 2, 10}; // 9-15

    /**
     * Pretty-print an address code
     * @param b address code value
     * @return  human string form
     */
    public static String formatAddressByte(int b) {
        return "House " + X10Sequence.houseValueToText(X10Sequence.decode((b >> 4) & 0x0F))
                + " address device " + X10Sequence.decode(b & 0x0f);
    }

    /**
     * Pretty-print a function code
     * @param b command code value
     * @return  human string form
     */
    public static String formatCommandByte(int b) {
        return "House " + X10Sequence.houseValueToText(X10Sequence.decode((b >> 4) & 0x0F))
                + " function: " + X10Sequence.functionName(b & 0x0f);
    }

    /**
     * Translate House Value (1 to 16) to text
     * @param hV device code value
     * @return  human string form
     */
    public static String houseValueToText(int hV) {
        if (hV >= 1 && hV <= 16) {
            return houseValueDecoder[hV];
        } else {
            return "??";
        }
    }
    static String[] houseValueDecoder = new String[]{"??",
        "A", "B", "C", "D", "E", "F", "G", "H",
        "I", "J", "K", "L", "M", "N", "O", "P"};

    /**
     * Translate House Code to text
     * @param hC house code
     * @return   A-P
     */
    public static String houseCodeToText(int hC) {
        String hCode = "";
        switch (hC) {
            case 0x06:
                hCode = "A";
                break;
            case 0x0E:
                hCode = "B";
                break;
            case 0x02:
                hCode = "C";
                break;
            case 0x0A:
                hCode = "D";
                break;
            case 0x01:
                hCode = "E";
                break;
            case 0x09:
                hCode = "F";
                break;
            case 0x05:
                hCode = "G";
                break;
            case 0x0D:
                hCode = "H";
                break;
            case 0x07:
                hCode = "I";
                break;
            case 0x0F:
                hCode = "J";
                break;
            case 0x03:
                hCode = "K";
                break;
            case 0x0B:
                hCode = "L";
                break;
            case 0x00:
                hCode = "M";
                break;
            case 0x08:
                hCode = "N";
                break;
            case 0x04:
                hCode = "O";
                break;
            case 0x0C:
                hCode = "P";
                break;
            default:
                hCode = "Unk hC:" + hC;
                break;
        }
        return hCode;
    }
}
