package apps.gui3.tabbedpreferences;

import java.awt.event.WindowEvent;
import javax.swing.JOptionPane;
import javax.swing.WindowConstants;
import jmri.InstanceManager;
import jmri.ShutDownManager;
import jmri.swing.PreferencesPanel;
import jmri.util.JmriJFrame;

/**
 * Support displaying {@link TabbedPreferences} in a window.
 *<p>
 * The {@link TabbedPreferences} object is requested from the InstanceManager, and
 * if need-be created and initialized in the process of doing that.
 * 
 * @author Kevin Dickerson Copyright 2010
 * @author Bob Jacobsen Copyright 2019
 */
public class TabbedPreferencesFrame extends JmriJFrame {

    @Override
    public String getTitle() {
        return getTabbedPreferences().getTitle();
    }

    public boolean isMultipleInstances() {
        return true;
    }

    public TabbedPreferencesFrame() {
        super();
        add(getTabbedPreferences());
        addHelpMenu("package.apps.TabbedPreferences", true); // NOI18N
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    }

    public void gotoPreferenceItem(String item, String sub) {
        getTabbedPreferences().gotoPreferenceItem(item, sub);
    }

    @Override
    public void windowClosing(WindowEvent e) {
        ShutDownManager sdm = InstanceManager.getDefault(ShutDownManager.class);
        if (!getTabbedPreferences().isPreferencesValid() && !sdm.isShuttingDown()) {
            for (PreferencesPanel panel : getTabbedPreferences().getPreferencesPanels().values()) {
                if (!panel.isPreferencesValid()) {
                    switch (JOptionPane.showConfirmDialog(this,
                            Bundle.getMessage("InvalidPreferencesMessage", panel.getTabbedPreferencesTitle()),
                            Bundle.getMessage("InvalidPreferencesTitle"),
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.ERROR_MESSAGE)) {
                        case JOptionPane.YES_OPTION:
                            // abort window closing and return to broken preferences
                            getTabbedPreferences().gotoPreferenceItem(panel.getPreferencesItem(), panel.getTabbedPreferencesTitle());
                            return;
                        default:
                            // do nothing
                            break;
                    }
                }
            }
        }
        if (getTabbedPreferences().isDirty()) {
            switch (JOptionPane.showConfirmDialog(this,
                    Bundle.getMessage("UnsavedChangesMessage", getTabbedPreferences().getTitle()), // NOI18N
                    Bundle.getMessage("UnsavedChangesTitle"), // NOI18N
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE)) {
                case JOptionPane.YES_OPTION:
                    // save preferences
                    getTabbedPreferences().savePressed(getTabbedPreferences().invokeSaveOptions());
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

    /**
     * Ensure a TabbedPreferences instance is always available.
     *
     * @return the default TabbedPreferences instance, creating it if needed
     */
    private TabbedPreferences getTabbedPreferences() {
        return InstanceManager.getOptionalDefault(TabbedPreferences.class).orElseGet(() -> {
            return InstanceManager.setDefault(TabbedPreferences.class, new TabbedPreferences());
        });
    }
}
