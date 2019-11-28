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
    default void addTc(TrafficController tcToAdd) {
        if (tcToAdd != null) {
            tcToAdd.addCanListener(this);
        }
    }
}
