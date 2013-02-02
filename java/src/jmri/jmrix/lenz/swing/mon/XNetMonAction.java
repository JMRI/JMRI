/**
 * XNetMonAction.java
 *
 * Description:		Swing action to create and register a
 *       			XNetMonFrame object
 *
 * @author			Bob Jacobsen    Copyright (C) 2002
 * @author			Paul Bender     Copyright (C) 2008
 * @version         $Revision$
 */

package jmri.jmrix.lenz.swing.mon;

import org.apache.log4j.Logger;
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

    public XNetMonAction(String s) {
         super(s);
         // If there is no system memo given, assume the system memo
         // is the first one in the instance list.
         _memo=(jmri.jmrix.lenz.XNetSystemConnectionMemo)(jmri.InstanceManager.
               getList(jmri.jmrix.lenz.XNetSystemConnectionMemo.class).get(0));
    }

    public XNetMonAction() {
         this("XPressNet Monitor");
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

	static Logger log = Logger.getLogger(XNetMonAction.class.getName());

}


/* @(#)XNetMonAction.java */
