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
 * A specialization of {@link LayoutTurnout}
 * corresponding to a wye turnout on the layout. 
 * <p>
 * A LayoutTurnout has three or four connection points, designated A, B, C, and
 * D. For right-handed or left-handed turnouts, A corresponds to the throat. At
 * the crossing, A-B (and C-D for crossovers) is a straight segment (continuing
 * route). A-C (and B-D for crossovers) is the diverging route. B-C (and A-D for
 * crossovers) is an illegal condition.
 * <br>
 * <pre>
 *    Wye           Three-way
 *
 *       B   
 *      //  
 * A ==**   
 *      \\    
 *       C  
 *
 * </pre>
 * <p>
 * A LayoutWye carries Block information. For
 * wye turnouts, the entire turnout is in one block, however, a block border may
 * occur at any connection (A,B,C,D). 
 * <p>
 * When LayoutWyes are first created, a rotation (degrees) is provided. For
 * 0.0 rotation, the turnout lies on the east-west line with A facing east.
 * Rotations are performed in a clockwise direction.
 * <p>
 * When LayoutWyes are first created, there are no connections. Block
 * information and connections may be added when available.
 * <p>
 * When a LayoutWyes is first created, it is enabled for control of an
 * assigned actual turnout. Clicking on the turnout center point will toggle the
 * turnout. This can be disabled via the popup menu.
 * <p>
 * Signal Head names are saved here to keep track of where signals are.
 * LayoutTurnout only serves as a storage place for signal head names. The names
 * are placed here by tools, e.g., Set Signals at Turnout, and Set Signals at
 * Double Crossover. Each connection point can have up to three SignalHeads and one SignalMast.
 * <p>
 * A LayoutWye may be linked to another LayoutTurnout to form a turnout
 * pair. 
 *<br>
 * Throat-To-Throat Turnouts - Two turnouts connected closely at their
 * throats, so closely that signals are not appropriate at the their throats.
 * This is the situation when two RH, LH, or WYE turnouts are used to model a
 * double slip. 
 *<br>
 * 3-Way Turnout - Two turnouts modeling a 3-way turnout, where the
 * throat of the second turnout is closely connected to the continuing track of
 * the first turnout. The throat will have three heads, or one head. A link is
 * required to be able to correctly interpret the use of signal heads.
 *
 * @author Dave Duchamp Copyright (c) 2004-2007
 * @author George Warner Copyright (c) 2017-2019
 */
public class LayoutWye extends LayoutTurnout {

    public LayoutWye(@Nonnull String id,
            @Nonnull Point2D c, double rot,
            double xFactor, double yFactor,
            @Nonnull LayoutEditor layoutEditor) {
        this(id, c, rot, xFactor, yFactor, layoutEditor, 1);
    }

    /**
     * Main constructor method.
     * @param id wye id string.
     * @param c 2D point position.
     * @param rot rotation.
     * @param xFactor horizontal factor.
     * @param yFactor vertical factor.
     * @param layoutEditor main layout editor.
     * @param v unused.
     */
    public LayoutWye(@Nonnull String id, @Nonnull Point2D c, double rot,
            double xFactor, double yFactor, @Nonnull LayoutEditor layoutEditor, int v) {
        super(id, TurnoutType.WYE_TURNOUT, c, rot, xFactor, yFactor, layoutEditor, 1);

        editor = new jmri.jmrit.display.layoutEditor.LayoutEditorDialogs.LayoutWyeEditor(layoutEditor);
    }
    
    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutWye.class);
}
