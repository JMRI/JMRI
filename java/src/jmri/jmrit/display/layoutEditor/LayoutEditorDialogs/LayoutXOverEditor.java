package jmri.jmrit.display.layoutEditor.LayoutEditorDialogs;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.geom.*;
import java.util.*;
import javax.annotation.*;
import javax.swing.JPopupMenu;

import jmri.*;
import jmri.jmrit.display.layoutEditor.*;
import jmri.util.*;

/**
 * MVC Editor component for LayoutXOver objects.
 *
 * @author Bob Jacobsen  Copyright (c) 2020
 * 
 */
public class LayoutXOverEditor extends LayoutTrackEditor {

    /**
     * constructor method
     */
    public LayoutXOverEditor(@Nonnull LayoutEditor layoutEditor) {
        super(layoutEditor);
    }

    /**
     * Edit a XOver
     */
    public void editLayoutTrack(@Nonnull LayoutTrack layoutTrack) {
        log.error("no editor installed for XOvers", new Exception("traceback"));
    }
    

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutXOverEditor.class);
}
