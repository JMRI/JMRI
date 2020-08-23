package jmri.jmrit.display.layoutEditor;

import static java.lang.Float.POSITIVE_INFINITY;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.text.MessageFormat;
import java.util.*;
import javax.annotation.*;
import javax.swing.*;
import jmri.*;
import jmri.jmrit.display.layoutEditor.blockRoutingTable.LayoutBlockRouteTableAction;
import jmri.jmrit.signalling.SignallingGuiTools;
import jmri.util.MathUtil;
import org.slf4j.*;

/**
 * A LayoutXOver corresponds to a crossover (connection between parallel tracks) on the layout. 
 * <p>
 * Three types are supported: double crossover,
 * right-handed single crossover, and left-handed single crossover. 
 * <p>
 * A LayoutXOver has four connection points, designated A, B, C, and
 * D. The A-B and C-D routes are a straight segment (continuing
 * route). A-C and B-D are the diverging routes. B-C and A-D illegal conditions.
 * <br>
 * <pre>
 *           Crossovers
 * Right-hand            left-hand
 * A ==**===== B      A ====**== B
 *      \\                 //
 *       \\               //
 *  D ====**== C     D ==**===== C
 *
 *             Double
 *        A ==**==**== B
 *             \\//
 *              XX
 *             //\\
 *        D ==**==**== C
 * </pre>
 * <p>
 * A LayoutXOver carries Block information. A block border may
 * occur at any connection (A,B,C,D). For a double crossover turnout, up to four
 * blocks may be assigned, one for each connection point, but if only one block
 * is assigned, that block applies to the entire turnout.
 * <p>
 * For drawing purposes, each LayoutXOver carries a center point and
 * displacements for B and C. 
 * For double crossovers, the center point is at the center of the turnout, and
 * the displacement for A = - the displacement for C and the displacement for D
 * = - the displacement for B. The center point and these displacements may be
 * adjusted by the user when in edit mode. For double crossovers, AB and BC are
 * constrained to remain perpendicular. For single crossovers, AB and CD are
 * constrained to remain parallel, and AC and BD are constrained to remain
 * parallel.
 * <p>
 * When LayoutXOvers are first created, a rotation (degrees) is provided. For
 * 0.0 rotation, the turnout lies on the east-west line with A facing east.
 * Rotations are performed in a clockwise direction.
 * <p>
 * When LayoutXOvers are first created, there are no connections. Block
 * information and connections may be added when available.
 * <p>
 * When a LayoutXOvers is first created, it is enabled for control of an
 * assigned actual turnout. Clicking on the turnout center point will toggle the
 * turnout. This can be disabled via the popup menu.
 * <p>
 * Signal Head names are saved here to keep track of where signals are.
 * LayoutTurnout only serves as a storage place for signal head names. The names
 * are placed here by tools, e.g., Set Signals at Turnout, and Set Signals at
 * Double Crossover. Each connection point can have up to three SignalHeads and one SignalMast.
 *
 * @author Dave Duchamp Copyright (c) 2004-2007
 * @author George Warner Copyright (c) 2017-2019
 */
abstract public class LayoutXOver extends LayoutTurnout {

    public LayoutXOver(@Nonnull String id, TurnoutType t, @Nonnull Point2D c, double rot,
            double xFactor, double yFactor, @Nonnull LayoutEditor layoutEditor, int v) {
        super(id, t, c, rot, xFactor, yFactor, layoutEditor, 1);
        
        editor = new jmri.jmrit.display.layoutEditor.LayoutEditorDialogs.LayoutXOverEditor(layoutEditor);
    }
    
    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutXOver.class);
}
