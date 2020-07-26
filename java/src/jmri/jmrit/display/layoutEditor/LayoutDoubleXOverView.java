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
 * MVC View component for the LayoutDoubleXOver class.
 *
 * @author Bob Jacobsen  Copyright (c) 2020
 * 
 */
public class LayoutDoubleXOverView extends LayoutXOverView {

    /**
     * Constructor method.
     * @param xover the layout double crossover to view.
     */
    public LayoutDoubleXOverView(@Nonnull LayoutDoubleXOver xover) {
        super(xover);
        // this.xover = xover;
    }
        
    // final private LayoutDoubleXOver xover;

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutDoubleXOverView.class);
}
