package jmri.jmrit.display.layoutEditor;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
 * <P>
 * Six types are supported: right-hand, left-hand, wye, double crossover,
 * right-handed single crossover, and left-handed single crossover. Note that
 * double-slip turnouts can be handled as two turnouts, throat to throat, and
 * three-way turnouts can be handles as two turnouts, left-hand and right-hand,
 * arranged throat to continuing route.
 * <P>
 * A LayoutTurnout has three or four connection points, designated A, B, C, and
 * D. For right-handed or left-handed turnouts, A corresponds to the throat. At
 * the crossing, A-B (and C-D for crossovers) is a straight segment (continuing
 * route). A-C (and B-D for crossovers) is the diverging route. B-C (and A-D for
 * crossovers) is an illegal condition.
 * <P>
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
 * <P>
 * A LayoutTurnout carries Block information. For right-handed, left-handed, and
 * wye turnouts, the entire turnout is in one block,however, a block border may
 * occur at any connection (A,B,C,D). For a double crossover turnout, up to four
 * blocks may be assigned, one for each connection point, but if only one block
 * is assigned, that block applies to the entire turnout.
 * <P>
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
 * <P>
 * When LayoutTurnouts are first created, a rotation (degrees) is provided. For
 * 0.0 rotation, the turnout lies on the east-west line with A facing east.
 * Rotations are performed in a clockwise direction.
 * <P>
 * When LayoutTurnouts are first created, there are no connections. Block
 * information and connections may be added when available.
 * <P>
 * When a LayoutTurnout is first created, it is enabled for control of an
 * assigned actual turnout. Clicking on the turnout center point will toggle the
 * turnout. This can be disabled via the popup menu.
 * <P>
 * Signal Head names are saved here to keep track of where signals are.
 * LayoutTurnout only serves as a storage place for signal head names. The names
 * are placed here by tools, e.g., Set Signals at Turnout, and Set Signals at
 * Double Crossover.
 * <P>
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
 */
public class LayoutTurnout extends LayoutTrack {

    // defined constants - turnout types
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
    //private Turnout turnout = null;
    protected NamedBeanHandle<Turnout> namedTurnout = null;
    //Second turnout is used to either throw a second turnout in a cross over or if one turnout address is used to throw two physical ones
    protected NamedBeanHandle<Turnout> secondNamedTurnout = null;

    protected LayoutBlock block = null;
    protected LayoutBlock blockB = null;  // Xover - second block, if there is one
    protected LayoutBlock blockC = null;  // Xover - third block, if there is one
    protected LayoutBlock blockD = null;  // Xover - forth block, if there is one

    private java.beans.PropertyChangeListener mTurnoutListener = null;

    // persistent instances variables (saved between sessions)
    // these should be the system or user name of an existing physical turnout
    private String turnoutName = "";
    private String secondTurnoutName = "";
    private boolean secondTurnoutInverted = false;

    // default is package protected
    String blockName = "";  // name for block, if there is one
    String blockBName = "";  // Xover/slip - name for second block, if there is one
    String blockCName = "";  // Xover/slip - name for third block, if there is one
    String blockDName = "";  // Xover/slip - name for forth block, if there is one

    protected NamedBeanHandle<SignalHead> signalA1HeadNamed = null; // signal 1 (continuing) (throat for RH, LH, WYE)
    protected NamedBeanHandle<SignalHead> signalA2HeadNamed = null; // signal 2 (diverging) (throat for RH, LH, WYE)
    protected NamedBeanHandle<SignalHead> signalA3HeadNamed = null; // signal 3 (second diverging) (3-way turnouts only)
    protected NamedBeanHandle<SignalHead> signalB1HeadNamed = null; // continuing (RH, LH, WYE) signal 1 (double crossover)
    protected NamedBeanHandle<SignalHead> signalB2HeadNamed = null; // LH_Xover and double crossover only
    protected NamedBeanHandle<SignalHead> signalC1HeadNamed = null; // diverging (RH, LH, WYE) signal 1 (double crossover)
    protected NamedBeanHandle<SignalHead> signalC2HeadNamed = null; // RH_Xover and double crossover only
    protected NamedBeanHandle<SignalHead> signalD1HeadNamed = null; // single or double crossover only
    protected NamedBeanHandle<SignalHead> signalD2HeadNamed = null; // LH_Xover and double crossover only

    public static final int POINTA = 0x01;
    public static final int POINTA2 = 0x03;
    public static final int POINTA3 = 0x05;
    public static final int POINTB = 0x10;
    public static final int POINTB2 = 0x12;
    public static final int POINTC = 0x20;
    public static final int POINTC2 = 0x22;
    public static final int POINTD = 0x30;
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
        block = null;
        blockName = "";
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
            blockB = null;
            blockBName = "";
            blockC = null;
            blockCName = "";
            blockD = null;
            blockDName = "";
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
            blockB = null;
            blockBName = "";
            blockC = null;
            blockCName = "";
            blockD = null;
            blockDName = "";
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
            blockB = null;
            blockBName = "";
            blockC = null;
            blockCName = "";
            blockD = null;
            blockDName = "";
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

    public String getBlockName() {
        return blockName;
    }

    public String getBlockBName() {
        if ((blockBName == null) || (blockBName.isEmpty())) {
            if (getLayoutBlockB() != null) {
                blockBName = getLayoutBlockB().getId();
            }
        }
        return blockBName;
    }

    public String getBlockCName() {
        if ((blockCName == null) || (blockCName.isEmpty())) {
            if (getLayoutBlockC() != null) {
                blockCName = getLayoutBlockC().getId();
            }
        }
        return blockCName;
    }

    public String getBlockDName() {
        if ((blockDName == null) || (blockDName.isEmpty())) {
            if (getLayoutBlockD() != null) {
                blockDName = getLayoutBlockD().getId();
            }
        }
        return blockDName;
    }

    public SignalHead getSignalHead(int loc) {
        NamedBeanHandle<SignalHead> signalHead = null;
        switch (loc) {
            case POINTA:
                signalHead = signalA1HeadNamed;
                break;
            case POINTA2:
                signalHead = signalA2HeadNamed;
                break;
            case POINTA3:
                signalHead = signalA3HeadNamed;
                break;
            case POINTB:
                signalHead = signalB1HeadNamed;
                break;
            case POINTB2:
                signalHead = signalB2HeadNamed;
                break;
            case POINTC:
                signalHead = signalC1HeadNamed;
                break;
            case POINTC2:
                signalHead = signalC2HeadNamed;
                break;
            case POINTD:
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

    public String getSignalA1Name() {
        if (signalA1HeadNamed != null) {
            return signalA1HeadNamed.getName();
        }
        return "";
    }

    public void setSignalA1Name(@Nullable String signalHead) {
        if (signalHead == null || signalHead.isEmpty()) {
            signalA1HeadNamed = null;
            return;
        }

        SignalHead head = InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(signalHead);
        if (head != null) {
            signalA1HeadNamed = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(signalHead, head);
        } else {
            signalA1HeadNamed = null;
            log.error("Signal Head " + signalHead + " Not found for turnout " + getTurnoutName());
        }
    }

    public String getSignalA2Name() {
        if (signalA2HeadNamed != null) {
            return signalA2HeadNamed.getName();
        }
        return "";
    }

    public void setSignalA2Name(@Nullable String signalHead) {
        if (signalHead == null || signalHead.isEmpty()) {
            signalA2HeadNamed = null;
            return;
        }

        SignalHead head = InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(signalHead);
        if (head != null) {
            signalA2HeadNamed = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(signalHead, head);
        } else {
            signalA2HeadNamed = null;
            log.error("Signal Head " + signalHead + " Not found for turnout " + getTurnoutName());
        }
    }

    public String getSignalA3Name() {
        if (signalA3HeadNamed != null) {
            return signalA3HeadNamed.getName();
        }
        return "";
    }

    public void setSignalA3Name(@Nullable String signalHead) {
        if (signalHead == null || signalHead.isEmpty()) {
            signalA3HeadNamed = null;
            return;
        }

        SignalHead head = InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(signalHead);
        if (head != null) {
            signalA3HeadNamed = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(signalHead, head);
        } else {
            signalA3HeadNamed = null;
            log.error("Signal Head " + signalHead + " Not found for turnout " + getTurnoutName());
        }
    }

    public String getSignalB1Name() {
        if (signalB1HeadNamed != null) {
            return signalB1HeadNamed.getName();
        }
        return "";
    }

    public void setSignalB1Name(@Nullable String signalHead) {
        if (signalHead == null || signalHead.isEmpty()) {
            signalB1HeadNamed = null;
            return;
        }

        SignalHead head = InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(signalHead);
        if (head != null) {
            signalB1HeadNamed = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(signalHead, head);
        } else {
            signalB1HeadNamed = null;
            log.error("Signal Head " + signalHead + " Not found for turnout " + getTurnoutName());
        }
    }

    public String getSignalB2Name() {
        if (signalB2HeadNamed != null) {
            return signalB2HeadNamed.getName();
        }
        return "";
    }

    public void setSignalB2Name(@Nullable String signalHead) {
        if (signalHead == null || signalHead.isEmpty()) {
            signalB2HeadNamed = null;
            return;
        }

        SignalHead head = InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(signalHead);
        if (head != null) {
            signalB2HeadNamed = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(signalHead, head);
        } else {
            signalB2HeadNamed = null;
            log.error("Signal Head " + signalHead + " Not found for turnout " + getTurnoutName());
        }
    }

    public String getSignalC1Name() {
        if (signalC1HeadNamed != null) {
            return signalC1HeadNamed.getName();
        }
        return "";
    }

    public void setSignalC1Name(@Nullable String signalHead) {
        if (signalHead == null || signalHead.isEmpty()) {
            signalC1HeadNamed = null;
            return;
        }

        SignalHead head = InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(signalHead);
        if (head != null) {
            signalC1HeadNamed = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(signalHead, head);
        } else {
            signalC1HeadNamed = null;
            log.error("Signal Head " + signalHead + " Not found for turnout " + getTurnoutName());
        }
    }

    public String getSignalC2Name() {
        if (signalC2HeadNamed != null) {
            return signalC2HeadNamed.getName();
        }
        return "";
    }

    public void setSignalC2Name(@Nullable String signalHead) {
        if (signalHead == null || signalHead.isEmpty()) {
            signalC2HeadNamed = null;
            return;
        }

        SignalHead head = InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(signalHead);
        if (head != null) {
            signalC2HeadNamed = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(signalHead, head);
        } else {
            signalC2HeadNamed = null;
            log.error("Signal Head " + signalHead + " Not found for turnout " + getTurnoutName());
        }
    }

    public String getSignalD1Name() {
        if (signalD1HeadNamed != null) {
            return signalD1HeadNamed.getName();
        }
        return "";
    }

    public void setSignalD1Name(@Nullable String signalHead) {
        if (signalHead == null || signalHead.isEmpty()) {
            signalD1HeadNamed = null;
            return;
        }

        SignalHead head = InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(signalHead);
        if (head != null) {
            signalD1HeadNamed = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(signalHead, head);
        } else {
            signalD1HeadNamed = null;
            log.error("Signal Head " + signalHead + " Not found for turnout " + getTurnoutName());
        }
    }

    public String getSignalD2Name() {
        if (signalD2HeadNamed != null) {
            return signalD2HeadNamed.getName();
        }
        return "";
    }

    public void setSignalD2Name(@Nullable String signalHead) {
        if (signalHead == null || signalHead.isEmpty()) {
            signalD2HeadNamed = null;
            return;
        }

        SignalHead head = InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(signalHead);
        if (head != null) {
            signalD2HeadNamed = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(signalHead, head);
        } else {
            signalD2HeadNamed = null;
            log.error("Signal Head " + signalHead + " Not found for turnout " + getTurnoutName());
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
                return;
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
                return;
            }
        } else if (nb instanceof SignalHead) {
            if (nb.equals(getSignalHead(POINTA))) {
                setSignalA1Name(null);
            }
            if (nb.equals(getSignalHead(POINTA2))) {
                setSignalA2Name(null);
            }
            if (nb.equals(getSignalHead(POINTA3))) {
                setSignalA3Name(null);
            }
            if (nb.equals(getSignalHead(POINTB))) {
                setSignalB1Name(null);
            }
            if (nb.equals(getSignalHead(POINTB2))) {
                setSignalB2Name(null);
            }
            if (nb.equals(getSignalHead(POINTC))) {
                setSignalC1Name(null);
            }
            if (nb.equals(getSignalHead(POINTC2))) {
                setSignalC2Name(null);
            }
            if (nb.equals(getSignalHead(POINTD))) {
                setSignalD1Name(null);
            }
            if (nb.equals(getSignalHead(POINTD2))) {
                setSignalD2Name(null);
            }
        }
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

    public void setSignalAMast(@Nullable String signalMast) {
        if (signalMast == null || signalMast.isEmpty()) {
            signalAMastNamed = null;
            return;
        }

        SignalMast mast = InstanceManager.getDefault(jmri.SignalMastManager.class).getSignalMast(signalMast);
        if (mast != null) {
            signalAMastNamed = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(signalMast, mast);
        } else {
            signalAMastNamed = null;
            log.error("Signal Mast " + signalMast + " Not found for turnout " + getTurnoutName());
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

    public void setSignalBMast(@Nullable String signalMast) {
        if (signalMast == null || signalMast.isEmpty()) {
            signalBMastNamed = null;
            return;
        }

        SignalMast mast = InstanceManager.getDefault(jmri.SignalMastManager.class).getSignalMast(signalMast);
        if (mast != null) {
            signalBMastNamed = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(signalMast, mast);
        } else {
            signalBMastNamed = null;
            log.error("Signal Mast " + signalMast + " Not found for turnout " + getTurnoutName());
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

    public void setSignalCMast(@Nullable String signalMast) {
        if (signalMast == null || signalMast.isEmpty()) {
            signalCMastNamed = null;
            return;
        }

        SignalMast mast = InstanceManager.getDefault(jmri.SignalMastManager.class).getSignalMast(signalMast);
        if (mast != null) {
            signalCMastNamed = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(signalMast, mast);
        } else {
            log.error("Signal Mast " + signalMast + " Not found for turnout " + getTurnoutName());
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

    public void setSignalDMast(@Nullable String signalMast) {
        if (signalMast == null || signalMast.isEmpty()) {
            signalDMastNamed = null;
            return;
        }

        SignalMast mast = InstanceManager.getDefault(jmri.SignalMastManager.class).getSignalMast(signalMast);
        if (mast != null) {
            signalDMastNamed = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(signalMast, mast);
        } else {
            log.error("Signal Mast " + signalMast + " Not found for turnout " + getTurnoutName());
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

    public void setSensorA(@Nullable String sensorName) {
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

    public void setSensorB(@Nullable String sensorName) {
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

    public void setSensorC(@Nullable String sensorName) {
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

    public void setSensorD(@Nullable String sensorName) {
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

    public void setLinkedTurnoutName(@Nullable String s) {
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

    public void setTurnout(@Nullable String tName) {
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

    public void setSecondTurnout(@Nullable String tName) {
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
            activateTurnout();
        } else {
            secondTurnoutName = "";
            secondNamedTurnout = null;
        }
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
                log.error("Invalid Connection Type " + connectionType); //I18IN
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
            log.error("unexpected type of connection to layoutturnout - " + type);
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
                log.error("Invalid Connection Type " + connectionType); //I18IN
                throw new jmri.JmriException("Invalid Connection Type " + connectionType);
        }
    }

    public void setConnectA(LayoutTrack o, int type) {
        connectA = o;
        if ((type != TRACK) && (type != NONE)) {
            log.error("unexpected type of A connection to layoutturnout - " + type);
        }
    }

    public void setConnectB(LayoutTrack o, int type) {
        connectB = o;
        if ((type != TRACK) && (type != NONE)) {
            log.error("unexpected type of B connection to layoutturnout - " + type);
        }
    }

    public void setConnectC(LayoutTrack o, int type) {
        connectC = o;
        if ((type != TRACK) && (type != NONE)) {
            log.error("unexpected type of C connection to layoutturnout - " + type);
        }
    }

    public void setConnectD(LayoutTrack o, int type) {
        connectD = o;
        if ((type != TRACK) && (type != NONE)) {
            log.error("unexpected type of D connection to layoutturnout - " + type);
        }
    }

    public LayoutBlock getLayoutBlock() {
        return block;
    }

    public LayoutBlock getLayoutBlockB() {
        if (blockB != null) {
            return blockB;
        }
        return block;
    }

    public LayoutBlock getLayoutBlockC() {
        if (blockC != null) {
            return blockC;
        }
        return block;
    }

    public LayoutBlock getLayoutBlockD() {
        if (blockD != null) {
            return blockD;
        }
        return block;
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
                log.error("Invalid connection type " + connectionType); //I18IN
        }
        return result;
    }

    /**
     * @return the bounds of this turnout
     */
    public Rectangle2D getBounds() {
        Rectangle2D result;

        Point2D pointA = getCoordsA();
        result = new Rectangle2D.Double(pointA.getX(), pointA.getY(), 0, 0);
        result.add(getCoordsB());
        result.add(getCoordsC());
        if ((getTurnoutType() == DOUBLE_XOVER)
                || (getTurnoutType() == LH_XOVER)
                || (getTurnoutType() == RH_XOVER)
                || (getTurnoutType() == SINGLE_SLIP)
                || (getTurnoutType() == DOUBLE_SLIP)) {
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
     * Set Up a Layout Block(s) for this Turnout
     */
    public void setLayoutBlock(LayoutBlock b) {
        if (block != b) {
            // block has changed, if old block exists, decrement use
            if ((block != null)
                    && (block != blockB)
                    && (block != blockC)
                    && (block != blockD)) {
                block.decrementUse();
            }
            block = b;
            if (b != null) {
                blockName = b.getId();
            } else {
                blockName = "";
            }
            // decrement use if block was already counted
            if ((block != null)
                    && ((block == blockB) || (block == blockC) || (block == blockD))) {
                block.decrementUse();
            }
        }
    }

    public void setLayoutBlockB(LayoutBlock b) {
        if ((getTurnoutType() == DOUBLE_XOVER)
                || (getTurnoutType() == LH_XOVER)
                || (getTurnoutType() == RH_XOVER)
                || (getTurnoutType() == SINGLE_SLIP)
                || (getTurnoutType() == DOUBLE_SLIP)) {
            if (blockB != b) {
                // block has changed, if old block exists, decrement use
                if ((blockB != null)
                        && (blockB != block)
                        && (blockB != blockC)
                        && (blockB != blockD)) {
                    blockB.decrementUse();
                }
                blockB = b;
                if (b != null) {
                    blockBName = b.getId();
                } else {
                    blockBName = "";
                }
                // decrement use if block was already counted
                if ((blockB != null)
                        && ((blockB == block) || (blockB == blockC) || (blockB == blockD))) {
                    blockB.decrementUse();
                }
            }
        } else {
            log.error("Attempt to set block B, but not a crossover");
        }
    }

    public void setLayoutBlockC(LayoutBlock b) {
        if ((getTurnoutType() == DOUBLE_XOVER)
                || (getTurnoutType() == LH_XOVER)
                || (getTurnoutType() == RH_XOVER)
                || (getTurnoutType() == SINGLE_SLIP)
                || (getTurnoutType() == DOUBLE_SLIP)) {
            if (blockC != b) {
                // block has changed, if old block exists, decrement use
                if ((blockC != null)
                        && (blockC != block)
                        && (blockC != blockB)
                        && (blockC != blockD)) {
                    blockC.decrementUse();
                }
                blockC = b;
                if (b != null) {
                    blockCName = b.getId();
                } else {
                    blockCName = "";
                }
                // decrement use if block was already counted
                if ((blockC != null)
                        && ((blockC == block) || (blockC == blockB) || (blockC == blockD))) {
                    blockC.decrementUse();
                }
            }
        } else {
            log.error("Attempt to set block C, but not a crossover");
        }
    }

    public void setLayoutBlockD(LayoutBlock b) {
        if ((getTurnoutType() == DOUBLE_XOVER)
                || (getTurnoutType() == LH_XOVER)
                || (getTurnoutType() == RH_XOVER)
                || (getTurnoutType() == SINGLE_SLIP)
                || (getTurnoutType() == DOUBLE_SLIP)) {
            if (blockD != b) {
                // block has changed, if old block exists, decrement use
                if ((blockD != null)
                        && (blockD != block)
                        && (blockD != blockB)
                        && (blockD != blockC)) {
                    blockD.decrementUse();
                }
                blockD = b;
                if (b != null) {
                    blockDName = b.getId();
                } else {
                    blockDName = "";
                }
                // decrement use if block was already counted
                if ((blockD != null)
                        && ((blockD == block) || (blockD == blockB) || (blockD == blockC))) {
                    blockD.decrementUse();
                }
            }
        } else {
            log.error("Attempt to set block D, but not a crossover");
        }
    }

    public void setLayoutBlockByName(@Nonnull String name) {
        blockName = name;
    }

    public void setLayoutBlockBByName(@Nonnull String name) {
        if ((getTurnoutType() == DOUBLE_XOVER)
                || (getTurnoutType() == LH_XOVER)
                || (getTurnoutType() == RH_XOVER)
                || (getTurnoutType() == SINGLE_SLIP)
                || (getTurnoutType() == DOUBLE_SLIP)) {
            blockBName = name;
        } else {
            log.error("Attempt to set block B name, but not a crossover or slip");
        }
    }

    public void setLayoutBlockCByName(@Nonnull String name) {
        if ((getTurnoutType() == DOUBLE_XOVER)
                || (getTurnoutType() == LH_XOVER)
                || (getTurnoutType() == RH_XOVER)
                || (getTurnoutType() == SINGLE_SLIP)
                || (getTurnoutType() == DOUBLE_SLIP)) {
            blockCName = name;
        } else {
            log.error("Attempt to set block C name, but not a crossover or slip");
        }
    }

    public void setLayoutBlockDByName(@Nonnull String name) {
        if ((getTurnoutType() == DOUBLE_XOVER)
                || (getTurnoutType() == LH_XOVER)
                || (getTurnoutType() == RH_XOVER)
                || (getTurnoutType() == SINGLE_SLIP)
                || (getTurnoutType() == DOUBLE_SLIP)) {
            blockDName = name;
        } else {
            log.error("Attempt to set block D name, but not a crossover or slip");
        }
    }

    /**
     * Methods to test if turnout legs are mainline track or not Returns true if
     * connecting track segment is mainline Defaults to not mainline if
     * connecting track segment is missing
     */
    public boolean isMainlineA() {
        if (connectA != null) {
            return ((TrackSegment) connectA).isMainline();
        } else {
            // if no connection, depends on type of turnout
            if ((getTurnoutType() == DOUBLE_XOVER)
                    || (getTurnoutType() == LH_XOVER)
                    || (getTurnoutType() == RH_XOVER)) {
                // All crossovers - straight continuing is B
                if (connectB != null) {
                    return ((TrackSegment) connectB).isMainline();
                }
            } else if ((getTurnoutType() == SINGLE_SLIP)
                    || (getTurnoutType() == DOUBLE_SLIP)) {
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
            if ((getTurnoutType() == DOUBLE_XOVER)
                    || (getTurnoutType() == LH_XOVER)
                    || (getTurnoutType() == RH_XOVER)) {
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
            if ((getTurnoutType() == DOUBLE_XOVER)
                    || (getTurnoutType() == LH_XOVER)
                    || (getTurnoutType() == RH_XOVER)) {
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
        } else if ((getTurnoutType() == SINGLE_SLIP)
                || (getTurnoutType() == DOUBLE_SLIP)) {
            if (connectB != null) {
                return ((TrackSegment) connectB).isMainline();
            }
        } else if (connectC != null) {
            return ((TrackSegment) connectC).isMainline();
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int findHitPointType(Point2D hitPoint, boolean useRectangles, boolean requireUnconnected) {
        int result = NONE;  // assume point not on connection

        if (useRectangles) {
            // calculate points control rectangle
            Rectangle2D r = layoutEditor.trackControlCircleRectAt(hitPoint);

            if (!requireUnconnected) {
                if (r.contains(center)) {
                    result = TURNOUT_CENTER;
                }
            }
            //check the A connection point
            if (NONE == result) {
                if (!requireUnconnected || (getConnectA() == null)) {
                    if (r.contains(getCoordsA())) {
                        result = TURNOUT_A;
                    }
                }
            }

            //check the B connection point
            if (NONE == result) {
                if (!requireUnconnected || (getConnectB() == null)) {
                    if (r.contains(getCoordsB())) {
                        result = TURNOUT_B;
                    }
                }
            }

            //check the C connection point
            if (NONE == result) {
                if (!requireUnconnected || (getConnectC() == null)) {
                    if (r.contains(getCoordsC())) {
                        result = TURNOUT_C;
                    }
                }
            }

            //check the D connection point
            if (NONE == result) {
                if ((getTurnoutType() == DOUBLE_XOVER)
                        || (getTurnoutType() == LH_XOVER)
                        || (getTurnoutType() == RH_XOVER)) {
                    if (!requireUnconnected || (getConnectD() == null)) {
                        if (r.contains(getCoordsD())) {
                            result = TURNOUT_D;
                        }
                    }
                }
            }
        } else {
            // calculate radius of turnout control circle
            double circleRadius = LayoutEditor.SIZE * layoutEditor.getTurnoutCircleSize();

            if (!requireUnconnected) {
                // calculate the distance to the center point of this turnout
                Double distance = hitPoint.distance(getCoordsCenter());
                if (distance <= circleRadius) {
                    result = TURNOUT_CENTER;
                }
            }

            //check the A connection point
            if (NONE == result) {
                if (!requireUnconnected || (getConnectA() == null)) {
                    Double distance = hitPoint.distance(getCoordsA());
                    if (distance <= circleRadius) {
                        result = TURNOUT_A;
                    }
                }
            }

            //check the B connection point
            if (NONE == result) {
                if (!requireUnconnected || (getConnectB() == null)) {
                    Double distance = hitPoint.distance(getCoordsB());
                    if (distance <= circleRadius) {
                        result = TURNOUT_B;
                    }
                }
            }

            //check the C connection point
            if (NONE == result) {
                if (!requireUnconnected || (getConnectC() == null)) {
                    Double distance = hitPoint.distance(getCoordsC());
                    if (distance <= circleRadius) {
                        result = TURNOUT_C;
                    }
                }
            }

            //check the D connection point
            if (NONE == result) {
                if ((getTurnoutType() == DOUBLE_XOVER)
                        || (getTurnoutType() == LH_XOVER)
                        || (getTurnoutType() == RH_XOVER)) {
                    if (!requireUnconnected || (getConnectD() == null)) {
                        Double distance = hitPoint.distance(getCoordsD());
                        if (distance <= circleRadius) {
                            result = TURNOUT_D;
                        }
                    }
                }
            }
        }
        return result;
    }

    /*
     * Modify coordinates methods
     */
    /**
     * set center coordinates
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
     * scale this LayoutTrack's coordinates by the x and y factors
     *
     * @param xFactor the amount to scale X coordinates
     * @param yFactor the amount to scale Y coordinates
     */
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
     * translate this LayoutTrack's coordinates by the x and y factors
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
        if (namedTurnout != null) {
            namedTurnout.getBean().addPropertyChangeListener(
                    mTurnoutListener = (java.beans.PropertyChangeEvent e) -> {
                        if (secondNamedTurnout != null) {
                            int new2ndState = secondNamedTurnout.getBean().getState();
                            if (e.getSource().equals(secondNamedTurnout.getBean())
                            && e.getNewValue().equals(new2ndState)) {
                                int old1stState = namedTurnout.getBean().getState();
                                int new1stState = new2ndState;
                                if (secondTurnoutInverted) {
                                    new1stState = Turnout.invertTurnoutState(new1stState);
                                }
                                if (old1stState != new1stState) {
                                    namedTurnout.getBean().setCommandedState(new1stState);
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
     * Toggle turnout if clicked on, physical turnout exists, and not disabled
     */
    public void toggleTurnout() {
        if (getTurnout() != null) {
            // toggle turnout
            if (getTurnout().getKnownState() == Turnout.CLOSED) {
                setState(Turnout.THROWN);
            } else {
                setState(Turnout.CLOSED);
            }
        } else {
            log.debug("Turnout Icon not associated with a Turnout");
        }
    }

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

    public int getState() {
        int result = Turnout.UNKNOWN;
        if (getTurnout() != null) {
            result = getTurnout().getKnownState();
        }
        return result;
    }

    /**
     * is this turnout occupied?
     *
     * @return true if occupied
     */
    private boolean isOccupied() {
        if ((getTurnoutType() == RH_TURNOUT)
                || (getTurnoutType() == LH_TURNOUT)
                || (getTurnoutType() == WYE_TURNOUT)) {
            if (getLayoutBlock().getOccupancy() == LayoutBlock.OCCUPIED) {
                log.debug("Block " + getBlockName() + "is Occupied");
                return true;
            }
        }
        if ((getTurnoutType() == DOUBLE_XOVER)
                || (getTurnoutType() == RH_XOVER)
                || (getTurnoutType() == LH_XOVER)) {
            //If the turnout is set for straight over, we need to deal with the straight over connecting blocks
            if (getTurnout().getKnownState() == Turnout.CLOSED) {
                if ((getLayoutBlock().getOccupancy() == LayoutBlock.OCCUPIED)
                        && (getLayoutBlockB().getOccupancy() == LayoutBlock.OCCUPIED)) {
                    log.debug("Blocks " + getBlockName() + " & " + getBlockBName() + " are Occupied");
                    return true;
                }
                if ((getLayoutBlockC().getOccupancy() == LayoutBlock.OCCUPIED)
                        && (getLayoutBlockD().getOccupancy() == LayoutBlock.OCCUPIED)) {
                    log.debug("Blocks " + getBlockCName() + " & " + getBlockDName() + " are Occupied");
                    return true;
                }
            }

        }
        if ((getTurnoutType() == DOUBLE_XOVER)
                || (getTurnoutType() == LH_XOVER)) {
            if (getTurnout().getKnownState() == Turnout.THROWN) {
                if ((getLayoutBlockB().getOccupancy() == LayoutBlock.OCCUPIED)
                        && (getLayoutBlockD().getOccupancy() == LayoutBlock.OCCUPIED)) {
                    log.debug("Blocks " + getBlockBName() + " & " + getBlockDName() + " are Occupied");
                    return true;
                }
            }
        }

        if ((getTurnoutType() == DOUBLE_XOVER)
                || (getTurnoutType() == RH_XOVER)) {
            if (getTurnout().getKnownState() == Turnout.THROWN) {
                if ((getLayoutBlock().getOccupancy() == LayoutBlock.OCCUPIED)
                        && (getLayoutBlockC().getOccupancy() == LayoutBlock.OCCUPIED)) {
                    log.debug("Blocks " + getLayoutBlock() + " & " + getBlockCName() + " are Occupied");
                    return true;
                }
            }
        }
        return false;
    }   // isOccupied

    // initialization instance variables (used when loading a LayoutEditor)
    public String connectAName = "";
    public String connectBName = "";
    public String connectCName = "";
    public String connectDName = "";
    public String tTurnoutName = "";
    public String tSecondTurnoutName = "";

    /**
     * Initialization method The above variables are initialized by
     * LayoutTurnoutXml, then the following method is called after the entire
     * LayoutEditor is loaded to set the specific TrackSegment objects.
     */
    public void setObjects(LayoutEditor p) {
        connectA = p.getFinder().findTrackSegmentByName(connectAName);
        connectB = p.getFinder().findTrackSegmentByName(connectBName);
        connectC = p.getFinder().findTrackSegmentByName(connectCName);
        connectD = p.getFinder().findTrackSegmentByName(connectDName);

        if (!blockName.isEmpty()) {
            block = p.getLayoutBlock(blockName);
            if (block != null) {
                block.incrementUse();
            } else {
                log.error("bad blockname '" + blockName + "' in layoutturnout " + getId());
            }
        }
        if (!blockBName.isEmpty()) {
            blockB = p.getLayoutBlock(blockBName);
            if (blockB != null) {
                if (block != blockB) {
                    blockB.incrementUse();
                }
            } else {
                log.error("bad blockname '" + blockBName + "' in layoutturnout " + getId());
            }
        }
        if (!blockCName.isEmpty()) {
            blockC = p.getLayoutBlock(blockCName);
            if (getLayoutBlockC() != null) {
                if ((block != getLayoutBlockC())
                        && (blockB != getLayoutBlockC())) {
                    getLayoutBlockC().incrementUse();
                }
            } else {
                log.error("bad blockname '" + blockCName + "' in layoutturnout " + getId());
            }
        }
        if (!blockDName.isEmpty()) {
            blockD = p.getLayoutBlock(blockDName);
            if (blockD != null) {
                if ((block != blockD) && (blockB != blockD)
                        && (getLayoutBlockC() != blockD)) {
                    blockD.incrementUse();
                }
            } else {
                log.error("bad blockname '" + blockDName + "' in layoutturnout " + getId());
            }
        }

        //Do the second one first then the activate is only called the once
        if (!tSecondTurnoutName.isEmpty()) {
            Turnout turnout = InstanceManager.turnoutManagerInstance().getTurnout(tSecondTurnoutName);
            if (turnout != null) {
                secondNamedTurnout = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(tSecondTurnoutName, turnout);
                secondTurnoutName = tSecondTurnoutName;
            } else {
                log.error("bad turnoutname '" + tSecondTurnoutName + "' in layoutturnout " + getId());
                secondTurnoutName = "";
                secondNamedTurnout = null;
            }
        }
        if (!tTurnoutName.isEmpty()) {
            Turnout turnout = InstanceManager.turnoutManagerInstance().getTurnout(tTurnoutName);
            if (turnout != null) {
                namedTurnout = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(tTurnoutName, turnout);
                turnoutName = tTurnoutName;
                activateTurnout();
            } else {
                log.error("bad turnoutname '" + tTurnoutName + "' in layoutturnout " + getId());
                turnoutName = "";
                namedTurnout = null;
            }
        }
    }   // setObjects

    private JPopupMenu popup = null;

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    protected JPopupMenu showPopup(@Nullable MouseEvent mouseEvent) {
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
            } else {
                jmi = popup.add(Bundle.getMessage("MakeLabel", Bundle.getMessage("BeanNameBlock")) + getLayoutBlock().getDisplayName());
            }
            jmi.setEnabled(false);

            if ((getTurnoutType() == DOUBLE_XOVER)
                    || (getTurnoutType() == RH_XOVER)
                    || (getTurnoutType() == LH_XOVER)) {
                // check if extra blocks have been entered
                if (getLayoutBlockB() != null) {
                    jmi = popup.add(Bundle.getMessage("MakeLabel", Bundle.getMessage("Block_ID", "B")) + getLayoutBlockB().getDisplayName());
                    jmi.setEnabled(false);
                }
                if (getLayoutBlockC() != null) {
                    jmi = popup.add(Bundle.getMessage("MakeLabel", Bundle.getMessage("Block_ID", "C")) + getLayoutBlockC().getDisplayName());
                    jmi.setEnabled(false);
                }
                if (getLayoutBlockD() != null) {
                    jmi = popup.add(Bundle.getMessage("MakeLabel", Bundle.getMessage("Block_ID", "D")) + getLayoutBlockD().getDisplayName());
                    jmi.setEnabled(false);
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
                    if (layoutEditor.removeLayoutTurnout(LayoutTurnout.this)) {
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
                        if ((getTurnoutType() == DOUBLE_XOVER)
                                || (getTurnoutType() == RH_XOVER)
                                || (getTurnoutType() == LH_XOVER)) {
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

                if (!getBlockName().isEmpty()) {
                    final String[] boundaryBetween = getBlockBoundaries();
                    boolean blockBoundaries = false;
                    for (int i = 0; i < boundaryBetween.length; i++) {
                        if (boundaryBetween[i] != null) {
                            blockBoundaries = true;
                            break;
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

                        if (InstanceManager.getDefault(LayoutBlockManager.class).isAdvancedRoutingEnabled()) {
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
                                    viewRouting.add(new AbstractAction(getBlockBName()) {
                                        @Override
                                        public void actionPerformed(ActionEvent e) {
                                            AbstractAction routeTableAction = new LayoutBlockRouteTableAction(blockName, layoutBlock);
                                            routeTableAction.actionPerformed(e);
                                        }
                                    });
                                }
                                popup.add(viewRouting);
                            }
                        }   // isAdvancedRoutingEnabled()
                    }   // if (blockBoundaries)
                }
            }
            setAdditionalEditPopUpMenu(popup);
            layoutEditor.setShowAlignmentMenu(popup);
            popup.show(mouseEvent.getComponent(), mouseEvent.getX(), mouseEvent.getY());
        } else if (!viewAdditionalMenu.isEmpty()) {
            setAdditionalViewPopUpMenu(popup);
            popup.show(mouseEvent.getComponent(), mouseEvent.getX(), mouseEvent.getY());
        }
        return popup;
    }   // showPopup

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
        if (getTurnoutType() >= DOUBLE_XOVER && getTurnoutType() <= LH_XOVER) {
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
        if (jmri.InstanceManager.getDefault(LayoutBlockManager.class).isAdvancedRoutingEnabled() && InstanceManager.getDefault(jmri.SignalMastLogicManager.class).isSignalMastUsed(signalMast)) {
            SignallingGuiTools.removeSignalMastLogic(null, signalMast);
        }
    }

    /**
     * Clean up when this object is no longer needed. Should not be called while
     * the object is still displayed; see remove()
     */
    void dispose() {
        if (popup != null) {
            popup.removeAll();
        }
        popup = null;
    }

    /**
     * Removes this object from display and persistance
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
     * draw this turnout
     *
     * @param g2 the graphics port to draw to
     */
    protected void draw(Graphics2D g2) {
        Turnout to = getTurnout();

        Point2D pointA = getCoordsA();
        Point2D pointB = getCoordsB();
        Point2D pointC = getCoordsC();
        Point2D pointD = getCoordsD();

        setColorForTrackBlock(g2, getLayoutBlock());

        if (getTurnoutType() == DOUBLE_XOVER) {
            //  double crossover turnout
            if (to == null) {
                // no physical turnout linked - draw A corner
                layoutEditor.setTrackStrokeWidth(g2, isMainlineA());
                g2.draw(new Line2D.Double(pointA, MathUtil.midPoint(pointA, pointB)));
                layoutEditor.setTrackStrokeWidth(g2, false);
                g2.draw(new Line2D.Double(pointA, MathUtil.midPoint(pointA, pointC)));

                // draw B corner
                setColorForTrackBlock(g2, getLayoutBlockB());
                layoutEditor.setTrackStrokeWidth(g2, isMainlineB());
                g2.draw(new Line2D.Double(pointB, MathUtil.midPoint(pointA, pointB)));
                layoutEditor.setTrackStrokeWidth(g2, false);
                g2.draw(new Line2D.Double(pointB, MathUtil.midPoint(pointB, pointD)));

                // draw C corner
                setColorForTrackBlock(g2, getLayoutBlockC());
                layoutEditor.setTrackStrokeWidth(g2, isMainlineC());
                g2.draw(new Line2D.Double(pointC, MathUtil.midPoint(pointC, pointD)));
                layoutEditor.setTrackStrokeWidth(g2, false);
                g2.draw(new Line2D.Double(pointC, MathUtil.midPoint(pointA, pointC)));

                // draw D corner
                setColorForTrackBlock(g2, getLayoutBlockD());
                layoutEditor.setTrackStrokeWidth(g2, isMainlineD());
                g2.draw(new Line2D.Double(pointD, MathUtil.midPoint(pointC, pointD)));
                layoutEditor.setTrackStrokeWidth(g2, false);
                g2.draw(new Line2D.Double(pointD, MathUtil.midPoint(pointB, pointD)));
            } else {
                int state = Turnout.CLOSED;
                if (layoutEditor.isAnimating()) {
                    state = to.getKnownState();
                }
                if (state == Turnout.CLOSED) {
                    // continuing path - not crossed over
                    setColorForTrackBlock(g2, getLayoutBlock());
                    layoutEditor.setTrackStrokeWidth(g2, isMainlineA());
                    g2.draw(new Line2D.Double(pointA, MathUtil.midPoint(pointA, pointB)));
                    layoutEditor.setTrackStrokeWidth(g2, false);
                    setColorForTrackBlock(g2, getLayoutBlock(), true);
                    g2.draw(new Line2D.Double(pointA, MathUtil.oneThirdPoint(pointA, pointC)));

                    setColorForTrackBlock(g2, getLayoutBlockB());
                    layoutEditor.setTrackStrokeWidth(g2, isMainlineB());
                    g2.draw(new Line2D.Double(pointB, MathUtil.midPoint(pointA, pointB)));
                    layoutEditor.setTrackStrokeWidth(g2, false);
                    setColorForTrackBlock(g2, getLayoutBlockB(), true);
                    g2.draw(new Line2D.Double(pointB, MathUtil.oneThirdPoint(pointB, pointD)));

                    setColorForTrackBlock(g2, getLayoutBlockC());
                    layoutEditor.setTrackStrokeWidth(g2, isMainlineC());
                    g2.draw(new Line2D.Double(pointC, MathUtil.midPoint(pointC, pointD)));
                    layoutEditor.setTrackStrokeWidth(g2, false);
                    setColorForTrackBlock(g2, getLayoutBlockC(), true);
                    g2.draw(new Line2D.Double(pointC, MathUtil.oneThirdPoint(pointC, pointA)));

                    setColorForTrackBlock(g2, getLayoutBlockD());
                    layoutEditor.setTrackStrokeWidth(g2, isMainlineD());
                    g2.draw(new Line2D.Double(pointD, MathUtil.midPoint(pointC, pointD)));
                    layoutEditor.setTrackStrokeWidth(g2, false);
                    setColorForTrackBlock(g2, getLayoutBlockD(), true);
                    g2.draw(new Line2D.Double(pointD, MathUtil.oneThirdPoint(pointD, pointB)));
                } else if (state == Turnout.THROWN) {
                    // diverting (crossed) path
                    setColorForTrackBlock(g2, getLayoutBlock());
                    layoutEditor.setTrackStrokeWidth(g2, isMainlineA());
                    g2.draw(new Line2D.Double(pointA, MathUtil.oneThirdPoint(pointA, pointB)));
                    layoutEditor.setTrackStrokeWidth(g2, false);
                    setColorForTrackBlock(g2, getLayoutBlock(), true);
                    g2.draw(new Line2D.Double(pointA, center));

                    setColorForTrackBlock(g2, getLayoutBlockB());
                    layoutEditor.setTrackStrokeWidth(g2, isMainlineB());
                    g2.draw(new Line2D.Double(pointB, MathUtil.oneThirdPoint(pointB, pointA)));
                    layoutEditor.setTrackStrokeWidth(g2, false);
                    setColorForTrackBlock(g2, getLayoutBlockB(), true);

                    g2.draw(new Line2D.Double(pointB, center));

                    setColorForTrackBlock(g2, getLayoutBlockC());
                    layoutEditor.setTrackStrokeWidth(g2, isMainlineC());
                    g2.draw(new Line2D.Double(pointC, MathUtil.oneThirdPoint(pointC, pointD)));
                    layoutEditor.setTrackStrokeWidth(g2, false);
                    setColorForTrackBlock(g2, getLayoutBlockC(), true);
                    g2.draw(new Line2D.Double(pointC, center));

                    setColorForTrackBlock(g2, getLayoutBlockD());
                    layoutEditor.setTrackStrokeWidth(g2, isMainlineD());
                    g2.draw(new Line2D.Double(pointD, MathUtil.oneThirdPoint(pointD, pointC)));
                    layoutEditor.setTrackStrokeWidth(g2, false);
                    setColorForTrackBlock(g2, getLayoutBlockD(), true);
                    g2.draw(new Line2D.Double(pointD, center));
                } else {
                    // unknown or inconsistent
                    layoutEditor.setTrackStrokeWidth(g2, isMainlineA());
                    g2.draw(new Line2D.Double(pointA, MathUtil.oneThirdPoint(pointA, pointB)));
                    layoutEditor.setTrackStrokeWidth(g2, false);
                    g2.draw(new Line2D.Double(pointA, MathUtil.oneThirdPoint(pointA, pointC)));

                    setColorForTrackBlock(g2, getLayoutBlockB());
                    layoutEditor.setTrackStrokeWidth(g2, isMainlineB());
                    g2.draw(new Line2D.Double(pointB, MathUtil.oneThirdPoint(pointB, pointA)));
                    layoutEditor.setTrackStrokeWidth(g2, false);
                    g2.draw(new Line2D.Double(pointB, MathUtil.oneThirdPoint(pointB, pointD)));

                    setColorForTrackBlock(g2, getLayoutBlockC());
                    layoutEditor.setTrackStrokeWidth(g2, isMainlineC());
                    g2.draw(new Line2D.Double(pointC, MathUtil.oneThirdPoint(pointC, pointD)));
                    layoutEditor.setTrackStrokeWidth(g2, false);
                    g2.draw(new Line2D.Double(pointC, MathUtil.oneThirdPoint(pointC, pointA)));

                    setColorForTrackBlock(g2, getLayoutBlockD());
                    layoutEditor.setTrackStrokeWidth(g2, isMainlineD());
                    g2.draw(new Line2D.Double(pointD, MathUtil.oneThirdPoint(pointD, pointC)));
                    layoutEditor.setTrackStrokeWidth(g2, false);
                    g2.draw(new Line2D.Double(pointD, MathUtil.oneThirdPoint(pointD, pointB)));
                }   // if (state == XXX) {} else...
            }   // if (to == null) {} else...
        } else if ((getTurnoutType() == RH_XOVER)
                || (getTurnoutType() == LH_XOVER)) {
            //  LH and RH crossover turnouts
            if (to == null) {
                // no physical turnout linked - draw A corner
                layoutEditor.setTrackStrokeWidth(g2, isMainlineA());
                g2.draw(new Line2D.Double(pointA, MathUtil.midPoint(pointA, pointB)));
                if (getTurnoutType() == RH_XOVER) {
                    layoutEditor.setTrackStrokeWidth(g2, false);
                    g2.draw(new Line2D.Double(MathUtil.midPoint(pointA, pointB), center));
                }

                // draw B corner
                setColorForTrackBlock(g2, getLayoutBlockB());
                layoutEditor.setTrackStrokeWidth(g2, isMainlineB());
                g2.draw(new Line2D.Double(pointB, MathUtil.midPoint(pointA, pointB)));
                if (getTurnoutType() == LH_XOVER) {
                    layoutEditor.setTrackStrokeWidth(g2, false);
                    g2.draw(new Line2D.Double(MathUtil.midPoint(pointA, pointB), center));
                }

                // draw C corner
                setColorForTrackBlock(g2, getLayoutBlockC());
                layoutEditor.setTrackStrokeWidth(g2, isMainlineC());
                g2.draw(new Line2D.Double(pointC, MathUtil.midPoint(pointC, pointD)));
                if (getTurnoutType() == RH_XOVER) {
                    layoutEditor.setTrackStrokeWidth(g2, false);
                    g2.draw(new Line2D.Double(MathUtil.midPoint(pointC, pointD), center));
                }

                // draw D corner
                setColorForTrackBlock(g2, getLayoutBlockD());
                layoutEditor.setTrackStrokeWidth(g2, isMainlineD());
                g2.draw(new Line2D.Double(pointD, MathUtil.midPoint(pointC, pointD)));
                if (getTurnoutType() == LH_XOVER) {
                    layoutEditor.setTrackStrokeWidth(g2, false);
                    g2.draw(new Line2D.Double(MathUtil.midPoint(pointC, pointD), center));
                }
            } else {
                int state = Turnout.CLOSED;
                if (layoutEditor.isAnimating()) {
                    state = to.getKnownState();
                }
                if (state == Turnout.CLOSED) {
                    // continuing path - not crossed over
                    layoutEditor.setTrackStrokeWidth(g2, isMainlineA());
                    g2.draw(new Line2D.Double(pointA, MathUtil.midPoint(pointA, pointB)));
                    if (getTurnoutType() == RH_XOVER) {
                        layoutEditor.setTrackStrokeWidth(g2, false);
                        setColorForTrackBlock(g2, getLayoutBlock(), true);
                        g2.draw(new Line2D.Double(center, MathUtil.oneThirdPoint(center, MathUtil.midPoint(pointA, pointB))));
                    }

                    setColorForTrackBlock(g2, getLayoutBlockB());
                    layoutEditor.setTrackStrokeWidth(g2, isMainlineB());
                    g2.draw(new Line2D.Double(pointB, MathUtil.midPoint(pointA, pointB)));

                    if (getTurnoutType() == LH_XOVER) {
                        layoutEditor.setTrackStrokeWidth(g2, false);
                        setColorForTrackBlock(g2, getLayoutBlockB(), true);
                        g2.draw(new Line2D.Double(center, MathUtil.oneThirdPoint(center, MathUtil.midPoint(pointA, pointB))));
                    }

                    setColorForTrackBlock(g2, getLayoutBlockC());
                    layoutEditor.setTrackStrokeWidth(g2, isMainlineC());
                    g2.draw(new Line2D.Double(pointC, MathUtil.midPoint(pointC, pointD)));
                    if (getTurnoutType() == RH_XOVER) {
                        layoutEditor.setTrackStrokeWidth(g2, false);
                        setColorForTrackBlock(g2, getLayoutBlockC(), true);
                        g2.draw(new Line2D.Double(center, MathUtil.oneThirdPoint(center, MathUtil.midPoint(pointC, pointD))));
                    }

                    setColorForTrackBlock(g2, getLayoutBlockD());
                    layoutEditor.setTrackStrokeWidth(g2, isMainlineD());
                    g2.draw(new Line2D.Double(pointD, MathUtil.midPoint(pointC, pointD)));
                    if (getTurnoutType() == LH_XOVER) {
                        layoutEditor.setTrackStrokeWidth(g2, false);
                        setColorForTrackBlock(g2, getLayoutBlockD(), true);
                        g2.draw(new Line2D.Double(center, MathUtil.oneThirdPoint(center, MathUtil.midPoint(pointC, pointD))));
                    }
                } else if (state == Turnout.THROWN) {
                    // diverting (crossed) path
                    layoutEditor.setTrackStrokeWidth(g2, isMainlineA());
                    if (getTurnoutType() == RH_XOVER) {
                        g2.draw(new Line2D.Double(pointA, MathUtil.midPoint(pointA, pointB)));
                        //layoutEditor.setTrackStrokeWidth(g2, false);
                        g2.draw(new Line2D.Double(MathUtil.midPoint(pointA, pointB), center));
                    } else if (getTurnoutType() == LH_XOVER) {
                        g2.draw(new Line2D.Double(pointA, MathUtil.oneFourthPoint(pointA, pointB)));
                    }

                    setColorForTrackBlock(g2, getLayoutBlockB());
                    layoutEditor.setTrackStrokeWidth(g2, isMainlineB());
                    if (getTurnoutType() == LH_XOVER) {
                        g2.draw(new Line2D.Double(pointB, MathUtil.midPoint(pointB, pointA)));
                        //layoutEditor.setTrackStrokeWidth(g2, false);
                        g2.draw(new Line2D.Double(MathUtil.midPoint(pointA, pointB), center));
                    } else if (getTurnoutType() == RH_XOVER) {
                        g2.draw(new Line2D.Double(pointB, MathUtil.oneFourthPoint(pointB, pointA)));
                    }

                    setColorForTrackBlock(g2, getLayoutBlockC());
                    layoutEditor.setTrackStrokeWidth(g2, isMainlineC());
                    if (getTurnoutType() == RH_XOVER) {
                        g2.draw(new Line2D.Double(pointC, MathUtil.midPoint(pointC, pointD)));
                        //layoutEditor.setTrackStrokeWidth(g2, false);
                        g2.draw(new Line2D.Double(MathUtil.midPoint(pointC, pointD), center));
                    } else if (getTurnoutType() == LH_XOVER) {
                        g2.draw(new Line2D.Double(pointC, MathUtil.oneFourthPoint(pointC, pointD)));
                    }

                    setColorForTrackBlock(g2, getLayoutBlockD());
                    layoutEditor.setTrackStrokeWidth(g2, isMainlineD());
                    if (getTurnoutType() == LH_XOVER) {
                        g2.draw(new Line2D.Double(pointD, MathUtil.midPoint(pointD, pointC)));
                        //layoutEditor.setTrackStrokeWidth(g2, false);
                        g2.draw(new Line2D.Double(MathUtil.midPoint(pointC, pointD), center));
                    } else if (getTurnoutType() == RH_XOVER) {
                        g2.draw(new Line2D.Double(pointD, MathUtil.oneFourthPoint(pointD, pointC)));
                    }
                } else {
                    // unknown or inconsistent
                    layoutEditor.setTrackStrokeWidth(g2, isMainlineA());
                    if (getTurnoutType() == RH_XOVER) {
                        g2.draw(new Line2D.Double(pointA, MathUtil.midPoint(pointA, pointB)));
                        //layoutEditor.setTrackStrokeWidth(g2, false);
                        g2.draw(new Line2D.Double(center, MathUtil.oneThirdPoint(center, MathUtil.midPoint(pointA, pointB))));
                    } else if (getTurnoutType() == LH_XOVER) {
                        g2.draw(new Line2D.Double(pointA, MathUtil.oneFourthPoint(pointA, pointB)));
                    }

                    setColorForTrackBlock(g2, getLayoutBlockB());
                    layoutEditor.setTrackStrokeWidth(g2, isMainlineB());
                    if (getTurnoutType() == LH_XOVER) {
                        g2.draw(new Line2D.Double(pointB, MathUtil.midPoint(pointB, pointA)));
                        //layoutEditor.setTrackStrokeWidth(g2, false);
                        g2.draw(new Line2D.Double(center, MathUtil.oneThirdPoint(center, MathUtil.midPoint(pointA, pointB))));
                    } else if (getTurnoutType() == RH_XOVER) {
                        g2.draw(new Line2D.Double(pointB, MathUtil.oneFourthPoint(pointB, pointA)));
                    }

                    setColorForTrackBlock(g2, getLayoutBlockC());
                    layoutEditor.setTrackStrokeWidth(g2, isMainlineC());
                    if (getTurnoutType() == RH_XOVER) {
                        g2.draw(new Line2D.Double(pointC, MathUtil.midPoint(pointC, pointD)));
                        //layoutEditor.setTrackStrokeWidth(g2, false);
                        g2.draw(new Line2D.Double(center, MathUtil.oneThirdPoint(center, MathUtil.midPoint(pointC, pointD))));
                    } else if (getTurnoutType() == LH_XOVER) {
                        g2.draw(new Line2D.Double(pointC, MathUtil.oneFourthPoint(pointC, pointD)));
                    }

                    setColorForTrackBlock(g2, getLayoutBlockD());
                    layoutEditor.setTrackStrokeWidth(g2, isMainlineD());
                    if (getTurnoutType() == LH_XOVER) {
                        g2.draw(new Line2D.Double(pointD, MathUtil.midPoint(pointC, pointD)));
                        //layoutEditor.setTrackStrokeWidth(g2, false);
                        g2.draw(new Line2D.Double(center, MathUtil.oneThirdPoint(center, MathUtil.midPoint(pointC, pointD))));
                    } else if (getTurnoutType() == RH_XOVER) {
                        g2.draw(new Line2D.Double(pointD, MathUtil.oneFourthPoint(pointD, pointC)));
                    }
                }   // if (state == XXX} {} else...
            }   // if (to == null) {} else...
        } else {
            // LH, RH, or WYE Turnouts
            if (to == null) {
                // no physical turnout linked - draw connected
                layoutEditor.setTrackStrokeWidth(g2, isMainlineA());
                g2.draw(new Line2D.Double(pointA, center));
                layoutEditor.setTrackStrokeWidth(g2, isMainlineB());
                g2.draw(new Line2D.Double(pointB, center));
                layoutEditor.setTrackStrokeWidth(g2, isMainlineC());
                g2.draw(new Line2D.Double(pointC, center));
            } else {
                layoutEditor.setTrackStrokeWidth(g2, isMainlineA());
                //line from throat to center
                g2.draw(new Line2D.Double(pointA, center));
                int state = Turnout.CLOSED;
                if (layoutEditor.isAnimating()) {
                    state = to.getKnownState();
                }
                switch (state) {
                    case Turnout.CLOSED:
                        if (getContinuingSense() == Turnout.CLOSED) {
                            layoutEditor.setTrackStrokeWidth(g2, isMainlineB());
                            //line from continuing leg to center
                            g2.draw(new Line2D.Double(pointB, center));
                            if (layoutEditor.getTurnoutDrawUnselectedLeg()) {
                                //line from diverging leg halfway to center
                                layoutEditor.setTrackStrokeWidth(g2, isMainlineC());
                                setColorForTrackBlock(g2, getLayoutBlockB(), true);
                                g2.draw(new Line2D.Double(pointC, MathUtil.midPoint(center, pointC)));
                            }
                        } else {
                            layoutEditor.setTrackStrokeWidth(g2, isMainlineC());
                            //line from diverging leg to center
                            g2.draw(new Line2D.Double(pointC, center));
                            if (layoutEditor.getTurnoutDrawUnselectedLeg()) {
                                //line from continuing leg halfway to center
                                layoutEditor.setTrackStrokeWidth(g2, isMainlineB());
                                setColorForTrackBlock(g2, getLayoutBlockC(), true);
                                g2.draw(new Line2D.Double(pointB, MathUtil.midPoint(center, pointB)));
                            }
                        }
                        break;
                    case Turnout.THROWN:
                        if (getContinuingSense() == Turnout.THROWN) {
                            layoutEditor.setTrackStrokeWidth(g2, isMainlineB());
                            g2.draw(new Line2D.Double(pointB, center));
                            if (layoutEditor.getTurnoutDrawUnselectedLeg()) {
                                layoutEditor.setTrackStrokeWidth(g2, isMainlineC());
                                setColorForTrackBlock(g2, getLayoutBlockB(), true);
                                g2.draw(new Line2D.Double(pointC, MathUtil.midPoint(center, pointC)));
                            }
                        } else {
                            layoutEditor.setTrackStrokeWidth(g2, isMainlineC());
                            g2.draw(new Line2D.Double(pointC, center));
                            if (layoutEditor.getTurnoutDrawUnselectedLeg()) {
                                layoutEditor.setTrackStrokeWidth(g2, isMainlineB());
                                setColorForTrackBlock(g2, getLayoutBlockC(), true);
                                g2.draw(new Line2D.Double(pointB, MathUtil.midPoint(center, pointB)));
                            }
                        }
                        break;
                    default:
                        // inconsistent or unknown
                        layoutEditor.setTrackStrokeWidth(g2, isMainlineC());
                        g2.draw(new Line2D.Double(pointC, MathUtil.midPoint(center, pointC)));
                        layoutEditor.setTrackStrokeWidth(g2, isMainlineB());
                        g2.draw(new Line2D.Double(pointB, MathUtil.midPoint(center, pointB)));
                }   // switch (state)
            }   // if (to == null) {} else...
        }   // if (getTurnoutType() == XXX) {} else if... {} else...
    }   // draw

    /**
     * {@inheritDoc}
     */
    @Override
    protected void drawUnconnected(Graphics2D g2) {
        if (getConnectA() == null) {
            g2.fill(layoutEditor.trackControlCircleAt(getCoordsA()));
        }

        if (getConnectB() == null) {
            g2.fill(layoutEditor.trackControlCircleAt(getCoordsB()));
        }

        if (getConnectC() == null) {
            g2.fill(layoutEditor.trackControlCircleAt(getCoordsC()));
        }
        if ((getTurnoutType() == DOUBLE_XOVER)
                || (getTurnoutType() == RH_XOVER)
                || (getTurnoutType() == LH_XOVER)) {
            if (getConnectD() == null) {
                g2.fill(layoutEditor.trackControlCircleAt(getCoordsD()));
            }
        }
    }

    protected void drawTurnoutControls(Graphics2D g2) {
        g2.draw(layoutEditor.trackControlCircleAt(center));
    }

    protected void drawEditControls(Graphics2D g2) {
        Point2D pt = getCoordsA();
        if (getTurnoutType() >= DOUBLE_XOVER && getTurnoutType() <= LH_XOVER) {
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
        g2.draw(layoutEditor.trackControlPointRectAt(pt));

        pt = getCoordsB();
        if (getConnectB() == null) {
            g2.setColor(Color.red);
        } else {
            g2.setColor(Color.green);
        }
        g2.draw(layoutEditor.trackControlPointRectAt(pt));

        pt = getCoordsC();
        if (getConnectC() == null) {
            g2.setColor(Color.red);
        } else {
            g2.setColor(Color.green);
        }
        g2.draw(layoutEditor.trackControlPointRectAt(pt));

        if ((getTurnoutType() == DOUBLE_XOVER)
                || (getTurnoutType() == RH_XOVER)
                || (getTurnoutType() == LH_XOVER)) {
            pt = getCoordsD();
            if (getConnectD() == null) {
                g2.setColor(Color.red);
            } else {
                g2.setColor(Color.green);
            }
            g2.draw(layoutEditor.trackControlPointRectAt(pt));
        }
    }   // drawEditControls

    /*
        this is used by ConnectivityUtil to determine the turnout state necessary to get from prevLayoutBlock ==> currLayoutBlock ==> nextLayoutBlock
     */
    protected int getConnectivityStateForLayoutBlocks(
            LayoutBlock currLayoutBlock,
            LayoutBlock prevLayoutBlock,
            LayoutBlock nextLayoutBlock,
            boolean suppress) {
        int result = Turnout.UNKNOWN;

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
                            log.error("Cannot determine turnout setting - " + getTurnoutName());
                        }
                        result = Turnout.CLOSED;
                    }
                } else if (layoutBlockB == currLayoutBlock) {
                    result = Turnout.CLOSED;
                } else if (layoutBlockC == currLayoutBlock) {
                    result = Turnout.THROWN;
                } else {
                    if (!suppress) {
                        log.error("Cannot determine turnout setting for " + getTurnoutName());
                    }
                    if (!suppress) {
                        log.error("lb " + currLayoutBlock + " nlb " + nextLayoutBlock + " connect B " + layoutBlockB + " connect C " + layoutBlockC);
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
                            log.error("Cannot determine turnout setting(A) - " + getTurnoutName());
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
                            log.error("Cannot determine turnout setting(B) - " + getTurnoutName());
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
                            log.error("Cannot determine turnout setting(C) - " + getTurnoutName());
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
                            log.error("Cannot determine turnout setting(D) - " + getTurnoutName());
                        }
                        result = Turnout.CLOSED;
                    }
                }
                break;
            }
            default: {
                log.warn("getTurnoutList() unknown tTyp: " + tTyp);
                break;
            }
        }   // switch (tTyp)

        return result;
    }   // getConnectivityStateForLayoutBlocks


    /*
     * {@inheritDoc}
     */
    //TODO: on the cross-overs check the internal boundary details.
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
            } else if (((getTurnoutType() == DOUBLE_XOVER)
                    || (getTurnoutType() == RH_XOVER)
                    || (getTurnoutType() == LH_XOVER)) && connectD == null) {
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
        } else if ((connectD == null) && ((getTurnoutType() == DOUBLE_XOVER)
                || (getTurnoutType() == RH_XOVER)
                || (getTurnoutType() == LH_XOVER))) {
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
                && ((getTurnoutType() == DOUBLE_XOVER)
                || (getTurnoutType() == RH_XOVER)
                || (getTurnoutType() == LH_XOVER))) {
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
    }   // getLayoutConnectivity()

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Integer> checkForFreeConnections() {
        List<Integer> result = new ArrayList<>();

        //check the A connection point
        if (getConnectA() == null) {
            result.add(Integer.valueOf(TURNOUT_A));
        }

        //check the B connection point
        if (getConnectB() == null) {
            result.add(Integer.valueOf(TURNOUT_B));
        }

        //check the C connection point
        if (getConnectC() == null) {
            result.add(Integer.valueOf(TURNOUT_C));
        }

        //check the D connection point
        if ((getTurnoutType() == DOUBLE_XOVER)
                || (getTurnoutType() == LH_XOVER)
                || (getTurnoutType() == RH_XOVER)) {
            if (getConnectD() == null) {
                result.add(Integer.valueOf(TURNOUT_D));
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
        if ((getBlockName() != null) && (connectA != null)) {
            blocksAndTracksMap.put(connectA, getBlockName());
        }
        if ((getBlockBName() != null) && (connectB != null)) {
            blocksAndTracksMap.put(connectB, getBlockBName());
        }
        if ((getBlockCName() != null) && (connectC != null)) {
            blocksAndTracksMap.put(connectC, getBlockCName());
        }
        if ((getTurnoutType() == DOUBLE_XOVER)
                || (getTurnoutType() == LH_XOVER)
                || (getTurnoutType() == RH_XOVER)
                || (getTurnoutType() == SINGLE_SLIP)
                || (getTurnoutType() == DOUBLE_SLIP)) {
            if ((getBlockDName() != null) && (connectD != null)) {
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
                log.info("-New block ('{}') trackNameSets", theBlockName);
                TrackNameSets = new ArrayList<>();
                blockNamesToTrackNameSetsMap.put(theBlockName, TrackNameSets);
            }
            if (TrackNameSet == null) {
                TrackNameSet = new LinkedHashSet<>();
                TrackNameSets.add(TrackNameSet);
            }
            if (TrackNameSet.add(getName())) {
                log.info("-    Add track '{}' to trackNameSet for block '{}'", getName(), theBlockName);
            }
            theConnect.collectContiguousTracksNamesInBlockNamed(theBlockName, TrackNameSet);
        }
    }   // collectContiguousTracksNamesInBlockNamed

    /**
     * {@inheritDoc}
     */
    public void collectContiguousTracksNamesInBlockNamed(
            @Nonnull String blockName,
            @Nonnull Set<String> TrackNameSet) {
        if (!TrackNameSet.contains(getName())) {

            // create list of our connects
            List<LayoutTrack> connects = new ArrayList<>();
            if ((this.blockName != null) && (this.blockName.equals(blockName))
                    && (connectA != null)) {
                connects.add(connectA);
            }
            if ((getBlockBName() != null) && (getBlockBName().equals(blockName))
                    && (connectB != null)) {
                connects.add(connectB);
            }
            if ((getBlockCName() != null) && (getBlockCName().equals(blockName))
                    && (connectC != null)) {
                connects.add(connectC);
            }
            if ((getTurnoutType() == DOUBLE_XOVER)
                    || (getTurnoutType() == LH_XOVER)
                    || (getTurnoutType() == RH_XOVER)
                    || (getTurnoutType() == SINGLE_SLIP)
                    || (getTurnoutType() == DOUBLE_SLIP)) {
                if ((getBlockDName() != null) && (getBlockDName().equals(blockName))
                        && (connectD != null)) {
                    connects.add(connectD);
                }
            }

            for (LayoutTrack connect : connects) {
                // if we are added to the TrackNameSet
                if (TrackNameSet.add(getName())) {
                    log.info("-    Add track '{}'for block '{}'", getName(), blockName);
                }
                // it's time to play... flood your neighbour!
                connect.collectContiguousTracksNamesInBlockNamed(blockName, TrackNameSet);
            }
        }
    }   // collectContiguousTracksNamesInBlockNamed

    /**
     * {@inheritDoc}
     */
    public void setAllLayoutBlocks(LayoutBlock layoutBlock) {
        setLayoutBlock(layoutBlock);
        if ((getTurnoutType() == DOUBLE_XOVER)
                || (getTurnoutType() == LH_XOVER)
                || (getTurnoutType() == RH_XOVER)
                || (getTurnoutType() == SINGLE_SLIP)
                || (getTurnoutType() == DOUBLE_SLIP)) {
            setLayoutBlockB(layoutBlock);
            setLayoutBlockC(layoutBlock);
            setLayoutBlockD(layoutBlock);
        }
    }

    private final static Logger log = LoggerFactory.getLogger(LayoutTurnout.class);

}
