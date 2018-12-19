package jmri.implementation;

import jmri.TurnoutOperator;

/**
 * Concrete subclass of TurnoutOperator for a turnout that has no feedback.
 *
 * @author John Harper Copyright 2005
 */
public class NoFeedbackTurnoutOperator extends TurnoutOperator {

    long interval;
    int maxTries;
    int tries = 0;

    public NoFeedbackTurnoutOperator(AbstractTurnout t, long i, int mt) {
        super(t);
        interval = i;
        maxTries = mt;
    }

    /**
     * Do the automation for a turnout with no feedback. This means try maxTries
     * times at an interval of interval. Note the call to operatorCheck each
     * time we're about to actually do something - if we're no longer the
     * current operator this throws TurnoutOperatorException which just
     * terminates the thread.
     */
    @Override
    public void run() {
        try {
            operatorCheck();
            myTurnout.forwardCommandChangeToLayout();
            while (++tries < maxTries) {
                try {
                    Thread.sleep(interval);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // retain if needed later
                }
                operatorCheck();
                myTurnout.forwardCommandChangeToLayout();
            }
            myTurnout.setKnownStateToCommanded();
        } catch (TurnoutOperatorException e) {
        }
    }

}
