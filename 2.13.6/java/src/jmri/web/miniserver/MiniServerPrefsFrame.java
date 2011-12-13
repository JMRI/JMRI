package jmri.web.miniserver;

/**
 *	@author Modifications by Steve Todd   Copyright (C) 2011
 *	@version $Revision$
 */

import jmri.util.JmriJFrame;
import java.util.ResourceBundle;


public class MiniServerPrefsFrame extends JmriJFrame{

    static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.MiniServer.MiniServerBundle");

    public MiniServerPrefsFrame(){
        
        this.setModifiedFlag(true);
        
        MiniServerPrefsPanel prefs = new MiniServerPrefsPanel(this);
        prefs.enableSave(); //  Makes save and cancel visible
        getContentPane().add(prefs);

        this.setTitle(rb.getString("TitleMiniServerPreferences"));
        this.pack();
        this.setVisible(true);
    }

    //private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MiniServerPrefsFrame.class.getName());
}
