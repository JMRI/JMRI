// SecurityElement.java

package jmri.jmrix.loconet;

import jmri.*;

/**
 * Test implementation of SecurityElement, ala 1/8 of an SE8.
 * <P>
 * This is implemented in terms of basic LocoNet, rather than using
 * references to Sensor and Turnout objects, to force it to be specific
 * about how messages are handled.
 * <P>
 * Messages listened to:
 * <UL>
 * <LI>Turnout commands, feedback
 * <LI>Sensor status
 * <LI>SE status (short form only)
 * </UL>
 * <P>
 * Note that the SE message turnout bit is set for CLOSED  or no
 * turnout associated. The configured turnout number is 1-N
 * <P>
 * The 0x10 and 0x20 bits of SE message element 5 encode direction reservations.
 * 0x10 set means that the transmitting SE is reserved toward its own A leg (AX travel)
 * 0x20 set means that the transmittion SE is reserved away from its own A leg (XA travel)
 * Internally, our "Direction" variables encode the direction of travel within
 * this SE.  Note that this combination results in an extra complement; if looking
 * at a remote SE's A leg and it's reserved AX, that it NOT coming toward us.
 *
 * <P>The algorithms in this class are a collaborative effort of Digitrax, Inc
 * and Bob Jacobsen.  Some of the message formats are copyright Digitrax, Inc.
 *
 * @author			Bob Jacobsen Copyright (C) 2002
 * @version         $Revision: 1.8 $
 */
public class SecurityElement implements LocoNetListener {

    // constants

    // direction codes
    public static final int NONE = 0;  // unknown or undecided
    public static final int AX = 8;    // enter from A, leave from B or C
    public static final int XA = 16;   // enter from B or C, leave from A

    // leg names
    public static final int A = 1;
    public static final int B = 2;
    public static final int C = 4;
    // also NONE for no connection

    // configuration information
    public int mNumber;     // own SE number

    //public int mLogic = -1;      // logic executed by this element
    //public static final int ABS = 0;
    //public static final int APB = 1;
    //public static final int HEADBLOCK = 2;

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

    // state information

    // existing state information - inputs
    int currentDsStateHere       = Sensor.UNKNOWN;
    int currentTurnoutState      = Turnout.UNKNOWN;

    int currentSpeedLimitFromA   = 0;   // speed limit on the SE leg attached to A
    int currentDsStateOnA        = Sensor.UNKNOWN;
    boolean currentReservedFromA = false;

    int currentSpeedLimitFromB   = 0;   // speed limit on the SE leg attached to B
    int currentDsStateOnB        = Sensor.UNKNOWN;
    boolean currentReservedFromB = false;

    int currentSpeedLimitFromC   = 0;   // speed limit on the SE leg attached to C
    int currentDsStateOnC        = Sensor.UNKNOWN;
    boolean currentReservedFromC = false;

    // outputs
    public int currentSpeedAX      = 0;
    public int currentSpeedXA      = 0;
    public int currentDirection    = NONE;  //  AX, XA or both

    // updated state information - inputs
    int newDsStateHere           = Sensor.INACTIVE; // start this way in case there's no connection
    int newTurnoutState          = Turnout.CLOSED;  // start this way in case there's no connection

    int newSpeedLimitFromA       = 0;   // speed limit on the SE leg attached to A
    int newDsStateOnA            = Sensor.UNKNOWN;
    boolean newReservedFromA     = false;

    int newSpeedLimitFromB       = 0;   // speed limit on the SE leg attached to B
    int newDsStateOnB            = Sensor.UNKNOWN;
    boolean newReservedFromB     = false;

    int newSpeedLimitFromC       = 0;   // speed limit on the SE leg attached to C
    int newDsStateOnC            = Sensor.UNKNOWN;
    boolean newReservedFromC     = false;

    int newReservedFromAux    = NONE;

    // outputs
    int newSpeedAX          = 0;
    int newSpeedXA          = 0;
    int newDirection        = NONE;

    boolean debug;

    public SecurityElement(int pNumber) {
        debug = log.isDebugEnabled();
        mNumber = pNumber;

        // default connections - same number for sensor, turnout
        dsSensor = pNumber;
        turnout = pNumber;

        // We draw the default with A facing left into the B of the
        // n-1th SE, and B facing right into the A of the n+1th SE.
        // The C leg is not attached by default
        attachAnum = pNumber-1;
        attachAleg = B;

        attachBnum = pNumber+1;
        attachBleg = A;

        attachCnum = 0;
        attachCleg = NONE;

        // At construction, register for all message types
        if (LnTrafficController.instance()!=null)
            LnTrafficController.instance().addLocoNetListener(~0, this);
        else
            log.error("Cannot connect to LocoNet, security element won't update");
    }
    public int getNumber() { return mNumber; }

    /**
     * Process incoming messages.
     * This includes:
     * <UL>
     * <LI>OPC_SE - Load state of adjacent SEs into new variables.
     * <LI>OPC_INPUT_REP - Sensor status change for occupancy. Store
     * result into newDsState and do update.
     * </UL>
     * @param l
     */
    public void message(LocoNetMessage l) {
        switch (l.getOpCode()) {
        case 0xE4: {
            // SE report
            if (l.getElement(1)!=0x09) break;
            int element = l.getElement(2)*128+l.getElement(3);
            boolean update = false;
            // be careful - you can be multiply connected!
            if (element == attachAnum) {
                newSpeedLimitFromA = getLimitFromMsg(l, attachAleg);
                newDsStateOnA = getDsFromMessage(l);
                newReservedFromA = getReservedFromMsg(l, attachAleg);
                if (debug) log.debug("Update "+mNumber+" A leg: "+newSpeedLimitFromA+" "+newDsStateOnA);
                update = true;
            }
            if (element == attachBnum) {
                newSpeedLimitFromB = getLimitFromMsg(l, attachBleg);
                newDsStateOnB = getDsFromMessage(l);
                newReservedFromB = getReservedFromMsg(l, attachBleg);
                if (debug) log.debug("Update "+mNumber+" B leg: "+newSpeedLimitFromB+" "+newDsStateOnB);
                update = true;
            }
            if (element == attachCnum) {
                newSpeedLimitFromC = getLimitFromMsg(l, attachCleg);
                newDsStateOnC = getDsFromMessage(l);
                newReservedFromC = getReservedFromMsg(l, attachCleg);
                if (debug) log.debug("Update "+mNumber+" C leg: "+newSpeedLimitFromC+" "+newDsStateOnC);
                update = true;
            }
            if (element == auxInput) {
                // if there's a reservation from the aux, it reserves
                // in _BOTH_ directions
                if (getReservedFromMsg(l, attachAleg)) newReservedFromAux = AX|XA;
                else if (getReservedFromMsg(l, attachBleg)) newReservedFromAux = AX|XA;
                else if (getReservedFromMsg(l, attachCleg)) newReservedFromAux = AX|XA;
                else newReservedFromAux = NONE;
                if (debug) log.debug("Update "+mNumber+" aux input: "+newReservedFromAux);
                update = true;
            }
            if (update) doUpdate();
            break;
        }
        case LnConstants.OPC_INPUT_REP: {
            // is this from the associated sensor?
            if (l.inputRepAddr() == dsSensor) {
                // yes, save new state
                int sw2 = l.getElement(2);
                int state = sw2 & 0x10;
                if (state !=0) newDsStateHere = Sensor.ACTIVE;
                else newDsStateHere = Sensor.INACTIVE;
                doUpdate();
            }
            break;
        }
        case LnConstants.OPC_SW_REQ: {               /* page 9 of Loconet PE */
            int sw2 = l.getElement(2);
            if (l.turnoutAddr()==turnout) {
                if (debug) log.debug("SW_REQ received with valid address");
                if ((sw2 & LnConstants.OPC_SW_REQ_DIR)!=0) {
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
            if (l.turnoutAddr()==turnout) {
                if (debug) log.debug("SW_REP received with valid address");
                if ((sw2 & LnConstants.OPC_SW_REQ_DIR)!=0) {
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
     * This OPC_SE message is from the SE attached to
     * a leg, find the occupancy it's asserting
     * @param l
     */
    int getDsFromMessage(LocoNetMessage l) {
        if ((l.getElement(5)&0x04)!=0)
            return Sensor.ACTIVE;
        else return Sensor.INACTIVE;
    }

    /**
     * This OPC_SE message is from the SE attached to
     * a leg, find the speed limit it's asserting
     * @param l
     */
    int getLimitFromMsg(LocoNetMessage l, int leg) {
        // figure out which leg is interesting
        int speedAX = l.getElement(6)*4;
        int speedXA = l.getElement(7)*4;
        boolean to = (l.getElement(5)&0x01)==0x01;
        switch (leg) {
        case A:
            return speedAX;

        case B:
            if (to) return speedXA;
            else return 0;    // can't enter if turnout against you

        case C:
            if (!to) return speedXA;
            else return 0;

        default:
            // includes case NONE - if you're attached, you have to
            // be attached to something!
            log.error("unexpected value for attachAleg: "+leg);
            return 0;
        }
    }

    /**
     * This OPC_SE message is from the SE attached to
     * a leg, find whether its asserting a reservation toward us
     * @param l Se message
     * @param leg Leg on the message-sending SE which this SE is
     * attached to.
     */
    boolean getReservedFromMsg(LocoNetMessage l, int leg) {
        // figure out which leg is interesting
        int m5 = l.getElement(5);
        log.debug("check reserved in "+getNumber()+" m5="+Integer.toHexString(m5)+" leg="+leg);
        switch (leg) {
        case A:
            return (m5&0x20)==0x20;  // checking XA as toward us

        case B:  // these are combined
        case C:
            return (m5&0x10)==0x10;  // checking AX as toward us

        default:
            // includes case NONE - if you're attached, you have to
            // be attached to something!
            log.error("unexpected value for attachAleg: "+leg);
            return false;
        }
    }

    /**
     * Load a new state for the local detection section
     * @param pNewState A Sensor state, e.g. Sensor.ACTIVE
     */
    void setDsState(int pNewState) {
        newDsStateHere = pNewState;
        doUpdate();
    }

    /**
     * Load a new state for the local turnout
     * @param pNewState a Turnout state, e.g. Turnout.CLOSED
     */
    void setTurnoutState(int pNewState) {
        newTurnoutState = pNewState;
        doUpdate();
    }

    /**
     * Update the calculation of speeds and direction.
     * This is the real core of the class, which does
     * the entire computation when anything changes.
     *<P>
     * The decision whether to send an update message is
     * based on differences between the previous (current) and new
     * output values.  See sendUpdate and firePropertyChange.
     */
    void doUpdate() {
        if (debug) log.debug("SE "+mNumber+" starts. Speeds: "
                             +newSpeedLimitFromA+","+newSpeedLimitFromB
                             +","+newSpeedLimitFromC
                             +" res: "+newDirection);

        // update the current reservation state
        makeReservationsHere();
        // calculate the effect on speed of geometry and braking
        doCalculateBaseSpeed();
        // adjust speed for reservations
        adjustForReservations();

        // and propagate as needed
        sendUpdate();
        firePropertyChange("SecurityElement", null, this);
    }

    void makeReservationsHere() {
        // First, calculate any new reservations based on occupancy here.
        // A new reservation requires this block has just become occupied
        if (newDsStateHere==Sensor.ACTIVE && currentDsStateHere==Sensor.INACTIVE) {
            log.debug("went occupied, new states are A="+(newDsStateOnA==Sensor.ACTIVE)
                        +" B="+(newDsStateOnB==Sensor.ACTIVE)
                        +" C="+(newDsStateOnC==Sensor.ACTIVE) );
            // check possible input blocks, and add direction setting if needed
            if (makeAReservation && newDsStateOnA==Sensor.ACTIVE)
                newDirection |= AX;
            if (makeBReservation && newDsStateOnB==Sensor.ACTIVE && newTurnoutState==Turnout.CLOSED)
                newDirection |= XA;
            if (makeCReservation && newDsStateOnC==Sensor.ACTIVE && newTurnoutState==Turnout.THROWN)
                newDirection |= XA;
        }
        // if we're not occupied, we're only propagating direction
        // reservations, so we will recalculate them later based on
        // current input information.
        // But if we are occupied, we hold our existing
        // reservations until the train is gone.
        else if (newDsStateHere==Sensor.INACTIVE) newDirection = NONE;

        // now include the effect of the reservations on either side.
        if (newSpeedLimitFromA==0 && newReservedFromA) {
            newDirection |= AX;  // reserved for a train coming into us from A
        }
        if (newTurnoutState==Turnout.CLOSED || turnout == 0) {
            // This is AB
            if (newSpeedLimitFromB==0 && newReservedFromB) {
                newDirection |= XA;  // reserved for a train coming into us from B
            }
        } else {
            // this is AC
            if (newSpeedLimitFromC==0 && newReservedFromC) {
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
        if (newTurnoutState==Turnout.CLOSED || turnout == 0 ) {
            // This is BA
            newSpeedXA = Math.min(maxSpeedBA, newSpeedLimitFromA+maxBrakingBA);
        } else {
            // this is CA
            newSpeedXA = Math.min(maxSpeedCA, newSpeedLimitFromA+maxBrakingCA);
        }
        if (newDsStateHere==Sensor.ACTIVE) newSpeedXA = 0;

        // calculate speed for AX
        // Speed is the minimum of:
        //    zero if occupied
        //    mechanical speed limit for AB or AC
        //    entry speed on the leg attached to B, C + decrement AB or AC
        // Start by seeing if this is coming from B or C
        if (newTurnoutState==Turnout.CLOSED || turnout == 0) {
            // This is AB
            newSpeedAX = Math.min(maxSpeedAB, newSpeedLimitFromB+maxBrakingAB);
        } else {
            // this is AC
            newSpeedAX = Math.min(maxSpeedAC, newSpeedLimitFromC+maxBrakingAC);
        }
        if (newDsStateHere==Sensor.ACTIVE) newSpeedAX = 0;
    }

    /**
     * Adjust the speed values for the effect of any reservations in
     * effect.
     */
    void adjustForReservations() {

        switch (onAXReservation) {
        case STOPOPPOSITE:
            if ( (newDirection&AX) == AX) newSpeedXA = 0;
            break;
        case STOPUNRESERVED:
            if ( (newDirection&AX) != AX) newSpeedAX = 0;
            break;
        default:
        }

        switch (onXAReservation) {
        case STOPOPPOSITE:
            if ( (newDirection&XA) == XA) newSpeedAX = 0;
            break;
        case STOPUNRESERVED:
            if ( (newDirection&XA) != XA) newSpeedXA = 0;
            break;
        default:
        }
    }

    /**
     * Format up a message containing the new values and send it.
     * In the process, copy the "new" values to the "current" values.
     */
    void sendUpdate() {
        // at least one value must have changed!
        if (newDsStateHere != currentDsStateHere
            || newTurnoutState != currentTurnoutState
            || newSpeedAX != currentSpeedAX
            || newSpeedXA != currentSpeedXA
            || newDirection != currentDirection) {
            // yes, send the update via LocoNet
            if (debug) log.debug("Send new values: "+
                                 newSpeedAX+" "+
                                 newSpeedXA+" "+
                                 newDirection);

            // format the status word
            // @todo define the 2nd status bits
            int seStat = 0;
            if ((newDirection&AX)==AX) seStat |= 0x10;
            if ((newDirection&XA)==XA) seStat |= 0x20;
            if (newTurnoutState==Turnout.CLOSED || turnout == 0) seStat |= 0x01;
            if (newDsStateHere==Sensor.ACTIVE) seStat |= 0x04;

            LocoNetMessage m1 = new LocoNetMessage(9);
            m1.setOpCode(0xE4);         // OPC_SE
            m1.setElement(1, 0x09);     // OPC_SE
            m1.setElement(2, mNumber/128);      // SE high
            m1.setElement(3, mNumber&0x7F);     // SE low
            m1.setElement(4, 0x00);             // SE CMD
            m1.setElement(5, seStat);     // SE STAT
            // @todo speed is only encoded in the lower range
            m1.setElement(6, (newSpeedAX/4)&0x3F);  // SE SPD_AX
            m1.setElement(7, (newSpeedXA/4)&0x3F);  // SE SPD_XA

            LnTrafficController.instance().sendLocoNetMessage(m1);
        }
        // copy always
        currentDsStateHere       = newDsStateHere;
        currentTurnoutState      = newTurnoutState;
        currentSpeedLimitFromA   = newSpeedLimitFromA;
        currentDsStateOnA        = newDsStateOnA;
        currentReservedFromA     = newReservedFromA;
        currentSpeedLimitFromB   = newSpeedLimitFromB;
        currentDsStateOnB        = newDsStateOnB;
        currentReservedFromB     = newReservedFromB;
        currentSpeedLimitFromC   = newSpeedLimitFromC;
        currentDsStateOnC        = newDsStateOnC;
        currentReservedFromC     = newReservedFromC;
        currentSpeedAX           = newSpeedAX;
        currentSpeedXA           = newSpeedXA;
        currentDirection         = newDirection;
    }

    public void dispose() {}

    // to hear of changes
    java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);
    public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }
    protected void firePropertyChange(String p, Object old, Object n) { pcs.firePropertyChange(p,old,n);}
    public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SecurityElement.class.getName());

}

/* @(#)SecurityElement.java */
