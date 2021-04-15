package jmri.jmrit.display.layoutEditor;

import java.awt.geom.Point2D;

import javax.annotation.Nonnull;

/**
 * MVC View component for the LayoutDoubleSlipclass.
 *
 * @author Bob Jacobsen  Copyright (c) 2020
 * 
 */
public class LayoutDoubleSlipView extends LayoutSlipView {

    /**
     * Constructor method.
     * @param slip the layout double slip to view.
     * @param c 2D point.
     * @param rot rotation.
     * @param layoutEditor main layout editor.
     */
    public LayoutDoubleSlipView(@Nonnull LayoutDoubleSlip slip, Point2D c, double rot, @Nonnull LayoutEditor layoutEditor) {
        super(slip, c, rot, layoutEditor);
        // this.slip = slip;

        editor = new jmri.jmrit.display.layoutEditor.LayoutEditorDialogs.LayoutDoubleSlipEditor(layoutEditor);
    }
        
    // final private LayoutDoubleSlip slip;

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutDoubleSlipView.class);
}
