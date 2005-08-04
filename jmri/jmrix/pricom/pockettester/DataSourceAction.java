// pricom.pockettester.DataSourceAction.java

package jmri.jmrix.pricom.pockettester;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

/**
 * Action to create and register a
 * pricom.pockettester.DataSource object
 *
 * @see jmri.jmrix.pricom.pockettester.DataSourceFrame
 *
 * @author			Bob Jacobsen    Copyright (C) 2002,2005
 * @version			$Revision: 1.1 $
 */
public class DataSourceAction extends AbstractAction  {

    public DataSourceAction(String s) { super(s);}
    public DataSourceAction() {
        java.util.ResourceBundle rb 
            = java.util.ResourceBundle.getBundle("jmri.jmrix.pricom.pockettester.TesterBundle");
        putValue(javax.swing.Action.NAME, rb.getString("ActionSource"));
    }
        

    public void actionPerformed(ActionEvent e) {
		// create a SerialDriverFrame
		DataSource f = new DataSource();
		try {
			f.init();
			}
		catch (Exception ex) {
			log.error("starting DataSource caught exception: "+ex.toString());
			}
		f.show();
	};

   static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(DataSourceAction.class.getName());

}


/* @(#)DataSourceAction.java */
