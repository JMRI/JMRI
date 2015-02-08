// TabbedPreferencesFrame.java
package apps.gui3;

import jmri.InstanceManager;
import jmri.util.JmriJFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide access to the various tables via a listed pane.
 * <P>
 * @author	Kevin Dickerson Copyright 2010
 * @version $Revision$
 */
public class TabbedPreferencesFrame extends JmriJFrame {

    private static final long serialVersionUID = 4861869203791661041L;

    @Override
    public String getTitle() {
        return InstanceManager.tabbedPreferencesInstance().getTitle();

    }

    public boolean isMultipleInstances() {
        return true;
    }

    static boolean init = false;
    static int lastdivider;

    public TabbedPreferencesFrame() {
        add(InstanceManager.tabbedPreferencesInstance());
        addHelpMenu("package.apps.TabbedPreferences", true);
    }

    public void gotoPreferenceItem(String item, String sub) {
        InstanceManager.tabbedPreferencesInstance().gotoPreferenceItem(item, sub);
    }

    static Logger log = LoggerFactory.getLogger(TabbedPreferencesFrame.class);
}
