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
 * MVC View component for the LevelXing class
 *
 * @author Bob Jacobsen  Copyright (c) 2020
 * 
 */
public class LevelXingView extends LayoutTrackView {

    /**
     * Constructor method.
     * @param xing the level crossing.
     */
    public LevelXingView(@Nonnull LevelXing xing) {
        super(xing);
        // this.xing = xing;
    }
    
    // final private LevelXing xing;

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LevelXingView.class);
}
