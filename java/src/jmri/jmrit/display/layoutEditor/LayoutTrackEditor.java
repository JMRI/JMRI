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
 * MVC Editor component for the LayoutTrack hierarchy.
 *
 * @author Bob Jacobsen  Copyright (c) 2020
 * 
 */
public class LayoutTrackEditor {

    /**
     * constructor method
     */
    public LayoutTrackEditor(@Nonnull LayoutTrack track) {
         this.layoutTrack = track;
    }

    final private LayoutTrack layoutTrack;

    // These now reflect to code in the base class; eventually this heirarchy will
    // expand and the code will be brought here
    

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutTrackEditor.class);
}
