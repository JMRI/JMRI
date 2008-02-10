// FourByteMacroEditor.java

package jmri.jmrix.loconet.sdfeditor;

import jmri.jmrix.loconet.sdf.SdfMacro;

/**
 * Editor panel for the generic four-byte macros from the Digitrax sound definition language
 * <p>
 * In theory, this should never be invoked, because all the macros
 * have specific editors.  But editors like this never keep up with
 * changes to content of underlying data, so we provide this as an
 * escape claus for unrecognized content.
 *
 * @author		Bob Jacobsen  Copyright (C) 2007, 2008
 * @version             $Revision: 1.3 $
 */

class FourByteMacroEditor extends SdfMacroEditor {

    public FourByteMacroEditor(SdfMacro inst) {
        super(inst);
    }
}

/* @(#)FourByteMacroEditor.java */
