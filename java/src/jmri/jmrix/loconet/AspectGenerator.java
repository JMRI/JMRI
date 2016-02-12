// AspectGenerator.java
package jmri.jmrix.loconet;

import jmri.InstanceManager;
import jmri.SignalHead;
import jmri.Turnout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test implementation of AspectGenerator, ala 1/8 of an SE8.
 * <P>
 * An AspectGenerator drives specific Aspects to SignalHead objects. The
 * SignalHead objects represent individual signals either on a screen or
 * physical items on the layout.
 * <P>
 * Note that this class is tightly bound to both the SecurityElement class and a
 * specific SecurityElement object. More thought needs to be given to whether
 * this is the same object, a related one (e.g. common config), or completely
 * separate. This matter for both state and configuration!
 * <P>
 * At runtime, we reference everything through a SecurityElement referece, at
 * least for now. Eventually, we can listen to messages to decouple this.
 * <P>
 * Head 0 guards entering the A leg, head 1 guards the B legt, head 2 guards the
 * C leg, head 3 is the 2nd head on the A leg
 * <P>
 * Simple hard-coded aspect chart: Green is 55MPH and up Flashing yellow is 35
 * and up Yellow is 15 and up Red is less than 15
 *
 * <P>
 * The SignalHead are created here if they don't already exist, because
 * instantiating the AspectGenerator is actually creating the connection to the
 * device on the layout. We assume the SE8cSignalHead implementation.
 *
 * <P>
 * The algorithms in this class are a collaborative effort of Digitrax, Inc and
 * Bob Jacobsen. Some of the message formats are copyright Digitrax, Inc.
 *
 * @author	Bob Jacobsen Copyright (C) 2002
 * @version $Revision$
 */
public class AspectGenerator implements java.beans.PropertyChangeListener {

    protected AspectGenerator(SecurityElement pElement) {
        // locate the SE
        mSE = pElement;

        // listen to that for changes
        mSE.addPropertyChangeListener(this);

        // load default values
        int headAddr = 257 + (mSE.getNumber() - 1) * 8;
        heads = new SignalHead[]{new SE8cSignalHead(headAddr, "" + mSE.getNumber() + "-A"),
            new SE8cSignalHead(headAddr + 4, "" + mSE.getNumber() + "-B"),
            new SE8cSignalHead(headAddr + 6, "" + mSE.getNumber() + "-C"),
            new SE8cSignalHead(headAddr + 2, "" + mSE.getNumber() + "-D")};

        // register with SignalHeadManager
        InstanceManager.signalHeadManagerInstance().register(heads[0]);
        InstanceManager.signalHeadManagerInstance().register(heads[1]);
        InstanceManager.signalHeadManagerInstance().register(heads[2]);
        InstanceManager.signalHeadManagerInstance().register(heads[3]);
    }

    public AspectGenerator(int se) {
        this(LnSecurityElementManager.instance().getSecurityElement(se));
        mSEName = "" + se;
    }

    /**
     * Access the state of a specific head
     *
     * @param num Head number 0 -> getNumHeads()-1
     * @return A SignalHead-defined constant
     */
    public int getHeadState(int num) {
        if (log.isDebugEnabled()) {
            log.debug("asked for state of " + getSEName()
                    + "/" + num + ", is " + heads[num].getAppearance());
        }
        return heads[num].getAppearance();
    }

    SecurityElement mSE;
    String mSEName;

    public String getSEName() {
        return mSEName;
    }

    /**
     * References to the signal heads in use. These are located during the
     * constructor.
     */
    SignalHead[] heads;

    public int getNumHeads() {
        return heads.length;
    }

    /**
     * The associated SecurityElement has changed state, so update our signal
     * heads
     */
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        boolean update = true;  // no suppression logic yet
        if (log.isDebugEnabled()) {
            log.debug("updating");
        }
        int speed;

        // Head A, in two parts
        speed = mSE.newSpeedAX;
        updateSpeed(heads[0], speed);
        updateDiverging(heads[0], heads[3]);

        if (mSE.newTurnoutStateHere == Turnout.CLOSED) {
            speed = mSE.newSpeedXA;
        } else {
            speed = 0;
        }
        updateSpeed(heads[1], speed);

        if (mSE.newTurnoutStateHere == Turnout.THROWN) {
            speed = mSE.newSpeedXA;
        } else {
            speed = 0;
        }
        updateSpeed(heads[2], speed);

        // and pass on to our listeners
        if (update) {
            firePropertyChange("Aspects", null, this);
        }
    }

    /**
     * Set a specific head to represent a specific speed.
     */
    void updateSpeed(SignalHead h, int speed) {
        if (log.isDebugEnabled()) {
            log.debug("Update head " + h.getSystemName() + ":" + h.getUserName() + " to speed " + speed);
        }
        if (speed >= 55) {
            h.setAppearance(SignalHead.GREEN);
        } else if (speed >= 35) {
            h.setAppearance(SignalHead.FLASHYELLOW);
        } else if (speed >= 15) {
            h.setAppearance(SignalHead.YELLOW);
        } else {
            h.setAppearance(SignalHead.RED);
        }
    }

    /**
     * Set a SignalHead to represent the "diverging" status of a AX route
     *
     * @param hu upper (primary) signal head on an A end
     * @param hl lower (secondary) signal head on an A end
     */
    void updateDiverging(SignalHead hu, SignalHead hl) {
        if (mSE.newTurnoutStateHere == Turnout.CLOSED) {
            // non-diverging case
            hl.setAppearance(SignalHead.RED);
            return;
        } else {
            // diverging case
            hl.setAppearance(SignalHead.YELLOW);
            return;
        }
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

    private final static Logger log = LoggerFactory.getLogger(AspectGenerator.class.getName());

    // for now, this is an internal class
    static class Aspect {

        public Aspect(int speed, int appearance) {
        }
    }
}


/* @(#)AspectGenerator.java */
