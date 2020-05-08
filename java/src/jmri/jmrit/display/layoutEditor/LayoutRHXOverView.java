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
 * MVC View component for the LayoutRHXOver class.
 *
 * @author Bob Jacobsen  Copyright (c) 2020
 * 
 */
public class LayoutRHXOverView extends LayoutXOverView {

    /**
     * constructor method
     */
    public LayoutRHXOverView(@Nonnull LayoutRHXOver xover, @Nonnull LayoutEditor layoutEditor) {
        super(xover, layoutEditor);
        // this.xover = xover;
    }
        
    // final private LayoutRHXOver xover;

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutRHXOverView.class);
}
