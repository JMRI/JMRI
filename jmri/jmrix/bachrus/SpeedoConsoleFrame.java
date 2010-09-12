// SpeedoConsoleFrame.java

package jmri.jmrix.bachrus;

import java.util.*;
import java.text.*;
import java.awt.*;
import java.text.MessageFormat;
import javax.swing.*;
import javax.swing.JComboBox;

import jmri.CommandStation;
import jmri.JmriException;
import jmri.PowerManager;
import jmri.DccThrottle;
import jmri.ThrottleListener;
import jmri.util.JmriJFrame;
import jmri.Programmer;
import jmri.ProgrammerException;
import jmri.ProgListener;


/**
 * Frame for Speedo Console for Bachrus running stand reader interface
 * 
 * @author			Andrew Crosland   Copyright (C) 2010
 * @version			$Revision: 1.15 $
 */
public class SpeedoConsoleFrame extends JmriJFrame implements SpeedoListener,
                                                        ThrottleListener, 
                                                        ProgListener {

    /***
     * TODO:
     *  Complete the help file
     *  Allow selection of arbitrary scale
     */


    // member declarations
    protected JLabel scaleLabel = new JLabel();
    protected JTextField speedTextField = new JTextField(12);

    protected ButtonGroup speedGroup = new ButtonGroup();
    protected JRadioButton mphButton = new JRadioButton("Miles per Hour");
    protected JRadioButton kphButton = new JRadioButton("Kilometers per Hour");
    protected ButtonGroup displayGroup = new ButtonGroup();
    protected JRadioButton numButton = new JRadioButton("Numeric");
    protected JRadioButton dialButton = new JRadioButton("Dial");
    protected SpeedoDial speedoDialDisplay = new SpeedoDial();

    GraphPane profileGraphPane;
    protected JLabel profileAddressLabel = new JLabel("Loco Address:");
    protected JTextField profileAddressField = new JTextField(6);
    protected JButton readAddressButton = new JButton("Read");
    protected JButton startProfileButton = new JButton("Start");
    protected JButton stopProfileButton = new JButton("Stop");
    protected JButton exportProfileButton = new JButton("Export");
    protected JButton printProfileButton = new JButton("Print");

    protected javax.swing.JLabel readerLabel = new javax.swing.JLabel();

    protected String[] scaleStrings = new String [] {
        "Z - 1.39mm - 1:220",
        "Euro N - 1.9mm - 1:160",
        "N Fine - 2mm - 1:152",
        "Japanese N - 2.03mm - 1:150",
        "British N - 2.0625mm - 1:148",
        "3mm - 1:120",
        "Triang TT - 3mm - 1:101.6",
        "00/EM/S4 - 4mm - 1:76",
        "HO - 3.5mm - 1:87",
        "S - 3/16inch - 1:64",
        "O - 1/4inch - 1:48",
        "O - 6.77mm - 1:45",
        "O - 7mm - 1:43"/*,
        "Other..."*/
        };

    protected float[] scales = new float[] {
        220,
        160,
        152,
        150,
        148,
        120,
        101.6F,
        76,
        87,
        64,
        48,
        45,
        43/*,
        -1*/
        };

    protected static final int defaultScale = 8;

    protected float selectedScale = 0;
    protected int series = 0;
    protected float sampleSpeed = 0;
    protected float targetSpeed = 0;
    protected float currentSpeed = 0;
    protected float incSpeed = 0;
    protected float oldSpeed = 0;
    protected float acc = 0;
    protected float avSpeed = 0;
    protected int range = 1;
    protected float circ = 0;
    protected float count = 1;
    protected float freq;
    protected static final int DISPLAY_UPDATE = 250;
    protected static final int FAST_DISPLAY_RATIO = 3;

    /*
     * At low speed, readings arrive less often and less filtering
     * is applied to minimise the delay in updating the display
     * 
     * Speed measurement is split into 3 ranges with an overlap, tp
     * prevent "hunting" between the ranges.
     */
    protected static final int RANGE1LO = 0;
    protected static final int RANGE1HI = 10;
    protected static final int RANGE2LO = 8;
    protected static final int RANGE2HI = 30;
    protected static final int RANGE3LO = 26;
    protected static final int RANGE3HI = 9999;
    protected static final int[] filterLength = {0, 3, 5, 8};
    protected enum displayType {NUMERIC, DIAL}
    protected displayType display = displayType.NUMERIC;

    /*
     * Keep track of the DCC services available
     */
    protected int dccServices;
    protected static final int BASIC = 0;
    protected static final int PROG = 1;
    protected static final int COMMAND = 2;
    protected static final int THROTTLE = 4;

    protected boolean timerRunning = false;

    protected dccSpeedProfile spFwd;
    protected dccSpeedProfile spRev;
    protected enum profileState {IDLE, WAIT_FOR_THROTTLE, RUNNING}
    protected profileState state = profileState.IDLE;
    protected enum profileDirection {FORWARD, REVERSE}
    protected profileDirection profileDir = profileDirection.FORWARD;
    protected DccThrottle throttle = null;
    protected int profileStep = 0;
    protected float profileSpeed;
    protected float profileIncrement;
    protected int profileAddress = 0;
    protected Programmer prog = null;
    protected CommandStation commandStation = null;
    protected enum progState {IDLE, WAIT29, WAIT3, WAIT17, WAIT18}
    protected progState readState = progState.IDLE;

    //Create the combo box, select item at index 4.
    //Indices start at 0, so 4 specifies british N.
    JComboBox scaleList = new JComboBox(scaleStrings);

    // members for handling the Speedo interface
    SpeedoTrafficController tc = null;
    String replyString;

    public SpeedoConsoleFrame() {
        super();
    }
    
    protected String title() { return "Speedo Console"; }
    
    public void dispose() {
        SpeedoTrafficController.instance().removeSpeedoListener(this);
        super.dispose();
    }
    
    public void initComponents() throws Exception {
        setTitle(title());
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        // What services do we have?
        dccServices = BASIC;
        if (jmri.InstanceManager.programmerManagerInstance()!=null &&
            jmri.InstanceManager.programmerManagerInstance().isGlobalProgrammerAvailable()) {
            prog = jmri.InstanceManager.programmerManagerInstance().getGlobalProgrammer();
        	dccServices |= PROG;
        }
        if (false /*jmri.InstanceManager.commandStationInstance() != null*/) {
            // We'll use the command station to send explicit speed steps
            commandStation = jmri.InstanceManager.commandStationInstance();
            log.info("Using CommandStation interface for profiling");
            dccServices |= COMMAND;
        } else if (jmri.InstanceManager.throttleManagerInstance()!=null) {
            // otherwise we'll send speed commands
            log.info("Using Throttle interface for profiling");
        	dccServices |= THROTTLE;
        }

        /*
         * Setup pane for basic operations
         */
        JPanel basicPane = new JPanel(); 
        basicPane.setLayout(new BoxLayout(basicPane, BoxLayout.Y_AXIS));

        // Scale panel
        JPanel scalePanel = new JPanel();
        scalePanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Select Scale"));
        scalePanel.setLayout(new FlowLayout());
        
        scaleList.setToolTipText("Select the scale");
        scaleList.setSelectedIndex(defaultScale);
        selectedScale = scales[defaultScale];

        // Listen to selection of scale
        scaleList.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                JComboBox cb = (JComboBox)e.getSource();
                selectedScale = scales[cb.getSelectedIndex()];
                // *** check if -1 and enable text entry box
            }
        });

        scaleLabel.setText("Scale:");
        scaleLabel.setVisible(true);
        
        readerLabel.setText("Unknown Reader");
        readerLabel.setVisible(true);

        scalePanel.add(scaleLabel);
        scalePanel.add(scaleList);
        scalePanel.add(readerLabel);
        
        basicPane.add(scalePanel);
 
        JPanel speedPanel = new JPanel();
        speedPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Measured Speed"));
        speedPanel.setLayout(new BoxLayout(speedPanel, BoxLayout.X_AXIS));

        // Display Panel which is a card layout with cards to show
        // numeric or dial type speed display
        final JPanel displayCards = new JPanel();
        displayCards.setLayout(new CardLayout());
 
        // Numeric speed card
        JPanel numericSpeedPanel = new JPanel();
        numericSpeedPanel.setLayout(new BoxLayout(numericSpeedPanel, BoxLayout.X_AXIS));
        Font f = new Font("", Font.PLAIN, 96);
        speedTextField.setFont(f);
        speedTextField.setHorizontalAlignment(JTextField.RIGHT);
        speedTextField.setColumns(3);
        speedTextField.setText("0.0");
        speedTextField.setVisible(true);
        speedTextField.setToolTipText("Speed will be displayed here");
        numericSpeedPanel.add(speedTextField);

        // Dial speed card
        JPanel dialSpeedPanel = new JPanel();
        dialSpeedPanel.setLayout(new BoxLayout(dialSpeedPanel, BoxLayout.X_AXIS));
        dialSpeedPanel.add(speedoDialDisplay);
        speedoDialDisplay.update(0.0F);

        // Add cards to panel
        displayCards.add(dialSpeedPanel, "DIAL");
        displayCards.add(numericSpeedPanel, "NUMERIC");
        CardLayout cl = (CardLayout)displayCards.getLayout();
        cl.show(displayCards, "DIAL");
        
        // button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        speedGroup.add(mphButton);
        speedGroup.add(kphButton);
        mphButton.setSelected(true);
        mphButton.setToolTipText("Display Speed in Miles per Hour");
        kphButton.setToolTipText("Display Speed in Kilometers per Hour");
        displayGroup.add(numButton);
        displayGroup.add(dialButton);
        dialButton.setSelected(true);
        numButton.setToolTipText("Display Speed in numeric format");
        dialButton.setToolTipText("Display Speed in dial format");
        buttonPanel.add(mphButton);
        buttonPanel.add(kphButton);
        buttonPanel.add(numButton);
        buttonPanel.add(dialButton);
      
        speedPanel.add(displayCards);
        speedPanel.add(buttonPanel);
        
        // Listen to change of units, convert current average and update display
        mphButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                profileGraphPane.setUnitsMph();
                profileGraphPane.repaint();
                speedoDialDisplay.setUnitsMph();
                speedoDialDisplay.repaint();
            }
        });
        kphButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                profileGraphPane.setUnitsKph();
                profileGraphPane.repaint();
                speedoDialDisplay.setUnitsKph();
                speedoDialDisplay.repaint();
            }
        });

        // Listen to change of display
        numButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                display = displayType.NUMERIC;
                CardLayout cl = (CardLayout)displayCards.getLayout();
                cl.show(displayCards, "NUMERIC");
            }
        });
        dialButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                display = displayType.DIAL;
                CardLayout cl = (CardLayout)displayCards.getLayout();
                cl.show(displayCards, "DIAL");
            }
        });

        basicPane.add(speedPanel);

        /*
         * Pane for profiling loco speed curve
         */
        JPanel profilePane = new JPanel();
        profilePane.setLayout(new BoxLayout(profilePane, BoxLayout.Y_AXIS));

        // pane to hold address
        JPanel profileAddressPane = new JPanel();
        profileAddressPane.setLayout(new FlowLayout());
        profileAddressPane.add(profileAddressLabel);
        profileAddressPane.add(profileAddressField);
        profileAddressField.setToolTipText("Enter Loco Address");
        profileAddressPane.add(readAddressButton);
        readAddressButton.setToolTipText("Read Loco Address - requires DCC programmer");
        
        if ((dccServices & PROG) != PROG) {
            // User must enter address
            readAddressButton.setEnabled(false);
            profileAddressField.setText(Integer.toString(0));
        }
        
        // Listen to text entry
        profileAddressField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                try {
                    profileAddress = Integer.parseInt(profileAddressField.getText());
                } catch (NumberFormatException ex) {
                    log.error("Non numeric address entered " + ex);
                    profileAddress = 0;
                    profileAddressField.setText("0");
                }
            }
        });

        // Listen to read button
        readAddressButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                readAddress();
            }
        });

        profilePane.add(profileAddressPane);
        
        // pane to hold the graph
        spFwd = new dccSpeedProfile(29);       // 28 step plus step 0
        spRev = new dccSpeedProfile(29);       // 28 step plus step 0
        profileGraphPane = new GraphPane(spFwd, spRev);
        profileGraphPane.setPreferredSize(new Dimension(600, 300));
        profileGraphPane.setXLabel("Speed Step");
        profileGraphPane.setUnitsMph();

        profilePane.add(profileGraphPane);
        
        // pane to hold the buttons
        JPanel profileButtonPane = new JPanel();
        profileButtonPane.setLayout(new FlowLayout());
        profileButtonPane.add(startProfileButton);
        profileButtonPane.add(stopProfileButton);
        profileButtonPane.add(exportProfileButton);
        profileButtonPane.add(printProfileButton);
        
        // Listen to start button
        startProfileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                startProfile();
            }
        });

        // Listen to stop button
        stopProfileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                stopProfile();
            }
        });

        // Listen to export button
        exportProfileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                spFwd.export();
            }
        });

        // Listen to print button
        printProfileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                Date today;
                String result;
                SimpleDateFormat formatter;
                formatter = new SimpleDateFormat("EEE d MMM yyyy", Locale.getDefault());
                today = new Date();
                result = formatter.format(today);
                String annotate = "Bachrus MTS-DCC profile for loco address "
                                    + profileAddress + " created on " + result;
                profileGraphPane.printProfile(annotate);
            }
        });

        profilePane.add(profileButtonPane);

        /*
         * Crreate the tabbed pane and add the panes
         */
        JTabbedPane tabbedPane = new JTabbedPane();
        // make basic panel
        tabbedPane.addTab("Setup", null, basicPane, "Basic Speedo Operation");

        if (((dccServices & THROTTLE) == THROTTLE)
            || ((dccServices & COMMAND) == THROTTLE)) {
            tabbedPane.addTab("Profile", null, profilePane, "Profile Loco");
        }

        // connect to TrafficController
        tc = SpeedoTrafficController.instance();
        tc.addSpeedoListener(this);

        // add help menu to window
    	addHelpMenu("package.jmri.jmrix.bachrus.SpeedoConsoleFrame", true);

        getContentPane().add(tabbedPane);
        
        // pack for display
        pack();
    }
    
    /**
     * Handle "replies" from the hardware. In fact, all the hardware does
     * is send a constant stream of unsolicited speed updates.
     * @param l
     */
    public synchronized void reply(SpeedoReply l) {  // receive a reply message and log it
        //log.debug("Speedo reply " + l.toString());
        count = l.getCount();
        series = l.getSeries();
        if (count > 0) {
            switch (series) {
                case 4:
                    circ = 12.5664F;
                    readerLabel.setText("40 Series Reader");
                    break;
                case 5:
                    circ = 18.8496F;
                    readerLabel.setText("50 Series Reader");
                    break;
                case 6:
                    circ = 50.2655F;
                    readerLabel.setText("60 Series Reader");
                    break;
                default:
                    speedTextField.setText("Error!");
                    log.error("Invalid reader type");
                    break;
            }

            // Update speed
            calcSpeed();
        }
        if (timerRunning == false) {
            // first reply starts the timer
            startReplyTimer();
            startDisplayTimer();
            startFastDisplayTimer();
            timerRunning = true;
        } else {
            // subsequnet replies restart it
            replyTimer.restart();
        }
    }

    /*
     * Calculate the scale speed in KPH
     */
    protected void calcSpeed() {
        if (series > 0) {
            // Scale the data and calculate kph
            try {
                freq = 1500000/count;
                sampleSpeed = (freq/24)*circ*selectedScale*3600/1000000;
            } catch (ArithmeticException ae) {
                log.error("Exception calculating sampleSpeed " + ae);
            }
            avFn(sampleSpeed);
            log.debug("New sample: "+sampleSpeed+" Average: "+avSpeed);
            log.debug("Acc: "+acc+" range: "+range);
            switchRange();
        }
    }

    // Averaging function used for speed is
    // S(t) = S(t-1) - [S(t-1)/N] + speed
    // A(t) = S(t)/N
    //
    // where S is an accumulator, N is the length of the filter (i.e.,
    // the number of samples included in the rolling average), and A is
    // the result of the averaging function.
    //
    // Re-arranged
    // S(t) = S(t-1) - A(t-1) + speed
    // A(t) = S(t)/N
    protected void avFn(float speed) {
        acc = acc - avSpeed + speed;
        avSpeed = acc/filterLength[range];
    }

    // Clear out the filter
    protected void avClr() {
        acc = 0;
        avSpeed = 0;
    }
    
    // When we switch range we must compensate the current accumulator
    // value for the longer filter.
    protected void switchRange() {
        switch (range) {
            case 1:
                if (sampleSpeed > RANGE1HI) {
                    range++;
                    acc = acc*filterLength[2]/filterLength[1];
                }
                break;
            case 2:
                if (sampleSpeed < RANGE2LO){
                    range--;
                    acc = acc*filterLength[1]/filterLength[2];
                }
                else if (sampleSpeed > RANGE2HI) {
                    range++;
                    acc = acc*filterLength[3]/filterLength[2];
                }
                break;
            case 3:
                if (sampleSpeed < RANGE3LO) {
                    range--;
                    acc = acc*filterLength[2]/filterLength[3];
                }
                break;
        }
    }

    /*
     * Display the speed
     */
    protected void showSpeed() {
        float speedForText = currentSpeed;
        if (mphButton.isSelected()) {
            speedForText = Speed.kphToMph(speedForText);
        }
        if (series > 0) {
            if ((currentSpeed < 0) || (currentSpeed > 999)) {
                log.error("Calculated speed out of range: " + currentSpeed);
                speedTextField.setText("999");
            } else {
                // Final smoothing as applied by Bachrus Console. Don't update display
                // unless speed has changed more than 2%
                if ((currentSpeed > oldSpeed*1.02) || (currentSpeed < oldSpeed*0.98)) {
                    speedTextField.setText(MessageFormat.format("{0,number,##0.0}", speedForText));
                    speedTextField.setHorizontalAlignment(JTextField.RIGHT);
                    oldSpeed = currentSpeed;
                }
                speedoDialDisplay.update(currentSpeed);
            }
        }
    }

    protected void startProfile() {
        if (profileAddress > 0) {
            if (state == profileState.IDLE) {
                profileTimer = new javax.swing.Timer(4000, new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        profileTimeout();
                    }
                });
                profileTimer.setRepeats(false);
                if ((dccServices & COMMAND) == COMMAND) {
                    // turn on power
                    try {
                        jmri.InstanceManager.powerManagerInstance().setPower(PowerManager.ON);
                    } catch (JmriException e) {
                        log.error("Exception during power on: "+e.toString());
                    }
                    // Start at step 0 with 28 step packets
                    profileStep = 0;
                    // send speed step
                    // using profile timer to trigger each next step
                    profileTimer.setRepeats(true);
                    state = profileState.RUNNING;
                } else {
                    // Need to request a throttle
                    state = profileState.WAIT_FOR_THROTTLE;
                    // Request a throttle
                    log.info("Requesting throttle");
                    jmri.InstanceManager.throttleManagerInstance().requestThrottle(profileAddress, this);
                }
                spFwd.clear();
                spRev.clear();
                profileDir = profileDirection.FORWARD;
                profileGraphPane.repaint();
                profileTimer.start();
            }
        } else {
            // Must have a non-zero address
            profileAddressField.setBackground(Color.RED);
            log.error("Attempt to profile loco address 0");
        }
    }

    protected void stopProfile() {
        if (state != profileState.IDLE) {
            tidyUp();
            state = profileState.IDLE;
            log.info("Profiling stopped by user");
        }
    }
    
    public void notifyThrottleFound(DccThrottle t) {
        profileTimer.stop();
        throttle = t;
        log.info("Throttle aquired, starting profiling");
        throttle.setSpeedStepMode(DccThrottle.SpeedStepMode28);
        if (throttle.getSpeedStepMode() != DccThrottle.SpeedStepMode28) {
            log.error("Failed to set 28 step mode");
            throttle.release();
            return;
        }
        // turn on power
        try {
            jmri.InstanceManager.powerManagerInstance().setPower(PowerManager.ON);
        } catch (JmriException e) {
            log.error("Exception during power on: "+e.toString());
        }
        state = profileState.RUNNING;
        // Start at step 0 with 28 step packets
        profileSpeed = 0.0F;
        profileStep = 0;
        profileIncrement = throttle.getSpeedIncrement();
        throttle.setSpeedSetting(profileSpeed);
        throttle.setIsForward(true);
        // using profile timer to trigger each next step
        profileTimer.setRepeats(true);
        profileTimer.start();
    }

    javax.swing.Timer replyTimer = null;
    javax.swing.Timer displayTimer = null;
    javax.swing.Timer fastDisplayTimer = null;
    javax.swing.Timer profileTimer = null;

	// Once we receive a speedoReply we expect them regularly, at
    // least once every 4 seconds
    protected void startReplyTimer() {
        replyTimer = new javax.swing.Timer(4000, new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                replyTimeout();
            }
        });
        replyTimer.setRepeats(true);     // refresh until stopped by dispose
        replyTimer.start();
    }

    /**
     * Internal routine to reset the speed on a timeout
     */
    synchronized protected void replyTimeout() {
        //log.debug("Timed out - display speed zero");
        targetSpeed = 0;
        avClr();
        oldSpeed = 0;
        showSpeed();
    }

	// A timer is used to update the target display speed
    protected void startDisplayTimer() {
        displayTimer = new javax.swing.Timer(DISPLAY_UPDATE, new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                displayTimeout();
            }
        });
        displayTimer.setRepeats(true);     // refresh until stopped by dispose
        displayTimer.start();
    }

	// A timer is used to update the display at faster rate
    protected void startFastDisplayTimer() {
        fastDisplayTimer = new javax.swing.Timer(DISPLAY_UPDATE/FAST_DISPLAY_RATIO, new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                fastDisplayTimeout();
            }
        });
        fastDisplayTimer.setRepeats(true);     // refresh until stopped by dispose
        fastDisplayTimer.start();
    }

    /**
     * Internal routine to update the target speed for display
     */
    synchronized protected void displayTimeout() {
        //log.info("Display timeout");
        targetSpeed = avSpeed;
        incSpeed = (targetSpeed - currentSpeed)/FAST_DISPLAY_RATIO;
    }

    /**
     * Internal routine to update the displayed speed
     */
    synchronized protected void fastDisplayTimeout() {
        //log.info("Display timeout");
        if (Math.abs(targetSpeed - currentSpeed) < Math.abs(incSpeed)) {
            currentSpeed = targetSpeed;
        } else {
            currentSpeed += incSpeed;
        }
        if (currentSpeed < 0.01F) {
            currentSpeed = 0.0F;
        }
        showSpeed();
    }

    /**
     * timeout requesting a throttle
     */
    synchronized protected void throttleTimeout() {
        jmri.InstanceManager.throttleManagerInstance().cancelThrottleRequest(profileAddress, this);
        state = profileState.IDLE;
        log.error("Timeout waiting for throttle");

    }

    /**
     * Time to change to next speed increment
     */
    synchronized protected void profileTimeout() {
        if (state == profileState.WAIT_FOR_THROTTLE) {
            tidyUp();
            log.error("Timeout waiting for throttle");
        } else if (state == profileState.RUNNING) {
//            log.info("Step: " + profileStep + " Speed: " + speed_ave);
            if (profileDir == profileDirection.FORWARD) {
                spFwd.setPoint(profileStep, avSpeed);
            } else {
                spRev.setPoint(profileStep, avSpeed);
            }
            profileGraphPane.repaint();
            if (profileStep == 29) {
                if (profileDir == profileDirection.FORWARD) {
                    profileDir = profileDirection.REVERSE;
                    throttle.setIsForward(false);
                    profileStep = 0;
                    avClr();
                    log.info("Forward profile complete");
                } else {
                    tidyUp();
                    log.info("Reverse profile complete");
                }
            } else {
                if (profileStep == 28) {
                    profileSpeed = 0.0F;
                } else {
                    profileSpeed += profileIncrement;
                }
                throttle.setSpeedSetting(profileSpeed);
                profileStep += 1;
                // adjust delay as we get faster and averaging is quicker
                profileTimer.setDelay(5000 - range*1000);
                //log.info("Step " + profileStep + " Set speed: "+profileSpeed);
            }
        } else {
            log.error("Unexpected profile timeout");
            profileTimer.stop();
        }
    }

    protected void tidyUp() {
        profileTimer.stop();
        // turn off power
        try {
            jmri.InstanceManager.powerManagerInstance().setPower(PowerManager.OFF);
        } catch (JmriException e) {
            log.error("Exception during power off: "+e.toString());
        }
        if (throttle != null) {
            throttle.setSpeedSetting(0.0F);
            //jmri.InstanceManager.throttleManagerInstance().cancelThrottleRequest(profileAddress, this);
            throttle.release();
            throttle = null;
        }
        state = profileState.IDLE;
    }
    
    protected void readAddress() {
        readState = progState.WAIT29;
        startRead(29);
    }

    protected void startRead(int cv) {
        try {
            prog.readCV(cv, this);
        } catch (ProgrammerException e) {
            log.error("Exception reading CV " + cv + " " + e);
        }
    }

    public void programmingOpReply(int value, int status) {
        if (status == 0) {
            switch(readState) {
                case IDLE:
                    log.error("unexpected reply in IDLE state");
                    break;

                case WAIT29:
                    // Check extended address bit
                    if ((value & 0x20) == 0) {
                        readState = progState.WAIT3;
                        startRead(3);
                    } else {
                        readState = progState.WAIT17;
                        startRead(17);
                    }
                    break;

                case WAIT3:
                    profileAddress = value;
                    profileAddressField.setBackground(Color.WHITE);
                    readState = progState.IDLE;
                    break;

                case WAIT17:
                    profileAddress = value;
                    readState = progState.WAIT18;
                    startRead(18);
                    break;

                case WAIT18:
                    profileAddress = ((profileAddress<<8) + value) & 0x03FF;
                    profileAddressField.setText(Integer.toString(profileAddress));
                    profileAddressField.setBackground(Color.WHITE);
                    readState = progState.IDLE;
                    break;

            }
        } else {
            // Error during programming
            log.error("Status not OK during read: " + status);
            profileAddressField.setText("Error");
            readState = progState.IDLE;
        }
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SpeedoConsoleFrame.class.getName());
    
}
