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
 * MVC Editor component for LayoutDoubleXOver objects.
 *
 * Note there might not be anything for this class to do;
 * LayoutTrackEditors has a comment saying that PositionablePoint
 * doesn't have an editor.
 *
 * @author Bob Jacobsen  Copyright (c) 2020
 * 
 */
public class LayoutDoubleXOverEditor extends LayoutXOverEditor {

    /**
     * constructor method.
     * @param layoutEditor main layout editor.
     */
    public LayoutDoubleXOverEditor(@Nonnull LayoutEditor layoutEditor) {
        super(layoutEditor);
    }

    // These now reflect to code in the base class; eventually this heirarchy will
    // expand and the code will be brought here
    

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutDoubleXOverEditor.class);
}
