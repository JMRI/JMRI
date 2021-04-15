package jmri.jmrit.display.layoutEditor.LayoutEditorDialogs;

import javax.annotation.Nonnull;

import jmri.Turnout;
import jmri.jmrit.display.layoutEditor.LayoutEditor;

/**
 * MVC Editor component for LayoutRHTurnout objects.
 *
 * @author Bob Jacobsen  Copyright (c) 2020
 * 
 */
public class LayoutRHTurnoutEditor extends LayoutTurnoutEditor {

    /**
     * constructor method.
     * @param layoutEditor main layout editor.
     */
    public LayoutRHTurnoutEditor(@Nonnull LayoutEditor layoutEditor) {
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
    

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutRHTurnoutEditor.class);
}
