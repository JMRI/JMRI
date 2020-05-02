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
 * MVC View component for the LayoutTrack hierarchy.
 *
 * @author Bob Jacobsen  Copyright (c) 2020
 * 
 */
public class LayoutTrackView {

    /**
     * constructor method
     */
    public LayoutTrackView(@Nonnull LayoutTrack track) {
         this.layoutTrack = track;
    }

    final private LayoutTrack layoutTrack;

    // These now reflect to code in the base class; eventually this heirarchy will
    // expand and the code will be brought here
    
    protected void draw1(Graphics2D g2, boolean isMain, boolean isBlock) {
        layoutTrack.draw1(g2, isMain, isBlock);
    }
    
    protected void draw2(Graphics2D g2, boolean isMain, float railDisplacement) {
        layoutTrack.draw2(g2, isMain, railDisplacement);
    }

    protected void drawDecorations(Graphics2D g2) {
        layoutTrack.drawDecorations(g2);
    }
    
    public boolean isHidden() {
        return layoutTrack.isHidden();
    }

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutTrackView.class);
}
