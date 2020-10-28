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
 * MVC View component for the LayoutSlip class.
 *
 * @author Bob Jacobsen  Copyright (c) 2020
 * 
 */
public class LayoutSlipView extends LayoutTurnoutView {

    /**
     * Constructor method.
     * @param slip the layout sip to create view for.
     */
    public LayoutSlipView(@Nonnull LayoutSlip slip) {
        super(slip);
        // this.slip = slip;
    }
        
    // final private LayoutSlip slip;

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutSlipView.class);
}
