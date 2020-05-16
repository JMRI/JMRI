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
 * MVC View component for the LayoutWye class.
 *
 * @author Bob Jacobsen  Copyright (c) 2020
 * 
 */
public class LayoutWyeView extends LayoutTurnoutView {

    /**
     * Constructor method.
     * @param wye the wye to base view on.
     * @param c 2D point position.
     * @param rot rotation.
     * @param xFactor horizontal factor.
     * @param yFactor vertical factor.
     * @param layoutEditor main layout editor.
     */
    public LayoutWyeView(@Nonnull LayoutWye wye, 
            @Nonnull Point2D c, double rot,
            double xFactor, double yFactor,
            @Nonnull LayoutEditor layoutEditor) {
        super(wye, c, rot, xFactor, yFactor, layoutEditor);
        // this.wye = wye;
    }
        
    // final private LayoutWye wye;

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutWyeView.class);
}
