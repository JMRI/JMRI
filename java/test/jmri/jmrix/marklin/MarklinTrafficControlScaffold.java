package jmri.jmrix.marklin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * TrafficControlScaffold for use in Marklin test classes.
 * @author Steve Young Copyright (C) 2024
 */
public class MarklinTrafficControlScaffold extends jmri.jmrix.marklin.simulation.MarklinSimTrafficController {

    private final List<MarklinMessage> list;

    /**
     * Create a new MarklinTrafficControlScaffold.
     */
    public MarklinTrafficControlScaffold(){
        list = Collections.synchronizedList(new ArrayList<>());
    }

    @Override
    public void sendMarklinMessage(MarklinMessage m, MarklinListener l ) {
        list.add(m);
        super.sendMarklinMessage(m, l);
    }

    /**
     * Get a list of Sent Messages.
     * @return an unmodifiable list of sent messages.
     */
    @javax.annotation.Nonnull
    public List<MarklinMessage> getSentMessages() {
        return Collections.unmodifiableList(list);
    }

    /**
     * Get the most recent outgoing message heard by the Traffic Controller.
     * @return null if no messages sent.
     */
    @javax.annotation.CheckForNull
    public MarklinMessage getLastMessageSent() {
        if ( !list.isEmpty() ) {
            return list.get(list.size()-1);
        }
        return null;
    }

    public int getNumberListeners() {
        return cmdListeners.size();
    }

}
