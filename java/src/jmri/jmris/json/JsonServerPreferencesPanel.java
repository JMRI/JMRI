package jmri.jmris.json;

/**
 * @author Randall Wood Copyright (C) 2012
 */
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import jmri.swing.JTitledSeparator;
import jmri.swing.PreferencesPanel;
import org.apache.log4j.Logger;

public class JsonServerPreferencesPanel extends JPanel implements PreferencesPanel {

    private static final long serialVersionUID = 5452568391598728906L;
    static Logger log = Logger.getLogger(JsonServerPreferencesPanel.class.getName());
    private JSpinner heartbeatIntervalSpinner;
    private JTextField port;
    private JButton btnSave;
    private JButton btnCancel;
    private JsonServerPreferences preferences;
    private JFrame parentFrame = null;

    public JsonServerPreferencesPanel() {
        preferences = JsonServerManager.getJsonServerPreferences();
        initGUI();
        setGUI();
    }

    public JsonServerPreferencesPanel(JFrame f) {
        this();
        parentFrame = f;
    }

    private void initGUI() {
        this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        add(new JTitledSeparator(Bundle.getMessage("ServerSectionTitle")));
        add(portPanel());
        add(new JTitledSeparator(Bundle.getMessage("JSONSectionTitle")));
        add(heartbeatPanel());
        add(Box.createVerticalGlue());
    }

    private void setGUI() {
        heartbeatIntervalSpinner.setValue(preferences.getHeartbeatInterval() / 1000); // convert from milliseconds to seconds
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
        preferences.setHeartbeatInterval((Integer) heartbeatIntervalSpinner.getValue() * 1000); // convert to milliseconds from seconds
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
        if (setValues()) {
            preferences.setIsDirty(true);
        }
    }

    protected void cancelValues() {
        if (getTopLevelAncestor() != null) {
            ((JFrame) getTopLevelAncestor()).setVisible(false);
        }
    }

    private JPanel heartbeatPanel() {
        JPanel panel = new JPanel();
        SpinnerNumberModel spinMod = new SpinnerNumberModel(15, 1, 3600, 1);
        heartbeatIntervalSpinner = new JSpinner(spinMod);
        ((JSpinner.DefaultEditor) heartbeatIntervalSpinner.getEditor()).getTextField().setEditable(false);
        panel.add(heartbeatIntervalSpinner);
        panel.add(new JLabel(Bundle.getMessage("HeartbeatLabel")));
        return panel;
    }

    private JPanel portPanel() {
        JPanel panel = new JPanel();
        port = new JTextField();
        port.setText(Integer.toString(this.preferences.getPort()));
        port.setColumns(6);
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
            this.preferences.save();
            if (this.parentFrame != null) {
                this.parentFrame.dispose();
            }
        }
    }
}
