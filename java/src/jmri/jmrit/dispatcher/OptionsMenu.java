package jmri.jmrit.dispatcher;

import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Enumeration;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import jmri.InstanceManager;
import jmri.Scale;
import jmri.ScaleManager;
import jmri.implementation.SignalSpeedMap;
import jmri.jmrit.display.PanelMenu;
import jmri.jmrit.display.layoutEditor.LayoutEditor;
import jmri.util.JmriJFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Set up and processes items in the Dispatcher Options menu.
 *
 * @author Dave Duchamp Copyright (C) 2008
 */
public class OptionsMenu extends JMenu {

    public OptionsMenu(DispatcherFrame f) {
        dispatcher = f;
        this.setText(Bundle.getMessage("MenuOptions"));
        autoDispatchItem = new JCheckBoxMenuItem(Bundle.getMessage("AutoDispatchItem"));
        this.add(autoDispatchItem);
        autoDispatchItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                handleAutoDispatch(event);
            }
        });
        autoTurnoutsItem = new JCheckBoxMenuItem(Bundle.getMessage("AutoTurnoutsItem"));
        this.add(autoTurnoutsItem);
        autoTurnoutsItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                handleAutoTurnouts(event);
            }
        });
        JMenuItem optionWindowItem = new JMenuItem(Bundle.getMessage("OptionWindowItem") + "...");
        this.add(optionWindowItem);
        optionWindowItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                optionWindowRequested(event);
            }
        });
        JMenuItem saveOptionsItem = new JMenuItem(Bundle.getMessage("SaveOptionsItem"));
        this.add(saveOptionsItem);
        saveOptionsItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                saveRequested(event);
            }
        });
        initializeMenu();
    }

    protected DispatcherFrame dispatcher = null;

    // Option menu items
    private JCheckBoxMenuItem autoDispatchItem = null;
    private JCheckBoxMenuItem autoTurnoutsItem = null;

    // Initialize check box items in menu from Dispatcher
    public void initializeMenu() {
        autoDispatchItem.setSelected(dispatcher.getAutoAllocate());
        autoTurnoutsItem.setSelected(dispatcher.getAutoTurnouts());
    }

    private void handleAutoDispatch(ActionEvent e) {
        boolean set = autoDispatchItem.isSelected();
        dispatcher.setAutoAllocate(set);
    }

    private void handleAutoTurnouts(ActionEvent e) {
        boolean set = autoTurnoutsItem.isSelected();
        dispatcher.setAutoTurnouts(set);
    }

    // options window items
    JmriJFrame optionsFrame = null;
    Container optionsPane = null;
    JCheckBox useConnectivityCheckBox = new JCheckBox(Bundle.getMessage("UseConnectivity"));
    JComboBox<String> layoutEditorBox = new JComboBox<>();
    ArrayList<LayoutEditor> layoutEditorList = new ArrayList<>();
    JCheckBox autoAllocateCheckBox = new JCheckBox(Bundle.getMessage("AutoAllocateBox"));
    JCheckBox autoTurnoutsCheckBox = new JCheckBox(Bundle.getMessage("AutoTurnoutsBox"));
    JRadioButton trainsFromRoster = new JRadioButton(Bundle.getMessage("TrainsFromRoster"));
    JRadioButton trainsFromTrains = new JRadioButton(Bundle.getMessage("TrainsFromTrains"));
    JRadioButton trainsFromUser = new JRadioButton(Bundle.getMessage("TrainsFromUser"));
    JComboBox<String> signalTypeBox;
    JCheckBox detectionCheckBox = new JCheckBox(Bundle.getMessage("DetectionBox"));
    JCheckBox shortNameCheckBox = new JCheckBox(Bundle.getMessage("ShortNameBox"));
    JCheckBox nameInBlockCheckBox = new JCheckBox(Bundle.getMessage("NameInBlockBox"));
    JCheckBox rosterInBlockCheckBox = new JCheckBox(Bundle.getMessage("RosterInBlockBox"));
    JCheckBox extraColorForAllocatedCheckBox = new JCheckBox(Bundle.getMessage("ExtraColorForAllocatedBox"));
    JCheckBox nameInAllocatedBlockCheckBox = new JCheckBox(Bundle.getMessage("NameInAllocatedBlockBox"));
    JCheckBox supportVSDecoderCheckBox = new JCheckBox(Bundle.getMessage("SupportVSDecoder"));
    JComboBox<Scale> layoutScaleBox = new JComboBox<>();
    JRadioButton scaleFeet = new JRadioButton(Bundle.getMessage("ScaleFeet"));
    JRadioButton scaleMeters = new JRadioButton(Bundle.getMessage("ScaleMeters"));
    JCheckBox openDispatcherWithPanel = new JCheckBox(Bundle.getMessage("OpenDispatcherWithPanelBox"));
    JSpinner minThrottleIntervalSpinner = new JSpinner(new SpinnerNumberModel(100, 20, 1000, 1));
    JSpinner fullRampTimeSpinner = new JSpinner(new SpinnerNumberModel(5000, 1000, 20000, 1));
    JCheckBox trustKnownTurnoutsCheckBox = new JCheckBox(Bundle.getMessage("trustKnownTurnouts"));
    JComboBox<String> stoppingSpeedBox = new JComboBox<>();

    String[] signalTypes = {Bundle.getMessage("SignalType1"), Bundle.getMessage("SignalType2")};

    private void optionWindowRequested(ActionEvent e) {
        if (optionsFrame == null) {
            optionsFrame = new JmriJFrame(Bundle.getMessage("OptionWindowItem"), false, true);
            optionsFrame.addHelpMenu("package.jmri.jmrit.dispatcher.Options", true);
            optionsPane = optionsFrame.getContentPane();
            optionsPane.setLayout(new BoxLayout(optionsFrame.getContentPane(), BoxLayout.Y_AXIS));
            JPanel p1 = new JPanel();
            p1.setLayout(new FlowLayout());
            p1.add(useConnectivityCheckBox);
            useConnectivityCheckBox.setToolTipText(Bundle.getMessage("UseConnectivityHint"));
            p1.add(layoutEditorBox);
            layoutEditorBox.setToolTipText(Bundle.getMessage("LayoutEditorHint"));
            signalTypeBox = new JComboBox<>(signalTypes);
            p1.add(signalTypeBox);
            signalTypeBox.setToolTipText(Bundle.getMessage("SignalTypeHint"));
            optionsPane.add(p1);
            JPanel p2 = new JPanel();
            p2.setLayout(new FlowLayout());
            ButtonGroup trainsGroup = new ButtonGroup();
            p2.add(trainsFromRoster);
            trainsFromRoster.setToolTipText(Bundle.getMessage("TrainsFromRosterHint"));
            trainsGroup.add(trainsFromRoster);

            ActionListener useRosterEntryListener = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (trainsFromRoster.isSelected()) {
                        rosterInBlockCheckBox.setEnabled(true);
                        if (nameInBlockCheckBox.isSelected() && e.getSource() == nameInBlockCheckBox) {
                            rosterInBlockCheckBox.setSelected(false);
                        } else if (rosterInBlockCheckBox.isSelected() && e.getSource() == rosterInBlockCheckBox) {
                            nameInBlockCheckBox.setSelected(false);
                        }
                    } else {
                        rosterInBlockCheckBox.setEnabled(false);
                    }
                }
            };
            trainsFromRoster.addActionListener(useRosterEntryListener);
            p2.add(new JLabel("     "));
            p2.add(trainsFromTrains);
            trainsFromTrains.setToolTipText(Bundle.getMessage("TrainsFromTrainsHint"));
            trainsFromTrains.addActionListener(useRosterEntryListener);
            trainsGroup.add(trainsFromTrains);
            p2.add(new JLabel("     "));
            p2.add(trainsFromUser);
            trainsFromUser.setToolTipText(Bundle.getMessage("TrainsFromUserHint"));
            trainsFromUser.addActionListener(useRosterEntryListener);
            trainsGroup.add(trainsFromUser);
            optionsPane.add(p2);
            JPanel p3 = new JPanel();
            p3.setLayout(new FlowLayout());
            p3.add(detectionCheckBox);
            detectionCheckBox.setToolTipText(Bundle.getMessage("DetectionBoxHint"));
            optionsPane.add(p3);
            JPanel p4 = new JPanel();
            p4.setLayout(new FlowLayout());
            p4.add(autoAllocateCheckBox);
            autoAllocateCheckBox.setToolTipText(Bundle.getMessage("AutoAllocateBoxHint"));
            optionsPane.add(p4);
            JPanel p5 = new JPanel();
            p5.setLayout(new FlowLayout());
            p5.add(autoTurnoutsCheckBox);
            autoTurnoutsCheckBox.setToolTipText(Bundle.getMessage("AutoTurnoutsBoxHint"));
            optionsPane.add(p5);
            JPanel p16 = new JPanel();
            p16.setLayout(new FlowLayout());
            p16.add(trustKnownTurnoutsCheckBox);
            trustKnownTurnoutsCheckBox.setToolTipText(Bundle.getMessage("trustKnownTurnoutsHint"));
            optionsPane.add(p16);
            JPanel p6 = new JPanel();
            p6.setLayout(new FlowLayout());
            p6.add(shortNameCheckBox);
            shortNameCheckBox.setToolTipText(Bundle.getMessage("ShortNameBoxHint"));
            optionsPane.add(p6);
            JPanel p7 = new JPanel();
            p7.setLayout(new FlowLayout());
            p7.add(nameInBlockCheckBox);
            nameInBlockCheckBox.setToolTipText(Bundle.getMessage("NameInBlockBoxHint"));
            nameInBlockCheckBox.addActionListener(useRosterEntryListener);
            optionsPane.add(p7);
            JPanel p7b = new JPanel();
            p7b.setLayout(new FlowLayout());
            p7b.add(rosterInBlockCheckBox);
            rosterInBlockCheckBox.setToolTipText(Bundle.getMessage("RosterInBlockBoxHint"));
            rosterInBlockCheckBox.addActionListener(useRosterEntryListener);
            optionsPane.add(p7b);

            JPanel p10 = new JPanel();
            p10.setLayout(new FlowLayout());
            p10.add(extraColorForAllocatedCheckBox);
            extraColorForAllocatedCheckBox.setToolTipText(Bundle.getMessage("ExtraColorForAllocatedBoxHint"));
            optionsPane.add(p10);
            JPanel p11 = new JPanel();
            p11.setLayout(new FlowLayout());
            p11.add(nameInAllocatedBlockCheckBox);
            nameInAllocatedBlockCheckBox.setToolTipText(Bundle.getMessage("NameInAllocatedBlockBoxHint"));
            optionsPane.add(p11);
            JPanel p13 = new JPanel();
            p13.setLayout(new FlowLayout());
            p13.add(supportVSDecoderCheckBox);
            supportVSDecoderCheckBox.setToolTipText(Bundle.getMessage("SupportVSDecoderBoxHint"));
            optionsPane.add(p13);
            JPanel p8 = new JPanel();
            initializeScaleCombo();
            p8.add(new JLabel(Bundle.getMessage("LabelLayoutScale")));
            p8.add(layoutScaleBox);
            layoutScaleBox.setToolTipText(Bundle.getMessage("ScaleBoxHint"));
            optionsPane.add(p8);
            JPanel p12 = new JPanel();
            p12.setLayout(new FlowLayout());
            p12.add(new JLabel(Bundle.getMessage("Units") + "  "));
            ButtonGroup scaleGroup = new ButtonGroup();
            p12.add(scaleFeet);
            scaleFeet.setToolTipText(Bundle.getMessage("ScaleFeetHint"));
            scaleGroup.add(scaleFeet);
            p12.add(new JLabel("  "));
            p12.add(scaleMeters);
            scaleMeters.setToolTipText(Bundle.getMessage("ScaleMetersHint"));
            scaleGroup.add(scaleMeters);
            optionsPane.add(p12);

            JPanel p14 = new JPanel();
            initializeStoppingSpeedCombo();
            p14.add(new JLabel(Bundle.getMessage("LabelStoppingSpeed")));
            p14.add(stoppingSpeedBox);
            stoppingSpeedBox.setToolTipText(Bundle.getMessage("StoppingSpeedHint"));
            optionsPane.add(p14);

            JPanel p15 = new JPanel();
            p15.setLayout(new FlowLayout());
            p15.add(new JLabel(Bundle.getMessage("minThrottleInterval") + ":"));
            minThrottleIntervalSpinner.setToolTipText(Bundle.getMessage("minThrottleIntervalHint"));
            p15.add(minThrottleIntervalSpinner);
            p15.add(new JLabel(Bundle.getMessage("LabelMilliseconds")));
            optionsPane.add(p15);

            JPanel p17 = new JPanel();
            p17.setLayout(new FlowLayout());
            p17.add(new JLabel(Bundle.getMessage("fullRampTime") + " :"));
            fullRampTimeSpinner.setToolTipText(Bundle.getMessage("fullRampTimeHint", Bundle.getMessage("RAMP_FAST")));
            p17.add(fullRampTimeSpinner);
            p17.add(new JLabel(Bundle.getMessage("LabelMilliseconds")));
            optionsPane.add(p17);

            JPanel p18 = new JPanel();
            p18.setLayout(new FlowLayout());
            p18.add(openDispatcherWithPanel);
            openDispatcherWithPanel.setToolTipText(Bundle.getMessage("OpenDispatcherWithPanelBoxHint"));
            optionsPane.add(p18);

            optionsPane.add(new JSeparator());
            JPanel p9 = new JPanel();
            p9.setLayout(new FlowLayout());
            JButton cancelButton = null;
            p9.add(cancelButton = new JButton(Bundle.getMessage("ButtonCancel")));
            cancelButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    cancelOptions(e);
                }
            });
            cancelButton.setToolTipText(Bundle.getMessage("CancelButtonHint2"));
            p9.add(new JLabel("     "));
            JButton applyButton = null;
            p9.add(applyButton = new JButton(Bundle.getMessage("ButtonApply")));
            applyButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    applyOptions(e);
                }
            });
            applyButton.setToolTipText(Bundle.getMessage("ApplyButtonHint"));
            optionsPane.add(p9);
        }
        if (initializeLayoutEditorCombo()) {
            useConnectivityCheckBox.setVisible(true);
            layoutEditorBox.setVisible(true);
        } else {
            useConnectivityCheckBox.setVisible(false);
            layoutEditorBox.setVisible(false);
        }
        useConnectivityCheckBox.setSelected(dispatcher.getUseConnectivity());
        signalTypeBox.setSelectedIndex(dispatcher.getSignalType());
        trainsFromRoster.setSelected(dispatcher.getTrainsFromRoster());
        trainsFromTrains.setSelected(dispatcher.getTrainsFromTrains());
        trainsFromUser.setSelected(dispatcher.getTrainsFromUser());
        detectionCheckBox.setSelected(dispatcher.getHasOccupancyDetection());
        autoAllocateCheckBox.setSelected(dispatcher.getAutoAllocate());
        autoTurnoutsCheckBox.setSelected(dispatcher.getAutoTurnouts());
        trustKnownTurnoutsCheckBox.setSelected(dispatcher.getTrustKnownTurnouts());
        shortNameCheckBox.setSelected(dispatcher.getShortActiveTrainNames());
        nameInBlockCheckBox.setSelected(dispatcher.getShortNameInBlock());
        rosterInBlockCheckBox.setSelected(dispatcher.getRosterEntryInBlock());
        extraColorForAllocatedCheckBox.setSelected(dispatcher.getExtraColorForAllocated());
        nameInAllocatedBlockCheckBox.setSelected(dispatcher.getNameInAllocatedBlock());
        supportVSDecoderCheckBox.setSelected(dispatcher.getSupportVSDecoder());
        scaleMeters.setSelected(dispatcher.getUseScaleMeters());
        scaleFeet.setSelected(!dispatcher.getUseScaleMeters());
        minThrottleIntervalSpinner.setValue(dispatcher.getMinThrottleInterval());
        fullRampTimeSpinner.setValue(dispatcher.getFullRampTime());

        if (dispatcher.getLayoutEditor() != null) {
            openDispatcherWithPanel.setSelected(dispatcher.getLayoutEditor().getOpenDispatcherOnLoad());
        }
        optionsFrame.pack();
        optionsFrame.setVisible(true);
    }

    private void applyOptions(ActionEvent e) {
        if (layoutEditorList.size() > 0) {
            int index = layoutEditorBox.getSelectedIndex();
            dispatcher.setLayoutEditor(layoutEditorList.get(index));
            dispatcher.setUseConnectivity(useConnectivityCheckBox.isSelected());
        }
        dispatcher.setTrainsFromRoster(trainsFromRoster.isSelected());
        dispatcher.setTrainsFromTrains(trainsFromTrains.isSelected());
        dispatcher.setTrainsFromUser(trainsFromUser.isSelected());
        dispatcher.setHasOccupancyDetection(detectionCheckBox.isSelected());
        dispatcher.setAutoAllocate(autoAllocateCheckBox.isSelected());
        autoDispatchItem.setSelected(autoAllocateCheckBox.isSelected());
        dispatcher.setAutoTurnouts(autoTurnoutsCheckBox.isSelected());
        autoTurnoutsItem.setSelected(autoTurnoutsCheckBox.isSelected());
        dispatcher.setTrustKnownTurnouts(trustKnownTurnoutsCheckBox.isSelected());
        dispatcher.setSignalType(signalTypeBox.getSelectedIndex());
        if (autoTurnoutsCheckBox.isSelected() && ((layoutEditorList.size() == 0)
                || (!useConnectivityCheckBox.isSelected()))) {
            JOptionPane.showMessageDialog(optionsFrame, Bundle.getMessage(
                    "AutoTurnoutsWarn"), Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
        }
        dispatcher.setShortActiveTrainNames(shortNameCheckBox.isSelected());
        dispatcher.setShortNameInBlock(nameInBlockCheckBox.isSelected());
        dispatcher.setExtraColorForAllocated(extraColorForAllocatedCheckBox.isSelected());
        dispatcher.setNameInAllocatedBlock(nameInAllocatedBlockCheckBox.isSelected());
        dispatcher.setRosterEntryInBlock(rosterInBlockCheckBox.isSelected());
        dispatcher.setSupportVSDecoder(supportVSDecoderCheckBox.isSelected());
        dispatcher.setScale((Scale) layoutScaleBox.getSelectedItem());
        dispatcher.setUseScaleMeters(scaleMeters.isSelected());
        dispatcher.setMinThrottleInterval((int) minThrottleIntervalSpinner.getValue());
        dispatcher.setFullRampTime((int) fullRampTimeSpinner.getValue());
        dispatcher.getLayoutEditor().setOpenDispatcherOnLoad(openDispatcherWithPanel.isSelected());
        dispatcher.setStoppingSpeedName( (String) stoppingSpeedBox.getSelectedItem());
        optionsFrame.setVisible(false);
        optionsFrame.dispose(); // prevent this window from being listed in the Window menu.
        optionsFrame = null;
        // display save options reminder
        InstanceManager.getDefault(jmri.UserPreferencesManager.class).
                showInfoMessage(Bundle.getMessage("ReminderTitle"), Bundle.getMessage("ReminderSaveOptions"),
                        OptionsMenu.class.getName(),
                        "remindSaveDispatcherOptions"); // NOI18N
        initializeMenu();
    }

    private void cancelOptions(ActionEvent e) {
        optionsFrame.setVisible(false);
        optionsFrame.dispose(); // prevent this window from being listed in the Window menu.
        optionsFrame = null;
    }

    /**
     * Save Dispatcher Option settings from pane to xml file.
     *
     * @param e the calling actionevent
     */
    private void saveRequested(ActionEvent e) {
        try {
            InstanceManager.getDefault(OptionsFile.class).writeDispatcherOptions(dispatcher);
        } catch (java.io.IOException ioe) {
            log.error("Exception writing Dispatcher options: " + ioe);
        }
    }

    private boolean initializeLayoutEditorCombo() {
        // get list of Layout Editor panels
        layoutEditorList = InstanceManager.getDefault(PanelMenu.class).getLayoutEditorPanelList();
        if (layoutEditorList.size() == 0) {
            return false;
        }
        layoutEditorBox.removeAllItems();
        for (int i = 0; i < layoutEditorList.size(); i++) {
            layoutEditorBox.addItem(layoutEditorList.get(i).getTitle());
        }
        if (layoutEditorList.size() > 1) {
            LayoutEditor le = dispatcher.getLayoutEditor();
            for (int j = 0; j < layoutEditorList.size(); j++) {
                if (le == layoutEditorList.get(j)) {
                    layoutEditorBox.setSelectedIndex(j);
                }
            }
        } else {
            layoutEditorBox.setSelectedIndex(0);
        }
        return true;
    }

    private void initializeScaleCombo() {
        layoutScaleBox.removeAllItems();
        for (Scale scale : ScaleManager.getScales()) {
            if (scale.getScaleName().equals("CUSTOM")) {  // No custom support yet, don't show.
                continue;
            }
            layoutScaleBox.addItem(scale);
        }
        jmri.util.swing.JComboBoxUtil.setupComboBoxMaxRows(layoutScaleBox);
        layoutScaleBox.setSelectedItem(dispatcher.getScale());
    }

    private void initializeStoppingSpeedCombo() {
        stoppingSpeedBox.removeAllItems();
        Enumeration<String> speedNamesList = jmri.InstanceManager.getDefault(SignalSpeedMap.class).getSpeedIterator();
        while (speedNamesList.hasMoreElements()) {
            stoppingSpeedBox.addItem(speedNamesList.nextElement());
        }
        stoppingSpeedBox.setSelectedItem(dispatcher.getStoppingSpeedName());
    }

    private final static Logger log = LoggerFactory.getLogger(OptionsMenu.class);

}
