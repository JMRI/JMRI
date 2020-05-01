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
 * MVC View component for the LayoutTurnout class.
 *
 * @author Bob Jacobsen  Copyright (c) 2020
 * 
 */
public class LayoutTurnoutView extends LayoutTrackView {

    /**
     * constructor method
     */
    public LayoutTurnoutView(@Nonnull LayoutTurnout turnout) {
        super(turnout);
        this.turnout = turnout;
    }
        
    final private LayoutTurnout turnout;

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutTurnoutView.class);
}
