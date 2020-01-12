package jmri.jmrix.lenz;

/**
 * XNetListener provides the call-back interface for notification when a new
 * XNet message arrives from the layout.
 * <p>
 * Note that the XNetListener implementation cannot assume that messages will be
 * returned in any particular thread. We may eventually revisit this, as
 * returning messages in the Swing GUI thread would result in some
 * simplification of client code. We've not done that yet because we're not sure
 * that deadlocks can be avoided in that case.
 *
 * @author Bob Jacobsen Copyright (C) 2002
 * @author Bob Jacobsen Copyright (C) 2010
 */
public interface XNetListener extends jmri.jmrix.AbstractMRListener {

    /**
     * Member function that will be invoked by an XNetInterface implementation to
     * forward an XNet message from the layout.
     *
     * @param msg The received XNet message. Note that this same object may be
     *            presented to multiple users. It should not be modified here.
     */
    public void message(XNetReply msg);

    /**
     * Member function that will be invoked by an XNetInterface implementation to
     * forward an XNet message sent to the layout. Normally, this function will
     * do nothing.
     *
     * @param msg The received XNet message. Note that this same object may be
     *            presented to multiple users. It should not be modified here.
     */
    public void message(XNetMessage msg);

    /**
     * Member function invoked by an XNetInterface implementation to notify a
     * sender that an outgoing message timed out and was dropped from the
     * queue.
     */
    public void notifyTimeout(XNetMessage msg);

}

