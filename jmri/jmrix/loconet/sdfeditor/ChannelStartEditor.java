// ChannelStartEditor.java

package jmri.jmrix.loconet.sdfeditor;

import jmri.jmrix.loconet.sdf.SdfMacro;
import javax.swing.JLabel;

/**
 * Editor panel for the CHANNEL_START macro from the Digitrax sound definition language
 *
 * @author		Bob Jacobsen  Copyright (C) 2007
 * @version             $Revision: 1.3 $
 */

class ChannelStartEditor extends SdfMacroEditor {

    public ChannelStartEditor(SdfMacro inst) {
        super(inst);
        // No editor needed, leave default message in place.
    }
}

/* @(#)ChannelStartEditor.java */
