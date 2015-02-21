// SdlVersionEditor.java
package jmri.jmrix.loconet.sdfeditor;

import jmri.jmrix.loconet.sdf.SdfMacro;

/**
 * Editor panel for the SDL_VERSION macro from the Digitrax sound definition
 * language
 *
 * @author	Bob Jacobsen Copyright (C) 2007
 * @version $Revision$
 */
class SdlVersionEditor extends SdfMacroEditor {

    /**
     *
     */
    private static final long serialVersionUID = -1892658707818585877L;

    public SdlVersionEditor(SdfMacro inst) {
        super(inst);
        // No editor needed, leave default message in place.
    }
}

/* @(#)SdlVersionEditor.java */
