// ClockMonAction.java

package jmri.jmrix.nce.clockmon;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;


/**
 * Create and register a NceClockMonitorFrame object.
 *
 * @author			Ken Cameron    Copyright (C) 2007
 * @version			$Revision: 1.1 $
 *
 * based on LocoNet.ClockMonAction by Bob Jacobsen Copyright (C) 2003
 */
public class ClockMonAction extends AbstractAction {

    public ClockMonAction(String s) { super(s);}
    
    public ClockMonAction() {
        this("NCE clock monitor");
    }

    ClockMonFrame f = null;
    
    public void actionPerformed(ActionEvent e) {
        if (f == null) {
            f = new ClockMonFrame();
            try {
                f.initComponents();
                }
            catch (Exception ex) {
                log.error("Exception: "+ex.toString());
                }
        }
        
        // in any case, make it visible
        f.setVisible(true);
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(ClockMonAction.class.getName());

}

/* @(#)ClockMonAction.java */
