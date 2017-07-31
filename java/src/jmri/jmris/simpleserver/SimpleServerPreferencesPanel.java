package jmri.jmris.simpleserver;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import jmri.swing.JTitledSeparator;
import jmri.swing.PreferencesPanel;
import org.openide.util.lookup.ServiceProvider;

/**
 * @author Randall Wood Copyright (C) 2012
 */
@ServiceProvider(service = PreferencesPanel.class)
public class SimpleServerPreferencesPanel extends JPanel implements PreferencesPanel {

    private JSpinner port;
    private SimpleServerPreferences preferences;
    private JFrame parentFrame = null;

    public SimpleServerPreferencesPanel() {
        this.preferences = new SimpleServerPreferences();
        this.preferences.apply(SimpleServerManager.getSimpleServerPreferences());
        initGUI();
        setGUI();
    }

    public SimpleServerPreferencesPanel(JFrame f) {
        this();
        parentFrame = f;
    }

    private void initGUI() {
        this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        add(new JTitledSeparator(Bundle.getMessage("ServerSectionTitle")));
        add(portPanel());
        add(new JTitledSeparator(Bundle.getMessage("SimpleServerSectionTitle")));
        add(Box.createVerticalGlue());
    }

    private void setGUI() {
        port.setValue(preferences.getPort());
    }

    /**
     * set the local prefs to match the GUI Local prefs are independent from the
     * singleton instance prefs.
     *
     * @return true if set, false if values are unacceptable.
     */
    private boolean setValues() {
        boolean didSet = true;
        int portNum;
        try {
            portNum = (Integer) port.getValue();
//            Integer currentValue = (Integer)jSpinner1.getValue();
        } catch (NumberFormatException NFE) { //  Not a number
            portNum = 0;
        }
        if ((portNum < 1) || (portNum > 65535)) { //  Invalid port value
            javax.swing.JOptionPane.showMessageDialog(this,
                    Bundle.getMessage("InvalidPortWarningMessage"),
                    Bundle.getMessage("InvalidPortWarningTitle"),
                    JOptionPane.WARNING_MESSAGE);
            didSet = false;
        } else {
            preferences.setPort(portNum);
        }
        return didSet;
    }

    /**
     * Update the singleton instance of prefs, then mark (isDirty) that the
     * values have changed and needs to save to xml file.
     */
    protected void applyValues() {
        this.setValues();
    }

    protected void cancelValues() {
        java.awt.Container ancestor = getTopLevelAncestor();
        if (ancestor != null && ancestor instanceof JFrame) {
            ((JFrame) ancestor).setVisible(false);
        }
    }

    private JPanel portPanel() {
        JPanel panel = new JPanel();
        port = new JSpinner(new SpinnerNumberModel(preferences.getPort(), 1, 65535, 1));
        ((JSpinner.DefaultEditor) port.getEditor()).getTextField().setEditable(true);
        port.setEditor(new JSpinner.NumberEditor(port, "#"));
        this.port.addChangeListener((ChangeEvent e) -> {
            this.setValues();
        });
        this.port.setToolTipText(Bundle.getMessage("PortToolTip"));
        panel.add(port);
        panel.add(new JLabel(Bundle.getMessage("LabelPort")));
        return panel;
    }

    @Override
    public String getPreferencesItem() {
        return Bundle.getMessage("PreferencesItem");
    }

    @Override
    public String getPreferencesItemText() {
        return Bundle.getMessage("PreferencesItemTitle");
    }

    @Override
    public String getTabbedPreferencesTitle() {
        return Bundle.getMessage("PreferencesTabTitle");
    }

    @Override
    public String getLabelKey() {
        return null;
    }

    @Override
    public JComponent getPreferencesComponent() {
        return this;
    }

    @Override
    public boolean isPersistant() {
        return false;
    }

    @Override
    public String getPreferencesTooltip() {
        return Bundle.getMessage("PreferencesTooltip");
    }

    @Override
    public void savePreferences() {
        if (this.setValues()) {
            SimpleServerManager.getSimpleServerPreferences().apply(this.preferences);
            SimpleServerManager.getSimpleServerPreferences().save();
            if (this.parentFrame != null) {
                this.parentFrame.dispose();
            }
        }
    }

    @Override
    public boolean isDirty() {
        return this.preferences.compareValuesDifferent(SimpleServerManager.getSimpleServerPreferences())
                || SimpleServerManager.getSimpleServerPreferences().isDirty();
    }

    @Override
    public boolean isRestartRequired() {
        return SimpleServerManager.getSimpleServerPreferences().isRestartRequired();
    }

    /**
     * Indicate that the preferences are valid.
     *
     * @return true if the preferences are valid, false otherwise
     */
    @Override
    public boolean isPreferencesValid() {
        return true;
    }

}
