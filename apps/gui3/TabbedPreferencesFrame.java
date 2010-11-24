// TabbedPreferencesFrame.java

package apps.gui3;

/**
 * Provide access to the various tables via a 
 * listed pane.
 * <P>
 * @author	Kevin Dickerson   Copyright 2010
 * @version $Revision: 1.1 $
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
    }
    
    public void gotoPreferenceItem(String item){
        jmri.InstanceManager.tabbedPreferencesInstance().gotoPreferenceItem(item);
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TabbedPreferencesFrame.class.getName());
}