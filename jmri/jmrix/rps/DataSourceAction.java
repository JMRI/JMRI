// rps.DataSourceAction.java

package jmri.jmrix.rps;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

/**
 * Action to create and register a
 * rps.DataSource object
 *
 * @see jmri.jmrix.rps.DataSource
 *
 * @author			Bob Jacobsen    Copyright (C) 2002,2005
 * @version			$Revision: 1.1 $
 */
public class DataSourceAction extends AbstractAction  {

    public DataSourceAction(String s) { super(s);}
    public DataSourceAction() {
        java.util.ResourceBundle rb 
            = java.util.ResourceBundle.getBundle("jmri.jmrix.rps.RpsBundle");
        putValue(javax.swing.Action.NAME, rb.getString("ActionSource"));
    }
        
    DataSource f = null;
    
    public void actionPerformed(ActionEvent e) {
		// create a SerialDriverFrame
		if (f==null) {
		    f = new DataSource();
            try {
                f.init();
                }
            catch (Exception ex) {
                log.error("starting DataSource caught exception: "+ex.toString());
                }
		}
		f.setVisible(true);
	};

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(DataSourceAction.class.getName());

    public void startup(String port) throws jmri.jmrix.SerialConfigException {
        actionPerformed(null);
        
        // select port and open
        f.portBox.setSelectedItem(port);
        f.openPortButtonActionPerformed(null);
        
        // start polling
        //f.poll.setSelected(true);
    }
}


/* @(#)DataSourceAction.java */
