package jmri.jmrit.display.layoutEditor;

import java.awt.geom.Point2D;

import javax.annotation.Nonnull;

/**
 * MVC View component for the LayoutXOver class.
 *
 * @author Bob Jacobsen  Copyright (c) 2020
 * 
 */
public class LayoutXOverView extends LayoutTurnoutView {

    /**
     * Constructor method.
     * @param xover the layout crossover
     * @param c displays location
     * @param rot for display
     * @param xFactor for display
     * @param yFactor for display
     * @param layoutEditor for access to tools
     */
    public LayoutXOverView(@Nonnull LayoutXOver xover, 
            @Nonnull Point2D c, double rot,
            double xFactor, double yFactor, 
            @Nonnull LayoutEditor layoutEditor) {
        super(xover, c, rot, xFactor, yFactor, layoutEditor);
        // this.xover = xover;

        editor = new jmri.jmrit.display.layoutEditor.LayoutEditorDialogs.LayoutXOverEditor(layoutEditor);
    }
        
    // final private LayoutXOver xover;

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutXOverView.class);
}
