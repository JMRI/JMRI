/** 
 * AbstractProgrammer.java
 *
 * Common implementations for the Programmer interface
 *
 * @author			Bob Jacobsen  Copyright (C) 2001
 * @version			
 */
 
 // Convert the jmri.Programmer interface into commands for the NCE powerstation

package jmri.jmrix;

import jmri.Programmer;
import jmri.ProgListener;

import java.util.Vector;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

public abstract class AbstractProgrammer implements Programmer {

	public String decodeErrorCode(int code) {
		if (code == ProgListener.OK) return "OK";
		StringBuffer sbuf = new StringBuffer("");
		// add each code; terminate each string with "; " please.
		if ((code & ProgListener.NoLocoDetected) != 0) sbuf.append("no locomotive detected; ");
		if ((code & ProgListener.ProgrammerBusy) != 0) sbuf.append("programmer busy; ");
		if ((code & ProgListener.NotImplemented) != 0) sbuf.append("requested capability not implemented; ");
		if ((code & ProgListener.UserAborted) != 0) sbuf.append("aborted by user; ");
		if ((code & ProgListener.ConfirmFailed) != 0) sbuf.append("confirm failed; ");
		if ((code & ProgListener.FailedTimeout) != 0) sbuf.append("timeout talking to command station; ");
		if ((code & ProgListener.UnknownError) != 0) sbuf.append("Unknown error; ");
		if ((code & ProgListener.NoAck) != 0) sbuf.append("No acknowledge from locomotive; ");

		// remove trailing separators
		if (sbuf.length() > 2) sbuf.setLength(sbuf.length()-2);
		
		String retval = sbuf.toString();
		if (retval.equals("")) 
			return "unknown status code: "+code;
		else return retval;
	}

	// data members to hold contact with the property listeners
	protected Vector propListeners = new Vector();
	
	public synchronized void addPropertyChangeListener(PropertyChangeListener l) {
		// add only if not already registered
		if (!propListeners.contains(l)) {
			propListeners.addElement(l);
		}
	}

	public synchronized void removePropertyChangeListener(PropertyChangeListener l) {
		if (propListeners.contains(l)) {
			propListeners.removeElement(l);
		}
	}
		
	/**
	 * Internal routine to start timer to protect the mode-change.
	 */
	protected void startShortTimer() {
		restartTimer(SHORT_TIMEOUT);
	}
	
	/**
	 * Internal routine to restart timer with a long delay
	 */
	protected void startLongTimer() {
		restartTimer(LONG_TIMEOUT);
	}
	
	/**
	 * Internal routine to stop timer, as all is well
	 */
	protected void stopTimer() {
		if (timer!=null) timer.stop();
	}

	/**
	 * Internal routine to handle timer starts & restarts
	 */
	protected void restartTimer(int delay) {
		if (timer==null) {
			timer = new javax.swing.Timer(delay, new java.awt.event.ActionListener() {
						public void actionPerformed(java.awt.event.ActionEvent e) {
							timeout();
						}
					});
		}
		timer.stop();
		timer.setInitialDelay(delay);
		timer.setRepeats(false);
		timer.start();
	}
	
	/**
	 * Internal routine to handle a timeout, should be synchronized!
	 */
	abstract protected void timeout();
	
	static private int SHORT_TIMEOUT=2000;
	static private int LONG_TIMEOUT=60000;
	
	javax.swing.Timer timer = null;
	
	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(AbstractProgrammer.class.getName());

}


/* @(#)NceProgrammer.java */
