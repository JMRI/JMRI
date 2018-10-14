package jmri.jmrit.display;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import jmri.InstanceManager;
import jmri.NamedBeanHandle;
import jmri.Turnout;
import jmri.jmrit.catalog.NamedIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An icon to display a status of a Slip, either Single or Double.<P>
 * This responds to only KnownState, leaving CommandedState to some other
 * graphic representation later.
 * <P>
 * A click on the icon will command a state change. Specifically, it will set
 * the CommandedState to the opposite (THROWN vs CLOSED) of the current
 * KnownState.
 * <P>
 * Note: lower west to lower east icon is used for storing the slip icon, in a
 * single slip, even if the slip is set for upper west to upper east.
 * <p>
 * With a 3-Way point we use the following translations
 * <ul>
 * <li>lower west to upper east - to upper exit
 * <li>upper west to lower east - to middle exit
 * <li>lower west to lower east - to lower exit
 * <li>west Turnout - First Turnout
 * <li>east Turnout - Second Turnout
 * <li>singleSlipRoute - translates to which exit the first turnout goes to
 * <li>true if upper, or false if lower
 * </ul>
 * <P>
 * Based upon the TurnoutIcon by Bob Jacobsen
 *
 * @author Kevin Dickerson Copyright (c) 2010
 */
public class SlipTurnoutIcon extends PositionableLabel implements java.beans.PropertyChangeListener {

    public SlipTurnoutIcon(Editor editor) {
        // super ctor call to make sure this is an icon label
        super(new NamedIcon("resources/icons/smallschematics/tracksegments/os-slip-lower-west-upper-east.gif",
                "resources/icons/smallschematics/tracksegments/os-slip-lower-west-upper-east.gif"), editor);
        _control = true;
        displayState(turnoutState());
        setPopupUtility(null);
    }

    // the associated Turnout object
    private NamedBeanHandle<Turnout> namedTurnoutWest = null;
    private NamedBeanHandle<Turnout> namedTurnoutWestLower = null;
    private NamedBeanHandle<Turnout> namedTurnoutEast = null;
    private NamedBeanHandle<Turnout> namedTurnoutEastLower = null;

    /**
     * Attached a named turnout to this display item
     *
     * @param pName Used as a system/user name to lookup the turnout object
     * @param turn  is used to determine which turnout position this is for.
     *              0x01 - West 0x02 - East 0x04 - Lower West 0x06 - Upper East
     */
    public void setTurnout(String pName, int turn) {
        if (InstanceManager.getNullableDefault(jmri.TurnoutManager.class) != null) {
            try {
                Turnout turnout = InstanceManager.turnoutManagerInstance().
                        provideTurnout(pName);
                setTurnout(jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(pName, turnout), turn);
            } catch (IllegalArgumentException e) {
                log.error("Turnout '" + pName + "' not available, icon won't see changes");
            }
        } else {
            log.error("No TurnoutManager for this protocol, icon won't see changes");
        }
    }

    /**
     * Attached a namedBean Handle turnout to this display item
     *
     * @param to   Used as the NamedBeanHandle to lookup the turnout object
     * @param turn is used to determine which turnout position this is for.
     * <ul>
     * <li>0x01 - West
     * <li>0x02 - East
     * <li>0x04 - Lower West
     * <li>0x06 - Upper East
     * </ul>
     */
    public void setTurnout(NamedBeanHandle<Turnout> to, int turn) {
        switch (turn) {
            case WEST:
                if (namedTurnoutWest != null) {
                    getTurnout(WEST).removePropertyChangeListener(this);
                }
                namedTurnoutWest = to;
                if (namedTurnoutWest != null) {
                    displayState(turnoutState());
                    getTurnout(WEST).addPropertyChangeListener(this, namedTurnoutWest.getName(), "Panel Editor Turnout");
                }
                break;
            case EAST:
                if (namedTurnoutEast != null) {
                    getTurnout(EAST).removePropertyChangeListener(this);
                }
                namedTurnoutEast = to;
                if (namedTurnoutEast != null) {
                    displayState(turnoutState());
                    getTurnout(EAST).addPropertyChangeListener(this, namedTurnoutEast.getName(), "Panel Editor Turnout");
                }
                break;
            case LOWERWEST:
                if (namedTurnoutWestLower != null) {
                    getTurnout(LOWERWEST).removePropertyChangeListener(this);
                }
                namedTurnoutWestLower = to;
                if (namedTurnoutWestLower != null) {
                    displayState(turnoutState());
                    getTurnout(LOWERWEST).addPropertyChangeListener(this, namedTurnoutWestLower.getName(), "Panel Editor Turnout");
                }
                break;
            case LOWEREAST:
                if (namedTurnoutEastLower != null) {
                    getTurnout(LOWEREAST).removePropertyChangeListener(this);
                }
                namedTurnoutEastLower = to;
                if (namedTurnoutEastLower != null) {
                    displayState(turnoutState());
                    getTurnout(LOWEREAST).addPropertyChangeListener(this, namedTurnoutEastLower.getName(), "Panel Editor Turnout");
                }
                break;
            default:
                log.error("turn value {} should not have appeared", turn);
        }
    }

    /**
     * Constant used to referred to the Turnout address configured to operate
     * the west (or first for a three way) of the Turnout.
     */
    public static final int WEST = 0x01;

    /**
     * Constant used to referred to the Turnout address configured to operate
     * the east (or second for a three way) of the Turnout.
     */
    public static final int EAST = 0x02;

    /**
     * Constant used for a scissor crossing using 4 turnout address, and refers
     * to the turnout located at the lower west.
     */
    public static final int LOWERWEST = 0x04;

    /**
     * Constant used for a scissor crossing using 4 turnout address, and refers
     * to the turnout located at the lower east.
     */
    public static final int LOWEREAST = 0x06;

    /**
     * Constant used to refer to a Double Slip Configuration.
     */
    public static final int DOUBLESLIP = 0x00;

    /**
     * Constant used to refer to a Single Slip Configuration.
     */
    public static final int SINGLESLIP = 0x02;

    /**
     * Constant used to refer to a Three Way Turnout Configuration.
     */
    public static final int THREEWAY = 0x04;

    /**
     * Constant used to refer to a Scissor (Double Crossover) Configuration.
     */
    public static final int SCISSOR = 0x08;

    //true for double slip, false for single.
    int turnoutType = DOUBLESLIP;

    /**
     * Sets the type of turnout configuration which is being used
     *
     * @param slip - valid values are
     * <ul>
     * <li>0x00 - Double Slip
     * <li>0x02 - Single Slip
     * <li>0x04 - Three Way Turnout
     * <li>0x08 - Scissor Crossing
     * </ul>
     */
    public void setTurnoutType(int slip) {
        turnoutType = slip;
    }

    public int getTurnoutType() {
        return turnoutType;
    }

    boolean singleSlipRoute = false;
    static boolean LOWERWESTtoLOWEREAST = false;
    static boolean UPPERWESTtoUPPEREAST = true;

    /**
     * Single Slip Route, determines if the slip route is from upper west to
     * upper east (true) or lower west to lower east (false) This also doubles
     * up for the three way and determines if the first turnout routes to the
     * upper (true) or lower (false) exit point.
     * <p>
     * In a Scissor crossing this returns true if only two turnout address are
     * required to set the crossing or false if four turnout address are
     * required
     *
     * @return true if route is through the turnout on a slip; false otherwise
     */
    public boolean getSingleSlipRoute() {
        return singleSlipRoute;
    }

    public void setSingleSlipRoute(boolean route) {
        singleSlipRoute = route;
    }

    /**
     * Returns the turnout located at the position specified.
     *
     * @param turn One of {@link #EAST}, {@link #WEST}, {@link #LOWEREAST}, or
     *             {@link #LOWERWEST}
     * @return the turnout at turn or null if turn is not a known constant or no
     *         turnout is at the position turn
     */
    public Turnout getTurnout(int turn) {
        switch (turn) {
            case EAST:
                return namedTurnoutEast.getBean();
            case WEST:
                return namedTurnoutWest.getBean();
            case LOWEREAST:
                return namedTurnoutEastLower.getBean();
            case LOWERWEST:
                return namedTurnoutWestLower.getBean();
            default:
                return null;
        }
    }

    /**
     * Returns the turnout located at the position specified.
     *
     * @param turn One of {@link #EAST}, {@link #WEST}, {@link #LOWEREAST}, or
     *             {@link #LOWERWEST}
     * @return the handle for the turnout at turn or null if turn is not a known
     *         constant or no turnout is at the position turn
     */
    public NamedBeanHandle<Turnout> getNamedTurnout(int turn) {
        switch (turn) {
            case EAST:
                return namedTurnoutEast;
            case WEST:
                return namedTurnoutWest;
            case LOWEREAST:
                return namedTurnoutEastLower;
            case LOWERWEST:
                return namedTurnoutWestLower;
            default:
                return null;
        }
    }

    /*
     Note: lower west to lower east icon is used for storing the slip icon, in a single slip,
     even if the slip is set for upper west to upper east.

     With a 3-Way point we use the following translations

     lower west to upper east - to upper exit
     upper west to lower east - to middle exit
     lower west to lower east - to lower exit

     With a Scissor Crossing we use the following to represent straight
     lower west to lower east
     upper west to upper east
     */
    // display icons
    String lowerWestToUpperEastLName = "resources/icons/smallschematics/tracksegments/os-slip-lower-west-upper-east.gif";
    NamedIcon lowerWestToUpperEast = new NamedIcon(lowerWestToUpperEastLName, lowerWestToUpperEastLName);
    String upperWestToLowerEastLName = "resources/icons/smallschematics/tracksegments/os-slip-upper-west-lower-east.gif";
    NamedIcon upperWestToLowerEast = new NamedIcon(upperWestToLowerEastLName, upperWestToLowerEastLName);
    String lowerWestToLowerEastLName = "resources/icons/smallschematics/tracksegments/os-slip-lower-west-lower-east.gif";
    NamedIcon lowerWestToLowerEast = new NamedIcon(lowerWestToLowerEastLName, lowerWestToLowerEastLName);
    String upperWestToUpperEastLName = "resources/icons/smallschematics/tracksegments/os-slip-upper-west-upper-east.gif";
    NamedIcon upperWestToUpperEast = new NamedIcon(upperWestToUpperEastLName, upperWestToUpperEastLName);
    String inconsistentLName = "resources/icons/smallschematics/tracksegments/os-slip-error-full.gif";
    NamedIcon inconsistent = new NamedIcon(inconsistentLName, inconsistentLName);
    String unknownLName = "resources/icons/smallschematics/tracksegments/os-slip-unknown-full.gif";
    NamedIcon unknown = new NamedIcon(unknownLName, unknownLName);

    public NamedIcon getLowerWestToUpperEastIcon() {
        return lowerWestToUpperEast;
    }

    public void setLowerWestToUpperEastIcon(NamedIcon i) {
        lowerWestToUpperEast = i;
        displayState(turnoutState());
    }

    public NamedIcon getUpperWestToLowerEastIcon() {
        return upperWestToLowerEast;
    }

    public void setUpperWestToLowerEastIcon(NamedIcon i) {
        upperWestToLowerEast = i;
        displayState(turnoutState());
    }

    public NamedIcon getLowerWestToLowerEastIcon() {
        return lowerWestToLowerEast;
    }

    public void setLowerWestToLowerEastIcon(NamedIcon i) {
        lowerWestToLowerEast = i;
        displayState(turnoutState());
        /*Only a double slip needs the fourth icon, we therefore set the upper west to upper east icon
         to be the same as the lower west to upper wast icon*/
        if (turnoutType != DOUBLESLIP) {
            setUpperWestToUpperEastIcon(i);
        }
    }

    public NamedIcon getUpperWestToUpperEastIcon() {
        return upperWestToUpperEast;
    }

    public void setUpperWestToUpperEastIcon(NamedIcon i) {
        upperWestToUpperEast = i;
        displayState(turnoutState());
    }

    public NamedIcon getInconsistentIcon() {
        return inconsistent;
    }

    public void setInconsistentIcon(NamedIcon i) {
        inconsistent = i;
        displayState(turnoutState());
    }

    public NamedIcon getUnknownIcon() {
        return unknown;
    }

    public void setUnknownIcon(NamedIcon i) {
        unknown = i;
        displayState(turnoutState());
    }

    @Override
    public int maxHeight() {
        return Math.max(
                Math.max((lowerWestToUpperEast != null) ? lowerWestToUpperEast.getIconHeight() : 0,
                        (upperWestToLowerEast != null) ? upperWestToLowerEast.getIconHeight() : 0),
                Math.max(
                        Math.max((upperWestToUpperEast != null) ? upperWestToUpperEast.getIconHeight() : 0,
                                (lowerWestToLowerEast != null) ? lowerWestToLowerEast.getIconHeight() : 0),
                        Math.max((unknown != null) ? unknown.getIconHeight() : 0,
                                (inconsistent != null) ? inconsistent.getIconHeight() : 0))
        );
    }

    @Override
    public int maxWidth() {
        return Math.max(
                Math.max((lowerWestToUpperEast != null) ? lowerWestToUpperEast.getIconWidth() : 0,
                        (upperWestToLowerEast != null) ? upperWestToLowerEast.getIconWidth() : 0),
                Math.max(
                        Math.max((upperWestToUpperEast != null) ? upperWestToUpperEast.getIconWidth() : 0,
                                (lowerWestToLowerEast != null) ? lowerWestToLowerEast.getIconWidth() : 0),
                        Math.max((unknown != null) ? unknown.getIconWidth() : 0,
                                (inconsistent != null) ? inconsistent.getIconWidth() : 0))
        );
    }

    /**
     * Get current state of attached turnouts This adds the two turnout states
     * together, however for the second turnout configured it will add 1 to the
     * Closed state and 3 to the Thrown state. This helps to indentify which
     * turnout is thrown and/or closed.
     * <p>
     * For a Scissor crossing that uses four turnouts, the code simply checks to
     * ensure that diagonally opposite turnouts are set the same. If not is will
     * return an Inconsistent state.
     * <p>
     * If any turnout that has either not been configured or in an Unknown or
     * Inconsistent state, the code will return the state UNKNOWN or
     * INCONSISTENT.
     *
     * @return A state variable from a Turnout, e.g. Turnout.CLOSED
     */
    int turnoutState() {
        //Need to rework this!
        //might be as simple as adding the two states together.
        //if either turnout is not entered then the state to report
        //back will be unknown
        int state;
        if (namedTurnoutWest != null) {
            if (getTurnout(WEST).getKnownState() == Turnout.UNKNOWN) {
                return Turnout.UNKNOWN;
            }
            if (getTurnout(WEST).getKnownState() == Turnout.INCONSISTENT) {
                return Turnout.INCONSISTENT;
            }
            state = +getTurnout(WEST).getKnownState();
        } else {
            return Turnout.UNKNOWN;
        }
        //We add 1 to the value of the west turnout to help identify the states for both turnouts
        if (namedTurnoutEast != null) {
            if (getTurnout(EAST).getKnownState() == Turnout.UNKNOWN) {
                return Turnout.UNKNOWN;
            }
            if (getTurnout(EAST).getKnownState() == Turnout.INCONSISTENT) {
                return Turnout.INCONSISTENT;
            }
            if (getTurnout(EAST).getKnownState() == Turnout.CLOSED) {
                state = state + (getTurnout(EAST).getKnownState() + 1);
            }
            if (getTurnout(EAST).getKnownState() == Turnout.THROWN) {
                state = state + (getTurnout(EAST).getKnownState() + 3);
            }
        } else {
            return Turnout.UNKNOWN;
        }
        if ((turnoutType == SCISSOR) && (!singleSlipRoute)) {
            //We simply need to check that the opposite turnout is set the same
            if (namedTurnoutEastLower != null) {
                if (getTurnout(LOWEREAST).getKnownState() == Turnout.UNKNOWN) {
                    return Turnout.UNKNOWN;
                }
                if (getTurnout(LOWEREAST).getKnownState() == Turnout.INCONSISTENT) {
                    return Turnout.INCONSISTENT;
                }
            } else {
                return Turnout.UNKNOWN;
            }
            if (namedTurnoutWestLower != null) {
                if (getTurnout(LOWERWEST).getKnownState() == Turnout.UNKNOWN) {
                    return Turnout.UNKNOWN;
                }
                if (getTurnout(LOWERWEST).getKnownState() == Turnout.INCONSISTENT) {
                    return Turnout.INCONSISTENT;
                }
            } else {
                return Turnout.UNKNOWN;
            }

            if (getTurnout(LOWEREAST).getKnownState() != getTurnout(WEST).getKnownState()) {
                return Turnout.INCONSISTENT;
            }
            if (getTurnout(LOWERWEST).getKnownState() != getTurnout(EAST).getKnownState()) {
                return Turnout.INCONSISTENT;
            }
        }

        return state;
    }

    // update icon as state of turnout changes
    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (log.isDebugEnabled()) {
            log.debug("property change: " + getNameString() + " " + e.getPropertyName() + " is now "
                    + e.getNewValue());
        }

        // when there's feedback, transition through inconsistent icon for better
        // animation
        if (getTristate()
                && (getTurnout(WEST).getFeedbackMode() != Turnout.DIRECT)
                && (e.getPropertyName().equals("CommandedState"))) {
            if ((getTurnout(WEST).getCommandedState() != getTurnout(WEST).getKnownState())
                    || (getTurnout(EAST).getCommandedState() != getTurnout(EAST).getKnownState())) {
                int now = Turnout.INCONSISTENT;
                displayState(now);
            }
            // this takes care of the quick double click
            if ((getTurnout(WEST).getCommandedState() == getTurnout(WEST).getKnownState())
                    || (getTurnout(EAST).getCommandedState() == getTurnout(EAST).getKnownState())) {
                displayState(turnoutState());
            }
        }

        if (e.getPropertyName().equals("KnownState")) {
            displayState(turnoutState());
        }
    }

    @Override
    public String getNameString() {
        String name;
        if (namedTurnoutWest == null) {
            name = Bundle.getMessage("NotConnected");
        } else {
            name = namedTurnoutWest.getName();
        }
        if (namedTurnoutEast != null) {
            name = name + " " + namedTurnoutEast.getName();
        }
        if ((getTurnoutType() == SCISSOR) && (!getSingleSlipRoute())) {
            if (namedTurnoutWestLower != null) {
                name = name + " " + namedTurnoutWestLower.getName();
            }
            if (namedTurnoutEastLower != null) {
                name = name + " " + namedTurnoutEastLower.getName();
            }
        }
        return name;
    }

    public void setTristate(boolean set) {
        tristate = set;
    }

    public boolean getTristate() {
        return tristate;
    }
    private boolean tristate = false;

    javax.swing.JCheckBoxMenuItem tristateItem = null;

    void addTristateEntry(JPopupMenu popup) {
        tristateItem = new javax.swing.JCheckBoxMenuItem(Bundle.getMessage("Tristate"));
        tristateItem.setSelected(getTristate());
        popup.add(tristateItem);
        tristateItem.addActionListener((java.awt.event.ActionEvent e) -> {
            setTristate(tristateItem.isSelected());
        });
    }

    /**
     * ****** popup AbstractAction.actionPerformed method overrides ********
     */
    @Override
    protected void rotateOrthogonal() {
        lowerWestToUpperEast.setRotation(lowerWestToUpperEast.getRotation() + 1, this);
        upperWestToLowerEast.setRotation(upperWestToLowerEast.getRotation() + 1, this);
        lowerWestToLowerEast.setRotation(lowerWestToLowerEast.getRotation() + 1, this);
        upperWestToUpperEast.setRotation(upperWestToUpperEast.getRotation() + 1, this);
        unknown.setRotation(unknown.getRotation() + 1, this);
        inconsistent.setRotation(inconsistent.getRotation() + 1, this);
        displayState(turnoutState());
        // bug fix, must repaint icons that have same width and height
        repaint();
    }

    @Override
    public void setScale(double s) {
        lowerWestToUpperEast.scale(s, this);
        upperWestToLowerEast.scale(s, this);
        lowerWestToLowerEast.scale(s, this);
        upperWestToUpperEast.scale(s, this);
        unknown.scale(s, this);
        inconsistent.scale(s, this);
        displayState(turnoutState());
    }

    @Override
    public void rotate(int deg) {
        lowerWestToUpperEast.rotate(deg, this);
        upperWestToLowerEast.rotate(deg, this);
        lowerWestToLowerEast.rotate(deg, this);
        upperWestToUpperEast.rotate(deg, this);
        unknown.rotate(deg, this);
        inconsistent.rotate(deg, this);
        displayState(turnoutState());
    }

    /**
     * Drive the current state of the display from the state of the turnout.
     * Here we have to alter the passed state to match the type of turnout we
     * are dealing with.
     *
     * @param state An integer value of the turnout states.
     */
    void displayState(int state) {
        //This needs to be worked on
        log.debug(getNameString() + " displayState " + state);
        updateSize();
        // we have to make some adjustments if we are using a single slip, three way point
        // or scissor arrangement to make sure that we get the correct representation.
        switch (getTurnoutType()) {
            case SINGLESLIP:
                if (singleSlipRoute && state == 9) {
                    state = 0;
                } else if ((!singleSlipRoute) && state == 7) {
                    state = 0;
                }
                break;
            case THREEWAY:
                if ((state == 7) || (state == 11)) {
                    if (singleSlipRoute) {
                        state = 11;
                    } else {
                        state = 9;
                    }
                } else if (state == 9) {
                    if (!singleSlipRoute) {
                        state = 11;
                    }
                }
                break;
            case SCISSOR:

                //State 11 should not be allowed for a scissor.
                switch (state) {
                    case 5:
                        state = 9;
                        break;
                    case 7:
                        state = 5;
                        break;
                    case 9:
                        state = 11;
                        break;
                    case 11:
                        state = 0;
                        break;
                    default:
                        log.warn("Unhandled scissors state: {}", state);
                        break;
                }
                break;
            case DOUBLESLIP:
                   // DOUBLESLIP is an allowed type, so it shouldn't
                   // cause a warning, even if we don't need special handling.
                break;
            default:
                log.warn("Unhandled turnout type: {}", getTurnoutType());
                break;
        }
        switch (state) {
            case Turnout.UNKNOWN:
                if (isText()) {
                    super.setText(Bundle.getMessage("BeanStateUnknown"));
                }
                if (isIcon()) {
                    super.setIcon(unknown);
                }
                break;
            case 5: //first closed, second closed
                if (isText()) {
                    super.setText(upperWestToLowerEastText);
                }
                if (isIcon()) {
                    super.setIcon(upperWestToLowerEast);
                }
                break;
            case 9: // first Closed, second Thrown
                if (isText()) {
                    super.setText(lowerWestToLowerEastText);
                }
                if (isIcon()) {
                    super.setIcon(lowerWestToLowerEast);
                }
                break;
            case 7: //first Thrown, second Closed
                if (isText()) {
                    super.setText(upperWestToUpperEastText);
                }
                if (isIcon()) {
                    super.setIcon(upperWestToUpperEast);
                }
                break;
            case 11: //first Thrown second Thrown
                if (isText()) {
                    super.setText(lowerWestToUpperEastText);
                }
                if (isIcon()) {
                    super.setIcon(lowerWestToUpperEast);
                }
                break;
            default:
                if (isText()) {
                    super.setText(Bundle.getMessage("BeanStateInconsistent"));
                }
                if (isIcon()) {
                    super.setIcon(inconsistent);
                }
                break;
        }
    }

    String lowerWestToUpperEastText = Bundle.getMessage("LowerWestToUpperEast");
    String upperWestToLowerEastText = Bundle.getMessage("UpperWestToLowerEast");
    String lowerWestToLowerEastText = Bundle.getMessage("LowerWestToLowerEast");
    String upperWestToUpperEastText = Bundle.getMessage("UpperWestToUpperEast");

    /**
     * Get the text used in the pop-up for setting the route from Lower West to
     * Upper East For a scissor crossing this the Left-hand crossing. For a 3
     * Way turnout this is the Upper Exit.
     *
     * @return localized description of route
     */
    public String getLWUEText() {
        return lowerWestToUpperEastText;
    }

    /**
     * Get the text used in the pop-up for setting the route from Upper West to
     * Lower East. For a scissor crossing this the Right-hand crossing. For a 3
     * Way turnout this is the Middle Exit.
     *
     * @return localized description of route
     */
    public String getUWLEText() {
        return upperWestToLowerEastText;
    }

    /**
     * Get the text used in the pop-up for setting the route from Lower West to
     * Lower East. For a scissor crossing this the Straight (Normal) Route. For
     * a 3 Way turnout this is the Lower Exit.
     *
     * @return localized description of route
     */
    public String getLWLEText() {
        return lowerWestToLowerEastText;
    }

    /**
     * Get the text used in the pop-up for setting the route from Upper West to
     * Upper East. For a scissor crossing this is not used. For a 3 Way turnout
     * this is not used.
     *
     * @return localized description of route
     */
    public String getUWUEText() {
        return upperWestToUpperEastText;
    }

    public void setLWUEText(String txt) {
        lowerWestToUpperEastText = txt;
    }

    public void setUWLEText(String txt) {
        upperWestToLowerEastText = txt;
    }

    public void setLWLEText(String txt) {
        lowerWestToLowerEastText = txt;
    }

    public void setUWUEText(String txt) {
        upperWestToUpperEastText = txt;
    }

    SlipIconAdder _iconEditor;

    @Override
    protected void edit() {
        if (_iconEditor == null) {
            _iconEditor = new SlipIconAdder();
        }
        makeIconEditorFrame(this, "SlipTOEditor", true, _iconEditor);
        _iconEditor.setPickList(jmri.jmrit.picker.PickListModel.turnoutPickModelInstance());
        _iconEditor.setTurnoutType(getTurnoutType());
        switch (getTurnoutType()) {
            case DOUBLESLIP:
                _iconEditor.setIcon(3, "LowerWestToUpperEast", getLowerWestToUpperEastIcon());
                _iconEditor.setIcon(2, "UpperWestToLowerEast", getUpperWestToLowerEastIcon());
                _iconEditor.setIcon(4, "LowerWestToLowerEast", getLowerWestToLowerEastIcon());
                _iconEditor.setIcon(5, "UpperWestToUpperEast", getUpperWestToUpperEastIcon());
                break;
            case SINGLESLIP:
                _iconEditor.setSingleSlipRoute(getSingleSlipRoute());
                _iconEditor.setIcon(3, "LowerWestToUpperEast", getLowerWestToUpperEastIcon());
                _iconEditor.setIcon(2, "UpperWestToLowerEast", getUpperWestToLowerEastIcon());
                _iconEditor.setIcon(4, "Slip", getLowerWestToLowerEastIcon());

                break;
            case THREEWAY:
                _iconEditor.setSingleSlipRoute(getSingleSlipRoute());
                _iconEditor.setIcon(3, "Upper", getLowerWestToUpperEastIcon());
                _iconEditor.setIcon(2, "Middle", getUpperWestToLowerEastIcon());
                _iconEditor.setIcon(4, "Lower", getLowerWestToLowerEastIcon());
                break;
            case SCISSOR:
                _iconEditor.setSingleSlipRoute(getSingleSlipRoute());
                _iconEditor.setIcon(3, "LowerWestToUpperEast", getLowerWestToUpperEastIcon());
                _iconEditor.setIcon(2, "UpperWestToLowerEast", getUpperWestToLowerEastIcon());
                _iconEditor.setIcon(4, "LowerWestToLowerEast", getLowerWestToLowerEastIcon());
                if (!getSingleSlipRoute()) {
                    _iconEditor.setTurnout("lowerwest", namedTurnoutWestLower);
                    _iconEditor.setTurnout("lowereast", namedTurnoutEastLower);
                }
                break;
            default:
                log.error("getTurnoutType() value {} should not have appeared", getTurnoutType());
        }
        _iconEditor.setIcon(0, "BeanStateInconsistent", getInconsistentIcon());
        _iconEditor.setIcon(1, "BeanStateUnknown", getUnknownIcon());
        _iconEditor.setTurnout("west", namedTurnoutWest);
        _iconEditor.setTurnout("east", namedTurnoutEast);

        _iconEditor.makeIconPanel(true);

        ActionListener addIconAction = (ActionEvent a) -> {
            updateTurnout();
        };
        _iconEditor.complete(addIconAction, true, true, true);
    }

    void updateTurnout() {
        setTurnoutType(_iconEditor.getTurnoutType());
        switch (_iconEditor.getTurnoutType()) {
            case DOUBLESLIP:
                setLowerWestToUpperEastIcon(_iconEditor.getIcon("LowerWestToUpperEast"));
                setUpperWestToLowerEastIcon(_iconEditor.getIcon("UpperWestToLowerEast"));
                setLowerWestToLowerEastIcon(_iconEditor.getIcon("LowerWestToLowerEast"));
                setUpperWestToUpperEastIcon(_iconEditor.getIcon("UpperWestToUpperEast"));
                break;
            case SINGLESLIP:
                setLowerWestToUpperEastIcon(_iconEditor.getIcon("LowerWestToUpperEast"));
                setUpperWestToLowerEastIcon(_iconEditor.getIcon("UpperWestToLowerEast"));
                setSingleSlipRoute(_iconEditor.getSingleSlipRoute());
                setLowerWestToLowerEastIcon(_iconEditor.getIcon("Slip"));
                break;
            case THREEWAY:
                setSingleSlipRoute(_iconEditor.getSingleSlipRoute());
                setLowerWestToUpperEastIcon(_iconEditor.getIcon("Upper"));
                setUpperWestToLowerEastIcon(_iconEditor.getIcon("Middle"));
                setLowerWestToLowerEastIcon(_iconEditor.getIcon("Lower"));
                break;
            case SCISSOR:
                setLowerWestToUpperEastIcon(_iconEditor.getIcon("LowerWestToUpperEast"));
                setUpperWestToLowerEastIcon(_iconEditor.getIcon("UpperWestToLowerEast"));
                setLowerWestToLowerEastIcon(_iconEditor.getIcon("LowerWestToLowerEast"));
                setSingleSlipRoute(_iconEditor.getSingleSlipRoute());
                if (!getSingleSlipRoute()) {
                    setTurnout(_iconEditor.getTurnout("lowerwest"), LOWERWEST);
                    setTurnout(_iconEditor.getTurnout("lowereast"), LOWEREAST);
                }
                break;
            default:
                log.error("_iconEditor.getTurnoutType() value {} should not have appeared", _iconEditor.getTurnoutType());
        }
        setInconsistentIcon(_iconEditor.getIcon("BeanStateInconsistent"));
        setUnknownIcon(_iconEditor.getIcon("BeanStateUnknown"));
        setTurnout(_iconEditor.getTurnout("west"), WEST);
        setTurnout(_iconEditor.getTurnout("east"), EAST);
        _iconEditorFrame.dispose();
        _iconEditorFrame = null;
        _iconEditor = null;
        invalidate();
    }

    /**
     * Throw the turnout when the icon is clicked.
     *
     * @param e the click event
     */
    @Override
    public void doMouseClicked(java.awt.event.MouseEvent e) {
        if (!_editor.getFlag(Editor.OPTION_CONTROLS, isControlling())) {
            return;
        }
        if (e.isMetaDown() || e.isAltDown()) {
            return;
        }
        if ((namedTurnoutWest == null) || (namedTurnoutEast == null)) {
            log.error("No turnout connection, can't process click");
            return;
        }
        switch (turnoutType) {
            case DOUBLESLIP:
                doDoubleSlipMouseClick();
                break;
            case SINGLESLIP:
                doSingleSlipMouseClick();
                break;
            case THREEWAY:
                do3WayMouseClick();
                break;
            case SCISSOR:
                doScissorMouseClick();
                break;
            default:
                log.error("turnoutType value {} should not have appeared", turnoutType);
        }

    }

    /**
     * Throw the turnouts for a double slip when the icon is clicked
     */
    private void doDoubleSlipMouseClick() {
        switch (turnoutState()) {
            case 5:
                setUpperWestToUpperEast();
                break;
            case 7:
                setLowerWestToUpperEast();
                break;
            case 9:
                setUpperWestToLowerEast();
                break;
            case 11:
                setLowerWestToLowerEast();
                break;
            default:
                setUpperWestToLowerEast();
        }
    }

    /**
     * Throw the turnouts for a single slip when the icon is clicked
     */
    private void doSingleSlipMouseClick() {
        switch (turnoutState()) {
            case 5:
                if (singleSlipRoute) {
                    setLowerWestToUpperEast();
                } else {
                    setLowerWestToLowerEast();
                }
                break;
            case 7:
                if (singleSlipRoute) {
                    setUpperWestToLowerEast();
                } else {
                    setLowerWestToUpperEast();
                }
                break;
            case 9:
                if (singleSlipRoute) {
                    setUpperWestToLowerEast();
                } else {
                    setLowerWestToUpperEast();
                }
                break;
            case 11:
                if (singleSlipRoute) {
                    setUpperWestToUpperEast();
                } else {
                    setUpperWestToLowerEast();
                }
                break;
            default:
                setUpperWestToLowerEast();
        }
    }

    /**
     * Throw the turnouts for a 3 way Turnout when the icon is clicked
     */
    private void do3WayMouseClick() {
        switch (turnoutState()) {
            case 5:
                if (singleSlipRoute) {
                    setLowerWestToLowerEast();
                } else {
                    setUpperWestToUpperEast();
                }
                break;
            case 7:
                if (singleSlipRoute) {
                    setLowerWestToUpperEast();
                } else {
                    setLowerWestToLowerEast();
                }
                break;
            case 9:
                if (singleSlipRoute) {
                    setLowerWestToUpperEast();
                } else {
                    setUpperWestToLowerEast();
                }
                break;
            case 11:
                if (singleSlipRoute) {
                    setUpperWestToLowerEast();
                } else {
                    setLowerWestToLowerEast();
                }
                break;
            default:
                setLowerWestToUpperEast();
        }
    }

    /**
     * Throw the turnouts for a scissor crossing when the icon is clicked
     */
    boolean firstStraight = false;

    private void doScissorMouseClick() {
        if (turnoutState() == 5) {
            if (firstStraight) {
                setUpperWestToLowerEast();
                firstStraight = false;
            } else {
                setLowerWestToUpperEast();
                firstStraight = true;
            }
        } else {
            setLowerWestToLowerEast();
        }
    }

    HashMap<Turnout, Integer> _turnoutSetting = new HashMap<>();

    protected HashMap<Turnout, Integer> getTurnoutSettings() {
        return _turnoutSetting;
    }

    protected void reset() {
        _turnoutSetting = new HashMap<>();
    }

    /**
     * Set the turnouts appropriate for Upper West to Lower East line in a Slip
     * which is the equivalent a of right hand crossing in a scissors. With a
     * three way turnout, this is also the middle route.
     */
    private void setUpperWestToLowerEast() {
        reset();
        if (getTurnoutType() == SCISSOR) {
            _turnoutSetting.put(getTurnout(WEST), jmri.Turnout.THROWN);
            _turnoutSetting.put(getTurnout(EAST), jmri.Turnout.CLOSED);
            if (!singleSlipRoute) {
                _turnoutSetting.put(namedTurnoutWestLower.getBean(), jmri.Turnout.CLOSED);
                _turnoutSetting.put(namedTurnoutEastLower.getBean(), jmri.Turnout.THROWN);
            }
        } else {
            _turnoutSetting.put(getTurnout(WEST), jmri.Turnout.CLOSED);
            _turnoutSetting.put(getTurnout(EAST), jmri.Turnout.CLOSED);
        }
        setSlip();
    }

    /**
     * Set the turns appropriate for Lower West to Upper East line in a Slip
     * which is the equivalent of the left hand crossing in a scissors. With a
     * three way turnout, this is also the upper route.
     */
    private void setLowerWestToUpperEast() {
        reset();
        if (getTurnoutType() == SCISSOR) {
            _turnoutSetting.put(getTurnout(EAST), jmri.Turnout.THROWN);
            _turnoutSetting.put(getTurnout(WEST), jmri.Turnout.CLOSED);
            if (!singleSlipRoute) {
                _turnoutSetting.put(namedTurnoutWestLower.getBean(), jmri.Turnout.THROWN);
                _turnoutSetting.put(namedTurnoutEastLower.getBean(), jmri.Turnout.CLOSED);
            }
        } else {
            _turnoutSetting.put(getTurnout(EAST), jmri.Turnout.THROWN);
            _turnoutSetting.put(getTurnout(WEST), jmri.Turnout.THROWN);
        }
        setSlip();
    }

    /**
     * Set the turnouts appropriate for Upper West to Upper East line in a Slip
     * which is the equivalent of the straight (normal route) in a scissors.
     * With a three way turnout, this is not used.
     */
    private void setUpperWestToUpperEast() {
        reset();
        if (getTurnoutType() == SCISSOR) {
            _turnoutSetting.put(getTurnout(WEST), jmri.Turnout.CLOSED);
            _turnoutSetting.put(getTurnout(EAST), jmri.Turnout.CLOSED);
            if (!singleSlipRoute) {
                _turnoutSetting.put(namedTurnoutWestLower.getBean(), jmri.Turnout.CLOSED);
                _turnoutSetting.put(namedTurnoutEastLower.getBean(), jmri.Turnout.CLOSED);
            }
        } else {
            _turnoutSetting.put(getTurnout(WEST), jmri.Turnout.THROWN);
            _turnoutSetting.put(getTurnout(EAST), jmri.Turnout.CLOSED);
        }
        setSlip();
    }

    /**
     * Set the turnouts appropriate for Lower West to Lower East line in a Slip
     * which is the equivalent of the straight (normal route) in a scissors.
     * With a three way turnout, this is the lower route.
     */
    private void setLowerWestToLowerEast() {
        reset();
        if (getTurnoutType() == SCISSOR) {
            _turnoutSetting.put(getTurnout(WEST), jmri.Turnout.CLOSED);
            _turnoutSetting.put(getTurnout(EAST), jmri.Turnout.CLOSED);
            if (!singleSlipRoute) {
                _turnoutSetting.put(namedTurnoutWestLower.getBean(), jmri.Turnout.CLOSED);
                _turnoutSetting.put(namedTurnoutEastLower.getBean(), jmri.Turnout.CLOSED);
            }
        } else {
            _turnoutSetting.put(getTurnout(WEST), jmri.Turnout.CLOSED);
            _turnoutSetting.put(getTurnout(EAST), jmri.Turnout.THROWN);
        }
        setSlip();
    }

    /**
     * Displays a popup menu to select a given state, rather than cycling
     * through each state.
     *
     * @param popup the menu to add the state menu to
     * @return true if anything added to menu
     */
    @Override
    public boolean showPopUp(JPopupMenu popup) {
        if (isEditable()) {
            // add tristate option if turnout has feedback
            boolean returnstate = false;
            if (namedTurnoutWest != null && getTurnout(WEST).getFeedbackMode() != Turnout.DIRECT) {
                addTristateEntry(popup);
                returnstate = true;
            }
            if (namedTurnoutEast != null && getTurnout(EAST).getFeedbackMode() != Turnout.DIRECT) {
                addTristateEntry(popup);
                returnstate = true;
            }
            return returnstate;
        } else {
            JMenuItem LWUE = new JMenuItem(lowerWestToUpperEastText);
            if ((turnoutType == THREEWAY) && (!singleSlipRoute)) {
                LWUE.addActionListener((ActionEvent e) -> {
                    setLowerWestToLowerEast();
                });

            } else {
                LWUE.addActionListener((ActionEvent e) -> {
                    setLowerWestToUpperEast();
                });
            }
            popup.add(LWUE);
            JMenuItem UWLE = new JMenuItem(upperWestToLowerEastText);
            UWLE.addActionListener((ActionEvent e) -> {
                setUpperWestToLowerEast();
            });
            popup.add(UWLE);
            if ((turnoutType == DOUBLESLIP) || ((turnoutType == SINGLESLIP) && (!singleSlipRoute))) {
                JMenuItem LWLE = new JMenuItem(lowerWestToLowerEastText);
                LWLE.addActionListener((ActionEvent e) -> {
                    setLowerWestToLowerEast();
                });
                popup.add(LWLE);
            }
            if ((turnoutType == DOUBLESLIP) || ((turnoutType == SINGLESLIP) && (singleSlipRoute))) {
                JMenuItem UWUE = new JMenuItem(upperWestToUpperEastText);
                UWUE.addActionListener((ActionEvent e) -> {
                    setUpperWestToUpperEast();
                });
                popup.add(UWUE);
            }
            if (turnoutType == THREEWAY) {
                JMenuItem LWLE = new JMenuItem(lowerWestToLowerEastText);
                if (!singleSlipRoute) {
                    LWLE.addActionListener((ActionEvent e) -> {
                        setLowerWestToUpperEast();
                    });
                } else {
                    LWLE.addActionListener((ActionEvent e) -> {
                        setLowerWestToLowerEast();
                    });
                }
                popup.add(LWLE);
            }
            if (turnoutType == SCISSOR) {
                JMenuItem LWLE = new JMenuItem(lowerWestToLowerEastText);
                LWLE.addActionListener((ActionEvent e) -> {
                    setLowerWestToLowerEast();
                });
                popup.add(LWLE);
            }
        }
        return true;
    }

    // overide
    @Override
    public boolean setTextEditMenu(JPopupMenu popup) {
        String popuptext = Bundle.getMessage("SetSlipText");
        if (turnoutType == THREEWAY) {
            popuptext = Bundle.getMessage("Set3WayText");
        } else if (turnoutType == SCISSOR) {
            popuptext = Bundle.getMessage("SetScissorText");
        }
        popup.add(new AbstractAction(popuptext) {
            @Override
            public void actionPerformed(ActionEvent e) {
                String name = getNameString();
                slipTurnoutTextEdit(name);
            }
        });
        return true;
    }

    public void slipTurnoutTextEdit(String name) {
        log.debug("make text edit menu");

        SlipTurnoutTextEdit f = new SlipTurnoutTextEdit();
        f.addHelpMenu("package.jmri.jmrit.display.SlipTurnoutTextEdit", true);
        try {
            f.initComponents(this, name);
        } catch (Exception ex) {
            log.error("Exception: " + ex.toString());
        }
        f.setVisible(true);
    }

    @Override
    public void dispose() {
        if (namedTurnoutWest != null) {
            getTurnout(WEST).removePropertyChangeListener(this);
        }
        namedTurnoutWest = null;
        if (namedTurnoutEast != null) {
            getTurnout(EAST).removePropertyChangeListener(this);
        }
        namedTurnoutEast = null;
        if (namedTurnoutWestLower != null) {
            getTurnout(WEST).removePropertyChangeListener(this);
        }
        namedTurnoutWestLower = null;
        if (namedTurnoutEastLower != null) {
            getTurnout(EAST).removePropertyChangeListener(this);
        }
        namedTurnoutEastLower = null;
        lowerWestToUpperEast = null;
        upperWestToLowerEast = null;
        lowerWestToLowerEast = null;
        upperWestToUpperEast = null;
        inconsistent = null;
        unknown = null;

        super.dispose();
    }

    boolean busy = false;

    /**
     * Set Slip busy when commands are being issued to Slip turnouts
     */
    protected void setSlipBusy() {
        busy = true;
    }

    /**
     * Set Slip not busy when all commands have been issued to Slip turnouts
     */
    protected void setSlipNotBusy() {
        busy = false;
    }

    /**
     * Check if Slip is busy.
     *
     * @return true if commands are being issued to Slips turnouts
     */
    protected boolean isSlipBusy() {
        return (busy);
    }

    /**
     * Set the Slip. Sets the slips Turnouts to the state required. This call is
     * ignored if the slip is 'busy', i.e., if there is a thread currently
     * sending commands to this Slips's turnouts.
     */
    private void setSlip() {
        if (!busy) {
            setSlipBusy();
            SetSlipThread thread = new SetSlipThread(this);
            thread.start();
        }
    }
    private final static Logger log = LoggerFactory.getLogger(SlipTurnoutIcon.class);

    static class SetSlipThread extends Thread {

        /**
         * Constructs the thread.
         *
         * @param aSlip the slip icon to manipulate in the thread
         */
        public SetSlipThread(SlipTurnoutIcon aSlip) {
            s = aSlip;
        }

        //This is used to set the two turnouts, with a delay of 250ms between each one.
        @Override
        public void run() {

            HashMap<Turnout, Integer> _turnoutSetting = s.getTurnoutSettings();

            _turnoutSetting.forEach((turnout, state) -> {
                jmri.util.ThreadingUtil.runOnLayout(() -> { // run on layout thread
                    turnout.setCommandedState(state);
                });
                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // retain if needed later
                }
            });

            //set Slip not busy
            s.setSlipNotBusy();
        }

        private final SlipTurnoutIcon s;

    }
}
