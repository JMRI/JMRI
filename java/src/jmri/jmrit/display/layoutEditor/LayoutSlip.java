package jmri.jmrit.display.layoutEditor;

import java.text.MessageFormat;
import java.util.*;
import java.util.Map.Entry;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.JComboBox;

import jmri.*;

/**
 * A LayoutSlip is a crossing of two straight tracks designed in such a way as
 * to allow trains to change from one straight track to the other, as well as
 * going straight across.
 * <p>
 * A LayoutSlip has four connection points, designated A, B, C, and D. A train
 * may proceed between A and D, A and C, B and D and in the case of
 * double-slips, B and C.
 * <br>
 * <pre>
 * \\      //
 *   A==-==D
 *    \\ //
 *      X
 *    // \\
 *   B==-==C
 *  //      \\
 * </pre>
 * <br>
 * For drawing purposes, each LayoutSlip carries a center point and
 * displacements for A and B. The displacements for C = - the displacement for
 * A, and the displacement for D = - the displacement for B. The center point
 * and these displacements may be adjusted by the user when in edit mode.
 * <p>
 * When LayoutSlips are first created, there are no connections. Block
 * information and connections are added when available.
 * <p>
 * SignalHead names are saved here to keep track of where signals are.
 * LayoutSlip only serves as a storage place for SignalHead names. The names are
 * placed here by Set Signals at Level Crossing in Tools menu.
 *
 * @author Dave Duchamp Copyright (c) 2004-2007
 * @author George Warner Copyright (c) 2017-2019
 */
abstract public class LayoutSlip extends LayoutTurnout {

    /**
     * Constructor method.
     * 
     * @param id slip ID.
     * @param models the layout editor.
     * @param type slip type, SINGLE_SLIP or DOUBLE_SLIP.
     */
    public LayoutSlip(String id, 
            LayoutEditor models, TurnoutType type) {
        super(id, models, type);

        turnoutStates.put(STATE_AC, new TurnoutState(Turnout.CLOSED, Turnout.CLOSED));
        turnoutStates.put(STATE_AD, new TurnoutState(Turnout.CLOSED, Turnout.THROWN));
        turnoutStates.put(STATE_BD, new TurnoutState(Turnout.THROWN, Turnout.THROWN));
        if (type == TurnoutType.SINGLE_SLIP) {
            turnoutStates.remove(STATE_BC);
        } else if (type == TurnoutType.DOUBLE_SLIP) {
            turnoutStates.put(STATE_BC, new TurnoutState(Turnout.THROWN, Turnout.CLOSED));
        } else {
            log.error("{}.setSlipType({}); invalid slip type", getName(), type); // I18IN
        }
    }

    public int currentState = UNKNOWN;

    private String turnoutBName = "";
    private NamedBeanHandle<Turnout> namedTurnoutB = null;

    private java.beans.PropertyChangeListener mTurnoutListener = null;

    // this should only be used for debugging...
    @Override
    public String toString() {
        return String.format("LayoutSlip %s (%s)", getId(), getSlipStateString(getSlipState()));
    }

    public TurnoutType getSlipType() {
        return type;
    }

    public int getSlipState() {
        return currentState;
    }

    public String getTurnoutBName() {
        if (namedTurnoutB != null) {
            return namedTurnoutB.getName();
        }
        return turnoutBName;
    }

    public Turnout getTurnoutB() {
        Turnout result = null;
        if (namedTurnoutB == null) {
            if (!turnoutBName.isEmpty()) {
                setTurnoutB(turnoutBName);
            }
        }
        if (namedTurnoutB != null) {
            result = namedTurnoutB.getBean();
        }
        return result;
    }

    public void setTurnoutB(@CheckForNull String tName) {
        boolean reactivate = false;
        if (namedTurnoutB != null) {
            deactivateTurnout();
            reactivate = (namedTurnout != null);
        }
        turnoutBName = tName;
        Turnout turnout = null;
        if (turnoutBName != null && !turnoutBName.isEmpty()) {
            turnout = InstanceManager.turnoutManagerInstance().getTurnout(turnoutBName);
        }
        if (turnout != null) {
            namedTurnoutB = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(turnoutBName, turnout);
            activateTurnout();
        } else {
            turnoutBName = "";
            namedTurnoutB = null;
        }
        if (reactivate) {
            // this has to be called even on a delete in order
            // to re-activate namedTurnout (A) (if necessary)
            activateTurnout();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LayoutTrack getConnection(HitPointType connectionType) {
        switch (connectionType) {
            case SLIP_A:
                return connectA;
            case SLIP_B:
                return connectB;
            case SLIP_C:
                return connectC;
            case SLIP_D:
                return connectD;
            default:
                String errString = MessageFormat.format("{0}.getConnection({1}); Invalid Connection Type",
                        getName(), connectionType); // I18IN
                log.error("will throw {}", errString);
                throw new IllegalArgumentException(errString);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setConnection(HitPointType connectionType, @CheckForNull LayoutTrack o, HitPointType type) throws jmri.JmriException {
        if ((type != HitPointType.TRACK) && (type != HitPointType.NONE)) {
            String errString = MessageFormat.format("{0}.setConnection({1}, {2}, {3}); Invalid type",
                    getName(), connectionType, (o == null) ? "null" : o.getName(), type); // I18IN
            log.error("will throw {}", errString);
            throw new jmri.JmriException(errString);
        }
        switch (connectionType) {
            case SLIP_A:
                connectA = o;
                break;
            case SLIP_B:
                connectB = o;
                break;
            case SLIP_C:
                connectC = o;
                break;
            case SLIP_D:
                connectD = o;
                break;
            default:
                String errString = MessageFormat.format("{0}.setConnection({1}, {2}, {3}); Invalid Connection Type",
                        getName(), connectionType, (o == null) ? "null" : o.getName(), type); // I18IN
                log.error("will throw {}", errString);
                throw new jmri.JmriException(errString);
        }
    }

    public String getDisplayName() {
        String name = "Slip " + getId();
        String tnA = getTurnoutName();
        String tnB = getTurnoutBName();
        if ((tnA != null) && !tnA.isEmpty()) {
            name += " (" + tnA;
        }
        if ((tnB != null) && !tnB.isEmpty()) {
            if (name.contains(" (")) {
                name += ", ";
            } else {
                name += "(";
            }
            name += tnB;
        }
        if (name.contains("(")) {
            name += ")";
        }
        return name;
    }

    String getSlipStateString(int slipState) { // package access for View forward
        String result = Bundle.getMessage("BeanStateUnknown");
        switch (slipState) {
            case STATE_AC: {
                result = "AC";
                break;
            }
            case STATE_BD: {
                result = "BD";
                break;
            }
            case STATE_AD: {
                result = "AD";
                break;
            }
            case STATE_BC: {
                result = "BC";
                break;
            }
            default: {
                break;
            }
        }
        return result;
    }

    /**
     * Toggle slip states if clicked on, physical turnout exists, and not
     * disabled.
     * @param selectedPointType the selected hit point type.
     */
    public void toggleState(HitPointType selectedPointType) {
        if (!disabled && !(disableWhenOccupied && isOccupied())) {
            int newSlipState = getSlipState();
            switch (selectedPointType) {
                case SLIP_LEFT: {
                    switch (newSlipState) {
                        case STATE_AC: {
                            if (type == TurnoutType.SINGLE_SLIP) {
                                newSlipState = STATE_BD;
                            } else {
                                newSlipState = STATE_BC;
                            }
                            break;
                        }
                        case STATE_AD: {
                            newSlipState = STATE_BD;
                            break;
                        }
                        case STATE_BC:
                        default: {
                            newSlipState = STATE_AC;
                            break;
                        }
                        case STATE_BD: {
                            newSlipState = STATE_AD;
                            break;
                        }
                    }
                    break;
                }
                case SLIP_RIGHT: {
                    switch (newSlipState) {
                        case STATE_AC: {
                            newSlipState = STATE_AD;
                            break;
                        }
                        case STATE_AD: {
                            newSlipState = STATE_AC;
                            break;
                        }
                        case STATE_BC:
                        default: {
                            newSlipState = STATE_BD;
                            break;
                        }
                        case STATE_BD: {
                            if (type == TurnoutType.SINGLE_SLIP) {
                                newSlipState = STATE_AC;
                            } else {
                                newSlipState = STATE_BC;
                            }
                            break;
                        }
                    }
                    break;
                }
                default:
                    jmri.util.LoggingUtil.warnOnce(log, "Unexpected selectedPointType = {}", selectedPointType);
                    break;
            }   // switch
            setSlipState(newSlipState);
        }
    }

    void setSlipState(int newSlipState) {
        if (disableWhenOccupied && isOccupied()) {
            log.debug("Turnout not changed as Block is Occupied");
        } else if (!disabled) {
            currentState = newSlipState;
            TurnoutState ts = turnoutStates.get(newSlipState);
            if (getTurnout() != null) {
                getTurnout().setCommandedState(ts.getTurnoutAState());
            }
            if (getTurnoutB() != null) {
                getTurnoutB().setCommandedState(ts.getTurnoutBState());
            }
        }
    }

    /**
     * is this turnout occupied?
     *
     * @return true if occupied
     */
    @Override
    boolean isOccupied() {
        Boolean result = false; // assume failure (pessimist!)
        switch (getSlipState()) {
            case STATE_AC: {
                result = ((getLayoutBlock().getOccupancy() == LayoutBlock.OCCUPIED)
                        || (getLayoutBlockC().getOccupancy() == LayoutBlock.OCCUPIED));
                break;
            }
            case STATE_AD: {
                result = ((getLayoutBlock().getOccupancy() == LayoutBlock.OCCUPIED)
                        || (getLayoutBlockD().getOccupancy() == LayoutBlock.OCCUPIED));
                break;
            }
            case STATE_BC: {
                result = ((getLayoutBlockB().getOccupancy() == LayoutBlock.OCCUPIED)
                        || (getLayoutBlockC().getOccupancy() == LayoutBlock.OCCUPIED));
                break;
            }
            case STATE_BD: {
                result = ((getLayoutBlockB().getOccupancy() == LayoutBlock.OCCUPIED)
                        || (getLayoutBlockD().getOccupancy() == LayoutBlock.OCCUPIED));
                break;
            }
            case UNKNOWN: {
                result = ((getLayoutBlock().getOccupancy() == LayoutBlock.OCCUPIED)
                        || (getLayoutBlockB().getOccupancy() == LayoutBlock.OCCUPIED)
                        || (getLayoutBlockC().getOccupancy() == LayoutBlock.OCCUPIED)
                        || (getLayoutBlockD().getOccupancy() == LayoutBlock.OCCUPIED));
                break;
            }
            default: {
                log.error("{}.isOccupied(); invalid slip state: {}", getName(), getSlipState());
                break;
            }
        }
        return result;
    }   // isOccupied()

    /**
     * Activate/Deactivate turnout to redraw when turnout state changes
     */
    void activateTurnout() {
        if (namedTurnout != null) {
            namedTurnout.getBean().addPropertyChangeListener(mTurnoutListener
                    = (java.beans.PropertyChangeEvent e) -> {
                        updateState();
                    }, namedTurnout.getName(), "Layout Editor Slip");
        }
        if (namedTurnoutB != null) {
            namedTurnoutB.getBean().addPropertyChangeListener(mTurnoutListener
                    = (java.beans.PropertyChangeEvent e) -> {
                        updateState();
                    }, namedTurnoutB.getName(), "Layout Editor Slip");
        }
        updateState();
    }

    void deactivateTurnout() {
        if (mTurnoutListener != null) {
            if (namedTurnout != null) {
                namedTurnout.getBean().removePropertyChangeListener(mTurnoutListener);
            }
            if (namedTurnoutB != null) {
                namedTurnoutB.getBean().removePropertyChangeListener(mTurnoutListener);
            }
            mTurnoutListener = null;
        }
    }


    @Override
    public void updateBlockInfo() {
        LayoutBlock b1 = null;
        LayoutBlock b2 = null;
        if (getLayoutBlock() != null) {
            getLayoutBlock().updatePaths();
        }
        if (connectA != null) {
            b1 = ((TrackSegment) connectA).getLayoutBlock();
            if ((b1 != null) && (b1 != getLayoutBlock())) {
                b1.updatePaths();
            }
        }
        if (connectC != null) {
            b2 = ((TrackSegment) connectC).getLayoutBlock();
            if ((b2 != null) && (b2 != getLayoutBlock()) && (b2 != b1)) {
                b2.updatePaths();
            }
        }

        if (connectB != null) {
            b1 = ((TrackSegment) connectB).getLayoutBlock();
            if ((b1 != null) && (b1 != getLayoutBlock())) {
                b1.updatePaths();
            }
        }
        if (connectD != null) {
            b2 = ((TrackSegment) connectD).getLayoutBlock();
            if ((b2 != null) && (b2 != getLayoutBlock()) && (b2 != b1)) {
                b2.updatePaths();
            }
        }
        reCheckBlockBoundary();
    }

    /**
     * Methods to test if mainline track or not Returns true if either
     * connecting track segment is mainline Defaults to not mainline if
     * connecting track segments are missing
     */
    @Override
    public boolean isMainline() {
        if (((connectA != null) && (((TrackSegment) connectA).isMainline()))
                || ((connectB != null) && (((TrackSegment) connectB).isMainline()))
                || ((connectC != null) && (((TrackSegment) connectC).isMainline()))
                || ((connectD != null) && (((TrackSegment) connectD).isMainline()))) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String[] getBlockBoundaries() {
        final String[] boundaryBetween = new String[4];

        if ((!getBlockName().isEmpty()) && (getLayoutBlock() != null)) {
            if ((connectA instanceof TrackSegment) && (((TrackSegment) connectA).getLayoutBlock() != getLayoutBlock())) {
                try {
                    boundaryBetween[0] = (((TrackSegment) connectA).getLayoutBlock().getDisplayName() + " - " + getLayoutBlock().getDisplayName());
                } catch (java.lang.NullPointerException e) {
                    // Can be considered normal if tracksegement hasn't yet been allocated a block
                    log.debug("TrackSegement at connection A doesn't contain a layout block");
                }
            }
            if ((connectC instanceof TrackSegment) && (((TrackSegment) connectC).getLayoutBlock() != getLayoutBlock())) {
                try {
                    boundaryBetween[2] = (((TrackSegment) connectC).getLayoutBlock().getDisplayName() + " - " + getLayoutBlock().getDisplayName());
                } catch (java.lang.NullPointerException e) {
                    // Can be considered normal if tracksegement hasn't yet been allocated a block
                    log.debug("TrackSegement at connection C doesn't contain a layout block");
                }
            }
            if ((connectB instanceof TrackSegment) && (((TrackSegment) connectB).getLayoutBlock() != getLayoutBlock())) {
                try {
                    boundaryBetween[1] = (((TrackSegment) connectB).getLayoutBlock().getDisplayName() + " - " + getLayoutBlock().getDisplayName());
                } catch (java.lang.NullPointerException e) {
                    // Can be considered normal if tracksegement hasn't yet been allocated a block
                    log.debug("TrackSegement at connection B doesn't contain a layout block");
                }
            }
            if ((connectD instanceof TrackSegment) && (((TrackSegment) connectD).getLayoutBlock() != getLayoutBlock())) {
                try {
                    boundaryBetween[3] = (((TrackSegment) connectD).getLayoutBlock().getDisplayName() + " - " + getLayoutBlock().getDisplayName());
                } catch (java.lang.NullPointerException e) {
                    // Can be considered normal if tracksegement hasn't yet been allocated a block
                    log.debug("TrackSegement at connection D doesn't contain a layout block");
                }
            }
        }
        return boundaryBetween;
    }

    /**
     * Removes this object from display and persistance
     */
    @Override
    public void remove() {
        disableSML(getSignalAMast());
        disableSML(getSignalBMast());
        disableSML(getSignalCMast());
        disableSML(getSignalDMast());
        removeSML(getSignalAMast());
        removeSML(getSignalBMast());
        removeSML(getSignalCMast());
        removeSML(getSignalDMast());
    }

    void disableSML(SignalMast signalMast) {
        if (signalMast == null) {
            return;
        }
        InstanceManager.getDefault(jmri.SignalMastLogicManager.class).disableLayoutEditorUse(signalMast);
    }

    HashMap<Integer, TurnoutState> turnoutStates = new LinkedHashMap<>(4);

    public HashMap<Integer, TurnoutState> getTurnoutStates() {
        return turnoutStates;
    }

    public int getTurnoutState(@Nonnull Turnout turn, int state) {
        if (turn == getTurnout()) {
            return getTurnoutState(state);
        }
        return getTurnoutBState(state);
    }

    public int getTurnoutState(int state) {
        return turnoutStates.get(state).getTurnoutAState();
    }

    public int getTurnoutBState(int state) {
        return turnoutStates.get(state).getTurnoutBState();
    }

    public void setTurnoutStates(int state, @Nonnull String turnStateA, @Nonnull String turnStateB) {
        if (!turnoutStates.containsKey(state)) {
            log.error("{}.setTurnoutStates({}, {}, {}); invalid state for slip",
                    getName(), state, turnStateA, turnStateB);
            return;
        }
        turnoutStates.get(state).setTurnoutAState(Integer.parseInt(turnStateA));
        turnoutStates.get(state).setTurnoutBState(Integer.parseInt(turnStateB));
    }

    // Internal call to update the state of the slip depending upon the turnout states.
    void updateState() {
        if ((getTurnout() != null) && (getTurnoutB() != null)) {
            int state_a = getTurnout().getKnownState();
            int state_b = getTurnoutB().getKnownState();
            for (Entry<Integer, TurnoutState> en : turnoutStates.entrySet()) {
                if (en.getValue().getTurnoutAState() == state_a) {
                    if (en.getValue().getTurnoutBState() == state_b) {
                        currentState = en.getKey();
                        models.redrawPanel();
                        return;
                    }
                }
            }
        }
    }

    /**
     * Check if either turnout is inconsistent. This is used to create an
     * alternate slip image.
     *
     * @return true if either turnout is inconsistent.
     */
    boolean isTurnoutInconsistent() {
        Turnout tA = getTurnout();
        if (tA != null && tA.getKnownState() == INCONSISTENT) {
            return true;
        }
        Turnout tB = getTurnoutB();
        if (tB != null && tB.getKnownState() == INCONSISTENT) {
            return true;
        }
        return false;
    }

    public static class TurnoutState {

        private int turnoutAstate = Turnout.CLOSED;
        private int turnoutBstate = Turnout.CLOSED;
        private JComboBox<String> turnoutABox;
        private JComboBox<String> turnoutBBox;

        TurnoutState(int turnoutAstate, int turnoutBstate) {
            this.turnoutAstate = turnoutAstate;
            this.turnoutBstate = turnoutBstate;
        }

        public int getTurnoutAState() {
            return turnoutAstate;
        }

        public int getTurnoutBState() {
            return turnoutBstate;
        }

        public void setTurnoutAState(int state) {
            turnoutAstate = state;
        }

        public void setTurnoutBState(int state) {
            turnoutBstate = state;
        }

        public JComboBox<String> getComboA() {
            if (turnoutABox == null) {
                String[] state = new String[]{InstanceManager.turnoutManagerInstance().getClosedText(),
                    InstanceManager.turnoutManagerInstance().getThrownText()};
                turnoutABox = new JComboBox<>(state);
                if (turnoutAstate == Turnout.THROWN) {
                    turnoutABox.setSelectedIndex(1);
                }
            }
            return turnoutABox;
        }

        public JComboBox<String> getComboB() {
            if (turnoutBBox == null) {
                String[] state = new String[]{InstanceManager.turnoutManagerInstance().getClosedText(),
                    InstanceManager.turnoutManagerInstance().getThrownText()};
                turnoutBBox = new JComboBox<>(state);
                if (turnoutBstate == Turnout.THROWN) {
                    turnoutBBox.setSelectedIndex(1);
                }
            }
            return turnoutBBox;
        }

        public int getTestTurnoutAState() {
            int result = Turnout.THROWN;
            if (turnoutABox != null) {
                if (turnoutABox.getSelectedIndex() == 0) {
                    result = Turnout.CLOSED;
                }
            }
            return result;
        }

        public int getTestTurnoutBState() {
            int result = Turnout.THROWN;
            if (turnoutBBox != null) {
                if (turnoutBBox.getSelectedIndex() == 0) {
                    result = Turnout.CLOSED;
                }
            }
            return result;
        }

        public void updateStatesFromCombo() {
            if ((turnoutABox != null) && (turnoutBBox != null)) {
                turnoutAstate = getTestTurnoutAState();
                turnoutBstate = getTestTurnoutBState();
            }
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (object == null) {
                return false;
            }
            if (!(object instanceof TurnoutState)) {
                return false;
            }
            TurnoutState tso = (TurnoutState) object;

            return ((getTurnoutAState() == tso.getTurnoutAState())
                    && (getTurnoutBState() == tso.getTurnoutBState()));
        }

        /**
         * Hash on the header
         */
        @Override
        public int hashCode() {
            int result = 7;
            result = (37 * result) + getTurnoutAState();
            result = (37 * result) + getTurnoutBState();

            return result;
        }

    }   // class TurnoutState

    /*
    this is used by ConnectivityUtil to determine the turnout state necessary to get from prevLayoutBlock ==> currLayoutBlock ==> nextLayoutBlock
     */
    @Override
    protected int getConnectivityStateForLayoutBlocks(
            @CheckForNull LayoutBlock thisLayoutBlock,
            @CheckForNull LayoutBlock prevLayoutBlock,
            @CheckForNull LayoutBlock nextLayoutBlock,
            boolean suppress) {
        int result = Turnout.UNKNOWN;
        LayoutBlock layoutBlockA = ((TrackSegment) getConnectA()).getLayoutBlock();
        LayoutBlock layoutBlockB = ((TrackSegment) getConnectB()).getLayoutBlock();
        LayoutBlock layoutBlockC = ((TrackSegment) getConnectC()).getLayoutBlock();
        LayoutBlock layoutBlockD = ((TrackSegment) getConnectD()).getLayoutBlock();

        if (layoutBlockA == thisLayoutBlock) {
            if (layoutBlockC == nextLayoutBlock || layoutBlockC == prevLayoutBlock) {
                result = LayoutSlip.STATE_AC;
            } else if (layoutBlockD == nextLayoutBlock || layoutBlockD == prevLayoutBlock) {
                result = LayoutSlip.STATE_AD;
            } else if (layoutBlockC == thisLayoutBlock) {
                result = LayoutSlip.STATE_AC;
            } else if (layoutBlockD == thisLayoutBlock) {
                result = LayoutSlip.STATE_AD;
            }
        } else if (layoutBlockB == thisLayoutBlock) {
            if (getTurnoutType() == TurnoutType.DOUBLE_SLIP) {
                if (layoutBlockD == nextLayoutBlock || layoutBlockD == prevLayoutBlock) {
                    result = LayoutSlip.STATE_BD;
                } else if (layoutBlockC == nextLayoutBlock || layoutBlockC == prevLayoutBlock) {
                    result = LayoutSlip.STATE_BC;
                } else if (layoutBlockD == thisLayoutBlock) {
                    result = LayoutSlip.STATE_BD;
                } else if (layoutBlockC == thisLayoutBlock) {
                    result = LayoutSlip.STATE_BC;
                }
            } else {
                if (layoutBlockD == nextLayoutBlock || layoutBlockD == prevLayoutBlock) {
                    result = LayoutSlip.STATE_BD;
                } else if (layoutBlockD == thisLayoutBlock) {
                    result = LayoutSlip.STATE_BD;
                }
            }
        } else if (layoutBlockC == thisLayoutBlock) {
            if (getTurnoutType() == TurnoutType.DOUBLE_SLIP) {
                if (layoutBlockA == nextLayoutBlock || layoutBlockA == prevLayoutBlock) {
                    result = LayoutSlip.STATE_AC;
                } else if (layoutBlockB == nextLayoutBlock || layoutBlockB == prevLayoutBlock) {
                    result = LayoutSlip.STATE_BC;
                } else if (layoutBlockA == thisLayoutBlock) {
                    result = LayoutSlip.STATE_AC;
                } else if (layoutBlockB == thisLayoutBlock) {
                    result = LayoutSlip.STATE_BC;
                }
            } else {
                if (layoutBlockA == nextLayoutBlock || layoutBlockA == prevLayoutBlock) {
                    result = LayoutSlip.STATE_AC;
                } else if (layoutBlockA == thisLayoutBlock) {
                    result = LayoutSlip.STATE_AC;
                }
            }
        } else if (layoutBlockD == thisLayoutBlock) {
            if (layoutBlockA == nextLayoutBlock || layoutBlockA == prevLayoutBlock) {
                result = LayoutSlip.STATE_AD;
            } else if (layoutBlockB == nextLayoutBlock || layoutBlockB == prevLayoutBlock) {
                result = LayoutSlip.STATE_BD;
            } else if (layoutBlockA == thisLayoutBlock) {
                result = LayoutSlip.STATE_AD;
            } else if (layoutBlockB == thisLayoutBlock) {
                result = LayoutSlip.STATE_AD;
            }
        } else {
            result = LayoutSlip.UNKNOWN;
        }
        if (!suppress && (result == LayoutSlip.UNKNOWN)) {
            log.error("{}.getConnectivityStateForLayoutBlocks(...); Cannot determine slip setting", getName());
        }
        return result;
    }   // getConnectivityStateForLayoutBlocks

    /*
    * {@inheritDoc}
     */
    @Override
    public void reCheckBlockBoundary() {
        if (connectA == null && connectB == null && connectC == null && connectD == null) {
            // This is no longer a block boundary, therefore will remove signal masts and sensors if present
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
            // May want to look at a method to remove the assigned mast from the panel and potentially any logics generated
        } else if (connectA == null || connectB == null || connectC == null || connectD == null) {
            // could still be in the process of rebuilding the point details
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
        if (connectC instanceof TrackSegment) {
            trkC = (TrackSegment) connectC;
            if (trkC.getLayoutBlock() == getLayoutBlock()) {
                if (signalCMastNamed != null) {
                    removeSML(getSignalCMast());
                }
                signalCMastNamed = null;
                sensorCNamed = null;
            }
        }
        if (connectB instanceof TrackSegment) {
            trkB = (TrackSegment) connectB;
            if (trkB.getLayoutBlock() == getLayoutBlock()) {
                if (signalBMastNamed != null) {
                    removeSML(getSignalBMast());
                }
                signalBMastNamed = null;
                sensorBNamed = null;
            }
        }

        if (connectD instanceof TrackSegment) {
            trkD = (TrackSegment) connectC;
            if (trkD.getLayoutBlock() == getLayoutBlock()) {
                if (signalDMastNamed != null) {
                    removeSML(getSignalDMast());
                }
                signalDMastNamed = null;
                sensorDNamed = null;
            }
        }
    }   // reCheckBlockBoundary()

    /*
    * {@inheritDoc}
     */
    @Override
    @Nonnull
    protected List<LayoutConnectivity> getLayoutConnectivity() {
        List<LayoutConnectivity> results = new ArrayList<>();

        log.trace("Start in LayoutSlip.getLayoutConnectivity for {}", getName());
        
        LayoutConnectivity lc = null;
        LayoutBlock lbA = getLayoutBlock(), lbB = getLayoutBlockB(), lbC = getLayoutBlockC(), lbD = getLayoutBlockD();
        
        log.trace("    type: {}", type);
        log.trace("     lbA: {}", lbA);
        log.trace("     lbB: {}", lbB);
        log.trace("     lbC: {}", lbC);
        log.trace("     lbD: {}", lbD);

        if (lbA != null) {
            if (lbA != lbC) {
                // have a AC block boundary, create a LayoutConnectivity
                log.debug("Block boundary  ('{}'<->'{}') found at {}", lbA, lbC, this);
                lc = new LayoutConnectivity(lbA, lbC);
                lc.setXoverBoundary(this, LayoutConnectivity.XOVER_BOUNDARY_AC);

                // The following line needs to change, because it uses location of 
                // the points on the SlipView itself. Switch to 
                // direction from connections
                //lc.setDirection(Path.computeDirection(getCoordsA(), getCoordsC()));
                lc.setDirection( models.computeDirectionAC(this) );
                
                log.trace("getLayoutConnectivity lbA != lbC");
                log.trace("  Block boundary  ('{}'<->'{}') found at {}", lbA, lbC, this);
                
                results.add(lc);
            }
            if (lbB != lbD) {
                // have a BD block boundary, create a LayoutConnectivity
                log.debug("Block boundary  ('{}'<->'{}') found at {}", lbB, lbD, this);
                lc = new LayoutConnectivity(lbB, lbD);
                lc.setXoverBoundary(this, LayoutConnectivity.XOVER_BOUNDARY_BD);

                // The following line needs to change, because it uses location of 
                // the points on the SlipView itself. Switch to 
                // direction from connections
                //lc.setDirection(Path.computeDirection(getCoordsB(), getCoordsD()));
                lc.setDirection( models.computeDirectionBD(this) );
                
                log.trace("getLayoutConnectivity lbA != lbC");
                log.trace("  Block boundary  ('{}'<->'{}') found at {}", lbB, lbD, this);
                
                results.add(lc);
            }
            if (lbA != lbD) {
                // have a AD block boundary, create a LayoutConnectivity
                log.debug("Block boundary  ('{}'<->'{}') found at {}", lbA, lbD, this);
                lc = new LayoutConnectivity(lbA, lbD);
                lc.setXoverBoundary(this, LayoutConnectivity.XOVER_BOUNDARY_AD);

                // The following line needs to change, because it uses location of 
                // the points on the SlipView itself. Switch to 
                // direction from connections
                //lc.setDirection(Path.computeDirection(getCoordsA(), getCoordsD()));
                lc.setDirection( models.computeDirectionAD(this) );
                
                log.trace("getLayoutConnectivity lbA != lbC");
                log.trace("  Block boundary  ('{}'<->'{}') found at {}", lbA, lbD, this);
                
                results.add(lc);
            }
            if ((type == TurnoutType.DOUBLE_SLIP) && (lbB != lbC)) {
                // have a BC block boundary, create a LayoutConnectivity
                log.debug("Block boundary  ('{}'<->'{}') found at {}", lbB, lbC, this);
                lc = new LayoutConnectivity(lbB, lbC);
                lc.setXoverBoundary(this, LayoutConnectivity.XOVER_BOUNDARY_BC);
                
                // The following line needs to change, because it uses location of 
                // the points on the SlipView itself. Switch to 
                // direction from connections
                //lc.setDirection(Path.computeDirection(getCoordsB(), getCoordsC()));
                lc.setDirection( models.computeDirectionBC(this) );
                
                log.trace("getLayoutConnectivity lbA != lbC");
                log.trace("  Block boundary  ('{}'<->'{}') found at {}", lbB, lbC, this);
                
                results.add(lc);
            }
        }
        return results;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<HitPointType> checkForFreeConnections() {
        List<HitPointType> result = new ArrayList<>();

        // check the A connection point
        if (getConnectA() == null) {
            result.add(HitPointType.SLIP_A);
        }

        // check the B connection point
        if (getConnectB() == null) {
            result.add(HitPointType.SLIP_B);
        }

        // check the C connection point
        if (getConnectC() == null) {
            result.add(HitPointType.SLIP_C);
        }

        // check the D connection point
        if (getConnectD() == null) {
            result.add(HitPointType.SLIP_D);
        }
        return result;
    }

    // NOTE: LayoutSlip uses the checkForNonContiguousBlocks
    //      and collectContiguousTracksNamesInBlockNamed methods
    //      inherited from LayoutTurnout
    //
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutSlip.class);
}
