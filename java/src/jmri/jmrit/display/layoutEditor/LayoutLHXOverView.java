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

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * MVC View component for the LayoutLHXOver class.
 *
 * @author Bob Jacobsen  Copyright (c) 2020
 * 
 */
@API(status = MAINTAINED)
public class LayoutLHXOverView extends LayoutXOverView {

    /**
     * Constructor method.
     * @param xover the layout left hand crossover to view.
     */
    public LayoutLHXOverView(@Nonnull LayoutLHXOver xover) {
        super(xover);
        // this.xover = xover;
    }
        
    // final private LayoutLHXOver xover;

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutLHXOverView.class);
}
