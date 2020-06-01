package jmri.jmrit.display.layoutEditor.LayoutEditorDialogs;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.geom.*;
import java.util.*;
import javax.annotation.*;
import javax.swing.JPopupMenu;

import jmri.*;
import jmri.jmrit.display.layoutEditor.*;
import jmri.util.*;

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
