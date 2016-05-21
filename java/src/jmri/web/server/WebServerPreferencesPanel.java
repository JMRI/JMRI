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
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import jmri.InstanceManager;
import jmri.swing.DefaultEditableListModel;
import jmri.swing.DefaultListCellEditor;
import jmri.swing.EditableList;
import jmri.swing.JTitledSeparator;
import jmri.swing.PreferencesPanel;

public class WebServerPreferencesPanel extends JPanel implements ListDataListener, PreferencesPanel {

    private JSpinner clickDelaySpinner;
    private JSpinner refreshDelaySpinner;
    private EditableList<String> disallowedFrames;
    private JCheckBox useAjaxCB;
    private JSpinner port;
    private JCheckBox readonlyPower;
    private final WebServerPreferences preferences;
    private boolean restartRequired = false;
    private JCheckBox startup;
    private final JCheckBox disableFrames = new JCheckBox();
    private final JCheckBox redirectFramesToPanels = new JCheckBox();
    private final JLabel clickDelayLabel = new JLabel(Bundle.getMessage("LabelClickDelay"));
    private final JLabel refreshDelayLabel = new JLabel(Bundle.getMessage("LabelRefreshDelay"));
    private final JLabel disallowedFramesLabel = new JLabel(Bundle.getMessage("LabelDisallowedFrames"));
    private ItemListener startupItemListener;
    private int startupActionPosition = -1;

    public WebServerPreferencesPanel() {
        preferences = WebServerPreferences.getDefault();
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
        add(this.disableFramesPanel());
        add(delaysPanel());
    }

    private void setGUI() {
        clickDelaySpinner.setValue(preferences.getClickDelay());
        refreshDelaySpinner.setValue(preferences.getRefreshDelay());
        DefaultEditableListModel<String> model = new DefaultEditableListModel<>();
        preferences.getDisallowedFrames().stream().forEach((frame) -> {
            model.addElement(frame);
        });
        model.addElement(" ");
        disallowedFrames.setModel(model);
        disallowedFrames.getModel().addListDataListener(this);
        useAjaxCB.setSelected(preferences.useAjax());
        port.setValue(preferences.getPort());
        readonlyPower.setSelected(preferences.isReadonlyPower());
        this.disableFrames.setSelected(preferences.isDisableFrames());
        this.redirectFramesToPanels.setSelected(preferences.isRedirectFramesToPanels());
        InstanceManager.getDefault(StartupActionsManager.class).addPropertyChangeListener((PropertyChangeEvent evt) -> {
            this.startup.setSelected(this.isStartupAction());
        });
        this.enableFrameControls(this.disableFrames.isSelected());
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
        ArrayList<String> frames = new ArrayList<>();
        for (int i = 0; i < disallowedFrames.getModel().getSize(); i++) {
            String frame = disallowedFrames.getModel().getElementAt(i).trim();
            if (!frame.isEmpty()) {
                frames.add(frame);
            }
        }
        preferences.setDisallowedFrames(frames);
        preferences.setUseAjax(useAjaxCB.isSelected());
        int portNum;
        try {
            portNum = (Integer) port.getValue();
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
        preferences.setDisableFrames(this.disableFrames.isSelected());
        preferences.setRedirectFramesToPanels(this.redirectFramesToPanels.isSelected());
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
            getTopLevelAncestor().setVisible(false);
        }
    }

    private JPanel delaysPanel() {
        JPanel panel = new JPanel();

        SpinnerNumberModel spinMod = new SpinnerNumberModel(1, 0, 999, 1);
        clickDelaySpinner = new JSpinner(spinMod);
        ((JSpinner.DefaultEditor) clickDelaySpinner.getEditor()).getTextField().setEditable(false);
        clickDelaySpinner.setToolTipText(Bundle.getMessage("ToolTipClickDelay"));
        panel.add(clickDelaySpinner);
        panel.add(this.clickDelayLabel);

        spinMod = new SpinnerNumberModel(5, 1, 999, 1);
        refreshDelaySpinner = new JSpinner(spinMod);
        ((JSpinner.DefaultEditor) refreshDelaySpinner.getEditor()).getTextField().setEditable(false);
        refreshDelaySpinner.setToolTipText(Bundle.getMessage("ToolTipRefreshDelay"));
        panel.add(refreshDelaySpinner);
        panel.add(this.refreshDelayLabel);

        useAjaxCB = new JCheckBox(Bundle.getMessage("LabelUseAjax"));
        useAjaxCB.setToolTipText(Bundle.getMessage("ToolTipUseAjax"));
        panel.add(useAjaxCB);

        JPanel dfPanel = new JPanel();
        disallowedFrames = new EditableList<>();
        JTextField tf = new JTextField();
        tf.setBorder(BorderFactory.createLineBorder(Color.black));
        disallowedFrames.setListCellEditor(new DefaultListCellEditor<>(tf));
        dfPanel.add(new JScrollPane(disallowedFrames));
        dfPanel.add(this.disallowedFramesLabel);
        dfPanel.setToolTipText(Bundle.getMessage("ToolTipDisallowedFrames"));

        panel.add(dfPanel);

        return panel;
    }

    private JPanel portPanel() {
        JPanel panel = new JPanel();
        port = new JSpinner(new SpinnerNumberModel(preferences.getPort(), 1, 65535, 1));
        ((JSpinner.DefaultEditor) port.getEditor()).getTextField().setEditable(true);
        port.setEditor(new JSpinner.NumberEditor(port, "#"));
        this.port.addChangeListener((ChangeEvent e) -> {
            this.setValues();
        });
        this.port.setToolTipText(Bundle.getMessage("ToolTipPort"));
        panel.add(port);
        panel.add(new JLabel(Bundle.getMessage("LabelPort")));
        return panel;
    }

    private JPanel powerPanel() {
        JPanel panel = new JPanel();
        readonlyPower = new JCheckBox(Bundle.getMessage("LabelReadonlyPower"), preferences.isReadonlyPower());
        panel.add(readonlyPower);
        ActionListener listener = (ActionEvent e) -> {
            readonlyPower.setToolTipText(Bundle.getMessage(readonlyPower.isSelected() ? "ToolTipReadonlyPowerTrue" : "ToolTipReadonlyPowerFalse"));
        };
        readonlyPower.addActionListener(listener);
        listener.actionPerformed(null);
        return panel;
    }

    private JPanel disableFramesPanel() {
        JPanel panel = new JPanel();

        this.disableFrames.setText(Bundle.getMessage("LabelDisableFrames"));
        this.disableFrames.setToolTipText(Bundle.getMessage("ToolTipDisableFrames"));
        this.disableFrames.addChangeListener((ChangeEvent e) -> {
            this.enableFrameControls(this.disableFrames.isSelected());
        });
        panel.add(this.disableFrames);

        this.redirectFramesToPanels.setText(Bundle.getMessage("LabelRedirectFramesToPanels"));
        this.redirectFramesToPanels.setToolTipText(Bundle.getMessage("ToolTipRedirectFramesToPanels"));
        panel.add(this.redirectFramesToPanels);

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
    // getModel() returns a JList model, which isn't powerful enough
    public void contentsChanged(ListDataEvent lde) {
        DefaultEditableListModel<String> model = (DefaultEditableListModel<String>) disallowedFrames.getModel();
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
    }

    private void enableFrameControls(boolean framesDisabled) {
        // enabled if frames are disabled
        this.redirectFramesToPanels.setEnabled(framesDisabled);

        // enabled if frames are enabled
        this.clickDelaySpinner.setEnabled(!framesDisabled);
        this.clickDelayLabel.setEnabled(!framesDisabled);
        this.refreshDelaySpinner.setEnabled(!framesDisabled);
        this.refreshDelayLabel.setEnabled(!framesDisabled);
        this.useAjaxCB.setEnabled(!framesDisabled);
        this.disallowedFrames.setEnabled(!framesDisabled);
        this.disallowedFramesLabel.setEnabled(!framesDisabled);
    }
}
