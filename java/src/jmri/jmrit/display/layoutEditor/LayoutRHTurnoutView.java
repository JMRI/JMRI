package jmri.jmrit.display.layoutEditor;

import java.awt.geom.Point2D;

import javax.annotation.Nonnull;

/**
 * MVC View component for the LayoutRHTurnout class.
 *
 * @author Bob Jacobsen  Copyright (c) 2020
 * 
 */
public class LayoutRHTurnoutView extends LayoutTurnoutView {

    /**
     * Constructor method.
     * @param turnout the turnout to view.
     * @param c       where to put it
     * @param rot     for display
     * @param xFactor     for display
     * @param yFactor     for display
     * @param layoutEditor what layout editor panel to put it in
     */
    public LayoutRHTurnoutView(@Nonnull LayoutRHTurnout turnout, 
            @Nonnull Point2D c, double rot,
            double xFactor, double yFactor,
            @Nonnull LayoutEditor layoutEditor) {
        super(turnout, c, rot, xFactor, yFactor, layoutEditor);
        
        // this.turnout = turnout;

        editor = new jmri.jmrit.display.layoutEditor.LayoutEditorDialogs.LayoutRHTurnoutEditor(layoutEditor);
    }
        
    // final private LayoutRHTurnout turnout;

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutRHTurnoutView.class);
}
