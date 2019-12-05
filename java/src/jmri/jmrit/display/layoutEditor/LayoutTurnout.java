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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.AbstractAction;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import jmri.InstanceManager;
import jmri.NamedBeanHandle;
import jmri.Path;
import jmri.Sensor;
import jmri.SignalHead;
import jmri.SignalMast;
import jmri.Turnout;
import jmri.jmrit.display.layoutEditor.blockRoutingTable.LayoutBlockRouteTableAction;
import jmri.jmrit.signalling.SignallingGuiTools;
import jmri.util.MathUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A LayoutTurnout corresponds to a turnout on the layout. A LayoutTurnout is an
 * extension of the standard Turnout object with drawing and connectivity
 * information added.
 * <p>
 * Six types are supported: right-hand, left-hand, wye, double crossover,
 * right-handed single crossover, and left-handed single crossover. Note that
 * double-slip turnouts can be handled as two turnouts, throat to throat, and
 * three-way turnouts can be handles as two turnouts, left-hand and right-hand,
 * arranged throat to continuing route.
 * <p>
 * A LayoutTurnout has three or four connection points, designated A, B, C, and
 * D. For right-handed or left-handed turnouts, A corresponds to the throat. At
 * the crossing, A-B (and C-D for crossovers) is a straight segment (continuing
 * route). A-C (and B-D for crossovers) is the diverging route. B-C (and A-D for
 * crossovers) is an illegal condition.
 * <p>
 * {@literal
 *           Turnouts
 * Right-hand       Left-hand
 *
 *                        C
 *                       //
 * A ==**== B       A ==**== B
 *      \\
 *       C
 *
 *    Wye           Three-way
 *
 *       B                D
 *      //               //
 * A ==**           A ==**== B
 *      \\               \\
 *       C                C
 *
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
 * literal}
 * <p>
 * A LayoutTurnout carries Block information. For right-handed, left-handed, and
 * wye turnouts, the entire turnout is in one block, however, a block border may
 * occur at any connection (A,B,C,D). For a double crossover turnout, up to four
 * blocks may be assigned, one for each connection point, but if only one block
 * is assigned, that block applies to the entire turnout.
 * <p>
 * For drawing purposes, each LayoutTurnout carries a center point and
 * displacements for B and C. For right-handed or left-handed turnouts, the
 * displacement for A = - the displacement for B, and the center point is at the
 * junction of the diverging route and the straight through continuing route.
 * For double crossovers, the center point is at the center of the turnout, and
 * the displacement for A = - the displacement for C and the displacement for D
 * = - the displacement for B. The center point and these displacements may be
 * adjusted by the user when in edit mode. For double crossovers, AB and BC are
 * constrained to remain perpendicular. For single crossovers, AB and CD are
 * constrained to remain parallel, and AC and BD are constrained to remain
 * parallel.
 * <p>
 * When LayoutTurnouts are first created, a rotation (degrees) is provided. For
 * 0.0 rotation, the turnout lies on the east-west line with A facing east.
 * Rotations are performed in a clockwise direction.
 * <p>
 * When LayoutTurnouts are first created, there are no connections. Block
 * information and connections may be added when available.
 * <p>
 * When a LayoutTurnout is first created, it is enabled for control of an
 * assigned actual turnout. Clicking on the turnout center point will toggle the
 * turnout. This can be disabled via the popup menu.
 * <p>
 * Signal Head names are saved here to keep track of where signals are.
 * LayoutTurnout only serves as a storage place for signal head names. The names
 * are placed here by tools, e.g., Set Signals at Turnout, and Set Signals at
 * Double Crossover.
 * <p>
 * A LayoutTurnout may be linked to another LayoutTurnout to form a turnout
 * pair. Throat-To-Throat Turnouts - Two turnouts connected closely at their
 * throats, so closely that signals are not appropriate at the their throats.
 * This is the situation when two RH, LH, or WYE turnouts are used to model a
 * double slip. 3-Way Turnout - Two turnouts modeling a 3-way turnout, where the
 * throat of the second turnout is closely connected to the continuing track of
 * the first turnout. The throat will have three heads, or one head. A link is
 * required to be able to correctly interpret the use of signal heads.
 *
 * @author Dave Duchamp Copyright (c) 2004-2007
 * @author George Warner Copyright (c) 2017-2019
 */
public class LayoutTurnout extends LayoutTrack {

    // defined constants - turnout types
    public static final int NONE = 0;
    public static final int RH_TURNOUT = 1;
    public static final int LH_TURNOUT = 2;
    public static final int WYE_TURNOUT = 3;
    public static final int DOUBLE_XOVER = 4;
    public static final int RH_XOVER = 5;
    public static final int LH_XOVER = 6;
    public static final int SINGLE_SLIP = 7;    // used for LayoutSlip which extends this class
    public static final int DOUBLE_SLIP = 8;    // used for LayoutSlip which extends this class

    // defined constants - link types
    public static final int NO_LINK = 0;
    public static final int FIRST_3_WAY = 1;       // this turnout is the first turnout of a 3-way
    // turnout pair (closest to the throat)
    public static final int SECOND_3_WAY = 2;      // this turnout is the second turnout of a 3-way
    // turnout pair (furthest from the throat)
    public static final int THROAT_TO_THROAT = 3;  // this turnout is one of two throat-to-throat
    // turnouts - no signals at throat

    // operational instance variables (not saved between sessions)
    public static final int UNKNOWN = Turnout.UNKNOWN;
    public static final int INCONSISTENT = Turnout.INCONSISTENT;
    public static final int STATE_AC = 0x02;
    public static final int STATE_BD = 0x04;
    public static final int STATE_AD = 0x06;
    public static final int STATE_BC = 0x08;

    // program default turnout size parameters
    public static final double turnoutBXDefault = 20.0;  // RH, LH, WYE
    public static final double turnoutCXDefault = 20.0;
    public static final double turnoutWidDefault = 10.0;
    public static final double xOverLongDefault = 30.0;   // DOUBLE_XOVER, RH_XOVER, LH_XOVER
    public static final double xOverHWidDefault = 10.0;
    public static final double xOverShortDefault = 10.0;

    // operational instance variables (not saved between sessions)
    protected NamedBeanHandle<Turnout> namedTurnout = null;
    //Second turnout is used to either throw a second turnout in a cross over or if one turnout address is used to throw two physical ones
    protected NamedBeanHandle<Turnout> secondNamedTurnout = null;

    private java.beans.PropertyChangeListener mTurnoutListener = null;

    // persistent instances variables (saved between sessions)
    // these should be the system or user name of an existing physical turnout
    private String turnoutName = "";
    private String secondTurnoutName = "";
    private boolean secondTurnoutInverted = false;

    // default is package protected
    protected NamedBeanHandle<LayoutBlock> namedLayoutBlockA = null;
    protected NamedBeanHandle<LayoutBlock> namedLayoutBlockB = null;  // Xover - second block, if there is one
    protected NamedBeanHandle<LayoutBlock> namedLayoutBlockC = null;  // Xover - third block, if there is one
    protected NamedBeanHandle<LayoutBlock> namedLayoutBlockD = null;  // Xover - forth block, if there is one

    protected NamedBeanHandle<SignalHead> signalA1HeadNamed = null; // signal 1 (continuing) (throat for RH, LH, WYE)
    protected NamedBeanHandle<SignalHead> signalA2HeadNamed = null; // signal 2 (diverging) (throat for RH, LH, WYE)
    protected NamedBeanHandle<SignalHead> signalA3HeadNamed = null; // signal 3 (second diverging) (3-way turnouts only)
    protected NamedBeanHandle<SignalHead> signalB1HeadNamed = null; // continuing (RH, LH, WYE) signal 1 (double crossover)
    protected NamedBeanHandle<SignalHead> signalB2HeadNamed = null; // LH_Xover and double crossover only
    protected NamedBeanHandle<SignalHead> signalC1HeadNamed = null; // diverging (RH, LH, WYE) signal 1 (double crossover)
    protected NamedBeanHandle<SignalHead> signalC2HeadNamed = null; // RH_Xover and double crossover only
    protected NamedBeanHandle<SignalHead> signalD1HeadNamed = null; // single or double crossover only
    protected NamedBeanHandle<SignalHead> signalD2HeadNamed = null; // LH_Xover and double crossover only

    public static final int POINTA1 = 0x01;
    public static final int POINTA2 = 0x03;
    public static final int POINTA3 = 0x05;
    public static final int POINTB1 = 0x10;
    public static final int POINTB2 = 0x12;
    public static final int POINTC1 = 0x20;
    public static final int POINTC2 = 0x22;
    public static final int POINTD1 = 0x30;
    public static final int POINTD2 = 0x32;

    protected NamedBeanHandle<SignalMast> signalAMastNamed = null; // Throat
    protected NamedBeanHandle<SignalMast> signalBMastNamed = null; // Continuing
    protected NamedBeanHandle<SignalMast> signalCMastNamed = null; // diverging
    protected NamedBeanHandle<SignalMast> signalDMastNamed = null; // single or double crossover only

    protected NamedBeanHandle<Sensor> sensorANamed = null; // Throat
    protected NamedBeanHandle<Sensor> sensorBNamed = null; // Continuing
    protected NamedBeanHandle<Sensor> sensorCNamed = null; // diverging
    protected NamedBeanHandle<Sensor> sensorDNamed = null; // single or double crossover only

    public int type = RH_TURNOUT;

    public LayoutTrack connectA = null;      // throat of LH, RH, RH Xover, LH Xover, and WYE turnouts
    public LayoutTrack connectB = null;      // straight leg of LH and RH turnouts
    public LayoutTrack connectC = null;
    public LayoutTrack connectD = null;      // double xover, RH Xover, LH Xover only

    public int continuingSense = Turnout.CLOSED;

    public boolean disabled = false;
    public boolean disableWhenOccupied = false;

    public Point2D dispB = new Point2D.Double(20.0, 0.0);
    public Point2D dispA = new Point2D.Double(20.0, 10.0);
    public Point2D pointA = new Point2D.Double(0, 0);
    public Point2D pointB = new Point2D.Double(40, 0);
    public Point2D pointC = new Point2D.Double(60, 20);
    public Point2D pointD = new Point2D.Double(20, 20);

    private int version = 1;

    public String linkedTurnoutName = ""; // name of the linked Turnout (as entered in tool)
    public int linkType = NO_LINK;

    private boolean useBlockSpeed = false;

    protected LayoutTurnout(@Nonnull String id,
            @Nonnull Point2D c, @Nonnull LayoutEditor layoutEditor) {
        super(id, c, layoutEditor);
    }

    public LayoutTurnout(@Nonnull String id, int t,
            @Nonnull Point2D c, double rot,
            double xFactor, double yFactor,
            @Nonnull LayoutEditor layoutEditor) {
        this(id, t, c, rot, xFactor, yFactor, layoutEditor, 1);
    }

    /**
     * constructor method
     */
    public LayoutTurnout(@Nonnull String id, int t, @Nonnull Point2D c, double rot,
            double xFactor, double yFactor, @Nonnull LayoutEditor layoutEditor, int v) {
        super(id, c, layoutEditor);

        namedTurnout = null;
        turnoutName = "";
        mTurnoutListener = null;
        disabled = false;
        disableWhenOccupied = false;
        type = t;
        version = v;

        // adjust initial coordinates
        if (type == LH_TURNOUT) {
            dispB.setLocation(layoutEditor.getTurnoutBX(), 0.0);
            dispA.setLocation(layoutEditor.getTurnoutCX(), -layoutEditor.getTurnoutWid());
        } else if (type == RH_TURNOUT) {
            dispB.setLocation(layoutEditor.getTurnoutBX(), 0.0);
            dispA.setLocation(layoutEditor.getTurnoutCX(), layoutEditor.getTurnoutWid());
        } else if (type == WYE_TURNOUT) {
            dispB.setLocation(layoutEditor.getTurnoutBX(), 0.5 * layoutEditor.getTurnoutWid());
            dispA.setLocation(layoutEditor.getTurnoutBX(), -0.5 * layoutEditor.getTurnoutWid());
        } else if (type == DOUBLE_XOVER) {
            if (version == 2) {
                center = new Point2D.Double(layoutEditor.getXOverLong(), layoutEditor.getXOverHWid());
                pointB.setLocation(layoutEditor.getXOverLong() * 2, 0);
                pointC.setLocation(layoutEditor.getXOverLong() * 2, (layoutEditor.getXOverHWid() * 2));
                pointD.setLocation(0, (layoutEditor.getXOverHWid() * 2));
                setCoordsCenter(c);
            } else {
                dispB.setLocation(layoutEditor.getXOverLong(), -layoutEditor.getXOverHWid());
                dispA.setLocation(layoutEditor.getXOverLong(), layoutEditor.getXOverHWid());
            }
        } else if (type == RH_XOVER) {
            if (version == 2) {
                center = new Point2D.Double(layoutEditor.getXOverLong(), layoutEditor.getXOverHWid());
                pointB.setLocation((layoutEditor.getXOverShort() + layoutEditor.getXOverLong()), 0);
                pointC.setLocation(layoutEditor.getXOverLong() * 2, (layoutEditor.getXOverHWid() * 2));
                pointD.setLocation((center.getX() - layoutEditor.getXOverShort()), (layoutEditor.getXOverHWid() * 2));
                setCoordsCenter(c);
            } else {
                dispB.setLocation(layoutEditor.getXOverShort(), -layoutEditor.getXOverHWid());
                dispA.setLocation(layoutEditor.getXOverLong(), layoutEditor.getXOverHWid());
            }
        } else if (type == LH_XOVER) {
            if (version == 2) {
                center = new Point2D.Double(layoutEditor.getXOverLong(), layoutEditor.getXOverHWid());

                pointA.setLocation((center.getX() - layoutEditor.getXOverShort()), 0);
                pointB.setLocation((layoutEditor.getXOverLong() * 2), 0);
                pointC.setLocation(layoutEditor.getXOverLong() + layoutEditor.getXOverShort(), (layoutEditor.getXOverHWid() * 2));
                pointD.setLocation(0, (layoutEditor.getXOverHWid() * 2));

                setCoordsCenter(c);
            } else {
                dispB.setLocation(layoutEditor.getXOverLong(), -layoutEditor.getXOverHWid());
                dispA.setLocation(layoutEditor.getXOverShort(), layoutEditor.getXOverHWid());
            }
        }
        rotateCoords(rot);
        // adjust size of new turnout
        Point2D pt = new Point2D.Double(Math.round(dispB.getX() * xFactor),
                Math.round(dispB.getY() * yFactor));
        dispB = pt;
        pt = new Point2D.Double(Math.round(dispA.getX() * xFactor),
                Math.round(dispA.getY() * yFactor));
        dispA = pt;
    }

    // this should only be used for debugging...
    @Override
    public String toString() {
        return "LayoutTurnout " + getId();
    }

    protected void rotateCoords(double rotDEG) {
        // rotate coordinates
        double rotRAD = Math.toRadians(rotDEG);
        double sineRot = Math.sin(rotRAD);
        double cosineRot = Math.cos(rotRAD);

        // rotate displacements around origin {0, 0}
        Point2D center_temp = center;
        center = MathUtil.zeroPoint2D;
        dispA = rotatePoint(dispA, sineRot, cosineRot);
        dispB = rotatePoint(dispB, sineRot, cosineRot);
        center = center_temp;

        pointA = rotatePoint(pointA, sineRot, cosineRot);
        pointB = rotatePoint(pointB, sineRot, cosineRot);
        pointC = rotatePoint(pointC, sineRot, cosineRot);
        pointD = rotatePoint(pointD, sineRot, cosineRot);
    }

    /**
     * Accessor methods
     */
    public int getVersion() {
        return version;
    }

    public void setVersion(int v) {
        version = v;
    }

    public boolean useBlockSpeed() {
        return useBlockSpeed;
    }

    public String getTurnoutName() {
        if (namedTurnout != null) {
            turnoutName = namedTurnout.getName();
        }
        return turnoutName;
    }

    public String getSecondTurnoutName() {
        if (secondNamedTurnout != null) {
            secondTurnoutName = secondNamedTurnout.getName();
        }
        return secondTurnoutName;
    }

    public boolean isSecondTurnoutInverted() {
        return secondTurnoutInverted;
    }

    @Nonnull
    public String getBlockName() {
        String result = null;
        if (namedLayoutBlockA != null) {
            result = namedLayoutBlockA.getName();
        }
        return ((result == null) ? "" : result);
    }

    @Nonnull
    public String getBlockBName() {
        String result = getBlockName();
        if (namedLayoutBlockB != null) {
            result = namedLayoutBlockB.getName();
        }
        return result;
    }

    @Nonnull
    public String getBlockCName() {
        String result = getBlockName();
        if (namedLayoutBlockC != null) {
            result = namedLayoutBlockC.getName();
        }
        return result;
    }

    @Nonnull
    public String getBlockDName() {
        String result = getBlockName();
        if (namedLayoutBlockD != null) {
            result = namedLayoutBlockD.getName();
        }
        return result;
    }

    public SignalHead getSignalHead(int loc) {
        NamedBeanHandle<SignalHead> signalHead = null;
        switch (loc) {
            case POINTA1:
                signalHead = signalA1HeadNamed;
                break;
            case POINTA2:
                signalHead = signalA2HeadNamed;
                break;
            case POINTA3:
                signalHead = signalA3HeadNamed;
                break;
            case POINTB1:
                signalHead = signalB1HeadNamed;
                break;
            case POINTB2:
                signalHead = signalB2HeadNamed;
                break;
            case POINTC1:
                signalHead = signalC1HeadNamed;
                break;
            case POINTC2:
                signalHead = signalC2HeadNamed;
                break;
            case POINTD1:
                signalHead = signalD1HeadNamed;
                break;
            case POINTD2:
                signalHead = signalD2HeadNamed;
                break;
            default:
                log.warn("Unhandled point type: {}", loc);
                break;
        }
        if (signalHead != null) {
            return signalHead.getBean();
        }
        return null;
    }

    public SignalHead getSignalA1() {
        return signalA1HeadNamed != null ? signalA1HeadNamed.getBean() : null;
    }

    public String getSignalA1Name() {
        if (signalA1HeadNamed != null) {
            return signalA1HeadNamed.getName();
        }
        return "";
    }

    public void setSignalA1Name(@CheckForNull String signalHead) {
        if (signalHead == null || signalHead.isEmpty()) {
            signalA1HeadNamed = null;
            return;
        }

        SignalHead head = InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(signalHead);
        if (head != null) {
            signalA1HeadNamed = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(signalHead, head);
        } else {
            signalA1HeadNamed = null;
            log.error("Signal Head {} Not found for turnout {}", signalHead, getTurnoutName());
        }
    }

    public SignalHead getSignalA2() {
        return signalA2HeadNamed != null ? signalA2HeadNamed.getBean() : null;
    }

    public String getSignalA2Name() {
        if (signalA2HeadNamed != null) {
            return signalA2HeadNamed.getName();
        }
        return "";
    }

    public void setSignalA2Name(@CheckForNull String signalHead) {
        if (signalHead == null || signalHead.isEmpty()) {
            signalA2HeadNamed = null;
            return;
        }

        SignalHead head = InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(signalHead);
        if (head != null) {
            signalA2HeadNamed = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(signalHead, head);
        } else {
            signalA2HeadNamed = null;
            log.error("Signal Head {} Not found for turnout {}", signalHead, getTurnoutName());
        }
    }

    public SignalHead getSignalA3() {
        return signalA3HeadNamed != null ? signalA3HeadNamed.getBean() : null;
    }

    public String getSignalA3Name() {
        if (signalA3HeadNamed != null) {
            return signalA3HeadNamed.getName();
        }
        return "";
    }

    public void setSignalA3Name(@CheckForNull String signalHead) {
        if (signalHead == null || signalHead.isEmpty()) {
            signalA3HeadNamed = null;
            return;
        }

        SignalHead head = InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(signalHead);
        if (head != null) {
            signalA3HeadNamed = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(signalHead, head);
        } else {
            signalA3HeadNamed = null;
            log.error("Signal Head {} Not found for turnout {}", signalHead, getTurnoutName());
        }
    }

    public SignalHead getSignalB1() {
        return signalB1HeadNamed != null ? signalB1HeadNamed.getBean() : null;
    }

    public String getSignalB1Name() {
        if (signalB1HeadNamed != null) {
            return signalB1HeadNamed.getName();
        }
        return "";
    }

    public void setSignalB1Name(@CheckForNull String signalHead) {
        if (signalHead == null || signalHead.isEmpty()) {
            signalB1HeadNamed = null;
            return;
        }

        SignalHead head = InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(signalHead);
        if (head != null) {
            signalB1HeadNamed = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(signalHead, head);
        } else {
            signalB1HeadNamed = null;
            log.error("Signal Head {} Not found for turnout {}", signalHead, getTurnoutName());
        }
    }

    public SignalHead getSignalB2() {
        return signalB2HeadNamed != null ? signalB2HeadNamed.getBean() : null;
    }

    public String getSignalB2Name() {
        if (signalB2HeadNamed != null) {
            return signalB2HeadNamed.getName();
        }
        return "";
    }

    public void setSignalB2Name(@CheckForNull String signalHead) {
        if (signalHead == null || signalHead.isEmpty()) {
            signalB2HeadNamed = null;
            return;
        }

        SignalHead head = InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(signalHead);
        if (head != null) {
            signalB2HeadNamed = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(signalHead, head);
        } else {
            signalB2HeadNamed = null;
            log.error("Signal Head {} Not found for turnout {}", signalHead, getTurnoutName());
        }
    }

    public SignalHead getSignalC1() {
        return signalC1HeadNamed != null ? signalC1HeadNamed.getBean() : null;
    }

    public String getSignalC1Name() {
        if (signalC1HeadNamed != null) {
            return signalC1HeadNamed.getName();
        }
        return "";
    }

    public void setSignalC1Name(@CheckForNull String signalHead) {
        if (signalHead == null || signalHead.isEmpty()) {
            signalC1HeadNamed = null;
            return;
        }

        SignalHead head = InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(signalHead);
        if (head != null) {
            signalC1HeadNamed = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(signalHead, head);
        } else {
            signalC1HeadNamed = null;
            log.error("Signal Head {} Not found for turnout {}", signalHead, getTurnoutName());
        }
    }

    public SignalHead getSignalC2() {
        return signalC2HeadNamed != null ? signalC2HeadNamed.getBean() : null;
    }

    public String getSignalC2Name() {
        if (signalC2HeadNamed != null) {
            return signalC2HeadNamed.getName();
        }
        return "";
    }

    public void setSignalC2Name(@CheckForNull String signalHead) {
        if (signalHead == null || signalHead.isEmpty()) {
            signalC2HeadNamed = null;
            return;
        }

        SignalHead head = InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(signalHead);
        if (head != null) {
            signalC2HeadNamed = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(signalHead, head);
        } else {
            signalC2HeadNamed = null;
            log.error("Signal Head {} Not found for turnout {}", signalHead, getTurnoutName());
        }
    }

    public SignalHead getSignalD1() {
        return signalD1HeadNamed != null ? signalD1HeadNamed.getBean() : null;
    }

    public String getSignalD1Name() {
        if (signalD1HeadNamed != null) {
            return signalD1HeadNamed.getName();
        }
        return "";
    }

    public void setSignalD1Name(@CheckForNull String signalHead) {
        if (signalHead == null || signalHead.isEmpty()) {
            signalD1HeadNamed = null;
            return;
        }

        SignalHead head = InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(signalHead);
        if (head != null) {
            signalD1HeadNamed = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(signalHead, head);
        } else {
            signalD1HeadNamed = null;
            log.error("Signal Head {} Not found for turnout {}", signalHead, getTurnoutName());
        }
    }

    public SignalHead getSignalD2() {
        return signalD2HeadNamed != null ? signalD2HeadNamed.getBean() : null;
    }

    public String getSignalD2Name() {
        if (signalD2HeadNamed != null) {
            return signalD2HeadNamed.getName();
        }
        return "";
    }

    public void setSignalD2Name(@CheckForNull String signalHead) {
        if (signalHead == null || signalHead.isEmpty()) {
            signalD2HeadNamed = null;
            return;
        }

        SignalHead head = InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(signalHead);
        if (head != null) {
            signalD2HeadNamed = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(signalHead, head);
        } else {
            signalD2HeadNamed = null;
            log.error("Signal Head {} Not found for turnout {}", signalHead, getTurnoutName());
        }
    }

    public void removeBeanReference(jmri.NamedBean nb) {
        if (nb == null) {
            return;
        }
        if (nb instanceof SignalMast) {
            if (nb.equals(getSignalAMast())) {
                setSignalAMast(null);
                return;
            }
            if (nb.equals(getSignalBMast())) {
                setSignalBMast(null);
                return;
            }
            if (nb.equals(getSignalCMast())) {
                setSignalCMast(null);
                return;
            }
            if (nb.equals(getSignalDMast())) {
                setSignalDMast(null);
            }
        } else if (nb instanceof Sensor) {
            if (nb.equals(getSensorA())) {
                setSensorA(null);
                return;
            }
            if (nb.equals(getSensorB())) {
                setSensorB(null);
                return;
            }
            if (nb.equals(getSensorC())) {
                setSensorC(null);
                return;
            }
            if (nb.equals(getSensorB())) {
                setSensorD(null);
            }
        } else if (nb instanceof SignalHead) {
            if (nb.equals(getSignalHead(POINTA1))) {
                setSignalA1Name(null);
            }
            if (nb.equals(getSignalHead(POINTA2))) {
                setSignalA2Name(null);
            }
            if (nb.equals(getSignalHead(POINTA3))) {
                setSignalA3Name(null);
            }
            if (nb.equals(getSignalHead(POINTB1))) {
                setSignalB1Name(null);
            }
            if (nb.equals(getSignalHead(POINTB2))) {
                setSignalB2Name(null);
            }
            if (nb.equals(getSignalHead(POINTC1))) {
                setSignalC1Name(null);
            }
            if (nb.equals(getSignalHead(POINTC2))) {
                setSignalC2Name(null);
            }
            if (nb.equals(getSignalHead(POINTD1))) {
                setSignalD1Name(null);
            }
            if (nb.equals(getSignalHead(POINTD2))) {
                setSignalD2Name(null);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canRemove() {
        ArrayList<String> beanReferences = getBeanReferences("All");  // NOI18N
        if (!beanReferences.isEmpty()) {
            displayRemoveWarningDialog(beanReferences, "BeanNameTurnout");  // NOI18N
        }
        return beanReferences.isEmpty();
    }

    /**
     * Build a list of sensors, signal heads, and signal masts attached to a
     * turnout point.
     *
     * @param pointName Specify the point (A-D) or all (All) points.
     * @return a list of bean reference names.
     */
    public ArrayList<String> getBeanReferences(String pointName) {
        ArrayList<String> references = new ArrayList<>();
        if (pointName.equals("A") || pointName.equals("All")) {  // NOI18N
            if (!getSignalAMastName().isEmpty()) {
                references.add(getSignalAMastName());
            }
            if (!getSensorAName().isEmpty()) {
                references.add(getSensorAName());
            }
            if (!getSignalA1Name().isEmpty()) {
                references.add(getSignalA1Name());
            }
            if (!getSignalA2Name().isEmpty()) {
                references.add(getSignalA2Name());
            }
            if (!getSignalA3Name().isEmpty()) {
                references.add(getSignalA3Name());
            }
        }
        if (pointName.equals("B") || pointName.equals("All")) {  // NOI18N
            if (!getSignalBMastName().isEmpty()) {
                references.add(getSignalBMastName());
            }
            if (!getSensorBName().isEmpty()) {
                references.add(getSensorBName());
            }
            if (!getSignalB1Name().isEmpty()) {
                references.add(getSignalB1Name());
            }
            if (!getSignalB2Name().isEmpty()) {
                references.add(getSignalB2Name());
            }
        }
        if (pointName.equals("C") || pointName.equals("All")) {  // NOI18N
            if (!getSignalCMastName().isEmpty()) {
                references.add(getSignalCMastName());
            }
            if (!getSensorCName().isEmpty()) {
                references.add(getSensorCName());
            }
            if (!getSignalC1Name().isEmpty()) {
                references.add(getSignalC1Name());
            }
            if (!getSignalC2Name().isEmpty()) {
                references.add(getSignalC2Name());
            }
        }
        if (pointName.equals("D") || pointName.equals("All")) {  // NOI18N
            if (!getSignalDMastName().isEmpty()) {
                references.add(getSignalDMastName());
            }
            if (!getSensorDName().isEmpty()) {
                references.add(getSensorDName());
            }
            if (!getSignalD1Name().isEmpty()) {
                references.add(getSignalD1Name());
            }
            if (!getSignalD2Name().isEmpty()) {
                references.add(getSignalD2Name());
            }
        }
        return references;
    }

    @Nonnull
    public String getSignalAMastName() {
        if (signalAMastNamed != null) {
            return signalAMastNamed.getName();
        }
        return "";
    }

    public SignalMast getSignalAMast() {
        if (signalAMastNamed != null) {
            return signalAMastNamed.getBean();
        }
        return null;
    }

    public void setSignalAMast(@CheckForNull String signalMast) {
        if (signalMast == null || signalMast.isEmpty()) {
            signalAMastNamed = null;
            return;
        }

        SignalMast mast = InstanceManager.getDefault(jmri.SignalMastManager.class).getSignalMast(signalMast);
        if (mast != null) {
            signalAMastNamed = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(signalMast, mast);
        } else {
            signalAMastNamed = null;
            log.error("Signal Mast {} Not found for turnout {}", signalMast, getTurnoutName());
        }
    }

    public String getSignalBMastName() {
        if (signalBMastNamed != null) {
            return signalBMastNamed.getName();
        }
        return "";
    }

    public SignalMast getSignalBMast() {
        if (signalBMastNamed != null) {
            return signalBMastNamed.getBean();
        }
        return null;
    }

    public void setSignalBMast(@CheckForNull String signalMast) {
        if (signalMast == null || signalMast.isEmpty()) {
            signalBMastNamed = null;
            return;
        }

        SignalMast mast = InstanceManager.getDefault(jmri.SignalMastManager.class).getSignalMast(signalMast);
        if (mast != null) {
            signalBMastNamed = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(signalMast, mast);
        } else {
            signalBMastNamed = null;
            log.error("Signal Mast {} Not found for turnout {}", signalMast, getTurnoutName());
        }
    }

    public String getSignalCMastName() {
        if (signalCMastNamed != null) {
            return signalCMastNamed.getName();
        }
        return "";
    }

    public SignalMast getSignalCMast() {
        if (signalCMastNamed != null) {
            return signalCMastNamed.getBean();
        }
        return null;
    }

    public void setSignalCMast(@CheckForNull String signalMast) {
        if (signalMast == null || signalMast.isEmpty()) {
            signalCMastNamed = null;
            return;
        }

        SignalMast mast = InstanceManager.getDefault(jmri.SignalMastManager.class).getSignalMast(signalMast);
        if (mast != null) {
            signalCMastNamed = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(signalMast, mast);
        } else {
            log.error("Signal Mast {} Not found for turnout {}", signalMast, getTurnoutName());
            signalCMastNamed = null;
        }
    }

    public String getSignalDMastName() {
        if (signalDMastNamed != null) {
            return signalDMastNamed.getName();
        }
        return "";
    }

    public SignalMast getSignalDMast() {
        if (signalDMastNamed != null) {
            return signalDMastNamed.getBean();
        }
        return null;
    }

    public void setSignalDMast(@CheckForNull String signalMast) {
        if (signalMast == null || signalMast.isEmpty()) {
            signalDMastNamed = null;
            return;
        }

        SignalMast mast = InstanceManager.getDefault(jmri.SignalMastManager.class).getSignalMast(signalMast);
        if (mast != null) {
            signalDMastNamed = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(signalMast, mast);
        } else {
            log.error("Signal Mast {} Not found for turnout {}", signalMast, getTurnoutName());
            signalDMastNamed = null;
        }
    }

    public String getSensorAName() {
        if (sensorANamed != null) {
            return sensorANamed.getName();
        }
        return "";
    }

    public Sensor getSensorA() {
        if (sensorANamed != null) {
            return sensorANamed.getBean();
        }
        return null;
    }

    public void setSensorA(@CheckForNull String sensorName) {
        if (sensorName == null || sensorName.isEmpty()) {
            sensorANamed = null;
            return;
        }

        try {
            Sensor sensor = InstanceManager.sensorManagerInstance().provideSensor(sensorName);
            sensorANamed = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(sensorName, sensor);
        } catch (IllegalArgumentException ex) {
            sensorANamed = null;
        }
    }

    public String getSensorBName() {
        if (sensorBNamed != null) {
            return sensorBNamed.getName();
        }
        return "";
    }

    public Sensor getSensorB() {
        if (sensorBNamed != null) {
            return sensorBNamed.getBean();
        }
        return null;
    }

    public void setSensorB(@CheckForNull String sensorName) {
        if (sensorName == null || sensorName.isEmpty()) {
            sensorBNamed = null;
            return;
        }

        try {
            Sensor sensor = InstanceManager.sensorManagerInstance().provideSensor(sensorName);
            sensorBNamed = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(sensorName, sensor);
        } catch (IllegalArgumentException ex) {
            sensorBNamed = null;
        }
    }

    public String getSensorCName() {
        if (sensorCNamed != null) {
            return sensorCNamed.getName();
        }
        return "";
    }

    public Sensor getSensorC() {
        if (sensorCNamed != null) {
            return sensorCNamed.getBean();
        }
        return null;
    }

    public void setSensorC(@CheckForNull String sensorName) {
        if (sensorName == null || sensorName.isEmpty()) {
            sensorCNamed = null;
            return;
        }

        try {
            Sensor sensor = InstanceManager.sensorManagerInstance().provideSensor(sensorName);
            sensorCNamed = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(sensorName, sensor);
        } catch (IllegalArgumentException ex) {
            sensorCNamed = null;
        }
    }

    public String getSensorDName() {
        if (sensorDNamed != null) {
            return sensorDNamed.getName();
        }
        return "";
    }

    public Sensor getSensorD() {
        if (sensorDNamed != null) {
            return sensorDNamed.getBean();
        }
        return null;
    }

    public void setSensorD(@CheckForNull String sensorName) {
        if (sensorName == null || sensorName.isEmpty()) {
            sensorDNamed = null;
            return;
        }

        try {
            Sensor sensor = InstanceManager.sensorManagerInstance().provideSensor(sensorName);
            sensorDNamed = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(sensorName, sensor);
        } catch (IllegalArgumentException ex) {
            sensorDNamed = null;
        }
    }

    public String getLinkedTurnoutName() {
        return linkedTurnoutName;
    }

    public void setLinkedTurnoutName(@CheckForNull String s) {
        linkedTurnoutName = s;
    }  //Could be done with changing over to a NamedBeanHandle

    public int getLinkType() {
        return linkType;
    }

    public void setLinkType(int type) {
        linkType = type;
    }

    public int getTurnoutType() {
        return type;
    }

    public boolean isTurnoutTypeXover() {
        return ((type == DOUBLE_XOVER) || (type == RH_XOVER) || (type == LH_XOVER));
    }

    public boolean isTurnoutTypeSlip() {
        return ((type == SINGLE_SLIP) || (type == DOUBLE_SLIP));
    }

    public LayoutTrack getConnectA() {
        return connectA;
    }

    public LayoutTrack getConnectB() {
        return connectB;
    }

    public LayoutTrack getConnectC() {
        return connectC;
    }

    public LayoutTrack getConnectD() {
        return connectD;
    }

    public Turnout getTurnout() {
        if (namedTurnout == null) {
            // set physical turnout if possible and needed
            setTurnout(turnoutName);
            if (namedTurnout == null) {
                return null;
            }
        }
        return namedTurnout.getBean();
    }

    public int getContinuingSense() {
        return continuingSense;
    }

    /**
     *
     * @return true is the continuingSense matches the known state
     */
    public boolean isInContinuingSenseState() {
        return getState() == continuingSense;
    }

    public void setTurnout(@CheckForNull String tName) {
        if (namedTurnout != null) {
            deactivateTurnout();
        }
        turnoutName = tName;
        Turnout turnout = null;
        if (turnoutName != null && !turnoutName.isEmpty()) {
            turnout = InstanceManager.turnoutManagerInstance().getTurnout(turnoutName);
        }
        if (turnout != null) {
            namedTurnout = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(turnoutName, turnout);
            activateTurnout();
        } else {
            turnoutName = "";
            namedTurnout = null;
            setDisableWhenOccupied(false);
        }
    }

    public Turnout getSecondTurnout() {
        Turnout result = null;
        if (secondNamedTurnout == null) {
            // set physical turnout if possible and needed
            setSecondTurnout(secondTurnoutName);
        }
        if (secondNamedTurnout != null) {
            result = secondNamedTurnout.getBean();
        }
        return result;
    }

    public void setSecondTurnout(@CheckForNull String tName) {
        if (tName != null && tName.equals(secondTurnoutName)) {
            return;
        }

        if (secondNamedTurnout != null) {
            deactivateTurnout();
        }
        String oldSecondTurnoutName = secondTurnoutName;
        secondTurnoutName = tName;
        Turnout turnout = null;
        if (tName != null) {
            turnout = InstanceManager.turnoutManagerInstance().getTurnout(secondTurnoutName);
        }
        if (turnout != null) {
            secondNamedTurnout = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(secondTurnoutName, turnout);
        } else {
            secondTurnoutName = "";
            secondNamedTurnout = null;
        }
        activateTurnout(); // Even if secondary is null, the primary Turnout may still need to be re-activated
        if ((getTurnoutType() == RH_TURNOUT)
                || (getTurnoutType() == LH_TURNOUT)
                || (getTurnoutType() == WYE_TURNOUT)) {
            LayoutEditorFindItems lf = layoutEditor.getFinder();
            if (oldSecondTurnoutName != null && !oldSecondTurnoutName.isEmpty()) {
                Turnout oldTurnout = InstanceManager.turnoutManagerInstance().getTurnout(oldSecondTurnoutName);
                String oldSystemName = (oldTurnout == null) ? null : oldTurnout.getSystemName();
                LayoutTurnout oldLinked = (oldSystemName == null) ? null
                        : lf.findLayoutTurnoutByTurnoutName(oldSystemName);
                if (oldLinked == null) {
                    String oldUserName = (oldTurnout == null) ? null : oldTurnout.getUserName();
                    oldLinked = (oldUserName == null) ? null
                            : lf.findLayoutTurnoutByTurnoutName(oldUserName);
                }
                if ((oldLinked != null) && oldLinked.getSecondTurnout() == getTurnout()) {
                    oldLinked.setSecondTurnout(null);
                }
            }
            if (turnout != null) {
                LayoutTurnout newLinked = lf.findLayoutTurnoutByTurnoutName(turnout.getSystemName());
                if (newLinked == null) {
                    newLinked = lf.findLayoutTurnoutByTurnoutName(turnout.getUserName());
                }
                if (newLinked != null) {
                    newLinked.setSecondTurnout(turnoutName);
                }
            }
        }
    }

    public void setSecondTurnoutInverted(boolean inverted) {
        secondTurnoutInverted = inverted;
    }

    public void setContinuingSense(int sense) {
        continuingSense = sense;
    }

    public void setDisabled(boolean state) {
        if (disabled != state) {
            disabled = state;
            if (layoutEditor != null) {
                layoutEditor.redrawPanel();
            }
        }
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisableWhenOccupied(boolean state) {
        if (disableWhenOccupied != state) {
            disableWhenOccupied = state;
            if (layoutEditor != null) {
                layoutEditor.redrawPanel();
            }
        }
    }

    public boolean isDisabledWhenOccupied() {
        return disableWhenOccupied;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LayoutTrack getConnection(int connectionType) throws jmri.JmriException {
        LayoutTrack result = null;
        switch (connectionType) {
            case TURNOUT_A: {
                result = connectA;
                break;
            }
            case TURNOUT_B: {
                result = connectB;
                break;
            }
            case TURNOUT_C: {
                result = connectC;
                break;
            }
            case TURNOUT_D: {
                result = connectD;
                break;
            }
            default: {
                log.error("Invalid Connection Type {}", connectionType); //I18IN
                throw new jmri.JmriException("Invalid Connection Type");
            }
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setConnection(int connectionType, LayoutTrack o, int type) throws jmri.JmriException {
        if ((type != TRACK) && (type != NONE)) {
            log.error("unexpected type of connection to layoutturnout - {}", type);
            throw new jmri.JmriException("unexpected type of connection to layoutturnout - " + type);
        }
        switch (connectionType) {
            case TURNOUT_A:
                connectA = o;
                break;
            case TURNOUT_B:
                connectB = o;
                break;
            case TURNOUT_C:
                connectC = o;
                break;
            case TURNOUT_D:
                connectD = o;
                break;
            default:
                log.error("Invalid Connection Type {}", connectionType); //I18IN
                throw new jmri.JmriException("Invalid Connection Type " + connectionType);
        }
    }

    public void setConnectA(LayoutTrack o, int type) {
        connectA = o;
        if ((type != TRACK) && (type != NONE)) {
            log.error("unexpected type of A connection to layoutturnout - {}", type);
        }
    }

    public void setConnectB(LayoutTrack o, int type) {
        connectB = o;
        if ((type != TRACK) && (type != NONE)) {
            log.error("unexpected type of B connection to layoutturnout - {}", type);
        }
    }

    public void setConnectC(LayoutTrack o, int type) {
        connectC = o;
        if ((type != TRACK) && (type != NONE)) {
            log.error("unexpected type of C connection to layoutturnout - {}", type);
        }
    }

    public void setConnectD(LayoutTrack o, int type) {
        connectD = o;
        if ((type != TRACK) && (type != NONE)) {
            log.error("unexpected type of D connection to layoutturnout - {}", type);
        }
    }

    public LayoutBlock getLayoutBlock() {
        return (namedLayoutBlockA != null) ? namedLayoutBlockA.getBean() : null;
    }

    public LayoutBlock getLayoutBlockB() {
        return (namedLayoutBlockB != null) ? namedLayoutBlockB.getBean() : getLayoutBlock();
    }

    public LayoutBlock getLayoutBlockC() {
        return (namedLayoutBlockC != null) ? namedLayoutBlockC.getBean() : getLayoutBlock();
    }

    public LayoutBlock getLayoutBlockD() {
        return (namedLayoutBlockD != null) ? namedLayoutBlockD.getBean() : getLayoutBlock();
    }

    public Point2D getCoordsA() {
        if ((getTurnoutType() == DOUBLE_XOVER)
                || (getTurnoutType() == LH_XOVER)
                || (getTurnoutType() == RH_XOVER)) {
            if (version == 2) {
                return pointA;
            }
            return MathUtil.subtract(center, dispA);
        } else if (getTurnoutType() == WYE_TURNOUT) {
            return MathUtil.subtract(center, MathUtil.midPoint(dispB, dispA));
        } else {
            return MathUtil.subtract(center, dispB);
        }
    }

    public Point2D getCoordsB() {
        if ((version == 2) && ((getTurnoutType() == DOUBLE_XOVER)
                || (getTurnoutType() == LH_XOVER)
                || (getTurnoutType() == RH_XOVER))) {
            return pointB;
        }
        return MathUtil.add(center, dispB);
    }

    public Point2D getCoordsC() {
        if ((version == 2) && ((getTurnoutType() == DOUBLE_XOVER)
                || (getTurnoutType() == LH_XOVER)
                || (getTurnoutType() == RH_XOVER))) {
            return pointC;
        }
        return MathUtil.add(center, dispA);
    }

    public Point2D getCoordsD() {
        if ((version == 2) && ((getTurnoutType() == DOUBLE_XOVER)
                || (getTurnoutType() == LH_XOVER)
                || (getTurnoutType() == RH_XOVER))) {
            return pointD;
        }
        // only allowed for single and double crossovers
        return MathUtil.subtract(center, dispB);
    }

    /**
     * return the coordinates for a specified connection type
     *
     * @param connectionType the connection type
     * @return the coordinates for the specified connection type
     */
    @Override
    public Point2D getCoordsForConnectionType(int connectionType) {
        Point2D result = center;
        switch (connectionType) {
            case TURNOUT_CENTER:
                break;
            case TURNOUT_A:
                result = getCoordsA();
                break;
            case TURNOUT_B:
                result = getCoordsB();
                break;
            case TURNOUT_C:
                result = getCoordsC();
                break;
            case TURNOUT_D:
                result = getCoordsD();
                break;
            default:
                log.error("Invalid connection type {}", connectionType); // NOI18N
        }
        return result;
    }

    /**
     * @return the bounds of this turnout
     */
    @Override
    public Rectangle2D getBounds() {
        Rectangle2D result;

        Point2D pointA = getCoordsA();
        result = new Rectangle2D.Double(pointA.getX(), pointA.getY(), 0, 0);
        result.add(getCoordsB());
        result.add(getCoordsC());
        if (isTurnoutTypeXover() || isTurnoutTypeSlip()) {
            result.add(getCoordsD());
        }
        return result;
    }

    // updates connectivity for blocks assigned to this turnout and connected track segments
    protected void updateBlockInfo() {
        LayoutBlock bA = null;
        LayoutBlock bB = null;
        LayoutBlock bC = null;
        LayoutBlock bD = null;
        layoutEditor.getLEAuxTools().setBlockConnectivityChanged();
        if (getLayoutBlock() != null) {
            getLayoutBlock().updatePaths();
        }
        if (connectA != null) {
            bA = ((TrackSegment) connectA).getLayoutBlock();
            if ((bA != null) && (bA != getLayoutBlock())) {
                bA.updatePaths();
            }
        }
        if ((getLayoutBlockB() != null)
                && (getLayoutBlockB() != getLayoutBlock())
                && (getLayoutBlockB() != bA)) {
            getLayoutBlockB().updatePaths();
        }
        if (connectB != null) {
            bB = ((TrackSegment) connectB).getLayoutBlock();
            if ((bB != null) && (bB != getLayoutBlock()) && (bB != bA)
                    && (bB != getLayoutBlockB())) {
                bB.updatePaths();
            }
        }
        if ((getLayoutBlockC() != null)
                && (getLayoutBlockC() != getLayoutBlock())
                && (getLayoutBlockC() != bA)
                && (getLayoutBlockC() != bB)
                && (getLayoutBlockC() != getLayoutBlockB())) {
            getLayoutBlockC().updatePaths();
        }
        if (connectC != null) {
            bC = ((TrackSegment) connectC).getLayoutBlock();
            if ((bC != null) && (bC != getLayoutBlock())
                    && (bC != bA) && (bC != getLayoutBlockB())
                    && (bC != bB)
                    && (bC != getLayoutBlockC())) {
                bC.updatePaths();
            }
        }
        if ((getLayoutBlockD() != null)
                && (getLayoutBlockD() != getLayoutBlock())
                && (getLayoutBlockD() != bA)
                && (getLayoutBlockD() != bB)
                && (getLayoutBlockD() != getLayoutBlockB())
                && (getLayoutBlockD() != bC)
                && (getLayoutBlockD() != getLayoutBlockC())) {
            getLayoutBlockD().updatePaths();
        }
        if (connectD != null) {
            bD = ((TrackSegment) connectD).getLayoutBlock();
            if ((bD != null) && (bD != getLayoutBlock())
                    && (bD != bA) && (bD != getLayoutBlockB())
                    && (bD != bB) && (bD != getLayoutBlockC())
                    && (bD != bC) && (bD != getLayoutBlockD())) {
                bD.updatePaths();
            }
        }
    }

    /**
     * Set default size parameters to correspond to this turnout's size.
     * <p>
     * note: only protected so LayoutTurnoutTest can call it
     */
    protected void setUpDefaultSize() {
        // remove the overall scale factor
        double bX = dispB.getX() / layoutEditor.getXScale();
        double bY = dispB.getY() / layoutEditor.getYScale();
        double cX = dispA.getX() / layoutEditor.getXScale();
        double cY = dispA.getY() / layoutEditor.getYScale();
        // calculate default parameters according to type of turnout
        double lenB = Math.hypot(bX, bY);
        double lenC = Math.hypot(cX, cY);
        double distBC = Math.hypot(bX - cX, bY - cY);
        if ((getTurnoutType() == LH_TURNOUT)
                || (getTurnoutType() == RH_TURNOUT)) {
            layoutEditor.setTurnoutBX(Math.round(lenB + 0.1));
            double xc = ((bX * cX) + (bY * cY)) / lenB;
            layoutEditor.setTurnoutCX(Math.round(xc + 0.1));
            layoutEditor.setTurnoutWid(Math.round(Math.sqrt((lenC * lenC) - (xc * xc)) + 0.1));
        } else if (getTurnoutType() == WYE_TURNOUT) {
            double xx = Math.sqrt((lenB * lenB) - (0.25 * (distBC * distBC)));
            layoutEditor.setTurnoutBX(Math.round(xx + 0.1));
            layoutEditor.setTurnoutCX(Math.round(xx + 0.1));
            layoutEditor.setTurnoutWid(Math.round(distBC + 0.1));
        } else {
            if (version == 2) {
                double aX = pointA.getX() / layoutEditor.getXScale();
                double aY = pointA.getY() / layoutEditor.getYScale();
                bX = pointB.getX() / layoutEditor.getXScale();
                bY = pointB.getY() / layoutEditor.getYScale();
                cX = pointC.getX() / layoutEditor.getXScale();
                cY = pointC.getY() / layoutEditor.getYScale();
                double lenAB = Math.hypot(bX - aX, bY - aY);
                if (getTurnoutType() == DOUBLE_XOVER) {
                    double lenBC = Math.hypot(bX - cX, bY - cY);
                    layoutEditor.setXOverLong(Math.round(lenAB / 2)); //set to half to be backwardly compatible
                    layoutEditor.setXOverHWid(Math.round(lenBC / 2));
                    layoutEditor.setXOverShort(Math.round((0.5 * lenAB) / 2));
                } else if (getTurnoutType() == RH_XOVER) {
                    lenAB = lenAB / 3;
                    layoutEditor.setXOverShort(Math.round(lenAB));
                    layoutEditor.setXOverLong(Math.round(lenAB * 2));
                    double opp = (aY - bY);
                    double ang = Math.asin(opp / (lenAB * 3));
                    opp = Math.sin(ang) * lenAB;
                    bY = bY + opp;
                    double adj = Math.cos(ang) * lenAB;
                    bX = bX + adj;
                    double lenBC = Math.hypot(bX - cX, bY - cY);
                    layoutEditor.setXOverHWid(Math.round(lenBC / 2));
                } else if (getTurnoutType() == LH_XOVER) {
                    double dY = pointD.getY() / layoutEditor.getYScale();
                    lenAB = lenAB / 3;
                    layoutEditor.setXOverShort(Math.round(lenAB));
                    layoutEditor.setXOverLong(Math.round(lenAB * 2));
                    double opp = (dY - cY);
                    double ang = Math.asin(opp / (lenAB * 3)); //Lenght of AB should be the same as CD
                    opp = Math.sin(ang) * lenAB;
                    cY = cY + opp;
                    double adj = Math.cos(ang) * lenAB;
                    cX = cX + adj;
                    double lenBC = Math.hypot(bX - cX, bY - cY);
                    layoutEditor.setXOverHWid(Math.round(lenBC / 2));
                }
            } else if (getTurnoutType() == DOUBLE_XOVER) {
                double lng = Math.sqrt((lenB * lenB) - (0.25 * (distBC * distBC)));
                layoutEditor.setXOverLong(Math.round(lng + 0.1));
                layoutEditor.setXOverHWid(Math.round((0.5 * distBC) + 0.1));
                layoutEditor.setXOverShort(Math.round((0.5 * lng) + 0.1));
            } else if (getTurnoutType() == RH_XOVER) {
                double distDC = Math.hypot(bX + cX, bY + cY);
                layoutEditor.setXOverShort(Math.round((0.25 * distDC) + 0.1));
                layoutEditor.setXOverLong(Math.round((0.75 * distDC) + 0.1));
                double hwid = Math.sqrt((lenC * lenC) - (0.5625 * distDC * distDC));
                layoutEditor.setXOverHWid(Math.round(hwid + 0.1));
            } else if (getTurnoutType() == LH_XOVER) {
                double distDC = Math.hypot(bX + cX, bY + cY);
                layoutEditor.setXOverShort(Math.round((0.25 * distDC) + 0.1));
                layoutEditor.setXOverLong(Math.round((0.75 * distDC) + 0.1));
                double hwid = Math.sqrt((lenC * lenC) - (0.0625 * distDC * distDC));
                layoutEditor.setXOverHWid(Math.round(hwid + 0.1));
            }
        }
    }

    /**
     * Set up Layout Block(s) for this Turnout.
     */
    public void setLayoutBlock(LayoutBlock newLayoutBlock) {
        LayoutBlock blockA = getLayoutBlock();
        LayoutBlock blockB = getLayoutBlockB();
        LayoutBlock blockC = getLayoutBlockC();
        LayoutBlock blockD = getLayoutBlockD();
        if (blockA != newLayoutBlock) {
            // block has changed, if old block exists, decrement use
            if ((blockA != null)
                    && (blockA != blockB)
                    && (blockA != blockC)
                    && (blockA != blockD)) {
                blockA.decrementUse();
            }

            blockA = newLayoutBlock;
            if (newLayoutBlock != null) {
                String userName = newLayoutBlock.getUserName();
                if (userName != null) {
                    namedLayoutBlockA = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(userName, newLayoutBlock);
                }
            } else {
                namedLayoutBlockA = null;
                setDisableWhenOccupied(false);
            }
            // decrement use if block was already counted
            if ((blockA != null)
                    && ((blockA == blockB) || (blockA == blockC) || (blockA == blockD))) {
                blockA.decrementUse();
            }
            setTrackSegmentBlocks();
        }
    }

    @SuppressFBWarnings(value = "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE", justification = "Null is accepted as a valid value")
    public void setLayoutBlockB(LayoutBlock newLayoutBlock) {
        if (getLayoutBlock() == null) {
            setLayoutBlock(newLayoutBlock);
        }
        if (isTurnoutTypeXover() || isTurnoutTypeSlip()) {
            LayoutBlock blockA = getLayoutBlock();
            LayoutBlock blockB = getLayoutBlockB();
            LayoutBlock blockC = getLayoutBlockC();
            LayoutBlock blockD = getLayoutBlockD();
            if (blockB != newLayoutBlock) {
                // block has changed, if old block exists, decrement use
                if ((blockB != null)
                        && (blockB != blockA)
                        && (blockB != blockC)
                        && (blockB != blockD)) {
                    blockB.decrementUse();
                }
                blockB = newLayoutBlock;
                if (newLayoutBlock != null) {
                    namedLayoutBlockB = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(newLayoutBlock.getUserName(), newLayoutBlock);
                } else {
                    namedLayoutBlockB = null;
                }
                // decrement use if block was already counted
                if ((blockB != null)
                        && ((blockB == blockA) || (blockB == blockC) || (blockB == blockD))) {
                    blockB.decrementUse();
                }
                setTrackSegmentBlocks();
            }
        } else {
            log.error("Attempt to set block B, but not a crossover");
        }
    }

    @SuppressFBWarnings(value = "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE", justification = "Null is accepted as a valid value")
    public void setLayoutBlockC(LayoutBlock newLayoutBlock) {
        if (getLayoutBlock() == null) {
            setLayoutBlock(newLayoutBlock);
        }
        if (isTurnoutTypeXover() || isTurnoutTypeSlip()) {
            LayoutBlock blockA = getLayoutBlock();
            LayoutBlock blockB = getLayoutBlockB();
            LayoutBlock blockC = getLayoutBlockC();
            LayoutBlock blockD = getLayoutBlockD();
            if (blockC != newLayoutBlock) {
                // block has changed, if old block exists, decrement use
                if ((blockC != null)
                        && (blockC != blockA)
                        && (blockC != blockB)
                        && (blockC != blockD)) {
                    blockC.decrementUse();
                }
                blockC = newLayoutBlock;
                if (newLayoutBlock != null) {
                    namedLayoutBlockC = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(newLayoutBlock.getUserName(), newLayoutBlock);
                } else {
                    namedLayoutBlockC = null;
                }
                // decrement use if block was already counted
                if ((blockC != null)
                        && ((blockC == blockA) || (blockC == blockB) || (blockC == blockD))) {
                    blockC.decrementUse();
                }
                setTrackSegmentBlocks();
            }
        } else {
            log.error("Attempt to set block C, but not a crossover");
        }
    }

    @SuppressFBWarnings(value = "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE", justification = "Null is accepted as a valid value")
    public void setLayoutBlockD(LayoutBlock newLayoutBlock) {
        if (getLayoutBlock() == null) {
            setLayoutBlock(newLayoutBlock);
        }
        if (isTurnoutTypeXover() || isTurnoutTypeSlip()) {
            LayoutBlock blockA = getLayoutBlock();
            LayoutBlock blockB = getLayoutBlockB();
            LayoutBlock blockC = getLayoutBlockC();
            LayoutBlock blockD = getLayoutBlockD();
            if (blockD != newLayoutBlock) {
                // block has changed, if old block exists, decrement use
                if ((blockD != null)
                        && (blockD != blockA)
                        && (blockD != blockB)
                        && (blockD != blockC)) {
                    blockD.decrementUse();
                }
                blockD = newLayoutBlock;
                if (newLayoutBlock != null) {
                    namedLayoutBlockD = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(newLayoutBlock.getUserName(), newLayoutBlock);
                } else {
                    namedLayoutBlockD = null;
                }
                // decrement use if block was already counted
                if ((blockD != null)
                        && ((blockD == blockA) || (blockD == blockB) || (blockD == blockC))) {
                    blockD.decrementUse();
                }
                setTrackSegmentBlocks();
            }
        } else {
            log.error("Attempt to set block D, but not a crossover");
        }
    }

    public void setLayoutBlockByName(@Nonnull String name) {
        setLayoutBlock(layoutEditor.provideLayoutBlock(name));
    }

    public void setLayoutBlockBByName(@Nonnull String name) {
        if (isTurnoutTypeXover() || isTurnoutTypeSlip()) {
            setLayoutBlockB(layoutEditor.provideLayoutBlock(name));
        } else {
            log.error("Attempt to set block B name ('{}') on Layout Turnout {}, but not a crossover or slip", name, getName());
        }
    }

    public void setLayoutBlockCByName(@Nonnull String name) {
        if (isTurnoutTypeXover() || isTurnoutTypeSlip()) {
            setLayoutBlockC(layoutEditor.provideLayoutBlock(name));
        } else {
            log.error("Attempt to set block C name ('{}') on Layout Turnout {}, but not a crossover or slip", name, getName());
        }
    }

    public void setLayoutBlockDByName(@Nonnull String name) {
        if (isTurnoutTypeXover() || isTurnoutTypeSlip()) {
            setLayoutBlockD(layoutEditor.provideLayoutBlock(name));
        } else {
            log.error("Attempt to set block D name ('{}') on Layout Turnout {}, but not a crossover or slip", name, getName());
        }
    }

    /**
     * Check each connection point and update the block value for very short
     * track segments.
     *
     * @since 4.11.6
     */
    void setTrackSegmentBlocks() {
        setTrackSegmentBlock(TURNOUT_A, false);
        setTrackSegmentBlock(TURNOUT_B, false);
        setTrackSegmentBlock(TURNOUT_C, false);
        if (getTurnoutType() > WYE_TURNOUT) {
            setTrackSegmentBlock(TURNOUT_D, false);
        }
    }

    /**
     * Update the block for a track segment that provides a short connection
     * between a turnout and another object, normally another turnout. These are
     * hard to see and are frequently missed.
     * <p>
     * Skip block changes if signal heads, masts or sensors have been assigned.
     * Only track segments with a length less than the turnout circle radius
     * will be changed.
     *
     * @since 4.11.6
     * @param pointType   The point type which indicates which turnout
     *                    connection.
     * @param isAutomatic True for the automatically generated track segment
     *                    created by the drag-n-drop process. False for existing
     *                    connections which require a track segment length
     *                    calculation.
     */
    void setTrackSegmentBlock(int pointType, boolean isAutomatic) {
        TrackSegment trkSeg;
        Point2D pointCoord;
        LayoutBlock blockA = getLayoutBlock();
        LayoutBlock blockB = getLayoutBlock();
        LayoutBlock blockC = getLayoutBlock();
        LayoutBlock blockD = getLayoutBlock();
        LayoutBlock currBlk = blockA;

        switch (pointType) {
            case TURNOUT_A:
            case SLIP_A:
                if (signalA1HeadNamed != null) {
                    return;
                }
                if (signalA2HeadNamed != null) {
                    return;
                }
                if (signalA3HeadNamed != null) {
                    return;
                }
                if (getSignalAMast() != null) {
                    return;
                }
                if (getSensorA() != null) {
                    return;
                }
                trkSeg = (TrackSegment) connectA;
                pointCoord = getCoordsA();
                break;
            case TURNOUT_B:
            case SLIP_B:
                if (signalB1HeadNamed != null) {
                    return;
                }
                if (signalB2HeadNamed != null) {
                    return;
                }
                if (getSignalBMast() != null) {
                    return;
                }
                if (getSensorB() != null) {
                    return;
                }
                trkSeg = (TrackSegment) connectB;
                pointCoord = getCoordsB();
                if (isTurnoutTypeXover()) {
                    currBlk = blockB != null ? blockB : blockA;
                }
                break;
            case TURNOUT_C:
            case SLIP_C:
                if (signalC1HeadNamed != null) {
                    return;
                }
                if (signalC2HeadNamed != null) {
                    return;
                }
                if (getSignalCMast() != null) {
                    return;
                }
                if (getSensorC() != null) {
                    return;
                }
                trkSeg = (TrackSegment) connectC;
                pointCoord = getCoordsC();
                if (isTurnoutTypeXover()) {
                    currBlk = blockC != null ? blockC : blockA;
                }
                break;
            case TURNOUT_D:
            case SLIP_D:
                if (signalD1HeadNamed != null) {
                    return;
                }
                if (signalD2HeadNamed != null) {
                    return;
                }
                if (getSignalDMast() != null) {
                    return;
                }
                if (getSensorD() != null) {
                    return;
                }
                trkSeg = (TrackSegment) connectD;
                pointCoord = getCoordsD();
                if (isTurnoutTypeXover()) {
                    currBlk = blockD != null ? blockD : blockA;
                }
                break;
            default:
                log.error("setTrackSegmentBlock: Invalid pointType: {}", pointType);
                return;
        }
        if (trkSeg != null) {
            double chkSize = LayoutEditor.SIZE * layoutEditor.getTurnoutCircleSize();
            double segLength = 0;
            if (!isAutomatic) {
                Point2D segCenter = trkSeg.getCoordsCenter();
                segLength = MathUtil.distance(pointCoord, segCenter) * 2;
            }
            if (segLength < chkSize) {
                if (log.isDebugEnabled()) {
                    log.debug("Set block:");
                    log.debug("    seg: {}", trkSeg);
                    log.debug("    cor: {}", pointCoord);
                    log.debug("    blk: {}", (currBlk == null) ? "null" : currBlk.getDisplayName());
                    log.debug("    len: {}", segLength);
                }

                trkSeg.setLayoutBlock(currBlk);
                layoutEditor.getLEAuxTools().setBlockConnectivityChanged();
            }
        }
    }

    /**
     * Test if turnout legs are mainline track or not.
     *
     * @return true if connecting track segment is mainline; Defaults to not
     *         mainline if connecting track segment is missing
     */
    public boolean isMainlineA() {
        if (connectA != null) {
            return ((TrackSegment) connectA).isMainline();
        } else {
            // if no connection, depends on type of turnout
            if (isTurnoutTypeXover()) {
                // All crossovers - straight continuing is B
                if (connectB != null) {
                    return ((TrackSegment) connectB).isMainline();
                }
            } else if (isTurnoutTypeSlip()) {
                if (connectD != null) {
                    return ((TrackSegment) connectD).isMainline();
                }
            } // must be RH, LH, or WYE turnout - A is the switch throat
            else if (((connectB != null)
                    && (((TrackSegment) connectB).isMainline()))
                    || ((connectC != null)
                    && (((TrackSegment) connectC).isMainline()))) {
                return true;
            }
        }
        return false;
    }

    public boolean isMainlineB() {
        if (connectB != null) {
            return ((TrackSegment) connectB).isMainline();
        } else {
            // if no connection, depends on type of turnout
            if (isTurnoutTypeXover()) {
                // All crossovers - straight continuing is A
                if (connectA != null) {
                    return ((TrackSegment) connectA).isMainline();
                }
            } else if (getTurnoutType() == DOUBLE_SLIP) {
                if (connectD != null) {
                    return ((TrackSegment) connectD).isMainline();
                }
            } // must be RH, LH, or WYE turnout - A is the switch throat,
            //      B is normally the continuing straight
            else if (continuingSense == Turnout.CLOSED) {
                // user hasn't changed the continuing turnout state
                if (connectA != null) { // if throat is mainline, this leg must be also
                    return ((TrackSegment) connectA).isMainline();
                }
            }
        }
        return false;
    }

    public boolean isMainlineC() {
        if (connectC != null) {
            return ((TrackSegment) connectC).isMainline();
        } else {
            // if no connection, depends on type of turnout
            if (isTurnoutTypeXover()) {
                // All crossovers - straight continuing is D
                if (connectD != null) {
                    return ((TrackSegment) connectD).isMainline();
                }
            } else if (getTurnoutType() == DOUBLE_SLIP) {
                if (connectB != null) {
                    return ((TrackSegment) connectB).isMainline();
                }
            } // must be RH, LH, or WYE turnout - A is the switch throat,
            //      B is normally the continuing straight
            else if (continuingSense == Turnout.THROWN) {
                // user has changed the continuing turnout state
                if (connectA != null) { // if throat is mainline, this leg must be also
                    return ((TrackSegment) connectA).isMainline();
                }
            }
        }
        return false;
    }

    public boolean isMainlineD() {
        // this is a crossover turnout
        if (connectD != null) {
            return ((TrackSegment) connectD).isMainline();
        } else if (isTurnoutTypeSlip()) {
            if (connectB != null) {
                return ((TrackSegment) connectB).isMainline();
            }
        } else if (connectC != null) {
            return ((TrackSegment) connectC).isMainline();
        }
        return false;
    }

    @Override
    public boolean isMainline() {
        return (isMainlineA() || isMainlineB() || isMainlineC() || isMainlineD());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int findHitPointType(@Nonnull Point2D hitPoint, boolean useRectangles, boolean requireUnconnected) {
        int result = NONE;  // assume point not on connection
        //note: optimization here: instead of creating rectangles for all the
        // points to check below, we create a rectangle for the test point
        // and test if the points below are in that rectangle instead.
        Rectangle2D r = layoutEditor.trackControlCircleRectAt(hitPoint);
        Point2D p, minPoint = MathUtil.zeroPoint2D;

        double circleRadius = LayoutEditor.SIZE * layoutEditor.getTurnoutCircleSize();
        double distance, minDistance = POSITIVE_INFINITY;

        // check center coordinates
        if (!requireUnconnected) {
            p = getCoordsCenter();
            distance = MathUtil.distance(p, hitPoint);
            if (distance < minDistance) {
                minDistance = distance;
                minPoint = p;
                result = TURNOUT_CENTER;
            }
        }

        //check the A connection point
        if (!requireUnconnected || (getConnectA() == null)) {
            p = getCoordsA();
            distance = MathUtil.distance(p, hitPoint);
            if (distance < minDistance) {
                minDistance = distance;
                minPoint = p;
                result = TURNOUT_A;
            }
        }

        //check the B connection point
        if (!requireUnconnected || (getConnectB() == null)) {
            p = getCoordsB();
            distance = MathUtil.distance(p, hitPoint);
            if (distance < minDistance) {
                minDistance = distance;
                minPoint = p;
                result = TURNOUT_B;
            }
        }

        //check the C connection point
        if (!requireUnconnected || (getConnectC() == null)) {
            p = getCoordsC();
            distance = MathUtil.distance(p, hitPoint);
            if (distance < minDistance) {
                minDistance = distance;
                minPoint = p;
                result = TURNOUT_C;
            }
        }

        //check the D connection point
        if (isTurnoutTypeXover()) {
            if (!requireUnconnected || (getConnectD() == null)) {
                p = getCoordsD();
                distance = MathUtil.distance(p, hitPoint);
                if (distance < minDistance) {
                    minDistance = distance;
                    minPoint = p;
                    result = TURNOUT_D;
                }
            }
        }
        if ((useRectangles && !r.contains(minPoint))
                || (!useRectangles && (minDistance > circleRadius))) {
            result = NONE;
        }
        return result;
    }   // findHitPointType

    /*
     * Modify coordinates methods
     */
    /**
     * Set center coordinates
     *
     * @param p the coordinates to set
     */
    @Override
    public void setCoordsCenter(@Nonnull Point2D p) {
        Point2D offset = MathUtil.subtract(p, center);
        pointA = MathUtil.add(pointA, offset);
        pointB = MathUtil.add(pointB, offset);
        pointC = MathUtil.add(pointC, offset);
        pointD = MathUtil.add(pointD, offset);
        center = p;
    }

    private void reCalculateCenter() {
        center = MathUtil.midPoint(pointA, pointC);
    }

    public void setCoordsA(@Nonnull Point2D p) {
        pointA = p;
        if (version == 2) {
            reCalculateCenter();
        }
        double x = center.getX() - p.getX();
        double y = center.getY() - p.getY();
        if (getTurnoutType() == DOUBLE_XOVER) {
            dispA = new Point2D.Double(x, y);
            // adjust to maintain rectangle
            double oldLength = MathUtil.length(dispB);
            double newLength = Math.hypot(x, y);
            dispB = MathUtil.multiply(dispB, newLength / oldLength);
        } else if ((getTurnoutType() == RH_XOVER)
                || (getTurnoutType() == LH_XOVER)) {
            dispA = new Point2D.Double(x, y);
            // adjust to maintain the parallelogram
            double a = 0.0;
            double b = -y;
            double xi = 0.0;
            double yi = b;
            if ((dispB.getX() + x) != 0.0) {
                a = (dispB.getY() + y) / (dispB.getX() + x);
                b = -y + (a * x);
                xi = -b / (a + (1.0 / a));
                yi = (a * xi) + b;
            }
            if (getTurnoutType() == RH_XOVER) {
                x = xi - (0.333333 * (-x - xi));
                y = yi - (0.333333 * (-y - yi));
            } else if (getTurnoutType() == LH_XOVER) {
                x = xi - (3.0 * (-x - xi));
                y = yi - (3.0 * (-y - yi));
            }
            dispB = new Point2D.Double(x, y);
        } else if (getTurnoutType() == WYE_TURNOUT) {
            // modify both to maintain same angle at wye
            double temX = (dispB.getX() + dispA.getX());
            double temY = (dispB.getY() + dispA.getY());
            double temXx = (dispB.getX() - dispA.getX());
            double temYy = (dispB.getY() - dispA.getY());
            double tan = Math.sqrt(((temX * temX) + (temY * temY))
                    / ((temXx * temXx) + (temYy * temYy)));
            double xx = x + (y / tan);
            double yy = y - (x / tan);
            dispA = new Point2D.Double(xx, yy);
            xx = x - (y / tan);
            yy = y + (x / tan);
            dispB = new Point2D.Double(xx, yy);
        } else {
            dispB = new Point2D.Double(x, y);
        }
    }

    public void setCoordsB(Point2D p) {
        pointB = p;
        double x = center.getX() - p.getX();
        double y = center.getY() - p.getY();
        dispB = new Point2D.Double(-x, -y);
        if ((getTurnoutType() == DOUBLE_XOVER)
                || (getTurnoutType() == WYE_TURNOUT)) {
            // adjust to maintain rectangle or wye shape
            double oldLength = MathUtil.length(dispA);
            double newLength = Math.hypot(x, y);
            dispA = MathUtil.multiply(dispA, newLength / oldLength);
        } else if ((getTurnoutType() == RH_XOVER)
                || (getTurnoutType() == LH_XOVER)) {
            // adjust to maintain the parallelogram
            double a = 0.0;
            double b = y;
            double xi = 0.0;
            double yi = b;
            if ((dispA.getX() - x) != 0.0) {
                if ((-dispA.getX() + x) == 0) {
                    /* we can in some situations eg 90' vertical end up with a 0 value,
                    so hence remove a small amount so that we
                    don't have a divide by zero issue */
                    x = x - 0.0000000001;
                }
                a = (dispA.getY() - y) / (dispA.getX() - x);
                b = y - (a * x);
                xi = -b / (a + (1.0 / a));
                yi = (a * xi) + b;
            }
            if (getTurnoutType() == LH_XOVER) {
                x = xi - (0.333333 * (x - xi));
                y = yi - (0.333333 * (y - yi));
            } else if (getTurnoutType() == RH_XOVER) {
                x = xi - (3.0 * (x - xi));
                y = yi - (3.0 * (y - yi));
            }
            dispA = new Point2D.Double(x, y);
        }
    }

    public void setCoordsC(Point2D p) {
        pointC = p;
        if (version == 2) {
            reCalculateCenter();
        }
        double x = center.getX() - p.getX();
        double y = center.getY() - p.getY();
        dispA = new Point2D.Double(-x, -y);
        if ((getTurnoutType() == DOUBLE_XOVER)
                || (getTurnoutType() == WYE_TURNOUT)) {
            // adjust to maintain rectangle or wye shape
            double oldLength = MathUtil.length(dispB);
            double newLength = Math.hypot(x, y);
            dispB = MathUtil.multiply(dispB, newLength / oldLength);
        } else if ((getTurnoutType() == RH_XOVER)
                || (getTurnoutType() == LH_XOVER)) {
            double a = 0.0;
            double b = -y;
            double xi = 0.0;
            double yi = b;
            if ((dispB.getX() + x) != 0.0) {
                if ((-dispB.getX() + x) == 0) {
                    /* we can in some situations eg 90' vertical end up with a 0 value,
                    so hence remove a small amount so that we
                    don't have a divide by zero issue */

                    x = x - 0.0000000001;
                }
                a = (-dispB.getY() + y) / (-dispB.getX() + x);
                b = -y + (a * x);
                xi = -b / (a + (1.0 / a));
                yi = (a * xi) + b;
            }
            if (getTurnoutType() == RH_XOVER) {
                x = xi - (0.333333 * (-x - xi));
                y = yi - (0.333333 * (-y - yi));
            } else if (getTurnoutType() == LH_XOVER) {
                x = xi - (3.0 * (-x - xi));
                y = yi - (3.0 * (-y - yi));
            }
            dispB = new Point2D.Double(-x, -y);
        }
    }

    public void setCoordsD(Point2D p) {
        pointD = p;

        // only used for crossovers
        double x = center.getX() - p.getX();
        double y = center.getY() - p.getY();
        dispB = new Point2D.Double(x, y);
        if (getTurnoutType() == DOUBLE_XOVER) {
            // adjust to maintain rectangle
            double oldLength = MathUtil.length(dispA);
            double newLength = Math.hypot(x, y);
            dispA = MathUtil.multiply(dispA, newLength / oldLength);
        } else if ((getTurnoutType() == RH_XOVER)
                || (getTurnoutType() == LH_XOVER)) {
            // adjust to maintain the parallelogram
            double a = 0.0;
            double b = y;
            double xi = 0.0;
            double yi = b;
            if ((dispA.getX() + x) != 0.0) {
                a = (dispA.getY() + y) / (dispA.getX() + x);
                b = -y + (a * x);
                xi = -b / (a + (1.0 / a));
                yi = (a * xi) + b;
            }
            if (getTurnoutType() == LH_XOVER) {
                x = xi - (0.333333 * (-x - xi));
                y = yi - (0.333333 * (-y - yi));
            } else if (getTurnoutType() == RH_XOVER) {
                x = xi - (3.0 * (-x - xi));
                y = yi - (3.0 * (-y - yi));
            }
            dispA = new Point2D.Double(x, y);
        }
    }

    /**
     * Scale this LayoutTrack's coordinates by the x and y factors
     *
     * @param xFactor the amount to scale X coordinates
     * @param yFactor the amount to scale Y coordinates
     */
    @Override
    public void scaleCoords(float xFactor, float yFactor) {
        Point2D factor = new Point2D.Double(xFactor, yFactor);
        center = MathUtil.granulize(MathUtil.multiply(center, factor), 1.0);

        dispA = MathUtil.granulize(MathUtil.multiply(dispA, factor), 1.0);
        dispB = MathUtil.granulize(MathUtil.multiply(dispB, factor), 1.0);

        pointA = MathUtil.granulize(MathUtil.multiply(pointA, factor), 1.0);
        pointB = MathUtil.granulize(MathUtil.multiply(pointB, factor), 1.0);
        pointC = MathUtil.granulize(MathUtil.multiply(pointC, factor), 1.0);
        pointD = MathUtil.granulize(MathUtil.multiply(pointD, factor), 1.0);
    }

    /**
     * Translate (2D move) this LayoutTrack's coordinates by the x and y factors
     *
     * @param xFactor the amount to translate X coordinates
     * @param yFactor the amount to translate Y coordinates
     */
    @Override
    public void translateCoords(float xFactor, float yFactor) {
        Point2D factor = new Point2D.Double(xFactor, yFactor);
        center = MathUtil.add(center, factor);
        pointA = MathUtil.add(pointA, factor);
        pointB = MathUtil.add(pointB, factor);
        pointC = MathUtil.add(pointC, factor);
        pointD = MathUtil.add(pointD, factor);
    }

    /**
     * Activate/Deactivate turnout to redraw when turnout state changes
     */
    private void activateTurnout() {
        deactivateTurnout();
        if (namedTurnout != null) {
            namedTurnout.getBean().addPropertyChangeListener(
                    mTurnoutListener = (java.beans.PropertyChangeEvent e) -> {
                        if (e.getNewValue() == null) {
                            return;
                        }
                        if (disableWhenOccupied && isOccupied()) {
                            return;
                        }
                        if (secondNamedTurnout != null) {
                            int t1state = namedTurnout.getBean().getCommandedState();
                            int t2state = secondNamedTurnout.getBean().getCommandedState();
                            if (e.getSource().equals(namedTurnout.getBean())
                            && e.getNewValue().equals(t1state)) {
                                if (secondTurnoutInverted) {
                                    t1state = Turnout.invertTurnoutState(t1state);
                                }
                                if (secondNamedTurnout.getBean().getCommandedState() != t1state) {
                                    secondNamedTurnout.getBean().setCommandedState(t1state);
                                }
                            } else if (e.getSource().equals(secondNamedTurnout.getBean())
                            && e.getNewValue().equals(t2state)) {
                                if (secondTurnoutInverted) {
                                    t2state = Turnout.invertTurnoutState(t2state);
                                }
                                if (namedTurnout.getBean().getCommandedState() != t2state) {
                                    namedTurnout.getBean().setCommandedState(t2state);
                                }
                            }
                        }
                        layoutEditor.redrawPanel();
                    },
                    namedTurnout.getName(),
                    "Layout Editor Turnout"
            );
        }
        if (secondNamedTurnout != null) {
            secondNamedTurnout.getBean().addPropertyChangeListener(mTurnoutListener, secondNamedTurnout.getName(), "Layout Editor Turnout");
        }
    }

    private void deactivateTurnout() {
        if (mTurnoutListener != null) {
            if (namedTurnout != null) {
                namedTurnout.getBean().removePropertyChangeListener(mTurnoutListener);
            }
            if (secondNamedTurnout != null) {
                secondNamedTurnout.getBean().removePropertyChangeListener(mTurnoutListener);
            }
            mTurnoutListener = null;
        }
    }

    /**
     * Toggle turnout if clicked on, physical turnout exists, and not disabled.
     */
    public void toggleTurnout() {
        if (getTurnout() != null) {
            // toggle turnout
            if (getTurnout().getCommandedState() == Turnout.CLOSED) {
                setState(Turnout.THROWN);
            } else {
                setState(Turnout.CLOSED);
            }
        } else {
            log.debug("Turnout Icon not associated with a Turnout");
        }
    }

    /**
     * Set the LayoutTurnout state Used for sending the toggle command Checks
     * not diabled, disable when occupied Also sets secondary Turnout commanded
     * state
     *
     * @param state New state to set, eg Turnout.CLOSED
     */
    public void setState(int state) {
        if ((getTurnout() != null) && !disabled) {
            if (disableWhenOccupied && isOccupied()) {
                log.debug("Turnout not changed as Block is Occupied");
            } else {
                getTurnout().setCommandedState(state);
                if (getSecondTurnout() != null) {
                    if (secondTurnoutInverted) {
                        if (state == Turnout.CLOSED) {
                            getSecondTurnout().setCommandedState(Turnout.THROWN);
                        } else {
                            getSecondTurnout().setCommandedState(Turnout.CLOSED);
                        }
                    } else {
                        getSecondTurnout().setCommandedState(state);
                    }
                }
            }
        }
    }

    /**
     * Get the LayoutTurnout state
     * <p>
     * Ensures the secondary Turnout state matches the primary
     *
     * @return the state, eg Turnout.CLOSED or Turnout.INCONSISTENT
     */
    public int getState() {
        int result = UNKNOWN;
        if (getTurnout() != null) {
            result = getTurnout().getKnownState();
        }
        if (getSecondTurnout() != null) {
            int t2state = getSecondTurnout().getKnownState();
            if (secondTurnoutInverted) {
                t2state = Turnout.invertTurnoutState(getSecondTurnout().getKnownState());
            }
            if (result != t2state) {
                return INCONSISTENT;
            }
        }
        return result;
    }

    /**
     * Is this turnout occupied?
     *
     * @return true if occupied
     */
    private boolean isOccupied() {
        if ((getTurnoutType() == RH_TURNOUT)
                || (getTurnoutType() == LH_TURNOUT)
                || (getTurnoutType() == WYE_TURNOUT)) {
            if (getLayoutBlock().getOccupancy() == LayoutBlock.OCCUPIED) {
                log.debug("Block {} is Occupied", getBlockName());
                return true;
            }
        }
        if (isTurnoutTypeXover()) {
            //If the turnout is set for straight over, we need to deal with the straight over connecting blocks
            if (getTurnout().getKnownState() == Turnout.CLOSED) {
                if ((getLayoutBlock().getOccupancy() == LayoutBlock.OCCUPIED)
                        && (getLayoutBlockB().getOccupancy() == LayoutBlock.OCCUPIED)) {
                    log.debug("Blocks {} & {} are Occupied", getBlockName(), getBlockBName());
                    return true;
                }
                if ((getLayoutBlockC().getOccupancy() == LayoutBlock.OCCUPIED)
                        && (getLayoutBlockD().getOccupancy() == LayoutBlock.OCCUPIED)) {
                    log.debug("Blocks {} & {} are Occupied", getBlockCName(), getBlockDName());
                    return true;
                }
            }

        }
        if ((getTurnoutType() == DOUBLE_XOVER)
                || (getTurnoutType() == LH_XOVER)) {
            if (getTurnout().getKnownState() == Turnout.THROWN) {
                if ((getLayoutBlockB().getOccupancy() == LayoutBlock.OCCUPIED)
                        && (getLayoutBlockD().getOccupancy() == LayoutBlock.OCCUPIED)) {
                    log.debug("Blocks {} & {} are Occupied", getBlockBName(), getBlockDName());
                    return true;
                }
            }
        }

        if ((getTurnoutType() == DOUBLE_XOVER)
                || (getTurnoutType() == RH_XOVER)) {
            if (getTurnout().getKnownState() == Turnout.THROWN) {
                if ((getLayoutBlock().getOccupancy() == LayoutBlock.OCCUPIED)
                        && (getLayoutBlockC().getOccupancy() == LayoutBlock.OCCUPIED)) {
                    log.debug("Blocks {} & {} are Occupied", getLayoutBlock(), getBlockCName());
                    return true;
                }
            }
        }
        return false;
    }

    // initialization instance variables (used when loading a LayoutEditor)
    public String connectAName = "";
    public String connectBName = "";
    public String connectCName = "";
    public String connectDName = "";

    public String tBlockAName = "";
    public String tBlockBName = "";
    public String tBlockCName = "";
    public String tBlockDName = "";

    /**
     * Initialization method. The above variables are initialized by
     * LayoutTurnoutXml, then the following method is called after the entire
     * LayoutEditor is loaded to set the specific TrackSegment objects.
     */
    @Override
    public void setObjects(LayoutEditor p) {
        connectA = p.getFinder().findTrackSegmentByName(connectAName);
        connectB = p.getFinder().findTrackSegmentByName(connectBName);
        connectC = p.getFinder().findTrackSegmentByName(connectCName);
        connectD = p.getFinder().findTrackSegmentByName(connectDName);

        LayoutBlock lb;
        if (!tBlockAName.isEmpty()) {
            lb = p.provideLayoutBlock(tBlockAName);
            if (lb != null) {
                String userName = lb.getUserName();
                if (userName != null) {
                    namedLayoutBlockA = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(userName, lb);
                    lb.incrementUse();
                }
            } else {
                log.error("bad blockname '{}' in layoutturnout {}", tBlockAName, getId());
                namedLayoutBlockA = null;
            }
            tBlockAName = null; //release this memory
        }

        if (!tBlockBName.isEmpty()) {
            lb = p.provideLayoutBlock(tBlockBName);
            if (lb != null) {
                String userName = lb.getUserName();
                if (userName != null) {
                    namedLayoutBlockB = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(userName, lb);
                }
                if (namedLayoutBlockB != namedLayoutBlockA) {
                    lb.incrementUse();
                }
            } else {
                log.error("bad blockname '{}' in layoutturnout {}", tBlockBName, getId());
                namedLayoutBlockB = null;
            }
            tBlockBName = null; //release this memory
        }

        if (!tBlockCName.isEmpty()) {
            lb = p.provideLayoutBlock(tBlockCName);
            if (lb != null) {
                String userName = lb.getUserName();
                if (userName != null) {
                    namedLayoutBlockC = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(userName, lb);
                }
                if ((namedLayoutBlockC != namedLayoutBlockA)
                        && (namedLayoutBlockC != namedLayoutBlockB)) {
                    lb.incrementUse();
                }
            } else {
                log.error("bad blockname '{}' in layoutturnout {}", tBlockCName, getId());
                namedLayoutBlockC = null;
            }
            tBlockCName = null; // release this memory
        }

        if (!tBlockDName.isEmpty()) {
            lb = p.provideLayoutBlock(tBlockDName);
            if (lb != null) {
                String userName = lb.getUserName();
                if (userName != null) {
                    namedLayoutBlockD = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(userName, lb);
                }
                if ((namedLayoutBlockD != namedLayoutBlockA)
                        && (namedLayoutBlockD != namedLayoutBlockB)
                        && (namedLayoutBlockD != namedLayoutBlockC)) {
                    lb.incrementUse();
                }
            } else {
                log.error("bad blockname '{}' in layoutturnout {}", tBlockDName, getId());
                namedLayoutBlockD = null;
            }
            tBlockDName = null; //release this memory
        }
        activateTurnout();
    } // setObjects

    private JPopupMenu popup = null;

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    protected JPopupMenu showPopup(@CheckForNull MouseEvent mouseEvent) {
        if (popup != null) {
            popup.removeAll();
        } else {
            popup = new JPopupMenu();
        }

        if (layoutEditor.isEditable()) {
            String label = "";
            switch (getTurnoutType()) {
                case RH_TURNOUT:
                    label = Bundle.getMessage("RightTurnout");
                    break;
                case LH_TURNOUT:
                    label = Bundle.getMessage("LeftTurnout");
                    break;
                case WYE_TURNOUT:
                    label = Bundle.getMessage("WYETurnout");
                    break;
                case DOUBLE_XOVER:
                    label = Bundle.getMessage("DoubleCrossover");
                    break;
                case RH_XOVER:
                    label = Bundle.getMessage("RightCrossover");
                    break;
                case LH_XOVER:
                    label = Bundle.getMessage("LeftCrossover");
                    break;
                default:
                    break;
            }
            JMenuItem jmi = popup.add(Bundle.getMessage("MakeLabel", label) + getId());
            jmi.setEnabled(false);

            if (getTurnout() == null) {
                jmi = popup.add(Bundle.getMessage("NoTurnout"));
            } else {
                String stateString = getTurnoutStateString(getTurnout().getKnownState());
                stateString = String.format(" (%s)", stateString);
                jmi = popup.add(Bundle.getMessage("BeanNameTurnout")
                        + ": " + getTurnoutName() + stateString);
            }
            jmi.setEnabled(false);

            if (getSecondTurnout() != null) {
                String stateString = getTurnoutStateString(getSecondTurnout().getKnownState());
                stateString = String.format(" (%s)", stateString);
                jmi = popup.add(Bundle.getMessage("Supporting",
                        Bundle.getMessage("BeanNameTurnout"))
                        + ": " + getSecondTurnoutName() + stateString);
            }
            jmi.setEnabled(false);

            if (getBlockName().isEmpty()) {
                jmi = popup.add(Bundle.getMessage("NoBlock"));
                jmi.setEnabled(false);
            } else {
                jmi = popup.add(Bundle.getMessage("MakeLabel", Bundle.getMessage("BeanNameBlock")) + getLayoutBlock().getDisplayName());
                jmi.setEnabled(false);
                if (isTurnoutTypeXover()) {
                    // check if extra blocks have been entered
                    if ((getLayoutBlockB() != null) && (getLayoutBlockB() != getLayoutBlock())) {
                        jmi = popup.add(Bundle.getMessage("MakeLabel", Bundle.getMessage("Block_ID", "B")) + getLayoutBlockB().getDisplayName());
                        jmi.setEnabled(false);
                    }
                    if ((getLayoutBlockC() != null) && (getLayoutBlockC() != getLayoutBlock())) {
                        jmi = popup.add(Bundle.getMessage("MakeLabel", Bundle.getMessage("Block_ID", "C")) + getLayoutBlockC().getDisplayName());
                        jmi.setEnabled(false);
                    }
                    if ((getLayoutBlockD() != null) && (getLayoutBlockD() != getLayoutBlock())) {
                        jmi = popup.add(Bundle.getMessage("MakeLabel", Bundle.getMessage("Block_ID", "D")) + getLayoutBlockD().getDisplayName());
                        jmi.setEnabled(false);
                    }
                }
            }

            // if there are any track connections
            if ((connectA != null) || (connectB != null)
                    || (connectC != null) || (connectD != null)) {
                JMenu connectionsMenu = new JMenu(Bundle.getMessage("Connections")); // there is no pane opening (which is what ... implies)
                if (connectA != null) {
                    connectionsMenu.add(new AbstractAction(Bundle.getMessage("MakeLabel", "A") + connectA.getName()) {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            LayoutEditorFindItems lf = layoutEditor.getFinder();
                            LayoutTrack lt = lf.findObjectByName(connectA.getName());
                            // this shouldn't ever be null... however...
                            if (lt != null) {
                                layoutEditor.setSelectionRect(lt.getBounds());
                                lt.showPopup();
                            }
                        }
                    });
                }
                if (connectB != null) {
                    connectionsMenu.add(new AbstractAction(Bundle.getMessage("MakeLabel", "B") + connectB.getName()) {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            LayoutEditorFindItems lf = layoutEditor.getFinder();
                            LayoutTrack lt = lf.findObjectByName(connectB.getName());
                            // this shouldn't ever be null... however...
                            if (lt != null) {
                                layoutEditor.setSelectionRect(lt.getBounds());
                                lt.showPopup();
                            }
                        }
                    });
                }
                if (connectC != null) {
                    connectionsMenu.add(new AbstractAction(Bundle.getMessage("MakeLabel", "C") + connectC.getName()) {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            LayoutEditorFindItems lf = layoutEditor.getFinder();
                            LayoutTrack lt = lf.findObjectByName(connectC.getName());
                            // this shouldn't ever be null... however...
                            if (lt != null) {
                                layoutEditor.setSelectionRect(lt.getBounds());
                                lt.showPopup();
                            }
                        }
                    });
                }
                if (connectD != null) {
                    connectionsMenu.add(new AbstractAction(Bundle.getMessage("MakeLabel", "D") + connectD.getName()) {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            LayoutEditorFindItems lf = layoutEditor.getFinder();
                            LayoutTrack lt = lf.findObjectByName(connectD.getName());
                            // this shouldn't ever be null... however...
                            if (lt != null) {
                                layoutEditor.setSelectionRect(lt.getBounds());
                                lt.showPopup();
                            }
                        }
                    });
                }
                popup.add(connectionsMenu);
            }
            popup.add(new JSeparator(JSeparator.HORIZONTAL));

            JCheckBoxMenuItem hiddenCheckBoxMenuItem = new JCheckBoxMenuItem(Bundle.getMessage("Hidden"));
            hiddenCheckBoxMenuItem.setSelected(hidden);
            popup.add(hiddenCheckBoxMenuItem);
            hiddenCheckBoxMenuItem.addActionListener((java.awt.event.ActionEvent e1) -> {
                JCheckBoxMenuItem o = (JCheckBoxMenuItem) e1.getSource();
                setHidden(o.isSelected());
            });

            JCheckBoxMenuItem cbmi = new JCheckBoxMenuItem(Bundle.getMessage("Disabled"));
            cbmi.setSelected(disabled);
            popup.add(cbmi);
            cbmi.addActionListener((java.awt.event.ActionEvent e2) -> {
                JCheckBoxMenuItem o = (JCheckBoxMenuItem) e2.getSource();
                setDisabled(o.isSelected());
            });

            cbmi = new JCheckBoxMenuItem(Bundle.getMessage("DisabledWhenOccupied"));
            if (getTurnout() == null || getBlockName().isEmpty()) {
                cbmi.setEnabled(false);
            }
            cbmi.setSelected(disableWhenOccupied);
            popup.add(cbmi);
            cbmi.addActionListener((java.awt.event.ActionEvent e3) -> {
                JCheckBoxMenuItem o = (JCheckBoxMenuItem) e3.getSource();
                setDisableWhenOccupied(o.isSelected());
            });

            // Rotate if there are no track connections
            if ((connectA == null) && (connectB == null)
                    && (connectC == null)
                    && (connectD == null)) {
                JMenuItem rotateItem = new JMenuItem(Bundle.getMessage("Rotate") + "...");
                popup.add(rotateItem);
                rotateItem.addActionListener((ActionEvent event) -> {
                    boolean entering = true;
                    boolean error = false;
                    String newAngle = "";
                    while (entering) {
                        // prompt for rotation angle
                        error = false;
                        newAngle = JOptionPane.showInputDialog(layoutEditor,
                                Bundle.getMessage("MakeLabel", Bundle.getMessage("EnterRotation")));
                        if (newAngle.isEmpty()) {
                            return;  // cancelled
                        }
                        double rot = 0.0;
                        try {
                            rot = Double.parseDouble(newAngle);
                        } catch (Exception e1) {
                            JOptionPane.showMessageDialog(layoutEditor, Bundle.getMessage("Error3")
                                    + " " + e1, Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                            error = true;
                            newAngle = "";
                        }
                        if (!error) {
                            entering = false;
                            if (rot != 0.0) {
                                rotateCoords(rot);
                                layoutEditor.redrawPanel();
                            }
                        }
                    }
                });
            }

            popup.add(new AbstractAction(Bundle.getMessage("UseSizeAsDefault")) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    setUpDefaultSize();
                }
            });
            popup.add(new AbstractAction(Bundle.getMessage("ButtonEdit")) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    layoutEditor.getLayoutTrackEditors().editLayoutTurnout(LayoutTurnout.this);
                }
            });
            popup.add(new AbstractAction(Bundle.getMessage("ButtonDelete")) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (canRemove() && layoutEditor.removeLayoutTurnout(LayoutTurnout.this)) {
                        // Returned true if user did not cancel
                        remove();
                        dispose();
                    }
                }
            });

            if (getTurnout() != null) {
                AbstractAction ssaa = new AbstractAction(Bundle.getMessage("SetSignals")) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        LayoutEditorTools tools = layoutEditor.getLETools();
                        if (isTurnoutTypeXover()) {
                            tools.setSignalsAtXoverTurnoutFromMenu(LayoutTurnout.this,
                                    layoutEditor.signalIconEditor, layoutEditor.signalFrame);
                        } else if (linkType == NO_LINK) {
                            tools.setSignalsAtTurnoutFromMenu(LayoutTurnout.this,
                                    layoutEditor.signalIconEditor, layoutEditor.signalFrame);
                        } else if (linkType == THROAT_TO_THROAT) {
                            tools.setSignalsAtThroatToThroatTurnoutsFromMenu(LayoutTurnout.this, linkedTurnoutName,
                                    layoutEditor.signalIconEditor, layoutEditor.signalFrame);
                        } else if (linkType == FIRST_3_WAY) {
                            tools.setSignalsAt3WayTurnoutFromMenu(getTurnoutName(), linkedTurnoutName,
                                    layoutEditor.signalIconEditor, layoutEditor.signalFrame);
                        } else if (linkType == SECOND_3_WAY) {
                            tools.setSignalsAt3WayTurnoutFromMenu(linkedTurnoutName, getTurnoutName(),
                                    layoutEditor.signalIconEditor, layoutEditor.signalFrame);
                        }
                    }
                };

                JMenu jm = new JMenu(Bundle.getMessage("SignalHeads"));
                if (layoutEditor.getLETools().addLayoutTurnoutSignalHeadInfoToMenu(
                        getTurnoutName(), linkedTurnoutName, jm)) {
                    jm.add(ssaa);
                    popup.add(jm);
                } else {
                    popup.add(ssaa);
                }
            }
            if (!getBlockName().isEmpty()) {
                final String[] boundaryBetween = getBlockBoundaries();
                boolean blockBoundaries = false;
                for (int i = 0; i < 4; i++) {
                    if (boundaryBetween[i] != null) {
                        blockBoundaries = true;

                    }
                }

                if (blockBoundaries) {
                    popup.add(new AbstractAction(Bundle.getMessage("SetSignalMasts")) {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            layoutEditor.getLETools().setSignalMastsAtTurnoutFromMenu(LayoutTurnout.this,
                                    boundaryBetween);
                        }
                    });
                    popup.add(new AbstractAction(Bundle.getMessage("SetSensors")) {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            layoutEditor.getLETools().setSensorsAtTurnoutFromMenu(
                                    LayoutTurnout.this,
                                    boundaryBetween,
                                    layoutEditor.sensorIconEditor,
                                    layoutEditor.sensorFrame);
                        }
                    });

                }

                if (InstanceManager.getDefault(LayoutBlockManager.class
                ).isAdvancedRoutingEnabled()) {
                    Map<String, LayoutBlock> map = new HashMap<>();
                    if (!getBlockName().isEmpty()) {
                        map.put(getBlockName(), getLayoutBlock());
                    }
                    if (!getBlockBName().isEmpty()) {
                        map.put(getBlockBName(), getLayoutBlockB());
                    }
                    if (!getBlockCName().isEmpty()) {
                        map.put(getBlockCName(), getLayoutBlockC());
                    }
                    if (!getBlockDName().isEmpty()) {
                        map.put(getBlockDName(), getLayoutBlockD());
                    }
                    if (blockBoundaries) {
                        if (map.size() == 1) {
                            popup.add(new AbstractAction(Bundle.getMessage("ViewBlockRouting")) {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    AbstractAction routeTableAction = new LayoutBlockRouteTableAction("ViewRouting", getLayoutBlock());
                                    routeTableAction.actionPerformed(e);
                                }
                            });
                        } else if (map.size() > 1) {
                            JMenu viewRouting = new JMenu(Bundle.getMessage("ViewBlockRouting"));
                            for (Map.Entry<String, LayoutBlock> entry : map.entrySet()) {
                                String blockName = entry.getKey();
                                LayoutBlock layoutBlock = entry.getValue();
                                viewRouting.add(new AbstractActionImpl(blockName, getBlockBName(), layoutBlock));
                            }
                            popup.add(viewRouting);
                        }
                    }   // if (blockBoundaries)
                }   // .isAdvancedRoutingEnabled()
            }   // getBlockName().isEmpty()
            setAdditionalEditPopUpMenu(popup);
            layoutEditor.setShowAlignmentMenu(popup);
            popup.show(mouseEvent.getComponent(), mouseEvent.getX(), mouseEvent.getY());
        } else if (!viewAdditionalMenu.isEmpty()) {
            setAdditionalViewPopUpMenu(popup);
            popup.show(mouseEvent.getComponent(), mouseEvent.getX(), mouseEvent.getY());
        }
        return popup;
    } // showPopup

    public String[] getBlockBoundaries() {
        final String[] boundaryBetween = new String[4];
        if ((getTurnoutType() == WYE_TURNOUT)
                || (getTurnoutType() == RH_TURNOUT)
                || (getTurnoutType() == LH_TURNOUT)) {
            //This should only be needed where we are looking at a single turnout.
            if (getLayoutBlock() != null) {
                LayoutBlock aLBlock = null;
                if (connectA instanceof TrackSegment) {
                    aLBlock = ((TrackSegment) connectA).getLayoutBlock();
                    if (aLBlock != getLayoutBlock()) {
                        try {
                            boundaryBetween[0] = (aLBlock.getDisplayName() + " - " + getLayoutBlock().getDisplayName());
                        } catch (java.lang.NullPointerException e) {
                            //Can be considered normal if tracksegement hasn't yet been allocated a block
                            log.debug("TrackSegement at connection A doesn't contain a layout block");
                        }
                    }
                }

                LayoutBlock bLBlock = null;
                if (connectB instanceof TrackSegment) {
                    bLBlock = ((TrackSegment) connectB).getLayoutBlock();
                    if (bLBlock != getLayoutBlock()) {
                        try {
                            boundaryBetween[1] = (bLBlock.getDisplayName() + " - " + getLayoutBlock().getDisplayName());
                        } catch (java.lang.NullPointerException e) {
                            //Can be considered normal if tracksegement hasn't yet been allocated a block
                            log.debug("TrackSegement at connection B doesn't contain a layout block");
                        }
                    }
                }

                LayoutBlock cLBlock = null;
                if ((connectC instanceof TrackSegment)
                        && (((TrackSegment) connectC).getLayoutBlock() != getLayoutBlock())) {
                    cLBlock = ((TrackSegment) connectC).getLayoutBlock();
                    if (cLBlock != getLayoutBlock()) {
                        try {
                            boundaryBetween[2] = (cLBlock.getDisplayName() + " - " + getLayoutBlock().getDisplayName());
                        } catch (java.lang.NullPointerException e) {
                            //Can be considered normal if tracksegement hasn't yet been allocated a block
                            log.debug("TrackSegement at connection C doesn't contain a layout block");
                        }
                    }
                }
            }
        } else {
            LayoutBlock aLBlock = null;
            LayoutBlock bLBlock = null;
            LayoutBlock cLBlock = null;
            LayoutBlock dLBlock = null;
            if (getLayoutBlock() != null) {
                if (connectA instanceof TrackSegment) {
                    aLBlock = ((TrackSegment) connectA).getLayoutBlock();
                    if (aLBlock != getLayoutBlock()) {
                        try {
                            boundaryBetween[0] = (aLBlock.getDisplayName() + " - " + getLayoutBlock().getDisplayName());
                        } catch (java.lang.NullPointerException e) {
                            //Can be considered normal if tracksegement hasn't yet been allocated a block
                            log.debug("TrackSegement at connection A doesn't contain a layout block");
                        }
                    } else if (getLayoutBlock() != getLayoutBlockB()) {
                        try {
                            boundaryBetween[0] = (getLayoutBlock().getDisplayName() + " - " + getLayoutBlockB().getDisplayName());
                        } catch (java.lang.NullPointerException e) {
                            //Can be considered normal if tracksegement hasn't yet been allocated a block
                            log.debug("TrackSegement at connection A doesn't contain a layout block");
                        }
                    }
                }

                if (connectB instanceof TrackSegment) {
                    bLBlock = ((TrackSegment) connectB).getLayoutBlock();

                    if (bLBlock != getLayoutBlock() && bLBlock != getLayoutBlockB()) {
                        try {
                            boundaryBetween[1] = (bLBlock.getDisplayName() + " - " + getLayoutBlockB().getDisplayName());
                        } catch (java.lang.NullPointerException e) {
                            //Can be considered normal if tracksegement hasn't yet been allocated a block
                            log.debug("TrackSegement at connection B doesn't contain a layout block");
                        }
                    } else if (getLayoutBlock() != getLayoutBlockB()) {
                        //This is an interal block on the turnout
                        try {
                            boundaryBetween[1] = (getLayoutBlockB().getDisplayName() + " - " + getLayoutBlock().getDisplayName());
                        } catch (java.lang.NullPointerException e) {
                            //Can be considered normal if tracksegement hasn't yet been allocated a block
                            log.debug("TrackSegement at connection A doesn't contain a layout block");
                        }
                    }
                }

                if (connectC instanceof TrackSegment) {
                    cLBlock = ((TrackSegment) connectC).getLayoutBlock();
                    if (cLBlock != getLayoutBlock() && cLBlock != getLayoutBlockB() && cLBlock != getLayoutBlockC()) {
                        try {
                            boundaryBetween[2] = (cLBlock.getDisplayName() + " - " + getLayoutBlockC().getDisplayName());
                        } catch (java.lang.NullPointerException e) {
                            //Can be considered normal if tracksegement hasn't yet been allocated a block
                            log.debug("TrackSegement at connection C doesn't contain a layout block");
                        }
                    } else if (getLayoutBlockC() != getLayoutBlockD()) {
                        //This is an interal block on the turnout
                        try {
                            boundaryBetween[2] = (getLayoutBlockC().getDisplayName() + " - " + getLayoutBlockD().getDisplayName());
                        } catch (java.lang.NullPointerException e) {
                            //Can be considered normal if tracksegement hasn't yet been allocated a block
                            log.debug("TrackSegement at connection A doesn't contain a layout block");
                        }
                    }
                }

                if (connectD instanceof TrackSegment) {
                    dLBlock = ((TrackSegment) connectD).getLayoutBlock();
                    if (dLBlock != getLayoutBlock() && dLBlock != getLayoutBlockB() && dLBlock != getLayoutBlockC() && dLBlock != getLayoutBlockD()) {
                        try {
                            boundaryBetween[3] = (dLBlock.getDisplayName() + " - " + getLayoutBlockD().getDisplayName());
                        } catch (java.lang.NullPointerException e) {
                            //Can be considered normal if tracksegement hasn't yet been allocated a block
                            log.debug("TrackSegement at connection C doesn't contain a layout block");
                        }
                    } else if (getLayoutBlockC() != getLayoutBlockD()) {
                        //This is an interal block on the turnout
                        try {
                            boundaryBetween[3] = (getLayoutBlockD().getDisplayName() + " - " + getLayoutBlockC().getDisplayName());
                        } catch (java.lang.NullPointerException e) {
                            //Can be considered normal if tracksegement hasn't yet been allocated a block
                            log.debug("TrackSegement at connection A doesn't contain a layout block");
                        }
                    }
                }
            }

        }
        return boundaryBetween;
    }   // getBlockBoundaries

    public ArrayList<LayoutBlock> getProtectedBlocks(jmri.NamedBean bean) {
        ArrayList<LayoutBlock> ret = new ArrayList<>(2);
        if (getLayoutBlock() == null) {
            return ret;
        }
        if (isTurnoutTypeXover()) {
            if ((getTurnoutType() == DOUBLE_XOVER || getTurnoutType() == RH_XOVER)
                    && (getSignalAMast() == bean || getSignalCMast() == bean || getSensorA() == bean || getSensorC() == bean)) {
                if (getSignalAMast() == bean || getSensorA() == bean) {
                    if (connectA != null) {
                        if (((TrackSegment) connectA).getLayoutBlock() == getLayoutBlock()) {
                            if (getLayoutBlockB() != null && getLayoutBlock() != getLayoutBlockB() && getLayoutBlockC() != null && getLayoutBlock() != getLayoutBlockC()) {
                                ret.add(getLayoutBlockB());
                                ret.add(getLayoutBlockC());
                            }
                        } else {
                            ret.add(getLayoutBlock());
                        }
                    }
                } else {
                    if (connectC != null && getLayoutBlockC() != null) {
                        if (((TrackSegment) connectC).getLayoutBlock() == getLayoutBlockC()) {
                            if (getLayoutBlockC() != getLayoutBlock() && getLayoutBlockD() != null && getLayoutBlockC() != getLayoutBlockD()) {
                                ret.add(getLayoutBlock());
                                ret.add(getLayoutBlockD());
                            }
                        } else {
                            ret.add(getLayoutBlockC());
                        }
                    }
                }
            }
            if ((getTurnoutType() == DOUBLE_XOVER || getTurnoutType() == LH_XOVER)
                    && (getSignalBMast() == bean || getSignalDMast() == bean || getSensorB() == bean || getSensorD() == bean)) {
                if (getSignalBMast() == bean || getSensorB() == bean) {
                    if (connectB != null && getLayoutBlockB() != null) {
                        if (((TrackSegment) connectB).getLayoutBlock() == getLayoutBlockB()) {
                            if (getLayoutBlock() != getLayoutBlockB() && getLayoutBlockD() != null && getLayoutBlockB() != getLayoutBlockD()) {
                                ret.add(getLayoutBlock());
                                ret.add(getLayoutBlockD());
                            }
                        } else {
                            ret.add(getLayoutBlockB());
                        }
                    }
                } else {
                    if (connectD != null && getLayoutBlockD() != null) {
                        if (((TrackSegment) connectD).getLayoutBlock() == getLayoutBlockD()) {
                            if (getLayoutBlockB() != null && getLayoutBlockB() != getLayoutBlockD() && getLayoutBlockC() != null && getLayoutBlockC() != getLayoutBlockD()) {
                                ret.add(getLayoutBlockB());
                                ret.add(getLayoutBlockC());
                            }
                        } else {
                            ret.add(getLayoutBlockD());
                        }
                    }
                }
            }
            if (getTurnoutType() == RH_XOVER && (getSignalBMast() == bean
                    || getSignalDMast() == bean || getSensorB() == bean || getSensorD() == bean)) {
                if (getSignalBMast() == bean || getSensorB() == bean) {
                    if (connectB != null && ((TrackSegment) connectB).getLayoutBlock() == getLayoutBlockB()) {
                        if (getLayoutBlockB() != getLayoutBlock()) {
                            ret.add(getLayoutBlock());
                        }
                    } else {
                        ret.add(getLayoutBlockB());
                    }
                } else {
                    if (connectD != null && ((TrackSegment) connectD).getLayoutBlock() == getLayoutBlockD()) {
                        if (getLayoutBlockC() != getLayoutBlockD()) {
                            ret.add(getLayoutBlockC());
                        }
                    } else {
                        ret.add(getLayoutBlockD());
                    }
                }
            }
            if (getTurnoutType() == LH_XOVER && (getSensorA() == bean
                    || getSensorC() == bean || getSignalAMast() == bean || getSignalCMast() == bean)) {
                if (getSignalAMast() == bean || getSensorA() == bean) {
                    if (connectA != null && ((TrackSegment) connectA).getLayoutBlock() == getLayoutBlock()) {
                        if (getLayoutBlockB() != getLayoutBlock()) {
                            ret.add(getLayoutBlockB());
                        }
                    } else {
                        ret.add(getLayoutBlock());
                    }
                } else {
                    if (connectC != null && ((TrackSegment) connectC).getLayoutBlock() == getLayoutBlockC()) {
                        if (getLayoutBlockC() != getLayoutBlockD()) {
                            ret.add(getLayoutBlockD());
                        }
                    } else {
                        ret.add(getLayoutBlockC());
                    }
                }
            }
        } else {
            if (connectA != null) {
                if (getSignalAMast() == bean || getSensorA() == bean) {
                    //Mast at throat
                    //if the turnout is in the same block as the segment connected at the throat, then we can be protecting two blocks
                    if (((TrackSegment) connectA).getLayoutBlock() == getLayoutBlock()) {
                        if (connectB != null && connectC != null) {
                            if (((TrackSegment) connectB).getLayoutBlock() != getLayoutBlock()
                                    && ((TrackSegment) connectC).getLayoutBlock() != getLayoutBlock()) {
                                ret.add(((TrackSegment) connectB).getLayoutBlock());
                                ret.add(((TrackSegment) connectC).getLayoutBlock());
                            }
                        }
                    } else {
                        ret.add(getLayoutBlock());
                    }
                } else if (getSignalBMast() == bean || getSensorB() == bean) {
                    //Mast at Continuing
                    if (connectB != null && ((TrackSegment) connectB).getLayoutBlock() == getLayoutBlock()) {
                        if (((TrackSegment) connectA).getLayoutBlock() != getLayoutBlock()) {
                            ret.add(((TrackSegment) connectA).getLayoutBlock());
                        }
                    } else {
                        ret.add(getLayoutBlock());
                    }
                } else if (getSignalCMast() == bean || getSensorC() == bean) {
                    //Mast at Diverging
                    if (connectC != null && ((TrackSegment) connectC).getLayoutBlock() == getLayoutBlock()) {
                        if (((TrackSegment) connectA).getLayoutBlock() != getLayoutBlock()) {
                            ret.add(((TrackSegment) connectA).getLayoutBlock());
                        }
                    } else {
                        ret.add(getLayoutBlock());
                    }
                }
            }
        }
        return ret;
    }   // getProtectedBlocks

    protected void removeSML(SignalMast signalMast) {
        if (signalMast == null) {
            return;

        }
        if (jmri.InstanceManager.getDefault(LayoutBlockManager.class
        ).isAdvancedRoutingEnabled() && InstanceManager.getDefault(jmri.SignalMastLogicManager.class
        ).isSignalMastUsed(signalMast)) {
            SignallingGuiTools.removeSignalMastLogic(null, signalMast);
        }
    }

    /**
     * Clean up when this object is no longer needed. Should not be called while
     * the object is still displayed; see {@link #remove()}
     */
    void dispose() {
        if (popup != null) {
            popup.removeAll();
        }
        popup = null;
    }

    /**
     * Remove this object from display and persistance.
     */
    void remove() {
        // if a turnout has been activated, deactivate it
        deactivateTurnout();
        // remove from persistance by flagging inactive
        active = false;
    }

    boolean active = true;

    /**
     * "active" means that the object is still displayed, and should be stored.
     */
    public boolean isActive() {
        return active;
    }

    ArrayList<JMenuItem> editAdditionalMenu = new ArrayList<>(0);
    ArrayList<JMenuItem> viewAdditionalMenu = new ArrayList<>(0);

    public void addEditPopUpMenu(JMenuItem menu) {
        if (!editAdditionalMenu.contains(menu)) {
            editAdditionalMenu.add(menu);
        }
    }

    public void addViewPopUpMenu(JMenuItem menu) {
        if (!viewAdditionalMenu.contains(menu)) {
            viewAdditionalMenu.add(menu);
        }
    }

    public void setAdditionalEditPopUpMenu(JPopupMenu popup) {
        if (editAdditionalMenu.isEmpty()) {
            return;
        }
        popup.addSeparator();
        for (JMenuItem mi : editAdditionalMenu) {
            popup.add(mi);
        }
    }

    public void setAdditionalViewPopUpMenu(JPopupMenu popup) {
        if (viewAdditionalMenu.isEmpty()) {
            return;
        }
        popup.addSeparator();
        for (JMenuItem mi : viewAdditionalMenu) {
            popup.add(mi);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void draw1(Graphics2D g2, boolean isMain, boolean isBlock) {
        if (isBlock && getLayoutBlock() == null) {
            // Skip the block layer if there is no block assigned.
            return;
        }

        Point2D pA = getCoordsA();
        Point2D pB = getCoordsB();
        Point2D pC = getCoordsC();
        Point2D pD = getCoordsD();

        boolean mainlineA = isMainlineA();
        boolean mainlineB = isMainlineB();
        boolean mainlineC = isMainlineC();
        boolean mainlineD = isMainlineD();

        boolean drawUnselectedLeg = layoutEditor.isTurnoutDrawUnselectedLeg();

        Color color = g2.getColor();

        // if this isn't a block line all these will be the same color
        Color colorA = color;
        Color colorB = color;
        Color colorC = color;
        Color colorD = color;

        if (isBlock) {
            LayoutBlock lb = getLayoutBlock();
            colorA = (lb == null) ? color : lb.getBlockColor();
            lb = getLayoutBlockB();
            colorB = (lb == null) ? color : lb.getBlockColor();
            lb = getLayoutBlockC();
            colorC = (lb == null) ? color : lb.getBlockColor();
            lb = getLayoutBlockD();
            colorD = (lb == null) ? color : lb.getBlockColor();
        }

        // middles
        Point2D pM = getCoordsCenter();
        Point2D pABM = MathUtil.midPoint(pA, pB);
        Point2D pAM = MathUtil.lerp(pA, pABM, 5.0 / 8.0);
        Point2D pAMP = MathUtil.midPoint(pAM, pABM);
        Point2D pBM = MathUtil.lerp(pB, pABM, 5.0 / 8.0);
        Point2D pBMP = MathUtil.midPoint(pBM, pABM);

        Point2D pCDM = MathUtil.midPoint(pC, pD);
        Point2D pCM = MathUtil.lerp(pC, pCDM, 5.0 / 8.0);
        Point2D pCMP = MathUtil.midPoint(pCM, pCDM);
        Point2D pDM = MathUtil.lerp(pD, pCDM, 5.0 / 8.0);
        Point2D pDMP = MathUtil.midPoint(pDM, pCDM);

        Point2D pAF = MathUtil.midPoint(pAM, pM);
        Point2D pBF = MathUtil.midPoint(pBM, pM);
        Point2D pCF = MathUtil.midPoint(pCM, pM);
        Point2D pDF = MathUtil.midPoint(pDM, pM);

        int state = UNKNOWN;
        if (layoutEditor.isAnimating()) {
            state = getState();
        }

        int type = getTurnoutType();
        if (type == DOUBLE_XOVER) {
            if (state != Turnout.THROWN && state != INCONSISTENT) { // unknown or continuing path - not crossed over
                if (isMain == mainlineA) {
                    g2.setColor(colorA);
                    g2.draw(new Line2D.Double(pA, pABM));
                    if (!isBlock || drawUnselectedLeg) {
                        g2.draw(new Line2D.Double(pAF, pM));
                    }
                }
                if (isMain == mainlineB) {
                    g2.setColor(colorB);
                    g2.draw(new Line2D.Double(pB, pABM));
                    if (!isBlock || drawUnselectedLeg) {
                        g2.draw(new Line2D.Double(pBF, pM));
                    }
                }
                if (isMain == mainlineC) {
                    g2.setColor(colorC);
                    g2.draw(new Line2D.Double(pC, pCDM));
                    if (!isBlock || drawUnselectedLeg) {
                        g2.draw(new Line2D.Double(pCF, pM));
                    }
                }
                if (isMain == mainlineD) {
                    g2.setColor(colorD);
                    g2.draw(new Line2D.Double(pD, pCDM));
                    if (!isBlock || drawUnselectedLeg) {
                        g2.draw(new Line2D.Double(pDF, pM));
                    }
                }
            }
            if (state != Turnout.CLOSED && state != INCONSISTENT) { // unknown or diverting path - crossed over
                if (isMain == mainlineA) {
                    g2.setColor(colorA);
                    g2.draw(new Line2D.Double(pA, pAM));
                    g2.draw(new Line2D.Double(pAM, pM));
                    if (!isBlock || drawUnselectedLeg) {
                        g2.draw(new Line2D.Double(pAMP, pABM));
                    }
                }
                if (isMain == mainlineB) {
                    g2.setColor(colorB);
                    g2.draw(new Line2D.Double(pB, pBM));
                    g2.draw(new Line2D.Double(pBM, pM));
                    if (!isBlock || drawUnselectedLeg) {
                        g2.draw(new Line2D.Double(pBMP, pABM));
                    }
                }
                if (isMain == mainlineC) {
                    g2.setColor(colorC);
                    g2.draw(new Line2D.Double(pC, pCM));
                    g2.draw(new Line2D.Double(pCM, pM));
                    if (!isBlock || drawUnselectedLeg) {
                        g2.draw(new Line2D.Double(pCMP, pCDM));
                    }
                }
                if (isMain == mainlineD) {
                    g2.setColor(colorD);
                    g2.draw(new Line2D.Double(pD, pDM));
                    g2.draw(new Line2D.Double(pDM, pM));
                    if (!isBlock || drawUnselectedLeg) {
                        g2.draw(new Line2D.Double(pDMP, pCDM));
                    }
                }
            }
            if (state == INCONSISTENT) {
                if (isMain == mainlineA) {
                    g2.setColor(colorA);
                    g2.draw(new Line2D.Double(pA, pAM));
                }
                if (isMain == mainlineB) {
                    g2.setColor(colorB);
                    g2.draw(new Line2D.Double(pB, pBM));
                }
                if (isMain == mainlineC) {
                    g2.setColor(colorC);
                    g2.draw(new Line2D.Double(pC, pCM));
                }
                if (isMain == mainlineD) {
                    g2.setColor(colorD);
                    g2.draw(new Line2D.Double(pD, pDM));
                }
                if (!isBlock || drawUnselectedLeg) {
                    if (isMain == mainlineA) {
                        g2.setColor(colorA);
                        g2.draw(new Line2D.Double(pAF, pM));
                    }
                    if (isMain == mainlineC) {
                        g2.setColor(colorC);
                        g2.draw(new Line2D.Double(pCF, pM));
                    }
                    if (isMain == mainlineB) {
                        g2.setColor(colorB);
                        g2.draw(new Line2D.Double(pBF, pM));
                    }
                    if (isMain == mainlineD) {
                        g2.setColor(colorD);
                        g2.draw(new Line2D.Double(pDF, pM));
                    }
                }
            }
        } else if ((type == RH_XOVER)
                || (type == LH_XOVER)) {    // draw (rh & lh) cross overs
            pAF = MathUtil.midPoint(pABM, pM);
            pBF = MathUtil.midPoint(pABM, pM);
            pCF = MathUtil.midPoint(pCDM, pM);
            pDF = MathUtil.midPoint(pCDM, pM);
            if (state != Turnout.THROWN && state != INCONSISTENT) { // unknown or continuing path - not crossed over
                if (isMain == mainlineA) {
                    g2.setColor(colorA);
                    g2.draw(new Line2D.Double(pA, pABM));
                }
                if (isMain == mainlineB) {
                    g2.setColor(colorB);
                    g2.draw(new Line2D.Double(pABM, pB));
                }
                if (isMain == mainlineC) {
                    g2.setColor(colorC);
                    g2.draw(new Line2D.Double(pC, pCDM));
                }
                if (isMain == mainlineD) {
                    g2.setColor(colorD);
                    g2.draw(new Line2D.Double(pCDM, pD));
                }
                if (!isBlock || drawUnselectedLeg) {
                    if (getTurnoutType() == RH_XOVER) {
                        if (isMain == mainlineA) {
                            g2.setColor(colorA);
                            g2.draw(new Line2D.Double(pAF, pM));
                        }
                        if (isMain == mainlineC) {
                            g2.setColor(colorC);
                            g2.draw(new Line2D.Double(pCF, pM));
                        }
                    } else if (getTurnoutType() == LH_XOVER) {
                        if (isMain == mainlineB) {
                            g2.setColor(colorB);
                            g2.draw(new Line2D.Double(pBF, pM));
                        }
                        if (isMain == mainlineD) {
                            g2.setColor(colorD);
                            g2.draw(new Line2D.Double(pDF, pM));
                        }
                    }
                }
            }
            if (state != Turnout.CLOSED && state != INCONSISTENT) { // unknown or diverting path - crossed over
                if (getTurnoutType() == RH_XOVER) {
                    if (isMain == mainlineA) {
                        g2.setColor(colorA);
                        g2.draw(new Line2D.Double(pA, pABM));
                        g2.draw(new Line2D.Double(pABM, pM));
                    }
                    if (!isBlock || drawUnselectedLeg) {
                        if (isMain == mainlineB) {
                            g2.setColor(colorB);
                            g2.draw(new Line2D.Double(pBM, pB));
                        }
                    }
                    if (isMain == mainlineC) {
                        g2.setColor(colorC);
                        g2.draw(new Line2D.Double(pC, pCDM));
                        g2.draw(new Line2D.Double(pCDM, pM));
                    }
                    if (!isBlock || drawUnselectedLeg) {
                        if (isMain == mainlineD) {
                            g2.setColor(colorD);
                            g2.draw(new Line2D.Double(pDM, pD));
                        }
                    }
                } else if (getTurnoutType() == LH_XOVER) {
                    if (!isBlock || drawUnselectedLeg) {
                        if (isMain == mainlineA) {
                            g2.setColor(colorA);
                            g2.draw(new Line2D.Double(pA, pAM));
                        }
                    }
                    if (isMain == mainlineB) {
                        g2.setColor(colorB);
                        g2.draw(new Line2D.Double(pB, pABM));
                        g2.draw(new Line2D.Double(pABM, pM));
                    }
                    if (!isBlock || drawUnselectedLeg) {
                        if (isMain == mainlineC) {
                            g2.setColor(colorC);
                            g2.draw(new Line2D.Double(pC, pCM));
                        }
                    }
                    if (isMain == mainlineD) {
                        g2.setColor(colorD);
                        g2.draw(new Line2D.Double(pD, pCDM));
                        g2.draw(new Line2D.Double(pCDM, pM));
                    }
                }
            }
            if (state == INCONSISTENT) {
                if (isMain == mainlineA) {
                    g2.setColor(colorA);
                    g2.draw(new Line2D.Double(pA, pAM));
                }
                if (isMain == mainlineB) {
                    g2.setColor(colorB);
                    g2.draw(new Line2D.Double(pB, pBM));
                }
                if (isMain == mainlineC) {
                    g2.setColor(colorC);
                    g2.draw(new Line2D.Double(pC, pCM));
                }
                if (isMain == mainlineD) {
                    g2.setColor(colorD);
                    g2.draw(new Line2D.Double(pD, pDM));
                }
                if (!isBlock || drawUnselectedLeg) {
                    if (getTurnoutType() == RH_XOVER) {
                        if (isMain == mainlineA) {
                            g2.setColor(colorA);
                            g2.draw(new Line2D.Double(pAF, pM));
                        }
                        if (isMain == mainlineC) {
                            g2.setColor(colorC);
                            g2.draw(new Line2D.Double(pCF, pM));
                        }
                    } else if (getTurnoutType() == LH_XOVER) {
                        if (isMain == mainlineB) {
                            g2.setColor(colorB);
                            g2.draw(new Line2D.Double(pBF, pM));
                        }
                        if (isMain == mainlineD) {
                            g2.setColor(colorD);
                            g2.draw(new Line2D.Double(pDF, pM));
                        }
                    }
                }
            }
        } else if (isTurnoutTypeSlip()) {
            log.error("slips should be being drawn by LayoutSlip sub-class");
        } else {    // LH, RH, or WYE Turnouts
            // draw A<===>center
            if (isMain == mainlineA) {
                g2.setColor(colorA);
                g2.draw(new Line2D.Double(pA, pM));
            }

            if (state == UNKNOWN || (continuingSense == state && state != INCONSISTENT)) { // unknown or continuing path
                // draw center<===>B
                if (isMain == mainlineB) {
                    g2.setColor(colorB);
                    g2.draw(new Line2D.Double(pM, pB));
                }
            } else if (!isBlock || drawUnselectedLeg) {
                // draw center<--=>B
                if (isMain == mainlineB) {
                    g2.setColor(colorB);
                    g2.draw(new Line2D.Double(MathUtil.twoThirdsPoint(pM, pB), pB));
                }
            }

            if (state == UNKNOWN || (continuingSense != state && state != INCONSISTENT)) { // unknown or diverting path
                // draw center<===>C
                if (isMain == mainlineC) {
                    g2.setColor(colorC);
                    g2.draw(new Line2D.Double(pM, pC));
                }
            } else if (!isBlock || drawUnselectedLeg) {
                // draw center<--=>C
                if (isMain == mainlineC) {
                    g2.setColor(colorC);
                    g2.draw(new Line2D.Double(MathUtil.twoThirdsPoint(pM, pC), pC));
                }
            }
        }
    }   // draw1

    /**
     * {@inheritDoc}
     */
    @Override
    protected void draw2(Graphics2D g2, boolean isMain, float railDisplacement) {
        int type = getTurnoutType();

        Point2D pA = getCoordsA();
        Point2D pB = getCoordsB();
        Point2D pC = getCoordsC();
        Point2D pD = getCoordsD();
        Point2D pM = getCoordsCenter();

        Point2D vAM = MathUtil.normalize(MathUtil.subtract(pM, pA));
        Point2D vAMo = MathUtil.orthogonal(MathUtil.normalize(vAM, railDisplacement));

        Point2D pAL = MathUtil.subtract(pA, vAMo);
        Point2D pAR = MathUtil.add(pA, vAMo);

        Point2D vBM = MathUtil.normalize(MathUtil.subtract(pB, pM));
        double dirBM_DEG = MathUtil.computeAngleDEG(vBM);
        Point2D vBMo = MathUtil.normalize(MathUtil.orthogonal(vBM), railDisplacement);
        Point2D pBL = MathUtil.subtract(pB, vBMo);
        Point2D pBR = MathUtil.add(pB, vBMo);
        Point2D pMR = MathUtil.add(pM, vBMo);

        Point2D vCM = MathUtil.normalize(MathUtil.subtract(pC, pM));
        double dirCM_DEG = MathUtil.computeAngleDEG(vCM);

        Point2D vCMo = MathUtil.normalize(MathUtil.orthogonal(vCM), railDisplacement);
        Point2D pCL = MathUtil.subtract(pC, vCMo);
        Point2D pCR = MathUtil.add(pC, vCMo);
        Point2D pML = MathUtil.subtract(pM, vBMo);

        double deltaBMC_DEG = MathUtil.absDiffAngleDEG(dirBM_DEG, dirCM_DEG);
        double deltaBMC_RAD = Math.toRadians(deltaBMC_DEG);

        double hypotF = railDisplacement / Math.sin(deltaBMC_RAD / 2.0);

        Point2D vDisF = MathUtil.normalize(MathUtil.add(vAM, vCM), hypotF);
        if (type == WYE_TURNOUT) {
            vDisF = MathUtil.normalize(vAM, hypotF);
        }
        Point2D pF = MathUtil.add(pM, vDisF);

        Point2D pFR = MathUtil.add(pF, MathUtil.multiply(vBMo, 2.0));
        Point2D pFL = MathUtil.subtract(pF, MathUtil.multiply(vCMo, 2.0));

        // Point2D pFPR = MathUtil.add(pF, MathUtil.normalize(vBMo, 2.0));
        // Point2D pFPL = MathUtil.subtract(pF, MathUtil.normalize(vCMo, 2.0));
        Point2D vDisAP = MathUtil.normalize(vAM, hypotF);
        Point2D pAP = MathUtil.subtract(pM, vDisAP);
        Point2D pAPR = MathUtil.add(pAP, vAMo);
        Point2D pAPL = MathUtil.subtract(pAP, vAMo);

        // Point2D vSo = MathUtil.normalize(vAMo, 2.0);
        // Point2D pSL = MathUtil.add(pAPL, vSo);
        // Point2D pSR = MathUtil.subtract(pAPR, vSo);
        boolean mainlineA = isMainlineA();
        boolean mainlineB = isMainlineB();
        boolean mainlineC = isMainlineC();
        boolean mainlineD = isMainlineD();

        int state = UNKNOWN;
        if (layoutEditor.isAnimating()) {
            state = getState();
        }

        switch (type) {
            case RH_TURNOUT: {
                if (isMain == mainlineA) {
                    g2.draw(new Line2D.Double(pAL, pML));
                    g2.draw(new Line2D.Double(pAR, pAPR));
                }
                if (isMain == mainlineB) {
                    g2.draw(new Line2D.Double(pML, pBL));
                    g2.draw(new Line2D.Double(pF, pBR));
                    if (continuingSense == state) {  // unknown or diverting path
//                         g2.draw(new Line2D.Double(pSR, pFPR));
//                     } else {
                        g2.draw(new Line2D.Double(pAPR, pF));
                    }
                }
                if (isMain == mainlineC) {
                    g2.draw(new Line2D.Double(pF, pCL));
                    g2.draw(new Line2D.Double(pFR, pCR));
                    GeneralPath path = new GeneralPath();
                    path.moveTo(pAPR.getX(), pAPR.getY());
                    path.quadTo(pMR.getX(), pMR.getY(), pFR.getX(), pFR.getY());
                    path.lineTo(pCR.getX(), pCR.getY());
                    g2.draw(path);
                    if (continuingSense != state) {  // unknown or diverting path
                        path = new GeneralPath();
                        path.moveTo(pAPL.getX(), pAPL.getY());
                        path.quadTo(pML.getX(), pML.getY(), pF.getX(), pF.getY());
                        g2.draw(path);
//                     } else {
//                         path = new GeneralPath();
//                         path.moveTo(pSL.getX(), pSL.getY());
//                         path.quadTo(pML.getX(), pML.getY(), pFPL.getX(), pFPL.getY());
//                         g2.draw(path);
                    }
                }
                break;
            }   // case RH_TURNOUT

            case LH_TURNOUT: {
                if (isMain == mainlineA) {
                    g2.draw(new Line2D.Double(pAR, pMR));
                    g2.draw(new Line2D.Double(pAL, pAPL));
                }
                if (isMain == mainlineB) {
                    g2.draw(new Line2D.Double(pMR, pBR));
                    g2.draw(new Line2D.Double(pF, pBL));
                    if (continuingSense == state) {  // straight path
//                         g2.draw(new Line2D.Double(pSL, pFPL));  Offset problem
//                     } else {
                        g2.draw(new Line2D.Double(pAPL, pF));
                    }
                }
                if (isMain == mainlineC) {
                    g2.draw(new Line2D.Double(pF, pCR));
                    GeneralPath path = new GeneralPath();
                    path.moveTo(pAPL.getX(), pAPL.getY());
                    path.quadTo(pML.getX(), pML.getY(), pFL.getX(), pFL.getY());
                    path.lineTo(pCL.getX(), pCL.getY());
                    g2.draw(path);
                    if (continuingSense != state) {  // unknown or diverting path
                        path = new GeneralPath();
                        path.moveTo(pAPR.getX(), pAPR.getY());
                        path.quadTo(pMR.getX(), pMR.getY(), pF.getX(), pF.getY());
                        g2.draw(path);
//                     } else {
//                         path = new GeneralPath();
//                         path.moveTo(pSR.getX(), pSR.getY());
//                         path.quadTo(pMR.getX(), pMR.getY(), pFPR.getX(), pFPR.getY());
//                         g2.draw(path);
                    }
                }
                break;
            }   // case LH_TURNOUT

            case WYE_TURNOUT: {
                if (isMain == mainlineA) {
                    g2.draw(new Line2D.Double(pAL, pAPL));
                    g2.draw(new Line2D.Double(pAR, pAPR));
                }
                if (isMain == mainlineB) {
                    g2.draw(new Line2D.Double(pF, pBL));
                    GeneralPath path = new GeneralPath();
                    path.moveTo(pAPR.getX(), pAPR.getY());
                    path.quadTo(pMR.getX(), pMR.getY(), pFR.getX(), pFR.getY());
                    path.lineTo(pBR.getX(), pBR.getY());
                    g2.draw(path);
                    if (continuingSense != state) {  // unknown or diverting path
                        path = new GeneralPath();
                        path.moveTo(pAPR.getX(), pAPR.getY());
                        path.quadTo(pMR.getX(), pMR.getY(), pF.getX(), pF.getY());
                        g2.draw(path);
//                     } else {
//                         path = new GeneralPath();
//                         path.moveTo(pSR.getX(), pSR.getY());
//                         path.quadTo(pMR.getX(), pMR.getY(), pFPR.getX(), pFPR.getY());
//                  bad    g2.draw(path);
                    }
                }
                if (isMain == mainlineC) {
                    pML = MathUtil.subtract(pM, vCMo);
                    GeneralPath path = new GeneralPath();
                    path.moveTo(pAPL.getX(), pAPL.getY());
                    path.quadTo(pML.getX(), pML.getY(), pFL.getX(), pFL.getY());
                    path.lineTo(pCL.getX(), pCL.getY());
                    g2.draw(path);
                    g2.draw(new Line2D.Double(pF, pCR));
                    if (continuingSense != state) {  // unknown or diverting path
//                         path = new GeneralPath();
//                         path.moveTo(pSL.getX(), pSL.getY());
//                         path.quadTo(pML.getX(), pML.getY(), pFPL.getX(), pFPL.getY());
//           bad              g2.draw(path);
                    } else {
                        path = new GeneralPath();
                        path.moveTo(pAPL.getX(), pAPL.getY());
                        path.quadTo(pML.getX(), pML.getY(), pF.getX(), pF.getY());
                        g2.draw(path);
                    }
                }
                break;
            }   // case WYE_TURNOUT

            case DOUBLE_XOVER: {
                // A, B, C, D end points (left and right)
                Point2D vAB = MathUtil.normalize(MathUtil.subtract(pB, pA), railDisplacement);
                double dirAB_DEG = MathUtil.computeAngleDEG(vAB);
                Point2D vABo = MathUtil.orthogonal(MathUtil.normalize(vAB, railDisplacement));
                pAL = MathUtil.subtract(pA, vABo);
                pAR = MathUtil.add(pA, vABo);
                pBL = MathUtil.subtract(pB, vABo);
                pBR = MathUtil.add(pB, vABo);
                Point2D vCD = MathUtil.normalize(MathUtil.subtract(pD, pC), railDisplacement);
                Point2D vCDo = MathUtil.orthogonal(MathUtil.normalize(vCD, railDisplacement));
                pCL = MathUtil.add(pC, vCDo);
                pCR = MathUtil.subtract(pC, vCDo);
                Point2D pDL = MathUtil.add(pD, vCDo);
                Point2D pDR = MathUtil.subtract(pD, vCDo);

                // AB, CD mid points (left and right)
                Point2D pABM = MathUtil.midPoint(pA, pB);
                Point2D pABL = MathUtil.midPoint(pAL, pBL);
                Point2D pABR = MathUtil.midPoint(pAR, pBR);
                Point2D pCDM = MathUtil.midPoint(pC, pD);
                Point2D pCDL = MathUtil.midPoint(pCL, pDL);
                Point2D pCDR = MathUtil.midPoint(pCR, pDR);

                // A, B, C, D mid points
                double halfParallelDistance = MathUtil.distance(pABM, pCDM) / 2.0;
                Point2D pAM = MathUtil.subtract(pABM, MathUtil.normalize(vAB, halfParallelDistance));
                Point2D pAML = MathUtil.subtract(pAM, vABo);
                Point2D pAMR = MathUtil.add(pAM, vABo);
                Point2D pBM = MathUtil.add(pABM, MathUtil.normalize(vAB, halfParallelDistance));
                Point2D pBML = MathUtil.subtract(pBM, vABo);
                Point2D pBMR = MathUtil.add(pBM, vABo);
                Point2D pCM = MathUtil.subtract(pCDM, MathUtil.normalize(vCD, halfParallelDistance));
                Point2D pCML = MathUtil.subtract(pCM, vABo);
                Point2D pCMR = MathUtil.add(pCM, vABo);
                Point2D pDM = MathUtil.add(pCDM, MathUtil.normalize(vCD, halfParallelDistance));
                Point2D pDML = MathUtil.subtract(pDM, vABo);
                Point2D pDMR = MathUtil.add(pDM, vABo);

                // crossing points
                Point2D vACM = MathUtil.normalize(MathUtil.subtract(pCM, pAM), railDisplacement);
                Point2D vACMo = MathUtil.orthogonal(vACM);
                Point2D vBDM = MathUtil.normalize(MathUtil.subtract(pDM, pBM), railDisplacement);
                Point2D vBDMo = MathUtil.orthogonal(vBDM);
                Point2D pBDR = MathUtil.add(pM, vACM);
                Point2D pBDL = MathUtil.subtract(pM, vACM);

                // crossing diamond point (no gaps)
                Point2D pVR = MathUtil.add(pBDL, vBDM);
                Point2D pKL = MathUtil.subtract(pBDL, vBDM);
                Point2D pKR = MathUtil.add(pBDR, vBDM);
                Point2D pVL = MathUtil.subtract(pBDR, vBDM);

                // crossing diamond points (with gaps)
                Point2D vACM2 = MathUtil.normalize(vACM, 2.0);
                Point2D vBDM2 = MathUtil.normalize(vBDM, 2.0);
                // (syntax of "pKLtC" is "point LK toward C", etc.)
                Point2D pKLtC = MathUtil.add(pKL, vACM2);
                Point2D pKLtD = MathUtil.add(pKL, vBDM2);
                Point2D pVLtA = MathUtil.subtract(pVL, vACM2);
                Point2D pVLtD = MathUtil.add(pVL, vBDM2);
                Point2D pKRtA = MathUtil.subtract(pKR, vACM2);
                Point2D pKRtB = MathUtil.subtract(pKR, vBDM2);
                Point2D pVRtB = MathUtil.subtract(pVR, vBDM2);
                Point2D pVRtC = MathUtil.add(pVR, vACM2);

                // A, B, C, D frog points
                vCM = MathUtil.normalize(MathUtil.subtract(pCM, pM));
                dirCM_DEG = MathUtil.computeAngleDEG(vCM);
                double deltaBAC_DEG = MathUtil.absDiffAngleDEG(dirAB_DEG, dirCM_DEG);
                double deltaBAC_RAD = Math.toRadians(deltaBAC_DEG);
                hypotF = railDisplacement / Math.sin(deltaBAC_RAD / 2.0);
                Point2D vACF = MathUtil.normalize(MathUtil.add(vACM, vAB), hypotF);
                Point2D pAFL = MathUtil.add(pAM, vACF);
                Point2D pCFR = MathUtil.subtract(pCM, vACF);
                Point2D vBDF = MathUtil.normalize(MathUtil.add(vBDM, vCD), hypotF);
                Point2D pBFL = MathUtil.add(pBM, vBDF);
                Point2D pDFR = MathUtil.subtract(pDM, vBDF);

                // A, B, C, D frog points
                Point2D pAFR = MathUtil.add(MathUtil.add(pAFL, vACMo), vACMo);
                Point2D pBFR = MathUtil.subtract(MathUtil.subtract(pBFL, vBDMo), vBDMo);
                Point2D pCFL = MathUtil.subtract(MathUtil.subtract(pCFR, vACMo), vACMo);
                Point2D pDFL = MathUtil.add(MathUtil.add(pDFR, vBDMo), vBDMo);

                // end of switch rails (closed)
                Point2D vABF = MathUtil.normalize(vAB, hypotF);
                pAP = MathUtil.subtract(pAM, vABF);
                pAPL = MathUtil.subtract(pAP, vABo);
                pAPR = MathUtil.add(pAP, vABo);
                Point2D pBP = MathUtil.add(pBM, vABF);
                Point2D pBPL = MathUtil.subtract(pBP, vABo);
                Point2D pBPR = MathUtil.add(pBP, vABo);

                Point2D vCDF = MathUtil.normalize(vCD, hypotF);
                Point2D pCP = MathUtil.subtract(pCM, vCDF);
                Point2D pCPL = MathUtil.add(pCP, vCDo);
                Point2D pCPR = MathUtil.subtract(pCP, vCDo);
                Point2D pDP = MathUtil.add(pDM, vCDF);
                Point2D pDPL = MathUtil.add(pDP, vCDo);
                Point2D pDPR = MathUtil.subtract(pDP, vCDo);

                // end of switch rails (open)
                Point2D vS = MathUtil.normalize(vABo, 2.0);
                Point2D pASL = MathUtil.add(pAPL, vS);
                // Point2D pASR = MathUtil.subtract(pAPR, vS);
                Point2D pBSL = MathUtil.add(pBPL, vS);
                // Point2D pBSR = MathUtil.subtract(pBPR, vS);
                Point2D pCSR = MathUtil.subtract(pCPR, vS);
                // Point2D pCSL = MathUtil.add(pCPL, vS);
                Point2D pDSR = MathUtil.subtract(pDPR, vS);
                // Point2D pDSL = MathUtil.add(pDPL, vS);

                // end of switch rails (open at frogs)
                Point2D pAFS = MathUtil.subtract(pAFL, vS);
                Point2D pBFS = MathUtil.subtract(pBFL, vS);
                Point2D pCFS = MathUtil.add(pCFR, vS);
                Point2D pDFS = MathUtil.add(pDFR, vS);

                // vSo = MathUtil.orthogonal(vS);
                // Point2D pAFSR = MathUtil.add(pAFL, vSo);
                // Point2D pBFSR = MathUtil.subtract(pBFL, vSo);
                // Point2D pCFSL = MathUtil.subtract(pCFR, vSo);
                // Point2D pDFSL = MathUtil.add(pDFR, vSo);
                if (isMain == mainlineA) {
                    g2.draw(new Line2D.Double(pAL, pABL));
                    g2.draw(new Line2D.Double(pVRtB, pKLtD));
                    g2.draw(new Line2D.Double(pAFL, pABR));
                    g2.draw(new Line2D.Double(pAFL, pKL));
                    GeneralPath path = new GeneralPath();
                    path.moveTo(pAR.getX(), pAR.getY());
                    path.lineTo(pAPR.getX(), pAPR.getY());
                    path.quadTo(pAMR.getX(), pAMR.getY(), pAFR.getX(), pAFR.getY());
                    path.lineTo(pVR.getX(), pVR.getY());
                    g2.draw(path);
                    if (state != Turnout.CLOSED) {  // unknown or diverting path
                        path = new GeneralPath();
                        path.moveTo(pAPL.getX(), pAPL.getY());
                        path.quadTo(pAML.getX(), pAML.getY(), pAFL.getX(), pAFL.getY());
                        g2.draw(path);
//                         g2.draw(new Line2D.Double(pASR, pAFSR));
                    } else {                        // continuing path
                        g2.draw(new Line2D.Double(pAPR, pAFL));
                        path = new GeneralPath();
                        path.moveTo(pASL.getX(), pASL.getY());
                        path.quadTo(pAML.getX(), pAML.getY(), pAFS.getX(), pAFS.getY());
//                         g2.draw(path);
                    }
                }
                if (isMain == mainlineB) {
                    g2.draw(new Line2D.Double(pABL, pBL));
                    g2.draw(new Line2D.Double(pKLtC, pVLtA));
                    g2.draw(new Line2D.Double(pBFL, pABR));
                    g2.draw(new Line2D.Double(pBFL, pKL));
                    GeneralPath path = new GeneralPath();
                    path.moveTo(pBR.getX(), pBR.getY());
                    path.lineTo(pBPR.getX(), pBPR.getY());
                    path.quadTo(pBMR.getX(), pBMR.getY(), pBFR.getX(), pBFR.getY());
                    path.lineTo(pVL.getX(), pVL.getY());
                    g2.draw(path);
                    if (state != Turnout.CLOSED) {  // unknown or diverting path
                        path = new GeneralPath();
                        path.moveTo(pBPL.getX(), pBPL.getY());
                        path.quadTo(pBML.getX(), pBML.getY(), pBFL.getX(), pBFL.getY());
                        g2.draw(path);
//                         g2.draw(new Line2D.Double(pBSR, pBFSR));
                    } else {
                        g2.draw(new Line2D.Double(pBPR, pBFL));
                        path = new GeneralPath();
                        path.moveTo(pBSL.getX(), pBSL.getY());
                        path.quadTo(pBML.getX(), pBML.getY(), pBFS.getX(), pBFS.getY());
//                         g2.draw(path);
                    }
                }
                if (isMain == mainlineC) {
                    g2.draw(new Line2D.Double(pCR, pCDR));
                    g2.draw(new Line2D.Double(pKRtB, pVLtD));
                    g2.draw(new Line2D.Double(pCFR, pCDL));
                    g2.draw(new Line2D.Double(pCFR, pKR));
                    GeneralPath path = new GeneralPath();
                    path.moveTo(pCL.getX(), pCL.getY());
                    path.lineTo(pCPL.getX(), pCPL.getY());
                    path.quadTo(pCML.getX(), pCML.getY(), pCFL.getX(), pCFL.getY());
                    path.lineTo(pVL.getX(), pVL.getY());
                    g2.draw(path);
                    if (state != Turnout.CLOSED) {  // unknown or diverting path
                        path = new GeneralPath();
                        path.moveTo(pCPR.getX(), pCPR.getY());
                        path.quadTo(pCMR.getX(), pCMR.getY(), pCFR.getX(), pCFR.getY());
                        g2.draw(path);
//                         g2.draw(new Line2D.Double(pCSL, pCFSL));
                    } else {
                        g2.draw(new Line2D.Double(pCPL, pCFR));
                        path = new GeneralPath();
                        path.moveTo(pCSR.getX(), pCSR.getY());
                        path.quadTo(pCMR.getX(), pCMR.getY(), pCFS.getX(), pCFS.getY());
//                         g2.draw(path);
                    }
                }
                if (isMain == mainlineD) {
                    g2.draw(new Line2D.Double(pCDR, pDR));
                    g2.draw(new Line2D.Double(pKRtA, pVRtC));
                    g2.draw(new Line2D.Double(pDFR, pCDL));
                    g2.draw(new Line2D.Double(pDFR, pKR));
                    GeneralPath path = new GeneralPath();
                    path.moveTo(pDL.getX(), pDL.getY());
                    path.lineTo(pDPL.getX(), pDPL.getY());
                    path.quadTo(pDML.getX(), pDML.getY(), pDFL.getX(), pDFL.getY());
                    path.lineTo(pVR.getX(), pVR.getY());
                    g2.draw(path);
                    if (state != Turnout.CLOSED) {  // unknown or diverting path
                        path = new GeneralPath();
                        path.moveTo(pDPR.getX(), pDPR.getY());
                        path.quadTo(pDMR.getX(), pDMR.getY(), pDFR.getX(), pDFR.getY());
                        g2.draw(path);
//                         g2.draw(new Line2D.Double(pDSL, pDFSL));
                    } else {
                        g2.draw(new Line2D.Double(pDPL, pDFR));
                        path = new GeneralPath();
                        path.moveTo(pDSR.getX(), pDSR.getY());
                        path.quadTo(pDMR.getX(), pDMR.getY(), pDFS.getX(), pDFS.getY());
//                         g2.draw(path);
                    }
                }
                break;
            }   // case DOUBLE_XOVER

            case RH_XOVER: {
                // A, B, C, D end points (left and right)
                Point2D vAB = MathUtil.normalize(MathUtil.subtract(pB, pA), railDisplacement);
                double dirAB_DEG = MathUtil.computeAngleDEG(vAB);
                Point2D vABo = MathUtil.orthogonal(MathUtil.normalize(vAB, railDisplacement));
                pAL = MathUtil.subtract(pA, vABo);
                pAR = MathUtil.add(pA, vABo);
                pBL = MathUtil.subtract(pB, vABo);
                pBR = MathUtil.add(pB, vABo);
                Point2D vCD = MathUtil.normalize(MathUtil.subtract(pD, pC), railDisplacement);
                Point2D vCDo = MathUtil.orthogonal(MathUtil.normalize(vCD, railDisplacement));
                pCL = MathUtil.add(pC, vCDo);
                pCR = MathUtil.subtract(pC, vCDo);
                Point2D pDL = MathUtil.add(pD, vCDo);
                Point2D pDR = MathUtil.subtract(pD, vCDo);

                // AB and CD mid points
                Point2D pABM = MathUtil.midPoint(pA, pB);
                Point2D pABL = MathUtil.subtract(pABM, vABo);
                Point2D pABR = MathUtil.add(pABM, vABo);
                Point2D pCDM = MathUtil.midPoint(pC, pD);
                Point2D pCDL = MathUtil.subtract(pCDM, vABo);
                Point2D pCDR = MathUtil.add(pCDM, vABo);

                // directions
                Point2D vAC = MathUtil.normalize(MathUtil.subtract(pCDM, pABM), railDisplacement);
                Point2D vACo = MathUtil.orthogonal(MathUtil.normalize(vAC, railDisplacement));
                double dirAC_DEG = MathUtil.computeAngleDEG(vAC);
                double deltaBAC_DEG = MathUtil.absDiffAngleDEG(dirAB_DEG, dirAC_DEG);
                double deltaBAC_RAD = Math.toRadians(deltaBAC_DEG);

                // AC mid points
                Point2D pACL = MathUtil.subtract(pM, vACo);
                Point2D pACR = MathUtil.add(pM, vACo);

                // frogs
                hypotF = railDisplacement / Math.sin(deltaBAC_RAD / 2.0);
                Point2D vF = MathUtil.normalize(MathUtil.add(vAB, vAC), hypotF);
                Point2D pABF = MathUtil.add(pABM, vF);
                Point2D pCDF = MathUtil.subtract(pCDM, vF);

                // frog primes
                Point2D pABFP = MathUtil.add(MathUtil.add(pABF, vACo), vACo);
                Point2D pCDFP = MathUtil.subtract(MathUtil.subtract(pCDF, vACo), vACo);

                // end of switch rails (closed)
                Point2D vABF = MathUtil.normalize(vAB, hypotF);
                pAP = MathUtil.subtract(pABM, vABF);
                pAPL = MathUtil.subtract(pAP, vABo);
                pAPR = MathUtil.add(pAP, vABo);
                Point2D pCP = MathUtil.add(pCDM, vABF);
                Point2D pCPL = MathUtil.add(pCP, vCDo);
                Point2D pCPR = MathUtil.subtract(pCP, vCDo);

                // end of switch rails (open)
                Point2D vS = MathUtil.normalize(vAB, 2.0);
                Point2D vSo = MathUtil.orthogonal(vS);
                Point2D pASL = MathUtil.add(pAPL, vSo);
                // Point2D pASR = MathUtil.subtract(pAPR, vSo);
                // Point2D pCSL = MathUtil.add(pCPL, vSo);
                Point2D pCSR = MathUtil.subtract(pCPR, vSo);

                // end of switch rails (open at frogs)
                Point2D pABFS = MathUtil.subtract(pABF, vSo);
                // Point2D pABFSP = MathUtil.subtract(pABF, vS);
                Point2D pCDFS = MathUtil.add(pCDF, vSo);
                // Point2D pCDFSP = MathUtil.add(pCDF, vS);

                if (isMain == mainlineA) {
                    g2.draw(new Line2D.Double(pAL, pABL));
                    GeneralPath path = new GeneralPath();
                    path.moveTo(pAR.getX(), pAR.getY());
                    path.lineTo(pAPR.getX(), pAPR.getY());
                    path.quadTo(pABR.getX(), pABR.getY(), pABFP.getX(), pABFP.getY());
                    path.lineTo(pACR.getX(), pACR.getY());
                    g2.draw(path);
                    g2.draw(new Line2D.Double(pABF, pACL));
                    if (state != Turnout.CLOSED) {  // unknown or diverting path
                        path = new GeneralPath();
                        path.moveTo(pAPL.getX(), pAPL.getY());
                        path.quadTo(pABL.getX(), pABL.getY(), pABF.getX(), pABF.getY());
                        g2.draw(path);
//                         g2.draw(new Line2D.Double(pASR, pABFSP));
                    } else {                        // continuing path
                        g2.draw(new Line2D.Double(pAPR, pABF));
                        path = new GeneralPath();
                        path.moveTo(pASL.getX(), pASL.getY());
                        path.quadTo(pABL.getX(), pABL.getY(), pABFS.getX(), pABFS.getY());
//                         g2.draw(path);
                    }
                }
                if (isMain == mainlineB) {
                    g2.draw(new Line2D.Double(pABL, pBL));
                    g2.draw(new Line2D.Double(pABF, pBR));
                }
                if (isMain == mainlineC) {
                    g2.draw(new Line2D.Double(pCR, pCDR));
                    GeneralPath path = new GeneralPath();
                    path.moveTo(pCL.getX(), pCL.getY());
                    path.lineTo(pCPL.getX(), pCPL.getY());
                    path.quadTo(pCDL.getX(), pCDL.getY(), pCDFP.getX(), pCDFP.getY());
                    path.lineTo(pACL.getX(), pACL.getY());
                    g2.draw(path);
                    g2.draw(new Line2D.Double(pCDF, pACR));
                    if (state != Turnout.CLOSED) {  // unknown or diverting path
                        path = new GeneralPath();
                        path.moveTo(pCPR.getX(), pCPR.getY());
                        path.quadTo(pCDR.getX(), pCDR.getY(), pCDF.getX(), pCDF.getY());
                        g2.draw(path);
//                         g2.draw(new Line2D.Double(pCSL, pCDFSP));
                    } else {                        // continuing path
                        g2.draw(new Line2D.Double(pCPL, pCDF));
                        path = new GeneralPath();
                        path.moveTo(pCSR.getX(), pCSR.getY());
                        path.quadTo(pCDR.getX(), pCDR.getY(), pCDFS.getX(), pCDFS.getY());
//                         g2.draw(path);
                    }
                }
                if (isMain == mainlineD) {
                    g2.draw(new Line2D.Double(pCDR, pDR));
                    g2.draw(new Line2D.Double(pCDF, pDL));
                }
                break;
            }   // case RH_XOVER

            case LH_XOVER: {
                // B, A, D, C end points (left and right)
                Point2D vBA = MathUtil.normalize(MathUtil.subtract(pA, pB), railDisplacement);
                double dirBA_DEG = MathUtil.computeAngleDEG(vBA);
                Point2D vBAo = MathUtil.orthogonal(MathUtil.normalize(vBA, railDisplacement));
                pBL = MathUtil.add(pB, vBAo);
                pBR = MathUtil.subtract(pB, vBAo);
                pAL = MathUtil.add(pA, vBAo);
                pAR = MathUtil.subtract(pA, vBAo);
                Point2D vDC = MathUtil.normalize(MathUtil.subtract(pC, pD), railDisplacement);
                Point2D vDCo = MathUtil.orthogonal(MathUtil.normalize(vDC, railDisplacement));
                Point2D pDL = MathUtil.subtract(pD, vDCo);
                Point2D pDR = MathUtil.add(pD, vDCo);
                pCL = MathUtil.subtract(pC, vDCo);
                pCR = MathUtil.add(pC, vDCo);

                // BA and DC mid points
                Point2D pBAM = MathUtil.midPoint(pB, pA);
                Point2D pBAL = MathUtil.add(pBAM, vBAo);
                Point2D pBAR = MathUtil.subtract(pBAM, vBAo);
                Point2D pDCM = MathUtil.midPoint(pD, pC);
                Point2D pDCL = MathUtil.add(pDCM, vBAo);
                Point2D pDCR = MathUtil.subtract(pDCM, vBAo);

                // directions
                Point2D vBD = MathUtil.normalize(MathUtil.subtract(pDCM, pBAM), railDisplacement);
                Point2D vBDo = MathUtil.orthogonal(MathUtil.normalize(vBD, railDisplacement));
                double dirBD_DEG = MathUtil.computeAngleDEG(vBD);
                double deltaABD_DEG = MathUtil.absDiffAngleDEG(dirBA_DEG, dirBD_DEG);
                double deltaABD_RAD = Math.toRadians(deltaABD_DEG);

                // BD mid points
                Point2D pBDL = MathUtil.add(pM, vBDo);
                Point2D pBDR = MathUtil.subtract(pM, vBDo);

                // frogs
                hypotF = railDisplacement / Math.sin(deltaABD_RAD / 2.0);
                Point2D vF = MathUtil.normalize(MathUtil.add(vBA, vBD), hypotF);
                Point2D pBFL = MathUtil.add(pBAM, vF);
                Point2D pBF = MathUtil.subtract(pBFL, vBDo);
                Point2D pBFR = MathUtil.subtract(pBF, vBDo);
                Point2D pDFR = MathUtil.subtract(pDCM, vF);
                Point2D pDF = MathUtil.add(pDFR, vBDo);
                Point2D pDFL = MathUtil.add(pDF, vBDo);

                // end of switch rails (closed)
                Point2D vBAF = MathUtil.normalize(vBA, hypotF);
                Point2D pBP = MathUtil.subtract(pBAM, vBAF);
                Point2D pBPL = MathUtil.add(pBP, vBAo);
                Point2D pBPR = MathUtil.subtract(pBP, vBAo);
                Point2D pDP = MathUtil.add(pDCM, vBAF);
                Point2D pDPL = MathUtil.subtract(pDP, vDCo);
                Point2D pDPR = MathUtil.add(pDP, vDCo);

                // end of switch rails (open)
                Point2D vS = MathUtil.normalize(vBA, 2.0);
                Point2D vSo = MathUtil.orthogonal(vS);
                Point2D pBSL = MathUtil.subtract(pBPL, vSo);
                // Point2D pBSR = MathUtil.add(pBPR, vSo);
                // Point2D pDSL = MathUtil.subtract(pDPL, vSo);
                Point2D pDSR = MathUtil.add(pDPR, vSo);

                // end of switch rails (open at frogs)
                Point2D pBAFS = MathUtil.add(pBFL, vSo);
                // Point2D pBAFSP = MathUtil.subtract(pBFL, vS);
                Point2D pDCFS = MathUtil.subtract(pDFR, vSo);
                // Point2D pDCFSP = MathUtil.add(pDFR, vS);

                if (isMain == mainlineA) {
                    g2.draw(new Line2D.Double(pBAL, pAL));
                    g2.draw(new Line2D.Double(pBFL, pAR));
                }
                if (isMain == mainlineB) {
                    g2.draw(new Line2D.Double(pBL, pBAL));
                    GeneralPath path = new GeneralPath();
                    path.moveTo(pBR.getX(), pBR.getY());
                    path.lineTo(pBPR.getX(), pBPR.getY());
                    path.quadTo(pBAR.getX(), pBAR.getY(), pBFR.getX(), pBFR.getY());
                    path.lineTo(pBDR.getX(), pBDR.getY());
                    g2.draw(path);
                    g2.draw(new Line2D.Double(pBFL, pBDL));
                    if (state != Turnout.CLOSED) {  // unknown or diverting path
                        path = new GeneralPath();
                        path.moveTo(pBPL.getX(), pBPL.getY());
                        path.quadTo(pBAL.getX(), pBAL.getY(), pBFL.getX(), pBFL.getY());
                        g2.draw(path);
//                         g2.draw(new Line2D.Double(pBSR, pBAFSP));
                    } else {                        // continuing path
                        g2.draw(new Line2D.Double(pBPR, pBFL));
                        path = new GeneralPath();
                        path.moveTo(pBSL.getX(), pBSL.getY());
                        path.quadTo(pBAL.getX(), pBAL.getY(), pBAFS.getX(), pBAFS.getY());
//                         g2.draw(path);
                    }
                }
                if (isMain == mainlineC) {
                    g2.draw(new Line2D.Double(pDCR, pCR));
                    g2.draw(new Line2D.Double(pDFR, pCL));
                }
                if (isMain == mainlineD) {
                    g2.draw(new Line2D.Double(pDR, pDCR));
                    GeneralPath path = new GeneralPath();
                    path.moveTo(pDL.getX(), pDL.getY());
                    path.lineTo(pDPL.getX(), pDPL.getY());
                    path.quadTo(pDCL.getX(), pDCL.getY(), pDFL.getX(), pDFL.getY());
                    path.lineTo(pBDL.getX(), pBDL.getY());
                    g2.draw(path);
                    g2.draw(new Line2D.Double(pDFR, pBDR));
                    if (state != Turnout.CLOSED) {  // unknown or diverting path
                        path = new GeneralPath();
                        path.moveTo(pDPR.getX(), pDPR.getY());
                        path.quadTo(pDCR.getX(), pDCR.getY(), pDFR.getX(), pDFR.getY());
                        g2.draw(path);
//                         g2.draw(new Line2D.Double(pDSL, pDCFSP));
                    } else {                        // continuing path
                        g2.draw(new Line2D.Double(pDPL, pDFR));
                        path = new GeneralPath();
                        path.moveTo(pDSR.getX(), pDSR.getY());
                        path.quadTo(pDCR.getX(), pDCR.getY(), pDCFS.getX(), pDCFS.getY());
//                         g2.draw(path);
                    }
                }
                break;
            }   // case LH_XOVER
            case SINGLE_SLIP:
            case DOUBLE_SLIP: {
                log.error("slips should be being drawn by LayoutSlip sub-class");
                break;
            }
            default: {
                // this should never happen... but...
                log.error("Unknown turnout type: {}", type);
                break;
            }
        }
    }   // draw2

    /**
     * {@inheritDoc}
     */
    @Override
    protected void highlightUnconnected(Graphics2D g2, int specificType) {
        if (((specificType == NONE) || (specificType == TURNOUT_A))
                && (getConnectA() == null)) {
            g2.fill(layoutEditor.trackControlCircleAt(getCoordsA()));
        }

        if (((specificType == NONE) || (specificType == TURNOUT_B))
                && (getConnectB() == null)) {
            g2.fill(layoutEditor.trackControlCircleAt(getCoordsB()));
        }

        if (((specificType == NONE) || (specificType == TURNOUT_C))
                && (getConnectC() == null)) {
            g2.fill(layoutEditor.trackControlCircleAt(getCoordsC()));
        }
        if (isTurnoutTypeXover()) {
            if (((specificType == NONE) || (specificType == TURNOUT_D))
                    && (getConnectD() == null)) {
                g2.fill(layoutEditor.trackControlCircleAt(getCoordsD()));
            }
        }
    }

    @Override
    protected void drawTurnoutControls(Graphics2D g2) {
        if (!disabled && !(disableWhenOccupied && isOccupied())) {
            Color foregroundColor = g2.getColor();
            // if turnout is thrown...
            if (getState() == Turnout.THROWN) {
                // ...then switch to background color
                g2.setColor(g2.getBackground());
            }
            if (layoutEditor.isTurnoutFillControlCircles()) {
                g2.fill(layoutEditor.trackControlCircleAt(center));
            } else {
                g2.draw(layoutEditor.trackControlCircleAt(center));
            }
            // if turnout is thrown...
            if (getState() == Turnout.THROWN) {
                // ... then restore foreground color
                g2.setColor(foregroundColor);
            }
        }
    }

    @Override
    protected void drawEditControls(Graphics2D g2) {
        Point2D pt = getCoordsA();
        if (isTurnoutTypeXover() || isTurnoutTypeSlip()) {
            if (getConnectA() == null) {
                g2.setColor(Color.magenta);
            } else {
                g2.setColor(Color.blue);
            }
        } else {
            if (getConnectA() == null) {
                g2.setColor(Color.red);
            } else {
                g2.setColor(Color.green);
            }
        }
        g2.draw(layoutEditor.trackEditControlRectAt(pt));

        pt = getCoordsB();
        if (getConnectB() == null) {
            g2.setColor(Color.red);
        } else {
            g2.setColor(Color.green);
        }
        g2.draw(layoutEditor.trackEditControlRectAt(pt));

        pt = getCoordsC();
        if (getConnectC() == null) {
            g2.setColor(Color.red);
        } else {
            g2.setColor(Color.green);
        }
        g2.draw(layoutEditor.trackEditControlRectAt(pt));

        if (isTurnoutTypeXover() || isTurnoutTypeSlip()) {
            pt = getCoordsD();
            if (getConnectD() == null) {
                g2.setColor(Color.red);
            } else {
                g2.setColor(Color.green);
            }
            g2.draw(layoutEditor.trackEditControlRectAt(pt));
        }
    }

    /*
     * Used by ConnectivityUtil to determine the turnout state necessary to get
     * from prevLayoutBlock ==> currLayoutBlock ==> nextLayoutBlock
     */
    protected int getConnectivityStateForLayoutBlocks(
            LayoutBlock currLayoutBlock,
            LayoutBlock prevLayoutBlock,
            LayoutBlock nextLayoutBlock,
            boolean suppress) {
        int result = UNKNOWN;

        LayoutBlock layoutBlockA = ((TrackSegment) getConnectA()).getLayoutBlock();
        LayoutBlock layoutBlockB = ((TrackSegment) getConnectB()).getLayoutBlock();
        LayoutBlock layoutBlockC = ((TrackSegment) getConnectC()).getLayoutBlock();
        //TODO: Determine if this should be being used
        //LayoutBlock layoutBlockD = ((TrackSegment) getConnectD()).getLayoutBlock();

        int tTyp = getTurnoutType();
        switch (tTyp) {
            case LayoutTurnout.RH_TURNOUT:
            case LayoutTurnout.LH_TURNOUT:
            case LayoutTurnout.WYE_TURNOUT: {
                if (layoutBlockA == currLayoutBlock) {
                    if ((layoutBlockC == nextLayoutBlock) || (layoutBlockC == prevLayoutBlock)) {
                        result = Turnout.THROWN;
                    } else if ((layoutBlockB == nextLayoutBlock) || (layoutBlockB == prevLayoutBlock)) {
                        result = Turnout.CLOSED;
                    } else if (layoutBlockB == currLayoutBlock) {
                        result = Turnout.CLOSED;
                    } else if (layoutBlockC == currLayoutBlock) {
                        result = Turnout.THROWN;
                    } else {
                        if (!suppress) {
                            log.error("Cannot determine turnout setting - {}", getTurnoutName());
                        }
                        result = Turnout.CLOSED;
                    }
                } else if (layoutBlockB == currLayoutBlock) {
                    result = Turnout.CLOSED;
                } else if (layoutBlockC == currLayoutBlock) {
                    result = Turnout.THROWN;
                } else {
                    if (!suppress) {
                        log.error("Cannot determine turnout setting for {}", getTurnoutName());
                    }
                    if (!suppress) {
                        log.error("lb {} nlb {} connect B {} connect C {}", currLayoutBlock, nextLayoutBlock, layoutBlockB, layoutBlockC);
                    }
                    result = Turnout.CLOSED;
                }
                break;
            }
            case LayoutTurnout.RH_XOVER:
            case LayoutTurnout.LH_XOVER:
            case LayoutTurnout.DOUBLE_XOVER: {
                if (getLayoutBlock() == currLayoutBlock) {
                    if ((tTyp != LayoutTurnout.LH_XOVER)
                            && ((getLayoutBlockC() == nextLayoutBlock)
                            || (getLayoutBlockC() == prevLayoutBlock))) {
                        result = Turnout.THROWN;
                    } else if ((getLayoutBlockB() == nextLayoutBlock) || (getLayoutBlockB() == prevLayoutBlock)) {
                        result = Turnout.CLOSED;
                    } else if (getLayoutBlockB() == currLayoutBlock) {
                        result = Turnout.CLOSED;
                    } else if ((tTyp != LayoutTurnout.LH_XOVER)
                            && (getLayoutBlockC() == currLayoutBlock)) {
                        result = Turnout.THROWN;
                    } else {
                        if (!suppress) {
                            log.error("Cannot determine turnout setting(A) - {}", getTurnoutName());
                        }
                        result = Turnout.CLOSED;
                    }
                } else if (getLayoutBlockB() == currLayoutBlock) {
                    if ((getLayoutBlock() == nextLayoutBlock) || (getLayoutBlock() == prevLayoutBlock)) {
                        result = Turnout.CLOSED;
                    } else if ((tTyp != LayoutTurnout.RH_XOVER)
                            && ((getLayoutBlockD() == nextLayoutBlock)
                            || (getLayoutBlockD() == prevLayoutBlock) || (getLayoutBlockD() == currLayoutBlock))) {
                        result = Turnout.THROWN;
                    } else {
                        if (!suppress) {
                            log.error("Cannot determine turnout setting(B) - {}", getTurnoutName());
                        }
                        result = Turnout.CLOSED;
                    }
                } else if (getLayoutBlockC() == currLayoutBlock) {
                    if ((tTyp != LayoutTurnout.LH_XOVER)
                            && ((getLayoutBlock() == nextLayoutBlock) || (getLayoutBlock() == prevLayoutBlock))) {
                        result = Turnout.THROWN;
                    } else if ((getLayoutBlockD() == nextLayoutBlock) || (getLayoutBlockD() == prevLayoutBlock) || (getLayoutBlockD() == currLayoutBlock)) {
                        result = Turnout.CLOSED;
                    } else if ((tTyp != LayoutTurnout.LH_XOVER)
                            && (getLayoutBlockD() == currLayoutBlock)) {
                        result = Turnout.THROWN;
                    } else {
                        if (!suppress) {
                            log.error("Cannot determine turnout setting(C) - {}", getTurnoutName());
                        }
                        result = Turnout.CLOSED;
                    }
                } else if (getLayoutBlockD() == currLayoutBlock) {
                    if ((getLayoutBlockC() == nextLayoutBlock) || (getLayoutBlockC() == prevLayoutBlock)) {
                        result = Turnout.CLOSED;
                    } else if ((tTyp != LayoutTurnout.RH_XOVER)
                            && ((getLayoutBlockB() == nextLayoutBlock) || (getLayoutBlockB() == prevLayoutBlock))) {
                        result = Turnout.THROWN;
                    } else {
                        if (!suppress) {
                            log.error("Cannot determine turnout setting(D) - {}", getTurnoutName());
                        }
                        result = Turnout.CLOSED;
                    }
                }
                break;
            }
            default: {
                log.warn("getTurnoutList() unknown tTyp: {}", tTyp);
                break;
            }
        }   // switch (tTyp)

        return result;
    }   // getConnectivityStateForLayoutBlocks


    /*
     * {@inheritDoc}
     */
    //TODO: on the cross-overs, check the internal boundary details.
    @Override
    public void reCheckBlockBoundary() {
        if (connectA == null && connectB == null && connectC == null) {
            if ((getTurnoutType() == RH_TURNOUT)
                    || (getTurnoutType() == LH_TURNOUT)
                    || (getTurnoutType() == WYE_TURNOUT)) {
                if (signalAMastNamed != null) {
                    removeSML(getSignalAMast());
                }
                if (signalBMastNamed != null) {
                    removeSML(getSignalBMast());
                }
                if (signalCMastNamed != null) {
                    removeSML(getSignalCMast());
                }
                signalAMastNamed = null;
                signalBMastNamed = null;
                signalCMastNamed = null;
                sensorANamed = null;
                sensorBNamed = null;
                sensorCNamed = null;
                return;
            } else if (isTurnoutTypeXover() && connectD == null) {
                if (signalAMastNamed != null) {
                    removeSML(getSignalAMast());
                }
                if (signalBMastNamed != null) {
                    removeSML(getSignalBMast());
                }
                if (signalCMastNamed != null) {
                    removeSML(getSignalCMast());
                }
                if (signalDMastNamed != null) {
                    removeSML(getSignalDMast());
                }
                signalAMastNamed = null;
                signalBMastNamed = null;
                signalCMastNamed = null;
                signalDMastNamed = null;
                sensorANamed = null;
                sensorBNamed = null;
                sensorCNamed = null;
                sensorDNamed = null;
                return;
            }
        }

        if (connectA == null || connectB == null || connectC == null) {
            //could still be in the process of rebuilding.
            return;
        } else if ((connectD == null) && isTurnoutTypeXover()) {
            //could still be in the process of rebuilding.
            return;
        }

        TrackSegment trkA;
        TrackSegment trkB;
        TrackSegment trkC;
        TrackSegment trkD;

        if (connectA instanceof TrackSegment) {
            trkA = (TrackSegment) connectA;
            if (trkA.getLayoutBlock() == getLayoutBlock()) {
                if (signalAMastNamed != null) {
                    removeSML(getSignalAMast());
                }
                signalAMastNamed = null;
                sensorANamed = null;
            }
        }
        if (connectB instanceof TrackSegment) {
            trkB = (TrackSegment) connectB;
            if (trkB.getLayoutBlock() == getLayoutBlock() || trkB.getLayoutBlock() == getLayoutBlockB()) {
                if (signalBMastNamed != null) {
                    removeSML(getSignalBMast());
                }
                signalBMastNamed = null;
                sensorBNamed = null;

            }
        }
        if (connectC instanceof TrackSegment) {
            trkC = (TrackSegment) connectC;
            if (trkC.getLayoutBlock() == getLayoutBlock()
                    || trkC.getLayoutBlock() == getLayoutBlockB()
                    || trkC.getLayoutBlock() == getLayoutBlockC()) {
                if (signalCMastNamed != null) {
                    removeSML(getSignalCMast());
                }
                signalCMastNamed = null;
                sensorCNamed = null;

            }
        }
        if (connectD != null && connectD instanceof TrackSegment
                && isTurnoutTypeXover()) {
            trkD = (TrackSegment) connectD;
            if (trkD.getLayoutBlock() == getLayoutBlock()
                    || trkD.getLayoutBlock() == getLayoutBlockB()
                    || trkD.getLayoutBlock() == getLayoutBlockC()
                    || trkD.getLayoutBlock() == getLayoutBlockD()) {
                if (signalDMastNamed != null) {
                    removeSML(getSignalDMast());
                }
                signalDMastNamed = null;
                sensorDNamed = null;
            }
        }
    }   // reCheckBlockBoundary

    /*
     * {@inheritDoc}
     */
    @Override
    protected List<LayoutConnectivity> getLayoutConnectivity() {
        List<LayoutConnectivity> results = new ArrayList<>();

        LayoutConnectivity lc = null;

        LayoutBlock lbA = getLayoutBlock(), lbB = getLayoutBlockB(), lbC = getLayoutBlockC(), lbD = getLayoutBlockD();
        if ((getTurnoutType() >= LayoutTurnout.DOUBLE_XOVER) && (lbA != null)) {
            // have a crossover turnout with at least one block, check for multiple blocks
            if ((lbA != lbB) || (lbA != lbC) || (lbA != lbD)) {
                // have multiple blocks and therefore internal block boundaries
                if (lbA != lbB) {
                    // have a AB block boundary, create a LayoutConnectivity
                    log.debug("Block boundary  ('{}'<->'{}') found at {}", lbA, lbB, this);
                    lc = new LayoutConnectivity(lbA, lbB);
                    lc.setXoverBoundary(this, LayoutConnectivity.XOVER_BOUNDARY_AB);
                    lc.setDirection(Path.computeDirection(getCoordsA(), getCoordsB()));
                    results.add(lc);
                }
                if ((getTurnoutType() != LayoutTurnout.LH_XOVER) && (lbA != lbC)) {
                    // have a AC block boundary, create a LayoutConnectivity
                    log.debug("Block boundary  ('{}'<->'{}') found at {}", lbA, lbC, this);
                    lc = new LayoutConnectivity(lbA, lbC);
                    lc.setXoverBoundary(this, LayoutConnectivity.XOVER_BOUNDARY_AC);
                    lc.setDirection(Path.computeDirection(getCoordsA(), getCoordsC()));
                    results.add(lc);
                }
                if (lbC != lbD) {
                    // have a CD block boundary, create a LayoutConnectivity
                    log.debug("Block boundary  ('{}'<->'{}') found at {}", lbC, lbD, this);
                    lc = new LayoutConnectivity(lbC, lbD);
                    lc.setXoverBoundary(this, LayoutConnectivity.XOVER_BOUNDARY_CD);
                    lc.setDirection(Path.computeDirection(getCoordsC(), getCoordsD()));
                    results.add(lc);
                }
                if ((getTurnoutType() != LayoutTurnout.RH_XOVER) && (lbB != lbD)) {
                    // have a BD block boundary, create a LayoutConnectivity
                    log.debug("Block boundary  ('{}'<->'{}') found at {}", lbB, lbD, this);
                    lc = new LayoutConnectivity(lbB, lbD);
                    lc.setXoverBoundary(this, LayoutConnectivity.XOVER_BOUNDARY_BD);
                    lc.setDirection(Path.computeDirection(getCoordsB(), getCoordsD()));
                    results.add(lc);
                }
            }
        }
        return results;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @Nonnull
    List<Integer> checkForFreeConnections() {
        List<Integer> result = new ArrayList<>();

        //check the A connection point
        if (getConnectA() == null) {
            result.add(TURNOUT_A);
        }

        //check the B connection point
        if (getConnectB() == null) {
            result.add(TURNOUT_B);
        }

        //check the C connection point
        if (getConnectC() == null) {
            result.add(TURNOUT_C);
        }

        //check the D connection point
        if (isTurnoutTypeXover()) {
            if (getConnectD() == null) {
                result.add(TURNOUT_D);
            }
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean checkForUnAssignedBlocks() {
        // because getLayoutBlock[BCD] will return block [A] if they're null
        // we only need to test block [A]
        return (getLayoutBlock() != null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void checkForNonContiguousBlocks(
            @Nonnull HashMap<String, List<Set<String>>> blockNamesToTrackNameSetsMap) {
        /*
         * For each (non-null) blocks of this track do:
         * #1) If it's got an entry in the blockNamesToTrackNameSetMap then
         * #2) If this track is already in the TrackNameSet for this block
         *     then return (done!)
         * #3) else add a new set (with this block/track) to
         *     blockNamesToTrackNameSetMap and check all the connections in this
         *     block (by calling the 2nd method below)
         * <p>
         *     Basically, we're maintaining contiguous track sets for each block found
         *     (in blockNamesToTrackNameSetMap)
         */

        // We're only using a map here because it's convient to
        // use it to pair up blocks and connections
        Map<LayoutTrack, String> blocksAndTracksMap = new HashMap<>();
        if (connectA != null) {
            blocksAndTracksMap.put(connectA, getBlockName());
        }
        if (connectB != null) {
            blocksAndTracksMap.put(connectB, getBlockBName());
        }
        if (connectC != null) {
            blocksAndTracksMap.put(connectC, getBlockCName());
        }
        if (isTurnoutTypeXover() || isTurnoutTypeSlip()) {
            if (connectD != null) {
                blocksAndTracksMap.put(connectD, getBlockDName());
            }
        }
        List<Set<String>> TrackNameSets = null;
        Set<String> TrackNameSet = null;
        for (Map.Entry<LayoutTrack, String> entry : blocksAndTracksMap.entrySet()) {
            LayoutTrack theConnect = entry.getKey();
            String theBlockName = entry.getValue();

            TrackNameSet = null;    // assume not found (pessimist!)
            TrackNameSets = blockNamesToTrackNameSetsMap.get(theBlockName);
            if (TrackNameSets != null) { // (#1)
                for (Set<String> checkTrackNameSet : TrackNameSets) {
                    if (checkTrackNameSet.contains(getName())) { // (#2)
                        TrackNameSet = checkTrackNameSet;
                        break;
                    }
                }
            } else {    // (#3)
                log.debug("*New block ('{}') trackNameSets", theBlockName);
                TrackNameSets = new ArrayList<>();
                blockNamesToTrackNameSetsMap.put(theBlockName, TrackNameSets);
            }
            if (TrackNameSet == null) {
                TrackNameSet = new LinkedHashSet<>();
                TrackNameSets.add(TrackNameSet);
            }
            if (TrackNameSet.add(getName())) {
                log.debug("*    Add track '{}' to trackNameSet for block '{}'", getName(), theBlockName);
            }
            theConnect.collectContiguousTracksNamesInBlockNamed(theBlockName, TrackNameSet);
        }
    }   // collectContiguousTracksNamesInBlockNamed

    /**
     * {@inheritDoc}
     */
    @Override
    public void collectContiguousTracksNamesInBlockNamed(
            @Nonnull String blockName,
            @Nonnull Set<String> TrackNameSet) {
        if (!TrackNameSet.contains(getName())) {

            // create list of our connects
            List<LayoutTrack> connects = new ArrayList<>();
            if (getBlockName().equals(blockName)
                    && (connectA != null)) {
                connects.add(connectA);
            }
            if (getBlockBName().equals(blockName)
                    && (connectB != null)) {
                connects.add(connectB);
            }
            if (getBlockCName().equals(blockName)
                    && (connectC != null)) {
                connects.add(connectC);
            }
            if (isTurnoutTypeXover() || isTurnoutTypeSlip()) {
                if (getBlockDName().equals(blockName)
                        && (connectD != null)) {
                    connects.add(connectD);
                }
            }

            for (LayoutTrack connect : connects) {
                // if we are added to the TrackNameSet
                if (TrackNameSet.add(getName())) {
                    log.debug("*    Add track '{}' for block '{}'", getName(), blockName);
                }
                // it's time to play... flood your neighbour!
                connect.collectContiguousTracksNamesInBlockNamed(blockName, TrackNameSet);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAllLayoutBlocks(LayoutBlock layoutBlock) {
        setLayoutBlock(layoutBlock);
        if (isTurnoutTypeXover() || isTurnoutTypeSlip()) {
            setLayoutBlockB(layoutBlock);
            setLayoutBlockC(layoutBlock);
            setLayoutBlockD(layoutBlock);
        }
    }

    private static class AbstractActionImpl extends AbstractAction {

        private final String blockName;
        private final LayoutBlock layoutBlock;

        public AbstractActionImpl(String name, String blockName, LayoutBlock layoutBlock) {
            super(name);
            this.blockName = blockName;
            this.layoutBlock = layoutBlock;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            AbstractAction routeTableAction = new LayoutBlockRouteTableAction(blockName, layoutBlock);
            routeTableAction.actionPerformed(e);
        }
    }

    private final static Logger log = LoggerFactory.getLogger(LayoutTurnout.class);
}
