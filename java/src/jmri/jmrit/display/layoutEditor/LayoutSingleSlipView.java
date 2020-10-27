package jmri.jmrit.display.layoutEditor;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.geom.*;
import java.util.*;
import javax.annotation.*;
import javax.swing.JPopupMenu;
import jmri.*;
import jmri.util.*;

/**
 * MVC View component for the LayoutSingleSlip class.
 *
 * @author Bob Jacobsen  Copyright (c) 2020
 * 
 */
public class LayoutSingleSlipView extends LayoutSlipView {

    /**
     * Constructor method.
     * @param slip the slip to create view for.
     * @param c       where to put it
     * @param rot     for display
     * @param layoutEditor what layout editor panel to put it in
     */
    public LayoutSingleSlipView(@Nonnull LayoutSingleSlip slip, Point2D c, double rot, @Nonnull LayoutEditor layoutEditor) {
        super(slip, c, rot, layoutEditor);
        // this.slip = slip;

        editor = new jmri.jmrit.display.layoutEditor.LayoutEditorDialogs.LayoutSingleSlipEditor(layoutEditor);
    }
        
    // final private LayoutSingleSlip slip;

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutSingleSlipView.class);
}
