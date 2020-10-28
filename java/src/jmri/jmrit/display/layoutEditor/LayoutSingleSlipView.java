package jmri.jmrit.display.layoutEditor;

import javax.annotation.*;

/**
 * MVC View component for the LayoutSingleSlip class.
 *
 * @author Bob Jacobsen  Copyright (c) 2020
 *
 */
public class LayoutSingleSlipView extends LayoutSlipView {

    /**
     * Constructor method.
     * @param slip the slip to create view for.
     */
    public LayoutSingleSlipView(@Nonnull LayoutSingleSlip slip) {
        super(slip);
    }

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutSingleSlipView.class);
}
