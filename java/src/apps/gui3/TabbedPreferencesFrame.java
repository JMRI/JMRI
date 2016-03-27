// TabbedPreferencesFrame.java
package apps.gui3;

import java.awt.event.WindowEvent;
import javax.swing.JOptionPane;
import javax.swing.WindowConstants;
import jmri.InstanceManager;
import jmri.util.JmriJFrame;

/**
 * Provide access to the various tables via a listed pane.
 * <P>
 * @author	Kevin Dickerson Copyright 2010
 * @version $Revision$
 */
public class TabbedPreferencesFrame extends JmriJFrame {

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
        this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    }

    public void gotoPreferenceItem(String item, String sub) {
        InstanceManager.tabbedPreferencesInstance().gotoPreferenceItem(item, sub);
    }

    @Override
    public void windowClosing(WindowEvent e) {
        if (InstanceManager.tabbedPreferencesInstance().isDirty()) {
            switch (JOptionPane.showConfirmDialog(this,
                    Bundle.getMessage("UnsavedChangesMessage", InstanceManager.tabbedPreferencesInstance().getTitle()), // NOI18N
                    Bundle.getMessage("UnsavedChangesTitle"), // NOI18N
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE)) {
                case JOptionPane.YES_OPTION:
                    // save preferences
                    InstanceManager.tabbedPreferencesInstance().savePressed(InstanceManager.tabbedPreferencesInstance().invokeSaveOptions());
                    break;
                case JOptionPane.NO_OPTION:
                    // do nothing
                    break;
                case JOptionPane.CANCEL_OPTION:
                default:
                    // abort window closing
                    return;
            }
        }
        this.setVisible(false);
    }
}
