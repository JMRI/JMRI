package jmri.jmrit.display.layoutEditor;

import javax.annotation.*;

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
     */
    public LayoutRHTurnoutView(@Nonnull LayoutRHTurnout turnout) {
        super(turnout);
        // this.turnout = turnout;
    }

    // final private LayoutRHTurnout turnout;

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutRHTurnoutView.class);
}
