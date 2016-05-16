// CMRInetMetricsAction.java

package jmri.jmrix.cmri.serial.cmrinetmanager;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

/**
 * CMRInet Network Metrics
 * 
 * @author                      Chuck Catania  2016
 * @version
 */
public class CMRInetMetricsAction extends AbstractAction {

	public CMRInetMetricsAction(String s) { super(s);}

    public CMRInetMetricsAction() {
        this("CMRInet Network Metrics");
    }

    public void actionPerformed(ActionEvent e) {
		// create a CMRInetMetricsAction
		CMRInetMetricsFrame f = new CMRInetMetricsFrame();
		try {
			f.initComponents();
			}
		catch (Exception ex) {
			log.warn("SerialFilterAction starting CMRInetMetricsAction: Exception: "+ex.toString());
			}
		f.setVisible(true);
	}

	static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(CMRInetMetricsAction.class.getName());

}


/* @(#)CMRInetStatsAction.java */
