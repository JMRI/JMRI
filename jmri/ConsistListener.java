//  ConsistListener.java

package jmri;

/**
 * Allow notification of delayed consisting errors.
 * <P>
 * This allows a {@link Consist} object to return delayed status.
 *
 * @author			Paul Bender  Copyright (C) 2004
 * @version			$Revision: 1.1 $
 */
public interface ConsistListener extends java.util.EventListener{

	/** Receive notification at the end of a consisting operation.
	 *
	 * @param locoaddress  Address of specific locomotive involved, if 
         *                     error is locomotive specific.
	 * @param status Denotes the completion code. Note that this is a
	 *                    bitwise combination of the various status coded defined
	 *                    in this interface.
	 */
	public void consistReply(int locoaddress, int status);

	/** Constant denoting that the request completed correctly. Note this
	 *  is a specific value; all others are bitwise combinations
	 */
	public final int OK 			= 0;

	/** 
	  * All of the slots available for the consist are full
	  */
	public final int CONSIST_FULL		= 1;

	/** Constant denoting that the request failed because it requested some
	 * unimplemented capability.  Note that this can also result in an
	 * exception during the original request; which happens is implementation
	 * dependent */
	public final int NotImplemented = 8;

}


/* @(#)ProgListener.java */
