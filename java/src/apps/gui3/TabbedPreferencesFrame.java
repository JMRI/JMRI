// TabbedPreferencesFrame.java

package apps.gui3;

import org.apache.log4j.Logger;

/**
 * Provide access to the various tables via a 
 * listed pane.
 * <P>
 * @author	Kevin Dickerson   Copyright 2010
 * @version $Revision$
 */
public class TabbedPreferencesFrame extends jmri.util.JmriJFrame {
    
    public String getTitle() {
        return jmri.InstanceManager.tabbedPreferencesInstance().getTitle();
    
    }
    public boolean isMultipleInstances() { return true; }

    static boolean init = false;
    static int lastdivider;
    
    public TabbedPreferencesFrame() {
        add(jmri.InstanceManager.tabbedPreferencesInstance());
        addHelpMenu("package.apps.TabbedPreferences", true);
    }
    
    public void gotoPreferenceItem(String item, String sub){
        jmri.InstanceManager.tabbedPreferencesInstance().gotoPreferenceItem(item, sub);
    }
    
    static Logger log = Logger.getLogger(TabbedPreferencesFrame.class.getName());
}
