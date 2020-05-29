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
     * Main constructor method.
     * @param xover the layout right hand crossover to view.
     * @param c 2D point.
     * @param rot rotation.
     * @param xFactor horizontal factor.
     * @param yFactor vertical factor.
     * @param layoutEditor main layout editor.
     */
    public LayoutRHXOverView(@Nonnull LayoutRHXOver xover, 
            @Nonnull Point2D c, double rot,
            double xFactor, double yFactor,
            @Nonnull LayoutEditor layoutEditor) {
        super(xover, c, rot, xFactor, yFactor, layoutEditor);
       // this.xover = xover;

        editor = new jmri.jmrit.display.layoutEditor.LayoutEditorDialogs.LayoutRHXOverEditor(layoutEditor);
    }
       
    // final private LayoutRHXOver xover;

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutRHXOverView.class);
}
