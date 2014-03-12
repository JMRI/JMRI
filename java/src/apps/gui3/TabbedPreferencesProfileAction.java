package apps.gui3;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.event.ActionEvent;
import javax.swing.SwingUtilities;
import jmri.util.swing.JmriPanel;
import jmri.util.swing.WindowInterface;
import javax.swing.Icon;

    /**
     * Tabbed Preferences Action for going direct to Profiles
     * <P>
     * @author	Bob Jacobsen (C) 2014
     * @version	$Revision$
     */

public class TabbedPreferencesProfileAction extends TabbedPreferencesAction {

    public TabbedPreferencesProfileAction() { 
        super(Bundle.getMessage("MenuItemPreferencesProfile"));
        //preferencesItem = jmri.profile.Bundle.getMessage("ProfilePreferencesPanel.enabledPanel.TabConstraints.tabTitle");
        preferencesItem = "Profiles";
    }
            
    static Logger log = LoggerFactory.getLogger(TabbedPreferencesProfileAction.class.getName());
    
}
