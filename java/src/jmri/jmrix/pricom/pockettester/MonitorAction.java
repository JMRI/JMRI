// pricom.pockettester.MonitorAction.java

package jmri.jmrix.pricom.pockettester;

import org.apache.log4j.Logger;
import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

/**
 * Swing action to create and register a
 * pricom.pockettester.MonitorAction object
 *
 * @see jmri.jmrix.pricom.pockettester.MonitorFrame
 *
 * @author			Bob Jacobsen    Copyright (C) 2002,2004
 * @version			$Revision$
 */
public abstract class MonitorAction extends AbstractAction  {

    public MonitorAction(String s) { super(s);}
    public MonitorAction() {
        java.util.ResourceBundle rb 
            = java.util.ResourceBundle.getBundle("jmri.jmrix.pricom.pockettester.TesterBundle");
        putValue(javax.swing.Action.NAME, rb.getString("ActionMonitor"));
    }
        

    public void actionPerformed(ActionEvent e) {
		MonitorFrame f = new MonitorFrame();
		try {
			f.initComponents();
			}
		catch (Exception ex) {
			log.error("starting MonitorFrame caught exception: "+ex.toString());
			}
	    connect(f);
		f.setVisible(true);
	}

    abstract void connect(DataListener l);
    
    static Logger log = Logger.getLogger(MonitorAction.class.getName());

}


/* @(#)MonitorAction.java */
