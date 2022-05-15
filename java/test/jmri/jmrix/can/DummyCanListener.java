package jmri.jmrix.can;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Basic CanListener for use in testing.
 * @author Steve Young Copyright (C) 2022
 */
public class DummyCanListener implements CanListener {
    
    private final TrafficController tc;
    private final List<CanMessage> m_list;
    private final List<CanReply> r_list;

    public DummyCanListener(TrafficController tcToAdd) {
        tc = tcToAdd;
        m_list = new CopyOnWriteArrayList<>();
        r_list = new CopyOnWriteArrayList<>();
        addTc(tc);
    }

    /**
     * Trigger for Outgoing CanMessage.
     * @param m Outgoing CanMessage
     */
    @Override
    public void message(CanMessage m) {
        m_list.add(m);
    }

    /**
     * Trigger for Incoming CanReply.
     * @param r Incoming CanReply
     */
    @Override
    public void reply(CanReply r) {
        r_list.add(r);
    }

    /**
     * Get List of outgoing messages.
     * @return list of CanMessage.
     */
    public List<CanMessage> getMessages() {
        return Collections.unmodifiableList(m_list);
    }

    /**
     * Get List of incoming replies.
     * @return list of CanReply.
     */
    public List<CanReply> getReplies() {
        return Collections.unmodifiableList(r_list);
    }

    /**
     * Remove CanListener.
     */
    public void dispose() {
        removeTc(tc);
    }
    
}
