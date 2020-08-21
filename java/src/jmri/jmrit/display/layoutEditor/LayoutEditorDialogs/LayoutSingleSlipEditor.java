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
 * MVC Editor component for LayoutSingleSlip objects.
 *
 * The single slip is the default behavior of the LayoutSlipEditor super class
 *
 * @author Bob Jacobsen  Copyright (c) 2020
 * 
 */
public class LayoutSingleSlipEditor extends LayoutSlipEditor {

    /**
     * constructor method.
     * @param layoutEditor main layout editor.
     */
    public LayoutSingleSlipEditor(@Nonnull LayoutEditor layoutEditor) {
        super(layoutEditor);
    }

    // These now reflect to code in the base class; eventually this heirarchy will
    // expand and the code will be brought here
    

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutSingleSlipEditor.class);
}
