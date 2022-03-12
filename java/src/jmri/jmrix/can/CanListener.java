package jmri.jmrix.can;

/**
 * Defines the interface for listening to CAN messages
 *
 * @author Andrew Crosland Copyright (C) 2008
 */
public interface CanListener extends jmri.jmrix.AbstractMRListener {

    public void message(CanMessage m);

    public void reply(CanReply m);
    
    /**
     * Add a Traffic Controller Listener.
     * Adding here, rather than in a class construction header
     * avoids Leaking Constructor errors.
     * @param tcToAdd The system memo CAN Traffic Controller
     */
    public default void addTc(TrafficController tcToAdd) {
        if (tcToAdd != null) {
            tcToAdd.addCanListener(this);
        }
    }
    
    /**
     * Add a Traffic Controller Listener.
     * Adding here, rather than in a class construction header
     * avoids Leaking Constructor errors.
     * @param memoToAdd The CAN system Connection
     */
    public default void addTc(CanSystemConnectionMemo memoToAdd) {
        if (memoToAdd != null) {
            addTc(memoToAdd.getTrafficController());
        }
    }
    
    /**
     * Remove a Traffic Controller Listener.
     * @param tcToRemove The system memo CAN Traffic Controller
     */
    public default void removeTc(TrafficController tcToRemove) {
        if (tcToRemove != null) {
            tcToRemove.removeCanListener(this);
        }
    }
    
    /**
     * Remove a Traffic Controller Listener.
     * @param memoToRemove The CAN system Connection
     */
    public default void removeTc(CanSystemConnectionMemo memoToRemove) {
        if (memoToRemove != null) {
            removeTc(memoToRemove.getTrafficController());
        }
    }
}
