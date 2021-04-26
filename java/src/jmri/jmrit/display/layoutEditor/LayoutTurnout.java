package jmri.jmrit.display.layoutEditor;

import java.text.MessageFormat;
import java.util.*;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import jmri.*;
import jmri.jmrit.signalling.SignallingGuiTools;

/**
 * LayoutTurnout is the abstract base for classes representing various types of turnout on the layout.
 * A LayoutTurnout is an
 * extension of the standard Turnout object with drawing and connectivity
 * information added.
 * <p>
 * Specific forms are represented: right-hand, left-hand, wye, double crossover,
 * right-handed single crossover, and left-handed single crossover. Note that
 * double-slip turnouts can be handled as two turnouts, throat to throat, and
 * three-way turnouts can be handled as two turnouts, left-hand and right-hand,
 * arranged throat to continuing route.
 * <p>
 * A LayoutTurnout has three or four connection points, designated A, B, C, and
 * D. For right-handed or left-handed turnouts, A corresponds to the throat. At
 * the crossing, A-B (and C-D for crossovers) is a straight segment (continuing
 * route). A-C (and B-D for crossovers) is the diverging route. B-C (and A-D for
 * crossovers) is an illegal condition.
 * <br>
 * <pre>
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
 * </pre>
 * (The {@link LayoutSlip} track objects follow a different pattern. They put A-D in
 * different places and have AD and BC as the normal-continuance parallel paths)
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
 * @author Bob Jacobsen Copyright (c) 2020
 */
abstract public class LayoutTurnout extends LayoutTrack {

    protected LayoutTurnout(@Nonnull String id,
            @Nonnull LayoutEditor models, TurnoutType t) {
        super(id, models);

        type = t;
    }

    protected LayoutTurnout(@Nonnull String id,
            @Nonnull LayoutEditor models) {
        this(id, models, TurnoutType.NONE);
    }

    public LayoutTurnout(@Nonnull String id, TurnoutType t,
            @Nonnull LayoutEditor models) {
        this(id, t, models, 1);
    }

    /**
     * Main constructor method.
     * @param id Layout Turnout ID.
     * @param t type, e.g. LH_TURNOUT, WYE_TURNOUT
     * @param models main layout editor.
     * @param v version.
     */
    public LayoutTurnout(@Nonnull String id, TurnoutType t,
            @Nonnull LayoutEditor models,
            int v) {
        super(id, models);

        namedTurnout = null;
        turnoutName = "";
        mTurnoutListener = null;
        disabled = false;
        disableWhenOccupied = false;
        type = t;
        version = v;
    }

    // Defined constants for turnout types
    // This is being replaced by subclasses; do not add more
    // references to it.
    public enum TurnoutType {
        NONE,
        RH_TURNOUT,
        LH_TURNOUT,
        WYE_TURNOUT,
        DOUBLE_XOVER,
        RH_XOVER,
        LH_XOVER,
        SINGLE_SLIP, // used for LayoutSlip which extends this class
        DOUBLE_SLIP     // used for LayoutSlip which extends this class
    }

    /**
     * Returns true if this is a turnout (not a crossover or slip)
     *
     * @param type the turnout type
     * @return boolean true if this is a turnout
     */
    public static boolean isTurnoutTypeTurnout(TurnoutType type) {
        return (type == TurnoutType.RH_TURNOUT
                || type == TurnoutType.LH_TURNOUT
                || type == TurnoutType.WYE_TURNOUT);
    }

    /**
     * Returns true if this is a turnout (not a crossover or slip)
     *
     * @return boolean true if this is a turnout
     */
    public boolean isTurnoutTypeTurnout() {
        return isTurnoutTypeTurnout(getTurnoutType());
    }

    /**
     * Returns true if this is a crossover
     *
     * @param type the turnout type
     * @return boolean true if this is a crossover
     */
    public static boolean isTurnoutTypeXover(TurnoutType type) {
        return (type == TurnoutType.DOUBLE_XOVER
                || type == TurnoutType.RH_XOVER
                || type == TurnoutType.LH_XOVER);
    }

    /**
     * Returns true if this is a crossover
     *
     * @return boolean true if this is a crossover
     */
    public boolean isTurnoutTypeXover() {
        return isTurnoutTypeXover(getTurnoutType());
    }

    /**
     * Returns true if this is a slip
     *
     * @param type the turnout type
     * @return boolean true if this is a slip
     */
    public static boolean isTurnoutTypeSlip(TurnoutType type) {
        return (type == TurnoutType.SINGLE_SLIP
                || type == TurnoutType.DOUBLE_SLIP);
    }

    /**
     * Returns true if this is a slip
     *
     * @return boolean true if this is a slip
     */
    public boolean isTurnoutTypeSlip() {
        return isTurnoutTypeSlip(getTurnoutType());
    }

    /**
     * Returns true if this has a single-track entrance end. (turnout or wye)
     *
     * @param type the turnout type
     * @return boolean true if single track entrance
     */
    public static boolean hasEnteringSingleTrack(TurnoutType type) {
        return isTurnoutTypeTurnout(type);
    }

    /**
     * Returns true if this has a single-track entrance end. (turnout or wye)
     *
     * @return boolean true if single track entrance
     */
    public boolean hasEnteringSingleTrack() {
        return hasEnteringSingleTrack(getTurnoutType());
    }

    /**
     * Returns true if this has double track on the entrance end (crossover or
     * slip)
     *
     * @param type the turnout type
     * @return boolean true if double track entrance
     */
    public static boolean hasEnteringDoubleTrack(TurnoutType type) {
        return isTurnoutTypeXover(type) || isTurnoutTypeSlip(type);
    }

    /**
     * Returns true if this has double track on the entrance end (crossover or
     * slip)
     *
     * @return boolean true if double track entrance
     */
    public boolean hasEnteringDoubleTrack() {
        return hasEnteringDoubleTrack(getTurnoutType());
    }

    public enum LinkType {
        NO_LINK,
        FIRST_3_WAY, // this turnout is the first turnout of a 3-way
        // turnout pair (closest to the throat)
        SECOND_3_WAY, // this turnout is the second turnout of a 3-way
        // turnout pair (furthest from the throat)
        THROAT_TO_THROAT  // this turnout is one of two throat-to-throat
        // turnouts - no signals at throat
    }

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
    // Second turnout is used to either throw a second turnout in a cross over or if one turnout address is used to throw two physical ones
    protected NamedBeanHandle<Turnout> secondNamedTurnout = null;

    private java.beans.PropertyChangeListener mTurnoutListener = null;

    // persistent instances variables (saved between sessions)
    // these should be the system or user name of an existing physical turnout
    @Nonnull private String turnoutName = ""; // "" means none, never null
    @Nonnull private String secondTurnoutName = ""; // "" means none, never null
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

    public enum Geometry {
        NONE,
        POINTA1,
        POINTA2,
        POINTA3,
        POINTB1,
        POINTB2,
        POINTC1,
        POINTC2,
        POINTD1,
        POINTD2
    }

    protected NamedBeanHandle<SignalMast> signalAMastNamed = null; // Throat
    protected NamedBeanHandle<SignalMast> signalBMastNamed = null; // Continuing
    protected NamedBeanHandle<SignalMast> signalCMastNamed = null; // diverging
    protected NamedBeanHandle<SignalMast> signalDMastNamed = null; // single or double crossover only

    protected NamedBeanHandle<Sensor> sensorANamed = null; // Throat
    protected NamedBeanHandle<Sensor> sensorBNamed = null; // Continuing
    protected NamedBeanHandle<Sensor> sensorCNamed = null; // diverging
    protected NamedBeanHandle<Sensor> sensorDNamed = null; // single or double crossover only

    protected final TurnoutType type;

    public LayoutTrack connectA = null;      // throat of LH, RH, RH Xover, LH Xover, and WYE turnouts
    public LayoutTrack connectB = null;      // straight leg of LH and RH turnouts
    public LayoutTrack connectC = null;
    public LayoutTrack connectD = null;      // double xover, RH Xover, LH Xover only

    public int continuingSense = Turnout.CLOSED;

    public boolean disabled = false;
    public boolean disableWhenOccupied = false;

    private int version = 1;

    @Nonnull public String linkedTurnoutName = ""; // name of the linked Turnout (as entered in tool); "" means none, never null
    public LinkType linkType = LinkType.NO_LINK;

    private final boolean useBlockSpeed = false;

    /**
     * {@inheritDoc}
     */
    // this should only be used for debugging...
    @Override
    @Nonnull
    public String toString() {
        return "LayoutTurnout " + getName();
    }

    /**
     * Get the Version.
     * @return turnout version.
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

    @Nonnull
    public String getTurnoutName() {
        if (namedTurnout != null) {
            turnoutName = namedTurnout.getName();
        }
        return turnoutName;
    }

    @Nonnull
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

    @CheckForNull
    public SignalHead getSignalHead(Geometry loc) {
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
                log.warn("{}.getSignalHead({}); Unhandled point type", getName(), loc);
                break;
        }
        if (signalHead != null) {
            return signalHead.getBean();
        }
        return null;
    }

    @CheckForNull
    public SignalHead getSignalA1() {
        return signalA1HeadNamed != null ? signalA1HeadNamed.getBean() : null;
    }

    @Nonnull
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
            log.error("{}.setSignalA1Name({}); not fournd for turnout {}", getName(), signalHead, getTurnoutName());
        }
    }

    @CheckForNull
    public SignalHead getSignalA2() {
        return signalA2HeadNamed != null ? signalA2HeadNamed.getBean() : null;
    }

    @Nonnull
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
            log.error("{}.setSignalA2Name({}); not fournd for turnout {}", getName(), signalHead, getTurnoutName());
        }
    }

    @CheckForNull
    public SignalHead getSignalA3() {
        return signalA3HeadNamed != null ? signalA3HeadNamed.getBean() : null;
    }

    @Nonnull
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
            log.error("{}.setSignalA3Name({}); not fournd for turnout {}", getName(), signalHead, getTurnoutName());
        }
    }

    @CheckForNull
    public SignalHead getSignalB1() {
        return signalB1HeadNamed != null ? signalB1HeadNamed.getBean() : null;
    }

    @Nonnull
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
            log.error("{}.setSignalB1Name({}); not fournd for turnout {}", getName(), signalHead, getTurnoutName());
        }
    }

    @CheckForNull
    public SignalHead getSignalB2() {
        return signalB2HeadNamed != null ? signalB2HeadNamed.getBean() : null;
    }

    @Nonnull
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
            log.error("{}.setSignalB2Name({}); not fournd for turnout {}", getName(), signalHead, getTurnoutName());
        }
    }

    @CheckForNull
    public SignalHead getSignalC1() {
        return signalC1HeadNamed != null ? signalC1HeadNamed.getBean() : null;
    }

    @Nonnull
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
            log.error("{}.setSignalC1Name({}); not fournd for turnout {}", getName(), signalHead, getTurnoutName());
        }
    }

    @CheckForNull
    public SignalHead getSignalC2() {
        return signalC2HeadNamed != null ? signalC2HeadNamed.getBean() : null;
    }

    @Nonnull
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
            log.error("{}.setSignalC2Name({}); not fournd for turnout {}", getName(), signalHead, getTurnoutName());
        }
    }

    @CheckForNull
    public SignalHead getSignalD1() {
        return signalD1HeadNamed != null ? signalD1HeadNamed.getBean() : null;
    }

    @Nonnull
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
            log.error("{}.setSignalD1Name({}); not fournd for turnout {}", getName(), signalHead, getTurnoutName());
        }
    }

    @CheckForNull
    public SignalHead getSignalD2() {
        return signalD2HeadNamed != null ? signalD2HeadNamed.getBean() : null;
    }

    @Nonnull
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
            log.error("{}.setSignalD2Name({}); not fournd for turnout {}", getName(), signalHead, getTurnoutName());
        }
    }

    public void removeBeanReference(@CheckForNull jmri.NamedBean nb) {
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
            if (nb.equals(getSignalHead(Geometry.POINTA1))) {
                setSignalA1Name(null);
            }
            if (nb.equals(getSignalHead(Geometry.POINTA2))) {
                setSignalA2Name(null);
            }
            if (nb.equals(getSignalHead(Geometry.POINTA3))) {
                setSignalA3Name(null);
            }
            if (nb.equals(getSignalHead(Geometry.POINTB1))) {
                setSignalB1Name(null);
            }
            if (nb.equals(getSignalHead(Geometry.POINTB2))) {
                setSignalB2Name(null);
            }
            if (nb.equals(getSignalHead(Geometry.POINTC1))) {
                setSignalC1Name(null);
            }
            if (nb.equals(getSignalHead(Geometry.POINTC2))) {
                setSignalC2Name(null);
            }
            if (nb.equals(getSignalHead(Geometry.POINTD1))) {
                setSignalD1Name(null);
            }
            if (nb.equals(getSignalHead(Geometry.POINTD2))) {
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
            models.displayRemoveWarning(this, beanReferences, "BeanNameTurnout");  // NOI18N
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
    @Nonnull
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

    // @CheckForNull temporary until we get central error check
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
            log.error("{}.setSignalAMast({}); not fournd for turnout {}", getName(), signalMast, getTurnoutName());
        }
    }

    @Nonnull
    public String getSignalBMastName() {
        if (signalBMastNamed != null) {
            return signalBMastNamed.getName();
        }
        return "";
    }

    // @CheckForNull temporary until we get central error check
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
            log.error("{}.setSignalBMast({}); not fournd for turnout {}", getName(), signalMast, getTurnoutName());
        }
    }

    @Nonnull
    public String getSignalCMastName() {
        if (signalCMastNamed != null) {
            return signalCMastNamed.getName();
        }
        return "";
    }

    // @CheckForNull temporary until we get central error check
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
            log.error("{}.setSignalCMast({}); not fournd for turnout {}", getName(), signalMast, getTurnoutName());
            signalCMastNamed = null;
        }
    }

    @Nonnull
    public String getSignalDMastName() {
        if (signalDMastNamed != null) {
            return signalDMastNamed.getName();
        }
        return "";
    }

    // @CheckForNull temporary until we get central error check
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
            log.error("{}.setSignalDMast({}); not fournd for turnout {}", getName(), signalMast, getTurnoutName());
            signalDMastNamed = null;
        }
    }

    @Nonnull
    public String getSensorAName() {
        if (sensorANamed != null) {
            return sensorANamed.getName();
        }
        return "";
    }

    @CheckForNull
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

    @Nonnull
    public String getSensorBName() {
        if (sensorBNamed != null) {
            return sensorBNamed.getName();
        }
        return "";
    }

    @CheckForNull
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

    @Nonnull
    public String getSensorCName() {
        if (sensorCNamed != null) {
            return sensorCNamed.getName();
        }
        return "";
    }

    @CheckForNull
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

    @Nonnull
    public String getSensorDName() {
        if (sensorDNamed != null) {
            return sensorDNamed.getName();
        }
        return "";
    }

    @CheckForNull
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

    @Nonnull
    public String getLinkedTurnoutName() {
        return linkedTurnoutName;
    }

    public void setLinkedTurnoutName(@Nonnull String s) {
        linkedTurnoutName = s;
    }  // Could be done with changing over to a NamedBeanHandle

    public LinkType getLinkType() {
        return linkType;
    }

    public void setLinkType(LinkType ltype) {
        linkType = ltype;
    }

    public TurnoutType getTurnoutType() {
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

    /**
     * Perhaps confusingly, this returns an actual Turnout reference
     * or null for the turnout associated with this is LayoutTurnout.
     * This is different from {@link #setTurnout(String)}, which
     * takes a name (system or user) or an empty string.
     * @return Null if no Turnout set
     */
    // @CheckForNull temporary - want to restore once better handled
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

    /**
     * Perhaps confusingly, this takes a Turnout name (system or user)
     * to locate and set the turnout associated with this is LayoutTurnout.
     * This is different from {@link #getTurnout()}, which returns an
     * actual Turnout reference or null.
     * @param tName provide empty string for none; never null
     */
    public void setTurnout(@Nonnull String tName) {
        assert tName != null;
        if (namedTurnout != null) {
            deactivateTurnout();
        }
        turnoutName = tName;
        Turnout turnout = null;
        if (!turnoutName.isEmpty()) {
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
        Turnout secondTurnout = getSecondTurnout();
        if (secondTurnout != null && secondTurnout.getFeedbackMode() == Turnout.DIRECT) {
            secondTurnout.setLeadingTurnout(turnout, false);
        }
    }

    // @CheckForNull temporary until we have central paradigm for null
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

    /**
     * @param tName provide empty string for none (not null)
     */
    public void setSecondTurnout(@Nonnull String tName) {
        assert tName != null;
        if (tName.equals(secondTurnoutName)) { // haven't changed anything
            return;
        }

        if (secondNamedTurnout != null) {
            deactivateTurnout();
            Turnout turnout = secondNamedTurnout.getBean();
            if (turnout.getLeadingTurnout() == namedTurnout.getBean()) {
                turnout.setLeadingTurnout(null);
            }
        }
        String oldSecondTurnoutName = secondTurnoutName;
        secondTurnoutName = tName;
        Turnout turnout = null;
        if (! tName.isEmpty()) {
            turnout = InstanceManager.turnoutManagerInstance().getTurnout(secondTurnoutName);
        }
        if (turnout != null) {
            secondNamedTurnout = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(secondTurnoutName, turnout);
            if (turnout.getFeedbackMode() == Turnout.DIRECT) {
                turnout.setLeadingTurnout(getTurnout(), false);
            }
        } else {
            secondTurnoutName = "";
            secondNamedTurnout = null;
        }
        activateTurnout(); // Even if secondary is null, the primary Turnout may still need to be re-activated
        if (isTurnoutTypeTurnout()) {
            LayoutEditorFindItems lf = new LayoutEditorFindItems(models);
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
                    oldLinked.setSecondTurnout("");
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
            if (models != null) {
                models.redrawPanel();
            }
        }
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisableWhenOccupied(boolean state) {
        if (disableWhenOccupied != state) {
            disableWhenOccupied = state;
            if (models != null) {
                models.redrawPanel();
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
    @CheckForNull
    public LayoutTrack getConnection(HitPointType connectionType) {
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
                String errString = MessageFormat.format("{0}.getConnection({1}); Invalid Connection Type",
                        getName(), connectionType); // I18IN
                log.error("will throw {}", errString);
                throw new IllegalArgumentException(errString);
            }
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setConnection(HitPointType connectionType, @CheckForNull LayoutTrack o, HitPointType type) throws jmri.JmriException {
        if ((type != HitPointType.TRACK) && (type != HitPointType.NONE)) {
            String errString = MessageFormat.format("{0}.setConnection({1}, {2}, {3}); unexpected type",
                    getName(), connectionType, (o == null) ? "null" : o.getName(), type, new Exception("traceback")); // I18IN
            log.error("will throw {}", errString);
            throw new jmri.JmriException(errString);
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
                String errString = MessageFormat.format("{0}.setConnection({1}, {2}, {3}); Invalid Connection Type",
                        getName(), connectionType, (o == null) ? "null" : o.getName(), type); // I18IN
                log.error("will throw {}", errString);
                throw new jmri.JmriException(errString);
        }
    }

    public void setConnectA(@CheckForNull LayoutTrack o, HitPointType type) {
        connectA = o;
        if ((type != HitPointType.TRACK) && (type != HitPointType.NONE)) {
            log.error("{}.setConnectA({}, {}); unexpected type",
                    getName(), (o == null) ? "null" : o.getName(), type);
        }
    }

    public void setConnectB(@CheckForNull LayoutTrack o, HitPointType type) {
        connectB = o;
        if ((type != HitPointType.TRACK) && (type != HitPointType.NONE)) {
            log.error("{}.setConnectB({}, {}); unexpected type",
                    getName(), (o == null) ? "null" : o.getName(), type);
        }
    }

    public void setConnectC(@CheckForNull LayoutTrack o, HitPointType type) {
        connectC = o;
        if ((type != HitPointType.TRACK) && (type != HitPointType.NONE)) {
            log.error("{}.setConnectC({}, {}); unexpected type",
                    getName(), (o == null) ? "null" : o.getName(), type);
        }
    }

    public void setConnectD(@CheckForNull LayoutTrack o, HitPointType type) {
        connectD = o;
        if ((type != HitPointType.TRACK) && (type != HitPointType.NONE)) {
            log.error("{}.setConnectD({}, {}); unexpected type",
                    getName(), (o == null) ? "null" : o.getName(), type);
        }
    }

    // @CheckForNull - temporary, until we can centralize protection for this
    public LayoutBlock getLayoutBlock() {
        return (namedLayoutBlockA != null) ? namedLayoutBlockA.getBean() : null;
    }

    // @CheckForNull - temporary, until we can centralize protection for this
    public LayoutBlock getLayoutBlockB() {
        return (namedLayoutBlockB != null) ? namedLayoutBlockB.getBean() : getLayoutBlock();
    }

    // @CheckForNull - temporary, until we can centralize protection for this
    public LayoutBlock getLayoutBlockC() {
        return (namedLayoutBlockC != null) ? namedLayoutBlockC.getBean() : getLayoutBlock();
    }

    // @CheckForNull - temporary, until we can centralize protection for this
    public LayoutBlock getLayoutBlockD() {
        return (namedLayoutBlockD != null) ? namedLayoutBlockD.getBean() : getLayoutBlock();
    }

    // updates connectivity for blocks assigned to this turnout and connected track segments
    public void updateBlockInfo() {
        LayoutBlock bA = null;
        LayoutBlock bB = null;
        LayoutBlock bC = null;
        LayoutBlock bD = null;
        models.getLEAuxTools().setBlockConnectivityChanged();
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
     * Set up Layout Block(s) for this Turnout.
     * @param newLayoutBlock the new layout block.
     */
    protected void setLayoutBlock(LayoutBlock newLayoutBlock) {
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
        }
    }

    protected void setLayoutBlockB(LayoutBlock newLayoutBlock) {
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
                    String userName = newLayoutBlock.getUserName();
                    if (userName != null) {
                        namedLayoutBlockB = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(userName, newLayoutBlock);
                    }
                } else {
                    namedLayoutBlockB = null;
                }
                // decrement use if block was already counted
                if ((blockB != null)
                        && ((blockB == blockA) || (blockB == blockC) || (blockB == blockD))) {
                    blockB.decrementUse();
                }
            }
        } else {
            log.error("{}.setLayoutBlockB({}); not a crossover/slip", getName(), newLayoutBlock.getUserName());
        }
    }

    protected void setLayoutBlockC(@CheckForNull LayoutBlock newLayoutBlock) {
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
                    String userName = newLayoutBlock.getUserName();
                    if (userName != null) {
                        namedLayoutBlockC = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(userName, newLayoutBlock);
                    }
                } else {
                    namedLayoutBlockC = null;
                }
                // decrement use if block was already counted
                if ((blockC != null)
                        && ((blockC == blockA) || (blockC == blockB) || (blockC == blockD))) {
                    blockC.decrementUse();
                }
            }
        } else {
            log.error("{}.setLayoutBlockC({}); not a crossover/slip", getName(), newLayoutBlock.getUserName());
        }
    }

    protected void setLayoutBlockD(LayoutBlock newLayoutBlock) {
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
                    String userName = newLayoutBlock.getUserName();
                    if (userName != null) {
                        namedLayoutBlockD = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(userName, newLayoutBlock);
                    }
                } else {
                    namedLayoutBlockD = null;
                }
                // decrement use if block was already counted
                if ((blockD != null)
                        && ((blockD == blockA) || (blockD == blockB) || (blockD == blockC))) {
                    blockD.decrementUse();
                }
            }
        } else {
            log.error("{}.setLayoutBlockD({}); not a crossover/slip", getName(), newLayoutBlock.getUserName());
        }
    }

    public void setLayoutBlockByName(@Nonnull String name) {
        setLayoutBlock(models.provideLayoutBlock(name));
    }

    public void setLayoutBlockBByName(@Nonnull String name) {
        if (isTurnoutTypeXover() || isTurnoutTypeSlip()) {
            setLayoutBlockB(models.provideLayoutBlock(name));
        } else {
            log.error("{}.setLayoutBlockBByName({}); not a crossover/slip", getName(), name);
        }
    }

    public void setLayoutBlockCByName(@Nonnull String name) {
        if (isTurnoutTypeXover() || isTurnoutTypeSlip()) {
            setLayoutBlockC(models.provideLayoutBlock(name));
        } else {
            log.error("{}.setLayoutBlockCByName({}); not a crossover/slip", getName(), name);
        }
    }

    public void setLayoutBlockDByName(@Nonnull String name) {
        if (isTurnoutTypeXover() || isTurnoutTypeSlip()) {
            setLayoutBlockD(models.provideLayoutBlock(name));
        } else {
            log.error("{}.setLayoutBlockDByName({}); not a crossover/slip", getName(), name);
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
            } else if (getTurnoutType() == TurnoutType.DOUBLE_SLIP) {
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
            } else if (getTurnoutType() == TurnoutType.DOUBLE_SLIP) {
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

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isMainline() {
        return (isMainlineA() || isMainlineB() || isMainlineC() || isMainlineD());
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
                        models.redrawPanel();
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
     * Set the LayoutTurnout state. Used for sending the toggle command Checks
     * not disabled, disable when occupied Also sets secondary Turnout commanded
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
                Turnout secondTurnout = getSecondTurnout();
                if (secondTurnout != null) {
                    if (secondTurnoutInverted) {
                        secondTurnout.setCommandedState(Turnout.invertTurnoutState(state));
                    } else {
                        secondTurnout.setCommandedState(state);
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
    boolean isOccupied() {
        if (isTurnoutTypeTurnout()) {
            if (getLayoutBlock().getOccupancy() == LayoutBlock.OCCUPIED) {
                log.debug("Block {} is Occupied", getBlockName());
                return true;
            }
        } else if (isTurnoutTypeXover()) {
            // If the turnout is set for straight over, we need to deal with the straight over connecting blocks
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
        if ((getTurnoutType() == TurnoutType.DOUBLE_XOVER)
                || (getTurnoutType() == TurnoutType.LH_XOVER)) {
            if (getTurnout().getKnownState() == Turnout.THROWN) {
                if ((getLayoutBlockB().getOccupancy() == LayoutBlock.OCCUPIED)
                        && (getLayoutBlockD().getOccupancy() == LayoutBlock.OCCUPIED)) {
                    log.debug("Blocks {} & {} are Occupied", getBlockBName(), getBlockDName());
                    return true;
                }
            }
        }

        if ((getTurnoutType() == TurnoutType.DOUBLE_XOVER)
                || (getTurnoutType() == TurnoutType.RH_XOVER)) {
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
    public void setObjects(@Nonnull LayoutEditor p) {
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
                log.error("{}.setObjects(...); bad blockname A '{}'", getName(), tBlockAName);
                namedLayoutBlockA = null;
            }
            tBlockAName = null; // release this memory
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
                log.error("{}.setObjects(...); bad blockname B '{}'", getName(), tBlockBName);
                namedLayoutBlockB = null;
            }
            tBlockBName = null; // release this memory
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
                log.error("{}.setObjects(...); bad blockname C '{}'", getName(), tBlockCName);
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
                log.error("{}.setObjects(...); bad blockname D '{}'", getName(), tBlockDName);
                namedLayoutBlockD = null;
            }
            tBlockDName = null; // release this memory
        }
        activateTurnout();
    } // setObjects

    public String[] getBlockBoundaries() {
        final String[] boundaryBetween = new String[4];
        if (isTurnoutTypeTurnout()) {
            // This should only be needed where we are looking at a single turnout.
            if (getLayoutBlock() != null) {
                LayoutBlock aLBlock = null;
                if (connectA instanceof TrackSegment) {
                    aLBlock = ((TrackSegment) connectA).getLayoutBlock();
                    if (aLBlock != getLayoutBlock()) {
                        try {
                            boundaryBetween[0] = (aLBlock.getDisplayName() + " - " + getLayoutBlock().getDisplayName());
                        } catch (java.lang.NullPointerException e) {
                            // Can be considered normal if tracksegement hasn't yet been allocated a block
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
                            // Can be considered normal if tracksegement hasn't yet been allocated a block
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
                            // Can be considered normal if tracksegement hasn't yet been allocated a block
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
                            // Can be considered normal if tracksegement hasn't yet been allocated a block
                            log.debug("TrackSegement at connection A doesn't contain a layout block");
                        }
                    } else if (getLayoutBlock() != getLayoutBlockB()) {
                        try {
                            boundaryBetween[0] = (getLayoutBlock().getDisplayName() + " - " + getLayoutBlockB().getDisplayName());
                        } catch (java.lang.NullPointerException e) {
                            // Can be considered normal if tracksegement hasn't yet been allocated a block
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
                            // Can be considered normal if tracksegement hasn't yet been allocated a block
                            log.debug("TrackSegement at connection B doesn't contain a layout block");
                        }
                    } else if (getLayoutBlock() != getLayoutBlockB()) {
                        // This is an interal block on the turnout
                        try {
                            boundaryBetween[1] = (getLayoutBlockB().getDisplayName() + " - " + getLayoutBlock().getDisplayName());
                        } catch (java.lang.NullPointerException e) {
                            // Can be considered normal if tracksegement hasn't yet been allocated a block
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
                            // Can be considered normal if tracksegement hasn't yet been allocated a block
                            log.debug("TrackSegement at connection C doesn't contain a layout block");
                        }
                    } else if (getLayoutBlockC() != getLayoutBlockD()) {
                        // This is an interal block on the turnout
                        try {
                            boundaryBetween[2] = (getLayoutBlockC().getDisplayName() + " - " + getLayoutBlockD().getDisplayName());
                        } catch (java.lang.NullPointerException e) {
                            // Can be considered normal if tracksegement hasn't yet been allocated a block
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
                            // Can be considered normal if tracksegement hasn't yet been allocated a block
                            log.debug("TrackSegement at connection C doesn't contain a layout block");
                        }
                    } else if (getLayoutBlockC() != getLayoutBlockD()) {
                        // This is an interal block on the turnout
                        try {
                            boundaryBetween[3] = (getLayoutBlockD().getDisplayName() + " - " + getLayoutBlockC().getDisplayName());
                        } catch (java.lang.NullPointerException e) {
                            // Can be considered normal if tracksegement hasn't yet been allocated a block
                            log.debug("TrackSegement at connection A doesn't contain a layout block");
                        }
                    }
                }
            }

        }
        return boundaryBetween;
    }   // getBlockBoundaries

    @Nonnull
    public ArrayList<LayoutBlock> getProtectedBlocks(jmri.NamedBean bean) {
        ArrayList<LayoutBlock> ret = new ArrayList<>(2);
        if (getLayoutBlock() == null) {
            return ret;
        }
        if (isTurnoutTypeXover()) {
            if ((getTurnoutType() == TurnoutType.DOUBLE_XOVER || getTurnoutType() == TurnoutType.RH_XOVER)
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
            if ((getTurnoutType() == TurnoutType.DOUBLE_XOVER || getTurnoutType() == TurnoutType.LH_XOVER)
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
            if (getTurnoutType() == TurnoutType.RH_XOVER && (getSignalBMast() == bean
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
            if (getTurnoutType() == TurnoutType.LH_XOVER && (getSensorA() == bean
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
                    // Mast at throat
                    // if the turnout is in the same block as the segment connected at the throat, then we can be protecting two blocks
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
                    // Mast at Continuing
                    if (connectB != null && ((TrackSegment) connectB).getLayoutBlock() == getLayoutBlock()) {
                        if (((TrackSegment) connectA).getLayoutBlock() != getLayoutBlock()) {
                            ret.add(((TrackSegment) connectA).getLayoutBlock());
                        }
                    } else {
                        ret.add(getLayoutBlock());
                    }
                } else if (getSignalCMast() == bean || getSensorC() == bean) {
                    // Mast at Diverging
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

    protected void removeSML(@CheckForNull SignalMast signalMast) {
        if (signalMast == null) {
            return;

        }
        if (jmri.InstanceManager.getDefault(LayoutBlockManager.class).isAdvancedRoutingEnabled()
                && InstanceManager.getDefault(jmri.SignalMastLogicManager.class).isSignalMastUsed(signalMast)) {

            SignallingGuiTools.removeSignalMastLogic(null, signalMast);
        }
    }

    /**
     * Remove this object from display and persistance.
     */
    public void remove() {
        // if a turnout has been activated, deactivate it
        deactivateTurnout();
        // remove from persistance by flagging inactive
        active = false;
    }

    boolean active = true;

    /**
     * "active" means that the object is still displayed, and should be stored.
     * @return true if active, else false.
     */
    public boolean isActive() {
        return active;
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
        // TODO: Determine if this should be being used
        // LayoutBlock layoutBlockD = ((TrackSegment) getConnectD()).getLayoutBlock();

        TurnoutType tTyp = getTurnoutType();
        switch (tTyp) {
            case RH_TURNOUT:
            case LH_TURNOUT:
            case WYE_TURNOUT: {
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
                            log.error("{}.getConnectivityStateForLayoutBlocks(...); Cannot determine turnout setting for {}",
                                    getName(), getTurnoutName());
                        }
                        result = Turnout.CLOSED;
                    }
                } else if (layoutBlockB == currLayoutBlock) {
                    result = Turnout.CLOSED;
                } else if (layoutBlockC == currLayoutBlock) {
                    result = Turnout.THROWN;
                } else {
                    if (!suppress) {
                        log.debug("lb {} nlb {} connect B {} connect C {}", currLayoutBlock, nextLayoutBlock, layoutBlockB, layoutBlockC);
                        log.error("{}.getConnectivityStateForLayoutBlocks(...); Cannot determine turnout setting for {}",
                                getName(), getTurnoutName());
                    }
                    result = Turnout.CLOSED;
                }
                break;
            }
            case RH_XOVER:
            case LH_XOVER:
            case DOUBLE_XOVER: {
                if (getLayoutBlock() == currLayoutBlock) {
                    if ((tTyp != TurnoutType.LH_XOVER)
                            && ((getLayoutBlockC() == nextLayoutBlock)
                            || (getLayoutBlockC() == prevLayoutBlock))) {
                        result = Turnout.THROWN;
                    } else if ((getLayoutBlockB() == nextLayoutBlock) || (getLayoutBlockB() == prevLayoutBlock)) {
                        result = Turnout.CLOSED;
                    } else if (getLayoutBlockB() == currLayoutBlock) {
                        result = Turnout.CLOSED;
                    } else if ((tTyp != LayoutTurnout.TurnoutType.LH_XOVER)
                            && (getLayoutBlockC() == currLayoutBlock)) {
                        result = Turnout.THROWN;
                    } else {
                        if (!suppress) {
                            log.error("{}.getConnectivityStateForLayoutBlocks(...); Cannot determine turnout setting for {}",
                                    getName(), getTurnoutName());
                        }
                        result = Turnout.CLOSED;
                    }
                } else if (getLayoutBlockB() == currLayoutBlock) {
                    if ((getLayoutBlock() == nextLayoutBlock) || (getLayoutBlock() == prevLayoutBlock)) {
                        result = Turnout.CLOSED;
                    } else if ((tTyp != TurnoutType.RH_XOVER)
                            && ((getLayoutBlockD() == nextLayoutBlock)
                            || (getLayoutBlockD() == prevLayoutBlock) || (getLayoutBlockD() == currLayoutBlock))) {
                        result = Turnout.THROWN;
                    } else {
                        if (!suppress) {
                            log.error("{}.getConnectivityStateForLayoutBlocks(...); Cannot determine turnout setting for {}",
                                    getName(), getTurnoutName());
                        }
                        result = Turnout.CLOSED;
                    }
                } else if (getLayoutBlockC() == currLayoutBlock) {
                    if ((tTyp != TurnoutType.LH_XOVER)
                            && ((getLayoutBlock() == nextLayoutBlock) || (getLayoutBlock() == prevLayoutBlock))) {
                        result = Turnout.THROWN;
                    } else if ((getLayoutBlockD() == nextLayoutBlock) || (getLayoutBlockD() == prevLayoutBlock) || (getLayoutBlockD() == currLayoutBlock)) {
                        result = Turnout.CLOSED;
                    } else if ((tTyp != TurnoutType.LH_XOVER)
                            && (getLayoutBlockD() == currLayoutBlock)) {
                        result = Turnout.THROWN;
                    } else {
                        if (!suppress) {
                            log.error("{}.getConnectivityStateForLayoutBlocks(...); Cannot determine turnout setting for {}",
                                    getName(), getTurnoutName());
                        }
                        result = Turnout.CLOSED;
                    }
                } else if (getLayoutBlockD() == currLayoutBlock) {
                    if ((getLayoutBlockC() == nextLayoutBlock) || (getLayoutBlockC() == prevLayoutBlock)) {
                        result = Turnout.CLOSED;
                    } else if ((tTyp != TurnoutType.RH_XOVER)
                            && ((getLayoutBlockB() == nextLayoutBlock) || (getLayoutBlockB() == prevLayoutBlock))) {
                        result = Turnout.THROWN;
                    } else {
                        if (!suppress) {
                            log.error("{}.getConnectivityStateForLayoutBlocks(...); Cannot determine turnout setting for {}",
                                    getName(), getTurnoutName());
                        }
                        result = Turnout.CLOSED;
                    }
                }
                break;
            }
            default: {
                log.warn("{}.getConnectivityStateForLayoutBlocks(...) unknown getTurnoutType: {}", getName(), tTyp);
                break;
            }
        }   // switch (tTyp)

        return result;
    }   // getConnectivityStateForLayoutBlocks

    /**
     * {@inheritDoc}
     */
    // TODO: on the cross-overs, check the internal boundary details.
    @Override
    public void reCheckBlockBoundary() {
        if (connectA == null && connectB == null && connectC == null) {
            if (isTurnoutTypeTurnout()) {
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
            // could still be in the process of rebuilding.
            return;
        } else if ((connectD == null) && isTurnoutTypeXover()) {
            // could still be in the process of rebuilding.
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

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    protected List<LayoutConnectivity> getLayoutConnectivity() {
        List<LayoutConnectivity> results = new ArrayList<>();

        log.trace("Start in layoutTurnout.getLayoutConnectivity for {}", getName());

        LayoutConnectivity lc = null;

        LayoutBlock lbA = getLayoutBlock(), lbB = getLayoutBlockB(), lbC = getLayoutBlockC(), lbD = getLayoutBlockD();

        log.trace("    type: {}", type);
        log.trace("     lbA: {}", lbA);
        log.trace("     lbB: {}", lbB);
        log.trace("     lbC: {}", lbC);
        log.trace("     lbD: {}", lbD);

        if (hasEnteringDoubleTrack() && (lbA != null)) {
            // have a crossover turnout with at least one block, check for multiple blocks
            if ((lbA != lbB) || (lbA != lbC) || (lbA != lbD)) {
                // have multiple blocks and therefore internal block boundaries
                if (lbA != lbB) {
                    // have a AB block boundary, create a LayoutConnectivity
                    log.debug("Block boundary  ('{}'<->'{}') found at {}", lbA, lbB, this);
                    lc = new LayoutConnectivity(lbA, lbB);
                    lc.setXoverBoundary(this, LayoutConnectivity.XOVER_BOUNDARY_AB);

                    // The following line needed to change, because it uses location of
                    // the points on the TurnoutView itself. Change to
                    // direction from connections.
                    //lc.setDirection(Path.computeDirection(getCoordsA(), getCoordsB()));
                    lc.setDirection( models.computeDirectionAB(this) );

                    log.trace("getLayoutConnectivity lbA != lbB");
                    log.trace("   Block boundary  ('{}'<->'{}') found at {}", lbA, lbB, this);

                    results.add(lc);
                }
                if ((getTurnoutType() != TurnoutType.LH_XOVER) && (lbA != lbC)) {
                    // have a AC block boundary, create a LayoutConnectivity
                    log.debug("Block boundary  ('{}'<->'{}') found at {}", lbA, lbC, this);
                    lc = new LayoutConnectivity(lbA, lbC);
                    lc.setXoverBoundary(this, LayoutConnectivity.XOVER_BOUNDARY_AC);

                    // The following line needed to change, because it uses location of
                    // the points on the TurnoutView itself. Change to
                    // direction from connections.
                    //lc.setDirection(Path.computeDirection(getCoordsA(), getCoordsC()));
                    lc.setDirection( models.computeDirectionAC(this) );

                    log.trace("getLayoutConnectivity lbA != lbC");
                    log.trace("   Block boundary  ('{}'<->'{}') found at {}", lbA, lbC, this);

                    results.add(lc);
                }
                if (lbC != lbD) {
                    // have a CD block boundary, create a LayoutConnectivity
                    log.debug("Block boundary  ('{}'<->'{}') found at {}", lbC, lbD, this);
                    lc = new LayoutConnectivity(lbC, lbD);
                    lc.setXoverBoundary(this, LayoutConnectivity.XOVER_BOUNDARY_CD);

                    // The following line needed to change, because it uses location of
                    // the points on the TurnoutView itself. Change to
                    // direction from connections.
                    //lc.setDirection(Path.computeDirection(getCoordsC(), getCoordsD()));
                    lc.setDirection( models.computeDirectionCD(this) );

                    log.trace("getLayoutConnectivity lbC != lbD");
                    log.trace("   Block boundary  ('{}'<->'{}') found at {}", lbC, lbD, this);

                    results.add(lc);
                }
                if ((getTurnoutType() != TurnoutType.RH_XOVER) && (lbB != lbD)) {
                    // have a BD block boundary, create a LayoutConnectivity
                    log.debug("Block boundary  ('{}'<->'{}') found at {}", lbB, lbD, this);
                    lc = new LayoutConnectivity(lbB, lbD);
                    lc.setXoverBoundary(this, LayoutConnectivity.XOVER_BOUNDARY_BD);

                    // The following line needed to change, because it uses location of
                    // the points on the TurnoutView itself. Change to
                    // direction from connections.
                    //lc.setDirection(Path.computeDirection(getCoordsB(), getCoordsD()));
                    lc.setDirection( models.computeDirectionBD(this) );

                    log.trace("getLayoutConnectivity lbB != lbD");
                    log.trace("   Block boundary  ('{}'<->'{}') found at {}", lbB, lbD, this);

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
    @Nonnull
    public List<HitPointType> checkForFreeConnections() {
        List<HitPointType> result = new ArrayList<>();

        // check the A connection point
        if (getConnectA() == null) {
            result.add(HitPointType.TURNOUT_A);
        }

        // check the B connection point
        if (getConnectB() == null) {
            result.add(HitPointType.TURNOUT_B);
        }

        // check the C connection point
        if (getConnectC() == null) {
            result.add(HitPointType.TURNOUT_C);
        }

        // check the D connection point
        if (isTurnoutTypeXover()) {
            if (getConnectD() == null) {
                result.add(HitPointType.TURNOUT_D);
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

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutTurnout.class);
}
