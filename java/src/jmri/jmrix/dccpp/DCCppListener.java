package jmri.jmrix.dccpp;

/**
 * DCCppListener provides the call-back interface for notification when a new
 * DCC++ message arrives from the layout.
 * <p>
 * Note that the DCCppListener implementation cannot assume that messages will be
 * returned in any particular thread. We may eventually revisit this, as
 * returning messages in the Swing GUI thread would result in some
 * simplification of client code. We've not done that yet because we're not sure
 * that deadlocks can be avoided in that case.
 *
 * @author Bob Jacobsen Copyright (C) 2002
 * @author Bob Jacobsen Copyright (C) 2010
 * @author Mark Underwood Copyright (C) 2015
  *
 * Based on XNetListener by Bob Jacobsen
 */
public interface DCCppListener extends jmri.jmrix.AbstractMRListener {

    /**
     * Member function that will be invoked by a DCCppInterface implementation to
     * forward a DCC++ message from the layout.
     *
     * @param msg The received DCC++ message. Note that this same object may be
     *            presented to multiple users. It should not be modified here.
     */
    public void message(DCCppReply msg);

    /**
     * Member function that will be invoked by a DCCppInterface implementation to
     * forward a DCC++ message sent to the layout. Normally, this function will
     * do nothing.
     *
     * @param msg The received DCC++ message. Note that this same object may be
     *            presented to multiple users. It should not be modified here.
     */
    public void message(DCCppMessage msg);

    /**
     * Member function invoked by an DCCppInterface implementation to notify a
     * sender that an outgoing message timed out and was dropped from the
     * queue.
     */
    public void notifyTimeout(DCCppMessage msg);

}

