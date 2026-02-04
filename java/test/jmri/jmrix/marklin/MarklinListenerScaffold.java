package jmri.jmrix.marklin;

import java.util.*;

/**
 * Class for using in testing the MarklinListener interface.
 * @author Steve Young Copyright (C) 2024
 */
public class MarklinListenerScaffold implements MarklinListener {

    private final List<MarklinMessage> mList;
    private final List<MarklinReply> rList;

    public MarklinListenerScaffold() {
        mList = Collections.synchronizedList(new ArrayList<>());
        rList = Collections.synchronizedList(new ArrayList<>());
    }

    @Override
    public void message(MarklinMessage m) {
        mList.add(m);
    }

    @Override
    public void reply(MarklinReply r) {
        rList.add(r);
    }

    /**
     * Get a list of Sent Messages.
     * @return an unmodifiable list of sent messages.
     */
    @javax.annotation.Nonnull
    public List<MarklinMessage> getMarklinMessageList() {
        return Collections.unmodifiableList(mList);
    }

    /**
     * Get a list of Replies.
     * @return an unmodifiable list of replies received.
     */
    @javax.annotation.Nonnull
    public List<MarklinReply> getMarklinReplyList() {
        return Collections.unmodifiableList(rList);
    }

}
