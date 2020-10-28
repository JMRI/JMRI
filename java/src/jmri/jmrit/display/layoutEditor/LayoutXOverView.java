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
 * MVC View component for the LayoutXOver class.
 *
 * @author Bob Jacobsen  Copyright (c) 2020
 * 
 */
public class LayoutXOverView extends LayoutTurnoutView {

    /**
     * Constructor method.
     * @param xover the layout crossover.
     */
    public LayoutXOverView(@Nonnull LayoutXOver xover) {
        super(xover);
        // this.xover = xover;
    }
        
    // final private LayoutXOver xover;

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutXOverView.class);
}
