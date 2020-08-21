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
 * MVC View component for the LayoutTurntable class.
 *
 * @author Bob Jacobsen  Copyright (c) 2020
 * 
 */
public class LayoutTurntableView extends LayoutTrackView {

    /**
     * Constructor method.
     * @param turntable the layout turntable to create view for.
     */
    public LayoutTurntableView(@Nonnull LayoutTurntable turntable) {
        super(turntable);
        this.turntable = turntable;
    }

    final private LayoutTurntable turntable;
   
    protected void drawTurnoutControls(Graphics2D g2) {
        turntable.drawTurnoutControls(g2);
    }
     
    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutTurntableView.class);
}
