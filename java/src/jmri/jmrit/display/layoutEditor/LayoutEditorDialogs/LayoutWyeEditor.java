package jmri.jmrit.display.layoutEditor.LayoutEditorDialogs;

import javax.annotation.*;
import jmri.*;
import jmri.jmrit.display.layoutEditor.*;

/**
 * MVC Editor component for LayoutWye objects.
 *
 * Note there might not be anything for this class to do;
 * LayoutTrackEditors has a comment saying that PositionablePoint
 * doesn't have an editor.
 *
 * @author Bob Jacobsen  Copyright (c) 2020
 * 
 */
public class LayoutWyeEditor extends LayoutTurnoutEditor {

    /**
     * constructor method.
     * @param layoutEditor main layout editor.
     */
    public LayoutWyeEditor(@Nonnull LayoutEditor layoutEditor) {
        super(layoutEditor);
    }

    // set the continuing route Turnout State
    @Override
    protected void setContinuingRouteTurnoutState() {
        layoutTurnout.setContinuingSense(Turnout.CLOSED);
        if (editLayoutTurnoutStateComboBox.getSelectedIndex() == editLayoutTurnoutThrownIndex) {
            layoutTurnout.setContinuingSense(Turnout.THROWN);
        }
    }
    

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutWyeEditor.class);
}
