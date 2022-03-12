package jmri.jmrit.display.layoutEditor;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.*;
import static java.lang.Float.POSITIVE_INFINITY;
import java.util.*;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.*;
import jmri.jmrit.display.layoutEditor.LayoutTurnout.Geometry;
import jmri.jmrit.display.layoutEditor.LayoutTurnout.LinkType;
import jmri.jmrit.display.layoutEditor.LayoutTurnout.TurnoutType;
import jmri.jmrit.display.layoutEditor.blockRoutingTable.LayoutBlockRouteTableAction;
import jmri.util.MathUtil;

/**
 * MVC View component for the LayoutTurnout class.
 *
 * @author Bob Jacobsen Copyright (c) 2020
 *
 */
public class LayoutTurnoutView extends LayoutTrackView {

    public LayoutTurnoutView(@Nonnull LayoutTurnout turnout,
            @Nonnull Point2D c, double rot,
            @Nonnull LayoutEditor layoutEditor) {
        this(turnout, c, rot, 1.0, 1.0, layoutEditor);
    }

    /**
     * Constructor method.
     *
     * @param turnout      the layout turnout to create the view for.
     * @param c            where to put it
     * @param rot          for display
     * @param xFactor      for display
     * @param yFactor      for display
     * @param layoutEditor what layout editor panel to put it in
     */
    public LayoutTurnoutView(@Nonnull LayoutTurnout turnout,
            @Nonnull Point2D c, double rot,
            double xFactor, double yFactor,
            @Nonnull LayoutEditor layoutEditor) {
        super(turnout, c, layoutEditor);
        this.turnout = turnout;

        setIdent(turnout.getName());

        int version = turnout.getVersion();

        // adjust initial coordinates
        if (turnout.getTurnoutType() == TurnoutType.LH_TURNOUT) {
            dispB = new Point2D.Double(layoutEditor.getTurnoutBX(), 0.0);
            dispA = new Point2D.Double(layoutEditor.getTurnoutCX(), -layoutEditor.getTurnoutWid());
        } else if (turnout.getTurnoutType() == TurnoutType.RH_TURNOUT) {
            dispB = new Point2D.Double(layoutEditor.getTurnoutBX(), 0.0);
            dispA = new Point2D.Double(layoutEditor.getTurnoutCX(), layoutEditor.getTurnoutWid());
        } else if (turnout.getTurnoutType() == TurnoutType.WYE_TURNOUT) {
            dispB = new Point2D.Double(layoutEditor.getTurnoutBX(), 0.5 * layoutEditor.getTurnoutWid());
            dispA = new Point2D.Double(layoutEditor.getTurnoutBX(), -0.5 * layoutEditor.getTurnoutWid());
        } else if (turnout.getTurnoutType() == TurnoutType.DOUBLE_XOVER) {
            if (version == 2) {
                super.setCoordsCenter(new Point2D.Double(layoutEditor.getXOverLong(), layoutEditor.getXOverHWid()));
                pointB = new Point2D.Double(layoutEditor.getXOverLong() * 2, 0);
                pointC = new Point2D.Double(layoutEditor.getXOverLong() * 2, (layoutEditor.getXOverHWid() * 2));
                pointD = new Point2D.Double(0, (layoutEditor.getXOverHWid() * 2));
                super.setCoordsCenter(c);
            } else {
                dispB = new Point2D.Double(layoutEditor.getXOverLong(), -layoutEditor.getXOverHWid());
                dispA = new Point2D.Double(layoutEditor.getXOverLong(), layoutEditor.getXOverHWid());
            }
        } else if (turnout.getTurnoutType() == TurnoutType.RH_XOVER) {
            if (version == 2) {
                super.setCoordsCenter(new Point2D.Double(layoutEditor.getXOverLong(), layoutEditor.getXOverHWid()));
                pointB = new Point2D.Double((layoutEditor.getXOverShort() + layoutEditor.getXOverLong()), 0);
                pointC = new Point2D.Double(layoutEditor.getXOverLong() * 2, (layoutEditor.getXOverHWid() * 2));
                pointD = new Point2D.Double((getCoordsCenter().getX() - layoutEditor.getXOverShort()), (layoutEditor.getXOverHWid() * 2));
                super.setCoordsCenter(c);
            } else {
                dispB = new Point2D.Double(layoutEditor.getXOverShort(), -layoutEditor.getXOverHWid());
                dispA = new Point2D.Double(layoutEditor.getXOverLong(), layoutEditor.getXOverHWid());
            }
        } else if (turnout.getTurnoutType() == TurnoutType.LH_XOVER) {
            if (version == 2) {
                super.setCoordsCenter(new Point2D.Double(layoutEditor.getXOverLong(), layoutEditor.getXOverHWid()));

                pointA = new Point2D.Double((getCoordsCenter().getX() - layoutEditor.getXOverShort()), 0);
                pointB = new Point2D.Double((layoutEditor.getXOverLong() * 2), 0);
                pointC = new Point2D.Double(layoutEditor.getXOverLong() + layoutEditor.getXOverShort(), (layoutEditor.getXOverHWid() * 2));
                pointD = new Point2D.Double(0, (layoutEditor.getXOverHWid() * 2));

                super.setCoordsCenter(c);
            } else {
                dispB = new Point2D.Double(layoutEditor.getXOverLong(), -layoutEditor.getXOverHWid());
                dispA = new Point2D.Double(layoutEditor.getXOverShort(), layoutEditor.getXOverHWid());
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

        editor = new jmri.jmrit.display.layoutEditor.LayoutEditorDialogs.LayoutTurnoutEditor(layoutEditor);
    }

    /**
     * Returns true if this is a turnout (not a crossover or slip)
     *
     * @param type the turnout type
     * @return boolean true if this is a turnout
     */
    public static boolean isTurnoutTypeTurnout(TurnoutType type) {
        return LayoutTurnout.isTurnoutTypeTurnout(type);
    }

    /**
     * Returns true if this is a turnout (not a crossover or slip)
     *
     * @return boolean true if this is a turnout
     */
    public boolean isTurnoutTypeTurnout() {
        return turnout.isTurnoutTypeTurnout();
    }

    /**
     * Returns true if this is a crossover
     *
     * @param type the turnout type
     * @return boolean true if this is a crossover
     */
    public static boolean isTurnoutTypeXover(TurnoutType type) {
        return LayoutTurnout.isTurnoutTypeXover(type);
    }

    /**
     * Returns true if this is a crossover
     *
     * @return boolean true if this is a crossover
     */
    public boolean isTurnoutTypeXover() {
        return turnout.isTurnoutTypeXover();
    }

    /**
     * Returns true if this is a slip
     *
     * @param type the turnout type
     * @return boolean true if this is a slip
     */
    public static boolean isTurnoutTypeSlip(TurnoutType type) {
        return LayoutTurnout.isTurnoutTypeSlip(type);
    }

    /**
     * Returns true if this is a slip
     *
     * @return boolean true if this is a slip
     */
    public boolean isTurnoutTypeSlip() {
        return turnout.isTurnoutTypeSlip();
    }

    /**
     * Returns true if this has a single-track entrance end. (turnout or wye)
     *
     * @param type the turnout type
     * @return boolean true if single track entrance
     */
    public static boolean hasEnteringSingleTrack(TurnoutType type) {
        return LayoutTurnout.hasEnteringSingleTrack(type);
    }

    /**
     * Returns true if this has a single-track entrance end. (turnout or wye)
     *
     * @return boolean true if single track entrance
     */
    public boolean hasEnteringSingleTrack() {
        return LayoutTurnout.hasEnteringSingleTrack(getTurnoutType());
    }

    /**
     * Returns true if this has double track on the entrance end (crossover or
     * slip)
     *
     * @param type the turnout type
     * @return boolean true if double track entrance
     */
    public static boolean hasEnteringDoubleTrack(TurnoutType type) {
        return LayoutTurnout.hasEnteringDoubleTrack(type);
    }

    /**
     * Returns true if this has double track on the entrance end (crossover or
     * slip)
     *
     * @return boolean true if double track entrance
     */
    public boolean hasEnteringDoubleTrack() {
        return turnout.hasEnteringDoubleTrack();
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

    public Point2D dispB = new Point2D.Double(20.0, 0.0);
    public Point2D dispA = new Point2D.Double(20.0, 10.0);
    public Point2D pointA = new Point2D.Double(0, 0);
    public Point2D pointB = new Point2D.Double(40, 0);
    public Point2D pointC = new Point2D.Double(60, 20);
    public Point2D pointD = new Point2D.Double(20, 20);

    private int version = 1;

    private final boolean useBlockSpeed = false;

    // temporary reference to the Editor that will eventually be part of View
    protected jmri.jmrit.display.layoutEditor.LayoutEditorDialogs.LayoutTurnoutEditor editor;

    final private LayoutTurnout turnout;

    public final LayoutTurnout getLayoutTurnout() {
        return turnout;
    }  // getTurnout() gets the real Turnout in the LayoutTurnout

    /**
     * {@inheritDoc}
     */
    // this should only be used for debugging...
    @Override
    @Nonnull
    public String toString() {
        return "LayoutTurnout " + getName();
    }

    //
    // Accessor methods
    //
    public int getVersion() {
        return version;
    }

    public void setVersion(int v) {
        version = v;
    }

    public boolean useBlockSpeed() {
        return useBlockSpeed;
    }

    // @CheckForNull - can this be null? or ""?
    public String getTurnoutName() {
        return turnout.getTurnoutName();
    }

    // @CheckForNull - can this be null? or ""?
    public String getSecondTurnoutName() {
        return turnout.getSecondTurnoutName();
    }

    @Nonnull
    public String getBlockName() {
        return turnout.getBlockName();
    }

    @Nonnull
    public String getBlockBName() {
        return turnout.getBlockBName();
    }

    @Nonnull
    public String getBlockCName() {
        return turnout.getBlockCName();
    }

    @Nonnull
    public String getBlockDName() {
        return turnout.getBlockDName();
    }

    @CheckForNull
    public SignalHead getSignalHead(Geometry loc) {
        return turnout.getSignalHead(loc);
    }

    @CheckForNull
    public SignalHead getSignalA1() {
        return turnout.getSignalA1();
    }

    @Nonnull
    public String getSignalA1Name() {
        return turnout.getSignalA1Name();
    }

    public void setSignalA1Name(@CheckForNull String signalHead) {
        turnout.setSignalA1Name(signalHead);
    }

    @CheckForNull
    public SignalHead getSignalA2() {
        return turnout.getSignalA2();
    }

    @Nonnull
    public String getSignalA2Name() {
        return turnout.getSignalA2Name();
    }

    public void setSignalA2Name(@CheckForNull String signalHead) {
        turnout.setSignalA2Name(signalHead);
    }

    @CheckForNull
    public SignalHead getSignalA3() {
        return turnout.getSignalA3();
    }

    @Nonnull
    public String getSignalA3Name() {
        return turnout.getSignalA3Name();
    }

    public void setSignalA3Name(@CheckForNull String signalHead) {
        turnout.setSignalA3Name(signalHead);
    }

    @CheckForNull
    public SignalHead getSignalB1() {
        return turnout.getSignalB1();
    }

    @Nonnull
    public String getSignalB1Name() {
        return turnout.getSignalB1Name();
    }

    public void setSignalB1Name(@CheckForNull String signalHead) {
        turnout.setSignalB1Name(signalHead);
    }

    @CheckForNull
    public SignalHead getSignalB2() {
        return turnout.getSignalB2();
    }

    @Nonnull
    public String getSignalB2Name() {
        return turnout.getSignalB2Name();
    }

    public void setSignalB2Name(@CheckForNull String signalHead) {
        turnout.setSignalB2Name(signalHead);
    }

    @CheckForNull
    public SignalHead getSignalC1() {
        return turnout.getSignalC1();
    }

    @Nonnull
    public String getSignalC1Name() {
        return turnout.getSignalC1Name();
    }

    public void setSignalC1Name(@CheckForNull String signalHead) {
        turnout.setSignalC1Name(signalHead);
    }

    @CheckForNull
    public SignalHead getSignalC2() {
        return turnout.getSignalC2();
    }

    @Nonnull
    public String getSignalC2Name() {
        return turnout.getSignalC2Name();
    }

    public void setSignalC2Name(@CheckForNull String signalHead) {
        turnout.setSignalC2Name(signalHead);
    }

    @CheckForNull
    public SignalHead getSignalD1() {
        return turnout.getSignalD1();
    }

    @Nonnull
    public String getSignalD1Name() {
        return turnout.getSignalD1Name();
    }

    public void setSignalD1Name(@CheckForNull String signalHead) {
        turnout.setSignalD1Name(signalHead);
    }

    @CheckForNull
    public SignalHead getSignalD2() {
        return turnout.getSignalD2();
    }

    @Nonnull
    public String getSignalD2Name() {
        return turnout.getSignalD2Name();
    }

    public void setSignalD2Name(@CheckForNull String signalHead) {
        turnout.setSignalD2Name(signalHead);
    }

    public void removeBeanReference(@CheckForNull jmri.NamedBean nb) {
        turnout.removeBeanReference(nb);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canRemove() {
        return turnout.canRemove();
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
        throw new IllegalArgumentException("should be called on LayoutTurnout");
    }

    @Nonnull
    public String getSignalAMastName() {
        return turnout.getSignalAMastName();
    }

    @CheckForNull
    public SignalMast getSignalAMast() {
        return turnout.getSignalAMast();
    }

    public void setSignalAMast(@CheckForNull String signalMast) {
        turnout.setSignalAMast(signalMast);
    }

    @Nonnull
    public String getSignalBMastName() {
        return turnout.getSignalBMastName();
    }

    @CheckForNull
    public SignalMast getSignalBMast() {
        return turnout.getSignalBMast();
    }

    public void setSignalBMast(@CheckForNull String signalMast) {
        turnout.setSignalBMast(signalMast);
    }

    @Nonnull
    public String getSignalCMastName() {
        return turnout.getSignalCMastName();
    }

    @CheckForNull
    public SignalMast getSignalCMast() {
        return turnout.getSignalCMast();
    }

    public void setSignalCMast(@CheckForNull String signalMast) {
        turnout.setSignalCMast(signalMast);
    }

    @Nonnull
    public String getSignalDMastName() {
        return turnout.getSignalDMastName();
    }

    @CheckForNull
    public SignalMast getSignalDMast() {
        return turnout.getSignalDMast();
    }

    public void setSignalDMast(@CheckForNull String signalMast) {
        turnout.setSignalDMast(signalMast);
    }

    @Nonnull
    public String getSensorAName() {
        return turnout.getSensorAName();
    }

    @CheckForNull
    public Sensor getSensorA() {
        return turnout.getSensorA();
    }

    public void setSensorA(@CheckForNull String sensorName) {
        turnout.setSensorA(sensorName);
    }

    @Nonnull
    public String getSensorBName() {
        return turnout.getSensorBName();
    }

    @CheckForNull
    public Sensor getSensorB() {
        return turnout.getSensorB();
    }

    public void setSensorB(@CheckForNull String sensorName) {
        turnout.setSensorB(sensorName);
    }

    @Nonnull
    public String getSensorCName() {
        return turnout.getSensorCName();
    }

    @CheckForNull
    public Sensor getSensorC() {
        return turnout.getSensorC();
    }

    public void setSensorC(@CheckForNull String sensorName) {
        turnout.setSensorC(sensorName);
    }

    @Nonnull
    public String getSensorDName() {
        return turnout.getSensorDName();
    }

    @CheckForNull
    public Sensor getSensorD() {
        return turnout.getSensorD();
    }

    public void setSensorD(@CheckForNull String sensorName) {
        turnout.setSensorD(sensorName);
    }

    public String getLinkedTurnoutName() {
        return turnout.getLinkedTurnoutName();
    }

    public void setLinkedTurnoutName(@Nonnull String s) {
        turnout.setSensorD(s);
    }  // Could be done with changing over to a NamedBeanHandle

    public LinkType getLinkType() {
        return turnout.getLinkType();
    }

    public void setLinkType(LinkType ltype) {
        turnout.setLinkType(ltype);
    }

    public TurnoutType getTurnoutType() {
        return turnout.getTurnoutType();
    }

    public LayoutTrack getConnectA() {
        return turnout.getConnectA();
    }

    public LayoutTrack getConnectB() {
        return turnout.getConnectB();
    }

    public LayoutTrack getConnectC() {
        return turnout.getConnectC();
    }

    public LayoutTrack getConnectD() {
        return turnout.getConnectD();
    }

    /**
     * @return null if no turnout set // temporary? Might want to run all calls
     *         through this class; but this is getModel equiv
     */
    // @CheckForNull  temporary
    public Turnout getTurnout() {
        return turnout.getTurnout();
    }

    public int getContinuingSense() {
        return turnout.getContinuingSense();
    }

    /**
     *
     * @return true is the continuingSense matches the known state
     */
    public boolean isInContinuingSenseState() {
        return turnout.isInContinuingSenseState();
    }

    public void setTurnout(@Nonnull String tName) {
        turnout.setTurnout(tName);
    }

    // @CheckForNull - need to have a better way to handle null case
    public Turnout getSecondTurnout() {
        return turnout.getSecondTurnout();
    }

    public void setSecondTurnout(@Nonnull String tName) {
        turnout.setSecondTurnout(tName);
    }

    public void setSecondTurnoutInverted(boolean inverted) {
        turnout.setSecondTurnoutInverted(inverted);
    }

    public void setContinuingSense(int sense) {
        turnout.setContinuingSense(sense);
    }

    public void setDisabled(boolean state) {
        turnout.setDisabled(state);
        if (layoutEditor != null) {
            layoutEditor.redrawPanel();
        }
    }

    public boolean isDisabled() {
        return turnout.isDisabled();
    }

    public void setDisableWhenOccupied(boolean state) {
        turnout.setDisableWhenOccupied(state);
        if (layoutEditor != null) {
            layoutEditor.redrawPanel();
        }
    }

    public boolean isDisabledWhenOccupied() {
        return turnout.isDisabledWhenOccupied();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @CheckForNull
    public LayoutTrack getConnection(HitPointType connectionType) throws jmri.JmriException {
        return turnout.getConnection(connectionType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setConnection(HitPointType connectionType, @CheckForNull LayoutTrack o, HitPointType type) throws jmri.JmriException {
        turnout.setConnection(connectionType, o, type);
    }

    public void setConnectA(@CheckForNull LayoutTrack o, HitPointType type) {
        turnout.setConnectA(o, type);
    }

    public void setConnectB(@CheckForNull LayoutTrack o, HitPointType type) {
        turnout.setConnectB(o, type);
    }

    public void setConnectC(@CheckForNull LayoutTrack o, HitPointType type) {
        turnout.setConnectC(o, type);
    }

    public void setConnectD(@CheckForNull LayoutTrack o, HitPointType type) {
        turnout.setConnectD(o, type);
    }

    // @CheckForNull - temporary while we work on centralized protection
    public LayoutBlock getLayoutBlock() {
        return turnout.getLayoutBlock();
    }

    // @CheckForNull - temporary while we work on centralized protection
    public LayoutBlock getLayoutBlockB() {
        return turnout.getLayoutBlockB();
    }

    // @CheckForNull - temporary while we work on centralized protection
    public LayoutBlock getLayoutBlockC() {
        return turnout.getLayoutBlockC();
    }

    // @CheckForNull - temporary while we work on centralized protection
    public LayoutBlock getLayoutBlockD() {
        return turnout.getLayoutBlockD();
    }

    @Nonnull
    public Point2D getCoordsA() {
        if (isTurnoutTypeXover()) {
            if (version == 2) {
                return pointA;
            }
            return MathUtil.subtract(getCoordsCenter(), dispA);
        } else if (getTurnoutType() == TurnoutType.WYE_TURNOUT) {
            return MathUtil.subtract(getCoordsCenter(), MathUtil.midPoint(dispB, dispA));
        } else {
            return MathUtil.subtract(getCoordsCenter(), dispB);
        }
    }

    @Nonnull
    public Point2D getCoordsB() {
        if ((version == 2) && isTurnoutTypeXover()) {
            return pointB;
        }
        return MathUtil.add(getCoordsCenter(), dispB);
    }

    @Nonnull
    public Point2D getCoordsC() {
        if ((version == 2) && isTurnoutTypeXover()) {
            return pointC;
        }
        return MathUtil.add(getCoordsCenter(), dispA);
    }

    @Nonnull
    public Point2D getCoordsD() {
        if ((version == 2) && isTurnoutTypeXover()) {
            return pointD;
        }
        // only allowed for single and double crossovers
        return MathUtil.subtract(getCoordsCenter(), dispB);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public Point2D getCoordsForConnectionType(HitPointType connectionType) {
        Point2D result = getCoordsCenter();
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
                log.error("{}.getCoordsForConnectionType({}); Invalid Connection Type",
                        getName(), connectionType); // NOI18N
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
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
    public void updateBlockInfo() {
        turnout.updateBlockInfo();
    }

    /**
     * Set default size parameters to correspond to this turnout's size.
     * <p>
     * note: only protected so LayoutTurnoutTest can call it
     */
    protected void setUpDefaultSize() {
        // remove the overall scale factor
        double bX = dispB.getX() / layoutEditor.gContext.getXScale();
        double bY = dispB.getY() / layoutEditor.gContext.getYScale();
        double cX = dispA.getX() / layoutEditor.gContext.getXScale();
        double cY = dispA.getY() / layoutEditor.gContext.getYScale();
        // calculate default parameters according to type of turnout
        double lenB = Math.hypot(bX, bY);
        double lenC = Math.hypot(cX, cY);
        double distBC = Math.hypot(bX - cX, bY - cY);
        if ((getTurnoutType() == TurnoutType.LH_TURNOUT)
                || (getTurnoutType() == TurnoutType.RH_TURNOUT)) {

            layoutEditor.setTurnoutBX(Math.round(lenB + 0.1));
            double xc = ((bX * cX) + (bY * cY)) / lenB;
            layoutEditor.setTurnoutCX(Math.round(xc + 0.1));
            layoutEditor.setTurnoutWid(Math.round(Math.sqrt((lenC * lenC) - (xc * xc)) + 0.1));
        } else if (getTurnoutType() == TurnoutType.WYE_TURNOUT) {
            double xx = Math.sqrt((lenB * lenB) - (0.25 * (distBC * distBC)));
            layoutEditor.setTurnoutBX(Math.round(xx + 0.1));
            layoutEditor.setTurnoutCX(Math.round(xx + 0.1));
            layoutEditor.setTurnoutWid(Math.round(distBC + 0.1));
        } else {
            if (version == 2) {
                double aX = pointA.getX() / layoutEditor.gContext.getXScale();
                double aY = pointA.getY() / layoutEditor.gContext.getYScale();
                bX = pointB.getX() / layoutEditor.gContext.getXScale();
                bY = pointB.getY() / layoutEditor.gContext.getYScale();
                cX = pointC.getX() / layoutEditor.gContext.getXScale();
                cY = pointC.getY() / layoutEditor.gContext.getYScale();
                double lenAB = Math.hypot(bX - aX, bY - aY);
                if (getTurnoutType() == TurnoutType.DOUBLE_XOVER) {
                    double lenBC = Math.hypot(bX - cX, bY - cY);
                    layoutEditor.setXOverLong(Math.round(lenAB / 2)); // set to half to be backwardly compatible
                    layoutEditor.setXOverHWid(Math.round(lenBC / 2));
                    layoutEditor.setXOverShort(Math.round((0.5 * lenAB) / 2));
                } else if (getTurnoutType() == TurnoutType.RH_XOVER) {
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
                } else if (getTurnoutType() == TurnoutType.LH_XOVER) {
                    double dY = pointD.getY() / layoutEditor.gContext.getYScale();
                    lenAB = lenAB / 3;
                    layoutEditor.setXOverShort(Math.round(lenAB));
                    layoutEditor.setXOverLong(Math.round(lenAB * 2));
                    double opp = (dY - cY);
                    double ang = Math.asin(opp / (lenAB * 3)); // Length of AB should be the same as CD
                    opp = Math.sin(ang) * lenAB;
                    cY = cY + opp;
                    double adj = Math.cos(ang) * lenAB;
                    cX = cX + adj;
                    double lenBC = Math.hypot(bX - cX, bY - cY);
                    layoutEditor.setXOverHWid(Math.round(lenBC / 2));
                }
            } else if (getTurnoutType() == TurnoutType.DOUBLE_XOVER) {
                double lng = Math.sqrt((lenB * lenB) - (0.25 * (distBC * distBC)));
                layoutEditor.setXOverLong(Math.round(lng + 0.1));
                layoutEditor.setXOverHWid(Math.round((0.5 * distBC) + 0.1));
                layoutEditor.setXOverShort(Math.round((0.5 * lng) + 0.1));
            } else if (getTurnoutType() == TurnoutType.RH_XOVER) {
                double distDC = Math.hypot(bX + cX, bY + cY);
                layoutEditor.setXOverShort(Math.round((0.25 * distDC) + 0.1));
                layoutEditor.setXOverLong(Math.round((0.75 * distDC) + 0.1));
                double hwid = Math.sqrt((lenC * lenC) - (0.5625 * distDC * distDC));
                layoutEditor.setXOverHWid(Math.round(hwid + 0.1));
            } else if (getTurnoutType() == TurnoutType.LH_XOVER) {
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
     *
     * @param newLayoutBlock See {@link LayoutTurnout#setLayoutBlock} for
     *                       definition
     */
    public void setLayoutBlock(LayoutBlock newLayoutBlock) {
        turnout.setLayoutBlock(newLayoutBlock);
        // correct any graphical artifacts
        setTrackSegmentBlocks();
    }

    public void setLayoutBlockB(LayoutBlock newLayoutBlock) {
        turnout.setLayoutBlockB(newLayoutBlock);
        // correct any graphical artifacts
        setTrackSegmentBlocks();
    }

    public void setLayoutBlockC(LayoutBlock newLayoutBlock) {
        turnout.setLayoutBlockC(newLayoutBlock);
        // correct any graphical artifacts
        setTrackSegmentBlocks();
    }

    public void setLayoutBlockD(LayoutBlock newLayoutBlock) {
        turnout.setLayoutBlockD(newLayoutBlock);
        // correct any graphical artifacts
        setTrackSegmentBlocks();
    }

    public void setLayoutBlockByName(@Nonnull String name) {
        turnout.setLayoutBlockByName(name);
    }

    public void setLayoutBlockBByName(@Nonnull String name) {
        turnout.setLayoutBlockByName(name);
    }

    public void setLayoutBlockCByName(@Nonnull String name) {
        turnout.setLayoutBlockByName(name);
    }

    public void setLayoutBlockDByName(@Nonnull String name) {
        turnout.setLayoutBlockByName(name);
    }

    /**
     * Check each connection point and update the block value for very short
     * track segments.
     *
     * @since 4.11.6
     */
    void setTrackSegmentBlocks() {
        setTrackSegmentBlock(HitPointType.TURNOUT_A, false);
        setTrackSegmentBlock(HitPointType.TURNOUT_B, false);
        setTrackSegmentBlock(HitPointType.TURNOUT_C, false);
        if (hasEnteringDoubleTrack()) {
            setTrackSegmentBlock(HitPointType.TURNOUT_D, false);
        }
    }

    /**
     * Update the block for a track segment that provides a (graphically) short
     * connection between a turnout and another object, normally another
     * turnout. These are hard to see and are frequently missed.
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
    void setTrackSegmentBlock(HitPointType pointType, boolean isAutomatic) {
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
                trkSeg = (TrackSegment) getConnectA();
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
                trkSeg = (TrackSegment) getConnectB();
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
                trkSeg = (TrackSegment) getConnectC();
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
                trkSeg = (TrackSegment) getConnectD();
                pointCoord = getCoordsD();
                if (isTurnoutTypeXover()) {
                    currBlk = blockD != null ? blockD : blockA;
                }
                break;
            default:
                log.error("{}.setTrackSegmentBlock({}, {}); Invalid pointType",
                        getName(), pointType, isAutomatic ? "AUTO" : "NON-AUTO");
                return;
        }
        if (trkSeg != null) {
            double chkSize = LayoutEditor.SIZE * layoutEditor.getTurnoutCircleSize();
            double segLength = 0;
            if (!isAutomatic) {
                Point2D segCenter = getCoordsCenter();
                segLength = MathUtil.distance(pointCoord, segCenter) * 2;
            }
            if (segLength < chkSize) {

                log.debug("Set block:");
                log.debug("    seg: {}", trkSeg);
                log.debug("    cor: {}", pointCoord);
                log.debug("    blk: {}", (currBlk == null) ? "null" : currBlk.getDisplayName());
                log.debug("    len: {}", segLength);

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
        return turnout.isMainlineA();
    }

    public boolean isMainlineB() {
        return turnout.isMainlineB();
    }

    public boolean isMainlineC() {
        return turnout.isMainlineC();
    }

    public boolean isMainlineD() {
        return turnout.isMainlineD();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected HitPointType findHitPointType(@Nonnull Point2D hitPoint, boolean useRectangles, boolean requireUnconnected) {
        HitPointType result = HitPointType.NONE;  // assume point not on connection
        // note: optimization here: instead of creating rectangles for all the
        // points to check below, we create a rectangle for the test point
        // and test if the points below are in that rectangle instead.
        Rectangle2D r = layoutEditor.layoutEditorControlCircleRectAt(hitPoint);
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
                result = HitPointType.TURNOUT_CENTER;
            }
        }

        // check the A connection point
        if (!requireUnconnected || (getConnectA() == null)) {
            p = getCoordsA();
            distance = MathUtil.distance(p, hitPoint);
            if (distance < minDistance) {
                minDistance = distance;
                minPoint = p;
                result = HitPointType.TURNOUT_A;
            }
        }

        // check the B connection point
        if (!requireUnconnected || (getConnectB() == null)) {
            p = getCoordsB();
            distance = MathUtil.distance(p, hitPoint);
            if (distance < minDistance) {
                minDistance = distance;
                minPoint = p;
                result = HitPointType.TURNOUT_B;
            }
        }

        // check the C connection point
        if (!requireUnconnected || (getConnectC() == null)) {
            p = getCoordsC();
            distance = MathUtil.distance(p, hitPoint);
            if (distance < minDistance) {
                minDistance = distance;
                minPoint = p;
                result = HitPointType.TURNOUT_C;
            }
        }

        // check the D connection point
        if (isTurnoutTypeXover()) {
            if (!requireUnconnected || (getConnectD() == null)) {
                p = getCoordsD();
                distance = MathUtil.distance(p, hitPoint);
                if (distance < minDistance) {
                    minDistance = distance;
                    minPoint = p;
                    result = HitPointType.TURNOUT_D;
                }
            }
        }
        if ((useRectangles && !r.contains(minPoint))
                || (!useRectangles && (minDistance > circleRadius))) {
            result = HitPointType.NONE;
        }
        return result;
    }   // findHitPointType

    /*
    * Modify coordinates methods
     */
    /**
     * {@inheritDoc}
     */
    @Override
    public void setCoordsCenter(@Nonnull Point2D p) {
        Point2D offset = MathUtil.subtract(p, getCoordsCenter());
        pointA = MathUtil.add(pointA, offset);
        pointB = MathUtil.add(pointB, offset);
        pointC = MathUtil.add(pointC, offset);
        pointD = MathUtil.add(pointD, offset);
        super.setCoordsCenter(p);
    }

    // temporary should be private once LayoutTurnout no longer needs it
    void reCalculateCenter() {
        super.setCoordsCenter(MathUtil.midPoint(pointA, pointC));
    }

    public void setCoordsA(@Nonnull Point2D p) {
        pointA = p;
        if (version == 2) {
            reCalculateCenter();
        }
        double x = getCoordsCenter().getX() - p.getX();
        double y = getCoordsCenter().getY() - p.getY();
        if (getTurnoutType() == TurnoutType.DOUBLE_XOVER) {
            dispA = new Point2D.Double(x, y);
            // adjust to maintain rectangle
            double oldLength = MathUtil.length(dispB);
            double newLength = Math.hypot(x, y);
            dispB = MathUtil.multiply(dispB, newLength / oldLength);
        } else if ((getTurnoutType() == TurnoutType.RH_XOVER)
                || (getTurnoutType() == TurnoutType.LH_XOVER)) {
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
            if (getTurnoutType() == TurnoutType.RH_XOVER) {
                x = xi - (0.333333 * (-x - xi));
                y = yi - (0.333333 * (-y - yi));
            } else if (getTurnoutType() == TurnoutType.LH_XOVER) {
                x = xi - (3.0 * (-x - xi));
                y = yi - (3.0 * (-y - yi));
            }
            dispB = new Point2D.Double(x, y);
        } else if (getTurnoutType() == TurnoutType.WYE_TURNOUT) {
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
        double x = getCoordsCenter().getX() - p.getX();
        double y = getCoordsCenter().getY() - p.getY();
        dispB = new Point2D.Double(-x, -y);
        if ((getTurnoutType() == TurnoutType.DOUBLE_XOVER)
                || (getTurnoutType() == TurnoutType.WYE_TURNOUT)) {
            // adjust to maintain rectangle or wye shape
            double oldLength = MathUtil.length(dispA);
            double newLength = Math.hypot(x, y);
            dispA = MathUtil.multiply(dispA, newLength / oldLength);
        } else if ((getTurnoutType() == TurnoutType.RH_XOVER)
                || (getTurnoutType() == TurnoutType.LH_XOVER)) {
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
            if (getTurnoutType() == TurnoutType.LH_XOVER) {
                x = xi - (0.333333 * (x - xi));
                y = yi - (0.333333 * (y - yi));
            } else if (getTurnoutType() == TurnoutType.RH_XOVER) {
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
        double x = getCoordsCenter().getX() - p.getX();
        double y = getCoordsCenter().getY() - p.getY();
        dispA = new Point2D.Double(-x, -y);
        if ((getTurnoutType() == TurnoutType.DOUBLE_XOVER)
                || (getTurnoutType() == TurnoutType.WYE_TURNOUT)) {
            // adjust to maintain rectangle or wye shape
            double oldLength = MathUtil.length(dispB);
            double newLength = Math.hypot(x, y);
            dispB = MathUtil.multiply(dispB, newLength / oldLength);
        } else if ((getTurnoutType() == TurnoutType.RH_XOVER)
                || (getTurnoutType() == TurnoutType.LH_XOVER)) {
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
            if (getTurnoutType() == TurnoutType.RH_XOVER) {
                x = xi - (0.333333 * (-x - xi));
                y = yi - (0.333333 * (-y - yi));
            } else if (getTurnoutType() == TurnoutType.LH_XOVER) {
                x = xi - (3.0 * (-x - xi));
                y = yi - (3.0 * (-y - yi));
            }
            dispB = new Point2D.Double(-x, -y);
        }
    }

    public void setCoordsD(Point2D p) {
        pointD = p;

        // only used for crossovers
        double x = getCoordsCenter().getX() - p.getX();
        double y = getCoordsCenter().getY() - p.getY();
        dispB = new Point2D.Double(x, y);
        if (getTurnoutType() == TurnoutType.DOUBLE_XOVER) {
            // adjust to maintain rectangle
            double oldLength = MathUtil.length(dispA);
            double newLength = Math.hypot(x, y);
            dispA = MathUtil.multiply(dispA, newLength / oldLength);
        } else if ((getTurnoutType() == TurnoutType.RH_XOVER)
                || (getTurnoutType() == TurnoutType.LH_XOVER)) {
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
            if (getTurnoutType() == TurnoutType.LH_XOVER) {
                x = xi - (0.333333 * (-x - xi));
                y = yi - (0.333333 * (-y - yi));
            } else if (getTurnoutType() == TurnoutType.RH_XOVER) {
                x = xi - (3.0 * (-x - xi));
                y = yi - (3.0 * (-y - yi));
            }
            dispA = new Point2D.Double(x, y);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void scaleCoords(double xFactor, double yFactor) {
        Point2D factor = new Point2D.Double(xFactor, yFactor);
        super.setCoordsCenter(MathUtil.granulize(MathUtil.multiply(getCoordsCenter(), factor), 1.0));

        dispA = MathUtil.granulize(MathUtil.multiply(dispA, factor), 1.0);
        dispB = MathUtil.granulize(MathUtil.multiply(dispB, factor), 1.0);

        pointA = MathUtil.granulize(MathUtil.multiply(pointA, factor), 1.0);
        pointB = MathUtil.granulize(MathUtil.multiply(pointB, factor), 1.0);
        pointC = MathUtil.granulize(MathUtil.multiply(pointC, factor), 1.0);
        pointD = MathUtil.granulize(MathUtil.multiply(pointD, factor), 1.0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void translateCoords(double xFactor, double yFactor) {
        Point2D factor = new Point2D.Double(xFactor, yFactor);
        super.setCoordsCenter(MathUtil.add(getCoordsCenter(), factor));
        pointA = MathUtil.add(pointA, factor);
        pointB = MathUtil.add(pointB, factor);
        pointC = MathUtil.add(pointC, factor);
        pointD = MathUtil.add(pointD, factor);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void rotateCoords(double angleDEG) {
        // rotate coordinates
        double rotRAD = Math.toRadians(angleDEG);
        double sineRot = Math.sin(rotRAD);
        double cosineRot = Math.cos(rotRAD);

        // rotate displacements around origin {0, 0}
        Point2D center_temp = getCoordsCenter();
        super.setCoordsCenter(MathUtil.zeroPoint2D);
        dispA = rotatePoint(dispA, sineRot, cosineRot);
        dispB = rotatePoint(dispB, sineRot, cosineRot);
        super.setCoordsCenter(center_temp);

        pointA = rotatePoint(pointA, sineRot, cosineRot);
        pointB = rotatePoint(pointB, sineRot, cosineRot);
        pointC = rotatePoint(pointC, sineRot, cosineRot);
        pointD = rotatePoint(pointD, sineRot, cosineRot);
    }

    public double getRotationDEG() {
        double result = 0;
        switch (getTurnoutType()) {
            case RH_TURNOUT:
            case LH_TURNOUT:
            case WYE_TURNOUT: {
                result = 90 - MathUtil.computeAngleDEG(getCoordsA(), getCoordsCenter());
                break;
            }
            case DOUBLE_XOVER:
            case RH_XOVER:
            case LH_XOVER: {
                result = 90 - MathUtil.computeAngleDEG(getCoordsA(), getCoordsB());
                break;
            }
            default: {
                break;
            }
        }
        return result;
    }

    /**
     * Toggle turnout if clicked on, physical turnout exists, and not disabled.
     */
    public void toggleTurnout() {
        turnout.toggleTurnout();
    }

    /**
     * Set the LayoutTurnout state. Used for sending the toggle command Checks
     * not disabled, disable when occupied Also sets secondary Turnout commanded
     * state
     *
     * @param state New state to set, eg Turnout.CLOSED
     */
    public void setState(int state) {
        turnout.setState(state);
    }

    /**
     * Get the LayoutTurnout state
     * <p>
     * Ensures the secondary Turnout state matches the primary
     *
     * @return the state, eg Turnout.CLOSED or Turnout.INCONSISTENT
     */
    public int getState() {
        return turnout.getState();
    }

    /**
     * Is this turnout occupied?
     *
     * @return true if occupied
     */
    private boolean isOccupied() {
        return turnout.isOccupied();
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
            JMenuItem jmi = popup.add(Bundle.getMessage("MakeLabel", label) + getName());
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
            if ((getConnectA() != null) || (getConnectB() != null)
                    || (getConnectC() != null) || (getConnectD() != null)) {
                JMenu connectionsMenu = new JMenu(Bundle.getMessage("Connections")); // there is no pane opening (which is what ... implies)
                if (getConnectA() != null) {
                    connectionsMenu.add(new AbstractAction(Bundle.getMessage("MakeLabel", "A") + getConnectA().getName()) {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            LayoutEditorFindItems lf = layoutEditor.getFinder();
                            LayoutTrack lt = lf.findObjectByName(getConnectA().getName());
                            // this shouldn't ever be null... however...
                            if (lt != null) {
                                LayoutTrackView ltv = layoutEditor.getLayoutTrackView(lt);
                                layoutEditor.setSelectionRect(ltv.getBounds());
                                ltv.showPopup();
                            }
                        }
                    });
                }
                if (getConnectB() != null) {
                    connectionsMenu.add(new AbstractAction(Bundle.getMessage("MakeLabel", "B") + getConnectB().getName()) {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            LayoutEditorFindItems lf = layoutEditor.getFinder();
                            LayoutTrack lt = lf.findObjectByName(getConnectB().getName());
                            // this shouldn't ever be null... however...
                            if (lt != null) {
                                LayoutTrackView ltv = layoutEditor.getLayoutTrackView(lt);
                                layoutEditor.setSelectionRect(ltv.getBounds());
                                ltv.showPopup();
                            }
                        }
                    });
                }
                if (getConnectC() != null) {
                    connectionsMenu.add(new AbstractAction(Bundle.getMessage("MakeLabel", "C") + getConnectC().getName()) {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            LayoutEditorFindItems lf = layoutEditor.getFinder();
                            LayoutTrack lt = lf.findObjectByName(getConnectC().getName());
                            // this shouldn't ever be null... however...
                            if (lt != null) {
                                LayoutTrackView ltv = layoutEditor.getLayoutTrackView(lt);
                                layoutEditor.setSelectionRect(ltv.getBounds());
                                ltv.showPopup();
                            }
                        }
                    });
                }
                if (getConnectD() != null) {
                    connectionsMenu.add(new AbstractAction(Bundle.getMessage("MakeLabel", "D") + getConnectD().getName()) {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            LayoutEditorFindItems lf = layoutEditor.getFinder();
                            LayoutTrack lt = lf.findObjectByName(getConnectD().getName());
                            // this shouldn't ever be null... however...
                            if (lt != null) {
                                LayoutTrackView ltv = layoutEditor.getLayoutTrackView(lt);
                                layoutEditor.setSelectionRect(ltv.getBounds());
                                ltv.showPopup();
                            }
                        }
                    });
                }
                popup.add(connectionsMenu);
            }
            popup.add(new JSeparator(JSeparator.HORIZONTAL));

            JCheckBoxMenuItem hiddenCheckBoxMenuItem = new JCheckBoxMenuItem(Bundle.getMessage("Hidden"));
            hiddenCheckBoxMenuItem.setSelected(isHidden());
            popup.add(hiddenCheckBoxMenuItem);
            hiddenCheckBoxMenuItem.addActionListener((java.awt.event.ActionEvent e1) -> {
                JCheckBoxMenuItem o = (JCheckBoxMenuItem) e1.getSource();
                setHidden(o.isSelected());
            });

            JCheckBoxMenuItem cbmi = new JCheckBoxMenuItem(Bundle.getMessage("Disabled"));
            cbmi.setSelected(isDisabled());
            popup.add(cbmi);
            cbmi.addActionListener((java.awt.event.ActionEvent e2) -> {
                JCheckBoxMenuItem o = (JCheckBoxMenuItem) e2.getSource();
                setDisabled(o.isSelected());
            });

            cbmi = new JCheckBoxMenuItem(Bundle.getMessage("DisabledWhenOccupied"));
            if (getTurnout() == null || getBlockName().isEmpty()) {
                cbmi.setEnabled(false);
            }
            cbmi.setSelected(isDisabledWhenOccupied());
            popup.add(cbmi);
            cbmi.addActionListener((java.awt.event.ActionEvent e3) -> {
                JCheckBoxMenuItem o = (JCheckBoxMenuItem) e3.getSource();
                setDisableWhenOccupied(o.isSelected());
            });

            // Rotate if there are no track connections
//            if ((getConnectA() == null) && (getConnectB() == null)
//                    && (getConnectC() == null)
//                    && (getConnectD() == null))
            {
                JMenuItem rotateItem = new JMenuItem(Bundle.getMessage("Rotate_",
                        String.format("%.1f", getRotationDEG())) + "...");
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
                    editor.editLayoutTrack(LayoutTurnoutView.this);
                }
            });
            popup.add(new AbstractAction(Bundle.getMessage("ButtonDelete")) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (canRemove() && layoutEditor.removeLayoutTurnout(turnout)) {
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
                        LayoutEditorToolBarPanel letbp = getLayoutEditorToolBarPanel();
                        if (isTurnoutTypeXover()) {
                            tools.setSignalsAtXoverTurnoutFromMenu(turnout,
                                    letbp.signalIconEditor, letbp.signalFrame);
                        } else if (getLinkType() == LinkType.NO_LINK) {
                            tools.setSignalsAtTurnoutFromMenu(turnout,
                                    letbp.signalIconEditor, letbp.signalFrame);
                        } else if (getLinkType() == LinkType.THROAT_TO_THROAT) {
                            tools.setSignalsAtThroatToThroatTurnoutsFromMenu(turnout, getLinkedTurnoutName(),
                                    letbp.signalIconEditor, letbp.signalFrame);
                        } else if (getLinkType() == LinkType.FIRST_3_WAY) {
                            tools.setSignalsAt3WayTurnoutFromMenu(getTurnoutName(), getLinkedTurnoutName(),
                                    letbp.signalIconEditor, letbp.signalFrame);
                        } else if (getLinkType() == LinkType.SECOND_3_WAY) {
                            tools.setSignalsAt3WayTurnoutFromMenu(getLinkedTurnoutName(), getTurnoutName(),
                                    letbp.signalIconEditor, letbp.signalFrame);
                        }
                    }
                };

                JMenu jm = new JMenu(Bundle.getMessage("SignalHeads"));
                if (layoutEditor.getLETools().addLayoutTurnoutSignalHeadInfoToMenu(
                        getTurnoutName(), getLinkedTurnoutName(), jm)) {
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
                            layoutEditor.getLETools().setSignalMastsAtTurnoutFromMenu(turnout,
                                    boundaryBetween);
                        }
                    });
                    popup.add(new AbstractAction(Bundle.getMessage("SetSensors")) {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            LayoutEditorToolBarPanel letbp = getLayoutEditorToolBarPanel();
                            layoutEditor.getLETools().setSensorsAtTurnoutFromMenu(
                                    turnout,
                                    boundaryBetween,
                                    letbp.sensorIconEditor,
                                    letbp.sensorFrame);
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
        return turnout.getBlockBoundaries();
    }

    public ArrayList<LayoutBlock> getProtectedBlocks(jmri.NamedBean bean) {
        return turnout.getProtectedBlocks(bean);
    }

    protected void removeSML(SignalMast signalMast) {
        turnout.removeSML(signalMast);
    }

    /**
     * Clean up when this object is no longer needed. Should not be called while
     * the object is still displayed; see {@link #remove()}
     */
    public void dispose() {
        if (popup != null) {
            popup.removeAll();
        }
        popup = null;
    }

    /**
     * Remove this object from display and persistance.
     */
    public void remove() {
        turnout.remove();
    }

    /**
     * "active" means that the object is still displayed, and should be stored.
     *
     * @return true if active
     */
    public boolean isActive() {
        return turnout.isActive();
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
     * Draw track decorations.
     * <p>
     * This type of track has none, so this method is empty.
     */
    @Override
    protected void drawDecorations(Graphics2D g2) {
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

        TurnoutType type = getTurnoutType();
        if (type == TurnoutType.DOUBLE_XOVER) {
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
        } else if ((type == TurnoutType.RH_XOVER)
                || (type == TurnoutType.LH_XOVER)) {    // draw (rh & lh) cross overs
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
                    if (getTurnoutType() == TurnoutType.RH_XOVER) {
                        if (isMain == mainlineA) {
                            g2.setColor(colorA);
                            g2.draw(new Line2D.Double(pAF, pM));
                        }
                        if (isMain == mainlineC) {
                            g2.setColor(colorC);
                            g2.draw(new Line2D.Double(pCF, pM));
                        }
                    } else if (getTurnoutType() == TurnoutType.LH_XOVER) {
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
                if (getTurnoutType() == TurnoutType.RH_XOVER) {
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
                } else if (getTurnoutType() == TurnoutType.LH_XOVER) {
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
                    if (getTurnoutType() == TurnoutType.RH_XOVER) {
                        if (isMain == mainlineA) {
                            g2.setColor(colorA);
                            g2.draw(new Line2D.Double(pAF, pM));
                        }
                        if (isMain == mainlineC) {
                            g2.setColor(colorC);
                            g2.draw(new Line2D.Double(pCF, pM));
                        }
                    } else if (getTurnoutType() == TurnoutType.LH_XOVER) {
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
            log.error("{}.draw1(...); slips should be being drawn by LayoutSlip sub-class", getName());
        } else {    // LH, RH, or WYE Turnouts
            // draw A<===>center
            if (isMain == mainlineA) {
                g2.setColor(colorA);
                g2.draw(new Line2D.Double(pA, pM));
            }

            if (state == UNKNOWN || (getContinuingSense() == state && state != INCONSISTENT)) { // unknown or continuing path
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

            if (state == UNKNOWN || (getContinuingSense() != state && state != INCONSISTENT)) { // unknown or diverting path
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
        TurnoutType type = getTurnoutType();

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
        if (type == TurnoutType.WYE_TURNOUT) {
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
                    if (getContinuingSense() == state) {  // unknown or diverting path
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
                    if (getContinuingSense() != state) {  // unknown or diverting path
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
                    if (getContinuingSense() == state) {  // straight path
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
                    if (getContinuingSense() != state) {  // unknown or diverting path
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
                    if (getContinuingSense() != state) {  // unknown or diverting path
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
                    if (getContinuingSense() != state) {  // unknown or diverting path
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
                log.error("{}.draw2(...); slips should be being drawn by LayoutSlip sub-class", getName());
                break;
            }
            default: {
                // this should never happen... but...
                log.error("{}.draw2(...); Unknown turnout type {}", getName(), type);
                break;
            }
        }
    }   // draw2

    /**
     * {@inheritDoc}
     */
    @Override
    protected void highlightUnconnected(Graphics2D g2, HitPointType specificType) {
        if (((specificType == HitPointType.NONE) || (specificType == HitPointType.TURNOUT_A))
                && (getConnectA() == null)) {
            g2.fill(trackControlCircleAt(getCoordsA()));
        }

        if (((specificType == HitPointType.NONE) || (specificType == HitPointType.TURNOUT_B))
                && (getConnectB() == null)) {
            g2.fill(trackControlCircleAt(getCoordsB()));
        }

        if (((specificType == HitPointType.NONE) || (specificType == HitPointType.TURNOUT_C))
                && (getConnectC() == null)) {
            g2.fill(trackControlCircleAt(getCoordsC()));
        }
        if (isTurnoutTypeXover()) {
            if (((specificType == HitPointType.NONE) || (specificType == HitPointType.TURNOUT_D))
                    && (getConnectD() == null)) {
                g2.fill(trackControlCircleAt(getCoordsD()));
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void drawTurnoutControls(Graphics2D g2) {
        if (!isDisabled() && !(isDisabledWhenOccupied() && isOccupied())) {
            Color foregroundColor = g2.getColor();

            if (getState() != Turnout.CLOSED) {
                // then switch to background (thrown) color
                g2.setColor(g2.getBackground());
            }

            if (layoutEditor.isTurnoutFillControlCircles()) {
                g2.fill(trackControlCircleAt(getCoordsCenter()));
            } else {
                g2.draw(trackControlCircleAt(getCoordsCenter()));
            }

            if (getState() != Turnout.CLOSED) {
                // then restore foreground color
                g2.setColor(foregroundColor);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
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
        g2.draw(layoutEditor.layoutEditorControlRectAt(pt));

        pt = getCoordsB();
        if (getConnectB() == null) {
            g2.setColor(Color.red);
        } else {
            g2.setColor(Color.green);
        }
        g2.draw(layoutEditor.layoutEditorControlRectAt(pt));

        pt = getCoordsC();
        if (getConnectC() == null) {
            g2.setColor(Color.red);
        } else {
            g2.setColor(Color.green);
        }
        g2.draw(layoutEditor.layoutEditorControlRectAt(pt));

        if (isTurnoutTypeXover() || isTurnoutTypeSlip()) {
            pt = getCoordsD();
            if (getConnectD() == null) {
                g2.setColor(Color.red);
            } else {
                g2.setColor(Color.green);
            }
            g2.draw(layoutEditor.layoutEditorControlRectAt(pt));
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

        return turnout.getConnectivityStateForLayoutBlocks(currLayoutBlock,
                prevLayoutBlock,
                nextLayoutBlock,
                suppress);
    }

    /**
     * {@inheritDoc}
     */
    // TODO: on the cross-overs, check the internal boundary details.
    @Override
    public void reCheckBlockBoundary() {

        turnout.reCheckBlockBoundary();

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<LayoutConnectivity> getLayoutConnectivity() {
        return turnout.getLayoutConnectivity();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @Nonnull
    List<HitPointType> checkForFreeConnections() {
        return turnout.checkForFreeConnections();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean checkForUnAssignedBlocks() {
        // because getLayoutBlock[BCD] will return block [A] if they're null
        // we only need to test block [A]
        return turnout.checkForUnAssignedBlocks();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void checkForNonContiguousBlocks(
            @Nonnull HashMap<String, List<Set<String>>> blockNamesToTrackNameSetsMap) {

        turnout.checkForNonContiguousBlocks(blockNamesToTrackNameSetsMap);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void collectContiguousTracksNamesInBlockNamed(
            @Nonnull String blockName,
            @Nonnull Set<String> TrackNameSet) {

        turnout.collectContiguousTracksNamesInBlockNamed(blockName, TrackNameSet);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAllLayoutBlocks(LayoutBlock layoutBlock) {
        turnout.setAllLayoutBlocks(layoutBlock);
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

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutTurnoutView.class);
}
