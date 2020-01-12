package jmri.jmrix.zimo;

/**
 * Mx1Interface defines the general connection to a MX-1 layout.
 * <p>
 * Use this interface to send messages to a MX-1 layout. Classes implementing
 * the Mx1Listener interface can register here to receive incoming MX-1 messages
 * as events.
 * <p>
 * The jmri.jrmix.zimo.Mx1TrafficManager provides the first implementation of
 * this interface.
 * <p>
 * How do you locate an implemenation of this interface? That's an interesting
 * question. This is inherently MX-1 specific, so it would be inappropriate to
 * put it in the jmri.InterfaceManager. And Java interfaces can't have static
 * members, so we can't provide an implementation() member. For now, we use a
 * static implementation member in the Mx1TrafficManager implementation to
 * locate _any_ implementation; this clearly needs to be improved.
 * <p>
 * Mx1Listener implementations registering for traffic updates cannot assume
 * that messages will be returned in any particular thread. See the Mx1Listener
 * doc for more background.
 *
 * @author	Bob Jacobsen Copyright (C) 2001, 2002
 * @see jmri.jmrix.zimo.Mx1Listener
 * @see jmri.jmrix.zimo.Mx1TrafficController
 *
 * Adapted by Sip Bosch for use with MX-1
 *
 */
public interface Mx1Interface {

    /*
     * Request a message be sent to the attached MX-1. Return is immediate,
     * with the message being queued for eventual sending.  If you're interested
     * in a reply, you need to register a Mx1Listener object to watch the
     * message stream. When sending, you specify (in 2nd parameter) who
     * you are so you're not redundantly notified of this message.
     */
    public void sendMx1Message(Mx1Message msg, Mx1Listener replyTo);

    /**
     * Request notification of things happening on the MX-1.
     * <p>
     * The same listener can register multiple times with different masks.
     * (Multiple registrations with a single mask value are equivalent to a
     * single registration) Mask values are defined as class constants. Note
     * that these are bit masks, and should be OR'd, not added, if multiple
     * values are desired.
     * <p>
     * The event notification contains the received message as source, not this
     * object, so that we can notify of an incoming message to multiple places
     * and then move on.
     *
     * @param mask     The OR of the key values of messages to be reported (to
     *                 reduce traffic, provide for listeners interested in
     *                 different things)
     *
     * @param listener Object to be notified of new messages as they arrive.
     *
     */
    void addMx1Listener(int mask, Mx1Listener listener);

    /*
     * Stop notification of things happening on the MX-1. Note that mask and Mx1Listener
     * must match a previous request exactly.
     */
    void removeMx1Listener(int mask, Mx1Listener listener);

    /*
     * Check whether an implementation is operational. True indicates OK.
     */
    public boolean status();

    /**
     * Mask value to request notification of all incoming messages
     */
    public static final int ALL = ~0;

    /**
     * Mask value to request notification of messages effecting slot status,
     * including the programming slot
     */
    public static final int SLOTINFO = 1;

    /**
     * Mask value to request notification of messages associated with
     * programming
     */
    public static final int PROGRAMMING = 2;

    /**
     * Mask value to request notification of messages indicating changes in
     * turnout status
     */
    public static final int TURNOUTS = 4;

    /**
     * Mask value to request notification of messages indicating changes in
     * sensor status
     */
    public static final int SENSORS = 8;

    /**
     * Mask value to request notification of messages associated with layout
     * power
     */
    public static final int POWER = 16;

}



