package apps.gui3;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import jmri.InstanceManager;
import jmri.ShutDownManager;
import jmri.swing.PreferencesPanel;

/**
 * Provide a preferences dialog.
 * 
 * @author Kevin Dickerson Copyright 2010
 */
public final class EditConnectionPreferencesDialog extends JDialog implements WindowListener {

    final EditConnectionPreferences editConnectionPreferences;
    boolean restartProgram = false;
    
    @Override
    public String getTitle() {
        return editConnectionPreferences.getTitle();
    }

    public boolean isMultipleInstances() {
        return true;
    }
    
    /**
     * Displays a dialog for editing the conenctions.
     * @return true if the program should restart, false if the program should quit.
     */
    public static boolean showDialog() {
        try {
            while (jmri.InstanceManager.getDefault(TabbedPreferences.class).init() != TabbedPreferences.INITIALISED) {
                final Object waiter = new Object();
                synchronized (waiter) {
                    waiter.wait(50);
                }
            }
            
            EditConnectionPreferencesDialog dialog = new EditConnectionPreferencesDialog();
            SwingUtilities.updateComponentTreeUI(dialog);
            
            dialog.pack();
            dialog.setVisible(true);
            return dialog.restartProgram;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    public EditConnectionPreferencesDialog() {
        super();
        setModal(true);
        editConnectionPreferences = new EditConnectionPreferences(this);
        editConnectionPreferences.init();
        add(editConnectionPreferences);
//        addHelpMenu("package.apps.TabbedPreferences", true); // NOI18N
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(this);
    }

    public void gotoPreferenceItem(String item, String sub) {
        editConnectionPreferences.gotoPreferenceItem(item, sub);
    }

    @Override
    public void windowClosing(WindowEvent e) {
        ShutDownManager sdm = InstanceManager.getNullableDefault(ShutDownManager.class);
        if (!editConnectionPreferences.isPreferencesValid() && (sdm == null || !sdm.isShuttingDown())) {
            for (PreferencesPanel panel : editConnectionPreferences.getPreferencesPanels().values()) {
                if (!panel.isPreferencesValid()) {
                    switch (JOptionPane.showConfirmDialog(this,
                            Bundle.getMessage("InvalidPreferencesMessage", panel.getTabbedPreferencesTitle()),
                            Bundle.getMessage("InvalidPreferencesTitle"),
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.ERROR_MESSAGE)) {
                        case JOptionPane.YES_OPTION:
                            // abort window closing and return to broken preferences
                            editConnectionPreferences.gotoPreferenceItem(panel.getPreferencesItem(), panel.getTabbedPreferencesTitle());
                            return;
                        default:
                            // do nothing
                            break;
                    }
                }
            }
        }
        if (editConnectionPreferences.isDirty()) {
            switch (JOptionPane.showConfirmDialog(this,
                    Bundle.getMessage("UnsavedChangesMessage", editConnectionPreferences.getTitle()), // NOI18N
                    Bundle.getMessage("UnsavedChangesTitle"), // NOI18N
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE)) {
                case JOptionPane.YES_OPTION:
                    // save preferences
                    editConnectionPreferences.savePressed(editConnectionPreferences.invokeSaveOptions());
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

    @Override
    public void windowOpened(WindowEvent e) {
    }

    @Override
    public void windowClosed(WindowEvent e) {
    }

    @Override
    public void windowIconified(WindowEvent e) {
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
    }

    @Override
    public void windowActivated(WindowEvent e) {
    }

    @Override
    public void windowDeactivated(WindowEvent e) {
    }
}
