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
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.Border;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import jmri.swing.DefaultEditableListModel;
import jmri.swing.DefaultListCellEditor;
import jmri.swing.EditableList;
import jmri.swing.JTitledSeparator;
import jmri.swing.PreferencesPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebServerPreferencesPanel extends JPanel implements ListDataListener, PreferencesPanel {

    private static final long serialVersionUID = 6907436730813458420L;
    static Logger log = LoggerFactory.getLogger(WebServerPreferencesPanel.class.getName());
    Border lineBorder;
    JSpinner clickDelaySpinner;
    JSpinner refreshDelaySpinner;
    EditableList disallowedFrames;
    JCheckBox useAjaxCB;
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

    /*
     private void initComponents() {
     GroupLayout layout = new GroupLayout(this);
     this.setLayout(layout);
     layout.setAutoCreateGaps(true);
     layout.setAutoCreateContainerGaps(true);
     SequentialGroup group = layout.createSequentialGroup();
     group.addComponent(new JTitledSeparator(Bundle.getMessage("TitleWebServerPreferences")));
     group.addGroup(webServerPreferences(layout));
     layout.setVerticalGroup(group);
     }
     */
    private void initGUI() {
        this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        add(new JTitledSeparator(Bundle.getMessage("TitleWebServerPreferences")));
        add(portPanel());
        add(new JTitledSeparator(Bundle.getMessage("TitleDelayPanel")));
        add(delaysPanel());
        add(new JSeparator());
        add(cancelApplySave());
    }

    /*
     private Group webServerPreferences(GroupLayout layout) {
     railroadName = new JTextField(preferences.getRailRoadName());
     railroadName.setToolTipText(Bundle.getMessage("ToolTipRailRoadName"));
     railroadName.setColumns(30);
     ParallelGroup group = layout.createParallelGroup(GroupLayout.Alignment.CENTER);
     group.addComponent(new JLabel(Bundle.getMessage("LabelRailRoadName")), GroupLayout.Alignment.TRAILING);
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
        int portNum;
        try {
            portNum = Integer.parseInt(port.getText());
        } catch (NumberFormatException NFE) { //  Not a number
            portNum = 0;
        }
        if ((portNum < 1) || (portNum > 65535)) { //  Invalid port value
            javax.swing.JOptionPane.showMessageDialog(this,
                    Bundle.getMessage("WarningInvalidPort"),
                    Bundle.getMessage("TitlePortWarningDialog"),
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

        SpinnerNumberModel spinMod = new SpinnerNumberModel(1, 0, 999, 1);
        clickDelaySpinner = new JSpinner(spinMod);
        ((JSpinner.DefaultEditor) clickDelaySpinner.getEditor()).getTextField().setEditable(false);
        clickDelaySpinner.setToolTipText(Bundle.getMessage("ToolTipClickDelay"));
        panel.add(clickDelaySpinner);
        panel.add(new JLabel(Bundle.getMessage("LabelClickDelay")));

        spinMod = new SpinnerNumberModel(5, 1, 999, 1);
        refreshDelaySpinner = new JSpinner(spinMod);
        ((JSpinner.DefaultEditor) refreshDelaySpinner.getEditor()).getTextField().setEditable(false);
        refreshDelaySpinner.setToolTipText(Bundle.getMessage("ToolTipRefreshDelay"));
        panel.add(refreshDelaySpinner);
        panel.add(new JLabel(Bundle.getMessage("LabelRefreshDelay")));

        useAjaxCB = new JCheckBox(Bundle.getMessage("LabelUseAjax"));
        useAjaxCB.setToolTipText(Bundle.getMessage("ToolTipUseAjax"));
        panel.add(useAjaxCB);

        JPanel dfPanel = new JPanel();
        disallowedFrames = new EditableList();
        JTextField tf = new JTextField();
        tf.setBorder(BorderFactory.createLineBorder(Color.black));
        disallowedFrames.setListCellEditor(new DefaultListCellEditor(tf));
        dfPanel.add(new JScrollPane(disallowedFrames));
        dfPanel.add(new JLabel(Bundle.getMessage("LabelDisallowedFrames")));
        dfPanel.setToolTipText(Bundle.getMessage("ToolTipDisallowedFrames"));

        panel.add(dfPanel);

        return panel;
    }

    private JPanel portPanel() {
        JPanel panel = new JPanel();
        port = new JTextField();
        port.setText(Integer.toString(preferences.getPort()));
        port.setColumns(6);
        port.setToolTipText(Bundle.getMessage("ToolTipPort"));
        panel.add(port);
        panel.add(new JLabel(Bundle.getMessage("LabelPort")));
        return panel;
    }

    private JPanel cancelApplySave() {
        JPanel panel = new JPanel();
        cancelB = new JButton(Bundle.getMessage("ButtonCancel"));
        cancelB.setVisible(false);
        cancelB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                cancelValues();
            }
        });
        JButton applyB = new JButton(Bundle.getMessage("ButtonApply"));
        applyB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                applyValues();
            }
        });
        saveB = new JButton(Bundle.getMessage("ButtonSave"));
        saveB.setVisible(false);
        saveB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                storeValues();
            }
        });
        panel.add(cancelB);
        panel.add(saveB);
        panel.add(new JLabel(Bundle.getMessage("LabelApplyWarning")));
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
        return Bundle.getMessage("PreferencesItem");
    }

    @Override
    public String getPreferencesItemText() {
        return Bundle.getMessage("PreferencesItemTitle");
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
