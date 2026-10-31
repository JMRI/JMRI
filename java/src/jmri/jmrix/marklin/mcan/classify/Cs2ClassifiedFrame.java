package jmri.jmrix.marklin.mcan.classify;

/**
 * Base class for all CS2 native protocol parse results.
 * <p>
 * Carries the full parse context: header, data and resolved command.
 * Used directly for {@link Cs2Command#UNKNOWN} frames.
 * Known commands are represented by typed subclasses in the {@code cs2}
 * sub-package that add semantic accessors.
 */
public class Cs2ClassifiedFrame {

    private final Cs2Header      header;
    private final Cs2Data        data;
    private final Cs2Command     command;
    private final ClassifiedFrame parent;

    public Cs2ClassifiedFrame(Cs2Header header, Cs2Data data, ClassifiedFrame parent) {
        this.header  = header;
        this.data    = data;
        this.command = header.resolvedCommand();
        this.parent  = parent;
    }

    public Cs2Header      header()  { return header; }
    public Cs2Data        data()    { return data; }
    public Cs2Command     command() { return command; }
    public ClassifiedFrame parent() { return parent; }
}
