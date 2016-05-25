// ChannelStartEditor.java
package jmri.jmrix.loconet.sdfeditor;

import jmri.jmrix.loconet.sdf.SdfMacro;

/**
 * Editor panel for the CHANNEL_START macro from the Digitrax sound definition
 * language
 *
 * @author	Bob Jacobsen Copyright (C) 2007
 * @version $Revision$
 */
class ChannelStartEditor extends SdfMacroEditor {

    /**
     *
     */
    private static final long serialVersionUID = 4814718693465031006L;

    public ChannelStartEditor(SdfMacro inst) {
        super(inst);
        // No editor needed, leave default message in place.
    }
}

/* @(#)ChannelStartEditor.java */
