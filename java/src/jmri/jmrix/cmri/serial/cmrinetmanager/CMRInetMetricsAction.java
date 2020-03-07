package jmri.jmrix.cmri.serial.cmrinetmanager;

import java.awt.event.ActionEvent;
import jmri.jmrix.cmri.CMRISystemConnectionMemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.AbstractAction;

/**
 * CMRInet Network Metrics
 * 
 * @author  Chuck Catania  2016, 2017
 */
public class CMRInetMetricsAction extends AbstractAction {

	CMRISystemConnectionMemo _memo = null;
        
	public CMRInetMetricsAction(String s,CMRISystemConnectionMemo memo) { 
            super(s);
            _memo = memo;
        }

    public CMRInetMetricsAction(CMRISystemConnectionMemo memo) {
        this(Bundle.getMessage("MetricsWindowTitle"), memo);
    }

        @Override
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

        private final static Logger log = LoggerFactory.getLogger(CMRInetMetricsAction.class.getName());

}
