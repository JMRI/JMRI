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

import jmri.jmrit.display.layoutEditor.LayoutTurnout.TurnoutType;

/**
 * MVC View component for the LayoutTurnout class.
 *
 * @author Bob Jacobsen  Copyright (c) 2020
 * 
 */
public class LayoutTurnoutView extends LayoutTrackView {

    /**
     * Constructor method.
     * @param turnout the layout turnout to create the view for.
     */
    public LayoutTurnoutView(@Nonnull LayoutTurnout turnout) {
        super(turnout);
        this.turnout = turnout;
    }
        
    final private LayoutTurnout turnout;

    // These now reflect to code in the base class; eventually this heirarchy will
    // expand and the code will be brought here
    
    protected boolean isDisabled() {
        return turnout.isDisabled();
    }
    public Point2D getCoordsA() {
        return turnout.getCoordsA();
    }
    public Point2D getCoordsB() {
        return turnout.getCoordsB();
    }
    public Point2D getCoordsC() {
        return turnout.getCoordsC();
    }
    public Point2D getCoordsD() {
        return turnout.getCoordsD();
    }
    public boolean isMainlineA() {
        return turnout.isMainlineA();
    }
    public boolean isMainlineB() {
        return turnout.isMainlineB();
    }
    public boolean isMainlineC() {
        return turnout.isMainlineC();
    }
    public boolean isMainlineD() {
        return turnout.isMainlineD();
    }

    protected void drawTurnoutControls(Graphics2D g2) {
        turnout.drawTurnoutControls(g2);
    }

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutTurnoutView.class);
}
