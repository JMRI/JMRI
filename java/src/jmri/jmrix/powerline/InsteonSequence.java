package jmri.jmrix.powerline;

/**
 * Represent a sequence of one or more Insteon commands (addresses and
 * functions).
 * <p>
 * These are Insteon specific, but not device/interface specific.
 * <p>
 * A sequence should consist of addressing (1 or more), and then one or more
 * commands. It can address multiple devices.
 *
 * @author	Bob Coleman Copyright (C) 2010
 * @author	Bob Jacobsen Copyright (C) 2008
 * @author	Ken Cameron Copyright (C) 2010
 */
public class InsteonSequence {

    // First implementation of this class uses a fixed length
    // array to hold the sequence; there's a practical limit to how
    // many Insteon commands anybody would want to send at once!
    private static final int MAXINDEX = 32;
    int index = 0;
    Command[] cmds = new Command[MAXINDEX];  // doesn't scale, but that's for another day

    /**
     * Append a new "do function" operation to the sequence
     */
    public void addFunction(int idhighbyte, int idmiddlebyte, int idlowbyte, int function, int flag, int command1, int command2) {
        if (index >= MAXINDEX) {
            throw new IllegalArgumentException("Sequence too long");
        }
        cmds[index] = new Function(idhighbyte, idmiddlebyte, idlowbyte, function, flag, command1, command2);
        index++;
    }

    /**
     * Append a new "set address" operation to the sequence
     */
    public void addAddress(int idhighbyte, int idmiddlebyte, int idlowbyte) {
        if (index >= MAXINDEX) {
            throw new IllegalArgumentException("Sequence too long");
        }
        cmds[index] = new Address(idhighbyte, idmiddlebyte, idlowbyte);
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
     */
    public Command getCommand() {
        return cmds[index++];
    }

    /**
     * Represent a single Insteon command, which is either a "set address" or
     * "do function" operation
     */
    public interface Command {

        public boolean isAddress();

        public boolean isFunction();

        public int getAddressHigh();

        public int getAddressMiddle();

        public int getAddressLow();
    }

    /**
     * Represent a single "set address" Insteon command
     */
    public static class Address implements Command {

        public Address(int idhighbyte, int idmiddlebyte, int idlowbyte) {
            this.idhighbyte = idhighbyte;
            this.idmiddlebyte = idmiddlebyte;
            this.idlowbyte = idlowbyte;
        }
        int idhighbyte;
        int idmiddlebyte;
        int idlowbyte;

        public int getAddressHigh() {
            return idhighbyte;
        }

        public int getAddressMiddle() {
            return idmiddlebyte;
        }

        public int getAddressLow() {
            return idlowbyte;
        }

        public boolean isAddress() {
            return true;
        }

        public boolean isFunction() {
            return false;
        }
    }

    /**
     * Represent a single "do function" Insteon command
     */
    public static class Function implements Command {

        public Function(int idhighbyte, int idmiddlebyte, int idlowbyte, int function, int flag, int command1, int command2) {
            this.idhighbyte = idhighbyte;
            this.idmiddlebyte = idmiddlebyte;
            this.idlowbyte = idlowbyte;
            this.function = function;
            this.flag = flag;
            this.command1 = command1;
            this.command2 = command2;
        }
        int idhighbyte;
        int idmiddlebyte;
        int idlowbyte;
        int function;
        int flag;
        int command1;
        int command2;

        public int getAddressHigh() {
            return idhighbyte;
        }

        public int getAddressMiddle() {
            return idmiddlebyte;
        }

        public int getAddressLow() {
            return idlowbyte;
        }

        public int getFunction() {
            return function;
        }

        public int getFlag() {
            return flag;
        }

        public int getCommand1() {
            return command1;
        }

        public int getCommand2() {
            return command2;
        }

        public boolean isAddress() {
            return false;
        }

        public boolean isFunction() {
            return true;
        }
    }

    /**
     * Represent a single "Extended Data" Insteon command
     */
    public static class ExtData implements Command {

        public ExtData(int value) {
            this.value = value;
            this.idhighbyte = -1;
            this.idmiddlebyte = -1;
            this.idlowbyte = -1;
        }
        int idhighbyte;
        int idmiddlebyte;
        int idlowbyte;
        int value;

        public int getAddressHigh() {
            return idhighbyte;
        }

        public int getAddressMiddle() {
            return idmiddlebyte;
        }

        public int getAddressLow() {
            return idlowbyte;
        }

        public int getExtData() {
            return value;
        }

        public boolean isAddress() {
            return false;
        }

        public boolean isFunction() {
            return false;
        }
    }

    /**
     * Return a human-readable name for a function code
     */
    public static String functionName(int i) {
        return X10Sequence.functionName(i);
    }

    /**
     * For the house (A-P) and device (1-16) codes, get the line-coded value.
     * Argument is from 1 to 16 only.
     */
    public static int encode(int i) {
        return X10Sequence.encode(i);
    }

    /**
     * Get house (A-P as 1-16) or device (1-16) from line-coded value.
     */
    public static int decode(int i) {
        return X10Sequence.decode(i);
    }

    /**
     * Pretty-print an address code
     */
    public static String formatAddressByte(int b) {
        return "House " + X10Sequence.houseValueToText(X10Sequence.decode((b >> 4) & 0x0F))
                + " address device " + X10Sequence.decode(b & 0x0f);
    }

    /**
     * Pretty-print a function code
     */
    public static String formatCommandByte(int b) {
        return "House " + X10Sequence.houseValueToText(X10Sequence.decode((b >> 4) & 0x0F))
                + " function: " + X10Sequence.functionName(b & 0x0f);
    }

    /**
     * Translate House Value (1 to 16) to text
     */
    public static String houseValueToText(int hV) {
        if (hV >= 1 || hV <= 16) {
            return X10Sequence.houseValueToText(hV);
        } else {
            return "??";
        }
    }

    /**
     * Translate House Code to text
     */
    public static String houseCodeToText(int hC) {
        return X10Sequence.houseCodeToText(hC);
    }

}
