//  ProgListener.java

package jmri;

/**
 * Interface to be implemented by classes using Programmer.
 * This allows a Programmer object to return delayed status, including
 * the CV value from a read operation.
 *
 * Callbacks are guaranteed to be in the Swing execution thread.
 *
 * @author			Bob Jacobsen  Copyright (C) 2001
 * @version			$Id: ProgListener.java,v 1.6 2002-07-24 05:19:37 jacobsen Exp $
 */
public interface ProgListener extends java.util.EventListener{
	/** Receive a callback at the end of a programming operation.
	 *
	 * @parameter value  Value from a read operation, or value written on a write
	 * @parameter status Denotes the completion code. Note that this is a
	 *                    bitwise combination of the various status coded defined
	 *                    in this interface.
	 */
	public void programmingOpReply(int value, int status);

	/** Constant denoting that the request completed correctly. Note this
	 *  is a specific value; all others are bitwise combinations
	 */
	public final int OK 			= 0;

	/** Constant denoting the request failed, but no specific reason is known */
	public final int UnknownError 	= 1;

	/** Constant denoting that no decoder was detected on the programming track */
	public final int NoLocoDetected = 2;

	/** Constant denoting that the request failed because the decoding hardware
	 * was already busy */
	public final int ProgrammerBusy = 4;

	/** Constant denoting that the request failed because it requested some
	 * unimplemented capability.  Note that this can also result in an
	 * exception during the original request; which happens is implementation
	 * dependent */
	public final int NotImplemented = 8;

	/** Constant denoting that the user (human or software) aborted the request
	 * before completion */
	public final int UserAborted    = 0x10;

	/** Constant denoting there was no acknowledge from the locomotive, so
	 *  the CV may or may not have been written on a write.  No value was read. */
	public final int NoAck			= 0x20;

	/** Constant denoting that confirm failed, likely due to another value being present */
	public final int ConfirmFailed    = 0x40;

	/** Constant denoting that the programming operation timed out */
	public final int FailedTimeout    = 0x80;

}


/* @(#)ProgListener.java */
