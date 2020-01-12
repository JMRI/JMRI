package jmri.jmrix.zimo;

/**
 * Mx1Listener provides the call-back interface for notification when a new MX-1
 * message arrives from the layout.
 * <p>
 * Note that the Mx1Listener implementation cannot assume that messages will be
 * returned in any particular thread. We may eventually revisit this, as
 * returning messages in the Swing GUI thread would result in some
 * simplification of client code. We've not done that yet because we're not sure
 * that deadlocks can be avoided in that case.
 *
 * @author	Bob Jacobsen Copyright (C) 2002
  *
 * Adapted by Sip Bosch for use with Zimo Mx-1
 */
public interface Mx1Listener extends java.util.EventListener {

    /**
     * Member function that will be invoked by a Mx1Interface implementation to
     * forward a MX-1 message from the layout.
     *
     * @param msg The received MX-1 message. Note that this same object may be
     *            presented to multiple users. It should not be modified here.
     */
    public void message(Mx1Message msg);
}



