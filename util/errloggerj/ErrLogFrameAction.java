/** 
 * ErrLogFrameAction.java
 *
 * Description:		Swing action to create and register a 
 *       			ErrLogFrame object
 *
 * @author			Bob Jacobsen    Copyright (C) 2001
 * @version			
 */

package ErrLoggerJ;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

public class ErrLogFrameAction			extends AbstractAction {

	public ErrLogFrameAction(String s) { super(s);}
	
    public void actionPerformed(ActionEvent e) {
		// create a MS100Frame
		ErrLogFrame f = new ErrLogFrame();
		try {
			f.initComponents();
			}
		catch (Exception ex) {
			ErrLog.msg(ErrLog.error, "ErrLogFrameAction","starting ErrLogFrame:", "Exception: "+ex.toString());
			}
		f.show();	
	};

}


/* @(#)LnHexFileAction.java */
