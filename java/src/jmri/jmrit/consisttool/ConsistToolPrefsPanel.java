package jmri.jmrit.consisttool;

import jmri.InstanceManager;
import jmri.swing.PreferencesPanel;
import java.awt.event.ActionEvent;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JComponent;
import javax.swing.JCheckBox;
import org.openide.util.lookup.ServiceProvider;

/**
 * @author Paul Bender Copyright (C) 2019
 */
@ServiceProvider(service = PreferencesPanel.class)
public class ConsistToolPrefsPanel extends JPanel implements PreferencesPanel {
        
    private JCheckBox writeCVOptionCheckBox;
    private boolean dirty = false;

    public ConsistToolPrefsPanel() {
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        JPanel p = new JPanel();
        p.setLayout(new java.awt.FlowLayout());
        writeCVOptionCheckBox = new JCheckBox(Bundle.getMessage("WriteCVToRosterOption"));
        writeCVOptionCheckBox.addActionListener((ActionEvent e) -> {
            ConsistPreferencesManager cpm = InstanceManager.getDefault(ConsistPreferencesManager.class);
            if(cpm.isUpdateCV19() != writeCVOptionCheckBox.isSelected()) {
               dirty = true;
               cpm.setUpdateCV19(writeCVOptionCheckBox.isSelected());
            }
        });
        writeCVOptionCheckBox.setSelected(InstanceManager.getDefault(ConsistPreferencesManager.class).isUpdateCV19());

        p.add(writeCVOptionCheckBox);
        writeCVOptionCheckBox.setToolTipText(Bundle.getMessage("WriteCVToRosterToolTip"));
        add(p);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPreferencesItem() {
        return "CONSISTTOOL"; // NOI18N
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPreferencesItemText() {
        return Bundle.getMessage("ConsistToolTitle");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTabbedPreferencesTitle() {
        return getPreferencesItemText();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLabelKey() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JComponent getPreferencesComponent() {
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isPersistant() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPreferencesTooltip() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void savePreferences() {
       // saved through preferences, so just reset dirty
       dirty = false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDirty() {
        return dirty;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isRestartRequired() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isPreferencesValid() {
        return true; // no validity checking performed
    }


}
