// LocoNetInterface

package jmri.jmrix.loconet;

/** 
 * LocoNetInterface defines the general connection to a LocoNet layout.
 * <P>
 * Use this interface to send messages to a LocoNet layout.
 * Classes implementing the LocoNetListener interface can register
 * here to receive incoming LocoNet messages as events.
 *
 * @author			Bob Jacobsen Copyright (C) 2001
 * 
 * @see jmri.jrmix.LocoNetListener
 *
 */
public interface LocoNetInterface {

	/*
	 * Request a message be sent to the attached LocoNet
	 */
	public void sendLocoNetMessage(LocoNetMessage msg);

	/**
	 * Request notification of things happening on the LocoNet. 
	 *<P>
	 * The same listener
	 * can register multiple times with different masks.  (Multiple registrations with
	 * a single mask value are equivalent to a single registration)
	 * Mask values are defined as class constants.  Note that these are bit masks,
	 * and should be OR'd, not added, if multiple values are desired.
	 *<P>
	 * The event notification contains the received message as source, not this object,
 	 * so that we can notify of an incoming message to multiple places and then move on.
	 *
	 * @param mask The OR of the key values of messages to be reported (to reduce traffic,
	 *             provide for listeners interested in different things)
	 *
	 * @param listener Object to be notified of new messages as they arrive.
	 *
	 */
	void addLocoNetListener(int mask, LocoNetListener l);

	/*
	 * Stop notification of things happening on the LocoNet. Note that mask and LocoNetListener
	 * must match a previous request exactly.
	 */
	void removeLocoNetListener(int mask, LocoNetListener listener);

	/*
	 * Check whether an implementation is operational. True indicates OK.
	 */
	public boolean status();

	/**
	 * Mask value to request notification of all incoming messages
	 */
	public static final int ALL				=  ~0;

	/**
	 * Mask value to request notification of messages effecting slot status, including the programming slot
	 */
	public static final int SLOTINFO		=   1;

	/**
	 * Mask value to request notification of messages associated with programming
	 */
	public static final int PROGRAMMING		=   2;

	/**
	 * Mask value to request notification of messages indicating changes in turnout status
	 */
	public static final int TURNOUTS		=   4;

	/**
	 * Mask value to request notification of messages indicating changes in sensor status
	 */
	public static final int SENSORS			=   8;

	/**
	 * Mask value to request notification of messages associated with layout power
	 */
	public static final int POWER			=  16;

}


/* @(#)LocoNetInterface.java */
