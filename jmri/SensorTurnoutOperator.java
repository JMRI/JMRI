/**
 * Concrete subclass of TurnoutOperator for a turnout that has sensor feedback.
 * 
 * @author	John Harper	Copyright 2005
 */

package jmri;

import jmri.jmrix.nce.NceSensorManager;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

public class SensorTurnoutOperator extends TurnoutOperator {
	long interval;
	int maxTries;
	int tries = 0;
	PropertyChangeListener listener;
	
	public SensorTurnoutOperator(AbstractTurnout t, long i, int mt) {
		super(t);
		interval = i;
		maxTries = mt;
	}
	
	/**
	 * Do the autmation for a turnout with no feedback. This means try
	 * maxTries times at an interval of interval. Note the call to
	 * operatorCheck each time we're about to actually do something -
	 * if we're no longer the current operator this throws
	 * TurnoutOperatorException which just terminates the thread.
	 */
	public void run() {
		long startTime = System.currentTimeMillis();
		listener = new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent e) {
				if (e.getPropertyName().equals("KnownState")) {
					synchronized(this) {
						this.notify();
					}
				}
			}
		};
		myTurnout.addPropertyChangeListener(listener);
		try {
			operatorCheck();
			myTurnout.forwardCommandChangeToLayout();
			while (++tries < maxTries) {
				long nextTry = System.currentTimeMillis() + interval;
				long remaining;
				while ((remaining = nextTry - System.currentTimeMillis()) > 0) {
					try {
						synchronized(this) {
							wait(remaining);
						}
					} catch (InterruptedException e) { };
				}
				if (myTurnout.isConsistentState()) {
					break;
				}
				operatorCheck();
				myTurnout.forwardCommandChangeToLayout();
				log.warn("retrying "+myTurnout.getSystemName()+", try #"+(tries+1));
			}
			if (!myTurnout.isConsistentState()) {
				log.warn("failed to throw "+myTurnout.getSystemName());
			}
		} catch (TurnoutOperatorException e) { };
		myTurnout.removePropertyChangeListener(listener);
	}
	
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SensorTurnoutOperator.class.getName());
}
