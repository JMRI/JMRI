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
 * MVC View component for the LayoutDoubleSlipclass.
 *
 * @author Bob Jacobsen  Copyright (c) 2020
 * 
 */
public class LayoutDoubleSlipView extends LayoutSlipView {

    /**
     * Constructor method.
     * @param slip the layout double slip to view.
     */
    public LayoutDoubleSlipView(@Nonnull LayoutDoubleSlip slip) {
        super(slip);
        // this.slip = slip;
    }
        
    // final private LayoutDoubleSlip slip;

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutDoubleSlipView.class);
}
