package jmri.jmrit.dispatcher;

import java.awt.BorderLayout;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import jmri.util.swing.JmriMouseAdapter;
import jmri.util.swing.JmriMouseEvent;
import jmri.util.swing.JmriMouseListener;
import java.beans.PropertyChangeEvent;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.implementation.SignalSpeedMap;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.roster.RosterIconFactory;

public class AutoEngineerMicro extends AbstractAutoTrainControl {

    private static int interp = jmri.InstanceManager.getDefault(SignalSpeedMap.class).getInterpretation();

    private static NamedIcon iconForward = null;
    private static NamedIcon iconReverse = null;
    private static NamedIcon iconEngineerAuto = null;
    private static NamedIcon iconEngineerManual = null;
    private static NamedIcon iconStopIcon = null;
    private static NamedIcon iconGoIcon = null;
    private static NamedIcon iconRestartIcon = null;
    private static NamedIcon iconDDCBackground = null;
    private static NamedIcon iconSpeedBackground = null;
    private static NamedIcon iconSpeedPCBackground = null;
    private static java.util.List<NamedIcon> iconSpeeds = null;

    static {
        switch (interp) {
            case SignalSpeedMap.SPEED_MPH:
                iconSpeedBackground =
                        new NamedIcon("resources/icons/AutoTrainsFrame/SpeedMPHBackground.png",
                                "resources/icons/AutoTrainsFrame/SpeedMPHBackground.png");
                break;
            case SignalSpeedMap.SPEED_KMPH:
                iconSpeedBackground =
                        new NamedIcon("resources/icons/AutoTrainsFrame/SpeedKPHBackground.png",
                                "resources/icons/AutoTrainsFrame/SpeedKPHBackground.png");
                break;
            default:
                iconSpeedBackground = null;
        }
        iconDDCBackground =
                new NamedIcon("resources/icons/AutoTrainsFrame/DCCBackground.png",
                        "resources/icons/AutoTrainsFrame/DCCBackground.png");
        iconSpeedPCBackground =
                new NamedIcon("resources/icons/AutoTrainsFrame/SpeedPCBackground.png",
                        "resources/icons/AutoTrainsFrame/SpeedPCBackground.png");
        iconForward =
                new NamedIcon("resources/icons/AutoTrainsFrame/Forward.png",
                        "resources/icons/AutoTrainsFrame/Forward.png");
        iconReverse =
                new NamedIcon("resources/icons/AutoTrainsFrame/Reverse.png",
                        "resources/icons/AutoTrainsFrame/Reverse.png");
        iconEngineerAuto =
                new NamedIcon("resources/icons/AutoTrainsFrame/EngineerAuto.png",
                        "resources/icons/AutoTrainsFrame/EngineerAuto.png");
        iconEngineerManual =
                new NamedIcon("resources/icons/AutoTrainsFrame/EngineerManual.png",
                        "resources/icons/AutoTrainsFrame/EngineerManual.png");
        iconStopIcon =
                new NamedIcon("resources/icons/AutoTrainsFrame/Stop.png", "resources/icons/AutoTrainsFrame/Stop.png");
        iconGoIcon =
                new NamedIcon("resources/icons/AutoTrainsFrame/Go.png", "resources/icons/AutoTrainsFrame/Go.png");
        iconRestartIcon =
                new NamedIcon("resources/icons/AutoTrainsFrame/Restart.png",
                        "resources/icons/AutoTrainsFrame/Restart.png");
        iconDDCBackground =
                new NamedIcon("resources/icons/AutoTrainsFrame/DCCBackground.png",
                        "resources/icons/AutoTrainsFrame/DCCBackground.png");
        iconSpeeds =
                Arrays.asList(
                        new NamedIcon("resources/icons/AutoTrainsFrame/ThrottleZero.png",
                                "resources/icons/AutoTrainsFrame/ThrottleZero.png"),
                        new NamedIcon("resources/icons/AutoTrainsFrame/ThrottleOne.png",
                                "resources/icons/AutoTrainsFrame/ThrottleOne.png"),
                        new NamedIcon("resources/icons/AutoTrainsFrame/ThrottleTwo.png",
                                "resources/icons/AutoTrainsFrame/ThrottleTwo.png"),
                        new NamedIcon("resources/icons/AutoTrainsFrame/ThrottleThree.png",
                                "resources/icons/AutoTrainsFrame/ThrottleThree.png"),
                        new NamedIcon("resources/icons/AutoTrainsFrame/ThrottleFour.png",
                                "resources/icons/AutoTrainsFrame/ThrottleFour.png"),
                        new NamedIcon("resources/icons/AutoTrainsFrame/ThrottleFive.png",
                                "resources/icons/AutoTrainsFrame/ThrottleFive.png"),
                        new NamedIcon("resources/icons/AutoTrainsFrame/ThrottleSix.png",
                                "resources/icons/AutoTrainsFrame/ThrottleSix.png"),
                        new NamedIcon("resources/icons/AutoTrainsFrame/ThrottleSeven.png",
                                "resources/icons/AutoTrainsFrame/ThrottleSeven.png"),
                        new NamedIcon("resources/icons/AutoTrainsFrame/ThrottleEight.png",
                                "resources/icons/AutoTrainsFrame/ThrottleEight.png"));
    }

    private int currentStep = 0;
    private float currentThrottleSetting = 0;
    private float currentThrottlePerHour = 0;

    public AutoEngineerMicro(AutoActiveTrain autoActiveTrain) {
        super(autoActiveTrain);
    }

    @Override
    protected void activeTrainNewModeDispatched() {
    }

    @Override
    protected void activeTrainNewModeAutomatic(){
        if (throttle != null) {
        btnNewBbtnStartStop.setIcon(iconGoIcon);
        }
    }
    @Override
    protected void activeTrainNewStatusStopped(){
        btnNewBbtnStartStop.setIcon(iconGoIcon);
    }
    @Override
    protected void activeTrainNewStatusRunning(){
        // do nothing
    }
    @Override
    protected void activeTrainNewStatusWaiting(){
        // do nothing
    }
    @Override
    protected void activeTrainNewStatusDone() {
        btnNewBbtnStartStop.setIcon(iconRestartIcon);
    }

    public void forwardReverseTrain() {
        if (activeTrain.getMode() == ActiveTrain.MANUAL) {
            throttle.setIsForward(!throttle.getIsForward());
            btnReverser.setIcon(throttle.getIsForward() ? iconForward : iconReverse);
        } else {
            log.debug("Ignoreing direction change click");
        }
    }

    public void throttleChange(int value) {
        if (activeTrain.getMode() == ActiveTrain.MANUAL) {
            if (currentStep + value > 8 || currentStep + value < 0) {
                return;
            }
            currentStep += value;
            currentThrottleSetting = currentStep * 1.0f / 8.0f;
            btnThrottle.setIcon(iconSpeeds.get(currentStep));
//            autoActiveTrain.getAutoEngineer().setSpeedImmediate(currentThrottleSetting);
            throttle.setSpeedSetting(currentThrottleSetting);
        } else {
            log.debug("Ignoreing speed change click");
        }
    }

    @Override
    protected void directionChange(PropertyChangeEvent e) {
        log.debug("{}:Property[{}]", activeTrain.getActiveTrainName(),e.getPropertyName());
        try {
        btnReverser.setIcon(throttle.getIsForward() ? iconForward : iconReverse);
        } catch (Exception ex) {
            log.error("[{}]:No throttle in throttle Listener", activeTrain.getActiveTrainName() );
        }

    }

    @Override
    protected void updateSpeedChange(PropertyChangeEvent e) {
        /* Convert throttle stop seven speed steps */
        if (throttle.getSpeedSetting() <= 0.0) {
            currentStep = 0;
            currentThrottleSetting = 0;
            currentThrottlePerHour = 0;
            btnThrottle.setIcon(iconSpeeds.get(0));
            updatePgEnd();
            return;
        }
        currentThrottleSetting = throttle.getSpeedSetting();
        currentStep = (int) Math.ceil(currentThrottleSetting * 8);
        // log.info("throt[{}]Step[{}]", currentThrottleSetting, currentStep);
        if (rosterEntry != null && rosterEntry.getSpeedProfile() != null) {
            currentThrottlePerHour = rosterEntry.getSpeedProfile()
                    .MMSToScaleSpeed(rosterEntry.getSpeedProfile().getSpeed(currentThrottleSetting, true));
        }
        updatePgEnd();
        btnThrottle.setIcon(iconSpeeds.get(currentStep));

    }

    private void updatePgEnd() {
        lblPageEndSpeed.setText(String.format("%3.0f", currentThrottlePerHour));
        lblPageEndThrottle.setText(String.format("%3.0f%%", currentThrottleSetting * 100));
    }

    private void stopResumeTrain() {
        if (activeTrain.getStatus() == ActiveTrain.STOPPED) {
            // resume
            autoActiveTrain.setEngineDirection();
            autoActiveTrain.getAutoEngineer().setHalt(false);
            autoActiveTrain.restoreSavedSpeedAndDirection();
            activeTrain.setStatus(autoActiveTrain.getSavedStatus());
            if ((activeTrain.getStatus() == ActiveTrain.RUNNING) ||
                    (activeTrain.getStatus() == ActiveTrain.WAITING)) {
                autoActiveTrain.setSpeedBySignal();
            }
            btnNewBbtnStartStop.setIcon(iconGoIcon);
        } else if (activeTrain.getStatus() == ActiveTrain.DONE) {
            // restart
            activeTrain.allocateAFresh();
            activeTrain.restart();
            btnNewBbtnStartStop.setIcon(iconGoIcon);
        } else {
            // stop
            autoActiveTrain.getAutoEngineer().setHalt(true);
            autoActiveTrain.saveSpeedAndDirection();
            autoActiveTrain.setSavedStatus(activeTrain.getStatus());
            activeTrain.setStatus(ActiveTrain.STOPPED);
            btnNewBbtnStartStop.setIcon(iconStopIcon);
        }
    }

    public void manualAutoTrain() {
        if (activeTrain.getMode() == ActiveTrain.AUTOMATIC) {
            activeTrain.setMode(ActiveTrain.MANUAL);
            btnManualAuto.setIcon(iconEngineerManual);
            if (autoActiveTrain.getAutoEngineer() != null) {
                autoActiveTrain.saveSpeedAndDirection();
                autoActiveTrain.getAutoEngineer().setHalt(true);
                autoActiveTrain.setTargetSpeed(0.0f);
                autoActiveTrain.waitUntilStopped();
                autoActiveTrain.getAutoEngineer().setHalt(false);
            }
        } else if (activeTrain.getMode() == ActiveTrain.MANUAL) {
            activeTrain.setMode(ActiveTrain.AUTOMATIC);
            btnManualAuto.setIcon(iconEngineerAuto);
            autoActiveTrain.saveSpeedAndDirection();
            //autoActiveTrain.setForward(!autoActiveTrain.getRunInReverse());
            if ((activeTrain.getStatus() == ActiveTrain.RUNNING) ||
                    (activeTrain.getStatus() == ActiveTrain.WAITING)) {
                autoActiveTrain.setSpeedBySignal();
            }
        }
    }

    private JLabel lblPageStart;
    private JLabel lblPageEndDCC;
    private JLabel lblPageEndThrottle;
    private JLabel lblPageEndSpeed;

    private jmri.jmrit.dispatcher.AutoEngineerJButton btnNewBbtnStartStop;
    private AutoEngineerJButton btnReverser ;
    private AutoEngineerJButton btnManualAuto;
    private AutoEngineerJButton btnThrottle ;

    //public JPanel componentBase;
    //public JToolBar componentJPanel;

    @Override
    protected void drawComponent() {

        currentStep = 0;   // to stop spotbug message.....

        componentJPanel = new JToolBar();

        componentJPanel.setLayout(new BorderLayout());
        componentJPanel.setBorder(BorderFactory.createLineBorder(Color.black));

        componentJPanel.setFloatable(true);

        ImageIcon iconRosterEntry = null;
        if (rosterEntry != null) {
            iconRosterEntry = jmri.InstanceManager.getDefault(RosterIconFactory.class).getIcon(rosterEntry);
        }
        if (iconRosterEntry != null) {
            lblPageStart = new JLabel(iconRosterEntry);
        } else {
            lblPageStart = new JLabel(activeTrain.getActiveTrainName(), SwingConstants.CENTER);
        }
        componentJPanel.add(lblPageStart, BorderLayout.PAGE_START);

        JPanel pnlPageEnd = new JPanel(new GridLayout(1, 3));
        GridBagConstraints constraintPageEnd = new GridBagConstraints();
        constraintPageEnd.weightx = 1.0;
        constraintPageEnd.weighty = 1.0;

        constraintPageEnd.gridx = 0;
        constraintPageEnd.gridy = 0;
        lblPageEndDCC = new JLabel() {
            @Override
            public void paintComponent(Graphics g) {
                g.drawImage(iconDDCBackground.getOriginalImage(), 0, 0, null);
                super.paintComponent(g);
            }
        };
        lblPageEndDCC.setHorizontalTextPosition(JLabel.CENTER);
        lblPageEndDCC.setVerticalTextPosition(JLabel.CENTER);
        lblPageEndDCC.setBorder(BorderFactory.createEtchedBorder());
        lblPageEndDCC.setText(activeTrain.getDccAddress());
        pnlPageEnd.add(lblPageEndDCC, constraintPageEnd);
        lblPageEndThrottle = new JLabel() {
            @Override
            public void paintComponent(Graphics g) {
                g.drawImage(iconSpeedPCBackground.getOriginalImage(), 0, 0, null);
                super.paintComponent(g);
            }
        };
        constraintPageEnd.gridx = 1;
        constraintPageEnd.gridy = 0;
        lblPageEndThrottle.setText(Integer.toString(currentStep));
        lblPageEndThrottle.setBorder(BorderFactory.createEtchedBorder());
        lblPageEndThrottle.setOpaque(false);
        pnlPageEnd.add(lblPageEndThrottle, constraintPageEnd);

        constraintPageEnd.gridx = 2;
        constraintPageEnd.gridy = 0;
        lblPageEndSpeed = new JLabel() {
            @Override
            public void paintComponent(Graphics g) {
                g.drawImage(iconSpeedBackground.getOriginalImage(), 0, 0, null);
                super.paintComponent(g);
            }
        };
        lblPageEndSpeed.setBorder(BorderFactory.createEtchedBorder());
        lblPageEndSpeed.setOpaque(false);
        pnlPageEnd.add(lblPageEndSpeed, constraintPageEnd);

        componentJPanel.add(pnlPageEnd, BorderLayout.PAGE_END);

        JPanel activities = new JPanel(new GridLayout(1, 5));
        //JToolBar activities = new JToolBar();
        Dimension buttonSize = new Dimension(64,64);
        // natural height, maximum width
        btnNewBbtnStartStop =
                new AutoEngineerJButton(iconStopIcon);
        btnNewBbtnStartStop.reSizeIcon(buttonSize);
        btnNewBbtnStartStop.setBorder(BorderFactory.createEtchedBorder());
        activities.add(btnNewBbtnStartStop);
        btnNewBbtnStartStop.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stopResumeTrain();
            }
        });

        btnReverser = new AutoEngineerJButton(iconForward);
        btnReverser.reSizeIcon(buttonSize);
        btnReverser.setHorizontalAlignment(SwingConstants.RIGHT);
        btnReverser.setBorder(BorderFactory.createEtchedBorder());
        activities.add(btnReverser);
        btnReverser.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                forwardReverseTrain();
            }
        });

        NamedIcon ThrottleZeroIcon =
                new NamedIcon("resources/icons/AutoTrainsFrame/ThrottleZero.png",
                        "resources/icons/AutoTrainsFrame/ThrottleZero.png");
        btnThrottle = new AutoEngineerJButton(ThrottleZeroIcon);
        btnThrottle.reSizeIcon(buttonSize);
        btnThrottle.setBorder(BorderFactory.createEtchedBorder());
        activities.add(btnThrottle);
        btnThrottle.addMouseListener(JmriMouseListener.adapt(new JmriMouseListener() {
            @Override
            public void mouseClicked(JmriMouseEvent e) {
                if (e.getY() > btnThrottle.getSize().getHeight() / 2) {
                    throttleChange(-1);
                } else {
                    throttleChange(1);
                }
                log.info("Clicked");
            }
            @Override
            public void mouseEntered(JmriMouseEvent arg0) {
            }
            @Override
            public void mousePressed(JmriMouseEvent e) {
            }
            @Override
            public void mouseReleased(JmriMouseEvent e) {
            }
            @Override
            public void mouseExited(JmriMouseEvent e) {
            }
        }));

        btnManualAuto =
                new AutoEngineerJButton(iconEngineerAuto);
        btnManualAuto.reSizeIcon(buttonSize);
        btnManualAuto.setBorder(BorderFactory.createEtchedBorder());
        activities.add(btnManualAuto);
        btnManualAuto.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                manualAutoTrain();
            }
        });
        componentJPanel.add(activities, BorderLayout.CENTER);
        componentJPanel.revalidate();
        add(componentJPanel, BorderLayout.EAST);

        //componentJPanel.setPreferredSize(componentJPanel.getMinimumSize());
        //log.info("dim[{}][{}]",componentJPanel.getHeight(),componentJPanel.getWidth());
        //componentJPanel.revalidate();
        //componentBase.add(componentJPanel, BorderLayout.CENTER);
        //BasicToolBarUI ui = new BasicToolBarUI();
        //componentJPanel.setUI(ui);
        //add(componentBase);
    }


    private final static Logger log = LoggerFactory.getLogger(AutoEngineerMicro.class);

}
