package jmri.web.server;

/**
 * @author Steve Todd Copyright (C) 2011
 * @author Randall Wood Copyright (C) 2012, 2014
 * @version $Revision$
 */
import apps.PerformActionModel;
import apps.StartupActionsManager;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.Border;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import jmri.InstanceManager;
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
    EditableList<String> disallowedFrames;
    JCheckBox useAjaxCB;
    JTextField port;
    JCheckBox readonlyPower;
    WebServerPreferences preferences;
    private boolean restartRequired = false;
    private JCheckBox startup;
    private ItemListener startupItemListener;
    int startupActionPosition = -1;

    public WebServerPreferencesPanel() {
        preferences = WebServerManager.getWebServerPreferences();
        initGUI();
        setGUI();
    }

    private void initGUI() {
        this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        add(new JTitledSeparator(Bundle.getMessage("TitleWebServerPreferences")));
        add(portPanel());
        add(powerPanel());
        add(startupPanel());
        add(new JTitledSeparator(Bundle.getMessage("TitleDelayPanel")));
        add(delaysPanel());
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
        DefaultEditableListModel<String> model = new DefaultEditableListModel<String>();
        for (String frame : preferences.getDisallowedFrames()) {
            model.addElement(frame);
        }
        model.addElement(" ");
        disallowedFrames.setModel(model);
        disallowedFrames.getModel().addListDataListener(this);
        useAjaxCB.setSelected(preferences.useAjax());
        port.setText(Integer.toString(preferences.getPort()));
        readonlyPower.setSelected(preferences.isReadonlyPower());
        InstanceManager.getDefault(StartupActionsManager.class).addPropertyChangeListener((PropertyChangeEvent evt) -> {
            this.startup.setSelected(this.isStartupAction());
        });
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
            this.restartRequired = (preferences.getPort() != portNum);
            preferences.setPort(portNum);
        }
        preferences.setReadonlyPower(readonlyPower.isSelected());
        return didSet;
    }

    public void storeValues() {
        if (setValues()) {
            preferences.save();
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
        disallowedFrames = new EditableList<String>();
        JTextField tf = new JTextField();
        tf.setBorder(BorderFactory.createLineBorder(Color.black));
        disallowedFrames.setListCellEditor(new DefaultListCellEditor<String>(tf));
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

    private JPanel powerPanel() {
        JPanel panel = new JPanel();
        readonlyPower = new JCheckBox(Bundle.getMessage("LabelReadonlyPower"), preferences.isReadonlyPower());
        panel.add(readonlyPower);
        ActionListener listener = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                readonlyPower.setToolTipText(Bundle.getMessage(readonlyPower.isSelected() ? "ToolTipReadonlyPowerTrue" : "ToolTipReadonlyPowerFalse"));
            }
        };
        readonlyPower.addActionListener(listener);
        listener.actionPerformed(null);
        return panel;
    }

    private JPanel startupPanel() {
        JPanel panel = new JPanel();
        this.startup = new JCheckBox(Bundle.getMessage("LabelStartup"), this.isStartupAction());
        this.startupItemListener = (ItemEvent e) -> {
            this.startup.removeItemListener(this.startupItemListener);
            StartupActionsManager manager = InstanceManager.getDefault(StartupActionsManager.class);
            if (this.startup.isSelected()) {
                PerformActionModel model = new PerformActionModel();
                model.setClassName(WebServerAction.class.getName());
                if (this.startupActionPosition == -1 || this.startupActionPosition >= manager.getActions().length) {
                    manager.addAction(model);
                } else {
                    manager.setActions(this.startupActionPosition, model);
                }
            } else {
                manager.getActions(PerformActionModel.class).stream().filter((model) -> (model.getClassName().equals(WebServerAction.class.getName()))).forEach((model) -> {
                    this.startupActionPosition = Arrays.asList(manager.getActions()).indexOf(model);
                    manager.removeAction(model);
                });
            }
            this.startup.addItemListener(this.startupItemListener);
        };
        this.startup.addItemListener(this.startupItemListener);
        panel.add(this.startup);
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
    @SuppressWarnings("unchecked") // getModel() returns a JList model, which isn't powerful enough
    public void contentsChanged(ListDataEvent lde) {
        DefaultEditableListModel<String> model = (DefaultEditableListModel) disallowedFrames.getModel();
        if (!model.getElementAt(model.getSize() - 1).equals(" ")) {
            model.addElement(" ");
        } else if (model.getElementAt(lde.getIndex0()).isEmpty()) {
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

    @Override
    public boolean isDirty() {
        return this.preferences.isDirty();
    }

    @Override
    public boolean isRestartRequired() {
        return this.restartRequired;
    }

    @Override
    public boolean isPreferencesValid() {
        return true; // no validity checking performed

    }

    private boolean isStartupAction() {
        return InstanceManager.getDefault(StartupActionsManager.class).getActions(PerformActionModel.class).stream()
                .anyMatch((model) -> (model.getClassName().equals(WebServerAction.class.getName())));
        // The above is what NetBeans recommended the following be condenced to
        // It's readable, but different, so including alternate form
        //for (PerformActionModel model : InstanceManager.getDefault(StartupActionsManager.class).getActions(PerformActionModel.class)) {
        //    if (model.getClassName().equals(WebServerAction.class.getName())) {
        //        return true;
        //    }
        //}
        //return false;
    }
}
