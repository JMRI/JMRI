package jmri.jmrix.loconet;

import java.util.Date;

/**
 * LnTrafficListener provides the call-back interface for notification when a
 * new LocoNet message arrives from the layout.
 *<P>
 * In contrast to LocoNetListener this interface defines separate methods to notify
 *  transmitted or received loconet messages. Further, the actual time stamp when a 
 *  message was passed to the hardware interface or was first seen is provided. 
 *  As most functions in JMRI do not depend on the actual time a message was sent
 *  or received, this interface may help in debugging communication. 
 *  Currently the LocoNet Monitor is the only user of this interface. 
 *
 *
 * @author			Matthias Keil  Copyright (C) 2013
 * @version 		$Revision: $
 *
 */
public interface LnTrafficListener {

	public final static int LN_TRAFFIC_NONE  = 0x00;
	public final static int LN_TRAFFIC_RX    = 0x01;
	public final static int LN_TRAFFIC_TX    = 0x02;
	public final static int LN_TRAFFIC_ALL   = 0x03;

	
	public void notifyXmit(Date timestamp, LocoNetMessage m);
	public void notifyRcv(Date timestamp, LocoNetMessage m);
}
