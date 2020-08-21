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
     */
    public LayoutSingleSlipView(@Nonnull LayoutSingleSlip slip) {
        super(slip);
        // this.slip = slip;
    }
        
    // final private LayoutSingleSlip slip;

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutSingleSlipView.class);
}
