// SprogVersionFrame.java

package jmri.jmrix.sprog.update;

import org.apache.log4j.Logger;
import javax.swing.*;

/**
 * Get the firmware version of the attached SPROG hardware
 * 
 * @author			Andrew Crosland   Copyright (C) 2008
 * @version			$Revision$
 */
public class SprogVersionFrame extends jmri.util.JmriJFrame implements SprogVersionListener {

    public SprogVersionFrame() {
        super();
    }
    
    synchronized public void initComponents() throws Exception {
        setTitle("SPROG Version");

        // add help menu to window
        addHelpMenu("package.jmri.jmrix.sprog.update.SprogVersionFrame", true);

        // Start the query
	SprogVersionQuery.requestVersion(this);
    }
    
    synchronized public void notifyVersion(SprogVersion v) {
        if (log.isDebugEnabled()) { log.debug("Version "+v.toString()+" notified"); }
        JOptionPane.showMessageDialog(null, v.toString(),
                "SPROG Version", JOptionPane.INFORMATION_MESSAGE);
        setVisible(false);
        dispose();
    }

    static Logger log = Logger.getLogger(SprogVersionFrame.class.getName());
}
