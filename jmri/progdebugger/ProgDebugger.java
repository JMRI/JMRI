/** 
 * ProgDebugger.java
 *
 * Description:		debugging implementation of Programmer
 * @author			Bob Jacobsen Copyright (C) 2001
 * @version			
 */

package jmri.progdebugger;

import jmri.ProgListener;
import jmri.Programmer;
import jmri.ProgrammerException;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.Vector;

public class ProgDebugger implements Programmer  {

	// write CV
	private int _lastWrite = -1;
	public int lastWrite() { return _lastWrite; }
	public void writeCV(int CV, int val, ProgListener p) throws ProgrammerException
	{
		final ProgListener m = p;
		// log out the request
		log.debug("write CV: "+CV+" to: "+val+" mode: "+getMode());
		_lastWrite = val;
		// return a notification via the queue to ensure end
		Runnable r = new Runnable() {
			ProgListener l = m;
			public void run() { l.programmingOpReply(-1, 0); }  // 0 is OK status
			};
		javax.swing.SwingUtilities.invokeLater(r);
	}
	
	// read CV
	private int _nextRead = 123;
	public void nextRead(int r) { _nextRead = r; }
	public void readCV(int CV, ProgListener p) throws ProgrammerException {
		final ProgListener m = p;
		log.debug("read CV: "+CV+" mode: "+getMode()+" will read "+_nextRead);
		// return a notification via the queue to ensure end
		Runnable r = new Runnable() {
			ProgListener l = m;
			public void run() { l.programmingOpReply(_nextRead, 0); }  // 0 is OK status
			};
		javax.swing.SwingUtilities.invokeLater(r);

	}
	

	// handle mode
	protected int _mode = 0;
	
	public void setMode(int mode) {
		log.info("setMode: old="+_mode+" new="+mode);
		if (mode != _mode) {
			notifyPropertyChange("Mode", _mode, mode);
			_mode = mode;
		}
	}
	public int getMode() { return _mode; }
	
	// data members to hold contact with the property listeners
	private Vector propListeners = new Vector();
	
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

	protected void notifyPropertyChange(String name, int oldval, int newval) {
		// make a copy of the listener vector to synchronized not needed for transmit
		Vector v;
		synchronized(this)
			{
				v = (Vector) propListeners.clone();
			}
		// forward to all listeners
		int cnt = v.size();
		for (int i=0; i < cnt; i++) {
			PropertyChangeListener client = (PropertyChangeListener) v.elementAt(i);
			client.propertyChange(new PropertyChangeEvent(this, name, new Integer(oldval), new Integer(newval)));
						}
	}

	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(ProgDebugger.class.getName());
}


/* @(#)ProgDebugger.java */
