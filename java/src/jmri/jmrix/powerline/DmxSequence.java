package jmri.jmrix.powerline;

/**
 * Represent a sequence of one or more Dmx commands (unit and intensity).
 * <p>
 * These are Dmx specific, but not device/interface specific.
 * <p>
 * A sequence should consist of addressing (1 or more), and then one or more
 * commands. It can address multiple devices.
 *
 * @author Bob Coleman Copyright (C) 2010
 * @author Bob Jacobsen Copyright (C) 2008
 * @author Ken Cameron Copyright (C) 2023
 */
public class DmxSequence {

    // First implementation of this class uses a fixed length
    // array to hold the sequence; there's a practical limit to how
    // many Dmx commands anybody would want to send at once!
    private static final int MAXINDEX = 32;
    private int index = 0;
    private Command[] cmds = new Cmd[MAXINDEX];  // doesn't scale, but that's for another day

    /**
     * Append a new "do command" operation to the sequence
     * @param unit   1st id value
     * @param value 2nd id value
     */
    public void addCommand(int unit, byte value) {
        if (index >= MAXINDEX) {
            throw new IllegalArgumentException("Sequence too long");
        }
        cmds[index] = new Cmd(unit, value);
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
     * @return single DMx cmd
     */
    public Command getCommand() {
        return cmds[index++];
    }

    /**
     * Represent a single Dmx command, which is a unit and intensity pair
     */
    public interface Command {

        public int getUnit();

        public byte getValue();
    }

    /**
     * Represent a single Dmx command
     */
    public static class Cmd implements Command {

        int unit;
        byte value;
        public Cmd(int unit, byte value) {
            this.unit = unit;
            this.value = value;
        }

        @Override
        public int getUnit() {
            return unit;
        }

        @Override
        public byte getValue() {
            return value;
        }
    }

}
