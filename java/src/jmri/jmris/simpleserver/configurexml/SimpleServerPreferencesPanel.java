package jmri.jmris.simpleserver.configurexml;

/**
 * @author Randall Wood Copyright (C) 2012
 */
import java.awt.event.ActionEvent;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import jmri.swing.JTitledSeparator;
import jmri.swing.PreferencesPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.jmris.simpleserver.SimpleServerManager;

public class SimpleServerPreferencesPanel extends JPanel implements PreferencesPanel {

    private static final long serialVersionUID = 03_16_2015L;
    private final static Logger log = LoggerFactory.getLogger(SimpleServerPreferencesPanel.class.getName());
    private JTextField port;
    private JButton btnSave;
    private JButton btnCancel;
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
        port.setText(Integer.toString(preferences.getPort()));
    }

    /**
     * Show the save and cancel buttons if displayed in its own frame.
     */
    public void enableSave() {
        btnSave.setVisible(true);
        btnCancel.setVisible(true);
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
            portNum = Integer.parseInt(port.getText());
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
        if (getTopLevelAncestor() != null) {
            ((JFrame) getTopLevelAncestor()).setVisible(false);
        }
    }

    private JPanel portPanel() {
        JPanel panel = new JPanel();
        port = new JTextField();
        port.setText(Integer.toString(this.preferences.getPort()));
        port.setColumns(6);
        port.addActionListener((ActionEvent e) -> {
            this.setValues();
        });
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
    public boolean isPreferencesValid(){
        return false;
    }


}
