package jmri.jmrix.cmri.serial.cmrinetmanager;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

import jmri.jmrix.cmri.CMRISystemConnectionMemo;
/**
 * CMRInet Network Metrics
 * 
 * @author                      Chuck Catania  2016
 * @version
 */
public class CMRInetMetricsAction extends AbstractAction {

    private CMRISystemConnectionMemo _memo = null;

    public CMRInetMetricsAction(String s,CMRISystemConnectionMemo memo){ 
        super(s);
        _memo = memo;
    }

    public CMRInetMetricsAction(CMRISystemConnectionMemo memo) {
        this("CMRInet Network Metrics",memo);
    }

    public void actionPerformed(ActionEvent e) {
		// create a CMRInetMetricsAction
		CMRInetMetricsFrame f = new CMRInetMetricsFrame(_memo);
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
