/**
 * Concrete subclass of TurnoutOperator for a turnout that has sensor feedback.
 *
 * @author John Harper Copyright 2005
 */
package jmri.implementation;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import jmri.TurnoutOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
     * Do the autmation for a turnout with sensor feedback. Keep trying up to
     * maxTries until the sensor tells us the change has actually happened. Note
     * the call to operatorCheck each time we're about to actually do something
     * - if we're no longer the current operator this throws
     * TurnoutOperatorException which just terminates the thread.
     */
    @Override
    public void run() {
        //long startTime = System.currentTimeMillis();
        listener = new PropertyChangeListener() {
            @SuppressFBWarnings(value = "NN_NAKED_NOTIFY",
                    justification = "notify not naked, outside sensor and turnout is shared state")
            @Override
            public void propertyChange(PropertyChangeEvent e) {
                if (e.getPropertyName().equals("KnownState")) {
                    synchronized (this) {
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
                        synchronized (this) {
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
                log.warn("retrying " + myTurnout.getSystemName() + ", try #" + (tries + 1));
            }
            if (!myTurnout.isConsistentState()) {
                log.warn("failed to throw " + myTurnout.getSystemName());
            }
        } catch (TurnoutOperatorException e) {
        }
        myTurnout.removePropertyChangeListener(listener);
    }

    private final static Logger log = LoggerFactory.getLogger(SensorTurnoutOperator.class);
}
