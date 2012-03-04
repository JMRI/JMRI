package jmri.web.server;

/**
 * @author Steve Todd Copyright (C) 2011
 * @author Randall Wood Copyright (C) 2012
 * @version $Revision$
 */
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import jmri.swing.EditableList;

public class WebServerPreferencesPanel extends JPanel {

    static final ResourceBundle rb = ResourceBundle.getBundle("jmri.web.server.WebServerStrings");
    Border lineBorder;
    JSpinner clickDelaySpinner;
    JSpinner refreshDelaySpinner;
    EditableList disallowedFrames;
    JCheckBox useAjaxCB;
    JCheckBox rebuildIndexCB;
    JTextField port;
    JButton saveB;
    JButton cancelB;
    WebServerPreferences preferences;
    JFrame parentFrame = null;
    boolean enableSave;

    public WebServerPreferencesPanel() {
        preferences = WebServerManager.getWebServerPreferences();
        initGUI();
        setGUI();
    }

    public WebServerPreferencesPanel(JFrame f) {
        this();
        parentFrame = f;
    }

    private void initGUI() {
        lineBorder = BorderFactory.createLineBorder(Color.black);
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        add(rebuildIndexPanel());
        add(portPanel());
        add(delaysPanel());
        add(cancelApplySave());

    }

    private void setGUI() {
        clickDelaySpinner.setValue(preferences.getClickDelay());
        refreshDelaySpinner.setValue(preferences.getRefreshDelay());
        disallowedFrames.setListData(preferences.getDisallowedFrames().toArray());
        useAjaxCB.setSelected(preferences.useAjax());
        rebuildIndexCB.setSelected(preferences.isRebuildIndex());
        port.setText(Integer.toString(preferences.getPort()));
    }

    /**
     * Show the save and cancel buttons if displayed in its own frame.
     */
    public void enableSave() {
        saveB.setVisible(true);
        cancelB.setVisible(true);
    }

    /**
     * set the local prefs to match the GUI Local prefs are independant from the
     * singleton instance prefs.
     *
     * @return true if set, false if values are unacceptable.
     */
    private boolean setValues() {
        boolean didSet = true;
        preferences.setClickDelay((Integer) clickDelaySpinner.getValue());
        preferences.setRefreshDelay((Integer) refreshDelaySpinner.getValue());
        // TODO: need to set disallowedFrames as they change
        // preferences.setDisallowedFrames(disallowedFrames.getText());
        preferences.setUseAjax(useAjaxCB.isSelected());
        preferences.setRebuildIndex(rebuildIndexCB.isSelected());
        int portNum;
        try {
            portNum = Integer.parseInt(port.getText());
        } catch (NumberFormatException NFE) { //  Not a number
            portNum = 0;
        }
        if ((portNum < 1) || (portNum > 65535)) { //  Invalid port value
            javax.swing.JOptionPane.showMessageDialog(this,
                    rb.getString("WarningInvalidPort"),
                    rb.getString("TitlePortWarningDialog"),
                    JOptionPane.WARNING_MESSAGE);
            didSet = false;
        } else {
            preferences.setPort(portNum);
        }

        return didSet;
    }

    public void storeValues() {
        if (setValues()) {
            preferences.save();

            if (parentFrame != null) {
                parentFrame.dispose();
            }
        }


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

    private JPanel delaysPanel() {
        JPanel panel = new JPanel();
        TitledBorder border = BorderFactory.createTitledBorder(lineBorder,
                rb.getString("TitleDelayPanel"), TitledBorder.CENTER, TitledBorder.TOP);
        panel.setBorder(border);

        SpinnerNumberModel spinMod = new SpinnerNumberModel(1, 0, 999, 1);
        clickDelaySpinner = new JSpinner(spinMod);
        ((JSpinner.DefaultEditor) clickDelaySpinner.getEditor()).getTextField().setEditable(false);
        panel.add(clickDelaySpinner);
        panel.add(new JLabel(rb.getString("LabelClickDelay")));

        spinMod = new SpinnerNumberModel(5, 1, 999, 1);
        refreshDelaySpinner = new JSpinner(spinMod);
        ((JSpinner.DefaultEditor) refreshDelaySpinner.getEditor()).getTextField().setEditable(false);
        panel.add(refreshDelaySpinner);
        panel.add(new JLabel(rb.getString("LabelRefreshDelay")));

        useAjaxCB = new JCheckBox(rb.getString("LabelUseAjax"));
        useAjaxCB.setToolTipText(rb.getString("ToolTipUseAjax"));
        panel.add(useAjaxCB);

        JPanel dfPanel = new JPanel();
        disallowedFrames = new EditableList();
        dfPanel.add(new JScrollPane(disallowedFrames));
        dfPanel.add(new JLabel(rb.getString("LabelDisallowedFrames")));

        panel.add(dfPanel);

        return panel;
    }

    private JPanel rebuildIndexPanel() {
        JPanel panel = new JPanel();
        TitledBorder border = BorderFactory.createTitledBorder(lineBorder,
                rb.getString("TitleRebuildIndexPanel"), TitledBorder.CENTER, TitledBorder.TOP);

        panel.setBorder(border);
        rebuildIndexCB = new JCheckBox(rb.getString("LabelRebuildIndex"));
        rebuildIndexCB.setToolTipText(rb.getString("ToolTipRebuildIndex"));
        panel.add(rebuildIndexCB);
        return panel;
    }

    private JPanel portPanel() {
        JPanel panel = new JPanel();
        TitledBorder portBorder = BorderFactory.createTitledBorder(lineBorder,
                rb.getString("TitlePortPanel"), TitledBorder.CENTER, TitledBorder.TOP);

        panel.setBorder(portBorder);
        port = new JTextField();
        port.setText("12080");
        port.setPreferredSize(port.getPreferredSize());
        panel.add(port);
        panel.add(new JLabel(rb.getString("LabelPort")));
        return panel;
    }

    private JPanel cancelApplySave() {
        JPanel panel = new JPanel();
        cancelB = new JButton(rb.getString("ButtonCancel"));
        cancelB.setVisible(false);
        cancelB.addActionListener(new ActionListener() {

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
        saveB = new JButton(rb.getString("ButtonSave"));
        saveB.setVisible(false);
        saveB.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent event) {
                storeValues();
            }
        });
        panel.add(cancelB);
        panel.add(saveB);
        panel.add(new JLabel(rb.getString("LabelApplyWarning")));
        panel.add(applyB);
        return panel;
    }
}
