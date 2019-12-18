package jmri.jmrit.withrottle;

import apps.PerformActionModel;
import apps.StartupActionsManager;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.util.Arrays;
import java.util.ResourceBundle;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import jmri.InstanceManager;
import jmri.swing.JTitledSeparator;
import jmri.swing.PreferencesPanel;
import jmri.util.FileUtil;
import jmri.util.zeroconf.ZeroConfPreferences;
import jmri.util.zeroconf.ZeroConfServiceManager;
import org.openide.util.lookup.ServiceProvider;

/**
 * @author Brett Hoffman Copyright (C) 2010
 */
@ServiceProvider(service = PreferencesPanel.class)
public class WiThrottlePrefsPanel extends JPanel implements PreferencesPanel {

    JCheckBox eStopCB;
    JSpinner delaySpinner;

    JCheckBox momF2CB;

    JSpinner port;

    JCheckBox powerCB;
    JCheckBox turnoutCB;
    JCheckBox turnoutCreationCB;
    JCheckBox routeCB;
    JCheckBox consistCB;
    JCheckBox startupCB;
    JCheckBox useIPv4CB;
    JCheckBox useIPv6CB;
    JCheckBox fastClockDisplayCB;
    ItemListener startupItemListener;
    int startupActionPosition = -1;
    JRadioButton wifiRB;
    JRadioButton dccRB;

    WiThrottlePreferences localPrefs;

    public WiThrottlePrefsPanel() {
        if (InstanceManager.getNullableDefault(WiThrottlePreferences.class) == null) {
            InstanceManager.store(new WiThrottlePreferences(FileUtil.getUserFilesPath() + "throttle" + File.separator + "WiThrottlePreferences.xml"), WiThrottlePreferences.class);
        }
        localPrefs = InstanceManager.getDefault(WiThrottlePreferences.class);
        initGUI();
        setGUI();
    }

    public void initGUI() {
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        add(new JTitledSeparator(Bundle.getMessage("TitleDelayPanel")));
        add(eStopDelayPanel());
        add(new JTitledSeparator(Bundle.getMessage("TitleFunctionsPanel")));
        add(functionsPanel());
        add(new JTitledSeparator(Bundle.getMessage("TitleNetworkPanel")));
        add(networkPanel());
        add(new JTitledSeparator(Bundle.getMessage("TitleControllersPanel")));
        add(allowedControllers());
    }

    private void setGUI() {
        eStopCB.setSelected(localPrefs.isUseEStop());
        delaySpinner.setValue(localPrefs.getEStopDelay());

        momF2CB.setSelected(localPrefs.isUseMomF2());

        port.setValue(localPrefs.getPort());
        powerCB.setSelected(localPrefs.isAllowTrackPower());
        turnoutCB.setSelected(localPrefs.isAllowTurnout());
        turnoutCreationCB.setSelected(localPrefs.isAllowTurnoutCreation());
        routeCB.setSelected(localPrefs.isAllowRoute());
        fastClockDisplayCB.setSelected(localPrefs.isDisplayFastClock());
        consistCB.setSelected(localPrefs.isAllowConsist());
        InstanceManager.getDefault(StartupActionsManager.class).addPropertyChangeListener((PropertyChangeEvent evt) -> {
            startupCB.setSelected(isStartUpAction());
        });
        useIPv4CB.setSelected(isUseIPv4());
        useIPv6CB.setSelected(isUseIPv6());
        wifiRB.setSelected(localPrefs.isUseWiFiConsist());
        dccRB.setSelected(!localPrefs.isUseWiFiConsist());
    }

    /**
     * set the local prefs to match the GUI Local prefs are independent from the
     * singleton instance prefs.
     *
     * @return true if set, false if values are unacceptable.
     */
    private boolean setValues() {
        boolean didSet = true;
        localPrefs.setUseEStop(eStopCB.isSelected());
        localPrefs.setEStopDelay((Integer) delaySpinner.getValue());

        localPrefs.setUseMomF2(momF2CB.isSelected());

        int portNum;
        try {
            portNum = (int) port.getValue();
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
            localPrefs.setPort((int) port.getValue());
        }

        localPrefs.setAllowTrackPower(powerCB.isSelected());
        localPrefs.setAllowTurnout(turnoutCB.isSelected());
        localPrefs.setAllowTurnoutCreation(turnoutCreationCB.isSelected());
        localPrefs.setAllowRoute(routeCB.isSelected());
        localPrefs.setDisplayFastClock(fastClockDisplayCB.isSelected());
        localPrefs.setAllowConsist(consistCB.isSelected());
        localPrefs.setUseWiFiConsist(wifiRB.isSelected());
        ZeroConfPreferences zeroConfPrefs = InstanceManager.getDefault(ZeroConfServiceManager.class).getPreferences();
        zeroConfPrefs.setUseIPv4(useIPv4CB.isSelected());
        zeroConfPrefs.setUseIPv6(useIPv6CB.isSelected());

        return didSet;
    }

    private JPanel eStopDelayPanel() {
        JPanel panel = new JPanel();

        eStopCB = new JCheckBox(Bundle.getMessage("LabelUseEStop"));
        eStopCB.setToolTipText(Bundle.getMessage("ToolTipUseEStop"));
        SpinnerNumberModel spinMod = new SpinnerNumberModel(10, 4, 60, 2);
        delaySpinner = new JSpinner(spinMod);
        ((JSpinner.DefaultEditor) delaySpinner.getEditor()).getTextField().setEditable(false);
        panel.add(eStopCB);
        panel.add(delaySpinner);
        panel.add(new JLabel(Bundle.getMessage("LabelEStopDelay")));
        return panel;
    }

    private JPanel functionsPanel() {
        JPanel panel = new JPanel();

        momF2CB = new JCheckBox(Bundle.getMessage("LabelMomF2"));
        momF2CB.setToolTipText(Bundle.getMessage("ToolTipMomF2"));
        panel.add(momF2CB);
        return panel;
    }

    private JPanel networkPanel() {
        JPanel nPanelRow1 = new JPanel();
        JPanel nPanelRow2 = new JPanel();
        JPanel nPanelRow3 = new JPanel();
        JPanel nPanel = new JPanel(new GridLayout(3, 1));

        port = new JSpinner(new SpinnerNumberModel(localPrefs.getPort(), 1, 65535, 1));
        port.setToolTipText(Bundle.getMessage("PortToolTip"));
        port.setEditor(new JSpinner.NumberEditor(port, "#"));
        JLabel label = new JLabel(Bundle.getMessage("LabelPort"));
        label.setToolTipText(port.getToolTipText());
        nPanelRow1.add(port);
        nPanelRow1.add(label);
        nPanel.add(nPanelRow1);

        startupCB = new JCheckBox(Bundle.getMessage("LabelStartup"), isStartUpAction());
        startupItemListener = (ItemEvent e) -> {
            this.startupCB.removeItemListener(this.startupItemListener);
            StartupActionsManager manager = InstanceManager.getDefault(StartupActionsManager.class);
            if (this.startupCB.isSelected()) {
                PerformActionModel model = new PerformActionModel();
                model.setClassName(WiThrottleCreationAction.class.getName());
                if (this.startupActionPosition == -1 || this.startupActionPosition >= manager.getActions().length) {
                    manager.addAction(model);
                } else {
                    manager.setActions(this.startupActionPosition, model);
                }
            } else {
                manager.getActions(PerformActionModel.class).stream().filter((model) -> (WiThrottleCreationAction.class.getName().equals(model.getClassName()))).forEach((model) -> {
                    this.startupActionPosition = Arrays.asList(manager.getActions()).indexOf(model);
                    manager.removeAction(model);
                });
            }
            this.startupCB.addItemListener(this.startupItemListener);
        };
        this.startupCB.addItemListener(this.startupItemListener);
        nPanelRow2.add(startupCB);
        nPanel.add(nPanelRow2);

        useIPv4CB = new JCheckBox(Bundle.getMessage("LabelUseIPv4"), isUseIPv4());
        useIPv4CB.setToolTipText(Bundle.getMessage("ToolTipUseIPv4"));
        nPanelRow3.add(useIPv4CB);
        useIPv6CB = new JCheckBox(Bundle.getMessage("LabelUseIPv6"), isUseIPv6());
        useIPv6CB.setToolTipText(Bundle.getMessage("ToolTipUseIPv6"));
        nPanelRow3.add(useIPv6CB);
        nPanel.add(nPanelRow3);

        return nPanel;
    }

    private JPanel allowedControllers() {
        JPanel panel = new JPanel();

        powerCB = new JCheckBox(Bundle.getMessage("LabelTrackPower"));
        powerCB.setToolTipText(Bundle.getMessage("ToolTipTrackPower"));

        turnoutCB = new JCheckBox(Bundle.getMessage("Turnouts"));
        turnoutCB.setToolTipText(Bundle.getMessage("ToolTipTurnout"));

        turnoutCreationCB = new JCheckBox(Bundle.getMessage("TurnoutCreation"));
        turnoutCreationCB.setToolTipText(Bundle.getMessage("ToolTipTurnoutCreation"));

        routeCB = new JCheckBox(Bundle.getMessage("LabelRoute"));
        routeCB.setToolTipText(Bundle.getMessage("ToolTipRoute"));

        fastClockDisplayCB = new JCheckBox(Bundle.getMessage("LabelFastClockDisplayed"));
        fastClockDisplayCB.setToolTipText(Bundle.getMessage("ToolTipFastClockDisplayed"));

        consistCB = new JCheckBox(Bundle.getMessage("LabelConsist"));
        consistCB.setToolTipText(Bundle.getMessage("ToolTipConsist"));

        wifiRB = new JRadioButton(Bundle.getMessage("LabelWiFiConsist"));
        wifiRB.setToolTipText(Bundle.getMessage("ToolTipWiFiConsist"));
        dccRB = new JRadioButton(Bundle.getMessage("LabelDCCConsist"));
        dccRB.setToolTipText(Bundle.getMessage("ToolTipDCCConsist"));

        ButtonGroup group = new ButtonGroup();
        group.add(wifiRB);
        group.add(dccRB);

        JPanel gridPanel = new JPanel(new GridLayout(0, 2));
        JPanel conPanel = new JPanel();

        gridPanel.add(powerCB);
        gridPanel.add(fastClockDisplayCB);
        gridPanel.add(turnoutCB);
        gridPanel.add(turnoutCreationCB);
        gridPanel.add(routeCB);

        conPanel.setLayout(new BoxLayout(conPanel, BoxLayout.Y_AXIS));
        wifiRB.setMargin(new Insets(0, 20, 0, 0));
        dccRB.setMargin(new Insets(0, 20, 0, 0));
        conPanel.add(consistCB);
        conPanel.add(wifiRB);
        conPanel.add(dccRB);

        panel.setLayout(new FlowLayout(FlowLayout.CENTER, 40, 0));
        panel.add(gridPanel);
        panel.add(conPanel);

        return panel;
    }

    //private final static Logger log = LoggerFactory.getLogger(WiThrottlePrefsPanel.class);
    @Override
    public String getPreferencesItem() {
        return "WITHROTTLE"; // NOI18N
    }

    @Override
    public String getPreferencesItemText() {
        return Bundle.getMessage("MenuMenu"); // NOI18N
    }

    @Override
    public String getTabbedPreferencesTitle() {
        return getPreferencesItemText();
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
        if (setValues()) {
            this.localPrefs.save();
        }
    }

    @Override
    public boolean isDirty() {
        return this.localPrefs.isDirty();
    }

    @Override
    public boolean isRestartRequired() {
        return this.localPrefs.isRestartRequired();
    }

    @Override
    public boolean isPreferencesValid() {
        return true; // no validity checking performed
    }

    private boolean isStartUpAction() {
        return InstanceManager.getDefault(StartupActionsManager.class).getActions(PerformActionModel.class).stream()
                .anyMatch((model) -> (WiThrottleCreationAction.class.getName().equals(model.getClassName())));
    }

    private boolean isUseIPv4() {
        return InstanceManager.getDefault(ZeroConfServiceManager.class).getPreferences().isUseIPv4();
    }

    private boolean isUseIPv6() {
        return InstanceManager.getDefault(ZeroConfServiceManager.class).getPreferences().isUseIPv6();
    }
}
