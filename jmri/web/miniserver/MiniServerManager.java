package jmri.web.miniserver;

import java.io.File;
import jmri.jmrit.XmlFile;

/**
 *	@author Modifications by Steve Todd   Copyright (C) 2011
 *	@version $Revision: 1.1 $
 */

public class MiniServerManager {
    
    static private MiniServerManager root;

    private MiniServerPreferences MiniServerPreferences = null;
    
    public MiniServerManager() {
        MiniServerPreferences = new MiniServerPreferences(XmlFile.prefsDir()+ "miniserver" +File.separator+ "MiniServerPreferences.xml");
    }

    static private MiniServerManager instance() {
        if (root==null) root = new MiniServerManager();
        return root;
    }
    
    static public MiniServerPreferences MiniServerPreferencesInstance(){
        return instance().MiniServerPreferences;
    }




}
