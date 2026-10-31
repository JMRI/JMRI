package jmri.jmrix.marklin.mcan.cs2;

import jmri.jmrix.marklin.mcan.FrameProtocol;
import jmri.jmrix.marklin.mcan.McanFrame;
import jmri.jmrix.marklin.mcan.RawFrame;

/**
 * Base class for all CS2 native protocol frames.
 * <p>
 * Carries header, data and resolved command. Used directly for
 * {@link Cs2Command#UNKNOWN} frames; known commands are represented by typed
 * subclasses that add semantic accessors.
 */
public class Cs2Frame extends McanFrame {

    private final Cs2Header  header;
    private final Cs2Data    data;
    private final Cs2Command command;

    public Cs2Frame(Cs2Header header, Cs2Data data, RawFrame raw) {
        super(FrameProtocol.CS2_NATIVE, raw);
        this.header  = header;
        this.data    = data;
        this.command = header.resolvedCommand();
    }

    public Cs2Header  header()  { return header; }
    public Cs2Data    data()    { return data; }
    public Cs2Command command() { return command; }
}
