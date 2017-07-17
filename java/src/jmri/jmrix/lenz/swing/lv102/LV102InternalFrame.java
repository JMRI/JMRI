package jmri.jmrix.lenz.swing.lv102;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import jmri.AddressedProgrammer;
import jmri.ProgListener;
import jmri.Programmer;
import jmri.ProgrammerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Internal Frame displaying the LV102 configuration utility
 *
 * This is a configuration utility for the LV102. It allows the user to set the
 * Track Voltage and E-line status.
 *
 * <p>
 * Note that ctor starts a listener thread; if you subclass this class, be sure
 * that initialization is in the right order.
 *
 * @author Paul Bender Copyright (C) 2005
  */
public class LV102InternalFrame extends javax.swing.JInternalFrame {

    private progReplyListener progListener = null;
    private Thread progListenerThread = null;

    final static int waitValue = 1000; // number of ms to wait after a 
    // programming operation.  This 
    // should not be more than 15.

    @SuppressFBWarnings(value = "SC_START_IN_CTOR",
            justification = "with existing code structure, we do not expect this to ever be subclassed.")

    public LV102InternalFrame() {

        // Set up the programmer listener
        progListener = new progReplyListener(this);
        progListenerThread = new Thread(progListener);
        progListenerThread.start();

        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        setTitle(Bundle.getMessage("LV102Power"));

        JPanel pane0 = new JPanel();
        pane0.setLayout(new FlowLayout());
        JLabel voltLabel = new JLabel(Bundle.getMessage("LV102Track"));
        pane0.add(voltLabel);
        voltLabel.setLabelFor(voltBox);
        pane0.add(voltBox);
        pane0.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
        getContentPane().add(pane0);

        JPanel pane1 = new JPanel();
        JLabel eLineLabel = new JLabel(Bundle.getMessage("LV102ELine"));
        pane1.add(eLineLabel);
        eLineLabel.setLabelFor(eLineBox);
        pane1.add(eLineBox);
        pane1.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
        getContentPane().add(pane1);

        JPanel pane2 = new JPanel();
        pane2.add(new JLabel(Bundle.getMessage("LV102RailCom")));
        pane2.add(railComBox);
        pane2.add(new JLabel(Bundle.getMessage("LV102RailComMode")));
        pane2.add(railComModeBox);
        pane2.add(new JLabel(Bundle.getMessage("LV102RailComTiming")));
        pane2.add(railComTimingBox);
        pane2.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
        getContentPane().add(pane2);

        JPanel pane3 = new JPanel();

        // Set the write button label and tool tip
        writeSettingsButton.setText(Bundle.getMessage("LV102WriteSettingsButtonLabel"));
        writeSettingsButton.setToolTipText(Bundle.getMessage("LV102WriteSettingsButtonToolTip"));

        pane3.add(writeSettingsButton);

        // Set the reset to Defaults button label and tool tip
        defaultButton.setText(Bundle.getMessage("LV102DefaultButtonLabel"));
        defaultButton.setToolTipText(Bundle.getMessage("LV102DefaultButtonToolTip"));

        // Set the reset button label and tool tip
        resetButton.setText(Bundle.getMessage("LV102ResetButtonLabel"));
        resetButton.setToolTipText(Bundle.getMessage("LV102ResetButtonToolTip"));

        pane3.add(defaultButton);
        pane3.add(resetButton);
        getContentPane().add(pane3);

        // Initilize the Combo Boxes

        /* configure the voltage selection box */
        voltBox.setVisible(true);
        voltBox.setToolTipText(Bundle.getMessage("LV102TrackTip"));
        for (int i = 0; i < validVoltage.length; i++) {
            voltBox.addItem(validVoltage[i]);
        }
        voltBox.setSelectedIndex(23);

        /* Configure the E-Line Active/Inactive box */
        eLineBox.setVisible(true);
        eLineBox.setToolTipText(Bundle.getMessage("LV102ELineTip"));
        for (int i = 0; i < validELineStatus.length; i++) {
            eLineBox.addItem(validELineStatus[i]);
        }
        eLineBox.setSelectedIndex(3);

        /* Configure the RailCom Active/Inactive box */
        railComBox.setVisible(true);
        railComBox.setToolTipText(Bundle.getMessage("LV102RailComTip"));
        for (int i = 0; i < validRailComStatus.length; i++) {
            railComBox.addItem(validRailComStatus[i]);
        }
        railComBox.setSelectedIndex(2);

        /* Configure the RailCom Mode selection box */
        railComModeBox.setVisible(true);
        railComModeBox.setToolTipText(Bundle.getMessage("LV102RailComModeTip"));
        for (int i = 0; i < validRailComMode.length; i++) {
            railComModeBox.addItem(validRailComMode[i]);
        }
        railComModeBox.setSelectedIndex(2);

        /* Configure the RailCom Timing selection box */
        railComTimingBox.setVisible(true);
        railComTimingBox.setToolTipText(Bundle.getMessage("LV102RailComTimingTip"));
        for (int i = 0; i < validRailComTiming.length; i++) {
            railComTimingBox.addItem(validRailComTiming[i]);
        }
        railComTimingBox.setSelectedIndex(4);

        synchronized (CurrentStatus) {
            CurrentStatus.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
            CurrentStatus.setVisible(true);
            CurrentStatus.setText(" ");
            if (log.isDebugEnabled()) {
                log.debug("Current Status: ");
            }
            getContentPane().add(CurrentStatus);
        }

        // and prep for display
        pack();

        writeSettingsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent a) {
                writeLV102Settings();
                writeSettingsButton.setSelected(false);
            }
        }
        );

        // install reset button handler
        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent a) {
                resetLV102Settings();
                resetButton.setSelected(false);
            }
        }
        );

        // install reset to defaults button handler
        defaultButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent a) {
                defaultLV102Settings();
                defaultButton.setSelected(false);
            }
        }
        );

        // install a handler to set the status line when the selected item 
        // changes in the e-Line box
        eLineBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent a) {
                synchronized (CurrentStatus) {
                    CurrentStatus.setText(Bundle.getMessage("LV102StatusChanged"));
                    if (log.isDebugEnabled()) {
                        log.debug("Current Status: " + Bundle.getMessage("LV102StatusChanged"));
                    }
                }
            }
        }
        );

        // install a handler to set the status line when the selected item 
        // changes in the RailCom box
        railComBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent a) {
                synchronized (CurrentStatus) {
                    CurrentStatus.setText(Bundle.getMessage("LV102StatusChanged"));
                    if (log.isDebugEnabled()) {
                        log.debug("Current Status: " + Bundle.getMessage("LV102StatusChanged"));
                    }
                }
            }
        }
        );

        // install a handler to set the status line when the selected item 
        // changes in the RailComMode box
        railComModeBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent a) {
                synchronized (CurrentStatus) {
                    CurrentStatus.setText(Bundle.getMessage("LV102StatusChanged"));
                    if (log.isDebugEnabled()) {
                        log.debug("Current Status: " + Bundle.getMessage("LV102StatusChanged"));
                    }
                }
            }
        }
        );

        // install a handler to set the status line when the selected item 
        // changes in the RailComTiming box
        railComTimingBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent a) {
                synchronized (CurrentStatus) {
                    CurrentStatus.setText(Bundle.getMessage("LV102StatusChanged"));
                    if (log.isDebugEnabled()) {
                        log.debug("Current Status: " + Bundle.getMessage("LV102StatusChanged"));
                    }
                }
            }
        }
        );

        // install a handler to set the status line when the selected item 
        // changes in the volt box
        voltBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent a) {
                synchronized (CurrentStatus) {
                    CurrentStatus.setText(Bundle.getMessage("LV102StatusChanged"));
                    if (log.isDebugEnabled()) {
                        log.debug("Current Status: " + Bundle.getMessage("LV102StatusChanged"));
                    }
                }
            }
        }
        );

        // configure internal frame options
        setClosable(false);  // don't let the user close this frame
        setResizable(false);  // don't let the user resize this frame
        setIconifiable(false); // don't let the user minimize this frame
        setMaximizable(false); // don't let the user maximize this frame

        // make the internal frame visible
        this.setVisible(true);
    }

    boolean read = false;

    JComboBox<String> voltBox = new javax.swing.JComboBox<String>();
    JComboBox<String> eLineBox = new javax.swing.JComboBox<String>();
    JComboBox<String> railComBox = new javax.swing.JComboBox<String>();
    JComboBox<String> railComModeBox = new javax.swing.JComboBox<String>();
    JComboBox<String> railComTimingBox = new javax.swing.JComboBox<String>();

    JLabel CurrentStatus = new JLabel(" ");

    JToggleButton writeSettingsButton = new JToggleButton(Bundle.getMessage("LV102WriteSettingsButtonLabel"));
    JToggleButton resetButton = new JToggleButton(Bundle.getMessage("LV102ResetButtonLabel"));
    JToggleButton defaultButton = new JToggleButton("LV102DefaultButtonLabel");

    protected String[] validVoltage = new String[]{"11V", "11.5V", "12V", "12.5V", "13V", "13.5V", "14V", "14.5V", "15V", "15.5V", "16V (factory default)", "16.5V", "17V", "17.5V", "18V", "18.5V", "19V", "19.5V", "20V", "20.5V", "21V", "21.5V", "22V", ""};
    protected int[] validVoltageValues = new int[]{22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 0};

    protected String[] validELineStatus = new String[]{Bundle.getMessage("LV102ELineActive"), Bundle.getMessage("LV102ELineInactive"), Bundle.getMessage("LV102ELineDefault"), ""};
    protected int[] validELineStatusValues = new int[]{90, 91, 99, 0};

    protected String[] validRailComStatus = new String[]{Bundle.getMessage("LV102RailComActive"), Bundle.getMessage("LV102RailComInactive"), ""};
    protected int[] validRailComStatusValues = new int[]{93, 92, 0};

    protected String[] validRailComMode = new String[]{Bundle.getMessage("LV102RailCom3BitMode"), Bundle.getMessage("LV102RailCom4BitMode"), ""};
    protected int[] validRailComModeValues = new int[]{94, 95, 0};

    protected String[] validRailComTiming = new String[]{Bundle.getMessage("LV102RailComDefaultTiming"), Bundle.getMessage("LV102RailComNCETiming"), Bundle.getMessage("LV102RailComIncreaseTiming"), Bundle.getMessage("LV102RailComDecreaseTiming"), ""};
    protected int[] validRailComTimingValues = new int[]{88, 89, 70, 71, 0};

    //Send Power Station settings
    void writeLV102Settings() {

        // obtain the programmer Manager
        jmri.AddressedProgrammerManager pm = jmri.InstanceManager.getNullableDefault(jmri.AddressedProgrammerManager.class);
        if(pm == null) {
           // no addressed programmer manager, cannot proceed.
           CurrentStatus.setText(Bundle.getMessage("LV102StatusNoPM"));
           log.error("No Programmer Manager Available, cannot configure LV102");
           return;
        }


        // Obtain an ops mode programmer instance
        AddressedProgrammer opsProg = pm.getAddressedProgrammer(false, 00);

        if(opsProg == null) {
           // no ops mode programmer programmer, cannot proceed.
           CurrentStatus.setText(Bundle.getMessage("LV102StatusNoPOM"));
           log.error("Failed to obtain Operations Mode Programmer, cannot configure LV102");
           return;
        }

        // write the values to the power station.
        writeVoltSetting(opsProg);
        writeELineSetting(opsProg);
        writeRailComSetting(opsProg);
        writeRailComModeSetting(opsProg);
        writeRailComTimingSetting(opsProg);

        // we're done now, so we can release the programmer.
        pm.releaseAddressedProgrammer(opsProg);
    }

    // Write the voltage setting
    void writeVoltSetting(Programmer opsProg) {
        if (!(((String) voltBox.getSelectedItem()).equals(""))
                && (String) voltBox.getSelectedItem() != null) {

            if (log.isDebugEnabled()) {
                log.debug("Selected Voltage: " + voltBox.getSelectedItem());
            }
            synchronized (CurrentStatus) {
                CurrentStatus.setText(Bundle.getMessage("LV102StatusProgMode"));
                CurrentStatus.doLayout();
                if (log.isDebugEnabled()) {
                    log.debug("Current Status: " + Bundle.getMessage("LV102StatusProgMode"));
                }
                /* Pause briefly to give the user a chance to see what is 
                 happening */
                new jmri.util.WaitHandler(this,waitValue);

                /* First, send the ops mode programing command to enter
                 programing mode */
                try {
                    opsProg.writeCV(7, 50, progListener);
                } catch (ProgrammerException e) {
                    // Don't do anything with this yet
                }

                /* Pause briefly to give the booster a chance to change 
                 into It's programming mode */
                new jmri.util.WaitHandler(this,waitValue);

                CurrentStatus.setText(Bundle.getMessage("LV102StatusWriteVolt"));
                CurrentStatus.doLayout();
                if (log.isDebugEnabled()) {
                    log.debug("Current Status: " + Bundle.getMessage("LV102StatusWriteVolt"));
                }

                /* Next, send the ops mode programing command for the voltage 
                 we want */
                try {
                    opsProg.writeCV(7, validVoltageValues[voltBox.getSelectedIndex()], progListener);
                } catch (ProgrammerException e) {
                    // Don't do anything with this yet
                }

                /* Pause briefly to wait for the programmer to send back a 
                 reply */
                new jmri.util.WaitHandler(this,waitValue);

            }  // End of synchronized(CurrentStatus) block for voltage setting
        } else {
            if (log.isDebugEnabled()) {
                log.debug("No Voltage Selected");
            }
        }
    }

    // Write the E-Line setting
    void writeELineSetting(Programmer opsProg) {
        if (!(((String) eLineBox.getSelectedItem()).equals(""))
                && (String) eLineBox.getSelectedItem() != null) {

            if (log.isDebugEnabled()) {
                log.debug("E-Line Setting: " + eLineBox.getSelectedItem());
            }
            synchronized (CurrentStatus) {
                CurrentStatus.setText(Bundle.getMessage("LV102StatusProgMode"));
                CurrentStatus.doLayout();
                if (log.isDebugEnabled()) {
                    log.debug("Current Status: " + Bundle.getMessage("LV102StatusProgMode"));
                }

                /* Pause briefly to give the user a chance to see what is 
                 happening */
                new jmri.util.WaitHandler(this,waitValue);

                /* First, send the ops mode programing command to enter
                 programing mode */
                try {
                    opsProg.writeCV(7, 50, progListener);
                } catch (ProgrammerException e) {
                    // Don't do anything with this yet
                }

                /* Pause briefly to give the booster a chance to change 
                 into It's programming mode */
                new jmri.util.WaitHandler(this,waitValue);

                CurrentStatus.setText(Bundle.getMessage("LV102StatusWriteELine"));
                CurrentStatus.doLayout();
                if (log.isDebugEnabled()) {
                    log.debug("Current Status: " + Bundle.getMessage("LV102StatusWriteELine"));
                }


                /* Next, send the ops mode programing command for the E line 
                 Status we want */
                try {
                    opsProg.writeCV(7, validELineStatusValues[eLineBox.getSelectedIndex()], progListener);
                } catch (ProgrammerException e) {
                    // Don't do anything with this yet
                }

                /* Pause briefly to wait for the programmer to send back a 
                 reply */
                new jmri.util.WaitHandler(this,waitValue);

            } // End of synchronized(CurrentStatus) block for E-line setting
        } else {
            if (log.isDebugEnabled()) {
                log.debug("No E-Line value Selected");
            }
        }

    }

    // Write the RailCom setting
    void writeRailComSetting(Programmer opsProg) {
        if (!(((String) railComBox.getSelectedItem()).equals(""))
                && (String) railComBox.getSelectedItem() != null) {

            if (log.isDebugEnabled()) {
                log.debug("RailCom Setting: " + railComBox.getSelectedItem());
            }
            synchronized (CurrentStatus) {
                CurrentStatus.setText(Bundle.getMessage("LV102StatusProgMode"));
                CurrentStatus.doLayout();
                if (log.isDebugEnabled()) {
                    log.debug("Current Status: " + Bundle.getMessage("LV102StatusProgMode"));
                }

                /* Pause briefly to give the user a chance to see what is 
                 happening */
                new jmri.util.WaitHandler(this,waitValue);

                /* First, send the ops mode programing command to enter
                 programing mode */
                try {
                    opsProg.writeCV(7, 50, progListener);
                } catch (ProgrammerException e) {
                    // Don't do anything with this yet
                }

                /* Pause briefly to give the booster a chance to change 
                 into It's programming mode */
                new jmri.util.WaitHandler(this,waitValue);

                CurrentStatus.setText(Bundle.getMessage("LV102StatusWriteRailCom"));
                CurrentStatus.doLayout();
                if (log.isDebugEnabled()) {
                    log.debug("Current Status: " + Bundle.getMessage("LV102StatusWriteRailCom"));
                }

                /* Next, send the ops mode programing command for the RailComm
                 Status we want */
                try {
                    opsProg.writeCV(7, validRailComStatusValues[railComBox.getSelectedIndex()], progListener);
                } catch (ProgrammerException e) {
                    // Don't do anything with this yet
                }

                /* Pause briefly to wait for the programmer to send back a 
                 reply */
                new jmri.util.WaitHandler(this,waitValue);

            } // End of synchronized(CurrentStatus) block for RailCom Setting
        } else {
            if (log.isDebugEnabled()) {
                log.debug("No RailCom value Selected");
            }
        }
    }

    // Write the RailCom Mode setting
    void writeRailComModeSetting(Programmer opsProg) {
        if (!(((String) railComModeBox.getSelectedItem()).equals(""))
                && (String) railComModeBox.getSelectedItem() != null) {

            if (log.isDebugEnabled()) {
                log.debug("RailCom Setting: " + railComModeBox.getSelectedItem());
            }
            synchronized (CurrentStatus) {
                CurrentStatus.setText(Bundle.getMessage("LV102StatusProgMode"));
                CurrentStatus.doLayout();
                if (log.isDebugEnabled()) {
                    log.debug("Current Status: " + Bundle.getMessage("LV102StatusProgMode"));
                }

                /* Pause briefly to give the user a chance to see what is 
                 happening */
                new jmri.util.WaitHandler(this,waitValue);

                /* First, send the ops mode programing command to enter
                 programing mode */
                try {
                    opsProg.writeCV(7, 50, progListener);
                } catch (ProgrammerException e) {
                    // Don't do anything with this yet
                }

                /* Pause briefly to give the booster a chance to change 
                 into It's programming mode */
                new jmri.util.WaitHandler(this,waitValue);

                CurrentStatus.setText(Bundle.getMessage("LV102StatusWriteRailComMode"));
                CurrentStatus.doLayout();
                if (log.isDebugEnabled()) {
                    log.debug("Current Status: " + Bundle.getMessage("LV102StatusWriteRailComMode"));
                }

                /* Next, send the ops mode programing command for the RailCom Mode
                 Status we want */
                try {
                    opsProg.writeCV(7, validRailComModeValues[railComModeBox.getSelectedIndex()], progListener);
                } catch (ProgrammerException e) {
                    // Don't do anything with this yet
                }

                /* Pause briefly to wait for the programmer to send back a 
                 reply */
                new jmri.util.WaitHandler(this,waitValue);

            } // End of synchronized(CurrentStatus) block for RailCom Mode
        } else {
            if (log.isDebugEnabled()) {
                log.debug("No RailCom Mode Selected");
            }
        }
    }

    // Write the RailCom Mode setting
    void writeRailComTimingSetting(Programmer opsProg) {
        if (!(((String) railComTimingBox.getSelectedItem()).equals(""))
                && (String) railComTimingBox.getSelectedItem() != null) {

            if (log.isDebugEnabled()) {
                log.debug("RailCom Timing Setting: " + railComTimingBox.getSelectedItem());
            }
            synchronized (CurrentStatus) {
                CurrentStatus.setText(Bundle.getMessage("LV102StatusProgMode"));
                CurrentStatus.doLayout();
                if (log.isDebugEnabled()) {
                    log.debug("Current Status: " + Bundle.getMessage("LV102StatusProgMode"));
                }

                /* Pause briefly to give the user a chance to see what is 
                 happening */
                new jmri.util.WaitHandler(this,waitValue);

                /* First, send the ops mode programing command to enter
                 programing mode */
                try {
                    opsProg.writeCV(7, 50, progListener);
                } catch (ProgrammerException e) {
                    // Don't do anything with this yet
                }

                /* Pause briefly to give the booster a chance to change 
                 into It's programming mode */
                new jmri.util.WaitHandler(this,waitValue);

                CurrentStatus.setText(Bundle.getMessage("LV102StatusWriteRailComMode"));
                CurrentStatus.doLayout();
                if (log.isDebugEnabled()) {
                    log.debug("Current Status: " + Bundle.getMessage("LV102StatusWriteRailComMode"));
                }

                /* Next, send the ops mode programing command for the RailCom 
                 Timing we want */
                try {
                    opsProg.writeCV(7, validRailComTimingValues[railComTimingBox.getSelectedIndex()], progListener);
                } catch (ProgrammerException e) {
                    // Don't do anything with this yet
                }

                /* Pause briefly to wait for the programmer to send back a 
                 reply */
                new jmri.util.WaitHandler(this,waitValue);

            } // End of synchronized(CurrentStatus) block for RailCom Mode
        } else {
            if (log.isDebugEnabled()) {
                log.debug("No RailCom Timing Mode Selected");
            }
        }
    }

    // Set to LV102 default values.  Voltage is 16, E Line is Active, 
    // Railcom is innactive, Railcom Mode is 3 bit cutout.
    void defaultLV102Settings() {
        voltBox.setSelectedIndex(10);
        eLineBox.setSelectedIndex(0);
        railComBox.setSelectedIndex(1);
        railComModeBox.setSelectedIndex(0);
        synchronized (CurrentStatus) {
            CurrentStatus.setText(Bundle.getMessage("LV102StatusInitial"));
            if (log.isDebugEnabled()) {
                log.debug("Current Status: " + Bundle.getMessage("LV102StatusInitial"));
            }
        }
    }

    // Set to initial values.
    void resetLV102Settings() {
        voltBox.setSelectedIndex(23);
        eLineBox.setSelectedIndex(3);
        railComBox.setSelectedIndex(2);
        railComModeBox.setSelectedIndex(2);
        synchronized (CurrentStatus) {
            CurrentStatus.setText(Bundle.getMessage("LV102StatusOK"));
            if (log.isDebugEnabled()) {
                log.debug("Current Status: " + Bundle.getMessage("LV102StatusOK"));
            }
        }
    }

    @Override
    public void dispose() {
        // take apart the JInternalFrame
        super.dispose();
    }

    private class progReplyListener implements Runnable, jmri.ProgListener {

        //private Object parent = null;
        progReplyListener(Object Parent) {
            //parent = Parent; 
        }

        @Override
        public void run() {
        }

        /**
         * This class is a programmer listener, so we implement the
         * programmingOpReply() function
         */
        @SuppressFBWarnings(value = "NO_NOTIFY_NOT_NOTIFYALL", justification = "There should only ever be one thread waiting for this method.")

        @Override
        public void programmingOpReply(int value, int status) {
            if (log.isDebugEnabled()) {
                log.debug("Programming Operation reply received, value is " + value + " ,status is " + status);
            }
            if (status == ProgListener.ProgrammerBusy) {
                synchronized (CurrentStatus) {
                    CurrentStatus.setText(Bundle.getMessage("LV102StatusBUSY"));
                    if (log.isDebugEnabled()) {
                        log.debug("Current Status: " + Bundle.getMessage("LV102StatusBUSY"));
                    }
                    CurrentStatus.notify();
                }
            } else if (status == ProgListener.OK) {
                if (CurrentStatus.getText().equals(Bundle.getMessage("LV102StatusProgMode"))) {
                    synchronized (CurrentStatus) {
                        CurrentStatus.setText(Bundle.getMessage("LV102StatusReadyProg"));
                        if (log.isDebugEnabled()) {
                            log.debug("Current Status: " + Bundle.getMessage("LV102StatusReadyProg"));
                        }
                        CurrentStatus.notify();
                    }
                } else {
                    synchronized (CurrentStatus) {
                        CurrentStatus.setText(Bundle.getMessage("LV102StatusWritten"));
                        if (log.isDebugEnabled()) {
                            log.debug("Current Status: " + Bundle.getMessage("LV102StatusWritten"));
                        }
                        CurrentStatus.notify();
                    }
                }
            } else {
                synchronized (CurrentStatus) {
                    CurrentStatus.setText(Bundle.getMessage("LV102StatusUnknown"));
                    if (log.isDebugEnabled()) {
                        log.debug("Current Status: " + Bundle.getMessage("LV102StatusUnknown"));
                    }
                    CurrentStatus.notify();
                }
            }
        }
    }

    private final static Logger log = LoggerFactory.getLogger(LV102InternalFrame.class.getName());

}
