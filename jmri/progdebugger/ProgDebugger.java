/** 
 * ProgDebugger.java
 *
 * Description:		debugging implementation of Programmer
 * @author			Bob Jacobsen Copyright (C) 2001
 * @version			
 */

package jmri.progdebugger;

import ErrLoggerJ.ErrLog;
import jmri.ProgListener;
import jmri.Programmer;
import jmri.ProgrammerException;

public class ProgDebugger implements Programmer  {

	// write CV
	public void writeCV(int CV, int val, int mode, ProgListener p) throws ProgrammerException
	{
		final ProgListener m = p;
		// log out the request
		ErrLog.msg(ErrLog.routine,"ProgDebugger", "writeCV", "write CV: "+CV+" to: "+val+" mode: "+mode);
		// return a notification via the queue to ensure end
		Runnable r = new Runnable() {
			ProgListener l = m;
			public void run() { l.programmingOpReply(-1, 0); }  // 0 is OK status
			};
		javax.swing.SwingUtilities.invokeLater(r);
	}
	
	// read CV
	public void readCV(int CV, int mode, ProgListener p) throws ProgrammerException {
		final ProgListener m = p;
		ErrLog.msg(ErrLog.routine,"ProgDebugger", "readCV", "read CV: "+CV+" mode: "+mode);
		// return a notification via the queue to ensure end
		Runnable r = new Runnable() {
			ProgListener l = m;
			public void run() { l.programmingOpReply(123, 0); }  // 0 is OK status
			};
		javax.swing.SwingUtilities.invokeLater(r);

	}
}


/* @(#)ProgDebugger.java */
