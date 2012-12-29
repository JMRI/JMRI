package jmri.web.server;

/**
 * @author Steve Todd Copyright (C) 2011
 * @author Randall Wood Copyright (C) 2012
 * @version $Revision$
 */
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import jmri.swing.DefaultEditableListModel;
import jmri.swing.DefaultListCellEditor;
import jmri.swing.EditableList;
import jmri.swing.JTitledSeparator;
import jmri.swing.PreferencesPanel;
import org.apache.log4j.Logger;

public class WebServerPreferencesPanel extends JPanel implements ListDataListener, PreferencesPanel {

    private static final long serialVersionUID = 6907436730813458420L;
    static Logger log = Logger.getLogger(WebServerPreferencesPanel.class.getName());
    Border lineBorder;
    JSpinner clickDelaySpinner;
    JSpinner refreshDelaySpinner;
    EditableList disallowedFrames;
    JCheckBox useAjaxCB;
    JCheckBox rebuildIndexCB;
    JTextField port;
    JTextField railroadName;
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

    /*
     private void initComponents() {
     GroupLayout layout = new GroupLayout(this);
     this.setLayout(layout);
     layout.setAutoCreateGaps(true);
     layout.setAutoCreateContainerGaps(true);
     SequentialGroup group = layout.createSequentialGroup();
     group.addComponent(new JTitledSeparator(WebServer.getString("TitleWebServerPreferences")));
     group.addGroup(webServerPreferences(layout));
     layout.setVerticalGroup(group);
     }
     */
    private void initGUI() {
        this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        add(new JTitledSeparator(WebServer.getString("TitleWebServerPreferences")));
        add(rrNamePanel());
        add(rebuildIndexPanel());
        add(portPanel());
        add(new JTitledSeparator(WebServer.getString("TitleDelayPanel")));
        add(delaysPanel());
        add(new JSeparator());
        add(cancelApplySave());
    }

    /*
     private Group webServerPreferences(GroupLayout layout) {
     railroadName = new JTextField(preferences.getRailRoadName());
     railroadName.setToolTipText(WebServer.getString("ToolTipRailRoadName"));
     railroadName.setColumns(30);
     ParallelGroup group = layout.createParallelGroup(GroupLayout.Alignment.CENTER);
     group.addComponent(new JLabel(WebServer.getString("LabelRailRoadName")), GroupLayout.Alignment.TRAILING);
     group.addComponent(this.railroadName, GroupLayout.Alignment.LEADING);
     return group;
     }
     */
    private void setGUI() {
        clickDelaySpinner.setValue(preferences.getClickDelay());
        refreshDelaySpinner.setValue(preferences.getRefreshDelay());
        DefaultEditableListModel model = new DefaultEditableListModel();
        for (String frame : preferences.getDisallowedFrames()) {
            model.addElement(frame);
        }
        model.addElement(" ");
        disallowedFrames.setModel(model);
        disallowedFrames.getModel().addListDataListener(this);
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
     * set the local prefs to match the GUI Local prefs are independent from the
     * singleton instance prefs.
     *
     * @return true if set, false if values are unacceptable.
     */
    private boolean setValues() {
        boolean didSet = true;
        preferences.setClickDelay((Integer) clickDelaySpinner.getValue());
        preferences.setRefreshDelay((Integer) refreshDelaySpinner.getValue());
        ArrayList<String> frames = new ArrayList<String>();
        for (int i = 0; i < disallowedFrames.getModel().getSize(); i++) {
            String frame = disallowedFrames.getModel().getElementAt(i).toString().trim();
            if (!frame.equals("")) {
                frames.add(frame);
            }
        }
        preferences.setDisallowedFrames(frames);
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
                    WebServer.getString("WarningInvalidPort"),
                    WebServer.getString("TitlePortWarningDialog"),
                    JOptionPane.WARNING_MESSAGE);
            didSet = false;
        } else {
            preferences.setPort(portNum);
        }
        preferences.setRailRoadName(railroadName.getText());
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

        SpinnerNumberModel spinMod = new SpinnerNumberModel(1, 0, 999, 1);
        clickDelaySpinner = new JSpinner(spinMod);
        ((JSpinner.DefaultEditor) clickDelaySpinner.getEditor()).getTextField().setEditable(false);
        clickDelaySpinner.setToolTipText(WebServer.getString("ToolTipClickDelay"));
        panel.add(clickDelaySpinner);
        panel.add(new JLabel(WebServer.getString("LabelClickDelay")));

        spinMod = new SpinnerNumberModel(5, 1, 999, 1);
        refreshDelaySpinner = new JSpinner(spinMod);
        ((JSpinner.DefaultEditor) refreshDelaySpinner.getEditor()).getTextField().setEditable(false);
        refreshDelaySpinner.setToolTipText(WebServer.getString("ToolTipRefreshDelay"));
        panel.add(refreshDelaySpinner);
        panel.add(new JLabel(WebServer.getString("LabelRefreshDelay")));

        useAjaxCB = new JCheckBox(WebServer.getString("LabelUseAjax"));
        useAjaxCB.setToolTipText(WebServer.getString("ToolTipUseAjax"));
        panel.add(useAjaxCB);

        JPanel dfPanel = new JPanel();
        disallowedFrames = new EditableList();
        JTextField tf = new JTextField();
        tf.setBorder(BorderFactory.createLineBorder(Color.black));
        disallowedFrames.setListCellEditor(new DefaultListCellEditor(tf));
        dfPanel.add(new JScrollPane(disallowedFrames));
        dfPanel.add(new JLabel(WebServer.getString("LabelDisallowedFrames")));
        dfPanel.setToolTipText(WebServer.getString("ToolTipDisallowedFrames"));

        panel.add(dfPanel);

        return panel;
    }

    private JPanel rrNamePanel() {
        JPanel panel = new JPanel();
        railroadName = new JTextField(preferences.getRailRoadName());
        railroadName.setToolTipText(WebServer.getString("ToolTipRailRoadName"));
        railroadName.setColumns(30);
        panel.add(new JLabel(WebServer.getString("LabelRailRoadName")));
        panel.add(railroadName);
        return panel;
    }

    private JPanel rebuildIndexPanel() {
        JPanel panel = new JPanel();
        rebuildIndexCB = new JCheckBox(WebServer.getString("LabelRebuildIndex"));
        rebuildIndexCB.setToolTipText(WebServer.getString("ToolTipRebuildIndex"));
        panel.add(rebuildIndexCB);
        return panel;
    }

    private JPanel portPanel() {
        JPanel panel = new JPanel();
        port = new JTextField();
        port.setText(Integer.toString(preferences.getPort()));
        port.setColumns(6);
        port.setToolTipText(WebServer.getString("ToolTipPort"));
        panel.add(port);
        panel.add(new JLabel(WebServer.getString("LabelPort")));
        return panel;
    }

    private JPanel cancelApplySave() {
        JPanel panel = new JPanel();
        cancelB = new JButton(WebServer.getString("ButtonCancel"));
        cancelB.setVisible(false);
        cancelB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                cancelValues();
            }
        });
        JButton applyB = new JButton(WebServer.getString("ButtonApply"));
        applyB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                applyValues();
            }
        });
        saveB = new JButton(WebServer.getString("ButtonSave"));
        saveB.setVisible(false);
        saveB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                storeValues();
            }
        });
        panel.add(cancelB);
        panel.add(saveB);
        panel.add(new JLabel(WebServer.getString("LabelApplyWarning")));
        panel.add(applyB);
        return panel;
    }

    @Override
    public void intervalAdded(ListDataEvent lde) {
        // throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void intervalRemoved(ListDataEvent lde) {
        // throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void contentsChanged(ListDataEvent lde) {
        DefaultEditableListModel model = (DefaultEditableListModel) disallowedFrames.getModel();
        if (!model.getElementAt(model.getSize() - 1).equals(" ")) {
            model.addElement(" ");
        } else if (model.getElementAt(lde.getIndex0()).toString().isEmpty()) {
            model.removeElementAt(lde.getIndex0());
        }
    }

    @Override
    public String getPreferencesItem() {
        return WebServer.getString("PreferencesItem");
    }

    @Override
    public String getPreferencesItemText() {
        return WebServer.getString("PreferencesItemTitle");
    }

    @Override
    public String getTabbedPreferencesTitle() {
        return null;
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
        return null;
    }

    @Override
    public void savePreferences() {
        this.storeValues();
    }
}
