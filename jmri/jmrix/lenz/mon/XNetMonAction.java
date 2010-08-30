/**
 * XNetMonAction.java
 *
 * Description:		Swing action to create and register a
 *       			XNetMonFrame object
 *
 * @author			Bob Jacobsen    Copyright (C) 2002
 * @author			Paul Bender     Copyright (C) 2008
 * @version         $Revision: 2.5 $
 */

package jmri.jmrix.lenz.mon;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;


public class XNetMonAction extends AbstractAction {

    private jmri.jmrix.lenz.XNetSystemConnectionMemo _memo;

    public XNetMonAction(String s,jmri.jmrix.lenz.XNetSystemConnectionMemo memo) { 
       super(s);
       _memo = memo;
    }

    public XNetMonAction(jmri.jmrix.lenz.XNetSystemConnectionMemo memo){
	this("XPressNet Monitor",memo);
    }

    public void actionPerformed(ActionEvent e) {
		// create a XNetMonFrame
		XNetMonFrame f = new XNetMonFrame(_memo);
		try {
			f.initComponents();
			}
		catch (Exception ex) {
			log.warn("XNetMonAction starting XNetMonFrame: Exception: "+ex.toString());
			}
		f.setVisible(true);

	}

	static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(XNetMonAction.class.getName());

}


/* @(#)XNetMonAction.java */
