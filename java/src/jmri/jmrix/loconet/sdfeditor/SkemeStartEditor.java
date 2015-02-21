// SkemeStartEditor.java
package jmri.jmrix.loconet.sdfeditor;

import jmri.jmrix.loconet.sdf.SdfMacro;

/**
 * Editor panel for the SKEME_START macro from the Digitrax sound definition
 * language.
 * <P>
 * This nests until the next SKEME_START.
 *
 * @author	Bob Jacobsen Copyright (C) 2007
 * @version $Revision$
 */
class SkemeStartEditor extends SdfMacroEditor {

    /**
     *
     */
    private static final long serialVersionUID = 4488606516130401644L;

    public SkemeStartEditor(SdfMacro inst) {
        super(inst);
        // No editor needed, leave default message in place.
    }
}

/* @(#)SkemeStartEditor.java */
