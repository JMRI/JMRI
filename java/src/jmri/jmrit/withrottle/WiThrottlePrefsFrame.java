package jmri.jmrit.withrottle;

/**
 *	@author Brett Hoffman   Copyright (C) 2010
 *	@version $Revision$
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.util.JmriJFrame;
import java.util.ResourceBundle;


public class WiThrottlePrefsFrame extends JmriJFrame{

    static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.withrottle.WiThrottleBundle");

    public WiThrottlePrefsFrame(){
        
        this.setModifiedFlag(true);
        
        WiThrottlePrefsPanel prefs = new WiThrottlePrefsPanel(this);
        prefs.enableSave(); //  Makes save and cancel visible
        getContentPane().add(prefs);

        this.setTitle(rb.getString("TitleWiThrottlePreferences"));
        this.pack();
        this.setVisible(true);
    }

    //private static Logger log = LoggerFactory.getLogger(WiThrottlePrefsFrame.class.getName());
}
