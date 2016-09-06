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
 */
public class TabbedPreferencesFrame extends JmriJFrame {

    final TabbedPreferences preferences;
    
    @Override
    public String getTitle() {
        return this.preferences.getTitle();

    }

    public boolean isMultipleInstances() {
        return true;
    }

    static boolean init = false;
    static int lastdivider;

    public TabbedPreferencesFrame() {
        this.preferences = InstanceManager.getDefault(TabbedPreferences.class);
        add(preferences);
        addHelpMenu("package.apps.TabbedPreferences", true);
        this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    }

    public void gotoPreferenceItem(String item, String sub) {
        this.preferences.gotoPreferenceItem(item, sub);
    }

    @Override
    public void windowClosing(WindowEvent e) {
        if (this.preferences.isDirty()) {
            switch (JOptionPane.showConfirmDialog(this,
                    Bundle.getMessage("UnsavedChangesMessage", this.preferences.getTitle()), // NOI18N
                    Bundle.getMessage("UnsavedChangesTitle"), // NOI18N
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE)) {
                case JOptionPane.YES_OPTION:
                    // save preferences
                    this.preferences.savePressed(this.preferences.invokeSaveOptions());
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
