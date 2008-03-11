// X10Sequence.java

package jmri.jmrix.powerline;


/**
 * Represent a sequence of one or more X10 commands (addresses and functions).
 * <p>
 * These are X10 specific, but not device/interface specific.
 * <p>
 * A sequence should consist of addressing (1 or more), and then
 * one or more commands. It can address multiple devices, but 
 * not more than one house-code.
 *
 * @author			Bob Jacobsen Copyright (C) 2008
 * @version			$Revision: 1.2 $
 */
public class X10Sequence {

    public static final int FUNCTION_ALL_UNITS_OFF          = 0;
    public static final int FUNCTION_ALL_LIGHTS_ON          = 1;
    public static final int FUNCTION_ON                     = 2;
    public static final int FUNCTION_OFF                    = 3;
    public static final int FUNCTION_DIM                    = 4;
    public static final int FUNCTION_BRIGHT                 = 5;
    public static final int FUNCTION_ALL_LIGHTS_OFF         = 6;
    public static final int FUNCTION_EXTENDED_CODE          = 7;
    public static final int FUNCTION_HAIL_REQUEST           = 8;
    public static final int FUNCTION_HAIL_ACKNOWLEDGE       = 9;
    public static final int FUNCTION_PRESET_DIM_1           = 10;
    public static final int FUNCTION_PRESET_DIM_2           = 11;
    public static final int FUNCTION_EXTENDED_DATA_TRANSFER = 12;
    public static final int FUNCTION_STATUS_ON              = 13;
    public static final int FUNCTION_STATUS_OFF             = 14;
    public static final int FUNCTION_STATUS_REQUEST         = 15;

    private static final int MAXINDEX = 32;
    
    public void addFunction(int house, int function, int dimcount) {
        if (index >= MAXINDEX) throw new IllegalArgumentException("Sequence too long");
        cmds[index] = new Function(house, function, dimcount);
        index++;
    }

    public void addAddress(int house, int device) {
        if (index >= MAXINDEX) throw new IllegalArgumentException("Sequence too long");
        cmds[index] = new Address(house, device);
        index++;
    }
    
    int index = 0;
    Command[] cmds = new Command[MAXINDEX];  // doesn't scale, but that's for another day
    
    /**
     * Next getCommand will be the first in the sequence
     */
    public void reset(){
        index = 0;
    }
    
    public Command getCommand() {
        return cmds[index++];
    }
    
    public interface Command {
        public boolean isAddress();
        public boolean isFunction();
        public int getHouseCode();
    }
    public class Address implements Command {
        public Address(int house, int device) {
            this.house = house;
            this.device = device;
        }
        int house;
        int device;
        public int getHouseCode() { return house; }
        public int getAddress()  { return device; }
        public boolean isAddress() { return true; }
        public boolean isFunction() { return false; }
    }
    public class Function implements Command {
        public Function(int house, int function, int dimcount) {
            this.house = house;
            this.function = function;
            this.dimcount = dimcount;
        }
        int house;
        int function;
        int dimcount;
        public int getHouseCode() { return house; }
        public int getFunction()  { return function; }
        public int getDimCount()  { return dimcount; }
        public boolean isAddress() { return false; }
        public boolean isFunction() { return true; }
    }
    
    static String[] functionNames = new String[]{
        "All Off", "All Lights On", "On", "Off",
        "Dim", "Bright", "All Lights Off", "Extended Code",
        "Hail Request", "Hail Ack", "Preset Dim 1", "Preset Dim 2",
        "Ext Data Trnsfr", "Status On", "Status Off", "Status Req"
    };
    
    /**
     * Return a readable name for a function code
     */
    public static String functionName(int i) {
        return functionNames[i];
    }
    
    /**
     * For the house (A-P) and device (1-16) codes, get
     * the line-coded value.
     * Argument is from 1 to 16 only.
     */
    public static int encode(int i) {
        if (i<1 || i>16) throw new IllegalArgumentException("Encode outside 1-16: "+i);
        return encoder[i];
    }
    static final int[] encoder = new int[]{-1, 
                                    0x6, 0xE, 0x2, 0xA,   0x1, 0x9, 0x5, 0xD, // 1-8
                                    0x7, 0xF, 0x3, 0xB,   0x0, 0x8, 0x4, 0xC};

    /**
     * Get house (A-P) or device (1-16) from line-coded
     * value.
     */
    public static int decode(int i) {
        if (i<0 || i>15) throw new IllegalArgumentException("Decode outside 1-16: "+i);
        return decoder[i];
    }
    static final int[] decoder = new int[]{13, 
                                    5, 3, 11, 15,   7, 1, 9, 14, // 1-8
                                    6, 4, 12, 16,   8, 2, 10}; // 9-15

    /**
     * Pretty-print an address code
     */
    public static String formatAddressByte(int b) {
        return "House "+ X10Sequence.decode((b>>4)&0x0F)
            +" address device "+X10Sequence.decode(b&0x0f);
    }

    /**
     * Pretty-print a function code
     */
    public static String formatCommandByte(int b) {
        return "House "+ X10Sequence.decode((b>>4)&0x0F)
                +" function: "+X10Sequence.functionName(b&0x0f);
    }

}


/* @(#)X10Sequence.java */
