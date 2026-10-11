package jmri.jmrit.display.layoutEditor;

import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.TurnoutIcon;

/**
 * This class exists to change the class name that's being 
 * created for a Layout Editor's Output Indicator so that it will show up
 * as such in the Layout Editor contextual menu.
 * 
 * Please don't add any behaviors here, as that will make 
 * this work differently from the parent TurnoutIcon.
 */
public class OutputIndicator extends TurnoutIcon {
    public OutputIndicator(NamedIcon ni, Editor editor) {
        super(ni, editor);
    }
    public OutputIndicator(Editor editor) {
        super(editor);
    }
}
