/**
 * Concrete subclass of TurnoutOperator for a turnout that has sensor feedback.
 * 
 * @author	John Harper	Copyright 2005
 */

package jmri.implementation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import jmri.*;

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
	 * Do the autmation for a turnout with sensor feedback.
	 * Keep trying up to maxTries until the sensor tells
	 * us the change has actually happened. Note the call to
	 * operatorCheck each time we're about to actually do something -
	 * if we're no longer the current operator this throws
	 * TurnoutOperatorException which just terminates the thread.
	 */
	public void run() {
		//long startTime = System.currentTimeMillis();
		listener = new PropertyChangeListener() {
	        @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="NN_NAKED_NOTIFY",
	                    justification="notify not naked, outside sensor and turnout is shared state")
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
					} catch (InterruptedException e) { 
					    Thread.currentThread().interrupt(); // retain if needed later
					}
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
		} catch (TurnoutOperatorException e) { }
		myTurnout.removePropertyChangeListener(listener);
	}
	
    static Logger log = LoggerFactory.getLogger(SensorTurnoutOperator.class.getName());
}
