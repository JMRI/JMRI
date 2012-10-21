package jmri.jmris.json;

/**
 * @author Randall Wood Copyright (C) 2012
 */
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import jmri.swing.JTitledSeparator;
import jmri.swing.PreferencesPanel;

import org.apache.log4j.Logger;

public class JsonServerPreferencesPanel extends JPanel implements PreferencesPanel {

	private static final long serialVersionUID = 5452568391598728906L;
	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmris.json.JsonServer");
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
        add(new JTitledSeparator(rb.getString("ServerSectionTitle")));
        add(portPanel());
        add(new JTitledSeparator(rb.getString("JSONSectionTitle")));
        add(heartbeatPanel());
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
                    rb.getString("InvalidPortWarningMessage"),
                    rb.getString("InvalidPortWarningTitle"),
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
        panel.add(new JLabel(rb.getString("HeartbeatLabel")));
        return panel;
    }

    private JPanel portPanel() {
        JPanel panel = new JPanel();
        port = new JTextField();
        port.setText("12080");
        port.setColumns(6);
        panel.add(port);
        panel.add(new JLabel(rb.getString("LabelPort")));
        return panel;
    }

    private JPanel cancelApplySave() {
        JPanel panel = new JPanel();
        btnCancel = new JButton(rb.getString("ButtonCancel"));
        btnCancel.setVisible(false);
        btnCancel.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent event) {
                cancelValues();
            }
        });
        JButton applyB = new JButton(rb.getString("ButtonApply"));
        applyB.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent event) {
                applyValues();
            }
        });
        btnSave = new JButton(rb.getString("ButtonSave"));
        btnSave.setVisible(false);
        btnSave.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent event) {
                savePreferences();
            }
        });
        panel.add(btnCancel);
        panel.add(btnSave);
        panel.add(new JLabel(rb.getString("LabelApplyWarning")));
        panel.add(applyB);
        return panel;
    }

	@Override
	public String getPreferencesItem() {
		return rb.getString("PreferencesItem");
	}

	@Override
	public String getPreferencesItemText() {
		return rb.getString("PreferencesItemTitle");
	}

	@Override
	public String getTabbedPreferencesTitle() {
		return rb.getString("PreferencesTabTitle");
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
		return rb.getString("PreferencesTooltip");
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
