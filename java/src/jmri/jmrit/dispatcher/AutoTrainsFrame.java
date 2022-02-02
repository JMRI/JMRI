package jmri.jmrit.dispatcher;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import java.beans.PropertyChangeEvent;

import javax.swing.JToolBar;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.plaf.basic.BasicToolBarUI;

import jmri.Throttle;
import jmri.jmrit.roster.RosterEntry;
import jmri.util.JmriJFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AutoTrainsFrame provides a user interface to trains that are running
 * automatically under Dispatcher.
 * <p>
 * There is only one AutoTrains window. AutoTrains are added and deleted from
 * this window as they are added or terminated.
 * <p>
 * This file is part of JMRI.
 * <p>
 * JMRI is open source software; you can redistribute it and/or modify it under
 * the terms of version 2 of the GNU General Public License as published by the
 * Free Software Foundation. See the "COPYING" file for a copy of this license.
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * @author Dave Duchamp Copyright (C) 2010
 */
public class AutoTrainsFrame extends jmri.util.JmriJFrame {

    public AutoTrainsFrame(DispatcherFrame disp) {
        super(false, true);
        initializeAutoTrainsWindow();
    }

    // instance variables
    private final ArrayList<AutoActiveTrain> _autoTrainsList = new ArrayList<>();
    //Keep track of throttle and listeners to update frame with their current state.

    // accessor functions
    public ArrayList<AutoActiveTrain> getAutoTrainsList() {
        return _autoTrainsList;
    }

    /**
     * Creates and initializes a new control of type AutoTrainControl
     * @param autoActiveTrain the new train.
     */
    public void addAutoActiveTrain(AutoActiveTrain autoActiveTrain) {
        if (autoActiveTrain != null) {
            log.debug("Adding ActiveTrain[{}]",autoActiveTrain.getActiveTrain().getActiveTrainName());
            AutoTrainControl atn = new AutoTrainControl(autoActiveTrain);
            if (!trainsCanBeFloated.isSelected()) {
                atn.componentJPanel.setFloatable(false);
            }
            trainsPanel.add(atn);
            atn.addPropertyChangeListener("terminated", (PropertyChangeEvent e) -> {
                AutoTrainControl atnn = (AutoTrainControl) e.getSource();
                // must be attached to make it really go away
                ((BasicToolBarUI) atnn.componentJPanel.getUI()).setFloating(false,null);
                trainsPanel.remove((AutoTrainControl) e.getSource());
                pack();
            });
            // bit of overkill for when a floater floats and comes back.
            atn.componentJPanel.addAncestorListener ( new AncestorListener ()
            {
                @Override
                public void ancestorAdded ( AncestorEvent event )
                {
                    log.trace("ancestorAdded");
                    pack();
                }
                @Override
                public void ancestorRemoved ( AncestorEvent event )
                {
                    log.trace("ancestorRemoved");
                    pack();
                }
                @Override
                public void ancestorMoved ( AncestorEvent event )
                {
                    // blank.
                }
              } );
            pack();
        }
    }
    
    // variables for AutoTrains window
    protected JmriJFrame autoTrainsFrame = null;
    private JPanel trainsPanel;
    private JScrollPane trainScrollPanel;
    private JCheckBoxMenuItem frameHasScrollBars = new JCheckBoxMenuItem(Bundle.getMessage("AutoTrainsFrameUseScrollBars"));
    private JCheckBoxMenuItem trainsCanBeFloated = new JCheckBoxMenuItem(Bundle.getMessage("AutoTrainsFrameAllowFloat"));

    jmri.UserPreferencesManager prefMan;
    
    private void initializeAutoTrainsWindow() {

        prefMan = jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class);
        frameHasScrollBars.setSelected(prefMan.getSimplePreferenceState(hasScrollBars));
        trainsCanBeFloated.setSelected(prefMan.getSimplePreferenceState(canFloat));

        autoTrainsFrame = this;
        autoTrainsFrame.setTitle(Bundle.getMessage("TitleAutoTrains"));
        trainsPanel = new JPanel();
        trainsPanel.setLayout(new BoxLayout(trainsPanel, BoxLayout.Y_AXIS));
        JMenuBar menuBar = new JMenuBar();
        JMenu optMenu = new JMenu(Bundle.getMessage("MenuOptions")); // NOI18N
        optMenu.add(frameHasScrollBars);
        frameHasScrollBars.addActionListener(e -> {
            setScrollBars();
        });

        optMenu.add(trainsCanBeFloated);
        trainsCanBeFloated.addActionListener(e -> {
            for (Object ob : trainsPanel.getComponents()) {
                if (ob instanceof AutoTrainControl) {
                    AutoTrainControl atnn = (AutoTrainControl) ob;
                    if (trainsCanBeFloated.isSelected()) {
                        atnn.componentJPanel.setFloatable(true);
                    } else {
                        // rejoin floating throttles before banning
                        // floating.
                        ((BasicToolBarUI) atnn.componentJPanel.getUI()).setFloating(false, null);
                        atnn.componentJPanel.setFloatable(false);
                    }
                }
            }
        });
        menuBar.add(optMenu);

        setJMenuBar(menuBar);
        autoTrainsFrame.addHelpMenu("package.jmri.jmrit.dispatcher.AutoTrains", true);
        trainsPanel.setLayout(new BoxLayout(trainsPanel, BoxLayout.Y_AXIS));
        JPanel pB = new JPanel();
        pB.setLayout(new FlowLayout());
        JButton stopAllButton = new JButton(Bundle.getMessage("StopAll"));
        pB.add(stopAllButton);
        stopAllButton.addActionListener(this::stopAllPressed);
        stopAllButton.setToolTipText(Bundle.getMessage("StopAllButtonHint"));
        trainsPanel.add(pB);
        trainsPanel.add(new JSeparator());
        trainsPanel.addComponentListener(this);
        trainsPanel.setVisible(true);
        trainsPanel.revalidate();
        trainScrollPanel = new JScrollPane();
        trainScrollPanel.getViewport().add(trainsPanel);
        autoTrainsFrame.getContentPane().setLayout(new BoxLayout(autoTrainsFrame.getContentPane(), BoxLayout.Y_AXIS));
        autoTrainsFrame.getContentPane().add(trainScrollPanel);
        setScrollBars();
        autoTrainsFrame.getContentPane().revalidate();
        autoTrainsFrame.pack();
        autoTrainsFrame.setVisible(true);

    }
    
    private void setScrollBars() {
        if (frameHasScrollBars.isSelected()) {
            trainScrollPanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            trainScrollPanel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            autoTrainsFrame.getContentPane().revalidate();
        } else {
            trainScrollPanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
            trainScrollPanel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            autoTrainsFrame.getContentPane().revalidate();
        }
    }

    private void stopAllPressed(ActionEvent e) {
        for (Object ob: autoTrainsFrame.getContentPane().getComponents()) {
            if (ob instanceof AutoTrainControl) {
                ((AutoTrainControl) ob).stopAll();
            }
        }
    }
    
    @Override
    public void dispose() {
        if (prefMan!=null) {
            prefMan.setSimplePreferenceState(hasScrollBars, frameHasScrollBars.isSelected());
            prefMan.setSimplePreferenceState(canFloat, trainsCanBeFloated.isSelected());
        }
        super.dispose();
    }
    String hasScrollBars = this.getClass().getName() + ".HasScrollBars"; // NOI18N
    String canFloat = this.getClass().getName() + ".CanFloat"; // NOI18N

    class AutoTrainControl extends JPanel {

        public AutoTrainControl(AutoActiveTrain autoActiveTrain) {

            this.autoActiveTrain = autoActiveTrain;
            activeTrain = autoActiveTrain.getActiveTrain();
            activeTrain.addPropertyChangeListener(activeTrainListener = new java.beans.PropertyChangeListener() {
                @Override
                public void propertyChange(java.beans.PropertyChangeEvent e) {
                    handleActiveTrainListen(e);
                }
            });
            rosterEntry = autoActiveTrain.getRosterEntry();
            drawComponent();
        }

        protected void stopAll() {
            if (activeTrain.getStatus() != ActiveTrain.STOPPED &&
                    activeTrain.getStatus() != ActiveTrain.DONE) {
                autoActiveTrain.getAutoEngineer().setHalt(true);
                autoActiveTrain.saveSpeedAndDirection();
                autoActiveTrain.setSavedStatus(activeTrain.getStatus());
                activeTrain.setStatus(ActiveTrain.STOPPED);
            }
        }

        private AutoActiveTrain autoActiveTrain = null;
        private java.beans.PropertyChangeListener activeTrainListener = null;
        private java.beans.PropertyChangeListener throttleListener = null;
        private jmri.Throttle throttle = null;
        private ActiveTrain activeTrain = null;
        private RosterEntry rosterEntry = null;

        private JLabel trainLabel;
        private JLabel throttleStatus;
        private JButton stopButton;
        private JButton resumeAutoRunningButton;
        private JRadioButton forwardButton;
        private JRadioButton reverseButton;
        private JSlider speedSlider;
        private JButton manualButton;

        private void handleThrottleListen(java.beans.PropertyChangeEvent e) {
            if (!e.getPropertyName().equals(Throttle.SPEEDSETTING) && !e.getPropertyName().equals(Throttle.ISFORWARD)) {
                return; // ignore if not speed or direction
            }
            updateThrottleDisplay();
        }

        private void updateThrottleDisplay() {
            StringBuilder sb = new StringBuilder();
            if (throttle != null) {
                if (rosterEntry != null && rosterEntry.getSpeedProfile() != null) {
                    sb.append("" +
                            rosterEntry.getSpeedProfile().convertThrottleSettingToScaleSpeedWithUnits(
                                    throttle.getSpeedSetting(),
                                    throttle.getIsForward()));
                } else {
                    sb.append("" + Math.round(throttle.getSpeedSetting() * 100));
                    sb.append("% ");
                }
                if (throttle.getIsForward()) {
                    sb.append("(fwd)");
                } else {
                    sb.append("(rev)");
                }
                if (forwardButton.isVisible() && throttle.getIsForward()) {
                    forwardButton.setSelected(throttle.getIsForward());
                }
                if (reverseButton.isVisible() && !throttle.getIsForward()) {
                    reverseButton.setSelected(throttle.getIsForward());
                }
                if (speedSlider.isVisible()) {
                    speedSlider.setValue((int)(throttle.getSpeedSetting() * 100.0f));
                }
                if (throttleStatus.isVisible()) {
                    throttleStatus.setText(sb.toString());
                }
            } else {
                if (throttleStatus.isVisible()) {
                    throttleStatus.setText("No Throttle");
                }
            }
        }

        private void handleActiveTrainListen(java.beans.PropertyChangeEvent e) {
            if (e.getNewValue() != null) {
            log.info("Property[{}] newValue[{}]",e.getPropertyName(),((Integer) e.getNewValue()).intValue());
            } else {
                log.info("Property[{}] newValue[{}]",e.getPropertyName(),"NULL");
            }
            if (e.getPropertyName().equals("mode")) {
                int newValue = ((Integer) e.getNewValue()).intValue();
                if (newValue == ActiveTrain.DISPATCHED) {
                    stopButton.setVisible(false);
                    manualButton.setVisible(false);
                    resumeAutoRunningButton.setVisible(true);
                    forwardButton.setVisible(false);
                    reverseButton.setVisible(false);
                    speedSlider.setVisible(false);
                    throttleStatus.setVisible(false);
                    jmri.InstanceManager.throttleManagerInstance().removeListener(throttle.getLocoAddress(),
                            throttleListener);
                } else if (newValue == ActiveTrain.AUTOMATIC) {
                    log.trace("[{}]:Set auto", autoActiveTrain.getActiveTrain().getActiveTrainName());
                    if (throttle == null && autoActiveTrain.getThrottle() != null) {
                        log.trace("[{}]:Set new throttle", autoActiveTrain.getActiveTrain().getActiveTrainName());
                        throttle = autoActiveTrain.getThrottle();
                        throttleListener = new java.beans.PropertyChangeListener() {
                            @Override
                            public void propertyChange(java.beans.PropertyChangeEvent e) {
                                handleThrottleListen(e);
                            }
                        };
                        throttle.addPropertyChangeListener(throttleListener);
                        stopButton.setText(Bundle.getMessage("StopButton"));
                        stopButton.setToolTipText(Bundle.getMessage("StopButtonHint"));
                        stopButton.setVisible(true);
                        manualButton.setText(Bundle.getMessage("ToManualButton"));
                        manualButton.setToolTipText(Bundle.getMessage("ToManualButtonHint"));
                        manualButton.setVisible(true);
                        resumeAutoRunningButton.setVisible(false);
                        forwardButton.setVisible(false);
                        reverseButton.setVisible(false);
                        speedSlider.setVisible(false);
                        throttleStatus.setVisible(true);
                        updateThrottleDisplay();
                    }
                } else if ((int) e.getNewValue() == ActiveTrain.TERMINATED) {
                    if (throttle != null && throttleListener != null) {
                        throttle.removePropertyChangeListener(throttleListener);
                        throttle = null;
                    }
                    activeTrain.removePropertyChangeListener(activeTrainListener);
                    // please someone stop me before I do something silly
                    firePropertyChange("terminated", null, null);
                }
            } else if (e.getPropertyName().equals("status")) {
                log.debug("NewStatus[{}]", e.getNewValue());
                if ((int) e.getNewValue() == ActiveTrain.STOPPED) {
                    stopButton.setText(Bundle.getMessage("ResumeButton"));
                    stopButton.setToolTipText(Bundle.getMessage("ResumeButtonHint"));
                    stopButton.setVisible(true);
                } else if ((int) e.getNewValue() == ActiveTrain.RUNNING ||
                        (int) e.getNewValue() == ActiveTrain.WAITING) {
                    log.trace("[{}]:Set auto STATUS RUNNING", autoActiveTrain.getActiveTrain().getActiveTrainName());
                    if (throttle == null && autoActiveTrain.getThrottle() != null) {
                        log.debug("[{}]:Set new throttle", autoActiveTrain.getActiveTrain().getActiveTrainName());
                        throttle = autoActiveTrain.getThrottle();
                        throttleListener = new java.beans.PropertyChangeListener() {
                            @Override
                            public void propertyChange(java.beans.PropertyChangeEvent e) {
                                handleThrottleListen(e);
                            }
                        };
                        throttle.addPropertyChangeListener(throttleListener);
                    }
                    stopButton.setText(Bundle.getMessage("StopButton"));
                    stopButton.setToolTipText(Bundle.getMessage("StopButtonHint"));
                    stopButton.setVisible(true);
                    manualButton.setText(Bundle.getMessage("ToManualButton"));
                    manualButton.setToolTipText(Bundle.getMessage("ToManualButtonHint"));
                    manualButton.setVisible(true);
                    resumeAutoRunningButton.setVisible(false);
                    forwardButton.setVisible(false);
                    reverseButton.setVisible(false);
                    speedSlider.setVisible(false);
                    throttleStatus.setVisible(true);
                    updateThrottleDisplay();
                } else if ((int) e.getNewValue() == ActiveTrain.DONE) {
                    stopButton.setText(Bundle.getMessage("RestartButton"));
                    stopButton.setToolTipText(Bundle.getMessage("RestartButtonHint"));
                    stopButton.setVisible(true);
                } else {
                    log.debug("Ignored newstatus[{}]", e.getNewValue());
                }
            }
            pack();
        }

        public void manualAutoTrain() {
            if (activeTrain.getMode() == ActiveTrain.AUTOMATIC) {
                activeTrain.setMode(ActiveTrain.MANUAL);
                manualButton.setText(Bundle.getMessage("ToAutoButton"));
                manualButton.setToolTipText(Bundle.getMessage("ToAutoButtonHint"));
                forwardButton.setVisible(true);
                reverseButton.setVisible(true);
                speedSlider.setVisible(true);
                if (autoActiveTrain.getAutoEngineer() != null) {
                    autoActiveTrain.saveSpeedAndDirection();
                    autoActiveTrain.getAutoEngineer().setHalt(true);
                    autoActiveTrain.setTargetSpeed(0.0f);
                    autoActiveTrain.waitUntilStopped();
                    autoActiveTrain.getAutoEngineer().setHalt(false);
                    if (throttle.getIsForward() ) {
                        forwardButton.setSelected(true);
                    } else {
                        reverseButton.setSelected(true);
                    }
                }

            } else if (activeTrain.getMode() == ActiveTrain.MANUAL) {
                activeTrain.setMode(ActiveTrain.AUTOMATIC);
                manualButton.setText(Bundle.getMessage("ToManualButton"));
                manualButton.setToolTipText(Bundle.getMessage("ToManualButtonHint"));
                manualButton.setVisible(true);
                forwardButton.setVisible(false);
                reverseButton.setVisible(false);
                speedSlider.setVisible(false);
                autoActiveTrain.restoreSavedSpeedAndDirection();
                // autoActiveTrain.setForward(!autoActiveTrain.getRunInReverse());
                if ((activeTrain.getStatus() == ActiveTrain.RUNNING) ||
                        (activeTrain.getStatus() == ActiveTrain.WAITING)) {
                    autoActiveTrain.setSpeedBySignal();
                }
            }
        }

        private JToolBar componentJPanel;

        private void drawComponent() {

            componentJPanel = new JToolBar();
            componentJPanel.setLayout(new FlowLayout());
            componentJPanel.setFloatable(true);

            trainLabel = new JLabel(autoActiveTrain.getActiveTrain().getTrainName());
            trainLabel.setVisible(true);
            componentJPanel.add(trainLabel);
            stopButton = new JButton(Bundle.getMessage("ResumeButton"));
            componentJPanel.add(stopButton);
            stopButton.addActionListener(e -> stopResume());
            manualButton = new JButton(Bundle.getMessage("ToManualButton"));
            componentJPanel.add(manualButton);
            manualButton.addActionListener(e -> manualAutoTrain());
            resumeAutoRunningButton = new JButton(Bundle.getMessage("ResumeAutoButton"));
            componentJPanel.add(resumeAutoRunningButton);
            resumeAutoRunningButton.addActionListener(e -> resumeAutoOperation());
            resumeAutoRunningButton.setVisible(false);
            resumeAutoRunningButton.setToolTipText(Bundle.getMessage("ResumeAutoButtonHint"));
            ButtonGroup directionGroup = new ButtonGroup();
            forwardButton = new JRadioButton(Bundle.getMessage("ForwardRadio"));
            componentJPanel.add(forwardButton);
            forwardButton.addActionListener(e -> directionButton());
            directionGroup.add(forwardButton);
            reverseButton = new JRadioButton(Bundle.getMessage("ReverseRadio"));
            componentJPanel.add(reverseButton);
            reverseButton.addActionListener(e -> directionButton());
            directionGroup.add(reverseButton);
            speedSlider = new JSlider(0, 100, 0);
            speedSlider.setPreferredSize(new Dimension(100, 20));
            componentJPanel.add(speedSlider);
            speedSlider.addChangeListener(e -> {
                int val = ((JSlider) (e.getSource())).getValue();
                sliderChanged(val);
            });

            throttleStatus = new JLabel();
            // prevent JFrame to resize on each % change
            throttleStatus.setPreferredSize(new Dimension(100, 20));
            throttleStatus.setText("Speed Unknown");
            componentJPanel.add(throttleStatus);
            componentJPanel.revalidate();
            add(componentJPanel, BorderLayout.EAST);
            pack();
        }

        public void stopResume() {
            if (autoActiveTrain.getAutoEngineer() != null) {
                ActiveTrain at = autoActiveTrain.getActiveTrain();
                if (at.getStatus() == ActiveTrain.STOPPED) {
                    log.trace("Train Is Stopped - Resume");
                    autoActiveTrain.setEngineDirection();
                    autoActiveTrain.getAutoEngineer().setHalt(false);
                    autoActiveTrain.restoreSavedSpeedAndDirection();
                    at.setStatus(autoActiveTrain.getSavedStatus());
                    if ((at.getStatus() == ActiveTrain.RUNNING) || (at.getStatus() == ActiveTrain.WAITING)) {
                        autoActiveTrain.setSpeedBySignal();
                    }
                } else if (at.getStatus() == ActiveTrain.DONE) {
                    log.trace("Train Is Done - Restart");
                    // restart
                    at.allocateAFresh();
                    at.restart();
                } else {
                    log.trace("Process As Stop");
                    // stop
                    autoActiveTrain.getAutoEngineer().setHalt(true);
                    autoActiveTrain.saveSpeedAndDirection();
                    autoActiveTrain.setSavedStatus(at.getStatus());
                    at.setStatus(ActiveTrain.STOPPED);
                    if (at.getMode() == ActiveTrain.MANUAL) {
                        speedSlider.setValue(0);
                    }
                }
            } else {
                log.error("unexpected null autoEngineer");
            }
        }


        public void resumeAutoOperation() {
            autoActiveTrain.resumeAutomaticRunning();
        }

        public void directionButton() {
            ActiveTrain at = autoActiveTrain.getActiveTrain();
            if (at.getMode() == ActiveTrain.MANUAL) {
                autoActiveTrain.setForward(forwardButton.isSelected());
            } else {
                log.warn("unexpected direction button change on line {}", at.getTrainName());
            }
        }

        public void sliderChanged(int value) {
            ActiveTrain at = autoActiveTrain.getActiveTrain();
            if (at.getMode() == ActiveTrain.MANUAL) {
                float speedValue = value;
                speedValue = speedValue * 0.01f;
                autoActiveTrain.getAutoEngineer().setSpeedImmediate(speedValue);
            } else {
                log.warn("unexpected slider change on line {}", at.getTrainName());
            }
        }
    }

        private final static Logger log = LoggerFactory.getLogger(AutoTrainsFrame.class);

}


