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
 * MVC View component for the PositionablePoint class.
 *
 * @author Bob Jacobsen  Copyright (c) 2020
 * 
 */
public class PositionablePointView extends LayoutTrackView {

    /**
     * constructor method.
     * @param point the positionable point.
     */
    public PositionablePointView(@Nonnull PositionablePoint point) {
        super(point);
        this.positionablePoint = point;
    }

    final private PositionablePoint positionablePoint;
    
    // These now reflect to code in the base class; eventually this heirarchy will
    // expand and the code will be brought here
    
    @Override
    protected void draw1(Graphics2D g2, boolean isMain, boolean isBlock) {
        positionablePoint.draw1(g2, isMain, isBlock);
    }
    
    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PositionablePointView.class);
}
