package jmri.jmrit.display.layoutEditor;

import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRootPane;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import jmri.BlockManager;
import jmri.InstanceManager;
import jmri.NamedBeanHandle;
import jmri.Path;
import jmri.Sensor;
import jmri.SignalHead;
import jmri.SignalMast;
import jmri.Turnout;
import jmri.jmrit.display.layoutEditor.blockRoutingTable.LayoutBlockRouteTableAction;
import jmri.jmrit.signalling.SignallingGuiTools;
import jmri.util.ColorUtil;
import jmri.util.JmriJFrame;
import jmri.util.MathUtil;
import jmri.util.swing.JmriBeanComboBox;
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

    // Defined text resource
    ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.display.layoutEditor.LayoutEditorBundle");

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
    protected LayoutBlock blockC = null;  // Xover - oneThirdPoint block, if there is one
    protected LayoutBlock blockD = null;  // Xover - oneFourthPoint block, if there is one

    private java.beans.PropertyChangeListener mTurnoutListener = null;

    // persistent instances variables (saved between sessions)
    public String turnoutName = "";   // should be the name (system or user) of
    //  an existing physical turnout
    public String secondTurnoutName = "";
    /* should be the name (system or user) of an existing physical turnout.
        Second turnout is used to allow the throwing of two different turnout
        to control one cross-over
     */
    private boolean secondTurnoutInverted = false;

    public String blockName = "";  // name for block, if there is one
    public String blockBName = "";  // Xover/slip - name for second block, if there is one
    public String blockCName = "";  // Xover/slip - name for oneThirdPoint block, if there is one
    public String blockDName = "";  // Xover/slip - name for oneFourthPoint block, if there is one

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

    public Object connectA = null;      // throat of LH, RH, RH Xover, LH Xover, and WYE turnouts
    public Object connectB = null;      // straight leg of LH and RH turnouts
    public Object connectC = null;
    public Object connectD = null;      // double xover, RH Xover, LH Xover only

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

    protected LayoutTurnout(String id, Point2D c, LayoutEditor layoutEditor) {
        super(id, c, layoutEditor);
    }

    public LayoutTurnout(String id, int t, Point2D c, double rot,
            double xFactor, double yFactor, LayoutEditor layoutEditor) {
        this(id, t, c, rot, xFactor, yFactor, layoutEditor, 1);
    }

    /**
     * constructor method
     */
    public LayoutTurnout(String id, int t, Point2D c, double rot,
            double xFactor, double yFactor, LayoutEditor layoutEditor, int v) {
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

        LayoutTrack.defaultTrackColor = ColorUtil.stringToColor(layoutEditor.getDefaultTrackColor());
    }

    // this should only be used for debugging...
    public String toString() {
        return "LayoutTurnout " + ident;
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
            return namedTurnout.getName();
        }
        return turnoutName;
    }

    public String getSecondTurnoutName() {
        if (secondNamedTurnout != null) {
            return secondNamedTurnout.getName();
        }
        return secondTurnoutName;
    }

    public boolean getSecondTurnoutInverted() {
        return secondTurnoutInverted;
    }

    public String getBlockName() {
        return blockName;
    }

    public String getBlockBName() {
        return blockBName;
    }

    public String getBlockCName() {
        return blockCName;
    }

    public String getBlockDName() {
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

    public void setSignalA1Name(String signalHead) {
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

    public void setSignalA2Name(String signalHead) {
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

    public void setSignalA3Name(String signalHead) {
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

    public void setSignalB1Name(String signalHead) {
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

    public void setSignalB2Name(String signalHead) {
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

    public void setSignalC1Name(String signalHead) {
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

    public void setSignalC2Name(String signalHead) {
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

    public void setSignalD1Name(String signalHead) {
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

    public void setSignalD2Name(String signalHead) {
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

    public void setSignalAMast(String signalMast) {
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

    public void setSignalBMast(String signalMast) {
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

    public void setSignalCMast(String signalMast) {
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

    public void setSignalDMast(String signalMast) {
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

    public void setSensorA(String sensorName) {
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

    public void setSensorB(String sensorName) {
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

    public void setSensorC(String sensorName) {
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

    public void setSensorD(String sensorName) {
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

    public void setLinkedTurnoutName(String s) {
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

    public Object getConnectA() {
        return connectA;
    }

    public Object getConnectB() {
        return connectB;
    }

    public Object getConnectC() {
        return connectC;
    }

    public Object getConnectD() {
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

    public void setTurnout(String tName) {
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

    public void setSecondTurnout(String tName) {

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
        if ((type == RH_TURNOUT) || (type == LH_TURNOUT) || (type == WYE_TURNOUT)) {
            if (oldSecondTurnoutName != null && !oldSecondTurnoutName.isEmpty()) {
                Turnout oldTurnout = InstanceManager.turnoutManagerInstance().getTurnout(oldSecondTurnoutName);
                LayoutTurnout oldLinked = layoutEditor.getFinder().findLayoutTurnoutByTurnoutName(oldTurnout.getSystemName());
                if (oldLinked == null) {
                    oldLinked = layoutEditor.getFinder().findLayoutTurnoutByTurnoutName(oldTurnout.getUserName());
                }
                if ((oldLinked != null) && oldLinked.getSecondTurnout() == getTurnout()) {
                    oldLinked.setSecondTurnout(null);
                }
            }
            if (turnout != null) {
                LayoutTurnout newLinked = layoutEditor.getFinder().findLayoutTurnoutByTurnoutName(turnout.getSystemName());
                if (newLinked == null) {
                    newLinked = layoutEditor.getFinder().findLayoutTurnoutByTurnoutName(turnout.getUserName());
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
     * get the object connected to this track for the specified connection type
     *
     * @param connectionType the specified connection type
     * @return the object connected for the specified connection type
     * @throws jmri.JmriException - if the connectionType is invalid
     */
    @Override
    public Object getConnection(int connectionType) throws jmri.JmriException {
        Object result = null;
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
     * set the object connected for the specified connection type
     *
     * @param connectionType the connection type (where it is connected to us)
     * @param o              the object that is being connected
     * @param type           the type of object that we're being connected to
     *                       (Should always be "NONE" or "TRACK")
     * @throws jmri.JmriException - if connectionType or type are invalid
     */
    @Override
    public void setConnection(int connectionType, Object o, int type) throws jmri.JmriException {
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

    public void setConnectA(Object o, int type) {
        connectA = o;
        if ((type != TRACK) && (type != NONE)) {
            log.error("unexpected type of A connection to layoutturnout - " + type);
        }
    }

    public void setConnectB(Object o, int type) {
        connectB = o;
        if ((type != TRACK) && (type != NONE)) {
            log.error("unexpected type of B connection to layoutturnout - " + type);
        }
    }

    public void setConnectC(Object o, int type) {
        connectC = o;
        if ((type != TRACK) && (type != NONE)) {
            log.error("unexpected type of C connection to layoutturnout - " + type);
        }
    }

    public void setConnectD(Object o, int type) {
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
        if ((type == DOUBLE_XOVER) || (type == LH_XOVER) || (type == RH_XOVER)) {
            if (version == 2) {
                return pointA;
            }
            return MathUtil.subtract(center, dispA);
        } else if (type == WYE_TURNOUT) {
            return MathUtil.subtract(center, MathUtil.midPoint(dispB, dispA));
        } else {
            return MathUtil.subtract(center, dispB);
        }
    }

    public Point2D getCoordsB() {
        if ((version == 2) && ((type == DOUBLE_XOVER) || (type == LH_XOVER) || (type == RH_XOVER))) {
            return pointB;
        }
        return MathUtil.add(center, dispB);
    }

    public Point2D getCoordsC() {
        if ((version == 2) && ((type == DOUBLE_XOVER) || (type == LH_XOVER) || (type == RH_XOVER))) {
            return pointC;
        }
        return MathUtil.add(center, dispA);
    }

    public Point2D getCoordsD() {
        if ((version == 2) && ((type == DOUBLE_XOVER) || (type == LH_XOVER) || (type == RH_XOVER))) {
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
        double circleRadius = controlPointSize * layoutEditor.getTurnoutCircleSize();
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
        if ((type == DOUBLE_XOVER) || (type == LH_XOVER) || (type == RH_XOVER)) {
            result.add(getCoordsD());
        }
        return result;
    }

    // updates connectivity for blocks assigned to this turnout and connected track segments
    private void updateBlockInfo() {
        LayoutBlock bA = null;
        LayoutBlock bB = null;
        LayoutBlock bC = null;
        LayoutBlock bD = null;
        layoutEditor.auxTools.setBlockConnectivityChanged();
        if (block != null) {
            block.updatePaths();
        }
        if (connectA != null) {
            bA = ((TrackSegment) connectA).getLayoutBlock();
            if ((bA != null) && (bA != block)) {
                bA.updatePaths();
            }
        }
        if ((blockB != null) && (blockB != block) && (blockB != bA)) {
            blockB.updatePaths();
        }
        if (connectB != null) {
            bB = ((TrackSegment) connectB).getLayoutBlock();
            if ((bB != null) && (bB != block) && (bB != bA) && (bB != blockB)) {
                bB.updatePaths();
            }
        }
        if ((blockC != null) && (blockC != block) && (blockC != bA)
                && (blockC != bB) && (blockC != blockB)) {
            blockC.updatePaths();
        }
        if (connectC != null) {
            bC = ((TrackSegment) connectC).getLayoutBlock();
            if ((bC != null) && (bC != block) && (bC != bA) && (bC != blockB) && (bC != bB)
                    && (bC != blockC)) {
                bC.updatePaths();
            }
        }
        if ((blockD != null) && (blockD != block) && (blockD != bA)
                && (blockD != bB) && (blockD != blockB) && (blockD != bC)
                && (blockD != blockC)) {
            blockD.updatePaths();
        }
        if (connectD != null) {
            bD = ((TrackSegment) connectD).getLayoutBlock();
            if ((bD != null) && (bD != block) && (bD != bA) && (bD != blockB) && (bD != bB)
                    && (bD != blockC) && (bD != bC) && (bD != blockD)) {
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
        if ((type == LH_TURNOUT) || (type == RH_TURNOUT)) {
            layoutEditor.setTurnoutBX(Math.round(lenB + 0.1));
            double xc = ((bX * cX) + (bY * cY)) / lenB;
            layoutEditor.setTurnoutCX(Math.round(xc + 0.1));
            layoutEditor.setTurnoutWid(Math.round(Math.sqrt((lenC * lenC) - (xc * xc)) + 0.1));
        } else if (type == WYE_TURNOUT) {
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
                if (type == DOUBLE_XOVER) {
                    double lenBC = Math.hypot(bX - cX, bY - cY);
                    layoutEditor.setXOverLong(Math.round(lenAB / 2)); //set to half to be backwardly compatible
                    layoutEditor.setXOverHWid(Math.round(lenBC / 2));
                    layoutEditor.setXOverShort(Math.round((0.5 * lenAB) / 2));
                } else if (type == RH_XOVER) {
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
                } else if (type == LH_XOVER) {
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
            } else if (type == DOUBLE_XOVER) {
                double lng = Math.sqrt((lenB * lenB) - (0.25 * (distBC * distBC)));
                layoutEditor.setXOverLong(Math.round(lng + 0.1));
                layoutEditor.setXOverHWid(Math.round((0.5 * distBC) + 0.1));
                layoutEditor.setXOverShort(Math.round((0.5 * lng) + 0.1));
            } else if (type == RH_XOVER) {
                double distDC = Math.hypot(bX + cX, bY + cY);
                layoutEditor.setXOverShort(Math.round((0.25 * distDC) + 0.1));
                layoutEditor.setXOverLong(Math.round((0.75 * distDC) + 0.1));
                double hwid = Math.sqrt((lenC * lenC) - (0.5625 * distDC * distDC));
                layoutEditor.setXOverHWid(Math.round(hwid + 0.1));
            } else if (type == LH_XOVER) {
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
        block = b;
        if (b != null) {
            blockName = b.getId();
        } else {
            blockName = "";
        }
    }

    public void setLayoutBlockB(LayoutBlock b) {
        if ((type == DOUBLE_XOVER) || (type == LH_XOVER) || (type == RH_XOVER)) {
            blockB = b;
            if (b != null) {
                blockBName = b.getId();
            } else {
                blockBName = "";
            }
        } else {
            log.error("Attempt to set block B, but not a crossover");
        }
    }

    public void setLayoutBlockC(LayoutBlock b) {
        if ((type == DOUBLE_XOVER) || (type == LH_XOVER) || (type == RH_XOVER)) {
            blockC = b;
            if (b != null) {
                blockCName = b.getId();
            } else {
                blockCName = "";
            }
        } else {
            log.error("Attempt to set block C, but not a crossover");
        }
    }

    public void setLayoutBlockD(LayoutBlock b) {
        if ((type == DOUBLE_XOVER) || (type == LH_XOVER) || (type == RH_XOVER)) {
            blockD = b;
            if (b != null) {
                blockDName = b.getId();
            } else {
                blockDName = "";
            }
        } else {
            log.error("Attempt to set block D, but not a crossover");
        }
    }

    public void setLayoutBlockByName(String name) {
        blockName = name;
    }

    public void setLayoutBlockBByName(String name) {
        if ((type == DOUBLE_XOVER) || (type == LH_XOVER) || (type == RH_XOVER)) {
            blockBName = name;
        } else {
            log.error("Attempt to set block B name, but not a crossover");
        }
    }

    public void setLayoutBlockCByName(String name) {
        if ((type == DOUBLE_XOVER) || (type == LH_XOVER) || (type == RH_XOVER)) {
            blockCName = name;
        } else {
            log.error("Attempt to set block C name, but not a crossover");
        }
    }

    public void setLayoutBlockDByName(String name) {
        if ((type == DOUBLE_XOVER) || (type == LH_XOVER) || (type == RH_XOVER)) {
            blockDName = name;
        } else {
            log.error("Attempt to set block D name, but not a crossover");
        }
    }

    /**
     * Methods to test if turnout legs are mainline track or not Returns true if
     * connecting track segment is mainline Defaults to not mainline if
     * connecting track segment is missing
     */
    public boolean isMainlineA() {
        if (connectA != null) {
            return ((TrackSegment) connectA).getMainline();
        } else {
            // if no connection, depends on type of turnout
            if ((type == DOUBLE_XOVER) || (type == LH_XOVER) || (type == RH_XOVER)) {
                // All crossovers - straight continuing is B
                if (connectB != null) {
                    return ((TrackSegment) connectB).getMainline();
                }
            } // must be RH, LH, or WYE turnout - A is the switch throat
            else if (((connectB != null) && (((TrackSegment) connectB).getMainline()))
                    || ((connectC != null) && (((TrackSegment) connectC).getMainline()))) {
                return true;
            }
        }
        return false;
    }

    public boolean isMainlineB() {
        if (connectB != null) {
            return ((TrackSegment) connectB).getMainline();
        } else {
            // if no connection, depends on type of turnout
            if ((type == DOUBLE_XOVER) || (type == LH_XOVER) || (type == RH_XOVER)) {
                // All crossovers - straight continuing is A
                if (connectA != null) {
                    return ((TrackSegment) connectA).getMainline();
                }
            } // must be RH, LH, or WYE turnout - A is the switch throat,
            //      B is normally the continuing straight
            else if (continuingSense == Turnout.CLOSED) {
                // user hasn't changed the continuing turnout state
                if (connectA != null) { // if throat is mainline, this leg must be also
                    return ((TrackSegment) connectA).getMainline();
                }
            }
        }
        return false;
    }

    public boolean isMainlineC() {
        if (connectC != null) {
            return ((TrackSegment) connectC).getMainline();
        } else {
            // if no connection, depends on type of turnout
            if ((type == DOUBLE_XOVER) || (type == LH_XOVER) || (type == RH_XOVER)) {
                // All crossovers - straight continuing is D
                if (connectD != null) {
                    return ((TrackSegment) connectD).getMainline();
                }
            } // must be RH, LH, or WYE turnout - A is the switch throat,
            //      B is normally the continuing straight
            else if (continuingSense == Turnout.THROWN) {
                // user has changed the continuing turnout state
                if (connectA != null) { // if throat is mainline, this leg must be also
                    return ((TrackSegment) connectA).getMainline();
                }
            }
        }
        return false;
    }

    public boolean isMainlineD() {
        // this is a crossover turnout
        if (connectD != null) {
            return ((TrackSegment) connectD).getMainline();
        } else if (connectC != null) {
            return ((TrackSegment) connectC).getMainline();
        }
        return false;
    }

    /**
     * find the hit (location) type for a point
     *
     * @param p                  the point
     * @param useRectangles      whether to use (larger) rectangles or (smaller)
     *                           circles for hit testing
     * @param requireUnconnected only return free connection hit types
     * @return the location type for the point (or NONE)
     * @since 7.4.3
     */
    protected int findHitPointType(Point2D p, boolean useRectangles, boolean requireUnconnected) {
        int result = NONE;  // assume point not on connection

        if (useRectangles) {
            // calculate points control rectangle
            Rectangle2D r = layoutEditor.trackControlCircleRectAt(p);

            if (!requireUnconnected) {
                if (r.contains(center)) {
                    result = LayoutTrack.TURNOUT_CENTER;
                }
            }
            //check the A connection point
            if (NONE == result) {
                if (!requireUnconnected || (getConnectA() == null)) {
                    if (r.contains(getCoordsA())) {
                        result = LayoutTrack.TURNOUT_A;
                    }
                }
            }

            //check the B connection point
            if (NONE == result) {
                if (!requireUnconnected || (getConnectB() == null)) {
                    if (r.contains(getCoordsB())) {
                        result = LayoutTrack.TURNOUT_B;
                    }
                }
            }

            //check the C connection point
            if (NONE == result) {
                if (!requireUnconnected || (getConnectC() == null)) {
                    if (r.contains(getCoordsC())) {
                        result = LayoutTrack.TURNOUT_C;
                    }
                }
            }

            //check the D connection point
            if (NONE == result) {
                if ((type == DOUBLE_XOVER) || (type == LH_XOVER) || (type == RH_XOVER)) {
                    if (!requireUnconnected || (getConnectD() == null)) {
                        if (r.contains(getCoordsD())) {
                            result = LayoutTrack.TURNOUT_D;
                        }
                    }
                }
            }
        } else {
            // calculate radius of turnout control circle
            double circleRadius = controlPointSize * layoutEditor.getTurnoutCircleSize();

            if (!requireUnconnected) {
                // calculate the distance to the center point of this turnout
                Double distance = p.distance(getCoordsCenter());
                if (distance <= circleRadius) {
                    result = TURNOUT_CENTER;
                }
            }

            //check the A connection point
            if (NONE == result) {
                if (!requireUnconnected || (getConnectA() == null)) {
                    Double distance = p.distance(getCoordsA());
                    if (distance <= circleRadius) {
                        result = LayoutTrack.TURNOUT_A;
                    }
                }
            }

            //check the B connection point
            if (NONE == result) {
                if (!requireUnconnected || (getConnectB() == null)) {
                    Double distance = p.distance(getCoordsB());
                    if (distance <= circleRadius) {
                        result = LayoutTrack.TURNOUT_B;
                    }
                }
            }

            //check the C connection point
            if (NONE == result) {
                if (!requireUnconnected || (getConnectC() == null)) {
                    Double distance = p.distance(getCoordsC());
                    if (distance <= circleRadius) {
                        result = LayoutTrack.TURNOUT_C;
                    }
                }
            }

            //check the D connection point
            if (NONE == result) {
                if ((type == DOUBLE_XOVER) || (type == LH_XOVER) || (type == RH_XOVER)) {
                    if (!requireUnconnected || (getConnectD() == null)) {
                        Double distance = p.distance(getCoordsD());
                        if (distance <= circleRadius) {
                            result = LayoutTrack.TURNOUT_D;
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
    public void setCoordsCenter(Point2D p) {
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

    public void setCoordsA(Point2D p) {
        pointA = p;
        if (version == 2) {
            reCalculateCenter();
        }
        double x = center.getX() - p.getX();
        double y = center.getY() - p.getY();
        if (type == DOUBLE_XOVER) {
            dispA = new Point2D.Double(x, y);
            // adjust to maintain rectangle
            double oldLength = MathUtil.length(dispB);
            double newLength = Math.hypot(x, y);
            dispB = MathUtil.multiply(dispB, newLength / oldLength);
        } else if ((type == RH_XOVER) || (type == LH_XOVER)) {
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
            if (type == RH_XOVER) {
                x = xi - (0.333333 * (-x - xi));
                y = yi - (0.333333 * (-y - yi));
            } else if (type == LH_XOVER) {
                x = xi - (3.0 * (-x - xi));
                y = yi - (3.0 * (-y - yi));
            }
            dispB = new Point2D.Double(x, y);
        } else if (type == WYE_TURNOUT) {
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
        if ((type == DOUBLE_XOVER) || (type == WYE_TURNOUT)) {
            // adjust to maintain rectangle or wye shape
            double oldLength = MathUtil.length(dispA);
            double newLength = Math.hypot(x, y);
            dispA = MathUtil.multiply(dispA, newLength / oldLength);
        } else if ((type == RH_XOVER) || (type == LH_XOVER)) {
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
            if (type == LH_XOVER) {
                x = xi - (0.333333 * (x - xi));
                y = yi - (0.333333 * (y - yi));
            } else if (type == RH_XOVER) {
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
        if ((type == DOUBLE_XOVER) || (type == WYE_TURNOUT)) {
            // adjust to maintain rectangle or wye shape
            double oldLength = MathUtil.length(dispB);
            double newLength = Math.hypot(x, y);
            dispB = MathUtil.multiply(dispB, newLength / oldLength);
        } else if ((type == RH_XOVER) || (type == LH_XOVER)) {
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
            if (type == RH_XOVER) {
                x = xi - (0.333333 * (-x - xi));
                y = yi - (0.333333 * (-y - yi));
            } else if (type == LH_XOVER) {
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
        if (type == DOUBLE_XOVER) {
            // adjust to maintain rectangle
            double oldLength = MathUtil.length(dispA);
            double newLength = Math.hypot(x, y);
            dispA = MathUtil.multiply(dispA, newLength / oldLength);
        } else if ((type == RH_XOVER) || (type == LH_XOVER)) {
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
            if (type == LH_XOVER) {
                x = xi - (0.333333 * (-x - xi));
                y = yi - (0.333333 * (-y - yi));
            } else if (type == RH_XOVER) {
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
            namedTurnout.getBean().removePropertyChangeListener(mTurnoutListener);
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
        if ((type == RH_TURNOUT) || (type == LH_TURNOUT) || (type == WYE_TURNOUT)) {
            if (block.getOccupancy() == LayoutBlock.OCCUPIED) {
                log.debug("Block " + blockName + "is Occupied");
                return true;
            }
        }
        if ((type == DOUBLE_XOVER) || (type == RH_XOVER) || (type == LH_XOVER)) {
            //If the turnout is set for straight over, we need to deal with the straight over connecting blocks
            if (getTurnout().getKnownState() == Turnout.CLOSED) {
                if ((block.getOccupancy() == LayoutBlock.OCCUPIED) && (blockB.getOccupancy() == LayoutBlock.OCCUPIED)) {
                    log.debug("Blocks " + blockName + " & " + blockBName + " are Occupied");
                    return true;
                }
                if ((blockC.getOccupancy() == LayoutBlock.OCCUPIED) && (blockD.getOccupancy() == LayoutBlock.OCCUPIED)) {
                    log.debug("Blocks " + blockCName + " & " + blockDName + " are Occupied");
                    return true;
                }
            }

        }
        if ((type == DOUBLE_XOVER) || (type == LH_XOVER)) {
            if (getTurnout().getKnownState() == Turnout.THROWN) {
                if ((blockB.getOccupancy() == LayoutBlock.OCCUPIED) && (blockD.getOccupancy() == LayoutBlock.OCCUPIED)) {
                    log.debug("Blocks " + blockBName + " & " + blockDName + " are Occupied");
                    return true;
                }
            }
        }

        if ((type == DOUBLE_XOVER) || (type == RH_XOVER)) {
            if (getTurnout().getKnownState() == Turnout.THROWN) {
                if ((block.getOccupancy() == LayoutBlock.OCCUPIED) && (blockC.getOccupancy() == LayoutBlock.OCCUPIED)) {
                    log.debug("Blocks " + block + " & " + blockCName + " are Occupied");
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
    public String tBlockName = "";
    public String tBlockBName = "";
    public String tBlockCName = "";
    public String tBlockDName = "";
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
        if (!tBlockName.isEmpty()) {
            block = p.getLayoutBlock(tBlockName);
            if (block != null) {
                blockName = tBlockName;
                block.incrementUse();
            } else {
                log.error("bad blockname '" + tBlockName + "' in layoutturnout " + ident);
            }
        }
        if (!tBlockBName.isEmpty()) {
            blockB = p.getLayoutBlock(tBlockBName);
            if (blockB != null) {
                blockBName = tBlockBName;
                if (block != blockB) {
                    blockB.incrementUse();
                }
            } else {
                log.error("bad blockname '" + tBlockBName + "' in layoutturnout " + ident);
            }
        }
        if (!tBlockCName.isEmpty()) {
            blockC = p.getLayoutBlock(tBlockCName);
            if (blockC != null) {
                blockCName = tBlockCName;
                if ((block != blockC) && (blockB != blockC)) {
                    blockC.incrementUse();
                }
            } else {
                log.error("bad blockname '" + tBlockCName + "' in layoutturnout " + ident);
            }
        }
        if (!tBlockDName.isEmpty()) {
            blockD = p.getLayoutBlock(tBlockDName);
            if (blockD != null) {
                blockDName = tBlockDName;
                if ((block != blockD) && (blockB != blockD)
                        && (blockC != blockD)) {
                    blockD.incrementUse();
                }
            } else {
                log.error("bad blockname '" + tBlockDName + "' in layoutturnout " + ident);
            }
        }

        //Do the second one first then the activate is only called the once
        if (!tSecondTurnoutName.isEmpty()) {
            Turnout turnout = InstanceManager.turnoutManagerInstance().getTurnout(tSecondTurnoutName);
            if (turnout != null) {
                secondNamedTurnout = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(tSecondTurnoutName, turnout);
                secondTurnoutName = tSecondTurnoutName;
            } else {
                log.error("bad turnoutname '" + tSecondTurnoutName + "' in layoutturnout " + ident);
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
                log.error("bad turnoutname '" + tTurnoutName + "' in layoutturnout " + ident);
                turnoutName = "";
                namedTurnout = null;
            }
        }
    }   // setObjects

    private JPopupMenu popup = null;
    private LayoutEditorTools tools = null;

    /**
     * Display popup menu for information and editing
     */
    protected void showPopup(MouseEvent e) {
        if (popup != null) {
            popup.removeAll();
        } else {
            popup = new JPopupMenu();
        }
        if (tools == null) {
            tools = new LayoutEditorTools(layoutEditor);
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
                    label = rb.getString("WYETurnout");
                    break;
                case DOUBLE_XOVER:
                    label = rb.getString("DoubleCrossover");
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
            JMenuItem jmi = popup.add(Bundle.getMessage("MakeLabel", label) + ident);
            jmi.setEnabled(false);

            if (getTurnout() == null) {
                jmi = popup.add(rb.getString("NoTurnout"));
            } else {
                jmi = popup.add(Bundle.getMessage("BeanNameTurnout")
                        + ": " + getTurnoutName());
            }
            jmi.setEnabled(false);

            if (getSecondTurnout() != null) {
                jmi = popup.add(Bundle.getMessage("Supporting",
                        Bundle.getMessage("BeanNameTurnout"))
                        + ": " + getSecondTurnoutName());
            }
            jmi.setEnabled(false);

            if (blockName.isEmpty()) {
                jmi = popup.add(rb.getString("NoBlock"));
            } else {
                jmi = popup.add(Bundle.getMessage("MakeLabel", Bundle.getMessage("BeanNameBlock")) + block.getDisplayName());
            }
            jmi.setEnabled(false);

            if ((type == DOUBLE_XOVER) || (type == RH_XOVER) || (type == LH_XOVER)) {
                // check if extra blocks have been entered
                if (blockB != null) {
                    jmi = popup.add(Bundle.getMessage("MakeLabel", Bundle.getMessage("Block_ID", 2)) + blockB.getDisplayName());
                    jmi.setEnabled(false);
                }
                if (blockC != null) {
                    jmi = popup.add(Bundle.getMessage("MakeLabel", Bundle.getMessage("Block_ID", 3)) + blockC.getDisplayName());
                    jmi.setEnabled(false);
                }
                if (blockD != null) {
                    jmi = popup.add(Bundle.getMessage("MakeLabel", Bundle.getMessage("Block_ID", 4)) + blockD.getDisplayName());
                    jmi.setEnabled(false);
                }
            }

            // if there are any track connections
            if ((connectA != null) || (connectB != null)
                    || (connectC != null) || (connectD != null)) {
                JMenu connectionsMenu = new JMenu(Bundle.getMessage("Connections")); // there is no pane opening (which is what ... implies)
                if (connectA != null) {
                    connectionsMenu.add(new AbstractAction(Bundle.getMessage("MakeLabel", "A") + ((LayoutTrack) connectA).getName()) {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            LayoutEditorFindItems lf = layoutEditor.getFinder();
                            LayoutTrack lt = (LayoutTrack) lf.findObjectByName(((LayoutTrack) connectA).getName());
                            layoutEditor.setSelectionRect(lt.getBounds());
                        }
                    });
                }
                if (connectB != null) {
                    connectionsMenu.add(new AbstractAction(Bundle.getMessage("MakeLabel", "B") + ((LayoutTrack) connectB).getName()) {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            LayoutEditorFindItems lf = layoutEditor.getFinder();
                            LayoutTrack lt = (LayoutTrack) lf.findObjectByName(((LayoutTrack) connectB).getName());
                            layoutEditor.setSelectionRect(lt.getBounds());
                        }
                    });
                }
                if (connectC != null) {
                    connectionsMenu.add(new AbstractAction(Bundle.getMessage("MakeLabel", "C") + ((LayoutTrack) connectC).getName()) {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            LayoutEditorFindItems lf = layoutEditor.getFinder();
                            LayoutTrack lt = (LayoutTrack) lf.findObjectByName(((LayoutTrack) connectC).getName());
                            layoutEditor.setSelectionRect(lt.getBounds());
                        }
                    });
                }
                if (connectD != null) {
                    connectionsMenu.add(new AbstractAction(Bundle.getMessage("MakeLabel", "D") + ((LayoutTrack) connectD).getName()) {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            LayoutEditorFindItems lf = layoutEditor.getFinder();
                            LayoutTrack lt = (LayoutTrack) lf.findObjectByName(((LayoutTrack) connectD).getName());
                            layoutEditor.setSelectionRect(lt.getBounds());
                        }
                    });
                }
                popup.add(connectionsMenu);
            }
            popup.add(new JSeparator(JSeparator.HORIZONTAL));

            JCheckBoxMenuItem hiddenCheckBoxMenuItem = new JCheckBoxMenuItem(rb.getString("Hidden"));
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

            cbmi = new JCheckBoxMenuItem(rb.getString("DisabledWhenOccupied"));
            cbmi.setSelected(disableWhenOccupied);
            popup.add(cbmi);
            cbmi.addActionListener((java.awt.event.ActionEvent e3) -> {
                JCheckBoxMenuItem o = (JCheckBoxMenuItem) e3.getSource();
                setDisableWhenOccupied(o.isSelected());
            });

            // Rotate if there are no track connections
            if ((connectA == null) && (connectB == null)
                    && (connectC == null) && (connectD == null)) {
                JMenuItem rotateItem = new JMenuItem(rb.getString("Rotate") + "...");
                popup.add(rotateItem);
                rotateItem.addActionListener((ActionEvent event) -> {
                    boolean entering = true;
                    boolean error = false;
                    String newAngle = "";
                    while (entering) {
                        // prompt for rotation angle
                        error = false;
                        newAngle = JOptionPane.showInputDialog(layoutEditor,
                                Bundle.getMessage("MakeLabel", rb.getString("EnterRotation")));
                        if (newAngle.isEmpty()) {
                            return;  // cancelled
                        }
                        double rot = 0.0;
                        try {
                            rot = Double.parseDouble(newAngle);
                        } catch (Exception e1) {
                            JOptionPane.showMessageDialog(layoutEditor, rb.getString("Error3")
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

            popup.add(new AbstractAction(rb.getString("UseSizeAsDefault")) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    setUpDefaultSize();
                }
            });
            popup.add(new AbstractAction(Bundle.getMessage("ButtonEdit")) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    editLayoutTurnout();
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
                AbstractAction ssaa = new AbstractAction(rb.getString("SetSignals")) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if ((getTurnoutType() == DOUBLE_XOVER) || (getTurnoutType() == RH_XOVER) || (getTurnoutType() == LH_XOVER)) {
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
                if (tools.addLayoutTurnoutSignalHeadInfoToMenu(getTurnoutName(), linkedTurnoutName, jm)) {
                    jm.add(ssaa);
                    popup.add(jm);
                } else {
                    popup.add(ssaa);
                }
            }
            if (!blockName.isEmpty()) {
                final String[] boundaryBetween = getBlockBoundaries();
                boolean blockBoundaries = false;
                for (int i = 0; i < 4; i++) {
                    if (boundaryBetween[i] != null) {
                        blockBoundaries = true;
                    }
                }
                if (InstanceManager.getDefault(LayoutBlockManager.class).isAdvancedRoutingEnabled()) {
                    if (blockBName.isEmpty() && blockCName.isEmpty() && blockDName.isEmpty()) {
                        popup.add(new AbstractAction(rb.getString("ViewBlockRouting")) {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                AbstractAction routeTableAction = new LayoutBlockRouteTableAction("ViewRouting", getLayoutBlock());
                                routeTableAction.actionPerformed(e);
                            }
                        });
                    } else {
                        JMenu viewRouting = new JMenu(rb.getString("ViewBlockRouting"));
                        viewRouting.add(new AbstractAction(blockName) {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                AbstractAction routeTableAction = new LayoutBlockRouteTableAction(blockName, getLayoutBlock());
                                routeTableAction.actionPerformed(e);
                            }
                        });
                        if (!blockBName.isEmpty() && !blockBName.equals(blockName)) {
                            viewRouting.add(new AbstractAction(blockBName) {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    AbstractAction routeTableAction = new LayoutBlockRouteTableAction(blockBName, getLayoutBlockB());
                                    routeTableAction.actionPerformed(e);
                                }
                            });
                        }

                        if (!blockCName.isEmpty() && !blockCName.equals(blockName) && !blockCName.equals(blockBName)) {
                            viewRouting.add(new AbstractAction(blockCName) {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    AbstractAction routeTableAction = new LayoutBlockRouteTableAction(blockCName, getLayoutBlockC());
                                    routeTableAction.actionPerformed(e);
                                }
                            });
                        }

                        if (!blockDName.isEmpty() && !blockDName.equals(blockName) && !blockDName.equals(blockBName) && !blockDName.equals(blockCName)) {
                            viewRouting.add(new AbstractAction(blockDName) {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    AbstractAction routeTableAction = new LayoutBlockRouteTableAction(blockDName, getLayoutBlockD());
                                    routeTableAction.actionPerformed(e);
                                }
                            });
                        }

                        popup.add(viewRouting);
                    }
                }

                if (blockBoundaries) {
                    popup.add(new AbstractAction(rb.getString("SetSignalMasts")) {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            tools.setSignalMastsAtTurnoutFromMenu(LayoutTurnout.this,
                                    boundaryBetween);
                        }
                    });
                    popup.add(new AbstractAction(rb.getString("SetSensors")) {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            tools.setSensorsAtTurnoutFromMenu(
                                    LayoutTurnout.this,
                                    boundaryBetween,
                                    layoutEditor.sensorIconEditor,
                                    layoutEditor.sensorFrame);
                        }
                    });
                }
            }
            setAdditionalEditPopUpMenu(popup);
            layoutEditor.setShowAlignmentMenu(popup);
            popup.show(e.getComponent(), e.getX(), e.getY());
        } else if (!viewAdditionalMenu.isEmpty()) {
            setAdditionalViewPopUpMenu(popup);
            popup.show(e.getComponent(), e.getX(), e.getY());
        }
    }   // showPopup

    public String[] getBlockBoundaries() {
        final String[] boundaryBetween = new String[4];
        //ArrayList<String> boundaryBetween = new ArrayList<String>(4);
        if ((type == WYE_TURNOUT) || (type == RH_TURNOUT) || (type == LH_TURNOUT)) {
            //This should only be needed where we are looking at a single turnout.
            if (block != null) {
                LayoutBlock aLBlock = null;
                if (connectA instanceof TrackSegment) {
                    aLBlock = ((TrackSegment) connectA).getLayoutBlock();
                    if (aLBlock != block) {
                        try {
                            boundaryBetween[0] = (aLBlock.getDisplayName() + " - " + block.getDisplayName());
                        } catch (java.lang.NullPointerException e) {
                            //Can be considered normal if tracksegement hasn't yet been allocated a block
                            log.debug("TrackSegement at connection A doesn't contain a layout block");
                        }
                    }
                }

                LayoutBlock bLBlock = null;
                if (connectB instanceof TrackSegment) {
                    bLBlock = ((TrackSegment) connectB).getLayoutBlock();
                    if (bLBlock != block) {
                        try {
                            boundaryBetween[1] = (bLBlock.getDisplayName() + " - " + block.getDisplayName());
                        } catch (java.lang.NullPointerException e) {
                            //Can be considered normal if tracksegement hasn't yet been allocated a block
                            log.debug("TrackSegement at connection B doesn't contain a layout block");
                        }
                    }
                }

                LayoutBlock cLBlock = null;
                if ((connectC instanceof TrackSegment) && (((TrackSegment) connectC).getLayoutBlock() != block)) {
                    cLBlock = ((TrackSegment) connectC).getLayoutBlock();
                    if (cLBlock != block) {
                        try {
                            boundaryBetween[2] = (cLBlock.getDisplayName() + " - " + block.getDisplayName());
                        } catch (java.lang.NullPointerException e) {
                            //Can be considered normal if tracksegement hasn't yet been allocated a block
                            log.debug("TrackSegement at connection C doesn't contain a layout block");
                        }
                    }
                }
            }
        } else {
            /*ArrayList<LayoutBlock> localblks = new ArrayList<LayoutBlock>(4);
             if(block!=null)
             localblks.add(block);
             if(blockB!=null)
             localblks.add(blockB);
             if(blockC!=null)
             localblks.add(blockC);
             if(blockD!=null)
             localblks.add(blockD);*/

            LayoutBlock aLBlock = null;
            LayoutBlock bLBlock = null;
            LayoutBlock cLBlock = null;
            LayoutBlock dLBlock = null;
            if (block != null) {
                if (connectA instanceof TrackSegment) {
                    aLBlock = ((TrackSegment) connectA).getLayoutBlock();
                    if (aLBlock != block) {
                        try {
                            boundaryBetween[0] = (aLBlock.getDisplayName() + " - " + block.getDisplayName());
                        } catch (java.lang.NullPointerException e) {
                            //Can be considered normal if tracksegement hasn't yet been allocated a block
                            log.debug("TrackSegement at connection A doesn't contain a layout block");
                        }
                    } else if (block != blockB) {
                        try {
                            boundaryBetween[0] = (block.getDisplayName() + " - " + blockB.getDisplayName());
                        } catch (java.lang.NullPointerException e) {
                            //Can be considered normal if tracksegement hasn't yet been allocated a block
                            log.debug("TrackSegement at connection A doesn't contain a layout block");
                        }
                    }
                }

                if (connectB instanceof TrackSegment) {
                    bLBlock = ((TrackSegment) connectB).getLayoutBlock();

                    if (bLBlock != block && bLBlock != blockB) {
                        try {
                            boundaryBetween[1] = (bLBlock.getDisplayName() + " - " + blockB.getDisplayName());
                        } catch (java.lang.NullPointerException e) {
                            //Can be considered normal if tracksegement hasn't yet been allocated a block
                            log.debug("TrackSegement at connection B doesn't contain a layout block");
                        }
                    } else if (block != blockB) {
                        //This is an interal block on the turnout
                        try {
                            boundaryBetween[1] = (blockB.getDisplayName() + " - " + block.getDisplayName());
                        } catch (java.lang.NullPointerException e) {
                            //Can be considered normal if tracksegement hasn't yet been allocated a block
                            log.debug("TrackSegement at connection A doesn't contain a layout block");
                        }
                    }
                }

                if (connectC instanceof TrackSegment) {
                    cLBlock = ((TrackSegment) connectC).getLayoutBlock();
                    if (cLBlock != block && cLBlock != blockB && cLBlock != blockC) {
                        try {
                            boundaryBetween[2] = (cLBlock.getDisplayName() + " - " + blockC.getDisplayName());
                        } catch (java.lang.NullPointerException e) {
                            //Can be considered normal if tracksegement hasn't yet been allocated a block
                            log.debug("TrackSegement at connection C doesn't contain a layout block");
                        }
                    } else if (blockC != blockD) {
                        //This is an interal block on the turnout
                        try {
                            boundaryBetween[2] = (blockC.getDisplayName() + " - " + blockD.getDisplayName());
                        } catch (java.lang.NullPointerException e) {
                            //Can be considered normal if tracksegement hasn't yet been allocated a block
                            log.debug("TrackSegement at connection A doesn't contain a layout block");
                        }
                    }
                }

                if (connectD instanceof TrackSegment) {
                    dLBlock = ((TrackSegment) connectD).getLayoutBlock();
                    if (dLBlock != block && dLBlock != blockB && dLBlock != blockC && dLBlock != blockD) {
                        try {
                            boundaryBetween[3] = (dLBlock.getDisplayName() + " - " + blockD.getDisplayName());
                        } catch (java.lang.NullPointerException e) {
                            //Can be considered normal if tracksegement hasn't yet been allocated a block
                            log.debug("TrackSegement at connection C doesn't contain a layout block");
                        }
                    } else if (blockC != blockD) {
                        //This is an interal block on the turnout
                        try {
                            boundaryBetween[3] = (blockD.getDisplayName() + " - " + blockC.getDisplayName());
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

    // variables for Edit Layout Turnout pane
    protected JmriJFrame editLayoutTurnoutFrame = null;
    private JmriBeanComboBox firstTurnoutComboBox = null;
    private JmriBeanComboBox secondTurnoutComboBox = null;
    private JLabel secondTurnoutLabel = null;
    protected JmriBeanComboBox blockNameComboBox = new JmriBeanComboBox(
            InstanceManager.getDefault(BlockManager.class), null, JmriBeanComboBox.DisplayOptions.DISPLAYNAME);
    private JmriBeanComboBox blockBNameComboBox = new JmriBeanComboBox(
            InstanceManager.getDefault(BlockManager.class), null, JmriBeanComboBox.DisplayOptions.DISPLAYNAME);
    private JmriBeanComboBox blockCNameComboBox = new JmriBeanComboBox(
            InstanceManager.getDefault(BlockManager.class), null, JmriBeanComboBox.DisplayOptions.DISPLAYNAME);
    private JmriBeanComboBox blockDNameComboBox = new JmriBeanComboBox(
            InstanceManager.getDefault(BlockManager.class), null, JmriBeanComboBox.DisplayOptions.DISPLAYNAME);
    private JComboBox<String> stateBox = new JComboBox<String>();
    private JCheckBox hiddenBox = new JCheckBox(rb.getString("HideTurnout"));
    private int turnoutClosedIndex;
    private int turnoutThrownIndex;
    private JButton turnoutEditBlock;
    private JButton turnoutEditDone;
    private JButton turnoutEditCancel;
    private JButton turnoutEditBlockB;
    private JButton turnoutEditBlockC;
    private JButton turnoutEditBlockD;
    private boolean editOpen = false;
    protected boolean needRedraw = false;
    protected boolean needsBlockUpdate = false;
    private JCheckBox additionalTurnout = new JCheckBox(rb.getString("SupportingTurnout"));
    private JCheckBox additionalTurnoutInvert = new JCheckBox(rb.getString("SecondTurnoutInvert"));

    /**
     * Edit a Layout Turnout
     */
    protected void editLayoutTurnout() {
        if (editOpen) {
            editLayoutTurnoutFrame.setVisible(true);
            return;
        }
        // Initialize if needed
        if (editLayoutTurnoutFrame == null) {
            editLayoutTurnoutFrame = new JmriJFrame(rb.getString("EditTurnout"), false, true);
            editLayoutTurnoutFrame.addHelpMenu("package.jmri.jmrit.display.EditLayoutTurnout", true);
            editLayoutTurnoutFrame.setLocation(50, 30);
            Container contentPane = editLayoutTurnoutFrame.getContentPane();
            contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
            // setup turnout name
            JPanel panel1 = new JPanel();
            panel1.setLayout(new FlowLayout());
            JLabel turnoutNameLabel = new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("BeanNameTurnout")));
            panel1.add(turnoutNameLabel);

            // add combobox to select turnout
            firstTurnoutComboBox = new JmriBeanComboBox(InstanceManager.turnoutManagerInstance(), getTurnout(), JmriBeanComboBox.DisplayOptions.DISPLAYNAME);
            LayoutEditor.setupComboBox(firstTurnoutComboBox, true, true);

            // disable items that are already in use
            PopupMenuListener pml = new PopupMenuListener() {
                @Override
                public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                    // This method is called before the popup menu becomes visible.
                    log.debug("PopupMenuWillBecomeVisible");
                    Object o = e.getSource();
                    if (o instanceof JmriBeanComboBox) {
                        JmriBeanComboBox jbcb = (JmriBeanComboBox) o;
                        jmri.Manager m = jbcb.getManager();
                        if (m != null) {
                            String[] systemNames = m.getSystemNameArray();
                            for (int idx = 0; idx < systemNames.length; idx++) {
                                String systemName = systemNames[idx];
                                jbcb.setItemEnabled(idx, layoutEditor.validatePhysicalTurnout(systemName, null));
                            }
                        }
                    }
                }

                @Override
                public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                    // This method is called before the popup menu becomes invisible
                    log.debug("PopupMenuWillBecomeInvisible");
                }

                @Override
                public void popupMenuCanceled(PopupMenuEvent e) {
                    // This method is called when the popup menu is canceled
                    log.debug("PopupMenuCanceled");
                }
            };

            firstTurnoutComboBox.addPopupMenuListener(pml);
            firstTurnoutComboBox.setEnabledColor(Color.green.darker().darker());
            firstTurnoutComboBox.setDisabledColor(Color.red);

            panel1.add(firstTurnoutComboBox);
            contentPane.add(panel1);

            JPanel panel1a = new JPanel();
            panel1a.setLayout(new BoxLayout(panel1a, BoxLayout.Y_AXIS));

            secondTurnoutComboBox = new JmriBeanComboBox(InstanceManager.turnoutManagerInstance(), getSecondTurnout(), JmriBeanComboBox.DisplayOptions.DISPLAYNAME);
            LayoutEditor.setupComboBox(secondTurnoutComboBox, true, false);

            secondTurnoutComboBox.addPopupMenuListener(pml);
            secondTurnoutComboBox.setEnabledColor(Color.green.darker().darker());
            secondTurnoutComboBox.setDisabledColor(Color.red);

            additionalTurnout.addActionListener((ActionEvent e) -> {
                boolean additionalEnabled = additionalTurnout.isSelected();
                secondTurnoutLabel.setEnabled(additionalEnabled);
                secondTurnoutComboBox.setEnabled(additionalEnabled);
                additionalTurnoutInvert.setEnabled(additionalEnabled);
            });
            if ((type != DOUBLE_XOVER) && (type != RH_XOVER) && (type != LH_XOVER)) {
                additionalTurnout.setText(rb.getString("ThrowTwoTurnouts"));
            }
            panel1a.add(additionalTurnout);
            contentPane.add(panel1a);

            secondTurnoutLabel = new JLabel(Bundle.getMessage("Supporting", Bundle.getMessage("BeanNameTurnout")));
            secondTurnoutLabel.setEnabled(false);
            JPanel panel1b = new JPanel();
            panel1b.add(secondTurnoutLabel);
            panel1b.add(secondTurnoutComboBox);
            additionalTurnoutInvert.addActionListener((ActionEvent e) -> {
                setSecondTurnoutInverted(additionalTurnoutInvert.isSelected());
            });
            additionalTurnoutInvert.setEnabled(false);
            panel1b.add(additionalTurnoutInvert);
            contentPane.add(panel1b);

            // add continuing state choice, if not crossover
            if ((type != DOUBLE_XOVER) && (type != RH_XOVER) && (type != LH_XOVER)) {
                JPanel panel3 = new JPanel();
                panel3.setLayout(new FlowLayout());
                stateBox.removeAllItems();
                stateBox.addItem(InstanceManager.turnoutManagerInstance().getClosedText());
                turnoutClosedIndex = 0;
                stateBox.addItem(InstanceManager.turnoutManagerInstance().getThrownText());
                turnoutThrownIndex = 1;
                stateBox.setToolTipText(rb.getString("StateToolTip"));
                panel3.add(new JLabel(rb.getString("ContinuingState")));
                panel3.add(stateBox);
                contentPane.add(panel3);
            }

            JPanel panel33 = new JPanel();
            panel33.setLayout(new FlowLayout());
            hiddenBox.setToolTipText(rb.getString("HiddenToolTip"));
            panel33.add(hiddenBox);
            contentPane.add(panel33);

            TitledBorder border = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black));
            border.setTitle(Bundle.getMessage("BeanNameBlock"));
            // setup block name
            JPanel panel2 = new JPanel();
            panel2.setBorder(border);
            panel2.setLayout(new FlowLayout());
            panel2.add(blockNameComboBox);
            LayoutEditor.setupComboBox(blockNameComboBox, false, true);
            blockNameComboBox.setToolTipText(rb.getString("EditBlockNameHint"));
            panel2.add(turnoutEditBlock = new JButton(rb.getString("CreateEdit")));
            turnoutEditBlock.addActionListener((ActionEvent e) -> {
                turnoutEditBlockPressed(e);
            });
            contentPane.add(panel2);
            if ((type == DOUBLE_XOVER) || (type == RH_XOVER) || (type == LH_XOVER)) {
                JPanel panel21 = new JPanel();
                panel21.setLayout(new FlowLayout());
                TitledBorder borderblk2 = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black));
                borderblk2.setTitle(Bundle.getMessage("BeanNameBlock") + " 2");
                panel21.setBorder(borderblk2);
                LayoutEditor.setupComboBox(blockBNameComboBox, false, true);
                blockBNameComboBox.setToolTipText(rb.getString("EditBlockBNameHint"));
                panel21.add(blockBNameComboBox);

                panel21.add(turnoutEditBlockB = new JButton(rb.getString("CreateEdit")));
                turnoutEditBlockB.addActionListener((ActionEvent e) -> {
                    turnoutEditBlockBPressed(e);
                });
                turnoutEditBlockB.setToolTipText(Bundle.getMessage("EditBlockHint", "2"));
                contentPane.add(panel21);

                JPanel panel22 = new JPanel();
                panel22.setLayout(new FlowLayout());
                TitledBorder borderblk3 = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black));
                borderblk3.setTitle(Bundle.getMessage("BeanNameBlock") + " 3");
                panel22.setBorder(borderblk3);
                LayoutEditor.setupComboBox(blockCNameComboBox, false, true);
                blockCNameComboBox.setToolTipText(rb.getString("EditBlockCNameHint"));
                panel22.add(blockCNameComboBox);
                panel22.add(turnoutEditBlockC = new JButton(rb.getString("CreateEdit")));
                turnoutEditBlockC.addActionListener((ActionEvent e) -> {
                    turnoutEditBlockCPressed(e);
                });
                turnoutEditBlockC.setToolTipText(Bundle.getMessage("EditBlockHint", "3"));
                contentPane.add(panel22);

                JPanel panel23 = new JPanel();
                panel23.setLayout(new FlowLayout());
                TitledBorder borderblk4 = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black));
                borderblk4.setTitle(Bundle.getMessage("BeanNameBlock") + " 4");
                panel23.setBorder(borderblk4);
                LayoutEditor.setupComboBox(blockDNameComboBox, false, true);
                blockDNameComboBox.setToolTipText(rb.getString("EditBlockDNameHint"));
                panel23.add(blockDNameComboBox);
                panel23.add(turnoutEditBlockD = new JButton(rb.getString("CreateEdit")));
                turnoutEditBlockD.addActionListener((ActionEvent e) -> {
                    turnoutEditBlockDPressed(e);
                });
                turnoutEditBlockD.setToolTipText(Bundle.getMessage("EditBlockHint", "4"));
                contentPane.add(panel23);
            }
            // set up Edit Block, Done and Cancel buttons
            JPanel panel5 = new JPanel();
            panel5.setLayout(new FlowLayout());
            // Edit Block

            turnoutEditBlock.setToolTipText(Bundle.getMessage("EditBlockHint", "")); // empty value for block 1
            // Done
            panel5.add(turnoutEditDone = new JButton(Bundle.getMessage("ButtonDone")));

            // make this button the default button (return or enter activates)
            // Note: We have to invoke this later because we don't currently have a root pane
            SwingUtilities.invokeLater(() -> {
                JRootPane rootPane = SwingUtilities.getRootPane(turnoutEditDone);
                rootPane.setDefaultButton(turnoutEditDone);
            });

            turnoutEditDone.addActionListener((ActionEvent e) -> {
                turnoutEditDonePressed(e);
            });
            turnoutEditDone.setToolTipText(Bundle.getMessage("DoneHint", Bundle.getMessage("ButtonDone")));
            // Cancel
            panel5.add(turnoutEditCancel = new JButton(Bundle.getMessage("ButtonCancel")));
            turnoutEditCancel.addActionListener((ActionEvent e) -> {
                turnoutEditCancelPressed(e);
            });
            turnoutEditCancel.setToolTipText(Bundle.getMessage("CancelHint", Bundle.getMessage("ButtonCancel")));
            contentPane.add(panel5);
        }

        hiddenBox.setSelected(hidden);

        // Set up for Edit
        blockNameComboBox.setText(blockName);
        if ((type == DOUBLE_XOVER) || (type == RH_XOVER) || (type == LH_XOVER)) {
            blockBNameComboBox.setText(blockBName);
            blockCNameComboBox.setText(blockCName);
            blockDNameComboBox.setText(blockDName);
        }
        firstTurnoutComboBox.setText(getTurnoutName());

        if (secondNamedTurnout != null) {
            additionalTurnout.setSelected(true);
            additionalTurnoutInvert.setEnabled(true);
            additionalTurnoutInvert.setSelected(getSecondTurnoutInverted());
            secondTurnoutLabel.setEnabled(true);
            secondTurnoutComboBox.setEnabled(true);
            secondTurnoutComboBox.setText(getSecondTurnoutName());
        }

        if ((type != DOUBLE_XOVER) && (type != RH_XOVER) && (type != LH_XOVER)) {
            if (continuingSense == Turnout.CLOSED) {
                stateBox.setSelectedIndex(turnoutClosedIndex);
            } else {
                stateBox.setSelectedIndex(turnoutThrownIndex);
            }
        }

        editLayoutTurnoutFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                turnoutEditCancelPressed(null);
            }
        });
        editLayoutTurnoutFrame.pack();
        editLayoutTurnoutFrame.setVisible(true);
        editOpen = true;
        needsBlockUpdate = false;
    }   // editLayoutTurnout

    void turnoutEditBlockPressed(ActionEvent a) {
        // check if a block name has been entered
        String newName = blockNameComboBox.getUserName();
        if (!blockName.equals(newName)) {
            // block has changed, if old block exists, decrement use
            if ((block != null) && (block != blockB) && (block != blockC)
                    && (block != blockD)) {
                block.decrementUse();
            }
            // get new block, or null if block has been removed
            blockName = newName;
            try {
                block = layoutEditor.provideLayoutBlock(blockName);
            } catch (IllegalArgumentException ex) {
                blockName = "";
            }
            // decrement use if block was already counted
            if ((block != null) && ((block == blockB) || (block == blockC) || (block == blockD))) {
                block.decrementUse();
            }
            needRedraw = true;
            needsBlockUpdate = true;
        }
        // check if a block exists to edit
        if (block == null) {
            JOptionPane.showMessageDialog(editLayoutTurnoutFrame,
                    rb.getString("Error1"),
                    Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
            return;
        }
        block.editLayoutBlock(editLayoutTurnoutFrame);
        needRedraw = true;
        layoutEditor.setDirty();
    }   // turnoutEditBlockPressed

    void turnoutEditBlockBPressed(ActionEvent a) {
        // check if a block name has been entered
        String newName = blockBNameComboBox.getUserName();
        if (!blockBName.equals(newName)) {
            // block has changed, if old block exists, decrement use
            if ((blockB != null) && (block != blockB) && (blockB != blockC)
                    && (blockB != blockD)) {
                blockB.decrementUse();
            }
            // get new block, or null if block has been removed
            blockBName = newName;
            try {
                blockB = layoutEditor.provideLayoutBlock(blockBName);
            } catch (IllegalArgumentException ex) {
                blockBName = "";
            }
            // decrement use if block was already counted
            if ((blockB != null) && ((block == blockB) || (blockB == blockC) || (blockB == blockD))) {
                blockB.decrementUse();
            }
            needRedraw = true;
            needsBlockUpdate = true;
        }
        // check if a block exists to edit
        if (blockB == null) {
            JOptionPane.showMessageDialog(editLayoutTurnoutFrame,
                    rb.getString("Error1"),
                    Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
            return;
        }
        blockB.editLayoutBlock(editLayoutTurnoutFrame);
        needRedraw = true;
        layoutEditor.setDirty();
    }   // turnoutEditBlockBPressed

    void turnoutEditBlockCPressed(ActionEvent a) {
        // check if a block name has been entered
        String newName = blockCNameComboBox.getUserName();
        if (!blockCName.equals(newName)) {
            // block has changed, if old block exists, decrement use
            if ((blockC != null) && (block != blockC) && (blockB != blockC)
                    && (blockC != blockD)) {
                blockC.decrementUse();
            }
            // get new block, or null if block has been removed
            blockCName = newName;
            try {
                blockC = layoutEditor.provideLayoutBlock(blockCName);
            } catch (IllegalArgumentException ex) {
                blockCName = "";
            }
            // decrement use if block was already counted
            if ((blockC != null) && ((block == blockC) || (blockB == blockC) || (blockC == blockD))) {
                blockD.decrementUse();
            }
            needRedraw = true;
            needsBlockUpdate = true;
        }
        // check if a block exists to edit
        if (blockC == null) {
            JOptionPane.showMessageDialog(editLayoutTurnoutFrame,
                    rb.getString("Error1"),
                    Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
            return;
        }
        blockC.editLayoutBlock(editLayoutTurnoutFrame);
        needRedraw = true;
        layoutEditor.setDirty();
    }   // turnoutEditBlockCPressed

    void turnoutEditBlockDPressed(ActionEvent a) {
        // check if a block name has been entered
        String newName = blockDNameComboBox.getUserName();
        if (!blockDName.equals(newName)) {
            // block has changed, if old block exists, decrement use
            if ((blockD != null) && (block != blockD) && (blockB != blockD)
                    && (blockC != blockD)) {
                blockD.decrementUse();
            }
            // get new block, or null if block has been removed
            blockDName = newName;
            try {
                blockD = layoutEditor.provideLayoutBlock(blockDName);
            } catch (IllegalArgumentException ex) {
                blockDName = "";
            }
            // decrement use if block was already counted
            if ((blockD != null) && ((block == blockD) || (blockB == blockD) || (blockC == blockD))) {
                blockD.decrementUse();
            }
            needRedraw = true;
            needsBlockUpdate = true;
        }
        // check if a block exists to edit
        if (blockD == null) {
            JOptionPane.showMessageDialog(editLayoutTurnoutFrame,
                    rb.getString("Error1"),
                    Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
            return;
        }
        blockD.editLayoutBlock(editLayoutTurnoutFrame);
        needRedraw = true;
        layoutEditor.setDirty();
    }   // turnoutEditBlockDPressed

    void turnoutEditDonePressed(ActionEvent a) {
        // check if Turnout changed
        String newName = firstTurnoutComboBox.getDisplayName();
        if (!turnoutName.equals(newName)) {
            // turnout has changed
            if (layoutEditor.validatePhysicalTurnout(newName, editLayoutTurnoutFrame)) {
                setTurnout(newName);
            } else {
                namedTurnout = null;
                turnoutName = "";
                firstTurnoutComboBox.setText("");
            }
            needRedraw = true;
        }

        if (additionalTurnout.isSelected()) {
            newName = secondTurnoutComboBox.getDisplayName();
            if (!secondTurnoutName.equals(newName)) {
                if ((type == DOUBLE_XOVER) || (type == RH_XOVER) || (type == LH_XOVER)) {
                    // turnout has changed
                    if (layoutEditor.validatePhysicalTurnout(newName,
                            editLayoutTurnoutFrame)) {
                        setSecondTurnout(newName);
                    } else {
                        additionalTurnout.setSelected(false);
                        secondNamedTurnout = null;
                        secondTurnoutName = "";
                        secondTurnoutComboBox.setText("");
                    }
                    needRedraw = true;
                } else {
                    setSecondTurnout(newName);
                }
            }
        } else {
            setSecondTurnout(null);
        }

        // set the continuing route Turnout State
        if ((type == RH_TURNOUT) || (type == LH_TURNOUT) || (type == WYE_TURNOUT)) {
            continuingSense = Turnout.CLOSED;
            if (stateBox.getSelectedIndex() == turnoutThrownIndex) {
                continuingSense = Turnout.THROWN;
            }
        }

        // check if Block changed
        newName = blockNameComboBox.getUserName();
        if (!blockName.equals(newName)) {
            // block has changed, if old block exists, decrement use
            if ((block != null) && (block != blockB) && (block != blockC)
                    && (block != blockD)) {
                block.decrementUse();
            }
            // get new block, or null if block has been removed
            blockName = newName;
            try {
                block = layoutEditor.provideLayoutBlock(blockName);
            } catch (IllegalArgumentException ex) {
                blockName = "";
            }
            // decrement use if block was already counted
            if ((block != null) && ((block == blockB) || (block == blockC) || (block == blockD))) {
                block.decrementUse();
            }
            needRedraw = true;
            needsBlockUpdate = true;
        }
        if ((type == DOUBLE_XOVER) || (type == LH_XOVER) || (type == RH_XOVER)) {
            // check if Block 2 changed
            newName = blockBNameComboBox.getUserName();
            if (!blockBName.equals(newName)) {
                // block has changed, if old block exists, decrement use
                if ((blockB != null) && (block != blockB) && (blockB != blockC)
                        && (blockB != blockD)) {
                    blockB.decrementUse();
                }
                // get new block, or null if block has been removed
                blockBName = newName;
                try {
                    blockB = layoutEditor.provideLayoutBlock(blockBName);
                } catch (IllegalArgumentException ex) {
                    blockBName = "";
                }
                // decrement use if block was already counted
                if ((blockB != null) && ((block == blockB) || (blockB == blockC) || (blockB == blockD))) {
                    blockB.decrementUse();
                }
                needRedraw = true;
                needsBlockUpdate = true;
            }
            // check if Block 3 changed
            newName = blockCNameComboBox.getUserName();
            if (!blockCName.equals(newName)) {
                // block has changed, if old block exists, decrement use
                if ((blockC != null) && (block != blockC) && (blockB != blockC)
                        && (blockC != blockD)) {
                    blockC.decrementUse();
                }
                // get new block, or null if block has been removed
                blockCName = newName;
                try {
                    blockC = layoutEditor.provideLayoutBlock(blockCName);
                } catch (IllegalArgumentException ex) {
                    blockCName = "";
                }

                // decrement use if block was already counted
                if ((blockC != null) && ((block == blockC) || (blockB == blockC) || (blockC == blockD))) {
                    blockC.decrementUse();
                }
                needRedraw = true;
                needsBlockUpdate = true;
            }
            // check if Block 4 changed
            newName = blockDNameComboBox.getUserName();
            if (!blockDName.equals(newName)) {
                // block has changed, if old block exists, decrement use
                if ((blockD != null) && (block != blockD) && (blockB != blockD)
                        && (blockC != blockD)) {
                    blockD.decrementUse();
                }
                // get new block, or null if block has been removed
                blockDName = newName;
                try {
                    blockD = layoutEditor.provideLayoutBlock(blockDName);
                } catch (IllegalArgumentException ex) {
                    blockDName = "";
                }
                // decrement use if block was already counted
                if ((blockD != null) && ((block == blockD) || (blockB == blockD) || (blockC == blockD))) {
                    blockD.decrementUse();
                }
                needRedraw = true;
                needsBlockUpdate = true;
            }
        }
        // set hidden
        boolean oldHidden = hidden;
        hidden = hiddenBox.isSelected();
        if (oldHidden != hidden) {
            needRedraw = true;
        }
        editOpen = false;
        editLayoutTurnoutFrame.setVisible(false);
        editLayoutTurnoutFrame.dispose();
        editLayoutTurnoutFrame = null;
        if (needsBlockUpdate) {
            updateBlockInfo();
            reCheckBlockBoundary();
        }
        if (needRedraw) {
            layoutEditor.redrawPanel();
            layoutEditor.setDirty();
        }
    }   // turnoutEditDonePressed

    void turnoutEditCancelPressed(ActionEvent a) {
        editOpen = false;
        editLayoutTurnoutFrame.setVisible(false);
        editLayoutTurnoutFrame.dispose();
        editLayoutTurnoutFrame = null;
        if (needsBlockUpdate) {
            updateBlockInfo();
        }
        if (needRedraw) {
            layoutEditor.redrawPanel();
            layoutEditor.setDirty();
        }
    }

    //@todo on the cross-overs check the internal boundary details.
    public void reCheckBlockBoundary() {
        if (connectA == null && connectB == null && connectC == null) {
            if ((type == RH_TURNOUT) || (type == LH_TURNOUT) || (type == WYE_TURNOUT)) {
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
            } else if (((type == DOUBLE_XOVER) || (type == RH_XOVER) || (type == LH_XOVER)) && connectD == null) {
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
        } else if ((connectD == null) && ((type == DOUBLE_XOVER) || (type == RH_XOVER) || (type == LH_XOVER))) {
            //could still be in the process of rebuilding.
            return;
        }

        TrackSegment trkA;
        TrackSegment trkB;
        TrackSegment trkC;
        TrackSegment trkD;

        if (connectA instanceof TrackSegment) {
            trkA = (TrackSegment) connectA;
            if (trkA.getLayoutBlock() == block) {
                if (signalAMastNamed != null) {
                    removeSML(getSignalAMast());
                }
                signalAMastNamed = null;
                sensorANamed = null;
            }
        }
        if (connectB instanceof TrackSegment) {
            trkB = (TrackSegment) connectB;
            if (trkB.getLayoutBlock() == block || trkB.getLayoutBlock() == blockB) {
                if (signalBMastNamed != null) {
                    removeSML(getSignalBMast());
                }
                signalBMastNamed = null;
                sensorBNamed = null;

            }
        }
        if (connectC instanceof TrackSegment) {
            trkC = (TrackSegment) connectC;
            if (trkC.getLayoutBlock() == block || trkC.getLayoutBlock() == blockB || trkC.getLayoutBlock() == blockC) {
                if (signalCMastNamed != null) {
                    removeSML(getSignalCMast());
                }
                signalCMastNamed = null;
                sensorCNamed = null;

            }
        }
        if (connectD != null && connectD instanceof TrackSegment
                && ((type == DOUBLE_XOVER) || (type == RH_XOVER) || (type == LH_XOVER))) {
            trkD = (TrackSegment) connectD;
            if (trkD.getLayoutBlock() == block || trkD.getLayoutBlock() == blockB || trkD.getLayoutBlock() == blockC || trkD.getLayoutBlock() == blockD) {
                if (signalDMastNamed != null) {
                    removeSML(getSignalDMast());
                }
                signalDMastNamed = null;
                sensorDNamed = null;
            }
        }
    }   // reCheckBlockBoundary

    public ArrayList<LayoutBlock> getProtectedBlocks(jmri.NamedBean bean) {
        ArrayList<LayoutBlock> ret = new ArrayList<LayoutBlock>(2);
        if (block == null) {
            return ret;
        }
        if (getTurnoutType() >= DOUBLE_XOVER && getTurnoutType() <= LH_XOVER) {
            if ((getTurnoutType() == DOUBLE_XOVER || getTurnoutType() == RH_XOVER)
                    && (getSignalAMast() == bean || getSignalCMast() == bean || getSensorA() == bean || getSensorC() == bean)) {
                if (getSignalAMast() == bean || getSensorA() == bean) {
                    if (connectA != null) {
                        if (((TrackSegment) connectA).getLayoutBlock() == block) {
                            if (blockB != null && block != blockB && blockC != null && block != blockC) {
                                ret.add(blockB);
                                ret.add(blockC);
                            }
                        } else {
                            ret.add(block);
                        }
                    }
                } else {
                    if (connectC != null && blockC != null) {
                        if (((TrackSegment) connectC).getLayoutBlock() == blockC) {
                            if (blockC != block && blockD != null && blockC != blockD) {
                                ret.add(block);
                                ret.add(blockD);
                            }
                        } else {
                            ret.add(blockC);
                        }
                    }
                }
            }
            if ((getTurnoutType() == DOUBLE_XOVER || getTurnoutType() == LH_XOVER)
                    && (getSignalBMast() == bean || getSignalDMast() == bean || getSensorB() == bean || getSensorD() == bean)) {
                if (getSignalBMast() == bean || getSensorB() == bean) {
                    if (connectB != null && blockB != null) {
                        if (((TrackSegment) connectB).getLayoutBlock() == blockB) {
                            if (block != blockB && blockD != null && blockB != blockD) {
                                ret.add(block);
                                ret.add(blockD);
                            }
                        } else {
                            ret.add(blockB);
                        }
                    }
                } else {
                    if (connectD != null && blockD != null) {
                        if (((TrackSegment) connectD).getLayoutBlock() == blockD) {
                            if (blockB != null && blockB != blockD && blockC != null && blockC != blockD) {
                                ret.add(blockB);
                                ret.add(blockC);
                            }
                        } else {
                            ret.add(blockD);
                        }
                    }
                }
            }
            if (getTurnoutType() == RH_XOVER && (getSignalBMast() == bean
                    || getSignalDMast() == bean || getSensorB() == bean || getSensorD() == bean)) {
                if (getSignalBMast() == bean || getSensorB() == bean) {
                    if (connectB != null && ((TrackSegment) connectB).getLayoutBlock() == blockB) {
                        if (blockB != block) {
                            ret.add(block);
                        }
                    } else {
                        ret.add(blockB);
                    }
                } else {
                    if (connectD != null && ((TrackSegment) connectD).getLayoutBlock() == blockD) {
                        if (blockC != blockD) {
                            ret.add(blockC);
                        }
                    } else {
                        ret.add(blockD);
                    }
                }
            }
            if (getTurnoutType() == LH_XOVER && (getSensorA() == bean
                    || getSensorC() == bean || getSignalAMast() == bean || getSignalCMast() == bean)) {
                if (getSignalAMast() == bean || getSensorA() == bean) {
                    if (connectA != null && ((TrackSegment) connectA).getLayoutBlock() == block) {
                        if (blockB != block) {
                            ret.add(blockB);
                        }
                    } else {
                        ret.add(block);
                    }
                } else {
                    if (connectC != null && ((TrackSegment) connectC).getLayoutBlock() == blockC) {
                        if (blockC != blockD) {
                            ret.add(blockD);
                        }
                    } else {
                        ret.add(blockC);
                    }
                }
            }
        } else {
            if (connectA != null) {
                if (getSignalAMast() == bean || getSensorA() == bean) {
                    //Mast at throat
                    //if the turnout is in the same block as the segment connected at the throat, then we can be protecting two blocks
                    if (((TrackSegment) connectA).getLayoutBlock() == block) {
                        if (connectB != null && connectC != null) {
                            if (((TrackSegment) connectB).getLayoutBlock() != block && ((TrackSegment) connectC).getLayoutBlock() != block) {
                                ret.add(((TrackSegment) connectB).getLayoutBlock());
                                ret.add(((TrackSegment) connectC).getLayoutBlock());
                            }
                        }
                    } else {
                        ret.add(block);
                    }
                } else if (getSignalBMast() == bean || getSensorB() == bean) {
                    //Mast at Continuing
                    if (connectB != null && ((TrackSegment) connectB).getLayoutBlock() == block) {
                        if (((TrackSegment) connectA).getLayoutBlock() != block) {
                            ret.add(((TrackSegment) connectA).getLayoutBlock());
                        }
                    } else {
                        ret.add(block);
                    }
                } else if (getSignalCMast() == bean || getSensorC() == bean) {
                    //Mast at Diverging
                    if (connectC != null && ((TrackSegment) connectC).getLayoutBlock() == block) {
                        if (((TrackSegment) connectA).getLayoutBlock() != block) {
                            ret.add(((TrackSegment) connectA).getLayoutBlock());
                        }
                    } else {
                        ret.add(block);
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

    ArrayList<JMenuItem> editAdditionalMenu = new ArrayList<JMenuItem>(0);
    ArrayList<JMenuItem> viewAdditionalMenu = new ArrayList<JMenuItem>(0);

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
    public void draw(Graphics2D g2) {
        Turnout to = getTurnout();

        Point2D pointA = getCoordsA();
        Point2D pointB = getCoordsB();
        Point2D pointC = getCoordsC();
        Point2D pointD = getCoordsD();

        setColorForTrackBlock(g2, getLayoutBlock());

        if (type == DOUBLE_XOVER) {
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
                }
            }
        } else if ((type == RH_XOVER) || (type == LH_XOVER)) {
            //  LH and RH crossover turnouts
            if (to == null) {
                // no physical turnout linked - draw A corner
                layoutEditor.setTrackStrokeWidth(g2, isMainlineA());
                g2.draw(new Line2D.Double(pointA, MathUtil.midPoint(pointA, pointB)));
                if (type == RH_XOVER) {
                    layoutEditor.setTrackStrokeWidth(g2, false);
                    g2.draw(new Line2D.Double(MathUtil.midPoint(pointA, pointB), center));
                }

                // draw B corner
                setColorForTrackBlock(g2, getLayoutBlockB());
                layoutEditor.setTrackStrokeWidth(g2, isMainlineB());
                g2.draw(new Line2D.Double(pointB, MathUtil.midPoint(pointA, pointB)));
                if (type == LH_XOVER) {
                    layoutEditor.setTrackStrokeWidth(g2, false);
                    g2.draw(new Line2D.Double(MathUtil.midPoint(pointA, pointB), center));
                }

                // draw C corner
                setColorForTrackBlock(g2, getLayoutBlockC());
                layoutEditor.setTrackStrokeWidth(g2, isMainlineC());
                g2.draw(new Line2D.Double(pointC, MathUtil.midPoint(pointC, pointD)));
                if (type == RH_XOVER) {
                    layoutEditor.setTrackStrokeWidth(g2, false);
                    g2.draw(new Line2D.Double(MathUtil.midPoint(pointC, pointD), center));
                }

                // draw D corner
                setColorForTrackBlock(g2, getLayoutBlockD());
                layoutEditor.setTrackStrokeWidth(g2, isMainlineD());
                g2.draw(new Line2D.Double(pointD, MathUtil.midPoint(pointC, pointD)));
                if (type == LH_XOVER) {
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
                    if (type == RH_XOVER) {
                        layoutEditor.setTrackStrokeWidth(g2, false);
                        setColorForTrackBlock(g2, getLayoutBlock(), true);
                        g2.draw(new Line2D.Double(center, MathUtil.oneThirdPoint(center, MathUtil.midPoint(pointA, pointB))));
                    }

                    setColorForTrackBlock(g2, getLayoutBlockB());
                    layoutEditor.setTrackStrokeWidth(g2, isMainlineB());
                    g2.draw(new Line2D.Double(pointB, MathUtil.midPoint(pointA, pointB)));

                    if (type == LH_XOVER) {
                        layoutEditor.setTrackStrokeWidth(g2, false);
                        setColorForTrackBlock(g2, getLayoutBlockB(), true);
                        g2.draw(new Line2D.Double(center, MathUtil.oneThirdPoint(center, MathUtil.midPoint(pointA, pointB))));
                    }

                    setColorForTrackBlock(g2, getLayoutBlockC());
                    layoutEditor.setTrackStrokeWidth(g2, isMainlineC());
                    g2.draw(new Line2D.Double(pointC, MathUtil.midPoint(pointC, pointD)));
                    if (type == RH_XOVER) {
                        layoutEditor.setTrackStrokeWidth(g2, false);
                        setColorForTrackBlock(g2, getLayoutBlockC(), true);
                        g2.draw(new Line2D.Double(center, MathUtil.oneThirdPoint(center, MathUtil.midPoint(pointC, pointD))));
                    }

                    setColorForTrackBlock(g2, getLayoutBlockD());
                    layoutEditor.setTrackStrokeWidth(g2, isMainlineD());
                    g2.draw(new Line2D.Double(pointD, MathUtil.midPoint(pointC, pointD)));
                    if (type == LH_XOVER) {
                        layoutEditor.setTrackStrokeWidth(g2, false);
                        setColorForTrackBlock(g2, getLayoutBlockD(), true);
                        g2.draw(new Line2D.Double(center, MathUtil.oneThirdPoint(center, MathUtil.midPoint(pointC, pointD))));
                    }
                } else if (state == Turnout.THROWN) {
                    // diverting (crossed) path
                    layoutEditor.setTrackStrokeWidth(g2, isMainlineA());
                    if (type == RH_XOVER) {
                        g2.draw(new Line2D.Double(pointA, MathUtil.midPoint(pointA, pointB)));
                        //layoutEditor.setTrackStrokeWidth(g2, false);
                        g2.draw(new Line2D.Double(MathUtil.midPoint(pointA, pointB), center));
                    } else if (type == LH_XOVER) {
                        g2.draw(new Line2D.Double(pointA, MathUtil.oneFourthPoint(pointA, pointB)));
                    }

                    setColorForTrackBlock(g2, getLayoutBlockB());
                    layoutEditor.setTrackStrokeWidth(g2, isMainlineB());
                    if (type == LH_XOVER) {
                        g2.draw(new Line2D.Double(pointB, MathUtil.midPoint(pointB, pointA)));
                        //layoutEditor.setTrackStrokeWidth(g2, false);
                        g2.draw(new Line2D.Double(MathUtil.midPoint(pointA, pointB), center));
                    } else if (type == RH_XOVER) {
                        g2.draw(new Line2D.Double(pointB, MathUtil.oneFourthPoint(pointB, pointA)));
                    }

                    setColorForTrackBlock(g2, getLayoutBlockC());
                    layoutEditor.setTrackStrokeWidth(g2, isMainlineC());
                    if (type == RH_XOVER) {
                        g2.draw(new Line2D.Double(pointC, MathUtil.midPoint(pointC, pointD)));
                        //layoutEditor.setTrackStrokeWidth(g2, false);
                        g2.draw(new Line2D.Double(MathUtil.midPoint(pointC, pointD), center));
                    } else if (type == LH_XOVER) {
                        g2.draw(new Line2D.Double(pointC, MathUtil.oneFourthPoint(pointC, pointD)));
                    }

                    setColorForTrackBlock(g2, getLayoutBlockD());
                    layoutEditor.setTrackStrokeWidth(g2, isMainlineD());
                    if (type == LH_XOVER) {
                        g2.draw(new Line2D.Double(pointD, MathUtil.midPoint(pointD, pointC)));
                        //layoutEditor.setTrackStrokeWidth(g2, false);
                        g2.draw(new Line2D.Double(MathUtil.midPoint(pointC, pointD), center));
                    } else if (type == RH_XOVER) {
                        g2.draw(new Line2D.Double(pointD, MathUtil.oneFourthPoint(pointD, pointC)));
                    }
                } else {
                    // unknown or inconsistent
                    layoutEditor.setTrackStrokeWidth(g2, isMainlineA());
                    if (type == RH_XOVER) {
                        g2.draw(new Line2D.Double(pointA, MathUtil.midPoint(pointA, pointB)));
                        //layoutEditor.setTrackStrokeWidth(g2, false);
                        g2.draw(new Line2D.Double(center, MathUtil.oneThirdPoint(center, MathUtil.midPoint(pointA, pointB))));
                    } else if (type == LH_XOVER) {
                        g2.draw(new Line2D.Double(pointA, MathUtil.oneFourthPoint(pointA, pointB)));
                    }

                    setColorForTrackBlock(g2, getLayoutBlockB());
                    layoutEditor.setTrackStrokeWidth(g2, isMainlineB());
                    if (type == LH_XOVER) {
                        g2.draw(new Line2D.Double(pointB, MathUtil.midPoint(pointB, pointA)));
                        //layoutEditor.setTrackStrokeWidth(g2, false);
                        g2.draw(new Line2D.Double(center, MathUtil.oneThirdPoint(center, MathUtil.midPoint(pointA, pointB))));
                    } else if (type == RH_XOVER) {
                        g2.draw(new Line2D.Double(pointB, MathUtil.oneFourthPoint(pointB, pointA)));
                    }

                    setColorForTrackBlock(g2, getLayoutBlockC());
                    layoutEditor.setTrackStrokeWidth(g2, isMainlineC());
                    if (type == RH_XOVER) {
                        g2.draw(new Line2D.Double(pointC, MathUtil.midPoint(pointC, pointD)));
                        //layoutEditor.setTrackStrokeWidth(g2, false);
                        g2.draw(new Line2D.Double(center, MathUtil.oneThirdPoint(center, MathUtil.midPoint(pointC, pointD))));
                    } else if (type == LH_XOVER) {
                        g2.draw(new Line2D.Double(pointC, MathUtil.oneFourthPoint(pointC, pointD)));
                    }

                    setColorForTrackBlock(g2, getLayoutBlockD());
                    layoutEditor.setTrackStrokeWidth(g2, isMainlineD());
                    if (type == LH_XOVER) {
                        g2.draw(new Line2D.Double(pointD, MathUtil.midPoint(pointC, pointD)));
                        //layoutEditor.setTrackStrokeWidth(g2, false);
                        g2.draw(new Line2D.Double(center, MathUtil.oneThirdPoint(center, MathUtil.midPoint(pointC, pointD))));
                    } else if (type == RH_XOVER) {
                        g2.draw(new Line2D.Double(pointD, MathUtil.oneFourthPoint(pointD, pointC)));
                    }
                }
            }
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
                }
            }
        }
    }   // draw

    public void drawControls(Graphics2D g2) {
        g2.draw(layoutEditor.trackControlCircleAt(center));
    }

    public void drawEditControls(Graphics2D g2) {
        Point2D pt = getCoordsA();
        if (type >= DOUBLE_XOVER && type <= LH_XOVER) {
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

        if ((type == DOUBLE_XOVER) || (type == RH_XOVER) || (type == LH_XOVER)) {
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
        LayoutBlock layoutBlockD = ((TrackSegment) getConnectD()).getLayoutBlock();

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
                    if ((tTyp != LayoutTurnout.LH_XOVER) && ((getLayoutBlockC() == nextLayoutBlock)
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
                    } else if ((tTyp != LayoutTurnout.RH_XOVER) && ((getLayoutBlockD() == nextLayoutBlock)
                            || (getLayoutBlockD() == prevLayoutBlock) || (getLayoutBlockD() == currLayoutBlock))) {
                        result = Turnout.THROWN;
                    } else {
                        if (!suppress) {
                            log.error("Cannot determine turnout setting(B) - " + getTurnoutName());
                        }
                        result = Turnout.CLOSED;
                    }
                } else if (getLayoutBlockC() == currLayoutBlock) {
                    if ((tTyp != LayoutTurnout.LH_XOVER) && ((getLayoutBlock() == nextLayoutBlock) || (getLayoutBlock() == prevLayoutBlock))) {
                        result = Turnout.THROWN;
                    } else if ((getLayoutBlockD() == nextLayoutBlock) || (getLayoutBlockD() == prevLayoutBlock) || (getLayoutBlockD() == currLayoutBlock)) {
                        result = Turnout.CLOSED;
                    } else if ((tTyp != LayoutTurnout.LH_XOVER) && (getLayoutBlockD() == currLayoutBlock)) {
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
                    } else if ((tTyp != LayoutTurnout.RH_XOVER) && ((getLayoutBlockB() == nextLayoutBlock) || (getLayoutBlockB() == prevLayoutBlock))) {
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
        return the layout connectivity for this Layout Turnout
     */
    protected ArrayList<LayoutConnectivity> getLayoutConnectivity() {
        ArrayList<LayoutConnectivity> results = new ArrayList<LayoutConnectivity>();

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

    private final static Logger log = LoggerFactory.getLogger(LayoutTurnout.class);
}
