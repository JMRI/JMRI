// SkemeStartEditor.java

package jmri.jmrix.loconet.sdfeditor;

import java.util.ArrayList;

import jmri.jmrix.loconet.sdf.SdfMacro;

/**
 * Editor panel for the SKEME_START macro from the Digitrax sound definition language.
 *<P>
 * This nests until the next SKEME_START.
 *
 * @author		Bob Jacobsen  Copyright (C) 2007
 * @version             $Revision: 1.2 $
 */

class SkemeStartEditor extends SdfMacroEditor {

    public SkemeStartEditor(SdfMacro inst) {
        super(inst);
    }
}

/* @(#)SkemeStartEditor.java */
