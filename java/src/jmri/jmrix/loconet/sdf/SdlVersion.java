// SdlVersion.java
package jmri.jmrix.loconet.sdf;

/**
 * Implement the SDL_VERSION macro from the Digitrax sound definition language
 *
 * @author	Bob Jacobsen Copyright (C) 2007
 * @version $Revision$
 */
public class SdlVersion extends SdfMacro {

    public SdlVersion(int version) {
        this.version = version;
    }

    public String name() {
        return "SDL_VERSION";
    }

    int version;

    public int length() {
        return 2;
    }

    static public SdfMacro match(SdfBuffer buff) {
        if ((buff.getAtIndex() & 0xFF) != 0x82) {
            return null;
        }
        buff.getAtIndexAndInc(); // drop op code
        return new SdlVersion(buff.getAtIndexAndInc());
    }

    /**
     * Store into a buffer.
     */
    public void loadByteArray(SdfBuffer buffer) {
        // data
        buffer.setAtIndexAndInc(0x82);
        buffer.setAtIndexAndInc(version);

        // store children
        super.loadByteArray(buffer);
    }

    public String toString() {
        return "Version " + (version == 0x10 ? "1" : "<unknown code>") + '\n';
    }

    public String oneInstructionString() {
        return name() + ' ' + (version == 0x10 ? "VERSION_1" : "Unknown code " + version) + '\n';
    }

    public String allInstructionString(String indent) {
        return indent + oneInstructionString();
    }
}

/* @(#)SdlVersion.java */
