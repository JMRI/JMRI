// AspectGenerator.java

package jmri.jmrix.loconet;

import jmri.*;

/**
 * Test implementation of AspectGenerator, ala 1/8 of an SE8.
 * <P>
 * Note that this class is tightly bound to
 * both the SecurityElement class and a specific SecurityElement object.
 * More thought needs to be given to whether this is
 * the same object, a related one (e.g. common config), or completely
 * separate.  This matter for both state and configuration!
 * <P>
 * At runtime, we reference everything through a SecurityElement referece,
 * at least for now. Eventually, we can listen to messages to decouple this.
 * <P>
 * Head 0 guards entering the A leg, head 1 guards the B legt, head 2
 * guards the C leg
 * <P>
 * Simple hard-coded aspect chart:
 *  Green is 55MPH and up
 *  Flashing yellow is 35 and up
 *  Yellow is 15 and up
 *  Red is less than 15
 *
 * @author			Bob Jacobsen Copyright (C) 2002
 * @version         $Revision: 1.2 $
 */
public class AspectGenerator implements java.beans.PropertyChangeListener{

	protected AspectGenerator(SecurityElement pElement) {
        // locate the SE
        mSE = pElement;

        // listen to that for changes
        mSE.addPropertyChangeListener(this);

        // load default values
	}

    public AspectGenerator(int se) {
        this(LnSecurityElementManager.instance().getSecurityElement(se));
        mSEName = ""+se;
    }

    /**
     * Access the state of a specific head
     * @param num Head number 0 -> getNumHeads()-1
     * @return A SignalHead-defined constant
     */
    public int getHeadState(int num) {
        if (log.isDebugEnabled()) log.debug("asked for state of "+getSEName()
                                            +"/"+num+", is "+heads[num].state);
        return heads[num].state;
    }

    SecurityElement mSE;
    String mSEName;
    public String getSEName() { return mSEName; }

    Head[] heads = new Head[]{new Head(), new Head(), new Head(), new Head()};

    public int getNumHeads() {return heads.length;}
    /**
     * The associated SecurityElement has changed state, so update
     * our signal heads
     */
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        boolean update = true;  // no suppression logic yet
        if (log.isDebugEnabled()) log.debug("updating");
        int speed;
        speed = mSE.newSpeedAX;
        updateSpeed(heads[0], speed);

        if (mSE.newTurnoutState==Turnout.CLOSED) speed = mSE.newSpeedXA;
        else speed = 0;
        updateSpeed(heads[1], speed);

        if (mSE.newTurnoutState==Turnout.THROWN) speed = mSE.newSpeedXA;
        else speed = 0;
        updateSpeed(heads[2], speed);

        // and pass on to our listeners
        if (update) firePropertyChange("Aspects", null, this);
    }

    void updateSpeed(Head h, int speed) {
        if (speed>=55) h.state = SignalHead.GREEN;
        else if (speed>=35) h.state = SignalHead.FLASHYELLOW;
        else if (speed>=15) h.state = SignalHead.YELLOW;
        else h.state = SignalHead.RED;
    }
	// to hear of changes
	java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);
	public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
		pcs.addPropertyChangeListener(l);
		}
	protected void firePropertyChange(String p, Object old, Object n) { pcs.firePropertyChange(p,old,n);}
	public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
		pcs.removePropertyChangeListener(l);
		}

	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(AspectGenerator.class.getName());

    // for now, this is an internal class
    class Aspect {
        public Aspect(int speed, int appearance) {};
    }
    class Head {
        int state = 0;
        public Head() {
            aspects = new Aspect[] { new Aspect(70, SignalHead.GREEN),
                                     new Aspect(60, SignalHead.FLASHYELLOW),
                                     new Aspect(40, SignalHead.YELLOW),
                                     new Aspect(10, SignalHead.RED) };
        }
        Aspect[] aspects;
    }
}


/* @(#)AspectGenerator.java */
