package jmri.jmrit.display.layoutEditor;

import javax.annotation.*;

/**
 * MVC View component for the LayoutRHXOver class.
 *
 * @author Bob Jacobsen  Copyright (c) 2020
 *
 */
public class LayoutRHXOverView extends LayoutXOverView {

    /**
     * Constructor method.
     * @param xover the layout right hand crossover to view.
     */
    public LayoutRHXOverView(@Nonnull LayoutRHXOver xover) {
        super(xover);
        // this.xover = xover;
    }

    // final private LayoutRHXOver xover;

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutRHXOverView.class);
}
