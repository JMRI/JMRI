package jmri.jmrix.loconet;

import java.util.Date;

/**
 * LnTrafficListenerFilter is a helper class used to suppress notifications a client
 * is not interested in.
 *
 * @author			Matthias Keil  Copyright (C) 2013
 * @version 		$Revision: $
 *
 */
public class LnTrafficListenerFilter {

	/* Overridden to compare the listener, not the filter objects.
	 *  
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		boolean result = true;
		if (!super.equals(obj)) {
			if (!l.equals(((LnTrafficListenerFilter)obj).l)) {
				result = false;
			}
		}
		return result;
	}

	/**
	 * Type of traffic the listener is interested in.
	 */
	int mask = 0;

	/**
	 * The listener.
	 */
	LnTrafficListener l = null;

	/** 
	 * Hide default constructor 
	 */
	@SuppressWarnings("unused")
	private LnTrafficListenerFilter() {}

	/**
	 * Constructor
	 * @param _mask Type of traffic the listener is interested in. 
	 * @param _l The listener interface.
	 */
	public LnTrafficListenerFilter(int _mask, LnTrafficListener _l) {
		mask = _mask;
		l = _l;
	}
	
	public void setFilter(int _mask) {
		mask = _mask;
	}

	public void fireXmit(Date timestamp, LocoNetMessage m) {
		if ((mask & LnTrafficListener.LN_TRAFFIC_TX) != 0) {
			l.notifyXmit(timestamp, m);
		}
	}

	public void fireRcv(Date timestamp, LocoNetMessage m) {
		if ((mask & LnTrafficListener.LN_TRAFFIC_RX) != 0) {
			l.notifyRcv(timestamp, m);
		}
	}
}
