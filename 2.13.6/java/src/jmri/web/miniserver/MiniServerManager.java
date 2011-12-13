package jmri.web.miniserver;

import java.io.File;
import jmri.jmrit.XmlFile;

/**
 *	@author Modifications by Steve Todd   Copyright (C) 2011
 *	@version $Revision$
 */

public class MiniServerManager {
    
    static private MiniServerManager root;

    private MiniServerPreferences MiniServerPreferences = null;
    
    public MiniServerManager() {
        if(jmri.InstanceManager.getDefault(jmri.web.miniserver.MiniServerPreferences.class)==null){
            jmri.InstanceManager.store(new MiniServerPreferences(XmlFile.prefsDir()+ "miniserver" +File.separator+ "MiniServerPreferences.xml"),jmri.web.miniserver.MiniServerPreferences.class);
        }
        MiniServerPreferences = jmri.InstanceManager.getDefault(jmri.web.miniserver.MiniServerPreferences.class);
    }

    static private MiniServerManager instance() {
        if (root==null) root = new MiniServerManager();
        return root;
    }
    
    static public MiniServerPreferences miniServerPreferencesInstance(){
        return instance().MiniServerPreferences;
    }




}
