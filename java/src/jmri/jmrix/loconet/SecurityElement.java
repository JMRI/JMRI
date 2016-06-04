// SecurityElement.java
package jmri.jmrix.loconet;

import jmri.Sensor;
import jmri.Turnout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test implementation of SecurityElement, ala 1/8 of an SE8.
 * <P>
 * This is implemented in terms of basic LocoNet, rather than using references
 * to Sensor and Turnout objects, to force it to be specific about how messages
 * are handled.
 * <P>
 * Messages listened to:
 * <UL>
 * <LI>Turnout commands, feedback
 * <LI>Sensor status
 * <LI>SE status
 * </UL>
 * <P>
 * Note that the SE message turnout bit is set for CLOSED or no turnout
 * associated. The configured turnout number is 1-N
 * <P>
 * Internally, our "Direction" variables encode the direction of travel within
 * this SE. Note that this combination results in an extra complement; if
 * looking at a remote SE's A leg and it is reserved in the AX direction, that
 * it NOT coming toward us.
 * <P>
 * SE messages include addresses coded as 0-4095, not 1-4096.
 * <P>
 * The algorithms in this class are a collaborative effort of Digitrax, Inc and
 * Bob Jacobsen.
 * <P>
 * Some of the message formats used in this class are Copyright Digitrax, Inc.
 * and used with permission as part of the JMRI project. That permission does
 * not extend to uses in other software products. If you wish to use this code,
 * algorithm or these message formats outside of JMRI, please contact Digitrax
 * Inc for separate permission.
 * <P>
 *
 * @author	Bob Jacobsen Copyright (C) 2002
 * @version $Revision$
 * @deprecated 2.13.5, Does not work with the multi-connection correctly,
 * believe not to work correctly before hand and that the feature is not used - left to allow old files to be read
 */
@Deprecated
public class SecurityElement implements LocoNetListener {

    // constants ===============================================================
    // direction codes
    public static final int NONE = 0;  // unknown or undecided
    public static final int AX = 8;    // enter from A, leave from B or C
    public static final int XA = 16;   // enter from B or C, leave from A

    // leg names
    public static final int A = 1;
    public static final int B = 2;
    public static final int C = 4;
    // also NONE for no connection

    // configuration information ===============================================
    public boolean calculates = true; // if false, no updates

    public int mNumber;     // own SE number

    public int onAXReservation;  // for reservation from A leg attachment
    public int onXAReservation;  // for reservation from B/C leg attachment
    // NONE is coded as zero
    public static final int STOPOPPOSITE = 1;
    public static final int STOPUNRESERVED = 2;

    public boolean makeAReservation; // make reservation when entered from A leg
    public boolean makeBReservation; // make reservation when entered from B leg
    public boolean makeCReservation; // make reservation when entered from C leg

    public int attachAnum;  // SE number that A is attached to
    public int attachAleg;  // leg of SE attachAnum that A is attached to

    public int attachBnum;  // SE that B is attached to
    public int attachBleg;  // leg of SE attachBnum that B is attached to

    public int attachCnum;  // SE that C is attached to
    public int attachCleg;  // leg of SE attachCnum that C is attached to

    public int dsSensor;    // associated occupancy sensor number;
    public int turnout;     // associated turnout number
    public int auxInput;    // associated SE

    public int maxSpeedAC = 70;  // speed limits set by track
    public int maxSpeedCA = 70;  // geometry; these are maxima
    public int maxSpeedAB = 70;
    public int maxSpeedBA = 70;

    public int maxBrakingAC = 20; // how much a train can brake while transiting
    public int maxBrakingCA = 20; // this section.
    public int maxBrakingAB = 20;
    public int maxBrakingBA = 20;

    // state information =======================================================
    // previous state information - inputs before processing present event
    int currentDsStateHere = Sensor.UNKNOWN;
    int currentTurnoutStateHere = Turnout.UNKNOWN;

    int currentSpeedLimitFromA = 0;   // speed limit on the SE leg attached to A
    int currentDsStateOnA = Sensor.UNKNOWN;
    int currentTurnoutStateOnA = Turnout.UNKNOWN;
    boolean currentReservedFromA = false;

    int currentSpeedLimitFromB = 0;   // speed limit on the SE leg attached to B
    int currentDsStateOnB = Sensor.UNKNOWN;
    int currentTurnoutStateOnB = Turnout.UNKNOWN;
    boolean currentReservedFromB = false;

    int currentSpeedLimitFromC = 0;   // speed limit on the SE leg attached to C
    int currentDsStateOnC = Sensor.UNKNOWN;
    int currentTurnoutStateOnC = Turnout.UNKNOWN;
    boolean currentReservedFromC = false;

    int currentDsStateOnAux = Sensor.UNKNOWN;
    boolean currentReservedFromAux = false;

    // updated state information - inputs from current event -------------------
    int newDsStateHere = Sensor.INACTIVE; // init in case no connection
    int newTurnoutStateHere = Turnout.CLOSED;  // init in case no connection

    int newSpeedLimitFromA = 0;   // speed limit on SE leg attached to A
    int newDsStateOnA = Sensor.UNKNOWN;
    int newTurnoutStateOnA = Turnout.UNKNOWN;
    boolean newReservedFromA = false;

    int newSpeedLimitFromB = 0;   // speed limit on SE leg attached to B
    int newDsStateOnB = Sensor.UNKNOWN;
    int newTurnoutStateOnB = Turnout.UNKNOWN;
    boolean newReservedFromB = false;

    int newSpeedLimitFromC = 0;   // speed limit on SE leg attached to C
    int newDsStateOnC = Sensor.UNKNOWN;
    int newTurnoutStateOnC = Turnout.UNKNOWN;
    boolean newReservedFromC = false;

    int newDsStateOnAux = Sensor.UNKNOWN;
    boolean newReservedFromAux = false;

    // output values from calculation ==========================================
    // output values most recently sent in a message
    public int currentSpeedAX = 0;
    public int currentSpeedXA = 0;
    public int currentDirection = NONE;  //  AX, XA or both

    // newly-calculated output values ------------------------------------------
    int newSpeedAX = 0;
    int newSpeedXA = 0;
    int newDirection = NONE;

    // internal variables ======================================================
    boolean debug;

    // code ====================================================================
    public SecurityElement(int pNumber) {
        debug = log.isDebugEnabled();
        mNumber = pNumber;

        // default connections - same number for sensor, turnout
        dsSensor = pNumber;
        turnout = pNumber;

        // We draw the default with A facing left into the B of the
        // n-1th SE, and B facing right into the A of the n+1th SE.
        // The C leg is not attached by default
        attachAnum = pNumber - 1;
        attachAleg = B;

        attachBnum = pNumber + 1;
        attachBleg = A;

        attachCnum = 0;
        attachCleg = NONE;

        // At construction, register for all message types
        if (LnTrafficController.instance() != null) {
            LnTrafficController.instance().addLocoNetListener(~0, this);
        } else {
            log.error("Cannot connect to LocoNet, security element won't update");
        }
    }

    public int getNumber() {
        return mNumber;
    }

    public String showInputSpeeds() {
        return "A:" + currentSpeedLimitFromA
                + " B:" + currentSpeedLimitFromB
                + " C:" + currentSpeedLimitFromC;
    }

    public String showReservations() {
        return "A:" + (currentReservedFromA ? "t" : "f")
                + " B:" + (currentReservedFromB ? "t" : "f")
                + " C:" + (currentReservedFromC ? "t" : "f")
                + " x:" + (currentReservedFromAux ? "t" : "f");
    }

    public String showOccupancy() {
        String s = "";
        switch (currentDsStateHere) {
            case Sensor.ACTIVE:
                s += "o";
                break;
            case Sensor.INACTIVE:
                s += "u";
                break;
            case Sensor.UNKNOWN:
                s += "?";
                break;
            case Sensor.INCONSISTENT:
                s += "i";
                break;
            default:
                s += "x";
                break;
        }

        s += " A:";
        switch (currentDsStateOnA) {
            case Sensor.ACTIVE:
                s += "o";
                break;
            case Sensor.INACTIVE:
                s += "u";
                break;
            case Sensor.UNKNOWN:
                s += "?";
                break;
            case Sensor.INCONSISTENT:
                s += "i";
                break;
            default:
                s += "x";
                break;
        }

        s += " B:";
        switch (currentDsStateOnB) {
            case Sensor.ACTIVE:
                s += "o";
                break;
            case Sensor.INACTIVE:
                s += "u";
                break;
            case Sensor.UNKNOWN:
                s += "?";
                break;
            case Sensor.INCONSISTENT:
                s += "i";
                break;
            default:
                s += "x";
                break;
        }

        s += " C:";
        switch (currentDsStateOnC) {
            case Sensor.ACTIVE:
                s += "o";
                break;
            case Sensor.INACTIVE:
                s += "u";
                break;
            case Sensor.UNKNOWN:
                s += "?";
                break;
            case Sensor.INCONSISTENT:
                s += "i";
                break;
            default:
                s += "x";
                break;
        }

        return s;
    }

    /**
     * Process incoming messages. This includes:
     * <UL>
     * <LI>OPC_SE - Load state of adjacent SEs into new variables.
     * <LI>OPC_INPUT_REP - Sensor status change for occupancy. Store result into
     * newDsState and do update.
     * </UL>
     *
     */
    public void message(LocoNetMessage l) {
        switch (l.getOpCode()) {
            case 0xE4: {
                // SE report
                if (l.getElement(1) != 0x0A) {
                    return;
                }
                int element = l.getElement(2) * 128 + l.getElement(3);
                boolean update = false;
                // process the command
                switch ((l.getElement(4) >> 4) & 0x7) {
                    case 0x2:
                    case 0x6:
                        // reserved, should not occur
                        log.warn("Unexpected CMD in SE message: " + l.getElement(4));
                        return;
                    case 0x4:
                    case 0x5:
                    case 0x7:
                        // AG message, ignore
                        return;
                    case 0x1:
                    case 0x3:
                        // write-force messages, not handled
                        log.warn("Unexpected write-force CMD, not yet handled: " + l.getElement(4));
                        return;
                    case 0x0:
                    // fall off end, and handle this!
                }
                if (!calculates) {
                    // is addressed here?
                    if (((l.getElement(2) * 128) + l.getElement(3) + 1) != mNumber) {
                        return;
                    }

                    // not calculating, so just record values
                    seMessageForHere(l);
                    // notify listeners
                    firePropertyChange("SecurityElement", null, this);
                    return;
                }
                // be careful - you can be multiply connected! Check all legs
                if (element == attachAnum) {
                    newSpeedLimitFromA = getLimitFromMsg(l, attachAleg);
                    newDsStateOnA = getDsFromMessage(l);
                    newReservedFromA = getReservedFromMsg(l, attachAleg);
                    if (debug) {
                        log.debug("Update " + mNumber + " A leg: " + newSpeedLimitFromA + " " + newDsStateOnA);
                    }
                    update = true;
                }
                if (element == attachBnum) {
                    newSpeedLimitFromB = getLimitFromMsg(l, attachBleg);
                    newDsStateOnB = getDsFromMessage(l);
                    newReservedFromB = getReservedFromMsg(l, attachBleg);
                    if (debug) {
                        log.debug("Update " + mNumber + " B leg: " + newSpeedLimitFromB + " " + newDsStateOnB);
                    }
                    update = true;
                }
                if (element == attachCnum) {
                    newSpeedLimitFromC = getLimitFromMsg(l, attachCleg);
                    newDsStateOnC = getDsFromMessage(l);
                    newReservedFromC = getReservedFromMsg(l, attachCleg);
                    if (debug) {
                        log.debug("Update " + mNumber + " C leg: " + newSpeedLimitFromC + " " + newDsStateOnC);
                    }
                    update = true;
                }
                if (element == auxInput) {
                    newDsStateOnAux = getDsFromMessage(l);
                    // We mark as "reserved from Aux" if there are _any_ reservations
                    // in the auxInput SE.  Since they're co-located, if that SE
                    // is reserved in either direction, we want to know that
                    newReservedFromAux = getReservedFromMsg(l, A)
                            || getReservedFromMsg(l, B)
                            || getReservedFromMsg(l, C);
                    if (debug) {
                        log.debug("Update " + mNumber + " aux occ: " + newDsStateOnAux
                                + " aux reserved: " + newReservedFromAux);
                    }
                    update = true;
                }
                if (update) {
                    doUpdate();
                }
                break;
            }
            case LnConstants.OPC_INPUT_REP: {
                // is this from the associated sensor?
                if (l.inputRepAddr() + 1 == dsSensor) {   // input definitions are in 1-4096 space
                    // yes, save new state
                    int sw2 = l.getElement(2);
                    int state = sw2 & 0x10;
                    if (state != 0) {
                        newDsStateHere = Sensor.ACTIVE;
                    } else {
                        newDsStateHere = Sensor.INACTIVE;
                    }
                    doUpdate();
                }
                break;
            }
            case LnConstants.OPC_SW_REQ: {               /* page 9 of Loconet PE */

                int sw2 = l.getElement(2);
                if (l.turnoutAddr() == turnout) {
                    if (debug) {
                        log.debug("SW_REQ received with valid address");
                    }
                    if ((sw2 & LnConstants.OPC_SW_REQ_DIR) != 0) {
                        setTurnoutState(Turnout.CLOSED);
                    } else {
                        setTurnoutState(Turnout.THROWN);
                    }
                    doUpdate();
                }
                break;
            }
            case LnConstants.OPC_SW_REP: {               /* page 9 of Loconet PE */

                int sw2 = l.getElement(2);
                if (l.turnoutAddr() == turnout) {
                    if (debug) {
                        log.debug("SW_REP received with valid address");
                    }
                    if ((sw2 & LnConstants.OPC_SW_REQ_DIR) != 0) {
                        setTurnoutState(Turnout.CLOSED);
                    } else {
                        setTurnoutState(Turnout.THROWN);
                    }
                    doUpdate();
                }
                break;
            }

        }   // end block of switch cases
    }   // end of message() function

    /**
     * This OPC_SE message is from the SE attached to a leg, find the occupancy
     * it's asserting
     *
     */
    int getDsFromMessage(LocoNetMessage l) {
        if ((l.getElement(5) & 0x01) != 0) {
            return Sensor.ACTIVE;
        } else {
            return Sensor.INACTIVE;
        }
    }

    /**
     * This OPC_SE message is from the SE attached to a leg, find the speed
     * limit it's asserting
     *
     */
    int getLimitFromMsg(LocoNetMessage l, int leg) {
        // figure out which leg is interesting
        int speedAX = l.getElement(7);
        if ((speedAX & 0x80) != 0) {
            speedAX = (speedAX & 0x7F) * 4 + 128;
        }
        int speedXA = l.getElement(8);
        if ((speedXA & 0x80) != 0) {
            speedXA = (speedXA & 0x7F) * 4 + 128;
        }

        boolean to = (l.getElement(6) & 0x01) == 0x01;

        if (log.isDebugEnabled()) {
            log.debug("Find speedAX=" + speedAX
                    + " speedXA=" + speedXA + " to=" + to + " for leg " + leg);
        }

        switch (leg) {
            case A:
                return speedAX;

            case B:
                if (!to) {
                    return speedXA;
                } else {
                    log.debug("speed 0 since TO set against leg B");
                    return 0;    // can't enter if turnout against you
                }

            case C:
                if (to) {
                    return speedXA;
                } else {
                    log.debug("speed 0 since TO set against leg C");
                    return 0;    // can't enter if turnout against you
                }

            default:
                // includes case NONE - if you're attached, you have to
                // be attached to something!
                log.error("unexpected value for attachAleg: " + leg);
                return 0;
        }
    }

    /**
     * This OPC_SE message is from the SE attached to a leg, find whether its
     * asserting a reservation toward us
     *
     * @param l   Se message
     * @param leg Leg on the message-sending SE which this SE is attached to.
     */
    boolean getReservedFromMsg(LocoNetMessage l, int leg) {
        // figure out which leg is interesting
        int m5 = l.getElement(5);
        if (debug) {
            log.debug("check reserved in " + getNumber() + " m5=" + Integer.toHexString(m5) + " leg=" + leg);
        }

        // compare "leg we're attached to" to "direction of reservation in that SE"
        // to determine if this is a reservation toward us
        switch (leg) {
            case A:
                return (m5 & 0x20) == 0x20;  // checking XA as toward us

            case B:  // these are combined
            case C:
                return (m5 & 0x10) == 0x10;  // checking AX as toward us

            default:
                // includes case NONE - if you're attached, you have to
                // be attached to something!
                log.error("unexpected value for attachAleg: " + leg);
                return false;
        }
    }

    /**
     * Load a new state for the local detection section
     *
     * @param pNewState A Sensor state, e.g. Sensor.ACTIVE
     */
    void setDsState(int pNewState) {
        newDsStateHere = pNewState;
        doUpdate();
    }

    /**
     * Load a new state for the local turnout
     *
     * @param pNewState a Turnout state, e.g. Turnout.CLOSED
     */
    void setTurnoutState(int pNewState) {
        newTurnoutStateHere = pNewState;
        doUpdate();
    }

    /**
     * Update the calculation of speeds and direction.
     * <P>
     * This is the real core of the class, which does the entire computation
     * when anything changes.
     * <P>
     * The decision whether to send an update message is based on differences
     * between the previous (current) and new output values. See sendUpdate and
     * firePropertyChange.
     */
    void doUpdate() {
        if (calculates) {
            if (debug) {
                log.debug("SE " + mNumber + " starts. Neighbor speeds: "
                        + newSpeedLimitFromA + "," + newSpeedLimitFromB
                        + "," + newSpeedLimitFromC
                        + " res: " + newDirection);
            }

            // update the current reservation state
            makeReservationsHere();
            // calculate the effect on speed of geometry and braking
            doCalculateBaseSpeed();
            // adjust speed for reservations
            adjustForReservations();
            // adjust speed for conditions in the Aux SE
            adjustForAuxState();

            // and propagate as needed
            sendUpdate();

            // notify listeners
            firePropertyChange("SecurityElement", null, this);
        }
    }

    void makeReservationsHere() {
        // First, calculate any new reservations based on occupancy here.
        // A new reservation requires this block has just become occupied
        if (newDsStateHere == Sensor.ACTIVE && currentDsStateHere == Sensor.INACTIVE) {
            if (debug) {
                log.debug("went occupied, new states are A=" + (newDsStateOnA == Sensor.ACTIVE)
                        + " B=" + (newDsStateOnB == Sensor.ACTIVE)
                        + " C=" + (newDsStateOnC == Sensor.ACTIVE));
            }
            // check possible input blocks, and add direction setting if needed
            if (makeAReservation && newDsStateOnA == Sensor.ACTIVE) {
                newDirection |= AX;
            }
            if (makeBReservation && newDsStateOnB == Sensor.ACTIVE && newTurnoutStateHere == Turnout.CLOSED) {
                newDirection |= XA;
            }
            if (makeCReservation && newDsStateOnC == Sensor.ACTIVE && newTurnoutStateHere == Turnout.THROWN) {
                newDirection |= XA;
            }
        } // if we're not occupied, we're only propagating direction
        // reservations, so we will recalculate them later based on
        // current input information.
        // But if we are occupied, we hold our existing
        // reservations until the train is gone.
        else if (newDsStateHere == Sensor.INACTIVE) {
            newDirection = NONE;
        }

        // now include the effect of the reservations on either side.
        if (newSpeedLimitFromA == 0 && newReservedFromA) {
            newDirection |= AX;  // reserved for a train coming into us from A
        }
        if (newTurnoutStateHere == Turnout.CLOSED || turnout == 0) {
            // This is AB
            if (newSpeedLimitFromB == 0 && newReservedFromB) {
                newDirection |= XA;  // reserved for a train coming into us from B
            }
        } else {
            // this is AC
            if (newSpeedLimitFromC == 0 && newReservedFromC) {
                newDirection |= XA;  // reserved for a train coming into us from C
            }
        }
    }

    void doCalculateBaseSpeed() {
        // calculate speed for XA
        // Speed is the minumum of:
        //    zero if occupied
        //    mechanical speed limit for BA or CA
        //    entry speed on the leg attached to A + decrement BA or CA
        // Start by seeing if this is B or C
        if (newTurnoutStateHere == Turnout.CLOSED || turnout == 0) {
            // This is BA
            newSpeedXA = Math.min(maxSpeedBA, newSpeedLimitFromA + maxBrakingBA);
        } else {
            // this is CA
            newSpeedXA = Math.min(maxSpeedCA, newSpeedLimitFromA + maxBrakingCA);
        }

        // calculate speed for AX
        // Speed is the minimum of:
        //    zero if occupied
        //    mechanical speed limit for AB or AC
        //    entry speed on the leg attached to B, C + decrement AB or AC
        // Start by seeing if this is coming from B or C
        if (newTurnoutStateHere == Turnout.CLOSED || turnout == 0) {
            // This is AB
            newSpeedAX = Math.min(maxSpeedAB, newSpeedLimitFromB + maxBrakingAB);
        } else {
            // this is AC
            newSpeedAX = Math.min(maxSpeedAC, newSpeedLimitFromC + maxBrakingAC);
        }
        if (newDsStateHere == Sensor.ACTIVE) {
            log.debug("Sensor active sets speeds to zero");
            newSpeedAX = 0;
            newSpeedXA = 0;
        }
        if (log.isDebugEnabled()) {
            log.debug("Newly calculated speeds are " + newSpeedAX + "," + newSpeedXA);
        }
    }

    /**
     * Adjust the speed values for the effect of any reservations in effect.
     */
    void adjustForReservations() {

        switch (onAXReservation) {
            case STOPOPPOSITE:
                if ((newDirection & AX) == AX) {
                    newSpeedXA = 0;
                    log.debug("STOPOPPOSITE reservation set XA to zero");
                }
                break;
            case STOPUNRESERVED:
                if ((newDirection & AX) != AX) {
                    newSpeedAX = 0;
                    log.debug("STOPUNRESERVED reservation set AX to zero");
                }
                break;
            default:
        }

        switch (onXAReservation) {
            case STOPOPPOSITE:
                if ((newDirection & XA) == XA) {
                    newSpeedAX = 0;
                    log.debug("STOPOPPOSITE reservation set AX to zero");
                }
                break;
            case STOPUNRESERVED:
                if ((newDirection & XA) != XA) {
                    newSpeedXA = 0;
                    log.debug("STOPUNRESERVED reservation set XA to zero");
                }
                break;
            default:
        }
    }

    /**
     * The Aux SE is used to indicate an overlapping region of space, though not
     * on the specific route this SE protects. For example, a crossing or
     * scissors crossover. If the Aux SE shows occupied, it's not safe to enter
     * this SE, so the speeds are set to zero.
     */
    void adjustForAuxState() {
        if (debug) {
            log.debug("SE " + getNumber() + " adjust aux with sensor "
                    + (newDsStateOnAux == Sensor.ACTIVE)
                    + " aux reserved " + newReservedFromAux);
        }
        if ((newDsStateOnAux == Sensor.ACTIVE)
                || newReservedFromAux) {
            newSpeedXA = 0;
            newSpeedAX = 0;
        }
    }

    /**
     * Format up a message containing the new values and send it. In the
     * process, copy the "new" values to the "current" values.
     */
    void sendUpdate() {
        // at least one value must have changed!
        if (newDsStateHere != currentDsStateHere
                || newTurnoutStateHere != currentTurnoutStateHere
                || newSpeedAX != currentSpeedAX
                || newSpeedXA != currentSpeedXA
                || newDirection != currentDirection) {
            // yes, send the update via LocoNet
            if (debug) {
                log.debug("Send new values from SE" + mNumber + ": "
                        + newSpeedAX + " "
                        + newSpeedXA + " "
                        + newDirection);
            }

            // format the status word
            // @todo define the 2nd status bits
            int seStat1 = 0;
            int seStat2 = 0;

            // reserved direction bits
            if ((newDirection & AX) == AX) {
                seStat1 |= 0x10;
            }
            if ((newDirection & XA) == XA) {
                seStat1 |= 0x20;
            }

            // turnout status bits
            if (newTurnoutStateHere == Turnout.THROWN) {
                seStat2 |= 0x01;
            }
            if (newTurnoutStateOnA == Turnout.THROWN) {
                seStat2 |= 0x02;
            }
            if (newTurnoutStateOnB == Turnout.THROWN) {
                seStat2 |= 0x04;
            }
            if (newTurnoutStateOnC == Turnout.THROWN) {
                seStat2 |= 0x08;
            }

            // occupancy bits
            if (newDsStateHere == Sensor.ACTIVE) {
                seStat1 |= 0x01;
            }
            if (newDsStateOnA == Sensor.ACTIVE) {
                seStat1 |= 0x02;
            }
            if (newDsStateOnB == Sensor.ACTIVE) {
                seStat1 |= 0x04;
            }
            if (newDsStateOnC == Sensor.ACTIVE) {
                seStat1 |= 0x08;
            }

            LocoNetMessage m1 = new LocoNetMessage(10);
            m1.setOpCode(0xE4);         // OPC_SE
            m1.setElement(1, 0x0A);     // OPC_SE
            m1.setElement(2, mNumber / 128);      // SE high
            m1.setElement(3, mNumber & 0x7F);     // SE low
            m1.setElement(4, 0x00);             // SE CMD
            m1.setElement(5, seStat1);     // SE STAT1
            m1.setElement(6, seStat2);     // SE STAT2
            // @todo speed is only encoded in the lower range
            m1.setElement(7, (newSpeedAX) & 0x7F);  // SE SPD_AX
            m1.setElement(8, (newSpeedXA) & 0x7F);  // SE SPD_XA

            if (log.isDebugEnabled()) {
                log.debug("Send SE message: " + m1.toString());
            }

            LnTrafficController.instance().sendLocoNetMessage(m1);
        }

        // Copy always. Note that "current" values are not necessarily
        // used in the rest of the class, but we copy everything to
        // make sure they're there if we ever start to use them.
        currentDsStateHere = newDsStateHere;
        currentTurnoutStateHere = newTurnoutStateHere;
        currentSpeedLimitFromA = newSpeedLimitFromA;
        currentDsStateOnA = newDsStateOnA;
        currentReservedFromA = newReservedFromA;
        currentSpeedLimitFromB = newSpeedLimitFromB;
        currentDsStateOnB = newDsStateOnB;
        currentReservedFromB = newReservedFromB;
        currentSpeedLimitFromC = newSpeedLimitFromC;
        currentDsStateOnC = newDsStateOnC;
        currentReservedFromC = newReservedFromC;
        currentDsStateOnAux = newDsStateOnAux;
        currentReservedFromAux = newReservedFromAux;

        currentSpeedAX = newSpeedAX;
        currentSpeedXA = newSpeedXA;
        currentDirection = newDirection;
    }

    /**
     * SE status message should be copied into our local memory
     */
    void seMessageForHere(LocoNetMessage m1) {

        // format the status word
        int seStat1 = m1.getElement(5);
        int seStat2 = m1.getElement(6);

        // turnout status bits
        if ((seStat2 & 0x01) != 0) {
            newTurnoutStateHere = Turnout.THROWN;
        } else {
            newTurnoutStateHere = Turnout.CLOSED;
        }

        if ((seStat2 & 0x02) != 0) {
            newTurnoutStateOnA = Turnout.THROWN;
        } else {
            newTurnoutStateOnA = Turnout.CLOSED;
        }

        if ((seStat2 & 0x04) != 0) {
            newTurnoutStateOnB = Turnout.THROWN;
        } else {
            newTurnoutStateOnB = Turnout.CLOSED;
        }

        if ((seStat2 & 0x08) != 0) {
            newTurnoutStateOnC = Turnout.THROWN;
        } else {
            newTurnoutStateOnC = Turnout.CLOSED;
        }

        // occupancy bits
        if ((seStat1 & 0x01) != 0) {
            newDsStateHere = Sensor.ACTIVE;
        } else {
            newDsStateHere = Sensor.INACTIVE;
        }

        if ((seStat1 & 0x02) != 0) {
            newDsStateOnA = Sensor.ACTIVE;
        } else {
            newDsStateOnA = Sensor.INACTIVE;
        }

        if ((seStat1 & 0x04) != 0) {
            newDsStateOnB = Sensor.ACTIVE;
        } else {
            newDsStateOnB = Sensor.INACTIVE;
        }

        if ((seStat1 & 0x08) != 0) {
            newDsStateOnC = Sensor.ACTIVE;
        } else {
            newDsStateOnC = Sensor.INACTIVE;
        }

        newSpeedAX = m1.getElement(7);  // SE SPD_AX
        newSpeedXA = m1.getElement(8);  // SE SPD_XA

        // and update copies
        currentDsStateHere = newDsStateHere;
        currentTurnoutStateHere = newTurnoutStateHere;
        currentDsStateOnA = newDsStateOnA;
        currentDsStateOnB = newDsStateOnB;
        currentDsStateOnC = newDsStateOnC;
        currentDsStateOnAux = newDsStateOnAux;

        currentSpeedAX = newSpeedAX;
        currentSpeedXA = newSpeedXA;

    }

    public void dispose() {
    }

    // to hear of changes
    java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);

    public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    protected void firePropertyChange(String p, Object old, Object n) {
        pcs.firePropertyChange(p, old, n);
    }

    public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    private final static Logger log = LoggerFactory.getLogger(SecurityElement.class.getName());

}

/* @(#)SecurityElement.java */
