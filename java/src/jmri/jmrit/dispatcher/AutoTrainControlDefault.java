package jmri.jmrit.dispatcher;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.beans.PropertyChangeEvent;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JToolBar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.Throttle;

public class AutoTrainControlDefault extends AbstractAutoTrainControl  {

    public AutoTrainControlDefault(AutoActiveTrain autoActiveTrain) {
        super(autoActiveTrain);
    }

        private JLabel trainLabel;
        private JLabel throttleStatus;
        protected JButton stopButton;
        private JButton resumeAutoRunningButton;
        private JRadioButton forwardButton;
        private JRadioButton reverseButton;
        private JSlider speedSlider;
        private JButton manualButton;

        private float lastReportedSpeed;   // for display purposes

        /*
         * Updates screen control throttle.
         */
        private void primeThrottleDisplay() {
            if (throttle != null) {
                if (throttle.getIsForward()) {
                    forwardButton.setSelected(true);
                } else {
                    reverseButton.setSelected(true);
                }
                lastReportedSpeed = throttle.getSpeedSetting();
                if (speedSlider.isVisible()) {
                    speedSlider.setValue(Math.round(lastReportedSpeed * 100.0f));
                }
            }
            updateThrottleStatus();
        }

        /*
         * Updates control from events
         */
        private void updateThrottleDisplay(java.beans.PropertyChangeEvent e) {
            if (throttle != null) {
                if (e.getPropertyName().equals(Throttle.ISFORWARD)) {
                    if ((boolean) e.getNewValue()) {
                        forwardButton.setSelected(true);
                    } else {
                        reverseButton.setSelected(true);
                    }
                } else {
                    lastReportedSpeed = (float) e.getNewValue();
                    if (speedSlider.isValid()) {
                        speedSlider.setValue(Math.round(lastReportedSpeed * 100.0f));
                    }
                }
            }
            updateThrottleStatus();
        }

        /*
         * Updates the status words.
         */
        private void updateThrottleStatus() {
            StringBuilder sb = new StringBuilder();
            if (throttle != null && throttleStatus.isVisible()) {
                if (rosterEntry != null && rosterEntry.getSpeedProfile() != null) {
                    sb.append("" +
                            rosterEntry.getSpeedProfile().convertThrottleSettingToScaleSpeedWithUnits(
                                    lastReportedSpeed,
                                    forwardButton.isSelected()));

                } else {
                    sb.append("" + Math.round(throttle.getSpeedSetting() * 100));
                    sb.append("% ");
                }
                if (forwardButton.isSelected()) {
                    sb.append("(fwd)");
                } else {
                    sb.append("(rev)");
                }
                throttleStatus.setText(sb.toString());
            } else if (throttleStatus.isVisible()) {
                throttleStatus.setText("No Throttle");
            }
        }

        @Override
        protected void directionChange(PropertyChangeEvent e) {
            updateThrottleDisplay(e);
        }

        @Override
        protected void updateSpeedChange(PropertyChangeEvent e) {
            updateThrottleDisplay(e);
        }

        @Override
        protected void activeTrainNewModeDispatched() {
            stopButton.setVisible(false);
            manualButton.setVisible(false);
            resumeAutoRunningButton.setVisible(true);
            forwardButton.setVisible(false);
            reverseButton.setVisible(false);
            speedSlider.setVisible(false);
            throttleStatus.setVisible(false);
        }

        @Override
        protected void activeTrainNewModeAutomatic(){
            if (throttle != null) {
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
                setStatusLabelWidth();
                primeThrottleDisplay();
            }

        }
        @Override
        protected void activeTrainNewStatusStopped(){
            stopButton.setText(Bundle.getMessage("ResumeButton"));
            stopButton.setToolTipText(Bundle.getMessage("ResumeButtonHint"));
            stopButton.setVisible(true);
        }
        @Override
        protected void activeTrainNewStatusRunning(){
            if (throttle != null) {
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
                primeThrottleDisplay();
            }
        }
        @Override
        protected void activeTrainNewStatusWaiting(){
            if (throttle == null && autoActiveTrain.getThrottle() != null) {
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
                primeThrottleDisplay();
            }
        }
        @Override
        protected void activeTrainNewStatusDone() {
            stopButton.setText(Bundle.getMessage("RestartButton"));
            stopButton.setToolTipText(Bundle.getMessage("RestartButtonHint"));
            stopButton.setVisible(true);
        }

        public void manualAutoTrain() {
            if (activeTrain.getMode() == ActiveTrain.AUTOMATIC) {
                autoToManual();
            } else if (activeTrain.getMode() == ActiveTrain.MANUAL) {
                manualToAuto();
            }
        }

        @Override
        protected void autoToManual() {                activeTrain.setMode(ActiveTrain.MANUAL);

            super.autoToManual();
            if (throttle.getIsForward() ) {
                forwardButton.setSelected(true);
            } else {
                reverseButton.setSelected(true);
            }
            manualButton.setText(Bundle.getMessage("ToAutoButton"));
            manualButton.setToolTipText(Bundle.getMessage("ToAutoButtonHint"));
            forwardButton.setVisible(true);
            reverseButton.setVisible(true);
            speedSlider.setVisible(true);

        }
        @Override
        protected void manualToAuto() {
                activeTrain.setMode(ActiveTrain.AUTOMATIC);
                manualButton.setText(Bundle.getMessage("ToManualButton"));
                manualButton.setToolTipText(Bundle.getMessage("ToManualButtonHint"));
                manualButton.setVisible(true);
                forwardButton.setVisible(false);
                reverseButton.setVisible(false);
                speedSlider.setVisible(false);
                super.manualToAuto();
            }

        //private JToolBar componentJPanel;

        @Override
        protected void drawComponent() {

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
            throttleStatus.setText("Speed Unknown");
            componentJPanel.add(throttleStatus);
            componentJPanel.revalidate();
            add(componentJPanel, BorderLayout.EAST);
        }

        /*
         * Using dummy strings get max size of the statustext
         */
        private void setStatusLabelWidth() {
            if (rosterEntry!=null && autoActiveTrain.getUseSpeedProfile()) {
                throttleStatus.setPreferredSize(
                        new Dimension(getGraphics().getFontMetrics().stringWidth(rosterEntry.getSpeedProfile().convertThrottleSettingToScaleSpeedWithUnits(
                                1.0f, true)),
                                getGraphics().getFontMetrics().getHeight()));
            } else {
                throttleStatus.setPreferredSize(
                        new Dimension(getGraphics().getFontMetrics().stringWidth("100.0% FWD"),
                                getGraphics().getFontMetrics().getHeight()));
            }
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
                log.debug("unexpected direction button change on line {}", at.getTrainName());
            }
        }

        public void sliderChanged(int value) {
            ActiveTrain at = autoActiveTrain.getActiveTrain();
            if (at.getMode() == ActiveTrain.MANUAL) {
                float speedValue = value;
                speedValue = speedValue * 0.01f;
                autoActiveTrain.getAutoEngineer().setSpeedImmediate(speedValue);
            } else {
                log.debug("unexpected slider change on line {}", at.getTrainName());
            }
        }
        private final static Logger log = LoggerFactory.getLogger(AutoTrainControlDefault.class);
    }
